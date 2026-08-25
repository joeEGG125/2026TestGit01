package com.syscom.fep.frmcommon.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Number {
    /**
     * 整數位數
     *
     * @return
     */
    int integerDigits();

    /**
     * 小數點位數
     *
     * @return
     */
    int scale() default 0;
}
