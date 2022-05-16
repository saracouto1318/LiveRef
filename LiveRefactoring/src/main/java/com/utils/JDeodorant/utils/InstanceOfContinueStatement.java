package com.utils.JDeodorant.utils;

import com.intellij.psi.PsiContinueStatement;
import com.intellij.psi.PsiStatement;

public class InstanceOfContinueStatement implements StatementInstanceChecker {

    public boolean instanceOf(PsiStatement statement) {
        return statement instanceof PsiContinueStatement;
    }

}
