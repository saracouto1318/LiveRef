package com.utils.importantValues;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiVariable;

import java.util.ArrayList;

public class MoveObject {
    public PsiClass _class;
    public int counterOccurrences;
    public ArrayList<PsiVariable> variables;

    public MoveObject(PsiClass _class, int counterOccurrences, ArrayList<PsiVariable> variables) {
        this._class = _class;
        this.counterOccurrences = counterOccurrences;
        this.variables = variables;
    }
}
