package liverefactoring.utils.JDeodorant.utils;

import com.intellij.psi.PsiReturnStatement;
import com.intellij.psi.PsiStatement;

public class InstanceOfReturnStatement implements StatementInstanceChecker {

    public boolean instanceOf(PsiStatement statement) {
        return statement instanceof PsiReturnStatement;
    }

}
