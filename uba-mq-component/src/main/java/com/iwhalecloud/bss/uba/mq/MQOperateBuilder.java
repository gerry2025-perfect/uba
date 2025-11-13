package com.iwhalecloud.bss.uba.mq;

import com.iwhalecloud.bss.uba.common.CommonUtils;
import com.iwhalecloud.bss.uba.common.magic.MagicRunner;
import com.iwhalecloud.bss.uba.common.prop.CommonConst;
import com.iwhalecloud.bss.uba.common.prop.PropertyHolder;
import com.iwhalecloud.bss.uba.mq.consume.MagicConsumeHandler;
import com.iwhalecloud.bss.uba.mq.magic.resource.MessageQueueInfo;
import com.iwhalecloud.bss.uba.mq.magic.resource.MessageQueueMagicDynamicRegistry;
import com.ztesoft.zsmart.core.log.ZSmartLogger;
import com.ztesoft.zsmart.core.mq.CoreMQClientFactory;
import com.ztesoft.zsmart.core.mq.common.MQProperties;
import com.ztesoft.zsmart.core.mq.common.exception.CoreMQClientApiException;
import com.ztesoft.zsmart.core.mq.config.CoreMqConfigUtil;
import com.ztesoft.zsmart.core.mq.consumer.CoreConsumer;
import com.ztesoft.zsmart.core.mq.consumer.CoreMQMessageHandler;
import com.ztesoft.zsmart.core.mq.consumer.CoreSubscription;
import com.ztesoft.zsmart.core.mq.producer.CoreProducer;

import java.util.*;

/**构造MQ的环境配置信息，可能来源于配置项，也可能来源于数据库*/
public class MQOperateBuilder {

    private static ZSmartLogger logger = ZSmartLogger.getLogger(MQOperateBuilder.class);

    private Properties convertToProperties(MQProperties mqProperties) {
        Properties properties = new Properties();
        properties.setProperty("currentModule", CommonConst.APP_CODE);
        Map<String, String> propertiesMap = new HashMap<>();
        propertiesMap.put("ConsumerId", PropertyHolder.getProp(CommonConst.mqConsumerNameKey));
        propertiesMap.put("ProducerId", PropertyHolder.getProp(CommonConst.mqProviderNameKey));
        mqProperties.setProperties(propertiesMap);
        CoreMqConfigUtil.toProperties(mqProperties, properties);
        return properties;
    }

    /**初始化MQ的订阅者，但是未启动，需要实际开始订阅，要调用start方法*/
    public CoreConsumer initConsumer(MessageQueueInfo messageQueueInfo, CoreMQClientFactory mqClientFactory,
                                     MagicRunner magicRunner, MessageQueueMagicDynamicRegistry messageQueueMagicDynamicRegistry) throws CoreMQClientApiException {
        Properties properties = buildMQProperties(messageQueueInfo);
        if(logger.isDebugEnabled()){
            logger.debug("init mq consumer , properties : " + CommonUtils.convertToString(properties));
        }
        CoreConsumer consumer = mqClientFactory.createConsumer(properties);
        Map<CoreSubscription, CoreMQMessageHandler> subscriptionTable = new HashMap<>();
        messageQueueInfo.getConsumerTopics().stream().forEach(topic -> {
            CoreSubscription subscription = new CoreSubscription();
            subscription.setTopic(topic.getTopicName());
            subscription.setPullMessageSize(0); // 根据需要设置
            subscriptionTable.put(subscription, new MagicConsumeHandler(magicRunner, messageQueueInfo, messageQueueMagicDynamicRegistry));
            logger.debug(String.format("<< consumer start : provider=[%s] consumerNameServAddr=[%s] topic=[%s] ",
                    messageQueueInfo.getKey(), messageQueueInfo.getNamesrvAddr(), topic.getTopicName()));
        });
        consumer.subscriptionTable(subscriptionTable);
        return consumer;
    }

    /**初始化MQ的生产者，但是未启动，需要实际可以使用，要调用start方法*/
    public CoreProducer initProducer(MessageQueueInfo messageQueueInfo,
                                     CoreMQClientFactory mqClientFactory) throws CoreMQClientApiException {
        return mqClientFactory.createProducer(buildMQProperties(messageQueueInfo));
    }

    public Properties buildMQProperties(MessageQueueInfo messageQueueInfo) {
        MQProperties mqProperties = new MQProperties();
        mqProperties.setAccessKey(messageQueueInfo.getAccessKey());
        mqProperties.setSecretKey(messageQueueInfo.getSecretKey());
        mqProperties.setType(messageQueueInfo.getType());
        mqProperties.setNamesrvAddr(messageQueueInfo.getNamesrvAddr());
        mqProperties.setUseNewConfig(true);
        return convertToProperties(mqProperties);
    }

    /**从配置项中获取MQ配置信息，构造对应Properties实体*/
    /*public Properties buildMQConsumeProperties(String mqProvider) {
        MQProperties mqProperties = new MQProperties();
        mqProperties.setAccessKey(PropertyHolder.getProp(String.format(CommonConst.mqProviderAccessKey, mqProvider)));
        mqProperties.setSecretKey(PropertyHolder.getProp(String.format(CommonConst.mqProviderSecretKey, mqProvider)));
        mqProperties.setType(PropertyHolder.getProp(String.format(CommonConst.mqProviderTypeKey, mqProvider)));
        mqProperties.setNamesrvAddr(getMQNamesrvAddr(CommonConst.mqProviderAddrKey, mqProvider));
        mqProperties.setUseNewConfig(true);
        return convertToProperties(mqProperties);
    }*/

    /**取特定MQ主机的nameServAddr，如果没有取到，就取默认*/
    public String getMQNamesrvAddr(String specialKey, String mqServer) {
        String consumerNameServ = PropertyHolder.getProp(String.format(specialKey, mqServer));
        if ((consumerNameServ == null || consumerNameServ.trim().equals(""))) {
            consumerNameServ = PropertyHolder.getProp(CommonConst.mqDefaultAddrKey);
        }
        return consumerNameServ;
    }

}
