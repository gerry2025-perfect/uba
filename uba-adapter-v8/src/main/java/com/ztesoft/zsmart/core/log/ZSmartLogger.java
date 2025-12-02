package com.ztesoft.zsmart.core.log;

import com.ztesoft.common.logger.LogModule;

public class ZSmartLogger {

    private final com.ztesoft.zsmart.core.utils.ZSmartLogger logger;

    private ZSmartLogger(com.ztesoft.zsmart.core.utils.ZSmartLogger logger) {
        this.logger = logger;
    }

    public static ZSmartLogger getLogger(Class<?> clazz) {
        return new ZSmartLogger(com.ztesoft.zsmart.core.utils.ZSmartLogger.getLogger(clazz));
    }

    public static ZSmartLogger getLogger(String loggerName) {
        return new ZSmartLogger(com.ztesoft.zsmart.core.utils.ZSmartLogger.getLogger(loggerName));
    }

    public static ZSmartLogger getLogger(Class<?> clazz, LogModule logModule) {
        return new ZSmartLogger(com.ztesoft.zsmart.core.utils.ZSmartLogger.getLogger(clazz));
    }

    public static ZSmartLogger getLogger(String loggerName, LogModule logModule) {
        return new ZSmartLogger(com.ztesoft.zsmart.core.utils.ZSmartLogger.getLogger(loggerName));
    }

    public boolean isDebugEnabled() {
        return this.logger.isDebugEnabled();
    }

    public void debug(Object msg) {
        this.logger.debug(msg);
    }

    public void debug(Throwable t) {
        this.logger.debug(t);
    }

    public void debug(String format, Object... args) {
        this.logger.debug(format, args);
    }

    public void debug(String msg, Throwable t) {
        this.logger.debug(msg, t);
    }

    public boolean isInfoEnabled() {
        return this.logger.isInfoEnabled();
    }

    public void info(Object msg) {
        this.logger.info(msg);
    }

    public void info(Throwable t) {
        this.logger.info(t);
    }
    public void info(String format, Object... args) {
        this.logger.info(format, args);
    }
    public void info(String msg, Throwable t) {
        this.logger.info(msg, t);
    }
    public boolean isWarnEnabled() {
        return this.logger.isWarnEnabled();
    }
    public void warn(Object msg) {
        this.logger.warn(msg);
    }
    public void warn(Throwable t) {
        this.logger.warn(t);
    }
    public void warn(String format, Object... args) {
        this.logger.warn(format, args);
    }
    public void warn(String msg, Throwable t) {
        this.logger.warn(msg, t);
    }
    public boolean isErrorEnabled() {
        return this.logger.isErrorEnabled();
    }
    public void error(Object msg) {
        this.logger.error(msg);
    }
    public void error(Throwable t) {
        this.logger.error(t);
    }
    public void error(String format, Object... args) {
        this.logger.error(format, args);
    }
    public void error(String msg, Throwable t) {
        this.logger.error(msg, t);
    }

}
