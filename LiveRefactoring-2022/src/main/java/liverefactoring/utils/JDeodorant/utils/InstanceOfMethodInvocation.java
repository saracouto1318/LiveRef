package com.utils.JDeodorant.utils;

import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiMethodCallExpression;

public class InstanceOfMethodInvocation implements ExpressionInstanceChecker {

    public boolean instanceOf(PsiExpression expression) {
        return expression instanceof PsiMethodCallExpression;
    }
}
