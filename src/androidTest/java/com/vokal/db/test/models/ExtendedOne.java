package com.vokal.db.test.models;


import com.vokal.codegen.Column;
import com.vokal.db.codegen.DataModel;

import static com.vokal.codegen.Column.Constraint.unique;

public class ExtendedOne extends DataModel {

    public @Column(constraint = {unique}) int     int1;
    public @Column                        String  string1;
    public @Column                        boolean boolean1;
    public @Column                        long    long1;
    public @Column                        double  double1;

}
