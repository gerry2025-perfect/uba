package com.iwhalecloud.bss.uba.plugin.config;

import com.ztesoft.zsmart.core.jdbc.mybatis.annotation.CoreMapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@CoreMapperScan(basePackages = "com.iwhalecloud.bss.uba.plugin.data.mapper",
        sqlSessionFactoryRef = "coreSqlSessionFactoryBean")
public class SpringConfiguration {
}
