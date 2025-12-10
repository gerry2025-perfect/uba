package com.iwhalecloud.bss.uba.comm.prop;

import java.util.regex.Pattern;

/**
 * 定义整个模块层面的常量
 * */
public class CommonConst {

    /**rest请求的版本号*/
    public static final String RESTFUL_VERSION = "v1";
    /**rest请求的上下文路径*/
    public static final String APP_CODE = "uba";
    /**系统默认字符集*/
    public static final String DEFAULT_CHARSET = "UTF-8";
    /**向BTC发送处理结果的topic*/
    public static final String BTC_RESPONSE_TOPIC = "B_respTopic";

    /**配置项key：dubbo客户端名称*/
    public static String dubboAppNameKey = APP_CODE + ".dubbo.app.name";
    /**配置项key：dubbo QOS端口*/
    public static String dubboQosPortKey = APP_CODE + ".dubbo.qos.port";
    /**配置项key：默认dubbo服务器code*/
    public static String dubboDefaultServerCode = "default";
    /**配置项key：默认MQ服务器code*/
    public static String mqDefaultServerCode = "default";
    /**配置项key：默认file配置*/
    public static String fileDefaultCode = "default";
    /**配置项key：默认restful站点code*/
    public static String restDefaultSiteCode = "default";
    /**配置项key：默认dubbo服务器code*/
    public static String mqDefaultAddrKey = "ftf.mq.namesrv-addr";

    /**配置项key：MQ消费者名称*/
    public static String mqConsumerNameKey = APP_CODE + ".mq.consumer.name";
    /**配置项key：MQ生产者名称*/
    public static String mqProviderNameKey = APP_CODE + ".mq.provider.name";

    /**参数替换的正则表达式*/
    public static Pattern PATTERN = Pattern.compile("\\$\\{([^}]+)}");

}
