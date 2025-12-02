package com.iwhalecloud.bss.uba.common.releaser;

import com.iwhalecloud.bss.uba.adapter.log.UbaLogger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**自动释放器*/
public class AutoReleaser {

    private static final UbaLogger logger = UbaLogger.getLogger(AutoReleaser.class);

    private static Map<IReleasable, Long> lastOperateMap = new ConcurrentHashMap<>();

    /**设置对象最后一次操作时间*/
    public static void updateLastOperateTime(IReleasable releasable, boolean isClear) {
        if(!isClear) {
            lastOperateMap.put(releasable, System.currentTimeMillis());
        }else {
            lastOperateMap.remove(releasable);
        }

    }

    Thread recyclerThread;
    // 线程运行标记（用于优雅停止）
    private volatile boolean isRunning = false;
    // 回收周期（1分钟，单位：毫秒）
    private static final long RECYCLE_PERIOD = TimeUnit.MINUTES.toMillis(1);

    /**
     * Spring 初始化完成后自动启动回收线程
     */
    public void init() {
        isRunning = true;
        // 创建回收线程
        // 资源回收线程
        recyclerThread = new Thread(this::releaseLoop, "auto-release-resource-thread");
        recyclerThread.setDaemon(true); // 设为守护线程，避免阻塞JVM退出
        recyclerThread.start();
        logger.debug(String.format("release resource thread is started, period is %dms", RECYCLE_PERIOD));
    }

    /**
     * Spring 容器关闭的时候需要关闭线程
     * */
    public void stop() {
        isRunning = false;
        if (recyclerThread != null) {
            recyclerThread.interrupt(); // 中断休眠，唤醒线程退出循环
            try {
                recyclerThread.join(1000); // 等待线程退出
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 回收线程的主循环：定时执行回收逻辑
     */
    private void releaseLoop() {
        while (isRunning) {
            try {
                // 执行资源回收
                releaseResources();
                // 休眠指定周期（若被中断则退出循环）
                Thread.sleep(RECYCLE_PERIOD);
            } catch (InterruptedException e) {
                // 捕获中断信号，退出循环
                Thread.currentThread().interrupt();
                logger.debug("release resource thread is be interrupted");
                break;
            } catch (Exception e) {
                // 防止异常导致线程退出
                logger.error(String.format("release resource error:%s" , e.getMessage()), e);
            }
        }
        logger.debug("release resource thread is stoped");
    }

    /**具体处理资源移除*/
    private void releaseResources() {
        logger.debug(String.format("Attempted to automatically release resources, attempted to release %d objects", lastOperateMap.size()));
        Set<IReleasable> needRemoves = new LinkedHashSet<>();
        lastOperateMap.entrySet().forEach(releasableEntry -> {
            if(releasableEntry.getValue()!=null && releasableEntry.getValue()>0
                    &&  System.currentTimeMillis()-releasableEntry.getValue()>releasableEntry.getKey().timeout()){
                releasableEntry.getKey().release(true);
                logger.debug(String.format("Auto release resource for %s", releasableEntry.getKey()));
                // updateLastOperateTime(releasable, true);//自动释放连接之后，将最后操作时间清空，不再进入自动释放的范畴
                if(releasableEntry.getKey().removeSelf()){
                    needRemoves.add(releasableEntry.getKey());// 加入到待删除的列表中
                };
            }
        });
        needRemoves.forEach(releasable -> {
            lastOperateMap.remove(releasable);
        });
    }


}
