package com.refactorings.candidates;

import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;

import java.util.ArrayList;
import java.util.List;

public class IntroduceParamObjCandidate {
    public PsiMethod method;
    public List<PsiParameter> originalParameters;
    public Editor editor;
    public PsiJavaFile file;

    public IntroduceParamObjCandidate(PsiMethod method, Editor editor, PsiJavaFile file){
        this.editor = editor;
        this.method = method;
        this.file = file;
        this.originalParameters = new ArrayList<>();

        for (PsiParameter parameter : this.method.getParameterList().getParameters()) {
            this.originalParameters.add(parameter);
        }
    }
}
