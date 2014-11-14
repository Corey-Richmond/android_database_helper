package com.vokal.db.test;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.test.ProviderTestCase2;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;

import com.vokal.db.DatabaseHelper;
import com.vokal.db.SimpleContentProvider;
import com.vokal.db.codegen.DataModel;
import com.vokal.db.test.models.*;
import com.vokal.db.util.ObjectCursor;

public class CodeGenTests extends ProviderTestCase2<SimpleContentProvider> {

    private Context mContext;

    public CodeGenTests() {
        super(SimpleContentProvider.class, "com.vokal.database");
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getMockContext();
        DatabaseHelper.registerModel(mContext, RegisterTablesHelper.getTables());
        DatabaseHelper.wipeDatabase(mContext);
    }

    public void testInsert() {
        CodeGenModel model = new CodeGenModel();
        model.byte_prim = 0;
        model.short_prim = 1;
        model.int_prim = 2;
        model.long_prim = 3l;
        model.float_prim = 4.1f;
        model.double_prim = 5.1d;
        model.boolean_prim = true;
        model.char_prim = 'a';
        model.string_object = "String_Test";
        model.byte_object = 6;
        model.short_object = 7;
        model.integer_object = 8;
        model.long_object = 9L;
        model.float_object = 10.1F;
        model.double_object = 11.2D;
        model.boolean_object = true;
        model.character_object = 'B';
        model.date_object = new Date(1415116088347l);

        model.byte_prim_array = new byte[3];
        setbyteArray(model.byte_prim_array, 3);
        model.byte_array = new Byte[3];
        setByteArray(model.byte_array, 3);
        model.char_prim_array = new char[3];
        setCharArray(model.char_prim_array, 3);
        model.character_array = new Character[3];
        setCharacterArray(model.character_array, 3);

        Uri uri = model.save(mContext);
        assertNotNull(uri);

        long id = model.getId();

        Cursor c = getMockContentResolver().query(DatabaseHelper.getContentUri(CodeGenModel.class), null, null, null,
                                                  null);
        ObjectCursor<CodeGenModel> cursor = new ObjectCursor<>(c, CodeGenModel.class);
        if (cursor.moveToFirst()) {
            CodeGenModel m = cursor.getModel();
            assertEquals(0, m.byte_prim);
            assertEquals(1, m.short_prim);
            assertEquals(2, m.int_prim);
            assertEquals(3l, m.long_prim);
            assertEquals(4.1f, m.float_prim);
            assertEquals(5.1d, m.double_prim);
            assertEquals(true, m.boolean_prim);
            assertEquals('a', m.char_prim);
            assertEquals("String_Test", m.string_object);
            assertEquals(6, (int) m.byte_object);
            assertEquals(7, (int) m.short_object);
            assertEquals(8, (int) m.integer_object);
            assertEquals(9L, (long) m.long_object);
            assertEquals(10.1F, m.float_object);
            assertEquals(11.2D, m.double_object);
            assertEquals(true, (boolean) m.boolean_object);
            assertEquals('B', (char) m.character_object);
            assertEquals(new Date(1415116088347l).getTime(), m.date_object.getTime());

            assertArrayEquals(setbyteArray(new byte[3], 3), m.byte_prim_array);
            assertArrayEquals(setByteArray(new Byte[3], 3), m.byte_array);
            assertArrayEquals(setCharArray(new char[3], 3), m.char_prim_array);
            assertArrayEquals(setCharacterArray(new Character[3], 3), m.character_array);
        } else {
            assertFalse("cursor empty", true);
        }
    }

    public void testDelete() {
        CodeGenModel model = new CodeGenModel();
        model.byte_prim = 0;
        model.short_prim = 1;
        model.int_prim = 2;
        model.long_prim = 3l;
        model.float_prim = 4.1f;
        model.double_prim = 5.1d;

        Uri uri = model.save(mContext);
        assertNotNull(uri);
        boolean success = model.delete(mContext);
        assertTrue(success);
    }

    public void testBulkInsert() {
        CodeGenModel model1 = new CodeGenModel();
        model1.byte_prim = 0;
        model1.short_prim = 1;
        model1.int_prim = 2;
        model1.long_prim = 3l;
        model1.float_prim = 4.1f;
        model1.double_prim = 5.1d;
        model1.boolean_prim = true;
        model1.char_prim = 'a';
        model1.string_object = "String_Test";
        model1.byte_object = 6;
        model1.short_object = 7;
        model1.integer_object = 8;
        model1.long_object = 9L;
        model1.float_object = 10.1F;
        model1.double_object = 11.2D;
        model1.boolean_object = true;
        model1.character_object = 'B';
        model1.date_object = new Date(1415116088347l);

        model1.byte_prim_array = new byte[3];
        setbyteArray(model1.byte_prim_array, 3);
        model1.byte_array = new Byte[3];
        setByteArray(model1.byte_array, 3);
        model1.char_prim_array = new char[3];
        setCharArray(model1.char_prim_array, 3);
        model1.character_array = new Character[3];
        setCharacterArray(model1.character_array, 3);

        CodeGenModel model2 = new CodeGenModel();
        model2.byte_prim = 12;
        model2.short_prim = 13;
        model2.int_prim = 14;
        model2.long_prim = 15l;
        model2.float_prim = 16.1f;
        model2.double_prim = 17.1d;
        model2.boolean_prim = true;
        model2.char_prim = 'c';
        model2.string_object = "String_Test2";
        model2.byte_object = 18;
        model2.short_object = 19;
        model2.integer_object = 20;
        model2.long_object = 21L;
        model2.float_object = 22.1F;
        model2.double_object = 23.2D;
        model2.boolean_object = false;
        model2.character_object = 'D';
        model2.date_object = new Date(1415116088333l);

        model2.byte_prim_array = new byte[3];
        setbyteArray(model2.byte_prim_array, 3);
        model2.byte_array = new Byte[3];
        setByteArray(model2.byte_array, 3);
        model2.char_prim_array = new char[3];
        setCharArray(model2.char_prim_array, 3);
        model2.character_array = new Character[3];
        setCharacterArray(model2.character_array, 3);

        CodeGenModel model3 = new CodeGenModel();
        model3.byte_prim = 24;
        model3.short_prim = 25;
        model3.int_prim = 26;
        model3.long_prim = 27l;
        model3.float_prim = 28.1f;
        model3.double_prim = 29.1d;
        model3.boolean_prim = false;
        model3.char_prim = 'E';
        model3.string_object = "String_Test3";
        model3.byte_object = 30;
        model3.short_object = 31;
        model3.integer_object = 32;
        model3.long_object = 33L;
        model3.float_object = 34.1F;
        model3.double_object = 35.2D;
        model3.boolean_object = true;
        model3.character_object = 'f';
        model3.date_object = new Date(1415116988347l);

        model3.byte_prim_array = new byte[3];
        setbyteArray(model3.byte_prim_array, 3);
        model3.byte_array = new Byte[3];
        setByteArray(model3.byte_array, 3);
        model3.char_prim_array = new char[3];
        setCharArray(model3.char_prim_array, 3);
        model3.character_array = new Character[3];
        setCharacterArray(model3.character_array, 3);

        ArrayList<DataModel> models = new ArrayList<>();
        models.add(model1);
        models.add(model2);
        models.add(model3);

        int count = CodeGenModel.bulkInsert(mContext, models);
        assertEquals(count, 3);

    }

    public void testUpdate() {
        CodeGenModel model = new CodeGenModel();
        model.byte_prim = 0;
        model.short_prim = 1;
        model.int_prim = 2;
        model.long_prim = 3l;
        model.float_prim = 4.1f;
        model.double_prim = 5.1d;
        Uri uri = model.save(mContext);
        assertNotNull(uri);

        model.long_prim =  20l;
        model.double_prim = 17.3d;

        uri = model.save(mContext);
        assertNotNull(uri);

        long id = model.getId();

        Cursor c = getMockContentResolver().query(DatabaseHelper.getContentUri(CodeGenModel.class),null,null,null,null);
        ObjectCursor<CodeGenModel> cursor = new ObjectCursor<>(c, CodeGenModel.class);
        if (cursor.moveToFirst()) {
            CodeGenModel m = cursor.getModel();
            assertEquals(20l, m.long_prim);
            assertEquals(17.3d, m.double_prim);
            assertEquals(id, m.getId());
        } else {
            assertFalse("cursor empty", true);
        }
    }

    public void testUniqueness() {
        CodeGenModel model = new CodeGenModel();
        model.byte_prim = 0;
        model.short_prim = 1;
        model.int_prim = 2;
        model.long_prim = 3l;
        model.float_prim = 4.1f;
        model.double_prim = 5.1d;
        model.char_prim = 'A';
        model.save(mContext);

        assertEquals('A', model.char_prim);
        assertEquals(3l, model.long_prim);

        Cursor c = getMockContentResolver().query(DatabaseHelper.getContentUri(CodeGenModel.class),null,null,null,null);
        assertTrue(c.moveToFirst());
        assertEquals(1, c.getCount());

        CodeGenModel model2 = new CodeGenModel();
        model2.byte_prim = 11;
        model2.short_prim = 12;
        model2.int_prim = 13;
        model2.long_prim = 3l;
        model2.float_prim = 14.1f;
        model2.double_prim = 15.1d;
        model2.char_prim = 'A';
        model2.short_object = 77;
        model2.save(mContext);

        assertEquals('A', model2.char_prim);
        assertEquals(3l, model2.long_prim);

        Cursor c2 = getMockContentResolver().query(DatabaseHelper.getContentUri(CodeGenModel.class),null,null,null,null);
        ObjectCursor<CodeGenModel> cursor2 = new ObjectCursor<>(c2, CodeGenModel.class);
        assertTrue(c2.moveToFirst());
        assertEquals(1, c2.getCount());
        assertEquals(13, cursor2.getModel().int_prim);

        CodeGenModel model3 = new CodeGenModel();
        model3.byte_prim = 11;
        model3.short_prim = 12;
        model3.int_prim = 99;
        model3.long_prim = 10l;
        model3.float_prim = 14.1f;
        model3.double_prim = 15.1d;
        model3.char_prim = 'A';
        model3.short_object = 77;
        model3.save(mContext);

        assertEquals('A', model3.char_prim);
        assertEquals(10l, model3.long_prim);

        Cursor c3 = getMockContentResolver().query(DatabaseHelper.getContentUri(CodeGenModel.class),null,null,null,null);
        ObjectCursor<CodeGenModel> cursor3 = new ObjectCursor<>(c3, CodeGenModel.class);
        assertTrue(c3.moveToFirst());
        assertEquals(1, c3.getCount());
        assertEquals(99, cursor3.getModel().int_prim);
    }


    public byte[] setbyteArray(byte[] aArray, int aLength) {
        for (byte i = 0; i < aLength; i++) {
            aArray[i] = i;
        }
        return aArray;
    }

    public Byte[] setByteArray(Byte[] aArray, int aLength) {
        for (Byte i = 0; i < aLength; i++) {
            aArray[i] = i;
        }
        return aArray;
    }

    public char[] setCharArray(char[] aArray, int aLength) {
        for (char i = 0; i < aLength; i++) {
            aArray[i] = i;
        }
        return aArray;
    }

    public Character[] setCharacterArray(Character[] aArray, int aLength) {
        for (Character i = 0; i < aLength; i++) {
            aArray[i] = i;
        }
        return aArray;
    }

    private void assertArrayEquals(char[] aExpectedArray, char[] aActualArray) {
        if (aExpectedArray.length != aActualArray.length)
            fail("Arrays were not the same Length. expected length " + aExpectedArray.length + " found " + aActualArray.length );
        for (int i = 0; i < aExpectedArray.length; i++) {
            if (aExpectedArray[i] != aActualArray[i]) {
                fail("Array index at " + i + " expected " + aExpectedArray[i] + " and found " + aActualArray[i]);
            }
        }
    }

    private void assertArrayEquals(byte[] aExpectedArray, byte[] aActualArray) {
        if (aExpectedArray.length != aActualArray.length)
            fail("Arrays were not the same Length. expected length " + aExpectedArray.length + " found " + aActualArray.length );
        for (int i = 0; i < aExpectedArray.length; i++) {
            if (aExpectedArray[i] != aActualArray[i]) {
                fail("Array index at " + i + " expected " + aExpectedArray[i] + " and found " + aActualArray[i]);
            }
        }
    }

    public static void assertArrayEquals(Object[] aExpectedArray, Object[] aActualArray) {
        if (aExpectedArray.length != aActualArray.length)
            fail("Arrays were not the same Length. expected length " + aExpectedArray.length + " found " + aActualArray.length );
        for (int i = 0; i < aExpectedArray.length; i++) {
            if (aExpectedArray[i] != aActualArray[i]) {
                fail("Array index at " + i + " expected " + aExpectedArray[i] + " and found " + aActualArray[i]);
            }
        }
    }
}
