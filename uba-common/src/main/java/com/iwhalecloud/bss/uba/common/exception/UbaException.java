package com.iwhalecloud.bss.uba.common.exception;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UbaException extends RuntimeException {
  private String errorCode = "CFG_ERROR_00000";
  private String errorMsg = "inner error";

  public UbaException(ExceptionDefine errorInfo,String busiMsg,Throwable e){
    super(errorInfo.getErrorCode()+":"+errorInfo.getErrorMsg(),e);
    errorCode = errorInfo.getErrorCode();
    errorMsg = errorInfo.getErrorMsg();
    if(busiMsg!=null){
      errorMsg += ":"+busiMsg;
    }
    if(e!=null && e.getMessage()!=null) errorMsg += ","+e.getMessage();
  }

  public UbaException(ExceptionDefine errorInfo,Throwable e){
    super(errorInfo.getErrorCode()+":"+errorInfo.getErrorMsg(),e);
    errorCode = errorInfo.getErrorCode();
    errorMsg = errorInfo.getErrorMsg();
    if(e!=null && e.getMessage()!=null) errorMsg += ","+e.getMessage();
  }

  public UbaException(ExceptionDefine errorInfo){
    super(errorInfo.getErrorCode()+":"+errorInfo.getErrorMsg());
    errorCode = errorInfo.getErrorCode();
    errorMsg = errorInfo.getErrorMsg();
  }

  public UbaException(String errorCode,String errorMsg,Throwable e){
    super(errorCode+":"+errorMsg,e);
    if(errorCode!=null) this.errorCode = errorCode;
    if(errorMsg!=null) this.errorMsg = errorMsg;
    if(e!=null && e.getMessage()!=null) this.errorMsg += ","+e.getMessage();
  }

    @Override
  public String getMessage() {
    return errorCode + ":" +errorMsg;
  }
}
