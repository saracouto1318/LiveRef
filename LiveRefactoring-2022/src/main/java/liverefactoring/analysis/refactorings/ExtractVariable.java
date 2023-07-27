package liverefactoring.analysis.refactorings;

import liverefactoring.analysis.candidates.ExtractVariableCandidate;
import liverefactoring.core.LastRefactoring;
import liverefactoring.core.MyRange;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.introduceVariable.IntroduceVariableHandler;
import liverefactoring.utils.RefactorUtils;
import liverefactoring.utils.importantValues.ThresholdsCandidates;
import liverefactoring.utils.importantValues.Values;

import java.util.ArrayList;
import java.util.List;

public class ExtractVariable{
    public RefactorUtils refactorUtils = new RefactorUtils();
    public Editor editor;
    public PsiJavaFile sourceFile;

    public ExtractVariable(PsiJavaFile sourceFile, Editor editor) {
        this.editor = editor;
        this.sourceFile = sourceFile;
    }

    public ExtractVariable(Editor editor) {
        this.editor = editor;
    }

    public void extractVariable(ExtractVariableCandidate candidate, double severity, int index) {
        Values.isRefactoring = true;
        candidate.editor = Values.editor;
        PsiElement[] elements = new PsiElement[]{candidate.node};

        IntroduceVariableHandler handler = new IntroduceVariableHandler();
        if(candidate.editor != null) {
            handler.invoke(candidate.editor.getProject(), candidate.editor, candidate.node);

            Values.lastRefactoring = new LastRefactoring(candidate.originalMethod, "Extract Variable", elements, Values.currentFile, severity, index);
            Values.allEV.add(candidate);
            System.out.println("============ Extract Variable Done!!! ============");
        }
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

    public void run() {
        List<ExtractVariableCandidate> firstCandidates = new ArrayList<>();
        for (PsiMethod psiMethod : PsiTreeUtil.findChildrenOfType(sourceFile, PsiMethod.class)) {
            getNonVoidCalls(refactorUtils.getCallExpressions(psiMethod)).forEach(nvc -> {
                if (nvc.getText().trim().length() >= ThresholdsCandidates.minLengthExtraction) {
                    LogicalPosition nvcStart = this.editor.offsetToLogicalPosition(nvc.getTextRange().getStartOffset());
                    LogicalPosition nvcEnd = this.editor.offsetToLogicalPosition(nvc.getTextRange().getEndOffset());
                    MyRange range = new MyRange(nvcStart, nvcEnd);
                    firstCandidates.add(new ExtractVariableCandidate(range, nvc, nvc.getText().trim().length(), psiMethod, this.editor));
                }
            });
        }

        List<ExtractVariableCandidate> secondCandidates = new ArrayList<>();
        for (ExtractVariableCandidate elem : firstCandidates) {
            boolean found = false;
            for (ExtractVariableCandidate candidate : secondCandidates) {
                if(candidate.toString().equals(elem.toString())) {
                    found = true;
                    break;
                }
            }
            if(!found) secondCandidates.add(elem);

        }

        secondCandidates.sort((a, b) -> b.length - a.length);
        Values.extractVariable = new ArrayList<>();
        Values.extractVariable.addAll(secondCandidates);
        System.out.println("Extract Variable Candidates: " + Values.extractVariable.size());
    }
}
