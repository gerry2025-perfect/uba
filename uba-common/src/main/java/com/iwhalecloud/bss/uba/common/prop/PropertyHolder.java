package com.iwhalecloud.bss.uba.common.prop;

import com.iwhalecloud.bss.uba.common.exception.ExceptionDefine;
import com.iwhalecloud.bss.uba.common.exception.UbaException;
import com.iwhalecloud.bss.uba.adapter.log.UbaLogger;
import org.springframework.core.env.*;

import java.util.HashMap;
import java.util.Map;

public class PropertyHolder {

    private static final UbaLogger logger = UbaLogger.getLogger(PropertyHolder.class);

    private static PropertyResolver propertyResolver;
    private static Map<String, String> properties = new HashMap<>();

    public static void setPropertyResolver(PropertyResolver propertyResolver, Environment environment) {
        PropertyHolder.propertyResolver = propertyResolver;

        // 1. 检查是否为 ConfigurableEnvironment (通常在 Spring Boot 中都是)
        if (environment instanceof ConfigurableEnvironment configurableEnvironment) {

            // 2. 获取所有 PropertySource (配置源)
            // 注意：Spring 会按照优先级顺序遍历这些 Source
            for (PropertySource<?> propertySource : configurableEnvironment.getPropertySources()) {

                // 3. 我们只能遍历 "Enumerable" 的源 (EnumerablePropertySource)
                // 绝大多数源（Map, Properties, yml, 环境变量）都继承此类
                if (propertySource instanceof EnumerablePropertySource) {

                    EnumerablePropertySource<?> source = (EnumerablePropertySource<?>) propertySource;
                    String[] propertyNames = source.getPropertyNames();

                    for (String key : propertyNames) {
                        // 4. 获取值
                        // 技巧：这里如果不希望被后续低优先级的源覆盖，可以使用 putIfAbsent
                        // 或者直接用 environment.getProperty(key) 来获取最终计算出的生效值
                        if (!properties.containsKey(key)) {
                            // 使用 environment.getProperty 确保获取的是经过占位符解析、优先级覆盖后的最终值
                            try {
                                properties.put(key, environment.getProperty(key));
                            }catch (Exception e) {
                                logger.warn(String.format("environment [%s] unavailable, for %s", key, e.getMessage()));
                            }
                        }
                    }
                }
            }
        }
    }

    /**获取spring配置的配置项值，没有默认值*/
    public static String getProp(String key) {
        return propertyResolver.getProperty(key);
    }

    /**获取spring配置的配置项值，配置项中没有获取到，返回默认值*/
    public static String getProp(String key, String defaultValue) {
        return propertyResolver.getProperty(key, defaultValue);
    }

    /**获取spring配置的配置项，如果没有获取到值就抛异常*/
    public static String getNoNullProp(String key) {
        String value = propertyResolver.getProperty(key);
        if (value == null) {
            throw new UbaException(ExceptionDefine.COMM_ERROR_TYPE_NULL,
                    String.format(", %s configuration item is not configured, please check",key), null);
        }
        return value;
    }

    /**根据前缀获取匹配的配置项清单*/
    public static Map<String, String> getProperties(String prefix){
        if(prefix==null || prefix.isEmpty()){
            return properties;
        }
        Map<String, String> result = new HashMap<>();
        properties.forEach((key, value) -> {
            if(key.startsWith(prefix)) result.put(key, value);
        });
        return result;
    }

}
