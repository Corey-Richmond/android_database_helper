package com.vokal.codegen;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface Column {
    public Constraint[] constraint() default Constraint.none;
    public String defaultValue() default "";

    public enum Constraint {
        none,
        notNull,
        unique
    }
}
