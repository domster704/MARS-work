package com.amber.armtp.aspects;

import android.os.Handler;

import com.amber.armtp.Config;
import com.amber.armtp.annotations.DelayedCalled;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;


@Aspect
public class AspectDelayedCalled {
    private final String pointCut = "@annotation(com.amber.armtp.annotations.DelayedCalled)";

    @Pointcut(pointCut)
    public void setPointCutDelay() {
    }

    @Around("setPointCutDelay()")
    public void setJoinPointDelay(final ProceedingJoinPoint joinPoint) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            DelayedCalled delayedCalleds = signature.getMethod().getAnnotation(DelayedCalled.class);
            int delay = delayedCalleds.delay();

            new Handler().postDelayed(() -> {
                try {
                    joinPoint.proceed();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }, delay);
        } catch (Exception e) {
            e.printStackTrace();
            Config.sout(e.getMessage());
        }
    }
}
