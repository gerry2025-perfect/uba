package com.iwhalecloud.bss.uba.adapter.configuration;

import com.iwhalecloud.bss.uba.adapter.comm.UbaContext;
import com.iwhalecloud.bss.uba.adapter.datasource.UbaAdapterDataSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class UbaAdapterConfiguration {

    @Bean
    public DataSource dataSource() {
        return new UbaAdapterDataSource();
    }

    @Bean
    static BeanDefinitionRegistryPostProcessor ubsV8Init(){
        return new BeanDefinitionRegistryPostProcessor() {
            @Override
            public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
                UbaContext.EnvFlag = UbaContext.ENV_FLAG_V8;
            }

            @Override
            public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {

            }
        };
    }

}
