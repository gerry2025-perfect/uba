package com.iwhalecloud.bss.uba.common.javaassist.weave;

import java.util.List;

public interface IWeaveActuator {
  WeaveCode executeWeave(WeaveContext paramWeaveContext, List<IWeave> paramList);
}