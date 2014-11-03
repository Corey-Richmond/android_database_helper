package com.vokal.db.test.models;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

import com.vokal.codegen.*;
import com.vokal.db.codegen.DataModel;

import static com.vokal.codegen.Column.Constraint.unique;

@Table(indexColumns = @Names("date1"),
       primaryKeys = {"date1"})
public class ExtendedTwo extends DataModel implements Parcelable {

    public @Column(constraint = unique) int     int1;
    public @Column                      Date    date1;
    public @Column                      String  string1;
    public @Column                      boolean boolean1;
    public @Column                      long    long1;
    public @Column                      float   float1;
    public @Column                      double  double1;

    public ExtendedTwo() {}

    public ExtendedTwo(Parcel aSource) {
//        super(aSource);
        long date = aSource.readLong();
        date1 = date == -1 ? null : new Date(date);
        string1 = aSource.readString();
        boolean1 = aSource.readInt() == 1;
        int1 = aSource.readInt();
        long1 = aSource.readLong();
        float1 = aSource.readFloat();
        double1 = aSource.readDouble();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
//        super.writeToParcel(dest, flags);
        dest.writeLong(date1 == null ? -1 : date1.getTime());
        dest.writeString(string1);
        dest.writeInt(boolean1 ? 1 : 0);
        dest.writeInt(int1);
        dest.writeLong(long1);
        dest.writeFloat(float1);
        dest.writeDouble(double1);
    }

    public long getId() {
        return _id;
    }

    public void setId(long id) {
        this._id = id;
    }
}
