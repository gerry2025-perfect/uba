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

    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
