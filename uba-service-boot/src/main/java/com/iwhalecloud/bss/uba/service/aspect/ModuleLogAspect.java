package com.iwhalecloud.bss.uba.service.aspect;

import com.iwhalecloud.bss.uba.common.log.LogGenerator;
import com.ztesoft.zsmart.core.log.ZSmartLogger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ModuleLogAspect {

    private static final ZSmartLogger logger = ZSmartLogger.getLogger(ModuleLogAspect.class);

    private final LogGenerator logGenerator;

    public ModuleLogAspect(LogGenerator logGenerator) {
        this.logGenerator = logGenerator;
    }

    /**
     * 定义切点，拦截实现 DynamicModule 接口的类中，所有被 @Comment 注解标记的方法。
     */
    @Pointcut("@annotation(org.ssssssss.script.annotation.Comment)")
    public void dynamicModuleMethods() {
        logger.error("===============================================================");
    }

    /**
     * 环绕通知，在方法执行前后记录日志。
     *
     * @param joinPoint ProceedingJoinPoint
     * @return 方法执行结果
     * @throws Throwable 异常
     */
    @Around("dynamicModuleMethods()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        // 1. 获取方法签名，从中可以获得方法、参数名等详细信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        // 2. 获取当前执行的类名和方法名
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = signature.getName();

        // 3. 获取方法入参名称清单和入参值清单
        String[] parameterNames = signature.getParameterNames();
        Object[] parameterValues = joinPoint.getArgs();

        logGenerator.inMethod("Magic-api", className, methodName, parameterNames, parameterValues);
        try {
            // 执行目标方法
            Object result = joinPoint.proceed();
            logGenerator.outMethod(result, null);
            return result;
        } catch (Throwable throwable) {
            logGenerator.outMethod(null, throwable);
            throw throwable;
        }
    }
}
