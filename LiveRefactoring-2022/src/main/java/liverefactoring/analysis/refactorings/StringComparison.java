package com.analysis.refactorings;

import com.analysis.candidates.StringComparisonCandidate;
import com.core.LastRefactoring;
import com.core.MyRange;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.utils.RefactorUtils;
import com.utils.importantValues.Values;

import java.util.ArrayList;
import java.util.List;

public class StringComparison {
    public RefactorUtils refactorUtils = new RefactorUtils();
    public Editor editor;
    public PsiJavaFile sourceFile;

    public StringComparison(PsiJavaFile sourceFile, Editor editor) {
        this.editor = editor;
        this.sourceFile = sourceFile;
    }

    public StringComparison(Editor editor) {
        this.editor = editor;
        Values.stringComp = new ArrayList<>();
    }

    public void stringComparison(StringComparisonCandidate candidate, double severity, int index) {
        Values.isRefactoring = true;
        PsiBinaryExpression binaryExpression = candidate.node;
        PsiExpression lExpr = binaryExpression.getLOperand();
        PsiExpression rExpr = binaryExpression.getROperand();
        
        PsiElementFactory factory = JavaPsiFacade.getInstance(this.editor.getProject()).getElementFactory();
        String newText = lExpr.getText() + ".equals(" + rExpr.getText() + ")";
        PsiMethodCallExpression equalsCall =
                (PsiMethodCallExpression) factory.createExpressionFromText(newText, null);
        
        equalsCall.getMethodExpression().getQualifierExpression().replace(lExpr);
        equalsCall.getArgumentList().getExpressions()[0].replace(rExpr);
        binaryExpression.replace(equalsCall);
        
        PsiElement[] elements = new PsiElement[]{binaryExpression};

        Values.lastRefactoring = new LastRefactoring(candidate.originalMethod, "String Comparison", elements, Values.currentFile, severity, index);
        Values.allSC.add(candidate);
        System.out.println("============ String Comparison Done!!! ============");
    }

    public void run()  {
        ArrayList<StringComparisonCandidate> firstCandidates = new ArrayList<>();

        for (PsiMethod psiMethod : PsiTreeUtil.findChildrenOfType(sourceFile, PsiMethod.class)) {
            for (PsiBinaryExpression psiBinaryExpression : PsiTreeUtil.findChildrenOfType(psiMethod, PsiBinaryExpression.class)) {
                PsiBinaryExpression binaryExpression = psiBinaryExpression;
                IElementType opSign = binaryExpression.getOperationTokenType();
                PsiExpression lExpr = binaryExpression.getLOperand();
                PsiExpression rExpr = binaryExpression.getROperand();

                if(opSign.toString().trim().equals("EQEQ")){
                    if(lExpr.getType() != null && rExpr.getType() != null) {
                        if (lExpr.getType().getCanonicalText() != null && rExpr.getType().getCanonicalText() != null) {
                            if (lExpr.getType().getCanonicalText().equals("java.lang.String") && rExpr.getType().getCanonicalText().equals("java.lang.String")) {
                                LogicalPosition start = editor.offsetToLogicalPosition(psiBinaryExpression.getTextRange().getStartOffset());
                                LogicalPosition end = editor.offsetToLogicalPosition(psiBinaryExpression.getTextRange().getEndOffset());
                                firstCandidates.add(new StringComparisonCandidate(new MyRange(start, end), psiBinaryExpression, psiMethod, this.editor));
                            }
                        }
                    }
                }
            }
        }

        List<StringComparisonCandidate> secondCandidates = new ArrayList<>();
        for (StringComparisonCandidate elem : firstCandidates) {
            boolean found = false;
            for (StringComparisonCandidate candidate : secondCandidates) {
                if(candidate.toString().equals(elem.toString())) {
                    found = true;
                    break;
                }
            }
            if(!found) {
                secondCandidates.add(elem);
            }
        }

        secondCandidates.sort((a, b) -> b.node.getText().length() - a.node.getText().length());
        Values.stringComp = new ArrayList<>();
        Values.stringComp.addAll(secondCandidates);
        System.out.println("String Comparison Candidates: " + Values.stringComp.size());
    }
}
