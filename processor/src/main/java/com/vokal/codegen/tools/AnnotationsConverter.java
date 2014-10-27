package com.vokal.codegen.tools;

import java.util.*;
import javax.annotation.processing.Messager;
import javax.lang.model.element.*;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Multimaps.index;

public class AnnotationsConverter {

    private final Messager messager;
    private final Elements elementUtils;

    public AnnotationsConverter(Messager messager, Elements elementUtils) {
        this.messager = messager;
        this.elementUtils = elementUtils;
    }

    public Map<EnclosingClass, Collection<ColumnField>> convert(
            Collection<? extends Element> annotatedElements) {

        FluentIterable<ColumnField> annotatedFields =
                from(annotatedElements).filter(new ValidModifier()).transform(new ToAnnotatedField());

        Set<String> erasedEnclosingClasses =
                annotatedFields.transform(new ToErasedEnclosingClass()).toSet();

        return index(annotatedFields, new ByEnclosingClass(erasedEnclosingClasses)).asMap();
    }

    private class ValidModifier implements Predicate<Element> {

        @Override
        public boolean apply(Element element) {
            boolean isInvalid = element.getModifiers().contains(Modifier.PRIVATE) ||
                    element.getModifiers().contains(Modifier.STATIC) ||
                    element.getModifiers().contains(Modifier.FINAL);

            if (isInvalid) {
                logError(element, "Field must not be private, static or final");
            }

            return !isInvalid;
        }
    }

    private void logError(Element element, String error) {
        messager.printMessage(Diagnostic.Kind.ERROR, error, element);
    }

    private class ToAnnotatedField implements Function<Element, ColumnField> {
        @Override
        public ColumnField apply(Element aFieldElement) {
            return new ColumnField(aFieldElement);
        }
    }

    private class ToErasedEnclosingClass implements Function<ColumnField, String> {

        @Override
        public String apply(ColumnField field) {
            TypeElement enclosingClassType = field.getEnclosingClassType();
            if (enclosingClassType.getModifiers().contains(Modifier.PRIVATE)) {
                logError(enclosingClassType, "Enclosing class must not be private");
            }
            return enclosingClassType.toString();
        }
    }

    private class ByEnclosingClass implements Function<ColumnField, EnclosingClass> {

        private final Set<String> annotatedClasses = new HashSet<>();

        private ByEnclosingClass(Set<String> erasedEnclosingClasses) {
            annotatedClasses.addAll(erasedEnclosingClasses);
        }

        @Override
        public EnclosingClass apply(ColumnField field) {
            TypeElement classType = field.getEnclosingClassType();
            String classPackage = getPackageName(classType);
            String targetClassName = getClassName(classType, classPackage);
            String sanitizedClassName = sanitize(targetClassName);
            return new EnclosingClass(classPackage, sanitizedClassName, classType);
        }

        private String getPackageName(TypeElement classType) {
            return elementUtils.getPackageOf(classType).getQualifiedName().toString();
        }

        private String getClassName(TypeElement classType, String classPackage) {
            int packageLength = classPackage.length() + 1;
            return classType.getQualifiedName().toString().substring(packageLength);
        }

        private String sanitize(String targetClass) {
            return targetClass.replace(".", "$");
        }
    }
}
