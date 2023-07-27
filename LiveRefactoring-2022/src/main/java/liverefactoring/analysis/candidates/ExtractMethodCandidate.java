package liverefactoring.analysis.candidates;

import liverefactoring.analysis.metrics.FileMetrics;
import liverefactoring.analysis.metrics.MethodMetrics;
import liverefactoring.core.MyRange;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiStatement;
import com.intellij.psi.util.PsiTreeUtil;
import liverefactoring.utils.MetricsUtils;
import liverefactoring.utils.RefactorUtils;
import liverefactoring.utils.UtilitiesOverall;

import java.util.ArrayList;
import java.util.Collections;

public class ExtractMethodCandidate {
    public int numberOfStatementsToExtract;
    public int oldNumberStatements;
    public int highestLOC = 0;
    public int highestCC = 0;
    public int highestCog = 0;
    public double highestLCOM = 0;
    public double highestEffort = 0;

    public int methodComplexity = 1;
    public int methodCognitiveComplexity = 1;
    public int numberOfStatements = 0;
    public double lcom = 0;
    public int highestMethodComplexity = 0;
    public int highestNumberOfStatements = 0;
    public  int highestMethodCognitiveComplexity = 0;

    public MyRange range;
    public PsiMethod method;
    public ArrayList<PsiStatement> nodes;
    public FileMetrics metrics;
    public PsiJavaFile sourceFile;

    public RefactorUtils refactorUtils = new RefactorUtils();
    public UtilitiesOverall utilitiesOverall = new UtilitiesOverall();
    public MetricsUtils metricsUtils = new MetricsUtils();

    public ExtractMethodCandidate(MyRange range, ArrayList<PsiStatement> nodes, PsiMethod method, FileMetrics metrics, PsiJavaFile sourceFile) {
        this.range = range;
        this.nodes = nodes;
        this.method = method;
        this.metrics = new FileMetrics(metrics);
        this.sourceFile = sourceFile;
        this.numberOfStatementsToExtract = nodes.size();
        for (PsiStatement node : nodes) {
            numberOfStatementsToExtract += PsiTreeUtil.findChildrenOfType(node, PsiStatement.class).size();
        }
        this.oldNumberStatements = refactorUtils.getAllStatements(this.method).size();
        this.predictMetrics();
    }

    private void predictMetrics() {
        /*PsiClass parentClass = this.method.getContainingClass();
        PsiMethod newMethod = JavaPsiFacade.getElementFactory(sourceFile.getProject()).createMethod("name", PsiType.VOID);
        for (PsiStatement node : nodes) {
            newMethod.add(node);
        }
        try {
            MethodMetrics newMetrics = new MethodMetrics(parentClass, newMethod, this.method.isConstructor());
            this.highestLOC = newMetrics.numberLinesOfCode;
            this.highestCC = newMetrics.complexityOfMethod;
            this.highestCog = newMetrics.cognitiveComplexity;
            this.highestLCOM = newMetrics.lackOfCohesionInMethod;
            this.highestEffort = newMetrics.halsteadEffort;
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        PsiClass parentClass = this.method.getContainingClass();
        if (parentClass != null) {
            this.lcom = metricsUtils.initializeLCOM(parentClass);
            this.createNewMethodMetrics();
            this.updateMethodMetrics();
        }
    }

    private void createNewMethodMetrics() {
        int methodComplexity = 1;
        int methodCognitive = 1;

        this.numberOfStatements = this.nodes.size();
        for (PsiStatement node : this.nodes) {
            methodComplexity += metricsUtils.increasesCyclomaticComplexity(node);
            methodCognitive += metricsUtils.increasesCognitiveComplexity(node);
        }

        this.methodComplexity = methodComplexity;
        this.methodCognitiveComplexity = methodCognitive;
    }

    private void updateMethodMetrics() {
        ArrayList<MethodMetrics> metricsNew = new ArrayList<>();
        for (MethodMetrics methodMetric : this.metrics.methodMetrics) {
            metricsNew.add(new MethodMetrics(methodMetric));
        }
        for (MethodMetrics mm : metricsNew) {
            if (mm.methodName.equals(this.method.getName())) {
                this.oldNumberStatements = mm.numberOfStatements;
                mm.complexityOfMethod -= this.methodComplexity;
                mm.numberOfStatements -= this.numberOfStatements;
                mm.cognitiveComplexity -= this.methodCognitiveComplexity;
            }
            mm.lackOfCohesionInMethod = this.lcom;
        }

        ArrayList<Integer> methodComplexityArray = new ArrayList<>();
        ArrayList<Integer> methodCogComplexityArray = new ArrayList<>();
        ArrayList<Integer> numberOfStatementsArray = new ArrayList<>();
        ArrayList<Double> lcomArray = new ArrayList<>();
        metricsNew.forEach(m -> {
            methodComplexityArray.add(m.complexityOfMethod);
            methodCogComplexityArray.add(m.cognitiveComplexity);
            numberOfStatementsArray.add(m.numberOfStatements);
            lcomArray.add(m.lackOfCohesionInMethod);
        });

        methodComplexityArray.add(this.methodComplexity);
        methodCogComplexityArray.add(this.methodCognitiveComplexity);
        numberOfStatementsArray.add(this.numberOfStatements);
        lcomArray.add(this.lcom);

        this.highestMethodComplexity = Collections.max(methodComplexityArray);
        this.highestMethodCognitiveComplexity = Collections.max(methodCogComplexityArray);
        this.highestNumberOfStatements = Collections.max(numberOfStatementsArray);
        this.highestLCOM = Collections.max(lcomArray);
    }

    public String toString() {
        return "\nExtract Method:\n\nMethod: " + this.method.getName() +
                "\nRange: " + this.range.start + " -> " + this.range.end;
    }

    public String nodesText(){
        String text = "";
        for (PsiStatement node : this.nodes) {
            text += node.getText() + "\n";
        }

        return text;
    }
}
