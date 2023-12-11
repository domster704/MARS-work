package com.amber.armtp.aspects;

import android.os.Handler;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class AspectAsyncUI {
    private final String pointCut = "@annotation(com.amber.armtp.annotations.AsyncUI)";

    @Pointcut(pointCut)
    public void setPointCutThreadUI() {
    }

    @Around("setPointCutThreadUI()")
    public void setJoinPointThreadUI(final ProceedingJoinPoint joinPoint) {
        new Handler().post(() -> {
            try {
                joinPoint.proceed();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }
}
