package com.amber.armtp.annotations;

import com.amber.armtp.GlobalVars;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PGShowing {
    boolean isCanceled() default false;
}
