package com.iwhalecloud.bss.uba.common.javaassist.process;

import com.iwhalecloud.bss.uba.common.javaassist.weave.WeaveCode;
import com.iwhalecloud.bss.uba.common.javaassist.weave.WeaveContext;
import com.ztesoft.zsmart.core.log.ZSmartLogger;
import javassist.CtClass;
import javassist.CtMethod;

/**在方法前、后都增强*/
public class BeforeAfterPreProcessor implements IMethodPreProcessor {

  private static final ZSmartLogger logger = ZSmartLogger.getLogger(BeforeAfterPreProcessor.class);

  public CtMethod preProcessor(CtClass ctClass, CtMethod method, WeaveContext weaveContext, WeaveCode weaveCode) throws Exception {
    if (method == null || ctClass.isFrozen()) {
      return method;
    }
    method.insertBefore(weaveCode.getBeforeCode().toString());
    if (logger.isDebugEnabled()) {
      logger.debug("{} method code to insert {} before :{}", method.getName(), ctClass.getName(), weaveCode.getBeforeCode() );
    }
    method.insertAfter(weaveCode.getAfterCode().toString());
    if (logger.isDebugEnabled()) {
      logger.debug("{} method code to insert {} before :{}", method.getName(), ctClass.getName(), weaveCode.getAfterCode() );
    }

    return null;
  }
}