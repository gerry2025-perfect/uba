package com.iwhalecloud.bss.uba.adapter.log;


import com.ztesoft.zsmart.core.log.UbaLoggerFactory;
import com.ztesoft.zsmart.core.log.ZSmartLogger;

public class UbaLogger {

    private final ZSmartLogger logger;

    private static final String UBA_LOGGER_FQCN = UbaLogger.class.getName();

    private UbaLogger(ZSmartLogger logger) {
        this.logger = logger;
    }

    public static UbaLogger getLogger(Class<?> clazz) {
        return new UbaLogger(UbaLoggerFactory.getLogger(clazz.getName(), UBA_LOGGER_FQCN));
    }

    public static UbaLogger getLogger(String loggerName) {
        return new UbaLogger(UbaLoggerFactory.getLogger(loggerName, UBA_LOGGER_FQCN));
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
