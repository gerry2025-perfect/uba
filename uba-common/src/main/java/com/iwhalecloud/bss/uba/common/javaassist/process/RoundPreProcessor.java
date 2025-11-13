package com.iwhalecloud.bss.uba.common.javaassist.process;

import com.iwhalecloud.bss.uba.common.javaassist.config.JAssistConfig;
import com.iwhalecloud.bss.uba.common.javaassist.weave.WeaveCode;
import com.iwhalecloud.bss.uba.common.javaassist.weave.WeaveContext;
import com.ztesoft.zsmart.core.log.ZSmartLogger;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.bytecode.AttributeInfo;

import java.util.List;
import java.util.ListIterator;

/**整体替换，原本方法重命名，新方法名为原本方法，在新方法中调用原本方法，以此在前、后、异常时、finally中均可以加强*/
public class RoundPreProcessor implements IMethodPreProcessor {

  private static final ZSmartLogger logger = ZSmartLogger.getLogger(RoundPreProcessor.class);

  public CtMethod preProcessor(CtClass ctClass, CtMethod method, WeaveContext weaveContext, WeaveCode weaveCode) throws Exception {
    if (method == null || ctClass.isFrozen()) {
      return method;
    }
    String methodName = method.getName();

    String oldMethodName = methodName + "$$" + weaveContext.getHashCode();
    method.setName(oldMethodName);
    CtMethod logAgentMethod = CtNewMethod.copy(method, methodName, ctClass, null);
    List<AttributeInfo> attrs = method.getMethodInfo().getAttributes();
    if (attrs != null) {
      ListIterator iterator = attrs.listIterator();
      while (iterator.hasNext()) {
        AttributeInfo attr = (AttributeInfo)iterator.next();
        String name = attr.getName();
        if (name.equals("RuntimeVisibleParameterAnnotations") || name.equals("RuntimeVisibleAnnotations")) {
          logAgentMethod.getMethodInfo().addAttribute(attr);
          iterator.remove();
        }
      }
    }
    StringBuffer bufferCode = new StringBuffer();
    bufferCode.append("{ ");
    bufferCode.append(weaveCode.getBeforeCode().toString());
    CtClass returnCtClass = method.getReturnType();
    boolean haveReturn = !"void".equals(returnCtClass.getName());
    if (haveReturn) {
      bufferCode.append(returnCtClass.getName()).append(" _reObj ;");
    }

    bufferCode.append("try{");
    bufferCode.append(weaveCode.getTryCode().toString());
    if (haveReturn) {
      bufferCode.append("_reObj =  ");
    }
    bufferCode.append(oldMethodName + "($$);\n");

    bufferCode.append(weaveCode.getAfterCode().toString());
    if (haveReturn) {
      bufferCode.append("return _reObj;");
    }

    bufferCode.append("}catch(Throwable ").append(JAssistConfig.S_AGENT_ERR_PARAM_NAME).append("){");
    bufferCode.append(weaveCode.getExceptionCode());
    bufferCode.append("throw ").append(JAssistConfig.S_AGENT_ERR_PARAM_NAME).append(";");
    bufferCode.append("}finally{");
    bufferCode.append(weaveCode.getFinallyCode());
    bufferCode.append("}");
    if (haveReturn) {
      bufferCode.append(" return _reObj;");
    }
    bufferCode.append("}");

    if (logger.isDebugEnabled()) {
      logger.debug("{} method code to insert {} :{}", methodName, ctClass.getName(), bufferCode);
    }

    logAgentMethod.setBody(bufferCode.toString());
    ctClass.addMethod(logAgentMethod);
    return logAgentMethod;
  }
}