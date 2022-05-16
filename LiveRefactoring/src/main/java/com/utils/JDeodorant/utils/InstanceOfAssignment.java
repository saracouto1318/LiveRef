package com.utils.JDeodorant.utils;

import com.intellij.psi.PsiAssignmentExpression;
import com.intellij.psi.PsiExpression;

public class InstanceOfAssignment implements ExpressionInstanceChecker {

    public boolean instanceOf(PsiExpression expression) {
        return expression instanceof PsiAssignmentExpression;
    }

}
