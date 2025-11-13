package com.iwhalecloud.bss.uba.common.javaassist;

import com.iwhalecloud.bss.uba.common.javaassist.config.JAssistConfig;
import com.iwhalecloud.bss.uba.common.javaassist.config.Path;
import com.iwhalecloud.bss.uba.common.javaassist.config.PathType;
import com.iwhalecloud.bss.uba.common.javaassist.process.AfterPreProcessor;
import com.iwhalecloud.bss.uba.common.javaassist.process.BeforeAfterPreProcessor;
import com.iwhalecloud.bss.uba.common.javaassist.process.IMethodPreProcessor;
import com.iwhalecloud.bss.uba.common.javaassist.process.RoundPreProcessor;
import com.iwhalecloud.bss.uba.common.javaassist.weave.*;
import com.ztesoft.zsmart.core.log.ZSmartLogger;
import javassist.*;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;

import java.io.ByteArrayInputStream;
import java.security.ProtectionDomain;
import java.util.*;

public class DefaultPreProcessor extends AbstractPreProcessor {

  private static final ZSmartLogger logger = ZSmartLogger.getLogger(DefaultPreProcessor.class);

  //默认排除当前框架中的类路径
  private final String DEFAULT_EXCLUDE_PATH = "com.iwhalecloud.crm.devops.alog";

  private List<SwoopRule> swoopRuleList = null;

  private IWeaveActuator logWeaveActuator = null;

  private IMethodPreProcessor beforePreProcessor = null;

  private IMethodPreProcessor afterPreProcessor = null;

  private IMethodPreProcessor roundPreProcessor = null;

  private IMethodPreProcessor beforeAfterPreProcessor = null;

  private int loggerStackDepth = 2;

  private ClassPool pool;

  /**将weaveName转成对应处理类实例，并迭代处理依赖关系，weaveName如果有冒号间隔，表示指定weaveName的同时能指定mcCode*/
  private void addLogWeaveAndReturnMcode(List<IWeave> weaves, String[] depends, Map<String, String> mcodeMap) {
    if (depends == null) {
      return ;
    }
    for (String weaveMcodeCfg : depends) {
      String weaveName = splitMcode(weaveMcodeCfg, mcodeMap);
      IWeave weave = WeaveFactory.getInstance().getLogWeave(weaveName);
      if (weave != null && !weaves.contains(weave)) {
        weaves.add(weave);
        addLogWeaveAndReturnMcode(weaves, weave.getDependLogWeave(), mcodeMap);
      }
    }
  }

  /**分解mcode和名称*/
  private String splitMcode(String weaveMcodeCfg, Map<String, String> mcodeMap){
    String[] weaveMcode = weaveMcodeCfg.split(":");
    String weaveName = weaveMcode[0];
    if (weaveMcode.length == 2) {
      mcodeMap.put(weaveName, weaveMcode[1]);
    }
    return weaveName;
  }

  public void initialize() {
    JAssistConfig config = JAssistConfig.getInstance();
    this.loggerStackDepth = config.getLoggerStackDepth();
    this.swoopRuleList = new ArrayList<>();
    Set<Path> basePaths = config.getWeaves().keySet();
    for (Path basePath : basePaths) {
      SwoopRule swoopRule = new SwoopRule();
      swoopRule.setPath(basePath);
      List<IWeave> weaves = config.getWeaves().get(basePath);
      Map<String, String> mcodeMap = new HashMap<>();
      for (IWeave weave : weaves) {
        splitMcode(weave.getName(), mcodeMap);
        String[] dependWeaves = weave.getDependLogWeave();
        if(dependWeaves!=null && dependWeaves.length>0) {
          addLogWeaveAndReturnMcode(weaves, dependWeaves, mcodeMap);
        }
      }
      weaves.sort((weave1, weave2) -> (weave1.getOrder() > weave2.getOrder()) ? 1 : -1);
      swoopRule.setWeaves(weaves);
      swoopRule.setMcodeMap(mcodeMap);
      this.swoopRuleList.add(swoopRule);
    }
    this.logWeaveActuator = new DefaultWeaveActuator();
    this.beforePreProcessor = new BeforeAfterPreProcessor();
    this.afterPreProcessor = new AfterPreProcessor();
    this.roundPreProcessor = new RoundPreProcessor();
    this.beforeAfterPreProcessor = new BeforeAfterPreProcessor();
  }


  private boolean isSupportMethodRule(String className) {
    for (SwoopRule rule : this.swoopRuleList) {
      Path path = rule.getPath();
      if (path.getType() == PathType.METHOD &&
        className.equals(path.getFullClassName())) {
        return true;
      }
    }

    return false;
  }
  private SwoopRule isSupportRule(String className, String methodName) {
    for (SwoopRule rule : this.swoopRuleList) {
      Path path = rule.getPath();
      if (path.getType() == PathType.PACKAGE) {
        if (className.startsWith(path.toString()))
          return rule;  continue;
      }
      if (path.getType() == PathType.CLASS) {
        if (className.equals(path.getFullClassName()))
          return rule;  continue;
      }
      if (path.getType() == PathType.METHOD && methodName != null &&
        methodName.equals(path.getMethodName()) &&
        className.equals(path.getFullClassName())) {
        return rule;
      }
    }


    return null;
  }

  /**从方法中获取方法参数类型*/
  private static String[] getMethodParamTypes(CtMethod cm){
    if(cm == null){
      return null;
    }
    try {
      CtClass[] types = cm.getParameterTypes();
      if(types==null || types.length==0){ return null;}
      String[] typeStrArr = new String[types.length];
      for (int i=0;i<types.length;i++) {
        typeStrArr[i] = types[i].getName();
      }
      return typeStrArr;
    } catch (NotFoundException e) {
      logger.error("getMethodParamTypes fail,method name:[{0}]",e,cm.getName());
      return null;
    }
  }

  /**从方法中获取方法参数实际名称*/
  private static String[] getMethodParamNames(CtMethod cm) {
    if (cm == null)
      return null;
    MethodInfo methodInfo = cm.getMethodInfo();
    CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
    LocalVariableAttribute attr = (LocalVariableAttribute)codeAttribute.getAttribute(LocalVariableAttribute.tag);

    if (attr == null) {
      return new String[0];
    }

    String[] paramNames = null;
    try {
      paramNames = new String[cm.getParameterTypes().length];
    } catch (NotFoundException e) {}

    int pos = Modifier.isStatic(cm.getModifiers()) ? 0 : 1;
    for (int i = 0; i < paramNames.length; i++) {
      String name = attr.variableName(i + pos);
      if ("this".equals(name)) {
        pos++;
        name = attr.variableName(i + pos);
      }
      paramNames[i] = name;
    }
    return paramNames;
  }

  /**实际处理代码植入*/
  public byte[] preProcess(ClassLoader classLoader, String classFile, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
    if (classFile == null) {
      return null;
    }
    String clazz = classFile;
    if (classFile.contains("/")) {
      clazz = classFile.replaceAll("/", ".");
    }
    //强行排除当前框架以及CGLib 和 SpringCGLib已增强的类
    if (clazz.startsWith(DEFAULT_EXCLUDE_PATH) || clazz.contains("$EnhancerByCGLIB$") || clazz.contains("$EnhancerBySpringCGLIB$")) {
      return null;
    }
    if (isExcludePath(clazz)) {
      return null;
    }
    SwoopRule rule = isSupportRule(clazz, null);
    boolean havClassRule = false;
    if ((rule != null && rule.getWeaves() != null && !rule.getWeaves().isEmpty())) {
      havClassRule = true;

    }
    else if (!isSupportMethodRule(clazz)) {
      return null;
    }
    WeaveContext context = new WeaveContext();
    context.setLoggerStackDepth(this.loggerStackDepth);
    context.setClassName(clazz);
    ByteArrayInputStream inp = new ByteArrayInputStream(classfileBuffer);
    boolean weaved = false;
    try {
      Class classLoaderCls = (classLoader != null) ? classLoader.getClass() : null;
      if (logger.isDebugEnabled()) {
        logger.debug("Processing [{}] class bytecode. Current Loader:{},paramClassLoader:{}",
                clazz, Thread.currentThread().getContextClassLoader().getClass(), classLoaderCls);
      }
      if (classLoaderCls != null && classLoaderCls.getName().startsWith("com.iwhalecloud.crm.devops.alog.agent.classloader.ClassLoaderHolder")) {
        if (logger.isDebugEnabled()) {
          logger.debug("{} defines the class in the loader for the proxy, no weaving is required.", new Object[0]);
        }
        return null;
      }
      if (this.pool == null) {
        this.pool = ClassPool.getDefault();
        this.pool.insertClassPath(new LoaderClassPath(Thread.currentThread().getContextClassLoader()));
      }
      CtClass ctClass = null;
      try {
        ctClass = this.pool.get(clazz);
      } catch (Exception e) {}
      if (ctClass == null) {
        ctClass = this.pool.makeClass(inp);
      }
      if (ctClass.isInterface() || ctClass.isFrozen()) {
        return null;
      }
      CtMethod[] ctMethods = ctClass.getDeclaredMethods();
      //ALogJMXReport.getALogJMXReport().getRunStatusInfo().incrementWeaveClassNum();
      for (CtMethod ctMethod : ctMethods) {
        String methodName = ctMethod.getName();
        SwoopRule methodRule = isSupportRule(clazz, methodName);
        if (methodRule == null || methodRule.getWeaves() == null || methodRule.getWeaves().isEmpty()) {
          if (havClassRule) {
            methodRule = rule;
          } else {
            continue;
          }
        }
        context.setRule(methodRule);
        context.setCreateInParams(false);
        if (!isExcludeMethod(null, methodName)) {
          if(logger.isDebugEnabled()){
            logger.debug("weave class:[{}],method:[{}]",clazz,methodName);
          }
          WeaveCode weaveCode = null ;
          try {
            context.setMethodName(methodName);
            context.setParamNumber(ctMethod.getParameterTypes().length);
            String[] paramNames = getMethodParamNames(ctMethod);
            context.setParamNames(paramNames);
            context.setParamTypes(getMethodParamTypes(ctMethod));
            CtClass returnCtClass = ctMethod.getReturnType();
            context.setReturnClass(returnCtClass.getName());
            weaveCode = this.logWeaveActuator.executeWeave(context, methodRule.getWeaves());
            int type = weaveCode.getType();
            switch (type) {
              case IMethodPreProcessor.BEFORE_TYPE:
                this.beforePreProcessor.preProcessor(ctClass, ctMethod, context, weaveCode);
                break;
              case IMethodPreProcessor.AFTER_TYPE:
                this.afterPreProcessor.preProcessor(ctClass, ctMethod, context, weaveCode);
                break;
              case IMethodPreProcessor.BEFORE_AFTER_TYPE:
                this.beforeAfterPreProcessor.preProcessor(ctClass, ctMethod, context, weaveCode);
                break;
              case IMethodPreProcessor.ROUND_TYPE:
                this.roundPreProcessor.preProcessor(ctClass, ctMethod, context, weaveCode);
                break;
            }
            weaved = true;
          } catch (Throwable e) {
            //ALogJMXReport.getALogJMXReport().getRunStatusInfo().incrementweaveErrClassNum();
            logger.error(String.format("weave class error [%s] method[%s]", clazz, methodName), e);
            if(weaveCode !=null){
              logger.warn("code information : before:[{}] after:[%s] exception:[%s] finally:[%s]",
                      weaveCode.getBeforeCode(), weaveCode.getAfterCode(), weaveCode.getExceptionCode(), weaveCode.getFinallyCode());
            }
          }
        }
      }
      byte[] code = ctClass.toBytecode();
      if (weaved) {
        saveWaveClassFile(classFile, code);
      }
      return code;
    } catch (Throwable e) {
      logger.error(String.format("weave class [%s]", clazz), e);
      return null;
    }
  }
}
