package com.vokal.codegen.tools;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.tools.JavaFileObject;

public class CodeGenWriter {

    protected final JavaFileObject mJavaFileObject;
    protected final String         mSuffix;

    protected FileFormatter mFileFormatter;
    protected String        mPackageName;
    protected String        mClassName;
    protected String        mHelperClassName;

    public CodeGenWriter(JavaFileObject aJfo, String suffix, String aPackageName, String aClassName) {
        mJavaFileObject = aJfo;
        mSuffix = suffix;
        mPackageName = aPackageName;
        mClassName = aClassName;
        mHelperClassName = mClassName + mSuffix;

        mFileFormatter = new FileFormatter();
    }

    public void write() throws IOException {
        java.io.Writer writer = mJavaFileObject.openWriter();
        writer.write(brewJava());
        writer.flush();
        writer.close();
    }

    protected String brewJava() {
        return mFileFormatter.format();
    }

}
