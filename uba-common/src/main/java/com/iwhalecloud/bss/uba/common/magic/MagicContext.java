package com.iwhalecloud.bss.uba.common.magic;

import com.iwhalecloud.bss.uba.common.TransactionIdGenerator;
import org.apache.commons.lang3.StringUtils;

//用来存储Magic相关的上下文信息
public class MagicContext {
    /**magic执行时候的tracingId，用来串接日志*/
    private static final ThreadLocal<String> magicTracingId = new InheritableThreadLocal<>();
    /**标识当前线程是否需要做规格参数转换，需要兼容前台配置和后台使用的情况，加载逻辑是同样的方法，需要有标识*/
    private static ThreadLocal<Boolean> replaceFlag = new InheritableThreadLocal<>();

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

    /**获取当前线程替换标识*/
    public static Boolean getReplaceFlag() {
        return replaceFlag.get() == null || replaceFlag.get();
    }
    /**设置当前线程替换标识*/
    public static void setReplaceFlag(Boolean replaceFlag) {
        MagicContext.replaceFlag.set(replaceFlag);
    }
    /**清除替换标识*/
    public static void clearReplaceFlag(){
        replaceFlag.remove();
    }
}
