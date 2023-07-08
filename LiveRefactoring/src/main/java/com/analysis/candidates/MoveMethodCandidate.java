package com.analysis.candidates;

import com.core.MyRange;
import com.intellij.psi.*;
import com.utils.RefactorUtils;

import java.util.ArrayList;
import java.util.Collections;

public class MoveMethodCandidate {
    public PsiMethod method;
    public PsiClass originalClass;
    public PsiClass targetClass;
    public double distance;
    public MyRange range;
    public ArrayList<PsiStatement> nodes = new ArrayList<>();
    public PsiJavaFile file;
    public RefactorUtils utils = new RefactorUtils();
    public PsiVariable[] variables = null;
    public double highestCCOld = 0;
    public double highestCogOld = 0;
    public double highestLCOMOld = 0;
    public double highestEffortOld = 0;
    public double highestMaintainabilityOld = 0;
    public double highestCCNew = 0;
    public double highestCogNew = 0;
    public double highestLCOMNew = 0;
    public double highestEffortNew = 0;
    public double highestMaintainabilityNew = 0;

    public MoveMethodCandidate(MyRange range, PsiMethod method, PsiClass originalClass, PsiClass targetClass, double distance, PsiJavaFile file, PsiVariable[] variables){
        this.method = method;
        this.originalClass = originalClass;
        this.targetClass = targetClass;
        this.distance = distance;
        this.range = range;
        this.file = file;
        this.variables = variables;
        Collections.addAll(this.nodes, this.method.getBody().getStatements());

        //this.measureNewMetrics();
    }

    public String toString(){
        return "Move Method\n" + this.method.getName() + " from " +
                this.originalClass.getName() + " to " + this.targetClass.getName() +
                " with distance " + this.distance;
    }
}
