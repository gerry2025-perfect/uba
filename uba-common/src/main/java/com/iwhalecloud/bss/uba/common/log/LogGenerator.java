package com.iwhalecloud.bss.uba.common.log;

import com.iwhalecloud.bss.uba.common.CommonUtils;
import com.iwhalecloud.bss.uba.common.TransactionIdGenerator;
import com.iwhalecloud.bss.uba.adapter.log.UbaLogger;

import java.util.Date;
import java.util.Stack;

/**
 * 运行跟踪日志生成器，生成出能跟踪调用过程的日志信息
 * 需要能支持如下步骤：
 * 1、初始化日志：放在方法入口，用来标识日志的起点，因为是日志链，所以需要控制是首次日志初始化，还是非首次，需要区分tracingId是否新生成、spanId是否新生成
 * 2、日志入参信息写入：向日志中写入请求信息，记录操作发起
 * 3、日志出餐信息写入：向日志中写入响应信息，记录操作结束
 * 4、汇总单条日志做闭环：自动计算完整日志信息，包括记录总执行时间
 * 考虑到子线程的情况，tracingId、spanId需要放在单独的InheritableThreadLocal对象中，确保子线程不能篡改；
 * */
public class LogGenerator {

    private static final UbaLogger logger = UbaLogger.getLogger(LogGenerator.class);

    /**当前线程的tracingId*/
    protected final ThreadLocal<String> tracingId = new InheritableThreadLocal<>();
    /**当前线程的spanId，格式为：a.b.c.d*/
    protected final ThreadLocal<Stack<String>> spanId = new InheritableThreadLocal<>();
    /**当前序列*/
    protected final ThreadLocal<Stack<Integer>> seq = new ThreadLocal<>();
    /**初始序列*/
    protected final int INIT_SEQ = 1;

    /**标识当前是否正常走完本方法*/
    protected final ThreadLocal<Boolean> isDirectEnd = new ThreadLocal<>();

    protected LogWriter logWriter;

    public LogGenerator(LogWriter logWriter){
        this.logWriter = logWriter;
    }

    /**清理当前线程中的值*/
    public void clear(){
        tracingId.remove();
        spanId.get().clear();
        seq.get().clear();
        isDirectEnd.remove();
    }

    /**构造当前日志ID*/
    public String getLogId(){
        return tracingId.get() + "." + spanId.get().peek();
    }

    /**进入方法时调用*/
    public LogInfo inMethod(String logType, String url, String method, String[] paramCodes, Object[] paramValues){
        if (isDirectEnd.get()==null){
            isDirectEnd.set(true);
        }
        if(tracingId.get()==null) {
            tracingId.set(genTracingId());
        }
        if(spanId.get()==null){
            spanId.set(new Stack<>());
        }
        if(seq.get()==null){
            seq.set(new Stack<>());
        }

        if(isDirectEnd.get()){
            increateSpan();
        }else{
            extendSpan();
        }
        String logId = getLogId();
        logWriter.write(LogInfo.RequestInfo.builder()
                .logId(logId).url(url).method(method).reqDate(new Date()).reqType("jvm")
                .build());
        if(paramCodes!=null && paramCodes.length>0){
            String paramJson = "";
            for (int i=0; i<paramCodes.length; i++){
                paramJson = CommonUtils.convertToString(paramValues[i]);
                logWriter.write(LogInfo.ReqParamInfo.builder()
                        .logId(logId)
                        .paramCode(paramCodes[i])
                        .paramType(paramValues[i]==null?"" : paramValues[i].getClass().getName())
                        .paramSeq(i).paramJson(paramJson).paramSize(paramJson.length())
                        .build());
            }
        }
        return LogInfo.builder().logId(logId)
                .tracingId(tracingId.get())
                .spanId(spanId.get().peek())
                .seq(seq.get().peek())
                .reqDate(new Date())
                .logType(logType).build();

    }

    /**离开方法*/
    public void outMethod(Object result, Throwable e){
        String logId = getLogId();
        boolean success = true;
        String resultJson = "";
        try {
            if (e != null) {
                success = false;
                resultJson = CommonUtils.getCompactStackTrace(e, 20, 10);
            } else {
                resultJson = CommonUtils.convertToString(result);
            }
            logWriter.write(LogInfo.ResponseInfo.builder()
                    .logId(logId)
                    .respDate(new Date()).success(success)
                    .build());
            logWriter.write(LogInfo.RespParamInfo.builder()
                    .logId(logId)
                    .paramType(result.getClass().getName())
                    .paramJson(resultJson).paramSize(resultJson.length())
                    .build());
        } catch (Exception ex) {
            logger.warn("outMethod record log fail", ex);
        }finally {
            isDirectEnd.set(true);
            spanId.get().pop();
            seq.get().pop();
        }

    }

    /**生成tracingId*/
    public String genTracingId(){
        return TransactionIdGenerator.generate();
    }

    /**尾分段加1*/
    public void increateSpan(){
        //首次进来的时候栈是空的，先初始化一个值进去
        if(spanId.get().isEmpty()) {
            spanId.get().push(String.valueOf(INIT_SEQ - 1));
        }
        String curSpanId = spanId.get().peek();
        int idx = curSpanId.lastIndexOf(".");
        int curSeq;
        String parSpanId = "";
        if(idx>=0){
            curSeq = Integer.parseInt(curSpanId.substring(idx+1));
            parSpanId = curSpanId.substring(0, idx);
        }else{
            curSeq = Integer.parseInt(curSpanId);
            parSpanId = "";
        }
        curSeq++;
        if(parSpanId.isEmpty()){
            spanId.get().push(String.valueOf(curSeq));
        }else {
            spanId.get().push(parSpanId + "." + curSeq);
        }
        seq.get().push(curSeq);
        isDirectEnd.set(false);
    }

    /**扩充子分段*/
    public void extendSpan(){
        //首次进来的时候栈是空的，先初始化一个值进去
        if(spanId.get().isEmpty()) {
            spanId.get().push(String.valueOf(INIT_SEQ - 1));
        }
        spanId.get().push(spanId.get().peek() + "." + INIT_SEQ);
        seq.get().push(INIT_SEQ);
    }


}
