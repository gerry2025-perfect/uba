package com.iwhalecloud.bss.uba.mq.config;

import com.iwhalecloud.bss.uba.common.magic.MagicRunner;
import com.iwhalecloud.bss.uba.common.prop.CommonConst;
import com.iwhalecloud.bss.uba.common.prop.PropertyHolder;
import com.iwhalecloud.bss.uba.mq.MQOperateBuilder;
import com.iwhalecloud.bss.uba.mq.consume.MagicConsumeHandler;
import com.iwhalecloud.bss.uba.mq.magic.resource.MessageQueueMagicDynamicRegistry;
import com.iwhalecloud.bss.uba.mq.magic.resource.MessageQueueMagicResourceStorage;
import com.ztesoft.mq.client.api.bean.consumer.ConsumerBean;
import com.ztesoft.mq.client.api.bean.consumer.Subscription;
import com.ztesoft.mq.client.api.common.exception.MQClientApiException;
import com.ztesoft.mq.client.api.consumer.MQMessageHandler;
import com.ztesoft.zsmart.core.log.ZSmartLogger;
import com.ztesoft.zsmart.core.mq.CoreMQClientFactory;
import com.ztesoft.zsmart.core.mq.common.exception.CoreMQClientApiException;
import com.ztesoft.zsmart.core.mq.consumer.CoreConsumer;
import com.ztesoft.zsmart.core.mq.consumer.CoreMQMessageHandler;
import com.ztesoft.zsmart.core.mq.consumer.CoreSubscription;
import com.ztesoft.zsmart.core.mq.producer.CoreProducer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.ssssssss.magicapi.spring.boot.starter.MagicModuleConfiguration;

import java.io.IOException;
import java.util.*;

@Configuration
@AutoConfigureAfter(MagicModuleConfiguration.class)
public class UbaMQConfig implements InitializingBean {

    private static ZSmartLogger logger = ZSmartLogger.getLogger(UbaMQConfig.class);

    @Bean
    @ConditionalOnMissingBean
    public MessageQueueMagicResourceStorage mqInfoMagicResourceStorage() {
        return new MessageQueueMagicResourceStorage();
    }

    @Bean
    @ConditionalOnMissingBean
    public MessageQueueMagicDynamicRegistry mqInfoMagicDynamicRegistry(MessageQueueMagicResourceStorage messageQueueMagicResourceStorage,
                                                                       @Autowired(required = false) CoreMQClientFactory mqClientFactory,
                                                                       MagicRunner magicRunner) {
        return new MessageQueueMagicDynamicRegistry(messageQueueMagicResourceStorage, mqClientFactory, magicRunner);
    }

    /*List<ConsumerBean> consumerBeans;

    @EventListener
    public void destroyResource(ContextClosedEvent contextClosedEvent) {
        if(consumerBeans!=null && !consumerBeans.isEmpty()){
            consumerBeans.forEach(consumer -> {
                try {
                    consumer.close();
                } catch (IOException e) {
                    //throw new RuntimeException(e);
                }
            });
        }
    }*/

    /**订阅ZMQ消息*/
    /*@Bean
    @ConditionalOnProperty(
            prefix = "ftf.mq",
            value = {"use-unitd-mq"},
            havingValue = "false",
            matchIfMissing = true
    )
    public ConsumerBean[] startConsumer(MagicRunner magicRunner) {
        consumerBeans = new ArrayList<>();
        String mqConsumers = PropertyHolder.getProp(CommonConst.mqProviderListKey, "default");
        Arrays.stream(mqConsumers.split(",")).forEach(mqProvider -> {
            String mqType = PropertyHolder.getNoNullProp(String.format(CommonConst.mqProviderTypeKey, mqProvider));
            if(!"zmq".equals(mqType)){
                logger.warn(String.format("mq provider not support: provider=[%s] mqType=[%s]", mqProvider,mqType));
                return;
            }
            ConsumerBean consumer = new ConsumerBean();
            Properties properties = new MQOperateBuilder().buildMQConsumeProperties(mqProvider);
            consumer.setProperties(properties);
            Map<Subscription, MQMessageHandler> subscriptionTable = new HashMap<>();
            String topicList = PropertyHolder.getProp(String.format(CommonConst.mqProviderTopicKey, mqProvider));
            if ((topicList != null && !topicList.trim().equals(""))) {
                Arrays.stream(topicList.split(",")).forEach(topic -> {
                    Subscription subscription = new Subscription();
                    subscription.setTopic(topic);
                    subscription.setPullMessageSize(0); // 根据需要设置
                    subscriptionTable.put(subscription, new MagicConsumeHandler(magicRunner, mqProvider, mqType));
                });
                consumer.setSubscriptionTable(subscriptionTable);
                try {
                    logger.debug(String.format("consumer start : provider=[%s] consumerNameServAddr=[%s] topicList=[%s] ",
                            mqProvider, properties.getProperty("NamesrvAddr"), topicList));
                    consumer.start();
                    consumerBeans.add(consumer);
                } catch (MQClientApiException e) {
                    logger.warn(String.format("MQ Consumer for provider [%s] start fail", mqProvider), e);
                }
            }
        });
        return consumerBeans.toArray(new ConsumerBean[0]);
    }*/

    /**订阅统一Q消息，通过mqType来区分不同的消息类型*/
    /*@Bean
    @ConditionalOnProperty(
            prefix = "ftf.mq",
            value = {"use-unitd-mq"},
            havingValue = "true"
    )
    public CoreConsumer[] startConsumer(@Autowired(required = false) CoreMQClientFactory mqClientFactory,
                                        MagicRunner magicRunner) {
        if(mqClientFactory==null){
            logger.warn("mqClientFactory is null, please check core-boot-autoconfigure in classpath, " +
                    "if you want to close unit-mq ,you can config ftf.mq.use-unitd-mq=false");
            return null;
        }
        coreConsumers = new ArrayList<>();
        String mqConsumers = PropertyHolder.getProp(CommonConst.mqProviderListKey, "default");
        Arrays.stream(mqConsumers.split(",")).forEach(mqProvider -> {
            try {
                Properties properties = new MQOperateBuilder().buildMQConsumeProperties(mqProvider);
                String mqType = PropertyHolder.getNoNullProp(String.format(CommonConst.mqProviderTypeKey, mqProvider));
                properties.setProperty("mqType", mqType);
                CoreConsumer consumer = mqClientFactory.createConsumer(properties);
                // consumer.setProperties(properties);
                Map<CoreSubscription, CoreMQMessageHandler> subscriptionTable = new HashMap<>();
                String topicList = PropertyHolder.getProp(String.format(CommonConst.mqProviderTopicKey, mqProvider));
                if ((topicList != null && !topicList.trim().equals(""))) {
                    Arrays.stream(topicList.split(",")).forEach(topic -> {
                        CoreSubscription subscription = new CoreSubscription();
                        subscription.setTopic(topic);
                        subscription.setPullMessageSize(0); // 根据需要设置
                        subscriptionTable.put(subscription, new MagicConsumeHandler(magicRunner, mqProvider, mqType));
                    });
                    consumer.subscriptionTable(subscriptionTable);

                    logger.debug(String.format("consumer start : provider=[%s] consumerNameServAddr=[%s] topicList=[%s] ",
                            mqProvider, properties.getProperty("NamesrvAddr"), topicList));
                    consumer.start();
                    coreConsumers.add(consumer);
                }
            } catch (CoreMQClientApiException e) {
                throw new RuntimeException(e);
            }
        });
        return coreConsumers.toArray(new CoreConsumer[0]);
    }*/

    /**初始化向MQ发送消息的provider*//*
    public CoreProducer initProducer(CoreMQClientFactory mqClientFactory){
        return null;
    }*/


    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
