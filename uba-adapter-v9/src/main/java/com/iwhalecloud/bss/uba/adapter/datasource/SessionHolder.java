package com.iwhalecloud.bss.uba.adapter.datasource;

/**为了兼容V8，V9中可以不需要*/
public class SessionHolder {

    public static void initSession(boolean inherit){}

    public static void closeSession(boolean commit){}

}
