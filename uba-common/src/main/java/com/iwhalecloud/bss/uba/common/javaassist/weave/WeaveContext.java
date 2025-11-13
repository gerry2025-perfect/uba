package com.iwhalecloud.bss.uba.common.javaassist.weave;

import com.iwhalecloud.bss.uba.common.javaassist.SwoopRule;
import lombok.Data;

/**增强过程中的上线文数据对象*/
@Data
public class WeaveContext
{
  private String hashCode;
  private SwoopRule rule;
  private String className;
  private String methodName;
  private String[] paramNames;
  private String[] paramTypes;
  private boolean isCreateInParams;
  private boolean hasReturn;
  private int loggerStackDepth = 2;
  private String returnClass;

  private int paramNumber = 0;

  public void setReturnClass(String returnClass) {
    this.returnClass = returnClass;
    setHasReturn(!"void".equals(returnClass));
  }

  public String getHashCode() {
    if (this.hashCode == null) {
      this.hashCode = Integer.toString(Math.abs(this.className.hashCode()), 32);
    }
    return this.hashCode;
  }
}
