package de.uni.bremen.monty.moco.test.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Monty {
    String value();

    Class<? extends Throwable> expect() default None.class;

    String matching() default "";
}
