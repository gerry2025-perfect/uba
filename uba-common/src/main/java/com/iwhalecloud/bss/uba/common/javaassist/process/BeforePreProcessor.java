package com.iwhalecloud.bss.uba.common.javaassist.process;

import com.iwhalecloud.bss.uba.common.javaassist.weave.WeaveCode;
import com.iwhalecloud.bss.uba.common.javaassist.weave.WeaveContext;
import com.iwhalecloud.bss.uba.adapter.log.UbaLogger;
import javassist.CtClass;
import javassist.CtMethod;

/**在方法前做增强*/
public class BeforePreProcessor implements IMethodPreProcessor {

  private static final UbaLogger logger = UbaLogger.getLogger(BeforePreProcessor.class);

  public CtMethod preProcessor(CtClass ctClass, CtMethod method, WeaveContext weaveContext, WeaveCode weaveCode) throws Exception {
    if (method == null || ctClass.isFrozen()) {
      return method;
    }
    method.insertBefore(weaveCode.getBeforeCode().toString());
    if (logger.isDebugEnabled()) {
      logger.debug("{} method code to insert {} before :{}", method.getName(), ctClass.getName(), weaveCode.getBeforeCode());
    }
    return null;
  }
}
