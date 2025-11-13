package com.iwhalecloud.bss.uba.common.javaassist.weave;

import java.util.HashMap;
import java.util.Map;

/**方法增强器工厂类，扩展的方法增强器都需要注册到这个类中，供后续使用*/
public class WeaveFactory
{
  private static WeaveFactory inst;
  private Map<String, String> logWeaveName = new HashMap();

  private Map<String, IWeave> logWeaveInst = new HashMap();

  private WeaveFactory() {
    /*register(new LogIdWeave());
    register(new RunningProcessesLogWeave());
    register(new ErrorLogWeave());
    register(new LoggerLogWeave());
    register(new DefinitionInParamLogWeave());
    register(new InterceptParamLogWeave());
    register(new ClassMethodNameWeave());
    register(new InterceptIntfLogWeave());
    register(new ControllerContextWeave());
    register(new TraceRunningProcessLogWeave());
    register(new ControllerProcessWeave());
    register(new DubboContextWeave());
    register(new DubboClientProcessWeave());
    register(new DubboServerProcessWeave());
    register(new TFMServiceProcessWeave());
    register(new AMPProcessWeave());
    register(new SqlProcessWeave());
    register(new LoggerWeave());*/

  }

  private void register(IWeave logWeave){
    this.logWeaveName.put(logWeave.getName(), logWeave.getClass().getName());
    this.logWeaveInst.put(logWeave.getName(), logWeave);
  }

  /**注册Weave执行类*/
  public static void registerWeave(IWeave logWeave){
    getInstance().register(logWeave);
  }

  public static WeaveFactory getInstance() {
    synchronized (WeaveFactory.class) {
      if (inst == null) {
        inst = new WeaveFactory();
      }
    }
    return inst;
  }

  public IWeave getLogWeave(String name) {
    if (this.logWeaveInst.containsKey(name)) {
      return this.logWeaveInst.get(name);
    }
    String className = this.logWeaveName.get(name);
    if (className == null)
    {
      return null;
    }
    try {
      IWeave weave = (IWeave)Class.forName(className).newInstance();
      this.logWeaveInst.put(name, weave);
      return weave;
    } catch (Throwable t) {
      t.printStackTrace();
      return null;
    }
  }
}
