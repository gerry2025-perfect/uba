package com.iwhalecloud.bss.uba.common.javaassist.weave;

import com.iwhalecloud.bss.uba.common.javaassist.config.Path;

import java.io.Serializable;

/**逻辑增强器接口*/
public interface IWeave extends Serializable {
  /**依赖的增强器清单*/
  public String[] getDependLogWeave();
  /**当前增强器名称*/
  public String getName();
  /**增强器优先级*/
  public int getOrder();
  /**适用的类或方法路径*/
  public Path[] getApplyPath();
  /**排除的方法名清单，支持正则表达式*/
  public String[] getExcludeMethodRegulars();


  /**补充在方法前的字符串*/
  public String beforWeave(WeaveContext paramWeaveContext);
  /**补充在try中的字符串*/
  public String tryWeave(WeaveContext paramWeaveContext);
  /**补充在异常中的字符串*/
  public String exceptionWeave(WeaveContext paramWeaveContext);
  /**补充在方法执行之后的字符串*/
  public String afterWeave(WeaveContext paramWeaveContext);
  /**补充在finally中的字符串*/
  public String finallyWeave(WeaveContext paramWeaveContext);
}
