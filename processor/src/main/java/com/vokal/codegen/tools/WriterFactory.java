package com.vokal.codegen.tools;

import java.io.IOException;
import java.util.Set;
import javax.annotation.processing.Filer;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

public class WriterFactory {

    private final Filer  filer;
    private final String suffix;

    public WriterFactory(Filer filer, String suffix) {
        this.filer = filer;
        this.suffix = suffix;
    }

    public ModelWriter writeModelClass(EnclosingClass enclosingClass) throws IOException {
        TypeElement classType = enclosingClass.getElement();
        String fqcn = enclosingClass.getClassPackage() + "." + enclosingClass.getClassName() + suffix;
        JavaFileObject jfo = filer.createSourceFile(fqcn, classType);
        return new ModelWriter(jfo, suffix, enclosingClass.getClassPackage(), enclosingClass.getClassName());
    }

    public RegistrationWriter writeRegistrationClass(Set<EnclosingClass> aEnclosingClass) throws IOException {
        EnclosingClass enclosingClass = aEnclosingClass.iterator().next();
        String fqcn = enclosingClass.getClassPackage() + ".RegisterTables" + suffix;
        JavaFileObject jfo = filer.createSourceFile(fqcn);
        return new RegistrationWriter(jfo, suffix, enclosingClass.getClassPackage(), "RegisterTables");
    }
}
