package com.vokal.codegen.tools;

import java.io.IOException;
import java.util.Collection;
import javax.tools.JavaFileObject;

public class CodeGenWriter {

    private final JavaFileObject mJavaFileObject;
    private final String         mSuffix;
    private final EnclosingClass mEnclosingClass;

    private FileFormatter mFileFormatter;
    private String        mFieldObject;
    private String        mPackageName;
    private String        mClassName;
    private String        mHelperClassName;

    public CodeGenWriter(JavaFileObject jfo, String suffix, EnclosingClass enclosingClass) {
        this.mJavaFileObject = jfo;
        this.mSuffix = suffix;
        this.mEnclosingClass = enclosingClass;
    }

    public void withFields(Collection<AnnotatedField> annotatedColumnFields,
                           Collection<AnnotatedField> annotatedUniqueFields) throws IOException {
        java.io.Writer writer = mJavaFileObject.openWriter();
        writer.write(brewJava(annotatedColumnFields, annotatedUniqueFields));
        writer.flush();
        writer.close();
    }

    public String getFieldString(String fieldName) {
        return mFieldObject + "." + fieldName;
    }

    protected String firstLetterToUpper(String word) {
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }

    private String brewJava(Collection<AnnotatedField> annotatedColumnFields,
                            Collection<AnnotatedField> annotatedUniqueFields) {
        mPackageName = mEnclosingClass.getClassPackage();
        mClassName = mEnclosingClass.getClassName();
        mHelperClassName = mClassName + mSuffix;

        mFieldObject = "m" + mClassName;

        mFileFormatter = new FileFormatter();

        imports();
        classNameAndFieldObject();
        staticStrings(annotatedColumnFields);
        populateContentValue(annotatedColumnFields);
        tableCreator(annotatedColumnFields, annotatedUniqueFields);
        cursorCreator(annotatedColumnFields);
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
                "" + mClassName + " " + mFieldObject + ";",
                "");
    }

    private void staticStrings(Collection<AnnotatedField> annotatedFields) {
        for (AnnotatedField annotatedField : annotatedFields) {
            mFileFormatter.addLine(
                    "private static final String " + annotatedField.getName().toUpperCase() + " = \"" + annotatedField.getName().toLowerCase() + "\";");
        }
    }

    private void populateContentValue(Collection<AnnotatedField> annotatedFields) {
        mFileFormatter.addLine(
                "@Override",
                "public void populateContentValues(ContentValues aValues) {",
                "if (" + mFieldObject + ".hasId())",
                "aValues.put(_ID, " + mFieldObject +".getId());");
        for (AnnotatedField annotatedField : annotatedFields) {
            addContent(annotatedField);
        }
        mFileFormatter.addLine("}");
    }

    private void addContent(AnnotatedField annotatedField) {
        if (annotatedField.getSimpleType().equals("Date"))
            mFileFormatter.addLine(
                    "if ("+ getFieldString(annotatedField.getName()) + " != null) " +
                    "aValues.put(" + annotatedField.getName().toUpperCase() + ", " + getFieldString(
                            annotatedField.getName()) + ".getTime());");
        else
            mFileFormatter.addLine(
                    "aValues.put(" + annotatedField.getName().toUpperCase() + ", " + getFieldString(
                            annotatedField.getName()) + ");");
    }

    private void cursorCreator(Collection<AnnotatedField> annotatedFields) {
        mFileFormatter.addLine(
                "public static final CursorCreator<" + mClassName + "> CURSOR_CREATOR = new CursorCreator<" + mClassName + ">() {",
                "public " + mClassName + " createFromCursorGetter(CursorGetter getter) {",
                mClassName + " model = new " + mClassName + "();");

        for (AnnotatedField annotatedField : annotatedFields) {
            mFileFormatter.addLine("model." + annotatedField.getName() + " = getter.get" + firstLetterToUpper(annotatedField.getSimpleType()) + "(" + annotatedField.getName().toUpperCase() + ");");
        }

        mFileFormatter.addLine(
                "model.setId(getter.getLong(_ID));",
                "return model;",
                "}",
                "};");
    }

    protected void tableCreator(Collection<AnnotatedField> annotatedFields,
                                Collection<AnnotatedField> annotatedUniqueFields) {
        mFileFormatter.addLine(
                "public static final SQLiteTable.TableCreator TABLE_CREATOR = new SQLiteTable.TableCreator() {",
                "@Override",
                "public SQLiteTable buildTableSchema(SQLiteTable.Builder aBuilder) {",
                "aBuilder");

        int size;
        if ((annotatedUniqueFields != null) && (size = annotatedUniqueFields.size()) > 1) {
            for (AnnotatedField annotatedField : annotatedFields) {
                tableBuilder(annotatedField, false);
            }
            mFileFormatter.addLine(".unique(");
            int index = 0;
            for (AnnotatedField annotatedUniqueField : annotatedUniqueFields) {
                mFileFormatter.addLine(annotatedUniqueField.getName().toUpperCase());
                if (index == (size-1)) {
                    mFileFormatter.addLine(")");
                } else {
                    mFileFormatter.addLine(", ");
                }
                ++index;
            }
        } else {
            for (AnnotatedField annotatedField : annotatedFields) {
                tableBuilder(annotatedField, (annotatedUniqueFields != null)
                        && annotatedUniqueFields.contains(annotatedField));
            }
        }
        mFileFormatter.addLine(";",
                               "return aBuilder.build();",
                               "}", "");

        mFileFormatter.addLine("@Override",
                               "public SQLiteTable updateTableSchema(SQLiteTable.Upgrader aUpgrader, int aOldVersion) {",
                               "return aUpgrader.recreate().build();",
                               "}",
                               "};");
    }

    private void tableBuilder(AnnotatedField annotatedField, boolean contains) {
        String addColumn;
        if (annotatedField.getSimpleType().equals("boolean") ||
            annotatedField.getSimpleType().equals("int")     ||
            annotatedField.getSimpleType().equals("long")    ||
            annotatedField.getSimpleType().equals("Date")) {
            addColumn = ".add" + "IntegerColumn(" + annotatedField.getName().toUpperCase() + ")";
        } else if (annotatedField.getSimpleType().equals("double") || annotatedField.getSimpleType().equals("float")) {
            addColumn = ".add" + "RealColumn(" + annotatedField.getName().toUpperCase() + ")";
        } else {
            addColumn = ".add" + firstLetterToUpper(
                    annotatedField.getSimpleType()) + "Column(" + annotatedField.getName().toUpperCase() + ")";
        }

        mFileFormatter.addLine((contains) ? (addColumn + ".unique()") : (addColumn));

    }

    private void setObject() {
        mFileFormatter.addLine(
                "@Override public void setObject(Object a) {",
                mFieldObject + " = ((" + mClassName + ") a);",
                "}");
    }



}
