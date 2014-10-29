package com.vokal.db.codegen;

import android.content.ContentValues;

import com.vokal.db.SQLiteTable;

public interface ModelHelper {
    void onTableCreate(SQLiteTable.Builder aBuilder);
    void populateContentValues(ContentValues aValues, Object a);
}
