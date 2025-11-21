package com.iwhalecloud.bss.uba.mq.config;

import com.iwhalecloud.bss.uba.common.magic.MagicRunner;
import com.iwhalecloud.bss.uba.mq.magic.resource.MessageQueueMagicDynamicRegistry;
import com.iwhalecloud.bss.uba.mq.magic.resource.MessageQueueMagicResourceStorage;
import com.ztesoft.zsmart.core.log.ZSmartLogger;
import com.ztesoft.zsmart.core.mq.CoreMQClientFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.iwhalecloud.bss.magic.magicapi.spring.boot.starter.MagicModuleConfiguration;

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
