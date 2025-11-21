package com.iwhalecloud.bss.uba.service.interceptor;

import com.iwhalecloud.bss.uba.common.log.LogGenerator;
import com.ztesoft.zsmart.core.log.ZSmartLogger;
import org.springframework.stereotype.Component;
import com.iwhalecloud.bss.magic.magicapi.core.interceptor.RequestInterceptor;
import com.iwhalecloud.bss.magic.magicapi.core.model.ApiInfo;
import com.iwhalecloud.bss.magic.magicapi.core.servlet.MagicHttpServletRequest;
import com.iwhalecloud.bss.magic.magicapi.core.servlet.MagicHttpServletResponse;
import com.iwhalecloud.bss.magic.script.MagicScriptContext;

@Component
public class TracingLogRequestInterceptor implements RequestInterceptor {

    private static final ZSmartLogger logger = ZSmartLogger.getLogger(TracingLogRequestInterceptor.class);

    private final LogGenerator logGenerator;

    public TracingLogRequestInterceptor(LogGenerator logGenerator) {
        this.logGenerator = logGenerator;
    }

    @Override
    public Object preHandle(ApiInfo info, MagicScriptContext context, MagicHttpServletRequest request, MagicHttpServletResponse response) throws Exception {
        try{
            logGenerator.inMethod("Magic-api", info.getPath(),info.getMethod(),
                    new String[]{"scriptName", "variables"}, new Object[]{context.getScriptName(), context.getRootVariables()});
        } catch (Exception e) {
            logger.warn("inMethod record log fail", e);
        }
        return null;
    }

    @Override
    public void afterCompletion(ApiInfo info, MagicScriptContext context, Object returnValue, MagicHttpServletRequest request, MagicHttpServletResponse response, Throwable throwable) {
        try{
            logGenerator.outMethod(returnValue, throwable);
        } catch (Exception e) {
            logger.warn("outMethod record log fail", e);
        }finally {
            logGenerator.clear();
        }
    }
}
