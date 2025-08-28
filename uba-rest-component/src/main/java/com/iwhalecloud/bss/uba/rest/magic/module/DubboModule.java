package com.iwhalecloud.bss.uba.rest.magic.module;

import com.iwhalecloud.bss.uba.common.CommonUtils;
import com.iwhalecloud.bss.uba.common.dubbo.DubboOperator;
import com.iwhalecloud.bss.uba.common.prop.CommonConst;
import com.iwhalecloud.bss.uba.rest.magic.resource.DubboMagicDynamicRegistry;
import com.ztesoft.zsmart.core.log.ZSmartLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.ssssssss.magicapi.core.annotation.MagicModule;
import org.ssssssss.magicapi.modules.DynamicModule;
import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.annotation.Comment;
import org.ssssssss.script.functions.DynamicAttribute;

import java.util.List;
import java.util.Map;

//@Component
@MagicModule("dubbo")
public class DubboModule implements DynamicAttribute<DubboModule, DubboModule>,DynamicModule<DubboModule> {

    private static final ZSmartLogger logger = ZSmartLogger.getLogger(DubboModule.class);

    private DubboOperator dubboOperator;
    private DubboMagicDynamicRegistry dubboMagicDynamicRegistry;

    public DubboModule(DubboOperator dubboOperator){
        this.dubboOperator = dubboOperator;
    }

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
        return new DubboModule(curOperator);
    }

    @Comment("使用纯泛化的方式调用，入参通过字符串指定类型、出参转换为字符串（复杂类型转换为JSON格式的字符串）")
    public String invokeForString(String interfaceName, String methodName, String[] paramTypes, List<Object> params){
        validate(interfaceName,methodName,paramTypes,params);
        return dubboOperator.invokeForString(interfaceName, methodName, paramTypes, params.toArray());
    }

    @Comment("使用纯泛化的方式调用，入参通过字符串指定类型、出参转换只能是简单数据类型")
    public Object invokeForSimple(String interfaceName,String methodName, String[] paramTypes,List<Object> params){
        validate(interfaceName,methodName,paramTypes,params);
        return dubboOperator.invokeForSimple(interfaceName, methodName, paramTypes, params.toArray());
    }

    @Comment("使用纯泛化的方式调用，入参通过字符串指定类型、出参转换不做转换，当时只能返回Map，否则报异常")
    public Map invokeForMap(String interfaceName,String methodName, String[] paramTypes,List<Object> params){
        validate(interfaceName,methodName,paramTypes,params);
        return dubboOperator.invokeForMap(interfaceName, methodName, paramTypes, params.toArray());
    }

    @Comment("使用纯泛化的方式调用，入参通过字符串指定类型、出参转换不做转换，当时只能返回List，否则报异常")
    public List invokeForList(String interfaceName,String methodName, String[] paramTypes,List<Object> params){
        validate(interfaceName,methodName,paramTypes,params);
        return dubboOperator.invokeForList(interfaceName, methodName, paramTypes, params.toArray());
    }

    private void validate(String interfaceName, String methodName, String[] paramTypes, List<Object> params){
        if(logger.isDebugEnabled()) {
            logger.info(String.format("invoke dubbo remote method, interfaceName: %s, methodName: %s, paramTypes: %s , params: %s",
                    interfaceName, methodName, CommonUtils.convertToString(paramTypes), CommonUtils.convertToString(params)));
        }
        if(dubboOperator == null){
            throw new RuntimeException("dubbo operator is empty, please check dubbo configuration");
        }
    }

    @Override
    public DubboModule getDynamicAttribute(String key) {
        DubboOperator currentOperator = dubboMagicDynamicRegistry.getDubboOperators().get(key);
        if(currentOperator == null){
            throw new RuntimeException("cannot find dubbo operator by key:"+key);
        }
        return new DubboModule(currentOperator);
    }
}
