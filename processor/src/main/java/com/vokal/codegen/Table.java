package com.vokal.codegen;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Table {
    public String[] primaryKeys() default {};
    public String[] uniqueColumns() default {};
    public Names[] indexColumns() default {};
}
