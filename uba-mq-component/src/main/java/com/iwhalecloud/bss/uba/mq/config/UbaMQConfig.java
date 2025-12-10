package com.iwhalecloud.bss.uba.mq.config;

import com.iwhalecloud.bss.uba.common.magic.MagicRunner;
import com.iwhalecloud.bss.uba.adapter.log.UbaLogger;
import com.iwhalecloud.bss.uba.mq.magic.resource.MessageQueueMagicDynamicRegistry;
import com.iwhalecloud.bss.uba.mq.magic.resource.MessageQueueMagicResourceStorage;
import com.ztesoft.mq.unite.MQClientFactoryCreator;
import com.ztesoft.zsmart.core.mq.CoreMQClientFactory;
import com.ztesoft.zsmart.core.mq.client.united.UnitedMQClientFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import com.iwhalecloud.bss.magic.magicapi.spring.boot.starter.MagicModuleConfiguration;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

@Configuration
@AutoConfigureAfter(MagicModuleConfiguration.class)
public class UbaMQConfig implements InitializingBean {

    private static UbaLogger logger = UbaLogger.getLogger(UbaMQConfig.class);

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

    @Configuration
    @ConditionalOnClass({MQClientFactoryCreator.class})
    @Conditional({UnitedMQEnableCondition.class})
    static class UnitedMQAutoConfiguration {
        @Bean
        public CoreMQClientFactory coreMQClientFactory() {
            return new UnitedMQClientFactory();
        }
    }

    static class UnitedMQEnableCondition extends SpringBootCondition {
        public ConditionOutcome getMatchOutcome(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
            ConditionMessage.Builder message = ConditionMessage.forCondition("Use United MQ Enable", new Object[0]);
            Environment environment = conditionContext.getEnvironment();
            String useUnitedMQ = "true";//environment.getProperty("ftf.mq.use-unitd-mq");
            return StringUtils.isNotBlank(useUnitedMQ) && "true".equals(useUnitedMQ) ? ConditionOutcome.match(message.because("ftf.mq.use-united-mq is configured to true")) : ConditionOutcome.noMatch(message.because("ftf.mq.use-united-mq is not configured or not be true"));
        }
    }
}
