package com.iwhalecloud.bss.uba.common.log;

import com.google.common.util.concurrent.RateLimiter;
import com.ztesoft.zsmart.core.log.ZSmartLogger;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 异步日志写入器，实现了生产者-消费者模式.
 * 1. 支持按对象类型分队列存储.
 * 2. 支持按队列配置容量和内存限制.
 * 3. 支持按对象类型配置TPS（每秒事务数）限流.
 * 4. 通过策略模式支持自定义写入逻辑.
 * 5. 异步单线程消费，保证日志先进先出顺序写入.
 */
public class LogWriter {

    private static final ZSmartLogger logger = ZSmartLogger.getLogger(LogWriter.class);

    // 存储不同日志类型的队列
    private final Map<Class<?>, BlockingQueue<Object>> queues = new ConcurrentHashMap<>();
    // 存储不同日志类型的TPS速率限制器
    private final Map<Class<?>, RateLimiter> rateLimiters = new ConcurrentHashMap<>();
    // 存储不同日志类型的持久化策略
    private final Map<Class<?>, LogPersistenceStrategy> persistenceStrategies = new ConcurrentHashMap<>();
    // 每个日志类型一个单独的线程池，确保隔离和顺序处理
    private final Map<Class<?>, ExecutorService> writerThreads = new ConcurrentHashMap<>();

    private volatile boolean running = true;

    /**
     * 初始化方法，在此处注册所有需要处理的日志类型.
     * 如果使用Spring, @PostConstruct注解会自动调用此方法.
     */
    public void init() {
        logger.info("Initializing LogWriter...");
        // 注册LogInfo及其所有内部类
        registerLogType(LogInfo.class, 10000, 100.0, new DefaultLog4jStrategy());
        registerLogType(LogInfo.RequestInfo.class, 5000, 50.0, new DefaultLog4jStrategy());
        registerLogType(LogInfo.ReqParamInfo.class, 5000, 50.0, new DefaultLog4jStrategy());
        registerLogType(LogInfo.ResponseInfo.class, 5000, 50.0, new DefaultLog4jStrategy());
        registerLogType(LogInfo.RespParamInfo.class, 5000, 50.0, new DefaultLog4jStrategy());

        startConsumers();
        logger.info("LogWriter initialized and consumer threads started.");
    }

    /**
     * 注册一个日志类型，并为其配置队列、TPS和写入策略.
     *
     * @param logType           日志对象的Class
     * @param queueCapacity     队列容量 (基于对象数量，可根据平均对象大小估算内存)
     * @param tps               每秒允许写入的次数 (TPS)
     * @param strategy          该类型日志的写入策略
     */
    public void registerLogType(Class<?> logType, int queueCapacity, double tps, LogPersistenceStrategy strategy) {
        // 1. 创建队列
        queues.putIfAbsent(logType, new LinkedBlockingQueue<>(queueCapacity));

        // 2. 创建TPS限流器
        // RateLimiter.create会创建一个平滑预热的限流器，防止启动时流量冲击
        rateLimiters.putIfAbsent(logType, RateLimiter.create(tps));

        // 3. 注册写入策略
        persistenceStrategies.putIfAbsent(logType, strategy);

        // 4. 为该类型创建一个单线程的Executor，保证FIFO处理
        writerThreads.putIfAbsent(logType, Executors.newSingleThreadExecutor(
                r -> new Thread(r, "log-writer-" + logType.getSimpleName()))
        );

        logger.info("Registered log type: {}, QueueCapacity: {}, TPS: {}", logType.getSimpleName(), queueCapacity, tps);
    }

    /**
     * 外部调用此方法将日志数据写入队列 (生产者).
     *
     * @param logData 要写入的日志对象
     */
    public void write(Object logData) {
        if (logData == null) {
            return;
        }

        Class<?> logType = logData.getClass();
        BlockingQueue<Object> queue = queues.get(logType);

        if (queue == null) {
            logger.warn("Received log for an unregistered type: {}. Please register it first.", logType.getSimpleName());
            return;
        }

        // offer方法是非阻塞的，如果队列已满，它会立即返回false
        boolean offered = queue.offer(logData);

        if (!offered) {
            // 要求1: 队列超出容量时打印告警日志
            logger.warn("Log queue for type '{}' is full (capacity: {}). Dropping new log. Please check consumer performance or increase queue capacity.",
                    logType.getSimpleName(), queue.size());
        }
    }

    /**
     * 启动所有消费者线程.
     */
    private void startConsumers() {
        for (Map.Entry<Class<?>, ExecutorService> entry : writerThreads.entrySet()) {
            Class<?> logType = entry.getKey();
            ExecutorService executor = entry.getValue();
            
            BlockingQueue<Object> queue = queues.get(logType);
            RateLimiter limiter = rateLimiters.get(logType);
            LogPersistenceStrategy strategy = persistenceStrategies.get(logType);

            executor.submit(() -> {
                logger.info("Consumer thread started for type: {}", logType.getSimpleName());
                while (running) {
                    try {
                        // 要求2: 按照先进先出的原则进行写 (take方法保证了FIFO)
                        Object logData = queue.take(); // take方法在队列为空时会阻塞

                        // 要求3: 控制写速度
                        limiter.acquire(); // 获取一个令牌，如果令牌桶中没有，则阻塞直到有为止

                        // 要求4: 使用策略执行实际写入
                        strategy.persist(logData);

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        logger.warn("Log writer thread for {} was interrupted.", logType.getSimpleName());
                        break; // 退出循环
                    } catch (Exception e) {
                        // 捕获写入策略中可能出现的异常，防止线程死亡
                        logger.error("Error while persisting log for type {}", logType.getSimpleName(), e);
                    }
                }
                logger.info("Consumer thread stopped for type: {}", logType.getSimpleName());
            });
        }
    }

    /**
     * 优雅停机.
     * 如果使用Spring, @PreDestroy注解会在容器销毁bean之前调用此方法.
     */
    @PreDestroy
    public void shutdown() {
        logger.info("Shutting down LogWriter...");
        this.running = false;

        writerThreads.values().forEach(executor -> {
            executor.shutdown(); // 禁止提交新任务
            try {
                // 等待最多5秒让现有任务执行完毕
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow(); // 强制停止
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        });

        // 处理队列中剩余的日志
        processRemainingLogs();
        logger.info("LogWriter shut down complete.");
    }

    /**
     * 在关闭时，处理队列中剩余的日志.
     */
    private void processRemainingLogs() {
        logger.info("Processing remaining logs in queues before final shutdown...");
        queues.forEach((logType, queue) -> {
            LogPersistenceStrategy strategy = persistenceStrategies.get(logType);
            if (strategy != null) {
                Object logData;
                int count = 0;
                while ((logData = queue.poll()) != null) { // poll是非阻塞的
                    try {
                        strategy.persist(logData);
                        count++;
                    } catch (Exception e) {
                        logger.error("Error persisting remaining log on shutdown for type {}", logType.getSimpleName(), e);
                    }
                }
                if (count > 0) {
                    logger.info("Persisted {} remaining logs for type {}.", count, logType.getSimpleName());
                }
            }
        });
    }
}