package com.iwhalecloud.bss.uba.common.log;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**结构化日志信息*/
@Data
@Builder
public class LogInfo {

    //日志ID：tracingId + "." + spanId + "." + seq
    private String logId;
    //全局跟踪ID
    private String tracingId;
    //段落ID
    private String spanId;
    //所在段落当前序号
    private Integer seq;
    //请求发起时间
    private Date reqDate;
    //响应时间
    private Date respDate;
    //运行时间，respDate - reqDate
    private int invokeTime;
    //发起方IP
    private String reqIP;
    //发起方服务器
    private String reqServ;
    //日志类型，用来标识日志链是什么类型的日志，比如magic执行日志、业务日志等等
    private String logType;



    @Data
    @Builder
    public static class RequestInfo{
        //日志ID
        private String logId;
        //请求类型，jvm、http、dubbo
        private String reqType;
        //请求URL
        private String url;
        //请求方法
        private String method;
        //请求时间
        private Date reqDate;
    }

    @Data
    @Builder
    public static class ReqParamInfo{
        //日志ID
        private String logId;
        //请求参数json
        private String paramJson;
        //参数序号
        private int paramSeq;
        //参数大小
        private int paramSize;
        //参数类型
        private String paramType;
        //参数编码
        private String paramCode;
    }

    @Data
    @Builder
    public static class ResponseInfo{
        //日志ID
        private String logId;
        //响应时间
        private Date respDate;
        //是否成功
        private Boolean success;
    }

    @Data
    @Builder
    public static class RespParamInfo{
        //日志ID
        private String logId;
        //请求参数json
        private String paramJson;
        //参数大小
        private int paramSize;
        //参数类型
        private String paramType;
    }

}
