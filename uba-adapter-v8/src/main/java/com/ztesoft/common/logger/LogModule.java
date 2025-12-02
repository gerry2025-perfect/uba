package com.ztesoft.common.logger;

import java.io.Serializable;

public class LogModule implements Serializable {
    private static final long serialVersionUID = 1L;
    private String logModule;

    public LogModule() {
    }

    public LogModule(String logModule) {
        this.logModule = logModule;
    }

    public String getLogModule() {
        return this.logModule;
    }

    public void setLogModule(String logModule) {
        this.logModule = logModule;
    }

    public String toString() {
        return this.logModule;
    }
}
