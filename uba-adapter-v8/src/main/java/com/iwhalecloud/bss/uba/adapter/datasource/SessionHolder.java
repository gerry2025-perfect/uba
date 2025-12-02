package com.iwhalecloud.bss.uba.adapter.datasource;

import com.iwhalecloud.bss.uba.adapter.log.UbaLogger;
import com.ztesoft.zsmart.core.exception.BaseAppException;
import com.ztesoft.zsmart.core.jdbc.Session;
import com.ztesoft.zsmart.core.jdbc.SessionContext;

import java.util.Stack;

/**数据库Session持有，因为V8需要在执行数据库操作之前开启事务*/
public class SessionHolder {

    private final static UbaLogger logger = UbaLogger.getLogger(SessionHolder.class);

    private final static ThreadLocal<Stack<Session>> session = new ThreadLocal<>();

    /**初始化数据库session，如果设置为继承，会判断当前上下文是否有session，如果有则直接用，没有才需要新增*/
    public static void initSession(boolean inherit){
        if(session.get()==null){
            session.set(new Stack<>());
        }
        try {
            Session curSession ;
            if(session.get().isEmpty() || !inherit){
                curSession = SessionContext.newSession();
                session.get().push(curSession);
            }
            curSession = session.get().peek();
            curSession.beginTrans();
        } catch (BaseAppException e) {
            throw new RuntimeException(e);
        }
    }

    /**关闭session，如果设置提交*/
    public static void closeSession(boolean commit){
        if (session.get()==null || session.get().isEmpty()){
            logger.warn("There is no session in the current thread, please make sure that the initSession method is called normally");
        }
        Session currentSession = session.get().pop();
        boolean isCommited = false;
        try {
            if(commit) {
                isCommited = currentSession.commitTrans();
            }else{
                //没有设置成commit，标识需要回滚，所以直接设置成已commit，这样finally中才会释放，以此达到回滚的目的
                isCommited = true;
            }
        } catch (BaseAppException e) {
            throw new RuntimeException(e);
        }finally {
            try {
                if(isCommited) {
                    currentSession.releaseTrans();
                }
            }catch (Exception e){
                logger.error(String.format("release current session [%s] error", currentSession.getSessionID()), e);
            }
        }
    }

}
