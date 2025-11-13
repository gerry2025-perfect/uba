package com.iwhalecloud.bss.uba.common.javaassist.process;

import com.iwhalecloud.bss.uba.common.javaassist.weave.WeaveCode;
import com.iwhalecloud.bss.uba.common.javaassist.weave.WeaveContext;
import javassist.CtClass;
import javassist.CtMethod;

/**方法增加的接口，通过实现类来确定在方法的什么地方增强*/
public interface IMethodPreProcessor {
  public static final int BEFORE_TYPE = 1;
  
  public static final int AFTER_TYPE = 3;
  
  public static final int BEFORE_AFTER_TYPE = 7;
  
  public static final int ROUND_TYPE = 15;
  
  CtMethod preProcessor(CtClass paramCtClass, CtMethod paramCtMethod, WeaveContext paramWeaveContext, WeaveCode paramWeaveCode) throws Exception;
}