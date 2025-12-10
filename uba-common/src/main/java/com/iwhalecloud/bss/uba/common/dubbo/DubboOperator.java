package com.iwhalecloud.bss.uba.common.dubbo;

import com.iwhalecloud.bss.uba.comm.CommonUtils;
import com.iwhalecloud.bss.uba.comm.exception.ExceptionDefine;
import com.iwhalecloud.bss.uba.comm.exception.UbaException;
import com.iwhalecloud.bss.uba.comm.prop.CommonConst;
import com.iwhalecloud.bss.uba.comm.prop.PropertyHolder;
import lombok.Data;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.utils.CompositeReferenceCache;
import org.apache.dubbo.rpc.model.ApplicationModel;
import org.apache.dubbo.rpc.service.GenericService;


import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**操作dubbo*/
public class DubboOperator {

    @Data
    public static class ConfigInfo{
        private String sourceCode;//dubbo服务编码
        private String registerAddress;//dubbo服务注册中心地址
        private Integer timeout;//dubbo服务超时时间
        private String groupName;//dubbo服务分组名称

        //从环境变量中获取配置项信息
        /*public static ConfigInfo loadFromEnvi(String dubboServiceCode){
            ConfigInfo configInfo = new ConfigInfo();
            configInfo.setSourceCode(dubboServiceCode);
            configInfo.setTimeout(Integer.parseInt(PropertyHolder.getNoNullProp(dubboServiceCode + CommonConst.dubboTimeoutKey).trim()));
            configInfo.setGroupName(PropertyHolder.getNoNullProp(dubboServiceCode + CommonConst.dubboGroupKey).trim());
            configInfo.setRegisterAddress(PropertyHolder.getNoNullProp(dubboServiceCode + CommonConst.dubboTimeoutKey).trim());
            return configInfo;
        }*/
    }

    /**zk注册中心缓存*/
    private static final Map<String, RegistryConfig> registryConfigMap = new HashMap<>();
    private static ApplicationConfig applicationConfig ;
    private RegistryConfig registryConfig;

    private DubboOperator(){}

    /**获取dubbo的操作类*/
    public static DubboOperator getInstance(String dubboServiceCode, ConfigInfo configInfo){
        if(applicationConfig==null){
            applicationConfig = new ApplicationConfig();
            applicationConfig.setName(PropertyHolder.getNoNullProp(CommonConst.dubboAppNameKey));
            if(PropertyHolder.getProp(CommonConst.dubboQosPortKey)!=null){
                applicationConfig.setQosPort(Integer.parseInt(PropertyHolder.getProp(CommonConst.dubboQosPortKey).trim()));
            }
        }
        DubboOperator dubboOperator = new DubboOperator();
        if(!registryConfigMap.containsKey(dubboServiceCode)){
             dubboOperator.registryConfig = new RegistryConfig();
            //如果参数中没有传入，就从环境变量中获取，这里主要是考虑初始化方有额外配置的情况
            //所有配置信息都配置到magic配置中，不通过配置项
            /*if(configInfo==null){
                configInfo = ConfigInfo.loadFromEnvi(dubboServiceCode);
            }*/
            dubboOperator.registryConfig.setAddress(configInfo.getRegisterAddress());
            dubboOperator.registryConfig.setTimeout(configInfo.getTimeout());
            dubboOperator.registryConfig.setGroup(configInfo.getGroupName());
            registryConfigMap.put(dubboServiceCode,dubboOperator.registryConfig);
        }else{
            dubboOperator.registryConfig = registryConfigMap.get(dubboServiceCode);
        }
        return dubboOperator;
    }

    /**移除dubbo处理类*/
    public static void remove(String dubboServiceCode){
        registryConfigMap.remove(dubboServiceCode);
    }

    /**按照常规方式调用dubbo接口，需要明确的入参和出参类型*/
    public <T> T invoke(String interfaceName,String methodName,Object[] params,Class<T> respClass,Type returnGenericType){
        GenericService genericService = getGenericService(interfaceName);
        // 基本类型以及Date,List,Map等不需要转换，直接调用
        Object result = genericService.$invoke(methodName, CommonUtils.getClassNameArr(params),params);
        return CommonUtils.convertObject(result,respClass,returnGenericType);
    }

    /** 使用纯泛化的方式调用，入参通过字符串指定类型、出参转换为字符串（复杂类型转换为JSON格式的字符串） */
    public String invokeForString(String interfaceName,String methodName, String[] paramTypes,Object[] params){
        return CommonUtils.convertToString(invoke(interfaceName, methodName, paramTypes,params));
    }

    /** 使用纯泛化的方式调用，入参通过字符串指定类型、只有返回参数为简单类型时才调用这个方法 */
    public Object invokeForSimple(String interfaceName,String methodName, String[] paramTypes,Object[] params){
        Object result = invoke(interfaceName, methodName, paramTypes,params);
        if(CommonUtils.isSimpleType(result)){
            return result;
        }else{
            throw new UbaException(ExceptionDefine.COMM_ERROR_TYPE, "current result is not simple java type , please invoke other method", null);
        }
    }

    /** 使用纯泛化的方式调用，入参通过字符串指定类型、只有返回参数必须时List才能调用这个 */
    public List invokeForList(String interfaceName,String methodName, String[] paramTypes,Object[] params){
        Object result = invoke(interfaceName, methodName, paramTypes,params);
        if(result instanceof List){
            return ((List)result);
        }else{
            throw new UbaException(ExceptionDefine.COMM_ERROR_TYPE, "current result is not List , please invoke other method", null);
        }
    }

    /** 使用纯泛化的方式调用，入参通过字符串指定类型、只有返回参数必须时Map才能调用这个 */
    public Map invokeForMap(String interfaceName,String methodName, String[] paramTypes,Object[] params){
        Object result = invoke(interfaceName, methodName, paramTypes,params);
        if(result instanceof Map){
            return ((Map)result);
        }else{
            throw new UbaException(ExceptionDefine.COMM_ERROR_TYPE, "current result is not Map , please invoke other method", null);
        }
    }

    public Object invoke(String interfaceName,String methodName,String[] paramTypes,Object[] params){
        GenericService genericService = getGenericService(interfaceName);
        return genericService.$invoke(methodName, paramTypes,params);
    }


    private GenericService getGenericService(String interfaceName){
        ReferenceConfig<GenericService> reference = new ReferenceConfig<>();
        reference.setApplication(applicationConfig);
        reference.setRegistry(registryConfig);
        reference.setInterface(interfaceName);
        reference.setGeneric("true"); // 声明为泛化接口
        CompositeReferenceCache cache = new CompositeReferenceCache(ApplicationModel.defaultModel());
        return cache.get(reference);
    }

    /*public static void main(String[] args){
        System.out.println("-->"+JSON.toJSONString(getInstance(PropertyHolder.getProp(CommonConst.dubboAddressKey), null).invoke(
                "com.iwhalecloud.zsmart.bss.cfg.intf.IConfigItemIntf",
                "pullConfigItemList", new Object[]{"de", "CPC", "CFG1", true}, List.class, new Type() {
                    public String getTypeName() {
                        return "java.util.List<com.iwhalecloud.zsmart.bss.cfg.data.ConfigItem>";
                    }
                })));
    }*/

}
