package com.iwhalecloud.bss.uba.common.javaassist.weave;

import java.util.List;

public class DefaultWeaveActuator implements IWeaveActuator
{
  public WeaveCode executeWeave(WeaveContext weaveContext, List<IWeave> logWeaves) {
    if (logWeaves == null || logWeaves.size() == 0) {
      return null;
    }
    WeaveCode weaveCode = new WeaveCode();
    for (IWeave logWeave : logWeaves) {
      weaveCode.append(WeaveCode.BEFORE, logWeave.beforWeave(weaveContext));
      weaveCode.append(WeaveCode.EXCEPTION, logWeave.exceptionWeave(weaveContext));
      weaveCode.append(WeaveCode.AFTER, logWeave.afterWeave(weaveContext));
      weaveCode.append(WeaveCode.FINALLY, logWeave.finallyWeave(weaveContext));
    }

    return weaveCode;
  }
}