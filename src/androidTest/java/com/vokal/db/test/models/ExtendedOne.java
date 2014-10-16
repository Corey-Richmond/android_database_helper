package com.vokal.db.test.models;


import com.vokal.codegen.Column;
import com.vokal.codegen.Unique;
import com.vokal.db.codegen.DataModel;


public class ExtendedOne extends DataModel {

    @Unique
    @Column public int     int1; // Todo: autoincrement
    @Column public String  string1;
    @Column public boolean boolean1;
    @Column public long    long1;
    @Column public double  double1;

    public long getId() {
        return _id;
    }

    public void setId(long id) {
        this._id = id;
    }
}
