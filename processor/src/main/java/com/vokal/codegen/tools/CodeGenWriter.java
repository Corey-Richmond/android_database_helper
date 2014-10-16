package com.vokal.codegen.tools;

import java.io.IOException;
import java.util.Collection;
import javax.tools.JavaFileObject;

import com.google.common.base.Joiner;

public class CodeGenWriter {

    private final JavaFileObject mJavaFileObject;
    private final String         mSuffix;
    private final EnclosingClass mEnclosingClass;

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

    private String brewJava(Collection<AnnotatedField> annotatedColumnFields,
                            Collection<AnnotatedField> annotatedUniqueFields) {
        String classPackage = mEnclosingClass.getClassPackage();
        String helperClassName = mEnclosingClass.getClassName() + mSuffix;

        return Joiner.on("\n").join(
                "// Generated code from CodeGen. Do not modify!",
                "package " + classPackage + ";",
                "import android.content.ContentValues;",
                "import com.vokal.db.SQLiteTable;",
                "import com.vokal.db.util.CursorCreator;",
                "import com.vokal.db.util.CursorGetter;",
                "import com.vokal.db.codegen.ModelHelper;",
                "import android.provider.BaseColumns;",
                "public class " + helperClassName + " implements ModelHelper, BaseColumns {",
                "\t" + mEnclosingClass.getClassName() + " m" + mEnclosingClass.getClassName() + ";",
                "",
                emitStaticStrings(annotatedColumnFields),
                emitPopulateContentValue(annotatedColumnFields),
                "",
                emitTableCreator(annotatedColumnFields, annotatedUniqueFields),
                "",
                emitCursorCreator(annotatedColumnFields),
                "",
                emitSetObject(),
                "",
                "}"
        );
    }

    private String emitCursorCreator(Collection<AnnotatedField> annotatedFields) {
        StringBuilder builder = new StringBuilder();
        builder.append(
                "\tpublic static final CursorCreator<" + mEnclosingClass.getClassName() + "> CURSOR_CREATOR = new CursorCreator<" + mEnclosingClass.getClassName() + ">() {\n" +
                        "\t\tpublic " + mEnclosingClass.getClassName() + " createFromCursorGetter(CursorGetter getter) {\n" +
                        "\t\t\t" + mEnclosingClass.getClassName() + " model = new " + mEnclosingClass.getClassName() + "();\n");
        for (AnnotatedField annotatedField : annotatedFields) {
            builder.append("\t\t\tmodel." + annotatedField.getName() + " = getter.get" + firstLetterToUpper(annotatedField.getSimpleType()) + "(" + annotatedField.getName().toUpperCase() + ");\n");
        }
        builder.append("\t\t\tmodel.setId(getter.getLong(_ID));\n");
        builder.append("\t\t\treturn model;\n\t\t}\n\t};");

        return builder.toString();
    }

    protected String emitTableCreator(Collection<AnnotatedField> annotatedFields,
                                      Collection<AnnotatedField> annotatedUniqueFields) {
        StringBuilder builder = new StringBuilder();
        builder.append("\tpublic static final SQLiteTable.TableCreator TABLE_CREATOR = new SQLiteTable.TableCreator() {\n" +
                       "\t\t@Override\n" +
                       "\t\tpublic SQLiteTable buildTableSchema(SQLiteTable.Builder aBuilder) {\n\t\t\taBuilder\n");

        int size;
        if ((annotatedUniqueFields != null) && (size = annotatedUniqueFields.size()) > 1) {
            for (AnnotatedField annotatedField : annotatedFields) {
                builder.append(emitTableBuilder(annotatedField, false));
            }
            builder.append("\t\t\t.unique(");
            int index = 0;
            for (AnnotatedField annotatedUniqueField : annotatedUniqueFields) {
                builder.append( annotatedUniqueField.getName().toUpperCase());
                if (index == (size-1)) {
                    builder.append( ")" );
                } else {
                    builder.append( ", " );
                }
                ++index;
            }
        } else {
            for (AnnotatedField annotatedField : annotatedFields) {
                builder.append(emitTableBuilder(annotatedField, (annotatedUniqueFields != null)
                        && annotatedUniqueFields.contains(annotatedField)));
            }
        }
        builder.append("\t\t\t;\n\t\t\treturn aBuilder.build();\n\t\t}\n\n");
        builder.append("\t\t@Override\n" +
                       "\t\tpublic SQLiteTable updateTableSchema(SQLiteTable.Upgrader aUpgrader, int aOldVersion) {\n" +
                       "\t\t\treturn aUpgrader.recreate().build();\n" +
                       "\t\t}\n\t};");

        return builder.toString();
    }

    private String emitTableBuilder(AnnotatedField annotatedField, boolean contains) {
        String addColumn = "";
        if (annotatedField.getSimpleType().equals("boolean") ||
            annotatedField.getSimpleType().equals("int")     ||
            annotatedField.getSimpleType().equals("long")    ||
            annotatedField.getSimpleType().equals("Date")) {
            addColumn = "\t\t\t.add" + "IntegerColumn(" + annotatedField.getName().toUpperCase() + ")";
        } else if (annotatedField.getSimpleType().equals("double") || annotatedField.getSimpleType().equals("float")) {
            addColumn = "\t\t\t.add" + "RealColumn(" + annotatedField.getName().toUpperCase() + ")";
        } else {
            addColumn = "\t\t\t.add" + firstLetterToUpper(
                    annotatedField.getSimpleType()) + "Column(" + annotatedField.getName().toUpperCase() + ")";
        }

        return (contains) ? (addColumn + ".unique()\n") : (addColumn + "\n");

    }

    private String  emitStaticStrings(Collection<AnnotatedField> annotatedFields) {
        StringBuilder builder = new StringBuilder();
        for (AnnotatedField annotatedField : annotatedFields) {
            builder.append("\tprivate static final String " + annotatedField.getName().toUpperCase() + " = \"" + annotatedField.getName().toLowerCase() + "\";\n");
        }
        return builder.toString();
    }

    private String emitSetObject() {
        StringBuilder builder = new StringBuilder();
        builder.append("\t@Override public void setObject(Object a) {\n" +
                       "\t\tm" + mEnclosingClass.getClassName() + " = ((" + mEnclosingClass.getClassName() + ") a);\n");
        builder.append("\t}");
        return builder.toString();
    }

    private String emitPopulateContentValue(Collection<AnnotatedField> annotatedFields) {
        StringBuilder builder = new StringBuilder();
        builder.append("\t@Override\n\tpublic void populateContentValues(ContentValues aValues) {\n\t\tif (m"  + mEnclosingClass.getClassName() + ".hasId()) aValues.put(_ID, m"+ mEnclosingClass.getClassName() +".getId());\n");
        for (AnnotatedField annotatedField : annotatedFields) {
            builder.append(emitSetters(annotatedField));
        }
        builder.append("\t}");
        return builder.toString();
    }

    private String emitSetters(AnnotatedField annotatedField) {
        if (annotatedField.getSimpleType().equals("Date"))
            return "\t\tif (m" + mEnclosingClass.getClassName() + "." + annotatedField.getName() + " != null) " +
                   "aValues.put(" + annotatedField.getName().toUpperCase()+ ", m" + mEnclosingClass.getClassName() + "." + annotatedField.getName() + ".getTime());\n";
        else
            return "\t\taValues.put("+annotatedField.getName().toUpperCase()+ "," + " m" + mEnclosingClass.getClassName() + "." + annotatedField.getName() + ");\n";
    }

    protected String firstLetterToUpper(String word) {
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }

}
