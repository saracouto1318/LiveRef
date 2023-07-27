package liverefactoring.utils.JDeodorant.utils;

import com.intellij.psi.PsiIfStatement;
import com.intellij.psi.PsiStatement;

public class InstanceOfIfStatement implements StatementInstanceChecker {

    public boolean instanceOf(PsiStatement statement) {
        return statement instanceof PsiIfStatement;
    }

}
