package com.iwhalecloud.bss.uba.adapter.comm;

import java.util.Stack;

/**顶级UBA上下文*/
public class UbaContext {

    public final static String ENV_FLAG_V9 = "V9";
    public final static String ENV_FLAG_V8 = "V8";

    /**在V8分布式数据库当前数据库名称*/
    private static ThreadLocal<String> dbName = new InheritableThreadLocal<>();
    /**标识当前环境是V8还是V9*/
    public static String EnvFlag = ENV_FLAG_V9;


    /**设置当前数据库，设置为null的时候清空当前值*/
    public static void setDbName(String dbName){
        if(dbName==null){
            UbaContext.dbName.remove();
        }else {
            UbaContext.dbName.set(dbName);
        }
    }

    /**获取当前数据库*/
    public static String getDbName(){
        return dbName.get();
    }

    public static boolean isV9(){
        return ENV_FLAG_V9.equals(EnvFlag);
    }

    public static boolean isV8(){
        return ENV_FLAG_V8.equals(EnvFlag);
    }

}
