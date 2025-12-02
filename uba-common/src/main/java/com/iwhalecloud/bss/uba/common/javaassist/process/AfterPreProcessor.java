package com.iwhalecloud.bss.uba.common.javaassist.process;

import com.iwhalecloud.bss.uba.common.javaassist.weave.WeaveCode;
import com.iwhalecloud.bss.uba.common.javaassist.weave.WeaveContext;
import com.iwhalecloud.bss.uba.adapter.log.UbaLogger;
import javassist.CtClass;
import javassist.CtMethod;

/**在方法后做增强*/
public class AfterPreProcessor implements IMethodPreProcessor {

  private static final UbaLogger logger = UbaLogger.getLogger(AfterPreProcessor.class);

  public CtMethod preProcessor(CtClass ctClass, CtMethod method, WeaveContext weaveContext, WeaveCode weaveCode) throws Exception {
    if (method == null || ctClass.isFrozen()) {
      return method;
    }
    method.insertAfter(weaveCode.getAfterCode().toString());
    if (logger.isDebugEnabled()) {
      logger.debug("{} method code to insert {} after :{}", method.getName(), ctClass.getName(), weaveCode.getAfterCode());
    }
    return null;
  }
}