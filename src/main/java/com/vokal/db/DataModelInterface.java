package com.vokal.db;

import android.content.ContentValues;

import static com.vokal.db.SQLiteTable.Builder;

public interface DataModelInterface {

    /*
     * table creation: add your model field columns and constraints using CreateBuilder
     */
    void onTableCreate(Builder aBuilder);

    /*
     * upgrades: re-create or add tables, index, seed data using UpgradeBuilder
     */
    void onTableUpgrade(SQLiteTable.Upgrader aUpgrader, int aOldVersion);

    /*
     * put values to be saved
     */
    void populateContentValues(ContentValues aValues);

}
