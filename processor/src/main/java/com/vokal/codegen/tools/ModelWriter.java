package com.vokal.codegen.tools;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.tools.JavaFileObject;

import com.vokal.codegen.Table;

public class ModelWriter extends CodeGenWriter {

    private boolean       mContainsByteArray;
    private boolean       mContainsCharacterArray;

    Set<? extends Element> mTableElements = null;
    Collection<ColumnField> mColumnFields;

    public ModelWriter(JavaFileObject aJfo, String suffix, String aPackageName, String aClassName) {
        super(aJfo, suffix, aPackageName, aClassName);
    }

    public ModelWriter withTableElements(Set<? extends Element> aTableElements) {
        mTableElements = aTableElements;
        return this;
    }

    public ModelWriter withFields(Collection<ColumnField> annotatedColumnFields) {
        mColumnFields = annotatedColumnFields;
        return this;
    }

    protected String firstLetterToUpper(String word) {
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }

    @Override
    protected String brewJava() {
        imports();
        classNameAndFieldObject();
        staticStrings();
        populateContentValueWithObject();
        tableCreator();
        cursorCreator();
        if (mContainsByteArray) createByteArrayConverter();
        if (mContainsCharacterArray) createCharacterArrayConverter();

        mFileFormatter.addLine("}");

        return super.brewJava();
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

    private void staticStrings() {
        for (ColumnField columnField : mColumnFields) {
            mFileFormatter.addLine(
                    "public static final String " + columnField.getName().toUpperCase() + " = \"" + columnField.getName().toLowerCase() + "\";");
        }
    }

    private void populateContentValueWithObject() {
        String className = mClassName.replace('$', '.');
        mFileFormatter.addLine("",
                               "@Override",
                               "public void populateContentValues(ContentValues aValues, Object aObject) {",
                               className + " model = ((" + className + ") aObject);",
                               "if (model.hasId())","aValues.put(_ID, model.getId());");
        for (ColumnField columnField : mColumnFields) {
            addContent(columnField);
        }
        mFileFormatter.addLine("}");
    }

    private void addContent(ColumnField columnField) {
        boolean isNotPrimitive = columnField.getType().contains(".") || columnField.getType().contains("[]");

        String ifStatement = "";
        String contentString = "aValues.put(" + columnField.getName().toUpperCase() + ", ";
        String fieldName = "model."+ columnField.getName();
        if (isNotPrimitive)
            ifStatement = "if ( " + fieldName + " != null) ";

        if ("Date".equals(columnField.getSimpleType())) {
            contentString += fieldName + ".getTime()";
        } else if ("char".equals(columnField.getSimpleType()) ||
                "Character".equals(columnField.getSimpleType())){
            contentString += "(int) " + fieldName;
        } else if ("char[]".equals(columnField.getSimpleType())) {
            contentString += "String.valueOf( " + fieldName + " )";
        } else if ("Byte[]".equals(columnField.getSimpleType())) {
            mContainsByteArray = true;
            contentString += "byteArrayToPrim( " + fieldName + " )";
        } else if ("Character[]".equals(columnField.getSimpleType())) {
            mContainsCharacterArray = true;
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

    private void cursorCreator() {
        String className = mClassName.replace('$', '.');
        mFileFormatter.addLine(
                "public static final CursorCreator<" + className + "> CURSOR_CREATOR = new CursorCreator<" + className + ">() {",
                "public " + className + " createFromCursorGetter(CursorGetter getter) {",
                className + " model = new " + className + "();");

        for (ColumnField columnField : mColumnFields) {
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
        if ("Integer".equals(columnField.getSimpleType()) ||
            "char".equals(columnField.getSimpleType())    ||
            "byte".equals(columnField.getSimpleType()) ) {
            getterType = "Int";
            castType = columnField.getSimpleType();
        } else if ("Character".equals(columnField.getSimpleType())||
                "Byte".equals(columnField.getSimpleType())) {
            getterType = "Int";
            castType = columnField.getSimpleType().substring(0, 4).toLowerCase();
        } else if ("byte[]".equals(columnField.getSimpleType())) {
            getterType = "Blob";
        } else if ("Byte[]".equals(columnField.getSimpleType()) ) {
            getterType = "ByteArray";
        } else if ("Character[]".equals(columnField.getSimpleType()) ) {
            getterType = "CharacterArray";
        } else if ("char[]".equals(columnField.getSimpleType())) {
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

    protected void tableCreator() {
        mFileFormatter.addLine("",
                "@Override",
                "public void onTableCreate(SQLiteTable.Builder aBuilder) {",
                "aBuilder");

        for (ColumnField columnField : mColumnFields) {
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
                if(mClassName.equals(e.getSimpleName().toString())) {
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
        if ("boolean".equals(columnField.getSimpleType())   ||
            "Boolean".equals(columnField.getSimpleType())   ||
            "int".equals(columnField.getSimpleType())       ||
            "long".equals(columnField.getSimpleType())      ||
            "Long".equals(columnField.getSimpleType())      ||
            "char".equals(columnField.getSimpleType())      ||
            "Character".equals(columnField.getSimpleType()) ||
            "byte".equals(columnField.getSimpleType())      ||
            "Byte".equals(columnField.getSimpleType())      ||
            "short".equals(columnField.getSimpleType())     ||
            "Short".equals(columnField.getSimpleType())     ||
            "Date".equals(columnField.getSimpleType())) {
            addColumn = ".addIntegerColumn(" + columnField.getName().toUpperCase() + ")";
        } else if ("double".equals(columnField.getSimpleType()) ||
                   "Double".equals(columnField.getSimpleType()) ||
                   "float".equals(columnField.getSimpleType())  ||
                   "Float".equals(columnField.getSimpleType())) {
            addColumn = ".addRealColumn(" + columnField.getName().toUpperCase() + ")";
        } else if ("byte[]".equals(columnField.getSimpleType())  ||
                   "Byte[]".equals(columnField.getSimpleType())) {
            addColumn = ".addBlobColumn(" + columnField.getName().toUpperCase() + ")";
        } else if ("char[]".equals(columnField.getSimpleType()) ||
                   "Character[]".equals(columnField.getSimpleType())) {
            addColumn = ".addStringColumn(" + columnField.getName().toUpperCase() + ")";
        } else {
            addColumn = ".add" + firstLetterToUpper(
                    columnField.getSimpleType()) + "Column(" + columnField.getName().toUpperCase() + ")";
        }

        String columnConstraints = "";
        if (columnField.mIsNotNull)
            columnConstraints +=  ".notNull()" ;
        if (columnField.mIsUnique)
            columnConstraints +=  ".unique()" ;
        if (!"".equals(columnField.mDefaultValue)) {
            if ("String".equals(columnField.getSimpleType()))
                columnConstraints += ".defaultValue(\"" + columnField.mDefaultValue + "\")";
            else if (!"byte[]".equals(columnField.getSimpleType()) ||
                     !"Byte[]".equals(columnField.getSimpleType()) ||
                     !"char[]".equals(columnField.getSimpleType()) ||
                     !"Character[]".equals(columnField.getSimpleType()))
                columnConstraints += ".defaultValue(" + columnField.mDefaultValue + ")";
        }
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
