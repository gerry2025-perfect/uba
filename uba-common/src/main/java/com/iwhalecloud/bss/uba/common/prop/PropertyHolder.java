package com.iwhalecloud.bss.uba.common.prop;

import com.iwhalecloud.bss.uba.common.exception.ExceptionDefine;
import com.iwhalecloud.bss.uba.common.exception.UbaException;
import org.springframework.core.env.PropertyResolver;

public class PropertyHolder {

    private static PropertyResolver propertyResolver;

    public static void setPropertyResolver(PropertyResolver propertyResolver) {
        PropertyHolder.propertyResolver = propertyResolver;
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

}
