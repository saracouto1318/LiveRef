package com.utils.Halstead;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaToken;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.SyntaxTraverser;

public class KeywordAnalyzer {
	PsiMethod method;
	KeywordAnalyzer(PsiMethod method){this.method = method;}
	public void analyzeKeywords() {

		SyntaxTraverser<PsiElement> traverser = SyntaxTraverser.psiTraverser(this.method);
		traverser.forEach(element -> {
			if (element instanceof PsiJavaToken) {
				PsiJavaToken psiJavaToken = (PsiJavaToken) element;
				if (Java8Keywords.isKeyword(psiJavaToken.toString())) {
					Operators.getInstance().insert(psiJavaToken.getText());
				} else if (psiJavaToken.getText().charAt(0) == '"') {
					Operands.getInstance().insert(psiJavaToken.getText());
				}
			}
		});
	}
}
