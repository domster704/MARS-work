package com.amber.armtp.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

@Aspect
public class AspectTimeLogger {
    private final String pointCut = "@annotation(com.amber.armtp.annotations.TimeLogger)";

    @Pointcut(pointCut)
    public void setPointCutTimeLogger() {
    }

    @Around("setPointCutTimeLogger()")
    public void setJoinPointTimeLogger(ProceedingJoinPoint joinPoint) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            long startTime = System.nanoTime();
            joinPoint.proceed();
            long endTime = System.nanoTime();
            long duration = (endTime - startTime);
//            Log.d("time", signature.getName() + " " + String.valueOf(duration / (1000000000f)) + " sec.");
            System.out.println(signature.getName() + " " + String.valueOf(duration / (1e9f)) + " sec.");
//            Config.sout(signature.getName() + " " + String.valueOf(duration / (1e9f)) + " sec.", Toast.LENGTH_LONG);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}
