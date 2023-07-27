package liverefactoring.core;

import com.intellij.psi.*;
import liverefactoring.utils.RefactorUtils;

import java.util.ArrayList;

public class MyMethod {
    public PsiMethod method;
    public PsiType returnType;
    public PsiParameter[] parameters;
    public PsiCodeBlock body;
    public ArrayList<PsiStatement> statements;
    public boolean isGetter = false;
    public boolean isSetter = false;
    public boolean isConstructor;

    public MyMethod(PsiMethod method){
        this.method = method;
        this.returnType = method.getReturnType();
        this.parameters = method.getParameterList().getParameters();
        this.body = method.getBody();

        RefactorUtils utils = new RefactorUtils();
        this.statements = utils.getAllStatements(this.method);
        this.isConstructor = this.method.isConstructor();
    }

    public void setGetter(){
        this.isGetter = true;
    }

    public void setSetter(){
        this.isSetter = true;
    }
}
