package com.vokal.db.test;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.test.ProviderTestCase2;

import java.util.Date;

import com.vokal.db.DatabaseHelper;
import com.vokal.db.SimpleContentProvider;
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
        DatabaseHelper.registerModel(mContext, CodeGenModel.class);
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
        boolean success = model.delete(mContext);
        assertTrue(success);
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
