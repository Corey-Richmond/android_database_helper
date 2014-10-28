package com.vokal.codegen.tools;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.tools.JavaFileObject;

import com.vokal.codegen.Table;

public class CodeGenWriter {

    private final JavaFileObject mJavaFileObject;
    private final String         mSuffix;
    private final EnclosingClass mEnclosingClass;

    private FileFormatter mFileFormatter;
    private String        mFieldObject;
    private String        mPackageName;
    private String        mClassName;
    private String        mHelperClassName;

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



    public String getFieldString(String fieldName) {
        return mFieldObject + "." + fieldName;
    }

    protected String firstLetterToUpper(String word) {
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }

    private String brewJava(Collection<ColumnField> aColumnFields) {
        mPackageName = mEnclosingClass.getClassPackage();
        mClassName = mEnclosingClass.getClassName();
        mHelperClassName = mClassName + mSuffix;

        mFieldObject = "m" + mClassName;

        mFileFormatter = new FileFormatter();

        imports();
        classNameAndFieldObject();
        staticStrings(aColumnFields);
        populateContentValue(aColumnFields);
        tableCreator(aColumnFields);
        cursorCreator(aColumnFields);
        setObject();

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
                "public class " + mHelperClassName + " implements ModelHelper, BaseColumns {",
                "",
                "" + mClassName + " " + mFieldObject + ";",
                "");
    }

    private void staticStrings(Collection<ColumnField> columnFields) {
        for (ColumnField columnField : columnFields) {
            mFileFormatter.addLine(
                    "private static final String " + columnField.getName().toUpperCase() + " = \"" + columnField.getName().toLowerCase() + "\";");
        }
    }

    private void populateContentValue(Collection<ColumnField> columnFields) {
        mFileFormatter.addLine("",
                "@Override",
                "public void populateContentValues(ContentValues aValues) {",
                "if (" + mFieldObject + ".hasId())",
                "aValues.put(_ID, " + mFieldObject +".getId());");
        for (ColumnField columnField : columnFields) {
            addContent(columnField);
        }
        mFileFormatter.addLine("}");
    }

    private void addContent(ColumnField columnField) {
        if (columnField.getSimpleType().equals("Date"))
            mFileFormatter.addLine(
                    "if ("+ getFieldString(columnField.getName()) + " != null) " +
                    "aValues.put(" + columnField.getName().toUpperCase() + ", " + getFieldString(
                            columnField.getName()) + ".getTime());");
        else
            mFileFormatter.addLine(
                    "aValues.put(" + columnField.getName().toUpperCase() + ", " + getFieldString(
                            columnField.getName()) + ");");
    }

    private void cursorCreator(Collection<ColumnField> columnFields) {
        mFileFormatter.addLine(
                "public static final CursorCreator<" + mClassName + "> CURSOR_CREATOR = new CursorCreator<" + mClassName + ">() {",
                "public " + mClassName + " createFromCursorGetter(CursorGetter getter) {",
                mClassName + " model = new " + mClassName + "();");

        for (ColumnField columnField : columnFields) {
            mFileFormatter.addLine("model." + columnField.getName() + " = getter.get" + firstLetterToUpper(
                    columnField.getSimpleType()) + "(" + columnField.getName().toUpperCase() + ");");
        }

        mFileFormatter.addLine(
                "model.setId(getter.getLong(_ID));",
                "return model;",
                "}",
                "};");
    }

    protected void tableCreator(Collection<ColumnField> columnFields) {
        mFileFormatter.addLine("",
                "@Override",
                "public void onTableCreate(SQLiteTable.Builder aBuilder) {",
                "aBuilder");

        for (ColumnField columnField : columnFields) {
            tableBuilder(columnField);
        }

        if(mTableElements != null) {
            for (Element e : mTableElements) {
                int length;
                if ((length = e.getAnnotation(Table.class).primaryKeys().length) > 0) {
                    String[] keys = e.getAnnotation(Table.class).primaryKeys();
                    String primaryKey = ".primaryKey(";
                    for (int i = 0; i <= length; i++) {
                        if (i == length) {
                            primaryKey += ")";
                            continue;
                        } else if (i > 0 ) {
                            primaryKey += ",";
                        }
                        primaryKey += keys[i].toUpperCase();
                    }
                    mFileFormatter.addLine(primaryKey);
                }


                if ((length = e.getAnnotation(Table.class).uniqueColumns().length) > 0) {
                    String[] uniques = e.getAnnotation(Table.class).uniqueColumns();
                    String uniqueColumns = ".unique(";
                    for (int i = 0; i <= length; i++) {
                        if (i == length) {
                            uniqueColumns += ")";
                            continue;
                        } else if (i > 0 ) {
                            uniqueColumns += ",";
                        }
                        uniqueColumns += uniques[i].toUpperCase();
                    }
                    mFileFormatter.addLine(uniqueColumns);
                }
            }
        }

        mFileFormatter.addLine(
                ";",
                "}", "");

        mFileFormatter.addLine(
                "@Override",
                "public void onTableUpgrade(SQLiteTable.Upgrader aUpgrader, int aOldVersion) {", "}","");
    }

    private void tableBuilder(ColumnField columnField) {
        String addColumn;
        if (columnField.getSimpleType().equals("boolean") ||
            columnField.getSimpleType().equals("int")     ||
            columnField.getSimpleType().equals("long")    ||
            columnField.getSimpleType().equals("Long")    ||
            columnField.getSimpleType().equals("Date")) {
            addColumn = ".add" + "IntegerColumn(" + columnField.getName().toUpperCase() + ")";
        } else if (columnField.getSimpleType().equals("double") || columnField.getSimpleType().equals("float")) {
            addColumn = ".add" + "RealColumn(" + columnField.getName().toUpperCase() + ")";
        } else {
            addColumn = ".add" + firstLetterToUpper(
                    columnField.getSimpleType()) + "Column(" + columnField.getName().toUpperCase() + ")";
        }

        String columnConstraints = "";
        if (columnField.mIsPrimaryKey)
            columnConstraints +=  ".primaryKey()" ;
        if (columnField.mIsNotNull)
            columnConstraints +=  ".notNull()" ;
        if (columnField.mIsUnique)
            columnConstraints +=  ".unique()" ;
        mFileFormatter.addLine(addColumn + columnConstraints);

    }

    private void setObject() {
        mFileFormatter.addLine("",
                "@Override public void setObject(Object a) {",
                mFieldObject + " = ((" + mClassName + ") a);",
                "}");
    }



}
