package com.utils.JDeodorant.utils;

import com.intellij.psi.PsiArrayAccessExpression;
import com.intellij.psi.PsiExpression;

public class InstanceOfArrayAccess implements ExpressionInstanceChecker {

    public boolean instanceOf(PsiExpression expression) {
        return expression instanceof PsiArrayAccessExpression;
    }

}
