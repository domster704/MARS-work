package com.amber.armtp.aspects;

import com.amber.armtp.GlobalVars;
import com.amber.armtp.ProgressBarLoading;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class AspectProgressBar {
    private final String pointCut = "@annotation(com.amber.armtp.interfaces.PGShowing)";

    @Pointcut(pointCut)
    public void PointCutPG() {
    }

    @Around("PointCutPG()")
    public void weavePGJoinPointBefore(ProceedingJoinPoint joinPoint) throws Throwable {
        ProgressBarLoading progressBarLoading = new ProgressBarLoading(GlobalVars.CurAc);

        progressBarLoading.show();
        joinPoint.proceed();
        progressBarLoading.stop();
    }
}
