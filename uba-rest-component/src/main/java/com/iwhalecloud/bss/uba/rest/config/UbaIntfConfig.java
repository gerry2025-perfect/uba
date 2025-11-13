package com.iwhalecloud.bss.uba.rest.config;

import com.iwhalecloud.bss.uba.rest.magic.resource.DubboMagicDynamicRegistry;
import com.iwhalecloud.bss.uba.rest.magic.resource.DubboMagicResourceStorage;
import com.iwhalecloud.bss.uba.rest.module.DubboModule;
import com.iwhalecloud.bss.uba.rest.module.MultiHttpModule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

@Configuration
public class UbaIntfConfig {


    @Bean
    @ConditionalOnMissingBean
    public DubboMagicResourceStorage dubboMagicResourceStorage() {
        return new DubboMagicResourceStorage();
    }

    @Bean
    @ConditionalOnMissingBean
    public DubboMagicDynamicRegistry dubboMagicDynamicRegistry(DubboMagicResourceStorage dubboMagicResourceStorage) {
        return new DubboMagicDynamicRegistry(dubboMagicResourceStorage);
    }

    @Bean
    public DubboModule dubboModule(DubboMagicDynamicRegistry dubboMagicDynamicRegistry){
        return new DubboModule(dubboMagicDynamicRegistry);
    }

    @Bean
    public MultiHttpModule multiHttpModule(){
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new StringHttpMessageConverter(StandardCharsets.UTF_8) {
            {
                setSupportedMediaTypes(Collections.singletonList(MediaType.ALL));
            }

            @Override
            public boolean supports(Class<?> clazz) {
                return true;
            }
        });
        return new MultiHttpModule(restTemplate);
    }

}
