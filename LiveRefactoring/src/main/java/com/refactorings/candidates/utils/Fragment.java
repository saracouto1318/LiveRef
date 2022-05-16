package com.refactorings.candidates.utils;

import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiStatement;
import com.utils.MyRange;

public class Fragment {
    public PsiStatement node;
    public MyRange range;
    public PsiMethod method;

    public Fragment(PsiStatement node, MyRange range, PsiMethod method) {
        this.node = node;
        this.range = range;
        this.method = method;
    }
}
