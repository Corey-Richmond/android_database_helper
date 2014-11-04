package com.vokal.codegen.tools;

import java.io.IOException;
import java.util.*;
import javax.lang.model.element.Element;
import javax.tools.JavaFileObject;

import com.vokal.codegen.Table;

public class CodeGenWriter {

    private final JavaFileObject mJavaFileObject;
    private final String         mSuffix;
    private final EnclosingClass mEnclosingClass;

    private FileFormatter mFileFormatter;
    private String        mPackageName;
    private String        mClassName;
    private String        mHelperClassName;
    private boolean       mContainsByteArray;
    private boolean       mContainsCharaterArray;

    Set<? extends Element> mTableElements = null;

    public CodeGenWriter(JavaFileObject jfo, String suffix, EnclosingClass enclosingClass) {
        this.mJavaFileObject = jfo;
        this.mSuffix = suffix;
        this.mEnclosingClass = enclosingClass;
    }

    public CodeGenWriter withTableElements(Set<? extends Element> aTableElements) {
        mTableElements = aTableElements;
        return this;
    }

    public void withFields(Collection<ColumnField> annotatedColumnFields) throws IOException {
        java.io.Writer writer = mJavaFileObject.openWriter();
        writer.write(brewJava(annotatedColumnFields));
        writer.flush();
        writer.close();
    }

    protected String firstLetterToUpper(String word) {
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }

    private String brewJava(Collection<ColumnField> aColumnFields) {
        mPackageName = mEnclosingClass.getClassPackage();
        mClassName = mEnclosingClass.getClassName();
        mHelperClassName = mClassName + mSuffix;

        mFileFormatter = new FileFormatter();

        imports();
        classNameAndFieldObject();
        staticStrings(aColumnFields);
        populateContentValueWithObject(aColumnFields);
        tableCreator(aColumnFields);
        cursorCreator(aColumnFields);
        if (mContainsByteArray) createByteArrayConverter();
        if (mContainsCharaterArray) createCharacterArrayConverter();


        mFileFormatter.addLine("}");

        return mFileFormatter.format();
    }
    private void imports() {
        mFileFormatter.addLine(
                "// Generated code from CodeGen. Do not modify!",
                "package " + mPackageName + ";",
                "import android.content.ContentValues;",
                "import com.vokal.db.SQLiteTable;",
                "import com.vokal.db.util.CursorCreator;",
                "import com.vokal.db.util.CursorGetter;",
                "import com.vokal.db.codegen.ModelHelper;",
                "import android.provider.BaseColumns;");
    }


    private void classNameAndFieldObject() {
        mFileFormatter.addLine(
                "public class " + mHelperClassName + " implements ModelHelper, BaseColumns {","");
    }

    private void staticStrings(Collection<ColumnField> columnFields) {
        for (ColumnField columnField : columnFields) {
            mFileFormatter.addLine(
                    "public static final String " + columnField.getName().toUpperCase() + " = \"" + columnField.getName().toLowerCase() + "\";");
        }
    }

    private void populateContentValueWithObject(Collection<ColumnField> columnFields) {
        String className = mClassName.replace('$', '.');
        mFileFormatter.addLine("",
                               "@Override",
                               "public void populateContentValues(ContentValues aValues, Object aObject) {",
                               className + " model = ((" + className + ") aObject);",
                               "if (model.hasId()) {","aValues.put(_ID, model.getId());","}");
        for (ColumnField columnField : columnFields) {
            addContent(columnField);
        }
        mFileFormatter.addLine("}");
    }

    private void addContent(ColumnField columnField) {
        boolean isNotPrimitive = columnField.getType().contains(".");

        String ifStatement = "";
        String contentString = "aValues.put(" + columnField.getName().toUpperCase() + ", ";
        String fieldName = "model."+ columnField.getName();
        if (isNotPrimitive)
            ifStatement = "if ( " + fieldName + " != null) ";

        if (columnField.getSimpleType().equals("Date")) {
            contentString += fieldName + ".getTime()";
        } else if (columnField.getSimpleType().equals("char") ||
                   columnField.getSimpleType().equals("Character")){
            contentString += "(int) " + fieldName;
        } else if (columnField.getSimpleType().equals("char[]")) {
            contentString += "String.valueOf( " + fieldName + " )";
        } else if (columnField.getSimpleType().equals("Byte[]")) {
            mContainsByteArray = true;
            contentString += "byteArrayToPrim( " + fieldName + " )";
        } else if (columnField.getSimpleType().equals("Character[]")) {
            mContainsCharaterArray = true;
            contentString += "characterArrayToString( " + fieldName + " )";
        } else {
            contentString += fieldName;
        }

        contentString += ");";
        if ("".equals(ifStatement))
            mFileFormatter.addLine(contentString);
        else
        mFileFormatter.addLine(ifStatement, contentString);
    }

    private void cursorCreator(Collection<ColumnField> columnFields) {
        String className = mClassName.replace('$', '.');
        mFileFormatter.addLine(
                "public static final CursorCreator<" + className + "> CURSOR_CREATOR = new CursorCreator<" + className + ">() {",
                "public " + className + " createFromCursorGetter(CursorGetter getter) {",
                className + " model = new " + className + "();");

        for (ColumnField columnField : columnFields) {
            cursorCreatorGetType(columnField);
        }

        mFileFormatter.addLine(
                "model.setId(getter.getLong(_ID));",
                "return model;",
                "}",
                "};");
    }

    private void cursorCreatorGetType(ColumnField columnField) {
        String getterType = "";
        String castType = "";
        if (columnField.getSimpleType().equals("Integer") ||
            columnField.getSimpleType().equals("char")     ||
            columnField.getSimpleType().equals("byte") ) {
            getterType = "Int";
            castType = columnField.getSimpleType();
        } else if (columnField.getSimpleType().equals("Character")||
                   columnField.getSimpleType().equals("Byte")) {
            getterType = "Int";
            castType = columnField.getSimpleType().substring(0, 4).toLowerCase();
        } else if (columnField.getSimpleType().equals("byte[]")) {
            getterType = "Blob";
        } else if (columnField.getSimpleType().equals("Byte[]") ) {
            getterType = "ByteArray";
        } else if (columnField.getSimpleType().equals("Character[]") ) {
            getterType = "CharacterArray";
        } else if (columnField.getSimpleType().equals("char[]")) {
            getterType = "CharArray";
        } else {
            getterType = firstLetterToUpper(columnField.getSimpleType());

        }

        if ("".equals(castType)) {
            mFileFormatter.addLine(
                    "model." + columnField.getName() + " = getter.get" + getterType +
                            "(" + columnField.getName().toUpperCase() + ");");
        } else {
            mFileFormatter.addLine(
                    "model." + columnField.getName() + " = (" + castType + ") getter.get" + getterType +
                            "(" + columnField.getName().toUpperCase() + ");");
        }
    }

    protected void tableCreator(Collection<ColumnField> columnFields) {
        mFileFormatter.addLine("",
                "@Override",
                "public void onTableCreate(SQLiteTable.Builder aBuilder) {",
                "aBuilder");

        for (ColumnField columnField : columnFields) {
            tableBuilder(columnField);
        }

        addTableConstraints();

        mFileFormatter.addLine(
                ";",
                "}", "");
    }

    private void addTableConstraints() {
        if(mTableElements != null) {
            for (Element e : mTableElements) {
                if(e.getSimpleName().toString().equals(mClassName)) {
                    addTableConstraints("primaryKey", e.getAnnotation(Table.class).primaryKeys());
                    addTableConstraints("unique", e.getAnnotation(Table.class).uniqueColumns());

                    int indexLength = e.getAnnotation(Table.class).indexColumns().length;
                    for (int i = 0; i < indexLength; i++) {
                        addTableConstraints("index", e.getAnnotation(Table.class).indexColumns()[i].value());
                    }
                }
            }
        }
    }

    private void addTableConstraints(String aConstraint, String[] aColumns) {
        int length = aColumns.length;
        if (length > 0) {
            aConstraint = "." + aConstraint + "(";
            for (int i = 0; i <= length; i++) {
                aConstraint += (i > 0 && i < length)
                              ? "," + aColumns[i].toUpperCase()
                              : (i == length) ? ")" : aColumns[i].toUpperCase();
            }
            mFileFormatter.addLine(aConstraint);
        }
    }

    private void tableBuilder(ColumnField columnField) {
        String addColumn;
        if (columnField.getSimpleType().equals("boolean")   ||
            columnField.getSimpleType().equals("Boolean")   ||
            columnField.getSimpleType().equals("int")       ||
            columnField.getSimpleType().equals("long")      ||
            columnField.getSimpleType().equals("Long")      ||
            columnField.getSimpleType().equals("char")      ||
            columnField.getSimpleType().equals("Character") ||
            columnField.getSimpleType().equals("byte")      ||
            columnField.getSimpleType().equals("Byte")      ||
            columnField.getSimpleType().equals("short")     ||
            columnField.getSimpleType().equals("Short")     ||
            columnField.getSimpleType().equals("Date")) {
            addColumn = ".addIntegerColumn(" + columnField.getName().toUpperCase() + ")";
        } else if (columnField.getSimpleType().equals("double") ||
                   columnField.getSimpleType().equals("Double") ||
                   columnField.getSimpleType().equals("float")  ||
                   columnField.getSimpleType().equals("Float")) {
            addColumn = ".addRealColumn(" + columnField.getName().toUpperCase() + ")";
        } else if (columnField.getSimpleType().equals("byte[]")  ||
                   columnField.getSimpleType().equals("Byte[]")) {
            addColumn = ".addBlobColumn(" + columnField.getName().toUpperCase() + ")";
        } else if (columnField.getSimpleType().equals("char[]") ||
                columnField.getSimpleType().equals("Character[]")) {
            addColumn = ".addStringColumn(" + columnField.getName().toUpperCase() + ")";
        } else {
            addColumn = ".add" + firstLetterToUpper(
                    columnField.getSimpleType()) + "Column(" + columnField.getName().toUpperCase() + ")";
        }

        String columnConstraints = "";
        if (columnField.mIsPrimaryKey)
            columnConstraints +=  ".primaryKey()" ;
        if (columnField.mIsAutoIncrement)
            columnConstraints +=  ".autoincrement()" ;
        if (columnField.mIsNotNull)
            columnConstraints +=  ".notNull()" ;
        if (columnField.mIsUnique)
            columnConstraints +=  ".unique()" ;
        mFileFormatter.addLine(addColumn + columnConstraints);

    }

    private void createCharacterArrayConverter() {
        mFileFormatter.addLine("",
                               "public byte[] byteArrayToPrim(Byte[] aByteArray) {",
                               "int length = aByteArray.length;",
                               "byte[] b = new byte[length];",
                               "for (int i = 0; i < length; ++i) {",
                               "b[i] = aByteArray[i];",
                               "}",
                               "return b;",
                               "}");
    }

    private void createByteArrayConverter() {
        mFileFormatter.addLine("",
                               "public String characterArrayToString(Character[] aCharacterArray) {",
                               "int length = aCharacterArray.length;",
                               "char[] c = new char[length];",
                               "for (int i = 0; i < length; ++i) {",
                               "c[i] = aCharacterArray[i];",
                               "}",
                               "return String.valueOf(c);",
                               "}");
    }

}
