package liverefactoring.utils.Halstead;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaToken;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.SyntaxTraverser;

public class SymbolAnalyzer {
	String[] symbols={"+","++","-","--","*",".",";","/","%","!",">","<",">=","<=","==","=",":","~"};
	PsiMethod method;
	int count = 0;
	SymbolAnalyzer(PsiMethod method){this.method = method;}
	public int countSymbols(int index){
		count=0;
		SyntaxTraverser<PsiElement> traverser = SyntaxTraverser.psiTraverser(this.method);
		traverser.forEach(element -> {
			if (element instanceof PsiJavaToken) {
				PsiJavaToken psiJavaToken = (PsiJavaToken) element;
				if(psiJavaToken.getText().equals(this.symbols[index])){
					++count;
				}
			}
		});
		return count;
	}
	public void analyzeSymbols(){
		for(int i=0;i<this.symbols.length;i++){
			int count=this.countSymbols(i);
			if(count>0)
				Operators.getInstance().insert(this.symbols[i], count);
		}
	}
}
