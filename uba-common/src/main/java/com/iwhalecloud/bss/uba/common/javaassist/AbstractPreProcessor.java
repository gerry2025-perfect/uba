package com.iwhalecloud.bss.uba.common.javaassist;

import com.iwhalecloud.bss.uba.common.javaassist.config.JAssistConfig;
import com.ztesoft.zsmart.core.log.ZSmartLogger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractPreProcessor implements IPreProcessor {

  private static final ZSmartLogger logger = ZSmartLogger.getLogger(AbstractPreProcessor.class);

  protected Set<String> excludeMethods;
  protected Set<String> excludeMethodRegulars;
  protected Set<String> excludePathRegulars;
  protected boolean isSaveWeaveClass;
  protected String saveWeaveClassPath;

  public AbstractPreProcessor() {
    this.excludeMethods = new HashSet();
    this.excludeMethodRegulars = new HashSet();
    this.excludePathRegulars = new HashSet();
    this.saveWeaveClassPath = null;
    this.isSaveWeaveClass = "yes".equals(JAssistConfig.getInstance().getIsSaveWeaveClass());
    if (this.isSaveWeaveClass) {
      this.saveWeaveClassPath = JAssistConfig.tmpdir + File.separator + "log-agent" + File.separator + "weave-class" + File.separator;
    }
    if (logger.isDebugEnabled()) {
      logger.debug("Enable or disable [{}] Save the planted class file to the specified directory:{}", Boolean.valueOf(this.isSaveWeaveClass), this.saveWeaveClassPath );
    }
    this.excludeMethods.add("main");
    this.excludeMethods.add("equals");
    this.excludeMethods.add("toString");
    this.excludeMethods.add("hashCode");
    this.excludeMethods.add("getClass");
    this.excludeMethods.add("notifyAll");
    this.excludeMethods.add("notify");
    this.excludeMethods.add("wait");
    this.excludeMethodRegulars.add(".*\\.[s|g]et[A-Z].*");
    String excludeMethodCfg = JAssistConfig.getInstance().getExcludeMethodCfg();
    if (excludeMethodCfg != null) {
      addRules(excludeMethodCfg, this.excludeMethodRegulars);
    }
    if (logger.isDebugEnabled()) {
      logger.debug("Exclusion rules for configuration file paths:{}", this.excludeMethodRegulars);
    }
    String excludePaths = JAssistConfig.getInstance().getExcludePaths();
    if (excludePaths != null) {
      addRules(excludePaths, this.excludePathRegulars);
    }
    if (logger.isDebugEnabled()) {
      logger.debug("Exclusion rules for configuration file paths:{}", this.excludePathRegulars);
    }
  }

  private void addRules(String rules, Set<String> set) {
    String[] ruleArray = rules.split(",");
    for (String ep : ruleArray) {
      if (!set.contains(ep)) {
        set.add(ep);
      }
    }
  }

  Map<String,Pattern> pMap = new HashMap<>();
  /**对正则表达式做缓存，不要每次使用的时候都解析一遍*/
  private Pattern getPattern(String p){
    if(!pMap.containsKey(p)){
      pMap.put(p,Pattern.compile(p));
    }
    return pMap.get(p);
  }

  /**当前方法是否排除，通过配置项中的正则表达式来控制*/
  protected boolean isExcludeMethod(String className, String methodName) {
    boolean b = this.excludeMethods.contains(methodName);

    if (!b) {
      String full = className + "." + methodName;
      for (String p : this.excludeMethodRegulars) {
        Pattern pattern = getPattern(p);
        Matcher matcher = pattern.matcher(full);
        b = matcher.matches();
        if (b) {
          break;
        }
      }
    }
    if (logger.isDebugEnabled()) {
      logger.debug("Check if {}.{} excludes the range :{}", className, methodName, Boolean.valueOf(b));
    }
    return b;
  }

  /**当前类是否排除，通过配置项中的正则表达式来控制*/
  protected boolean isExcludePath(String name) {
    boolean b = false;
    for (String p : this.excludePathRegulars) {
      Pattern pattern = getPattern(p);
      Matcher matcher = pattern.matcher(name);
      b = matcher.matches();
      if (b) {
        break;
      }
    }
    if (logger.isDebugEnabled()) {
      logger.debug("Check whether class {} is excluded :{}", name, Boolean.valueOf(b));
    }
    return b;
  }

  protected void saveWaveClassFile(String name, byte[] code) {
    if (!this.isSaveWeaveClass) {
      return;
    }
    FileOutputStream fos = null;
    try {
      File file = new File(this.saveWeaveClassPath + name + ".class");
      if (!file.getParentFile().exists()) {
        file.getParentFile().mkdirs();
      }

      fos = new FileOutputStream(file);
      fos.write(code);
    }
    catch (IOException ioe) {
      logger.error("Save the [{}] exception", ioe, this.saveWeaveClassPath + name + ".class");
    } finally {
      try {
        if (fos != null) {
          fos.close();
        }
      } catch (IOException ii) {}
    }
  }

  public static void main(String[] args) {
    String pp = ".*\\.[s|g]et[A-Z].*";
    String str = "setAsss";
    Pattern pattern = Pattern.compile(pp, 34);
    Matcher matcher = pattern.matcher(str);
    boolean b = matcher.find();
    System.out.println(b);

    String cl1 = "com.iwhalecloud.crm.devops.ec.sm.smo.impl.StaffManageSMOImpl$$EnhancerByCGLIB$$6ac3feb6";

    System.out.println(cl1.indexOf("$EnhancerByCGLIB$"));
    String p2 = ".*\\$EnhancerByCGLIB+.*";
    String[] ps = p2.split(",");
    System.out.println(ps.length);
    System.out.println(ps[0]);
    pattern = Pattern.compile(ps[0]);
    matcher = pattern.matcher(cl1);
    b = matcher.matches();
    System.out.println(b);

    String cl2 = "al.ec.sm.interceptor.Test";

    pattern = Pattern.compile("al.ec.sm.interceptor.*");
    matcher = pattern.matcher(cl2);
    b = matcher.matches();
    System.out.println(b);
  }
}
