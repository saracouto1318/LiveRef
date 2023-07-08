package com.analysis.candidates;

import com.analysis.metrics.MethodMetrics;
import com.core.MyRange;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.*;
import com.utils.importantValues.Values;

public class StringComparisonCandidate {
    public MyRange range;
    public PsiBinaryExpression node;
    public PsiMethod originalMethod;
    public Editor editor;
    public int highestCC = 0;
    public int highestCog = 0;
    public double highestEffort = 0;

    public StringComparisonCandidate(MyRange range, PsiBinaryExpression node, PsiMethod method, Editor editor) {
        this.range = range;
        this.node = node;
        this.originalMethod = method;
        this.editor = editor;

        //this.newMetrics();
    }

    private void newMetrics() {
        PsiBinaryExpression binaryExpression = node;
        PsiExpression lExpr = binaryExpression.getLOperand();
        PsiExpression rExpr = binaryExpression.getROperand();

        PsiElementFactory factory = JavaPsiFacade.getInstance(Values.editor.getProject()).getElementFactory();
        String newText = lExpr.getText() + ".equals(" + rExpr.getText() + ")";
        PsiMethodCallExpression equalsCall =
                (PsiMethodCallExpression) factory.createExpressionFromText(newText, null);
        PsiMethod newMethod = JavaPsiFacade.getElementFactory(Values.editor.getProject()).createMethod("name", PsiType.VOID);
        for (PsiStatement statement : originalMethod.getBody().getStatements()) {
            if(!statement.getText().contains(binaryExpression.getText()))
                newMethod.add(statement);
            else
                newMethod.add(equalsCall);
        }
        try {
            MethodMetrics newMetrics = new MethodMetrics(this.originalMethod.getContainingClass(), newMethod, this.originalMethod.isConstructor());
            highestCC = newMetrics.complexityOfMethod;
            highestCog = newMetrics.cognitiveComplexity;
            highestEffort = newMetrics.halsteadEffort;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String toString() {
        return "String Comparison:\n\nRange: " + this.range.start + " -> " + this.range.end +
                "\nMethod: " + this.originalMethod.getName() + "\nNode: " + this.node.getText();
    }
}
