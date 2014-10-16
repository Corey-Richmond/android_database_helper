package com.vokal.db.codegen;


import android.content.ContentValues;
import android.os.Parcel;
import android.util.Log;

import com.vokal.db.AbstractDataModel;
import com.vokal.db.SQLiteTable;
import com.vokal.db.util.CursorCreator;
import com.vokal.db.util.CursorGetter;

public class DataModel extends AbstractDataModel implements Model {

    ModelHelper mClass;

    public DataModel() {
        try {
            mClass = ((ModelHelper) Class.forName(getClass().getName() + "Helper").newInstance());
            mClass.setObject(this);
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    protected DataModel(Parcel aSource) { super(aSource);}

    @Override
    public void populateContentValues(ContentValues contentValues) {
        mClass.populateContentValues(contentValues);
    }

}
