package com.vokal.db.test;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.test.ProviderTestCase2;

import java.util.ArrayList;
import java.util.Date;

import com.vokal.db.*;
import com.vokal.db.test.models.*;
import com.vokal.db.util.ObjectCursor;

public class AbstractDataModelTests extends ProviderTestCase2<SimpleContentProvider> {

    private Context mContext;

    public AbstractDataModelTests() {
        super(SimpleContentProvider.class, "com.vokal.database");
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getMockContext();
        DatabaseHelper.registerModel(mContext, ExtendedOne.class, ExtendedTwo.class, TestInterface.class);
    }

    public void testInsert() {
        ExtendedOne testModel = new ExtendedOne();
        testModel.boolean1 = false;
        testModel.double1 = 2.3;
        testModel.string1 = "test";
        testModel.long1 = 123123l;
        Uri uri = testModel.save(mContext);
        assertNotNull(uri);

        long id = testModel.getId();

        Cursor c = getMockContentResolver().query(DatabaseHelper.getContentUri(ExtendedOne.class),null,null,null,null);
        ObjectCursor<ExtendedOne> cursor = new ObjectCursor<>(c, ExtendedOne.class);
        if (cursor.moveToFirst()) {
            ExtendedOne m = cursor.getModel();
            assertEquals(false, m.boolean1);
            assertEquals(2.3, m.double1);
            assertEquals("test", m.string1);
            assertEquals(123123l, m.long1);
            assertEquals(id, m.getId());
        } else {
            assertFalse("cursor empty", true);
        }
    }

    public void testDelete() {
        ExtendedOne testModel = new ExtendedOne();
        testModel.boolean1 = false;
        testModel.double1 = 2.3;
        testModel.string1 = "test";
        testModel.long1 = 123123l;
        Uri uri = testModel.save(mContext);
        assertNotNull(uri);
        boolean success = testModel.delete(mContext);
        assertTrue(success);
    }


    public void testBulkInsert() {
        ExtendedOne testModel = new ExtendedOne();
        testModel.boolean1 = false;
        testModel.double1 = 1.3;
        testModel.string1 = "tasdf";
        testModel.long1 = 23123123l;

        ExtendedOne testModel2 = new ExtendedOne();
        testModel.boolean1 = true;
        testModel.double1 = 2.1;
        testModel.string1 = "aaaa";
        testModel.long1 = 2312l;

        ExtendedOne testModel3 = new ExtendedOne();
        testModel.boolean1 = false;
        testModel.double1 = 2.3;
        testModel.string1 = "test";
        testModel.long1 = 123123l;

        ArrayList<AbstractDataModel> models = new ArrayList<>();
        models.add(testModel);
        models.add(testModel2);
        models.add(testModel3);

        int count = ExtendedOne.bulkInsert(mContext, models);
        assertEquals(count, 3);

    }

    public void testUpdate() {
        ExtendedOne testModel = new ExtendedOne();
        testModel.boolean1 = false;
        testModel.double1 = 2.3;
        testModel.string1 = "test";
        testModel.long1 = 123123l;
        Uri uri = testModel.save(mContext);
        assertNotNull(uri);

        testModel.boolean1 = true;
        testModel.double1 = 4.1;

        uri = testModel.save(mContext);
        assertNotNull(uri);

        long id = testModel.getId();

        Cursor c = getMockContentResolver().query(DatabaseHelper.getContentUri(ExtendedOne.class),null,null,null,null);
        ObjectCursor<ExtendedOne> cursor = new ObjectCursor<>(c, ExtendedOne.class);
        if (cursor.moveToFirst()) {
            ExtendedOne m = cursor.getModel();
            assertEquals(true, m.boolean1);
            assertEquals(4.1, m.double1);
            assertEquals(id, m.getId());
        } else {
            assertFalse("cursor empty", true);
        }
    }

    public void testWipeDatabase() {
        ExtendedOne testModel = new ExtendedOne();
        testModel.boolean1 = false;
        testModel.double1 = 2.3;
        testModel.string1 = "test";
        testModel.long1 = 123123l;
        testModel.save(mContext);

        ExtendedTwo test2Model = new ExtendedTwo();
        test2Model.boolean1 = true;
        test2Model.double1 = 3.4;
        test2Model.string1 = "test2";
        test2Model.long1 = 555444333;
        test2Model.save(mContext);

        Cursor c = getMockContentResolver().query(DatabaseHelper.getContentUri(ExtendedOne.class),null,null,null,null);
        ObjectCursor<ExtendedOne> cursor = new ObjectCursor<>(c, ExtendedOne.class);
        if (cursor.moveToFirst()) {
            ExtendedOne m = cursor.getModel();
            assertEquals(false, m.boolean1);
            assertEquals(2.3, m.double1);
            assertEquals("test", m.string1);
            assertEquals(123123l, m.long1);
        } else {
            assertFalse("cursor empty", true);
        }

        c = getMockContentResolver().query(DatabaseHelper.getContentUri(ExtendedTwo.class),null,null,null,null);
        ObjectCursor<ExtendedTwo> cursor2 = new ObjectCursor<>(c, ExtendedTwo.class);
        if (cursor2.moveToFirst()) {
            ExtendedTwo m = cursor2.getModel();
            assertEquals(true, m.boolean1);
            assertEquals(3.4, m.double1);
            assertEquals("test2", m.string1);
            assertEquals(555444333, m.long1);
        } else {
            assertFalse("cursor empty", true);
        }

        DatabaseHelper.wipeDatabase(mContext);

        c = getMockContentResolver().query(DatabaseHelper.getContentUri(ExtendedOne.class),null,null,null,null);
        cursor = new ObjectCursor<ExtendedOne>(c, ExtendedOne.class);
        assertEquals(cursor.moveToFirst(), false);

        c = getMockContentResolver().query(DatabaseHelper.getContentUri(ExtendedTwo.class),null,null,null,null);
        cursor2 = new ObjectCursor<ExtendedTwo>(c, ExtendedTwo.class);
        assertEquals(cursor2.moveToFirst(), false);


    }

    public void testAutoIncrement() {
        ExtendedOne testModel = new ExtendedOne();
        testModel.boolean1 = false;
        testModel.double1 = 2.3;
        testModel.string1 = "test";
        testModel.long1 = 123123l;
        testModel.save(mContext);

        long id = testModel.getId();

        ExtendedOne test2Model = new ExtendedOne();
        test2Model.boolean1 = true;
        test2Model.double1 = 3.4;
        test2Model.string1 = "test2";
        test2Model.long1 = 555444333;
        test2Model.save(mContext);

        long id2 = test2Model.getId();

        assertEquals(id+1, id2);

    }

    public void testUniqueness() {
        ExtendedTwo testModel = new ExtendedTwo();
        testModel.boolean1 = false;
        testModel.double1 = 2.3;
        testModel.string1 = "test";
        testModel.long1 = 123123l;
        testModel.int1 = 12;
        testModel.save(mContext);

        assertEquals(testModel.int1, 12);

        Cursor c = getMockContentResolver().query(DatabaseHelper.getContentUri(ExtendedTwo.class),null,null,null,null);
        assertTrue(c.moveToFirst());
        assertEquals(c.getCount(), 1);

        ExtendedTwo test2Model = new ExtendedTwo();
        test2Model.boolean1 = true;
        test2Model.double1 = 3.4;
        test2Model.string1 = "test2";
        test2Model.long1 = 555444333;
        test2Model.int1 = 12;
        test2Model.save(mContext);
        assertEquals(test2Model.int1, 12);

        Cursor c2 = getMockContentResolver().query(DatabaseHelper.getContentUri(ExtendedTwo.class),null,null,null,null);
        ObjectCursor<ExtendedTwo> cursor2 = new ObjectCursor<>(c2, ExtendedTwo.class);
        assertTrue(c2.moveToFirst());
        assertEquals(c2.getCount(), 1);
        assertEquals(cursor2.getModel().double1, 3.4);
    }

    public void testParcelable() {
        //Create parcelable object and put to Bundle

        final long id = 123L;

        ExtendedTwo extendedTwo = new ExtendedTwo();
        extendedTwo.setId(id);
        extendedTwo.string1 = "parcelled";
        extendedTwo.boolean1 = true;
        extendedTwo.int1 = 12;
        extendedTwo.long1 = 555444333;
        extendedTwo.float1 = 123.456f;
        extendedTwo.double1 = 3.4;
        extendedTwo.date1 = new Date();

        extendedTwo.save(mContext);
        assertEquals(extendedTwo.int1, 12);
        Bundle b = new Bundle();
        b.putParcelable("e2", extendedTwo);

        //Save bundle to parcel
        Parcel parcel = Parcel.obtain();
        b.writeToParcel(parcel, 0);

        //Extract bundle from parcel
        parcel.setDataPosition(0);
        Bundle b2 = parcel.readBundle();
        b2.setClassLoader(ExtendedTwo.class.getClassLoader());
        ExtendedTwo e2 = b2.getParcelable("e2");

        //Check that objects are not same and test that objects are equal
        assertFalse("Bundle is the same", b2 == b);
        assertFalse("model is the same", e2 == extendedTwo);
        assertEquals(id, e2.getId());
        assertEquals("parcelled", e2.string1);
        assertTrue(e2.boolean1);
        assertEquals(extendedTwo.int1, e2.int1);
        assertEquals(extendedTwo.long1, e2.long1);
        assertEquals(extendedTwo.float1, e2.float1);
        assertEquals(extendedTwo.double1, e2.double1);
        assertEquals(extendedTwo.date1, e2.date1);
    }
}
