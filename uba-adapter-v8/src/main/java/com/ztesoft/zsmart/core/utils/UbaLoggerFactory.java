package com.ztesoft.zsmart.core.utils;

import com.ztesoft.zsmart.core.utils.logging.InternalLogger;
import com.ztesoft.zsmart.core.utils.logging.InternalLoggerFactory;

/**
 * A factory to create ZSmartLogger instances with a custom FQCN.
 * This class is in the same package as ZSmartLogger to access its package-private constructor.
 * This is a workaround for the fact that ZSmartLogger.getLogger() hardcodes its own FQCN,
 * which makes it difficult to wrap.
 */
public final class UbaLoggerFactory {

    private UbaLoggerFactory() {
        // private constructor
    }

    public static ZSmartLogger getLogger(String name, String fqcn) {
        InternalLogger internalLogger = InternalLoggerFactory.getInstance(name, fqcn);
        // Call the package-private constructor of ZSmartLogger
        return new ZSmartLogger(internalLogger, name);
    }
}
