package com.iwhalecloud.bss.uba.common.prop;

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
    /**配置项key：dubbo对应的zk地址*/
    public static String dubboAddressKey = ".dubbo.zk.address";
    /**配置项key：dubbo超时时间*/
    public static String dubboTimeoutKey = ".dubbo.timeout.time";
    /**配置项key：dubbo超时时间*/
    public static String dubboGroupKey = ".dubbo.group.name";
    /**配置项key：默认dubbo服务器code*/
    public static String dubboDefaultServerCode = "default";
    /**配置项key：默认MQ服务器code*/
    public static String mqDefaultServerCode = "default_mq";
    /**配置项key：默认file配置*/
    public static String fileDefaultCode = "default_file";
    /**配置项key：默认dubbo服务器code*/
    public static String mqDefaultAddrKey = "ftf.mq.namesrv-addr";

    /**配置项key：MQ消费者名称*/
    public static String mqConsumerNameKey = APP_CODE + ".mq.consumer.name";
    /**配置项key：MQ生产者清单*/
    public static String mqProviderListKey = APP_CODE + ".mq.provider.list";
    /**配置项key：MQ生产者地址*/
    public static String mqProviderAddrKey = "mq.provider.%s.NamesrvAddr";
    /**配置项key：MQ生产者topic*/
    public static String mqProviderTopicKey = "mq.provider.%s.topic.list";
    /**配置项key：MQ类型，可以是zmq,ctgmq,alimq或kafka*/
    public static String mqProviderTypeKey = "mq.provider.%s.mq.type";
    /**配置项key：MQ服务器用户名*/
    public static String mqProviderAccessKey = "mq.provider.%s.access.key";
    /**配置项key：MQ服务器密码*/
    public static String mqProviderSecretKey = "mq.provider.%s.secret.key";

    /**配置项key：MQ生产者名称*/
    public static String mqProviderNameKey = APP_CODE + ".mq.provider.name";
    /**配置项key：MQ服务器清单*/
    public static String mqServerListKey = APP_CODE + ".mq.server.list";
    /**配置项key：MQ服务器地址*/
    public static String mqServerAddrKey = "mq.server.%s.NamesrvAddr";
    /**配置项key：MQ类型，可以是zmq,ctgmq,alimq或kafka*/
    public static String mqServerTypeKey = "mq.server.%s.mq.type";
    /**配置项key：MQ服务器用户名*/
    public static String mqServerAccessKey = "mq.server.%s.access.key";
    /**配置项key：MQ服务器密码*/
    public static String mqServerSecretKey = "mq.server.%s.secret.key";

}
