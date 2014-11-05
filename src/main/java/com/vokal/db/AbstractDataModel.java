package com.vokal.db;

import android.content.*;
import android.net.Uri;
import android.provider.BaseColumns;

import java.util.List;

import com.vokal.db.util.CursorGetter;

public abstract class AbstractDataModel implements DataModelInterface, BaseColumns {

    private static final String   WHERE_ID = _ID + "=?";
    private final        String[] ID_ARG   = new String[1];

    protected transient long _id;

    protected AbstractDataModel() {}

    protected AbstractDataModel(CursorGetter aGetter) {
        if (aGetter.hasColumn(_ID) && !aGetter.isNull(_ID)) {
            _id = aGetter.getLong(_ID);
        }
    }

    public Uri save(Context aContext) {
        ContentValues values = new ContentValues();
        populateContentValues(values);
        if (hasId()) values.put(_ID, _id);

        int updated = 0;
        Uri uri = getContentItemUri();
        if (uri != null) {
            updated = aContext.getContentResolver().update(uri, values, null, null);
        } else {
            // TODO:
            //  - test above update
            // - fall back to SELECT w/ ARG
        }
        if (updated == 0 || !hasId()) {
            uri = aContext.getContentResolver().insert(getContentUri(), values);
            try {
                _id = ContentUris.parseId(uri);
            } catch (Exception e) {
            }
        }

        return uri;
    }

    public boolean delete(Context aContext) {
        boolean result = false;
        if (hasId()) {
            ID_ARG[0] = Long.toString(_id);
            result = aContext.getContentResolver().delete(getContentUri(), WHERE_ID, ID_ARG) == 1;
        }
        return result;
    }

    public final Uri getContentItemUri() {
        return hasId() ? ContentUris.withAppendedId(getContentUri(), _id) : null;
    }

    public final Uri getContentUri() {
        return DatabaseHelper.getContentUri(((Object) this).getClass());
    }

    public static int bulkInsert(Context aContext, List<? extends AbstractDataModel> aModelList) {
        ContentValues[] values = new ContentValues[aModelList.size()];
        int index = 0;
        Uri uri = null;
        for (AbstractDataModel model : aModelList) {
            values[index] = new ContentValues();
            if (model.hasId())
                values[index].put(_ID, model._id);
            model.populateContentValues(values[index++]);
            if (uri == null) {
                uri = model.getContentUri();
            } else if (!model.getContentUri().equals(uri)) {
                throw new IllegalStateException("models must all be of the same concrete type to bulk insert");
            }
        }
        int result = 0;
        if (index > 0 && uri != null)
            result = aContext.getContentResolver().bulkInsert(uri, values);
        return result;
    }

    public boolean hasId() {
        return _id > 0;
    }

    public long getId() {
        return _id;
    }

    public void setId(long id) {
        _id = id;
    }

}
