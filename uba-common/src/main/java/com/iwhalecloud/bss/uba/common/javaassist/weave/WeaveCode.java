package com.iwhalecloud.bss.uba.common.javaassist.weave;

public class WeaveCode
{
  public static final int BEFORE = 0;
  public static final int TRY = 1;
  public static final int AFTER = 2;
  public static final int EXCEPTION = 3;
  public static final int FINALLY = 4;
  private StringBuffer beforeCode = new StringBuffer();
  private StringBuffer exceptionCode = new StringBuffer();
  private StringBuffer afterCode = new StringBuffer();
  private StringBuffer tryCode = new StringBuffer();
  private StringBuffer finallyCode = new StringBuffer();

  private int type;

  public int getType() { return this.type; }

  public StringBuffer getBeforeCode() { return this.beforeCode; }

  public StringBuffer getTryCode() { return this.tryCode; }

  public StringBuffer getAfterCode() { return this.afterCode; }

  public StringBuffer getExceptionCode() { return this.exceptionCode; }

  public StringBuffer getFinallyCode() { return this.finallyCode; }

  public void append(int position, String code) {
    if (code == null || code.trim().length() == 0) {
      return;
    }
    switch (position) {
      case BEFORE:
        this.type |= 0x1;
        this.beforeCode.append(code);
        break;
      case TRY:
        this.type |= 0xF;
        this.tryCode.append(code);
        break;
      case EXCEPTION:
        this.type |= 0xF;
        this.exceptionCode.append(code);
        break;
      case AFTER:
        if (this.type == 1) {
          this.type = 7;
        } else {
          this.type |= 0x1;
        }
        this.afterCode.append(code);
        break;
      case FINALLY:
        this.type |= 0xF;
        this.finallyCode.append(code);
        break;
    }
  }
}
