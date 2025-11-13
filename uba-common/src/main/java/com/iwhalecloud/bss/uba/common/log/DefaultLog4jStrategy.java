package com.iwhalecloud.bss.uba.common.log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ztesoft.zsmart.core.log.ZSmartLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 默认的日志写入策略，将日志对象序列化为JSON字符串并使用Log4j(SLF4J)写入.
 */
public class DefaultLog4jStrategy implements LogPersistenceStrategy {

    // 使用一个专用的logger来写入结构化日志，方便在log4j配置中单独路由到指定文件
    private static final ZSmartLogger logger = ZSmartLogger.getLogger("MagicLog");
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void persist(Object logData) {
        try {
            logger.info(objectMapper.writeValueAsString(logData));
        } catch (Exception e) {
            // 记录序列化错误
            LoggerFactory.getLogger(DefaultLog4jStrategy.class).error("Failed to serialize log data to JSON", e);
        }
    }
}