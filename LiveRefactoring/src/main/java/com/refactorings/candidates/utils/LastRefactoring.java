package com.refactorings.candidates.utils;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;

public class LastRefactoring {
    public String type;
    public PsiClass _class = null;
    public PsiClass newClass = null;
    public PsiMethod method = null;
    public PsiMethod newMethod = null;
    public PsiElement[] nodes;
    public double severity = 0;
    public double index = 0;

    public LastRefactoring(PsiMethod method, String type, PsiElement[] nodes, double severity, int index) {
        this.type = type;
        this.method = method;
        this.nodes = nodes;
        this.newMethod = null;
        this.newClass = null;
        this.severity = severity;
        this.index = index;
    }

    public LastRefactoring(PsiClass _class, PsiElement[] nodes, double severity, int index) {
        this.type = "Extract Class";
        this._class = _class;
        this.nodes = nodes;
        this.newClass = null;
        this.severity = severity;
        this.index = index;
    }

    public void setClass(PsiClass newClass){
        this.newClass = newClass;
    }

    public void setMethod(PsiMethod newMethod){
        this.newMethod = newMethod;
    }
}
