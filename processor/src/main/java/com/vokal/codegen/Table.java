package com.vokal.codegen;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Table {
    String[] primaryKeys() default {};
    String[] uniqueColumns() default {};
}
