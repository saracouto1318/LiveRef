package liverefactoring.utils.JDeodorant.utils;

import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiSuperExpression;

public class InstanceOfSuperMethodInvocation implements ExpressionInstanceChecker {

    public boolean instanceOf(PsiExpression expression) {
        return expression instanceof PsiSuperExpression;
    }

}
