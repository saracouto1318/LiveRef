package com.analysis.candidates;

import com.analysis.metrics.FileMetrics;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaFile;

public class InheritanceToDelegationCandidate {
    public PsiClass _class;
    public PsiClass target;
    public FileMetrics metrics;
    public PsiJavaFile sourceFile;
    public double inherited;
    public double override;

    public InheritanceToDelegationCandidate(PsiClass original, PsiClass target, FileMetrics metrics, PsiJavaFile sourceFile, double inherited, double override) {
        this._class = original;
        this.target = target;
        this.metrics = new FileMetrics(metrics);
        this.sourceFile = sourceFile;
        this.inherited =  inherited;
        this.override = override;
    }

    public String toString() {
        return "\nInheritance to Delegation:\n\nOriginal Class: " + this._class.getName() +
                "\nTarget Class: " + this.target.getName();
    }
}
