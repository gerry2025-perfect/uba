package com.ztesoft.zsmart.core.util;

import com.ztesoft.common.logger.LogModule;
import com.ztesoft.common.logger.ModuleCodeConstants;
import com.ztesoft.zsmart.core.exception.BaseAppException;
import com.ztesoft.zsmart.core.exception.ExceptionHandler;
import com.ztesoft.zsmart.core.log.ZSmartLogger;
import net.sf.cglib.beans.BeanCopier;
import net.sf.cglib.core.Converter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class BeanUtil {

    /**
     * 日志
     */
    private static final ZSmartLogger logger = ZSmartLogger.getLogger(BeanUtil.class, new LogModule(ModuleCodeConstants.FTF_BASE_UTILITY));

    /**
     * 对象复制
     *
     * @param from    被复制的对象
     * @param toClass 复制后的类对象
     * @param <F>     F
     * @param <T>     T
     * @return 复制后的对象实例
     * @throws BaseAppException e
     */
    public static <F, T> T copy(F from, Class<T> toClass) throws BaseAppException {
        if (from == null || toClass == null) {
            return null;
        }
        if (from instanceof String || ClassUtils.isPrimitiveWrapper(from.getClass())) {
            return (T) from;
        }

        try {
            T to = toClass.newInstance();
            copyByConverter(from, to);
            return to;
        }
        catch (InstantiationException e) {
            ExceptionHandler.publish("7071001", e);
        }
        catch (IllegalAccessException e) {
            ExceptionHandler.publish("7071001", e);
        }
        return null;
    }

    /**
     * 对象复制
     *
     * @param from 被复制的对象
     * @param <T>  T
     * @return 复制后的对象实例
     * @throws BaseAppException e
     */
    @SuppressWarnings("unchecked")
    public static <T> T copy(T from) throws BaseAppException {

        if (from instanceof String || ClassUtils.isPrimitiveWrapper(from.getClass())) {
            return from;
        }

        return (T) copy(from, from.getClass());
    }

    /**
     * 集合对象复制
     *
     * @param froms   被复制的对象
     * @param toClass 复制后的类对象
     * @param <F>     F
     * @param <T>     T
     * @return 复制后的对象实例
     * @throws BaseAppException e
     */
    public static <F, T> List<T> copy(List<F> froms, Class<T> toClass) throws BaseAppException {
        if (CollectionUtils.isEmpty(froms) || toClass == null) {
            return new ArrayList<>(0);
        }

        List<T> tos = new ArrayList<T>(froms.size());
        for (int i = 0; i < froms.size(); i++) {
            F from = froms.get(i);
            T to = copy(from, toClass);
            tos.add(to);
        }
        return tos;
    }

    public static <T> Set<T> copy(Set<T> froms) throws BaseAppException {
        if (CollectionUtils.isEmpty(froms)) {
            return new HashSet<>(0);
        }

        Set<T> tos = new HashSet<>(froms.size());
        Iterator<T> iterator = froms.iterator();
        while (iterator.hasNext()) {
            T from = iterator.next();
            T to = copy(from);
            tos.add(to);
        }
        return tos;
    }

    /**
     * 对象复制
     *
     * @param from 被复制的对象
     * @param to   复制后的对象
     */
    private static void copyByConverter(final Object from, final Object to) {
        copy(from, to, new Converter() {

            @SuppressWarnings("unchecked")
            @Override
            public Object convert(Object value, @SuppressWarnings("rawtypes") Class target, Object context) {
                if (to != null && value != null && context != null) {
                    if (value instanceof List) {
                        // 1、得到字段名称
                        String fieldName = StringUtils.uncapitalize(String.valueOf(context).substring(3));
                        // 2、通过字段名称反射址List里的泛型属性在to对象里
                        Class<?> targetFieldGenericType = getClassGeneric(to.getClass(), fieldName);

                        if (ClassUtils.isPrimitiveWrapper(targetFieldGenericType)) {
                            return value;
                        }
                        try {
                            List<?> values = (List<?>) value;
                            return copy(values, targetFieldGenericType);
                        }
                        catch (BaseAppException e) {
                            logger.debug(e);
                            throw new RuntimeException("Failed to clone the list object", e);
                        }
                    }
                    else if (value instanceof Set) {
                        try {
                            Set<?> values = (Set<?>) value;
                            return copy(values);
                        }
                        catch (BaseAppException e) {
                            logger.debug(e);
                            throw new RuntimeException("Failed to clone the set object", e);
                        }
                    }
                    else if (!target.equals(value.getClass())
                            && !StringUtils.containsIgnoreCase(value.getClass().getName(), target.getName()) && !(value instanceof Map)) {
                        // 值的类型跟目标类型不能相同 && 不处理Map对象(值类型:java.util.Map 和 目标类型:java.util.HashMap)
                        if (value instanceof java.sql.Date && java.util.Date.class.isAssignableFrom(target)) {
                            // 3、处理日期类型
                            return new java.util.Date(((java.sql.Date) value).getTime());
                        }
                        else if (value instanceof java.util.Date && java.sql.Date.class.isAssignableFrom(target)) {
                            // 4、处理日期类型
                            return new java.sql.Date(((java.util.Date) value).getTime());
                        }

                        // 5、处理业务对象
                        try {
                            return copy(value, target);
                        }
                        catch (BaseAppException e) {
                            logger.debug(e);
                            throw new RuntimeException("Failed to clone the object", e);
                        }
                    }
                }
                return value;
            }
        });
    }

    /**
     * 对象复制
     *
     * @param from      被复制的对象
     * @param to        复制后的对象
     * @param converter 转换器
     */
    public static void copy(Object from, Object to, Converter converter) {
        BeanCopier beanCopier = BeanCopier.create(from.getClass(), to.getClass(), converter == null ? false : true);
        beanCopier.copy(from, to, converter);
    }

    /**
     * 反射对象里的字段值
     *
     * @param obj  对象
     * @param name 字段名称
     * @return 字段对象信息
     */
    @Deprecated
    public static Object getProperty(Object obj, String name) {
        return getPropertyValue(obj, name);
    }

    /**
     * 反射对象里的字段值
     *
     * @param obj  对象
     * @param name 字段名称
     * @return 字段对象信息
     */
    public static Object getPropertyValue(Object obj, String name) {
        Field field = ReflectionUtils.findField(obj.getClass(), name);
        if (field != null) {
            field.setAccessible(true);
            return ReflectionUtils.getField(field, obj);
        }
        return null;
    }


    /**
     * 得到类里某个字段接口的泛型
     *
     * @param type      类
     * @param fieldName 字段名
     * @return Class<?> 类对象
     * @author henry
     */
    public static Class<?> getClassGeneric(Class<?> type, String fieldName) {
        Field field = ReflectionUtils.findField(type, fieldName);
        if (field != null) {
            return getClassGeneric(field.getGenericType());
        }
        return null;
    }

    /**
     * 得到接口类型的第一个泛型 <br>
     *
     * @param type 接口类型
     * @return <br>
     * @author henry<br>
     * @taskId <br>
     */
    public static Class<?> getClassGeneric(final Type type) {
        return getClassGeneric(type, 0);
    }

    /**
     * 得到接口类型的第index个泛型 <br>
     *
     * @param type  接口类型
     * @param index 索引
     * @return <br>
     * @author henry<br>
     * @taskId <br>
     */
    public static Class<?> getClassGeneric(final Type type, final int index) {
        if (!(type instanceof ParameterizedType)) {
            return null;
        }
        Type[] params = ((ParameterizedType) type).getActualTypeArguments();
        if (index >= params.length || index < 0) {
            return null;
        }
        if (!(params[index] instanceof Class)) {
            return null;
        }
        return (Class<?>) params[index];
    }

}
