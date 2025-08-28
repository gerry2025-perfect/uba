package com.iwhalecloud.bss.uba.common.exception;

import lombok.Getter;

@Getter
public enum ExceptionDefine {

    COMM_ERROR_TYPE_CONVERT("UBA_COMM_ERROR_00001", "value type no match"),
    COMM_ERROR_TYPE_CONVERT_JSON("UBA_COMM_ERROR_00002", "can not convert json"),
    COMM_ERROR_TYPE("UBA_COMM_ERROR_00003", "type not match"),
    COMM_ERROR_TYPE_NULL("UBA_COMM_ERROR_00004", "current value is null"),
    COMM_ERROR_TYPE_CHARSET_NOT_SUPPORT("UBA_COMM_ERROR_00005", "current charset is not support"),
    COMM_ERROR_TYPE_FILE_NOT_EXISTS("UBA_COMM_ERROR_00006", "file not exists"),

    MQ_ERROR_TYPE_SEND("UBA_MQ_ERROR_00001", "send MQ error"),
    MQ_ERROR_TYPE_SEND_FAIL("UBA_MQ_ERROR_00002", "send MQ fail"),
    MQ_ERROR_TYPE_OPERATE_FAIL("UBA_MQ_ERROR_00003", "operate MQ fail"),
    BTC_ERROR_TYPE_VALIDATE_FAIL("UBA_MQ_ERROR_00004", "BTC message validate fail");

    private final String errorCode;
    private final String errorMsg;

    private ExceptionDefine(String errorCode,String errorMsg){
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

}
