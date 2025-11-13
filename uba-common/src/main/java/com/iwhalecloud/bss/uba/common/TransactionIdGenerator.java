package com.iwhalecloud.bss.uba.common;

import com.iwhalecloud.bss.uba.common.config.SpringHolder;
import com.iwhalecloud.bss.uba.common.log.LogGenerator;
import org.apache.commons.lang3.StringUtils;

import java.lang.management.ManagementFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 生成唯一的16位交易流水ID。
 * ID构成: 时间戳 (9位) + 实例ID (2位) + 序列号 (5位) = 16位
 * 所有部分都使用Base36编码以在有限长度内表示更多信息。
 */
public class TransactionIdGenerator {

    /**
     * 实例ID部分。通过启动参数 -Dinstance.id=01 指定。
     * 必须是两位，可以是数字或字母。这允许 36*36 = 1296 个实例。
     */
    private static String INSTANCE_ID;

    /**
     * 序列号计数器，使用原子类确保线程安全。
     */
    private static final AtomicLong sequence = new AtomicLong(0);

    /**
     * 序列号部分的最大值 (36^5 - 1)，对应5位Base36最大值。
     * 每毫秒内可生成超过6000万个ID，足以保证唯一性。
     */
    private static final long SEQUENCE_MAX = 60466175L; // 36^5 - 1

    static {
        INSTANCE_ID = System.getProperty("z_app");
        if (StringUtils.isBlank(INSTANCE_ID)) {
            // 如果没有通过系统属性指定，尝试从JMX名称中获取一个值（例如PID），但这在跨主机时不能保证唯一。
            // 最佳实践是始终通过 -Dinstance.id=... 来指定。
            String jvmName = ManagementFactory.getRuntimeMXBean().getName();
            String[] parts = jvmName.split("@");
            INSTANCE_ID = parts[0]; // 使用PID并补零
        }
    }

    /**
     * 生成一个分布式环境唯一的16位字符串ID。
     *
     * @return 16位唯一ID
     */
    public synchronized static String generate() {

        // 1. 时间戳部分 (9位)
        // 使用Base36编码，可以表示到2058年
        String timestampPart = StringUtils.leftPad(Long.toString(System.currentTimeMillis(), 36), 9, '0');

        // 2. 实例ID部分 (2位)
        // 已在静态块中初始化
        String instancePart = INSTANCE_ID;

        // 3. 序列号部分 (5位)
        // 获取当前序列值，并在达到最大值时回绕。
        long currentSequence = sequence.getAndIncrement() % SEQUENCE_MAX;
        String sequencePart = StringUtils.leftPad(Long.toString(currentSequence, 36), 5, '0');

        return timestampPart + instancePart + sequencePart;
    }
}
