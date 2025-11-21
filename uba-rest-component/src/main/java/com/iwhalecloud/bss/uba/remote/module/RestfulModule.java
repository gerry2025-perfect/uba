package com.iwhalecloud.bss.uba.remote.module;

import com.iwhalecloud.bss.uba.common.prop.CommonConst;
import com.iwhalecloud.bss.uba.remote.magic.resource.RestfulMagicDynamicRegistry;
import com.ztesoft.zsmart.core.log.ZSmartLogger;
import org.springframework.http.MediaType;
import com.iwhalecloud.bss.magic.magicapi.core.annotation.MagicModule;
import com.iwhalecloud.bss.magic.magicapi.modules.DynamicModule;
import com.iwhalecloud.bss.magic.script.MagicScriptContext;
import com.iwhalecloud.bss.magic.script.annotation.Comment;
import com.iwhalecloud.bss.magic.script.functions.DynamicAttribute;

import java.util.Map;

@MagicModule("restful")
public class RestfulModule implements DynamicAttribute<RestfulModule, RestfulModule>, DynamicModule<RestfulModule> {

    private static final ZSmartLogger logger = ZSmartLogger.getLogger(RestfulModule.class);

    private final RestfulMagicDynamicRegistry registry;
    private final ThreadLocal<RestSiteOperator> operatorHolder = new ThreadLocal<>();

    public RestfulModule(RestfulMagicDynamicRegistry registry) {
        this.registry = registry;
    }

    @Override
    public RestfulModule getDynamicModule(MagicScriptContext context) {
        Map<String, RestSiteOperator> operators = registry.getOperators();
        RestSiteOperator curOperator = null;
        if (operators.isEmpty()) {
            throw new RuntimeException("restful operator is empty, please check restful configuration");
        } else if (operators.size() == 1) {
            curOperator = operators.values().stream().findFirst().get();
        } else {
            String siteKey = context.getString(CommonConst.restDefaultSiteCode);
            if (siteKey != null && operators.containsKey(siteKey)) {
                curOperator = operators.get(siteKey);
            } else if (operators.containsKey(CommonConst.restDefaultSiteCode)) {
                curOperator = operators.get(CommonConst.restDefaultSiteCode);
            }
            if (curOperator == null) {
                throw new RuntimeException("cannot find restful operator by key:" + siteKey);
            }
        }
        operatorHolder.set(curOperator);
        return this;
    }

    @Override
    public RestfulModule getDynamicAttribute(String key) {
        RestSiteOperator currentOperator = registry.getOperators().get(key);
        if (currentOperator == null) {
            throw new RuntimeException("cannot find restful operator by key:" + key);
        }
        operatorHolder.set(currentOperator);
        return this;
    }

    @Comment("GET 按方法名调用外部 REST")
    public String get(@Comment(name = "apiCode", value = "接口方法名") String apiCode,
                      @Comment(name = "body", value = "请求体") Map<String,Object> body,
                      @Comment(name = "params", value = "URL 参数") Map<String, Object> params,
                      @Comment(name = "header", value = "http header清单") Map<String, String> headers) {
        return operatorHolder.get().get(apiCode, body, params, headers);
    }

    @Comment("DELETE 按方法名调用外部 REST")
    public String delete(@Comment(name = "apiCode", value = "接口方法名") String apiCode,
                         @Comment(name = "params", value = "URL 参数") Map<String, Object> params,
                         @Comment(name = "header", value = "http header清单") Map<String, String> headers) {
        return operatorHolder.get().delete(apiCode, params, headers);
    }

    @Comment("POST 按方法名调用外部 REST（JSON 请求体）")
    public String post(@Comment(name = "apiCode", value = "接口方法名") String apiCode,
                       @Comment(name = "body", value = "请求体") Map<String,Object> body,
                       @Comment(name = "params", value = "URL 参数") Map<String, Object> params,
                       @Comment(name = "header", value = "http header清单") Map<String, String> headers) {
        return operatorHolder.get().post(apiCode, body, params, headers);
    }

    @Comment("PUT 按方法名调用外部 REST（JSON 请求体）")
    public String put(@Comment(name = "apiCode", value = "接口方法名") String apiCode,
                      @Comment(name = "body", value = "请求体") Map<String,Object> body,
                      @Comment(name = "params", value = "URL 参数") Map<String, Object> params,
                      @Comment(name = "header", value = "http header清单") Map<String, String> headers) {
        return operatorHolder.get().put(apiCode, body, params, headers);
    }

    @Comment("PATCH 按方法名调用外部 REST（JSON 请求体）")
    public String patch(@Comment(name = "apiCode", value = "接口方法名") String apiCode,
                        @Comment(name = "body", value = "请求体") Map<String,Object> body,
                        @Comment(name = "params", value = "URL 参数") Map<String, Object> params,
                        @Comment(name = "header", value = "http header清单") Map<String, String> headers) {
        return operatorHolder.get().patch(apiCode, body, params, headers);
    }

    @Comment("通用执行restful远程调用")
    public <T> T invokeRest(@Comment(name = "apiCode", value = "接口方法名") String apiCode,
                            @Comment(name = "params", value = "URL 参数") Map<String, Object> params,
                            @Comment(name = "header", value = "http header清单") Map<String, String> headers,
                            @Comment(name = "body", value = "请求体") Map<String, Object> body,
                            @Comment(name = "contentType", value = "rest交易的内容类型") MediaType contentType,
                            @Comment(name = "cls", value = "返回数据类型") Class<T> cls){
        return operatorHolder.get().invoke(apiCode, null, params, headers, body, contentType, cls);
    }
}