package com.vokal.codegen.tools;

import java.util.Set;
import javax.tools.JavaFileObject;

public class RegistrationWriter extends CodeGenWriter {

    Set<EnclosingClass> mClasses;

    public RegistrationWriter(JavaFileObject aJfo, String suffix, String aPackageName, String aClassName) {
        super(aJfo, suffix, aPackageName, aClassName);
    }

    public RegistrationWriter withClasses(Set<EnclosingClass> aEnclosingClasses) {
        mClasses = aEnclosingClasses;
        return this;
    }

    @Override
    protected String brewJava() {
        mFileFormatter.addLine(
                "// Generated code from CodeGen. Do not modify!",
                "package " + mPackageName + ";",
                "public class " + mHelperClassName + " {", "",
                "public static Class[] getTables() {"
        );

        int length = mClasses.size();
        int i = 0;
        String classes = "return new Class[] {";
        for (EnclosingClass clazz : mClasses) {
            if (i < length - 1)
                classes += clazz.getClassName() + ".class, ";
            else
                classes += clazz.getClassName() + ".class };";
            ++i;
        }

        mFileFormatter.addLine(classes,
                               "}",
                               "}");
        return super.brewJava();
    }
}
