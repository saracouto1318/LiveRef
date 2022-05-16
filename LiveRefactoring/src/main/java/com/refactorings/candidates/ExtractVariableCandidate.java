package com.refactorings.candidates;

import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiMethod;
import com.utils.MyRange;

public class ExtractVariableCandidate {
    public MyRange range;
    public PsiExpression node;
    public int length;
    public PsiMethod originalMethod;
    public Editor editor;

    public ExtractVariableCandidate(MyRange range, PsiExpression node, int length, PsiMethod method, Editor editor) {
        this.range = range;
        this.node = node;
        this.length = length;
        this.originalMethod = method;
        this.editor = editor;
    }

    public String toString() {
        return "Extract Variable:\n\nRange: " + this.range.start + " -> " + this.range.end + "\n";
    }
}
