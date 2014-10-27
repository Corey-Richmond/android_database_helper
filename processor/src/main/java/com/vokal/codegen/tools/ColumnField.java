package com.vokal.codegen.tools;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import com.vokal.codegen.Column;

public class ColumnField {

    private final String      mName;
    private final TypeMirror  mType;
    private final TypeElement mEnclosingClassType;
    public        boolean     mIsPrimaryKey;
    public        boolean     mIsNotNull;
    public        boolean     mIsUnique;

    ColumnField(Element aElement) {
        this(aElement.getSimpleName().toString(),
             aElement.asType(),
             (TypeElement) aElement.getEnclosingElement(),
             aElement.getAnnotation(Column.class).constraint());
    }

    ColumnField(String aName,
                TypeMirror aType,
                TypeElement aEnclosingClassType,
                Column.Constraint[] aConstraints) {
        mName = aName;
        mType = aType;
        mEnclosingClassType = aEnclosingClassType;
        for (Column.Constraint constraint : aConstraints) {
            switch (constraint) {
                case primaryKey:
                    mIsPrimaryKey = true;
                    break;
                case notNull:
                    mIsNotNull = true;
                    break;
                case unique:
                    mIsUnique = true;
                    break;
            }
        }
    }

    public String getName() {
        return mName;
    }

    public String getSimpleType() {
        return mType.toString().substring(mType.toString().lastIndexOf('.') + 1);
    }

    public TypeElement getEnclosingClassType() {
        return mEnclosingClassType;
    }

    @Override
    public boolean equals(Object obj) {
        return ((ColumnField) obj).getName().equals(mName);
    }
}
