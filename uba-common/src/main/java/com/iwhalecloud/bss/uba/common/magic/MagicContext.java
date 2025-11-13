package com.iwhalecloud.bss.uba.common.magic;

import com.iwhalecloud.bss.uba.common.TransactionIdGenerator;
import org.apache.commons.lang3.StringUtils;

//用来存储Magic相关的上下文信息
public class MagicContext {
    /**magic执行时候的tracingId，用来串接日志*/
    private static final ThreadLocal<String> magicTracingId = new InheritableThreadLocal<>();

    /**设置magicTracingId*/
    public static String getMagicTracingId() {
        return magicTracingId.get();
    }

    /**移除magicTracingId*/
    public static void removeMagicTracingId(){
        magicTracingId.remove();
    }

    /**设置magicTracingId*/
    public static void setMagicTracingId(String tracingId){
        if(StringUtils.isBlank(tracingId)){
            tracingId = TransactionIdGenerator.generate();
        }
        magicTracingId.set(tracingId);
    }


}
