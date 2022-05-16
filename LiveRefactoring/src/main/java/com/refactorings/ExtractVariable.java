package com.refactorings;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.introduceVariable.IntroduceVariableHandler;
import com.metrics.FileMetrics;
import com.metrics.MethodMetrics;
import com.refactorings.candidates.ExtractVariableCandidate;
import com.refactorings.candidates.utils.LastRefactoring;
import com.utils.MyRange;
import com.utils.RefactorUtils;
import com.utils.ThresholdsCandidates;
import com.utils.Values;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ExtractVariable implements Runnable{
    public RefactorUtils refactorUtils = new RefactorUtils();
    public Editor editor;
    public PsiJavaFile sourceFile;
    public List<ExtractVariableCandidate> candidates;
    public int version = 0;

    public ExtractVariable(PsiJavaFile sourceFile, Editor editor, int version) {
        this.editor = editor;
        this.sourceFile = sourceFile;
        this.candidates = new ArrayList<>();
        this.version = version;
    }

    public ExtractVariable(Editor editor) {
        this.editor = editor;
        this.candidates = new ArrayList<>();
    }

    public void extractVariable(ExtractVariableCandidate candidate, double severity, int index) {
        PsiElement[] elements = new PsiElement[1];
        elements[1] = candidate.node;

        IntroduceVariableHandler handler = new IntroduceVariableHandler();
        handler.invoke(Objects.requireNonNull(candidate.editor.getProject()), candidate.editor, candidate.node);

        Values.lastRefactoring = new LastRefactoring(candidate.originalMethod, "Extract Variable", elements, severity, index);
        System.out.println("============ Extract Variable Done!!! ============");
        Values.isRefactoring = true;
    }

    public ArrayList<PsiMethodCallExpression> getNonVoidCalls(ArrayList<PsiMethodCallExpression> expressions) {
        ArrayList<PsiMethodCallExpression> nonVoidCalls = new ArrayList<>();
        for (PsiMethodCallExpression expression : expressions) {
            PsiMethod method = expression.resolveMethod();
            if (method != null && !method.getContainingFile().getName().contains(".class")) {
                if (method.getReturnType() != null)
                    if (!method.getReturnType().equals(PsiType.VOID)) {
                        nonVoidCalls.add(expression);
                    }
            }
        }

        return nonVoidCalls;
    }

    @Override
    public void run() {
        FileMetrics metrics = null;
        if(Values.before != null) {
            metrics = Values.after;
            Values.before = metrics;
        }
        else{
            boolean exists = false;
            for(int i=0; i < Values.openedFiles.size(); i++){
                if(Values.openedFiles.get(i).fileName.equals(sourceFile.getName())){
                    metrics = Values.openedFiles.get(i);
                    break;
                }
            }
            if(!exists) {
                metrics = new FileMetrics(editor, sourceFile);
                Values.openedFiles.add(metrics);
            }
            Values.before = metrics;
        }
        Values.currentFile = metrics;

        final int minimumLength = ThresholdsCandidates.minLengthExtraction;
        List<ExtractVariableCandidate> aux = new ArrayList<>();

        for (PsiMethod psiMethod : PsiTreeUtil.findChildrenOfType(sourceFile, PsiMethod.class)) {
            LogicalPosition cursor = editor.getCaretModel().getLogicalPosition();
            LogicalPosition startMethod = editor.offsetToLogicalPosition(psiMethod.getTextRange().getStartOffset());
            LogicalPosition endMethod = editor.offsetToLogicalPosition(psiMethod.getTextRange().getEndOffset());
            boolean doIt = false;
            if(this.version > 4){
                if(startMethod.line <= cursor.line && cursor.line <= endMethod.line) {
                    doIt = true;
                }
            }
            if(doIt || version < 4){
                getNonVoidCalls(refactorUtils.getCallExpressions(psiMethod)).forEach(nvc -> {
                    if (nvc.getText().trim().length() >= minimumLength) {
                        LogicalPosition nvcStart = this.editor.offsetToLogicalPosition(nvc.getTextRange().getStartOffset());
                        LogicalPosition nvcEnd = this.editor.offsetToLogicalPosition(nvc.getTextRange().getEndOffset());
                        MyRange range = new MyRange(nvcStart, nvcEnd);
                        aux.add(new ExtractVariableCandidate(range, nvc, nvc.getText().trim().length(), psiMethod, this.editor));
                    }
                });
            }
        }


        aux.sort((a, b) -> b.length - a.length);

        candidates = new ArrayList<>(aux);

        System.out.println("Extract Variable: " + this.candidates.size());
    }
}
