package com.vokal.db.util;

import android.annotation.TargetApi;
import android.database.Cursor;
import android.os.Build;
import android.support.v4.util.SimpleArrayMap;

import java.util.Date;

import static android.text.TextUtils.isEmpty;

public class CursorGetter {

    private SimpleArrayMap<String, Integer> mMap = new SimpleArrayMap<>(12);
    private Cursor mCursor;
    private String mTable;
    
    public CursorGetter() {
        this(null, null);
    }

    public CursorGetter(Cursor aCursor) {
        this(aCursor, null);
    }

    public CursorGetter(Cursor aCursor, String aTableName) {
        mCursor = aCursor;
        setTable(aTableName);
    }

    public void swapCursor(Cursor c, boolean flushCache) {
        mCursor = c;
        if (flushCache) {
            clearColumnCache();
        }
    }

    public CursorGetter setTable(String aTableName) {
        if (isEmpty(aTableName)) {
            mTable = null;
        } else {
            mTable = aTableName.concat("_");
        }
        return this;
    }

    public boolean hasColumn(String aColumn) {
        return getColumnIndex(aColumn) != -1;
    }

    public boolean getBoolean(String aColumn) {
        return mCursor.getInt(getColumnIndex(aColumn)) != 0;
    }

    public String getString(String aColumn) {
        return mCursor.getString(getColumnIndex(aColumn));
    }

    public short getShort(String aColumn) {
        return mCursor.getShort(getColumnIndex(aColumn));
    }

    public int getInt(String aColumn) {
        return mCursor.getInt(getColumnIndex(aColumn));
    }

    public long getLong(String aColumn) {
        return mCursor.getLong(getColumnIndex(aColumn));
    }

    public float getFloat(String aColumn) {
        return mCursor.getFloat(getColumnIndex(aColumn));
    }

    public double getDouble(String aColumn) {
        return mCursor.getDouble(getColumnIndex(aColumn));
    }

    public byte[] getBlob(String aColumn) {
        return mCursor.getBlob(getColumnIndex(aColumn));
    }

    public Byte[] getByteArray(String aColumn) {
        byte[] b = mCursor.getBlob(getColumnIndex(aColumn));
        if (b == null) return null;
        Byte[] B = new Byte[b.length];
        for (int i = 0; i < b.length; i++)
            B[i] = b[i];
        return B;
    }

    public char[] getCharArray(String aColumn) {
        String charString = mCursor.getString(getColumnIndex(aColumn));
        if (charString == null) return null;
        return charString.toCharArray();
    }

    public Character[] getCharacterArray(String aColumn) {
        String charString = mCursor.getString(getColumnIndex(aColumn));
        if (charString == null) return null;
        char[] c = charString.toCharArray();
        Character[] C = new Character[c.length];
        for (int i = 0; i < c.length; i++)
            C[i] = c[i];
        return C;
    }

    public Date getDate(String aColumn) {
        int i = getColumnIndex(aColumn);
        if (i >= 0 && !mCursor.isNull(i)) {
            return new Date(mCursor.getLong(i));
        }
        return null;
    }

    public boolean isNull(String aColumn) {
        return mCursor.isNull(getColumnIndex(aColumn));
    }

    private int getColumnIndex(String aColumn) {
        if (mTable != null) {
            aColumn = mTable.concat(aColumn);
        }

        if (mMap.containsKey(aColumn)) {
            return mMap.get(aColumn);
        } 

        int i = mCursor.getColumnIndex(aColumn);
        mMap.put(aColumn, i);
        return i;
    }

    public void clearColumnCache() {
        mMap.clear();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private int getType(String aColumn) {
        return mCursor.getType(mCursor.getColumnIndex(aColumn));
    }

}
