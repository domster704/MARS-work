package com.amber.armtp.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class AspectAsync {
    private final String pointCut = "@annotation(com.amber.armtp.interfaces.Async)";

    @Pointcut(pointCut)
    public void setPointCutThread() {
    }

    @Around("setPointCutThread()")
    public void setJoinPointThread(final ProceedingJoinPoint joinPoint) {
        new Thread(() -> {
            try {
                System.out.println(1);
                joinPoint.proceed();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }).start();
    }
}