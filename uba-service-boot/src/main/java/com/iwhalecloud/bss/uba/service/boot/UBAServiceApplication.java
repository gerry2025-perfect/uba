package com.iwhalecloud.bss.uba.service.boot;

import com.iwhalecloud.bss.uba.adapter.log.UbaLogger;
import com.ztesoft.zsmart.core.log.ZSmartLogger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;

@SpringBootApplication(exclude = {
        //RedissonCacheStatisticsAutoConfiguration.class
})
@ComponentScan(basePackages = {
        "com.iwhalecloud.bss.uba"
})
@EnableCaching
public class UBAServiceApplication extends SpringBootServletInitializer {

    private static final UbaLogger logger = UbaLogger.getLogger(ZSmartLogger.class);

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(UBAServiceApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(UBAServiceApplication.class, args);
        logger.info("UBA service started successfully");
    }

    @SuppressWarnings("rawtypes")
    @Bean
    @Primary
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        Long cacheTime = 1440L;
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(cacheTime))
                .disableCachingNullValues()
                .computePrefixWith(name -> name + ":");
        return RedisCacheManager.builder(RedisCacheWriter.nonLockingRedisCacheWriter(redisConnectionFactory))
                .cacheDefaults(redisCacheConfiguration).build();
    }

}
