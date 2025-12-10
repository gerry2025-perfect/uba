package com.iwhalecloud.bss.uba.comm;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.iwhalecloud.bss.uba.comm.exception.ExceptionDefine;
import com.iwhalecloud.bss.uba.comm.exception.UbaException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonUtils {

    static{
        // 序列化器
        JSON.register(LocalDateTime.class, (ObjectWriter<LocalDateTime>) (writer, value, fieldName, fieldType, features) -> {
            writer.writeString(((LocalDateTime)value).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        });

        // 反序列化器
        JSON.register(LocalDateTime.class, (ObjectReader<LocalDateTime>) (jsonReader, fieldType, fieldName, features) -> {
            String str = jsonReader.readString();
            return LocalDateTime.parse(str, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        });
    }

    /**对象转换为字符串，如果为复杂对象转换成json字符串，否则*/
    public static String convertToString(Object obj){
        if (obj == null) {
            return "";
        }
        // 判断是否为简单类型
        if (isSimpleType(obj)) {
            return obj.toString();
        }
        // 复杂类型使用 FastJSON2 转换为 JSON
        try {
            return JSON.toJSONString(obj);
        } catch (Exception e) {
            throw new UbaException(ExceptionDefine.COMM_ERROR_TYPE_CONVERT_JSON, e);
        }
    }

    /**判断一个对象是否为简单类型，转字符串的时候不能用json来转化*/
    public static boolean isSimpleType(Object obj){
        if (obj == null) return false;
        Class<?> clazz = obj.getClass();
        return clazz.isPrimitive() ||
                (clazz.isAssignableFrom(Boolean.class) ||
                        clazz.isAssignableFrom(Byte.class) ||
                        clazz.isAssignableFrom(Short.class) ||
                        clazz.isAssignableFrom(Integer.class) ||
                        clazz.isAssignableFrom(Long.class) ||
                        clazz.isAssignableFrom(Float.class) ||
                        clazz.isAssignableFrom(Double.class) ||
                        clazz.isAssignableFrom(Character.class) ||
                        clazz.isAssignableFrom(String.class) ||
                        clazz.isEnum() ||                  // 枚举类型
                        clazz.isAssignableFrom(Date.class) );  // 日期时间
    }

    /**获取节点下的节点值，以子节点方式*/
    public static String getNodeValue(Node node){
        if(node!=null){
            NodeList nodeList = node.getChildNodes();
            for (int i = 0; i <nodeList.getLength() ; i++) {
                if(nodeList.item(i).getNodeType()==Node.TEXT_NODE){
                    String nodeValue = nodeList.item(i).getNodeValue();
                    nodeValue = nodeValue.replaceAll("\n","");
                    nodeValue = nodeValue.replaceAll("\t","");
                    return nodeValue;
                }
            }
        }
        return null;
    }

    public static <T> T convertObject(Object obj, Class<T> respClass, Type returnGenericType) {
        try {
            // 将对象序列化为 JSON 字符串
            String jsonStr = JSON.toJSONString(obj,
                    JSONWriter.Feature.WriteNullListAsEmpty,
                    JSONWriter.Feature.WriteNullStringAsEmpty
            );;

            // 根据泛型类型选择不同的反序列化方式
            if (returnGenericType instanceof ParameterizedType) {
                // 处理泛型类型
                return JSON.parseObject(jsonStr, new TypeReference<T>(returnGenericType) {});
            } else {
                // 处理普通类型
                return JSON.parseObject(jsonStr, respClass);
            }
        } catch (Exception e) {
            throw new RuntimeException("JSON deserialization failed", e);
        }
    }

    /**将Map对象转换为java对象*/
    public static <T> T convertMap(Map<String,Object> map, Class<T> t){
        String jsonStr = JSON.toJSONString(map);
        return JSON.parseObject(jsonStr,t);
    }

    /**将字符串强转成其他简单数据类型*/
    public static <T> T convertObject(String str,Class<T> t){
        try {
            if(Integer.class.equals(t)){
                return (T)Integer.valueOf(str);
            }
            if(Long.class.equals(t)){
                return (T)Long.valueOf(str);
            }
            if(Float.class.equals(t)){
                return (T)Float.valueOf(str);
            }
            if(Double.class.equals(t)){
                return (T)Double.valueOf(str);
            }
        }catch (Exception e){
            throw new UbaException(ExceptionDefine.COMM_ERROR_TYPE_CONVERT," current value type is not "+t+",value:"+str,e);
        }
        return null;
    }

    /**判断字符串是否有值*/
    public static boolean stringHasValue(String s) {
        return s != null && s.length() > 0;
    }

    /**将字符串转为驼峰字符串*/
    public static String getCamelCaseString(String inputString,
                                            boolean firstCharacterUppercase) {
        StringBuilder sb = new StringBuilder();

        boolean nextUpperCase = false;
        for (int i = 0; i < inputString.length(); i++) {
            char c = inputString.charAt(i);

            switch (c) {
                case '_':
                case '-':
                case '@':
                case '$':
                case '#':
                case ' ':
                case '/':
                case '&':
                    if (sb.length() > 0) {
                        nextUpperCase = true;
                    }
                    break;

                default:
                    if (nextUpperCase) {
                        sb.append(Character.toUpperCase(c));
                        nextUpperCase = false;
                    } else {
                        sb.append(Character.toLowerCase(c));
                    }
                    break;
            }
        }

        if (firstCharacterUppercase) {
            sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        }

        return sb.toString();
    }

    /**判断列表中是否有含字符串*/
    public static boolean isContains(List<String> list, String str,boolean isIgnoreCase){
        if(list==null){
            return false;
        }
        for (String s:list) {
            if(isIgnoreCase && str.equalsIgnoreCase(s)) return true;
            if(!isIgnoreCase && str.equals(s)) return true;
        }
        return false;
    }

    /**对字符串进行分段*/
    public static List<String> split(String str,String delim){
        List<String> list = new ArrayList<String>();
        if(str.indexOf(delim)>0){
            StringTokenizer st = new StringTokenizer(str,delim);
            while (st.hasMoreTokens()){
                list.add(st.nextToken());
            }
        }else{
            list.add(str);
        }
        return list;
    }

    /**获取默认ClassLoader*/
    public static final ClassLoader getDefaultClassLoader() {
        try {
            return Thread.currentThread().getContextClassLoader();
        } catch (Throwable var5) {
            try {
                return CommonUtils.class.getClassLoader();
            } catch (SecurityException var4) {
                try {
                    return ClassLoader.getSystemClassLoader();
                } catch (Exception var3) {
                    return null;
                }
            }
        }
    }

    /**从类实例中获取属性值*/
    public static final <T> T getFieldValue(Object object, String fieldName, Class<T> fieldClass) {
        Class<?> objectClass = object.getClass();
        Field field = getField(objectClass, fieldName);
        if (field != null) {
            return getFieldValue(object, field, fieldClass);
        } else {
            Method method = getReadMethod(objectClass, fieldName);
            if (method == null) {
                throw new RuntimeException(String.format("no field [%s] in class [%s]", fieldName, object.getClass()));
            } else {
                return (T)invokeMethod(object, method);
            }
        }
    }

    /**从类定义中获取属性定义*/
    public static final Field getField(Class<?> clazz, String fieldName) {
        Field field;
        for(field = null; clazz != null && field == null; clazz = clazz.getSuperclass()) {
            try {
                field = clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException var4) {
                ;
            }
        }

        return field;
    }

    /**执行类方法*/
    public static final Object invokeMethod(Object target, Method method, Object... args) {
        boolean isAccessible = method.isAccessible();
        if (!isAccessible) {
            method.setAccessible(true);
        }
        try {
            return method.invoke(target, args);
        } catch (Exception e) {
            throw new RuntimeException(String.format("can not invoke method,parameter: [%s],method [%s]", Arrays.toString(args), method.getName()), e);
        } finally {
            if (!isAccessible) {
                method.setAccessible(false);
            }
        }
    }

    /**获取类中属性的get方法*/
    public static final Method getReadMethod(Class<?> clazz, String name) {
        try {
            return getReadOrWriteMethods(clazz, name, false);
        } catch (IntrospectionException var3) {
            throw new RuntimeException(String.format("Exception occurred while retrieving property [%s] in class [%s], reason:", clazz, name), var3);
        }
    }

    /**从类中获取属性值*/
    private static <T> T getFieldValue(Object object, Field field, Class<T> clazz) {
        boolean isAccessible = field.isAccessible();
        if (!isAccessible) {
            field.setAccessible(true);
        }
        try {
            return cast(field.get(object), clazz);
        } catch (Exception var8) {
            throw new RuntimeException(var8);
        } finally {
            if (!isAccessible) {
                field.setAccessible(false);
            }

        }
    }

    /**做对象强转*/
    public static final <T> T cast(Object object, Class<T> clazz) {
        if (object == null) {
            return null;
        } else if (!clazz.isAssignableFrom(object.getClass())) {
            throw new RuntimeException(String.format("object:[%s] cast to [%s] fail", object, clazz.getSimpleName()));
        } else {
            return (T)object;
        }
    }

    /**获取属性的读/写方法*/
    private static final Method getReadOrWriteMethods(Class<?> clazz, String name, boolean isRead) throws IntrospectionException {
        PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(clazz).getPropertyDescriptors();
        for(int i = 0; i < propertyDescriptors.length; ++i) {
            PropertyDescriptor propertyDescriptor = propertyDescriptors[i];
            if (propertyDescriptor.getName().equals(name)) {
                return isRead ? propertyDescriptor.getReadMethod() : propertyDescriptor.getWriteMethod();
            }
        }
        return null;
    }

    /**获取类中的方法，迭代取父类，迭代取匹配参数父类*/
    public static Method getDeclaredMethod(Class clazz,String methodName,Class... paramTypes){
        Method method = getSimpleDeclaredMethod(clazz,methodName,paramTypes);
        if(method==null){
            Class curClass = clazz.getSuperclass();
            while(curClass != Object.class){
                method = getSimpleDeclaredMethod(curClass,methodName,paramTypes);
                if(method==null){
                    curClass = curClass.getSuperclass();
                }else{
                    break;
                }
            }
        }
        return method;
    }

    /**获取类中的方法，当前类，迭代取匹配参数父类*/
    public static Method getSimpleDeclaredMethod(Class clazz,String methodName,Class... paramTypes){
        Method method = null;
        try {
            method = clazz.getDeclaredMethod(methodName,paramTypes);
        } catch (NoSuchMethodException e) {
            if(paramTypes!=null && paramTypes.length>0){
                List<Class>[] lists = new LinkedList[paramTypes.length];
                for (int i = 0; i <paramTypes.length ; i++) {
                    lists[i].add(paramTypes[i]);
                    Class curClass = paramTypes[i].getSuperclass();
                    while(curClass != Object.class){
                        lists[i].add(curClass);
                        curClass = curClass.getSuperclass();
                    }
                }
                Class[] curClasses = new Class[paramTypes.length];
                for (int i = 0; i <lists.length ; i++) {
                    for (Class c:lists[i]) {
                        for (int j = 0; j <lists.length ; j++) {

                        }
                    }

                    Class curClass = paramTypes[i].getSuperclass();
                    while (curClass != Object.class){
                        try {
                            method = clazz.getDeclaredMethod(methodName,paramTypes);
                            return method;
                        } catch (NoSuchMethodException e1) {
                            curClass = curClass.getSuperclass();
                        }
                    }
                }
            }
        }
        return method;
    }

    /**获取实例对象对应的类路径数组*/
    public static String[] getClassNameArr(Object[] params){
        if(params==null) return null;
        else{
            List<String> list = new ArrayList<String>();
            for (Object obj:params) {
                list.add(obj.getClass().getName());
            }
            return list.toArray(new String[]{});
        }
    }

    /**获取实例对象对应的类数组*/
    public static Class[] getClassArr(Object[] params){
        if(params==null) return null;
        else{
            List<Class> list = new ArrayList<Class>();
            for (Object obj:params) {
                list.add(obj.getClass());
            }
            return list.toArray(new Class[]{});
        }
    }

    /**获取异常堆栈*/
    public static String getCompactStackTrace(Throwable throwable, int maxElementsPerLevel, int maxDepth) {
        if (throwable == null) return "";

        StringBuilder sb = new StringBuilder(512);
        Throwable current = throwable;
        int causeCount = 0;

        while (current != null) {
            if(maxDepth>0 && causeCount>=maxDepth){
                break;
            }

            if (causeCount++ > 0) {
                sb.append("\nCaused by: ");
            }

            sb.append(current.getClass().getName());
            if (current.getMessage() != null) {
                sb.append(": ").append(current.getMessage());
            }

            StackTraceElement[] elements = current.getStackTrace();
            int limit = Math.min(elements.length, maxElementsPerLevel);

            for (int i = 0; i < limit; i++) {
                sb.append("\n\tat ").append(elements[i]);
            }

            if (elements.length > limit) {
                sb.append("\n\t... ").append(elements.length - limit).append(" more");
            }

            current = current.getCause();
        }

        return sb.toString();
    }

    /**
     * 将 InputStream 转换为 byte[]
     * @param inputStream 输入流（需调用方确保关闭，或使用 try-with-resources）
     * @return 字节数组
     * @throws IOException 流操作异常
     */
    public static byte[] toByteArray(InputStream inputStream) throws IOException {
        // 缓冲区大小：根据实际场景调整（如 8192 适合一般文件，更大的值适合大文件）
        byte[] buffer = new byte[8192];
        int bytesRead;

        // 用于缓冲数据的输出流
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            // 循环读取输入流到缓冲区，再写入输出流
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            // 将缓冲的数据转换为字节数组
            return outputStream.toByteArray();
        }
    }

    /**
     * 构建完整的文件路径
     */
    public static String buildFullPath(String rootDir, String filePath) {
        if (rootDir == null || rootDir.trim().isEmpty() || filePath.startsWith(rootDir)) {
            return filePath;
        }

        if (!rootDir.endsWith(File.separator)) {
            rootDir += File.separator;
        }

        if (filePath.startsWith(File.separator)) {
            filePath = filePath.substring(1);
        }

        return rootDir + filePath;
    }

    /**
     * 对字符串做占位符替换，替换的信息来源于注入方法
     * */
    public static String[] replaceStr(Pattern pattern, String str, Function<String, String> sourceFun){
        Matcher matcher = pattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        StringBuilder replaceSb = new StringBuilder();
        while (matcher.find()) {
            String key = matcher.group(1);
            //待替换的信息从function上获取
            String value = sourceFun.apply(key); //PropertyHolder.getProp(key);
            if (value != null) {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(value));
                replaceSb.append(matcher.group(0)).append(" --> ").append(value);
            } else {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group(0)));
                replaceSb.append(matcher.group(0)).append(" --> replace fail , parameter is not exists");
            }
            replaceSb.append("\r\n");
        }
        matcher.appendTail(sb);
        return new String[]{sb.toString(), replaceSb.toString()};
    }

}
