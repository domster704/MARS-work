package com.amber.armtp.aspects;

import com.amber.armtp.Config;
import com.amber.armtp.GlobalVars;
import com.amber.armtp.ProgressBarLoading;
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
        ProgressBarLoading progressBarLoading = null;
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            PGShowing delayedCalled = signature.getMethod().getAnnotation(PGShowing.class);
            boolean isCanceled = delayedCalled.isCanceled();
            if (isCanceled) {
                progressBarLoading = new ProgressBarLoading(GlobalVars.CurFragmentContext, true, GlobalVars.downloadPhotoTread);
            } else {
                progressBarLoading = new ProgressBarLoading(GlobalVars.CurFragmentContext);
            }
            GlobalVars.currentPB = progressBarLoading;

            progressBarLoading.show();
            joinPoint.proceed();
            progressBarLoading.dismiss();
        } catch (Exception e) {
            if (progressBarLoading != null)
                progressBarLoading.dismiss();
            e.printStackTrace();
            Config.sout(e.getMessage());
        }
    }
}
