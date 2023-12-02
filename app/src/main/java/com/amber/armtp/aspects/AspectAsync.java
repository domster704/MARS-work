package com.amber.armtp.aspects;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

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
                Object target = joinPoint.getTarget();
                System.out.println(getContext(target));
//                Method[] m = target.getClass().getMethods();
//                for (Method i : m) {
//                    System.out.println(i.getName());
//                }
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });
        th.setPriority(10);
        th.start();
    }

    private AppCompatActivity getContext(Object object) {
        if (object instanceof AppCompatActivity) {
            return (AppCompatActivity) object;
        } else if (object instanceof Fragment) {
            Fragment fragment = (Fragment) object;
            return (AppCompatActivity) fragment.getActivity();
        } else if (object instanceof android.app.Fragment) {
            Fragment fragment = (Fragment) object;
            return (AppCompatActivity) fragment.getActivity();
        } else if (object instanceof View) {
            View view = (View) object;
            return (AppCompatActivity) view.getContext();
        }
        return null;
    }
}
