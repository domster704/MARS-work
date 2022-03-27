package com.amber.armtp.aspects;

import com.amber.armtp.GlobalVars;

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
        GlobalVars.CurAc.runOnUiThread(() -> {
            try {
                joinPoint.proceed();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }
}
