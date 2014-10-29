package com.vokal.db.codegen;


import android.content.ContentValues;
import android.os.Parcel;
import android.util.Log;

import com.vokal.db.AbstractDataModel;
import com.vokal.db.SQLiteTable;
import com.vokal.db.util.CursorCreator;
import com.vokal.db.util.CursorGetter;

public class DataModel extends AbstractDataModel {

    ModelHelper mClass;

    public DataModel() {
        try {
            mClass = ((ModelHelper) Class.forName(getClass().getName() + "Helper").newInstance());
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    protected DataModel(Parcel aSource) {
        super(aSource);
    }

    protected DataModel(CursorGetter aGetter) {
        super(aGetter);
    }

    @Override
    public void onTableCreate(SQLiteTable.Builder aBuilder) {
        mClass.onTableCreate(aBuilder);
    }

    @Override
    public void onTableUpgrade(SQLiteTable.Upgrader aUpgrader, int aOldVersion) {}

    @Override
    public void populateContentValues(ContentValues contentValues) {
        mClass.populateContentValues(contentValues, this);
    }
}
