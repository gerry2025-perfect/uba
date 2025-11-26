package com.iwhalecloud.bss.uba.service.configuration;

import com.iwhalecloud.bss.magic.magicapi.core.model.MagicEntity;
import com.iwhalecloud.bss.magic.magicapi.core.resource.Resource;
import com.iwhalecloud.bss.magic.magicapi.core.service.MagicResourceService;
import com.iwhalecloud.bss.magic.magicapi.core.service.MagicResourceStorage;
import com.iwhalecloud.bss.uba.common.magic.UbaDatasourceInfoMagicResourceStorage;
import com.iwhalecloud.bss.uba.common.magic.UbaMagicResourceService;
import com.iwhalecloud.bss.uba.service.interceptor.CustomRequestInterceptor;
import com.iwhalecloud.bss.uba.service.interceptor.CustomUIAuthorizationInterceptor;
import com.iwhalecloud.bss.uba.service.provider.*;
import com.iwhalecloud.bss.uba.service.scripts.CustomFunction;
import com.iwhalecloud.bss.uba.service.scripts.CustomFunctionExtension;
import com.iwhalecloud.bss.uba.service.scripts.CustomModule;
import com.iwhalecloud.bss.uba.service.web.MagicExtController;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import com.iwhalecloud.bss.magic.magicapi.core.config.MagicAPIProperties;
import com.iwhalecloud.bss.magic.magicapi.core.config.MagicConfiguration;
import com.iwhalecloud.bss.magic.magicapi.datasource.model.MagicDynamicDataSource;
import com.iwhalecloud.bss.magic.magicapi.modules.db.provider.PageProvider;
import com.iwhalecloud.bss.magic.magicapi.spring.boot.starter.MagicModuleConfiguration;
import com.iwhalecloud.bss.magic.magicapi.utils.Mapping;

import javax.sql.DataSource;
import java.util.List;

/**
 * magic-api 配置类
 * 以下只配置了多数据源
 * 其它如果有需要可以自行放开 // @Bean 注释查看效果
 */
@Configuration
@AutoConfigureAfter({MagicModuleConfiguration.class, MagicConfiguration.class})
@EnableAspectJAutoProxy
public class MagicAPIConfiguration {

	private final MagicAPIProperties properties;
	private final ObjectProvider<List<MagicResourceStorage<? extends MagicEntity>>> magicResourceStoragesProvider;
	private final ApplicationContext applicationContext;

	@Autowired
	@Lazy
	private RequestMappingHandlerMapping requestMappingHandlerMapping;

	public MagicAPIConfiguration(MagicAPIProperties properties,
								 ObjectProvider<List<MagicResourceStorage<? extends MagicEntity>>> magicResourceStoragesProvider,
								 ApplicationContext applicationContext) {
		this.properties = properties;
		this.magicResourceStoragesProvider = magicResourceStoragesProvider;
		this.applicationContext = applicationContext;
	}

	/**
	 * 配置多数据源
	 *
	 * @see MagicDynamicDataSource
	 */
	@Bean
	public MagicDynamicDataSource magicDynamicDataSource(DataSource dataSource) {
		MagicDynamicDataSource dynamicDataSource = new MagicDynamicDataSource();
		dynamicDataSource.setDefault(dataSource); // 设置默认数据源
		dynamicDataSource.add("slave", dataSource);
		return dynamicDataSource;
	}

	@Bean
	public MagicExtController magicExtController(MagicConfiguration configuration) {
		//借这里的初始化逻辑加载额外Controller类
		String base = properties.getWeb();
		Mapping mapping = Mapping.create(requestMappingHandlerMapping, base);
		MagicExtController magicExtController = new MagicExtController(configuration);
		if (base != null) {
			mapping.registerController(new MagicExtController(configuration));
		}
		return magicExtController;
	}

	@Bean
	@ConditionalOnMissingBean
	public UbaDatasourceInfoMagicResourceStorage dataSourceInfoMagicResourceStorage() {
		return new UbaDatasourceInfoMagicResourceStorage();
	}

	@Bean
	@ConditionalOnMissingBean
	public MagicResourceService magicResourceService(Resource workspace) {
		return new UbaMagicResourceService(workspace, magicResourceStoragesProvider.getObject(), applicationContext);
	}


	/*@Bean
	public ModuleLogAspect moduleLogAspect(LogGenerator logGenerator){
		return new ModuleLogAspect(logGenerator);
	}*/

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
