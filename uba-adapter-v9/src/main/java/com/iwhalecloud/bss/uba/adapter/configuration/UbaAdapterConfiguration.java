package com.iwhalecloud.bss.uba.adapter.configuration;

import com.iwhalecloud.bss.uba.adapter.UbaContext;
import com.iwhalecloud.bss.uba.adapter.datasource.MasterDBModule;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class UbaAdapterConfiguration {

    @Bean
    static BeanDefinitionRegistryPostProcessor ubsV8Init(){
        return new BeanDefinitionRegistryPostProcessor() {
            @Override
            public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
                UbaContext.EnvFlag = UbaContext.ENV_FLAG_V9;
            }

            @Override
            public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {

            }
        };
    }

    @Bean
    public MasterDBModule masterDBModule(DataSource dataSource,
                                         PlatformTransactionManager transactionManager){
        return new MasterDBModule(dataSource, transactionManager);
    }

}
