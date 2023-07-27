package liverefactoring.utils.JDeodorant.utils;

import com.intellij.psi.PsiForStatement;
import com.intellij.psi.PsiStatement;

public class InstanceOfForStatement implements StatementInstanceChecker {

    public boolean instanceOf(PsiStatement statement) {
        return statement instanceof PsiForStatement;
    }

}
