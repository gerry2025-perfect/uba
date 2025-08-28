package com.iwhalecloud.bss.uba.common;

import com.iwhalecloud.bss.uba.common.exception.UbaException;

/**用来定义返回对象，业务侧处理结果都返回这个对象*/
public class ResultEntity {

    public final static String SUCCESS = "0";

    private ResultFlag resultFlag = ResultFlag.SUCCESS;
    private String resultCode = SUCCESS;
    private String resultMsg;
    private String errorInfo;
    private Object data;

    /**构造成功的返回对象*/
    public static ResultEntity successResult(Object data){
        return new ResultEntity(ResultFlag.SUCCESS,SUCCESS,ResultFlag.SUCCESS.getFlag(),null,data,false);
    }

    /**构造处理失败的返回对象*/
    public static ResultEntity failResult(String resultCode,String resultMsg,Object... params){
        return new ResultEntity(ResultFlag.FAILURE,resultCode,resultMsg,null,null,false,params);
    }

    /**构造处理异常的返回对象*/
    public static ResultEntity errorResult(UbaException e,boolean includeStack,String resultMsg,Object... params){
        return new ResultEntity(ResultFlag.ERROR,null,resultMsg,e,null,includeStack,params);
    }

    public ResultEntity(ResultFlag resultFlag, String resultCode, String resultMsg, UbaException e, Object data, boolean includeStack, Object... params){
        this.resultFlag = resultFlag;
        this.resultCode = resultCode;
        this.resultMsg = resultMsg;
        this.data = data;
        if(e!=null){
            this.resultFlag = ResultFlag.ERROR;
            this.resultCode = e.getErrorCode();
            if(includeStack){
                this.errorInfo = CommonUtils.getCompactStackTrace(e, 10, 5);
            }
            this.resultMsg = this.resultMsg==null?e.getMessage():resultMsg + " - " + e.getMessage();
        }
        if(this.resultMsg!=null && params!=null && params.length>0){
            this.resultMsg = String.format(this.resultMsg, params);
        }
    }

    public ResultFlag getResultFlag() {
        return resultFlag;
    }

    public String getResultCode() {
        return resultCode;
    }

    public String getResultMsg() {
        return resultMsg;
    }

    public String getErrorInfo() {
        return errorInfo;
    }

    public Object getData() {
        return data;
    }

    public static enum ResultFlag{
        SUCCESS("success"),FAILURE("failure"),ERROR("error"),RETRY("retry");

        /**用来上游展现*/
        private String flag;
        ResultFlag(String flag){
            this.flag = flag;
        }

        public String getFlag() {
            return flag;
        }

        @Override
        public String toString() {
            return flag;
        }
    }

}
