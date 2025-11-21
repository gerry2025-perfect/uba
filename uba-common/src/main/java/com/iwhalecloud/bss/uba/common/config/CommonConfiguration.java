package com.iwhalecloud.bss.uba.common.config;

import com.iwhalecloud.bss.uba.common.log.LogGenerator;
import com.iwhalecloud.bss.uba.common.log.LogWriter;
import com.iwhalecloud.bss.uba.common.magic.MagicRunner;
import com.iwhalecloud.bss.uba.common.prop.PropertyHolder;
import com.iwhalecloud.bss.uba.common.releaser.AutoReleaser;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.PropertyResolver;
import com.iwhalecloud.bss.magic.magicapi.core.service.MagicAPIService;

@Configuration
public class CommonConfiguration implements InitializingBean {

    private final PropertyResolver propertyResolver;

    public CommonConfiguration(PropertyResolver propertyResolver, ApplicationContext applicationContext) {
        this.propertyResolver = propertyResolver;
        SpringHolder.setApplicationContext(applicationContext);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        //将spring容器上下文中的properties配置获取信息放入到Holder类，确保后续能通过静态方法获取
        PropertyHolder.setPropertyResolver(propertyResolver);
    }

    @Bean
    public MagicRunner magicRunner(MagicAPIService magicAPIService) {
        return new MagicRunner(magicAPIService);
    }

    @Bean(initMethod = "init", destroyMethod = "stop")
    public AutoReleaser autoReleaser() {
        return new AutoReleaser();
    }

    @Bean
    public LogWriter logWriter(){
        LogWriter logWriter = new LogWriter();
        logWriter.init();
        return logWriter;
    }

    @Bean
    public LogGenerator logGenerator(LogWriter logWriter){
        return new LogGenerator(logWriter);
    }

}
