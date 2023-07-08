package com.analysis.candidates;

import com.core.MyRange;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;

public class ExtractVariableCandidate {
    public MyRange range;
    public PsiMethodCallExpression node;
    public int length;
    public PsiMethod originalMethod;
    public Editor editor;

    public ExtractVariableCandidate(MyRange range, PsiMethodCallExpression node, int length, PsiMethod method, Editor editor) {
        this.range = range;
        this.node = node;
        this.length = length;
        this.originalMethod = method;
        this.editor = editor;
    }

    public String toString() {
        return "Extract Variable:\n\nRange: " + this.range.start + " -> " + this.range.end +
                "\nMethod: " + this.originalMethod.getName() + "\nNode: " + this.node.getText();
    }
}
