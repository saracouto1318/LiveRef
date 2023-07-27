package liverefactoring.utils.Halstead;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaToken;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.SyntaxTraverser;

import java.util.ArrayList;
import java.util.Iterator;

public class LiteralAnalyzer {
	PsiMethod method;
	LiteralAnalyzer(PsiMethod method){this.method = method;}

	public void analyzeLiterals(){
		ArrayList<String> cpTokens = new ArrayList<>();
		SyntaxTraverser<PsiElement> traverser = SyntaxTraverser.psiTraverser(this.method);
		traverser.forEach(element -> {
			if (element instanceof PsiJavaToken) {
				PsiJavaToken psiJavaToken = (PsiJavaToken) element;
				cpTokens.add(psiJavaToken.getText());
			}
		});

		for(Iterator<String> iterator = cpTokens.iterator();iterator.hasNext();){
			String token = iterator.next();
			if(Operators.getInstance().name.contains(token)){
				iterator.remove();
			}
			else if(Operands.getInstance().name.contains(token)){
				iterator.remove();
			}
			else if(token.equals("<EOF>")){
				iterator.remove();
			}
		}
		for(int i = 0; i < cpTokens.size(); i++){
			Operands.getInstance().insert(cpTokens.get(i));
		}
	}
}
