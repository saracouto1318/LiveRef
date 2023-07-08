package com.core;

import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiStatement;

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
