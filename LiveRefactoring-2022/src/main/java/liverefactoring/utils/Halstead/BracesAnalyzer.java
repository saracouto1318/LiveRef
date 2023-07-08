package com.utils.Halstead;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaToken;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.SyntaxTraverser;

public class BracesAnalyzer {
	PsiMethod method;
	String[][] braces={{"{","}"},{"(",")"},{"[","]"},{"<",">"}};
	int counter=0,startCount=0,endCount=0;
	BracesAnalyzer(PsiMethod method){this.method = method;}
	public int countBraces(int index){
		counter=0;
		startCount=0;
		endCount=0;

		SyntaxTraverser<PsiElement> traverser = SyntaxTraverser.psiTraverser(this.method);
		traverser.forEach(element -> {
			if (element instanceof PsiJavaToken) {
				PsiJavaToken psiJavaToken = (PsiJavaToken) element;
				if(psiJavaToken.getText().equals(this.braces[index][0])){
					++startCount;
				}
				else if(psiJavaToken.getText().equals(this.braces[index][1])){
					++endCount;
				}
			}
		});

		if(startCount==endCount){
			counter=startCount;
		}
		return counter;
	}
	public void analyzeBraces(){
		for(int i=0;i<this.braces.length;i++){
			int count=this.countBraces(i);
			if(count>0)
				Operators.getInstance().insert(this.braces[i][0]+" "+this.braces[i][1],count);
		}
	}
}
