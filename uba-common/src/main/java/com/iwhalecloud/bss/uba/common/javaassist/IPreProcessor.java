package com.iwhalecloud.bss.uba.common.javaassist;

import java.security.ProtectionDomain;

public interface IPreProcessor {
  void initialize();
  
  byte[] preProcess(ClassLoader paramClassLoader, String paramString, ProtectionDomain paramProtectionDomain, byte[] paramArrayOfByte);
}
