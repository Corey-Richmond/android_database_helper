package com.vokal.codegen.processor;

import java.io.IOException;
import java.util.*;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

import com.vokal.codegen.tools.*;

@SupportedAnnotationTypes({"com.vokal.codegen.Column", "com.vokal.codegen.Table"})
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class ModelProcessor extends AbstractProcessor {

    public static final String SUFFIX            = "Helper";
    public static final String COLUMN_ANNOTATION = "com.vokal.codegen.Column";
    public static final String TABLE_ANNOTATION  = "com.vokal.codegen.Table";

    private Set<? extends Element> mTableElements;


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement annotation : annotations) {
            if (annotation.getQualifiedName().toString().equals(TABLE_ANNOTATION)) {
                System.out.println("******************* PRE-COMPILER START ********************");
                mTableElements = roundEnv.getElementsAnnotatedWith(annotation);
            }
        }

        for (TypeElement annotation : annotations) {
            if (annotation.getQualifiedName().toString().equals(COLUMN_ANNOTATION)) {
                write(classesWithFieldsAnnotatedWith(roundEnv.getElementsAnnotatedWith(annotation)), mTableElements);
                System.out.println("******************* PRE-COMPILER END ********************");
            }
        }

        return true;
    }

    private void write(Map<EnclosingClass, Collection<ColumnField>> columnFieldsByEnclosingClass,
                       Set<? extends Element> mTableElements) {
        WriterFactory writerFactory = new WriterFactory(filer(), SUFFIX);
        for (EnclosingClass enclosingClass : columnFieldsByEnclosingClass.keySet()) {
            try {
                writerFactory.writeClass(enclosingClass)
                        .withTableElements(mTableElements)
                        .withFields(columnFieldsByEnclosingClass.get(enclosingClass));
            } catch (IOException e) {
                messager().printMessage(Diagnostic.Kind.ERROR,
                                        "Error generating helper for class " + enclosingClass.getClassName()
                                                + ". Reason: " + e.getMessage());
            }
        }
    }


    private Map<EnclosingClass, Collection<ColumnField>> classesWithFieldsAnnotatedWith(
            Set<? extends Element> annotatedElements) {
        return new AnnotationsConverter(messager(), elementUtils())
                .convert(annotatedElements);
    }


    private Messager messager() {
        return processingEnv.getMessager();
    }

    private Elements elementUtils() {
        return processingEnv.getElementUtils();
    }

    private Filer filer() {
        return processingEnv.getFiler();
    }
}
