package com.amber.armtp.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class AspectAsync {
    private final String pointCut = "@annotation(com.amber.armtp.annotations.Async)";

    @Pointcut(pointCut)
    public void setPointCutThread() {
    }

    @Around("setPointCutThread()")
    public void setJoinPointThread(final ProceedingJoinPoint joinPoint) {
        Thread th = new Thread(() -> {
            try {
                joinPoint.proceed();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });
        th.setPriority(10);
        th.start();
    }
}
