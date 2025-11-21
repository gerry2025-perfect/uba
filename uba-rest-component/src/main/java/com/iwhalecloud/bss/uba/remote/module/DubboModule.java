package com.iwhalecloud.bss.uba.remote.module;

import com.iwhalecloud.bss.uba.common.CommonUtils;
import com.iwhalecloud.bss.uba.common.dubbo.DubboOperator;
import com.iwhalecloud.bss.uba.common.prop.CommonConst;
import com.iwhalecloud.bss.uba.remote.magic.resource.DubboMagicDynamicRegistry;
import com.ztesoft.zsmart.core.log.ZSmartLogger;
import com.iwhalecloud.bss.magic.magicapi.core.annotation.MagicModule;
import com.iwhalecloud.bss.magic.magicapi.modules.DynamicModule;
import com.iwhalecloud.bss.magic.script.MagicScriptContext;
import com.iwhalecloud.bss.magic.script.annotation.Comment;
import com.iwhalecloud.bss.magic.script.functions.DynamicAttribute;

import java.util.List;
import java.util.Map;

//@Component
@MagicModule("dubbo")
public class DubboModule implements DynamicAttribute<DubboModule, DubboModule>,DynamicModule<DubboModule> {

    private static final ZSmartLogger logger = ZSmartLogger.getLogger(DubboModule.class);

    private ThreadLocal<DubboOperator> dubboOperator = new ThreadLocal<>();
    private final DubboMagicDynamicRegistry dubboMagicDynamicRegistry;

//    @Autowired
    public DubboModule(DubboMagicDynamicRegistry dubboMagicDynamicRegistry){
        this.dubboMagicDynamicRegistry = dubboMagicDynamicRegistry;
    }

    @Override
    public DubboModule getDynamicModule(MagicScriptContext context) {
        Map<String, DubboOperator> operators = dubboMagicDynamicRegistry.getDubboOperators();
        DubboOperator curOperator = null;
        if(operators.isEmpty()){
            throw new RuntimeException("dubbo operator is empty, please check dubbo configuration");
        }else if(operators.size()==1){
            curOperator = operators.values().stream().findFirst().get();
        }else{
            String dubboKey = context.getString(CommonConst.dubboDefaultServerCode);
            if(dubboKey!=null && operators.containsKey(dubboKey)){
                curOperator = operators.get(dubboKey);
            }else if(operators.containsKey(CommonConst.dubboDefaultServerCode)){
                curOperator = operators.get(CommonConst.dubboDefaultServerCode);
            }
            if(curOperator==null){
                throw new RuntimeException(String.format("cannot find dubbo operator by key: %s and %s" , dubboKey,CommonConst.dubboDefaultServerCode ));
            }
        }
        this.dubboOperator.set(curOperator);
        return this;
    }

    @Comment("使用纯泛化的方式调用，入参通过字符串指定类型、出参转换为字符串（复杂类型转换为JSON格式的字符串）")
    public String invokeForString(@Comment("") String interfaceName, @Comment("") String methodName, @Comment("") String[] paramTypes, @Comment("") List<Object> params){
        validate(interfaceName,methodName,paramTypes,params);
        return dubboOperator.get().invokeForString(interfaceName, methodName, paramTypes, params.toArray());
    }

    @Comment("使用纯泛化的方式调用，入参通过字符串指定类型、出参转换只能是简单数据类型")
    public Object invokeForSimple(String interfaceName,String methodName, String[] paramTypes,List<Object> params){
        validate(interfaceName,methodName,paramTypes,params);
        return dubboOperator.get().invokeForSimple(interfaceName, methodName, paramTypes, params.toArray());
    }

    @Comment("使用纯泛化的方式调用，入参通过字符串指定类型、出参转换不做转换，当时只能返回Map，否则报异常")
    public Map invokeForMap(String interfaceName,String methodName, String[] paramTypes,List<Object> params){
        validate(interfaceName,methodName,paramTypes,params);
        return dubboOperator.get().invokeForMap(interfaceName, methodName, paramTypes, params.toArray());
    }

    @Comment("使用纯泛化的方式调用，入参通过字符串指定类型、出参转换不做转换，当时只能返回List，否则报异常")
    public List invokeForList(@Comment(name = "interfaceName", value = "接口名称") String interfaceName,
                              @Comment(name = "methodName", value = "methodName") String methodName,
                              @Comment(name = "paramTypes", value = "paramTypes") String[] paramTypes,
                              @Comment(name = "params", value = "params") List<Object> params){
        validate(interfaceName,methodName,paramTypes,params);
        return dubboOperator.get().invokeForList(interfaceName, methodName, paramTypes, params.toArray());
    }

    private void validate(String interfaceName, String methodName, String[] paramTypes, List<Object> params){
        if(logger.isDebugEnabled()) {
            logger.info(String.format("invoke dubbo remote method, interfaceName: %s, methodName: %s, paramTypes: %s , params: %s",
                    interfaceName, methodName, CommonUtils.convertToString(paramTypes), CommonUtils.convertToString(params)));
        }
        if(dubboOperator.get() == null){
            throw new RuntimeException("dubbo operator is empty, please check dubbo configuration");
        }
    }

    @Override
    public DubboModule getDynamicAttribute(String key) {
        DubboOperator currentOperator = dubboMagicDynamicRegistry.getDubboOperators().get(key);
        if(currentOperator == null){
            throw new RuntimeException("cannot find dubbo operator by key:"+key);
        }
        this.dubboOperator.set(currentOperator);
        return this;
    }
}
