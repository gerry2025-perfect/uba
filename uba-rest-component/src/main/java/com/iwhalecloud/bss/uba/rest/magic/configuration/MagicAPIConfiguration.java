package com.iwhalecloud.bss.uba.rest.magic.configuration;

import com.iwhalecloud.bss.uba.rest.magic.interceptor.CustomRequestInterceptor;
import com.iwhalecloud.bss.uba.rest.magic.interceptor.CustomUIAuthorizationInterceptor;
import com.iwhalecloud.bss.uba.rest.magic.module.DubboModule;
import com.iwhalecloud.bss.uba.rest.magic.module.MultiHttpModule;
import com.iwhalecloud.bss.uba.rest.magic.provider.*;
import com.iwhalecloud.bss.uba.rest.magic.resource.DubboMagicDynamicRegistry;
import com.iwhalecloud.bss.uba.rest.magic.resource.DubboMagicResourceStorage;
import com.iwhalecloud.bss.uba.rest.magic.scripts.CustomFunction;
import com.iwhalecloud.bss.uba.rest.magic.scripts.CustomFunctionExtension;
import com.iwhalecloud.bss.uba.rest.magic.scripts.CustomModule;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.ssssssss.magicapi.datasource.model.MagicDynamicDataSource;
import org.ssssssss.magicapi.modules.db.provider.PageProvider;
import org.ssssssss.magicapi.spring.boot.starter.MagicModuleConfiguration;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

/**
 * magic-api 配置类
 * 以下只配置了多数据源
 * 其它如果有需要可以自行放开 // @Bean 注释查看效果
 */
@Configuration
@AutoConfigureAfter(MagicModuleConfiguration.class)
public class MagicAPIConfiguration {

	/**
	 * 配置多数据源
	 *
	 * @see org.ssssssss.magicapi.datasource.model.MagicDynamicDataSource
	 */
	@Bean
	public MagicDynamicDataSource magicDynamicDataSource(DataSource dataSource) {
		MagicDynamicDataSource dynamicDataSource = new MagicDynamicDataSource();
		dynamicDataSource.setDefault(dataSource); // 设置默认数据源
		dynamicDataSource.add("slave", dataSource);
		return dynamicDataSource;
	}

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

	/**
	 * 配置自定义JSON结果
	 */
	// @Bean
	public CustomJsonValueProvider customJsonValueProvider() {
		return new CustomJsonValueProvider();
	}

	/**
	 * 配置分页获取方式
	 */
	// @Bean
	public PageProvider pageProvider() {
		return new CustomPageProvider();
	}

	/**
	 * 自定义UI界面鉴权
	 */
	// @Bean
	public CustomUIAuthorizationInterceptor customUIAuthorizationInterceptor() {
		return new CustomUIAuthorizationInterceptor();
	}

	/**
	 * 自定义请求拦截器（鉴权）
	 */
	// @Bean
	public CustomRequestInterceptor customRequestInterceptor() {
		return new CustomRequestInterceptor();
	}

	/**
	 * 自定义SQL缓存
	 */
	// @Bean
	public CustomSqlCache customSqlCache() {
		return new CustomSqlCache();
	}

	/**
	 * 自定义函数
	 */
	// @Bean
	public CustomFunction customFunction() {
		return new CustomFunction();
	}

	/**
	 * 自定义方法扩展
	 */
	// @Bean
	public CustomFunctionExtension customFunctionExtension() {
		return new CustomFunctionExtension();
	}

	/**
	 * 自定义模块
	 */
	// @Bean
	public CustomModule customModule() {
		return new CustomModule();
	}

	/**
	 * 自定义脚本语言
	 */
	// @Bean
	public CustomLanguageProvider customLanguageProvider() {
		return new CustomLanguageProvider();
	}

	/**
	 * 自定义列名转换
	 */
	// @Bean
	public CustomMapperProvider customMapperProvider() {
		return new CustomMapperProvider();
	}

}
