package liverefactoring.utils.JDeodorant.utils;

import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiPostfixExpression;

public class InstanceOfPostfixExpression implements ExpressionInstanceChecker {

    public boolean instanceOf(PsiExpression expression) {
        return expression instanceof PsiPostfixExpression;
    }

}
