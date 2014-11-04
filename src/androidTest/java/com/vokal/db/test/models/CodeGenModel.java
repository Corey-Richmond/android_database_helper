package com.vokal.db.test.models;

import java.util.Date;

import com.vokal.codegen.Column;
import com.vokal.db.codegen.DataModel;

public class CodeGenModel extends DataModel {

    /* Primitives */
    public @Column byte    byte_prim;
    public @Column short   short_prim;
    public @Column int     int_prim;
    public @Column long    long_prim;
    public @Column float   float_prim;
    public @Column double  double_prim;
    public @Column boolean boolean_prim;
    public @Column char    char_prim;

    /* Arrays */
    public @Column byte[]      byte_prim_array;
    public @Column Byte[]      byte_array;
    public @Column char[]      char_prim_array;
    public @Column Character[] character_array;

    /* Objects */
    public @Column String    string_object;
    public @Column Byte      byte_object;
    public @Column Short     short_object;
    public @Column Integer   integer_object;
    public @Column Long      long_object;
    public @Column Float     float_object;
    public @Column Double    double_object;
    public @Column Boolean   boolean_object;
    public @Column Character character_object;

    /* Misc. Objects */
    public @Column Date date_object;

    public CodeGenModel() {}
}
