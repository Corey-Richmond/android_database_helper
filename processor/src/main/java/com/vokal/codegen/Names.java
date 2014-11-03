package com.vokal.codegen;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Names {
    public String[] value() default {};
}
