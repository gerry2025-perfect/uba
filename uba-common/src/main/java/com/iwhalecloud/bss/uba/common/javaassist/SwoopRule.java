package com.iwhalecloud.bss.uba.common.javaassist;

import com.iwhalecloud.bss.uba.common.javaassist.config.Path;
import com.iwhalecloud.bss.uba.common.javaassist.weave.IWeave;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SwoopRule
{
  private Path path;
  private List<SwoopMethodInfo> methodInfoList;
  private List<IWeave> weaves;
  private Map<String, String> mcodeMap ;

}
