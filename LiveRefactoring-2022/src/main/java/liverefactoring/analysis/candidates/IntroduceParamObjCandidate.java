package com.analysis.candidates;

import com.core.MyRange;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IntroduceParamObjCandidate {
    public PsiMethod method;
    public List<PsiParameter> originalParameters;
    public Editor editor;
    public PsiJavaFile file;
    public MyRange range;

    public IntroduceParamObjCandidate(PsiMethod method, Editor editor, PsiJavaFile file){
        this.editor = editor;
        this.method = method;
        this.file = file;
        this.originalParameters = new ArrayList<>();
        LogicalPosition start = editor.offsetToLogicalPosition(this.method.getParameterList().getTextRange().getStartOffset());
        LogicalPosition end = editor.offsetToLogicalPosition(this.method.getParameterList().getTextRange().getEndOffset());

        this.range = new MyRange(start, end);

        Collections.addAll(this.originalParameters, this.method.getParameterList().getParameters());
    }

    public String toString() {
        return "\nIntroduce Parameter Object:\n\nMethod: " + this.method.getName() +
                "\nNumber Parameters: " + this.method.getParameterList().getParametersCount();
    }
}
