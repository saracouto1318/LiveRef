package com.refactorings.candidates;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiStatement;
import com.utils.MyRange;
import com.utils.RefactorUtils;

import java.util.ArrayList;

public class MoveMethodCandidate {
    public PsiMethod method = null;
    public PsiClass originalClass = null;
    public PsiClass targetClass = null;
    public double distance = 0;
    public MyRange range;
    public ArrayList<PsiStatement> nodes = new ArrayList<>();
    public PsiJavaFile file;

    public MoveMethodCandidate(MyRange range, PsiMethod method, PsiClass originalClass, PsiClass targetClass, double distance, PsiJavaFile file){
        this.method = method;
        this.originalClass = originalClass;
        this.targetClass = targetClass;
        this.distance = distance;
        this.range = range;
        this.file = file;
        RefactorUtils utils = new RefactorUtils();
        this.nodes = utils.getAllStatements(this.method);
    }
}
