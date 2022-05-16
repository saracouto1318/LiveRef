package com.refactorings.candidates.utils;

import com.intellij.psi.PsiField;
import com.intellij.psi.PsiType;

public class MyAttribute {
    public PsiField attribute;
    public PsiType type;
    public String text;

    public MyAttribute(PsiField attribute){
        this.attribute = attribute;
        this.type = this.attribute.getType();
        this.text = this.attribute.getText();
    }
}
