package com.utils.Halstead;

import com.intellij.psi.PsiMethod;

public class Initiator {

	public MetricsEvaluator initiate(PsiMethod method){
		Operators.getInstance().count.clear();
		Operators.getInstance().name.clear();
		Operands.getInstance().name.clear();
		Operands.getInstance().count.clear();
		KeywordAnalyzer ka = new KeywordAnalyzer(method);
		ka.analyzeKeywords();
		SymbolAnalyzer sa = new SymbolAnalyzer(method);
		sa.analyzeSymbols();
		BracesAnalyzer ba = new BracesAnalyzer(method);
		ba.analyzeBraces();
		LiteralAnalyzer la = new LiteralAnalyzer(method);
		la.analyzeLiterals();
		MetricsEvaluator me = new MetricsEvaluator();
		me.evaluate();
		return me;
	}

}
