package com.vokal.codegen;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface Column {
    Constraint[] constraint() default Constraint.none;

    public enum Constraint {
        none,
        primaryKey,
        notNull,
        unique
    }
}
