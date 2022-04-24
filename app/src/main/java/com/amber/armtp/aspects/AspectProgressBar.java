package com.amber.armtp.aspects;

import com.amber.armtp.GlobalVars;
import com.amber.armtp.ProgressBarLoading;
import com.amber.armtp.annotations.DelayedCalled;
import com.amber.armtp.annotations.PGShowing;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

@Aspect
public class AspectProgressBar {
    private final String pointCut = "@annotation(com.amber.armtp.annotations.PGShowing)";

    @Pointcut(pointCut)
    public void setPointCutPG() {
    }

    @Around("setPointCutPG()")
    public void setJoinPointPG(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        PGShowing delayedCalleds = signature.getMethod().getAnnotation(PGShowing.class);
        boolean isCanceled = delayedCalleds.isCanceled();
        ProgressBarLoading progressBarLoading;
        if (isCanceled) {
            progressBarLoading = new ProgressBarLoading(GlobalVars.CurFragmentContext, true, GlobalVars.downloadPhotoTread);
        } else {
            progressBarLoading = new ProgressBarLoading(GlobalVars.CurFragmentContext);
        }

        progressBarLoading.show();
        joinPoint.proceed();
        progressBarLoading.dismiss();
    }
}
