package com.core;

import com.analysis.metrics.FileMetrics;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;

public class LastRefactoring {
    public String type;
    public PsiClass _class = null;
    public PsiClass newClass;
    public PsiMethod method = null;
    public PsiMethod newMethod = null;
    public PsiElement[] nodes;
    public FileMetrics metrics;
    public double severity;
    public double index;

    public LastRefactoring(PsiMethod method, String type, PsiElement[] nodes, FileMetrics metrics, double severity, int index) {
        this.type = type;
        this.method = method;
        this._class = method.getContainingClass();
        this.nodes = nodes;
        this.newMethod = null;
        this.newClass = null;
        this.metrics = metrics;
        this.severity = severity;
        this.index = index;
    }

    public LastRefactoring(PsiClass _class, PsiElement[] nodes, FileMetrics metrics, double severity, int index, String type) {
        this.type = type;
        this._class = _class;
        this.nodes = nodes;
        this.newClass = null;
        this.metrics = metrics;
        this.severity = severity;
        this.index = index;
    }

    public LastRefactoring(PsiMethod method, PsiClass _class, PsiElement[] nodes, FileMetrics metrics, double severity, int index, String type) {
        this.type = type;
        this.method = method;
        this._class = _class;
        this.nodes = nodes;
        this.newClass = null;
        this.metrics = metrics;
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
