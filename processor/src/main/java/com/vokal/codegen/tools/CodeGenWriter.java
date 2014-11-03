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
        mFileFormatter.addLine("",
                               "@Override",
                               "public void populateContentValues(ContentValues aValues, Object aObject) {",
                               mClassName.replace('$', '.') + " model = ((" + mClassName.replace('$', '.') + ") aObject);",
                               "if (model.hasId()) {","aValues.put(_ID, model.getId());","}");
        for (ColumnField columnField : columnFields) {
            addContent(columnField);
        }
        mFileFormatter.addLine("}");
    }

    private void addContent(ColumnField columnField) {
        if (columnField.getSimpleType().equals("Date"))
            mFileFormatter.addLine(
                    "if ( model."+ columnField.getName() + " != null) " +
                    "aValues.put(" + columnField.getName().toUpperCase() +
                            ", model." + columnField.getName() + ".getTime());");
        else
            mFileFormatter.addLine(
                    "aValues.put(" + columnField.getName().toUpperCase() + ", model." + columnField.getName() + ");");
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
        if (columnField.getSimpleType().equals("Integer")) {
            getterType = "Int";
        } else {
            getterType = firstLetterToUpper(columnField.getSimpleType());
        }
        mFileFormatter.addLine(
                "model." + columnField.getName() + " = getter.get" + getterType +
                        "(" + columnField.getName().toUpperCase() + ");");
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
        if (columnField.getSimpleType().equals("boolean") ||
            columnField.getSimpleType().equals("Boolean") ||
            columnField.getSimpleType().equals("int")     ||
            columnField.getSimpleType().equals("long")    ||
            columnField.getSimpleType().equals("Long")    ||
            columnField.getSimpleType().equals("Date")) {
            addColumn = ".add" + "IntegerColumn(" + columnField.getName().toUpperCase() + ")";
        } else if (columnField.getSimpleType().equals("double") ||
                   columnField.getSimpleType().equals("Double") ||
                   columnField.getSimpleType().equals("float")  ||
                   columnField.getSimpleType().equals("Float")) {
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
}
