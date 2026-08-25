package com.syscom.fep.frmcommon.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface StackTracePointCut {

    /**
     * 呼叫程式的名稱
     * @return
     */
    String caller() default "";

    /**
     * 呼叫程式的方法
     * @return
     */
    String method() default "";

}
