package liverefactoring.utils.JDeodorant.utils;

import com.intellij.psi.PsiStatement;
import com.intellij.psi.PsiTryStatement;

public class InstanceOfTryStatement implements StatementInstanceChecker {

    public boolean instanceOf(PsiStatement statement) {
        return statement instanceof PsiTryStatement;
    }

}
