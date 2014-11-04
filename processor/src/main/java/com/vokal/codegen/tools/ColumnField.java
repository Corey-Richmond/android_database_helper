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
    public        boolean     mIsAutoIncrement;
    public        String      mDefaultValue;

    ColumnField(Element aElement) {
        this(aElement.getSimpleName().toString(),
             aElement.asType(),
             (TypeElement) aElement.getEnclosingElement(),
             aElement.getAnnotation(Column.class).constraint(),
             aElement.getAnnotation(Column.class).defaultValue());
    }

    ColumnField(String aName,
                TypeMirror aType,
                TypeElement aEnclosingClassType,
                Column.Constraint[] aConstraints,
                String aDefaultValue) {
        mName = aName;
        mType = aType;
        mEnclosingClassType = aEnclosingClassType;
        mDefaultValue = aDefaultValue;
        for (Column.Constraint constraint : aConstraints) {
            switch (constraint) {
                case primaryKey:
                    mIsPrimaryKey = true;
                    break;
                case autoincrement:
                    mIsAutoIncrement = true;
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

    public String getType() {
        return mType.toString();
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
