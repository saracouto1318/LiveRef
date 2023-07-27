package liverefactoring.utils.JDeodorant.utils;

import com.intellij.psi.PsiExpression;

interface ExpressionInstanceChecker {
    boolean instanceOf(PsiExpression expression);
}
