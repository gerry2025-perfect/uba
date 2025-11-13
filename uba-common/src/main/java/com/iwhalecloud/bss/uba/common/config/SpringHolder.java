package com.iwhalecloud.bss.uba.common.config;

import org.springframework.context.ApplicationContext;

/**操作Spring Bean*/
public class SpringHolder {

    private static ApplicationContext applicationContext;

    public static void setApplicationContext(ApplicationContext applicationContext){
        SpringHolder.applicationContext = applicationContext;
    }

    /**根据beanId获取Spring bean*/
    public static Object getBean(String beanId){
        return applicationContext.getBean(beanId);
    }

    /**根据bean类型获取Spring bean*/
    public static <T> T getBean(Class<T> clazz){
        return applicationContext.getBean(clazz);
    }

}
