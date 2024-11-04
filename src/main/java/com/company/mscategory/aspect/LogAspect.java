package com.company.mscategory.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LogAspect {

    private static final Logger log = LoggerFactory.getLogger(LogAspect.class);

    @Around("@annotation(com.company.mscategory.aspect.Log)")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        var methodName = joinPoint.getSignature().getName();
        var className = joinPoint.getTarget().getClass().getSimpleName();
        var args = joinPoint.getArgs();
        log.info("Entering method: {} in class: {} with arguments: {}", methodName, className, Arrays.toString(args));
        var result = new Object();
        try {
            result = joinPoint.proceed();
            log.info("Method: {} in class: {} returned: {}", methodName, className, result);
        } catch (Exception ex) {
            log.error("Exception in method: {} in class: {} with message: {}", methodName, className, ex.getMessage());
            throw ex;
        }
        return result;
    }
}
