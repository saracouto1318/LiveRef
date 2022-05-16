package com.refactorings.candidates;

import com.intellij.lang.jvm.types.JvmReferenceType;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.metrics.FileMetrics;
import com.metrics.MethodMetrics;
import com.utils.MetricsUtils;
import com.utils.MyRange;
import com.utils.RefactorUtils;
import com.utils.Utilities;

import java.util.*;

public class ExtractMethodCandidate {
    public int methodComplexity = 1;
    public int methodCognitiveComplexity = 1;
    public int numberOfStatementsToExtract;
    public int oldNumberStatements;
    public int numberOfStatements = 0;
    public double lcom = 0;

    public int highestMethodComplexity = 0;
    public int highestNumberOfStatements = 0;
    public double highestLCOM = 0;
    public  int highestMethodCognitiveComplexity = 0;

    public double score = 0.0;

    public MyRange range;
    public PsiMethod method;
    public ArrayList<PsiStatement> nodes;
    public FileMetrics metrics;
    public PsiJavaFile sourceFile;

    public RefactorUtils refactorUtils = new RefactorUtils();
    public MetricsUtils metricsUtils = new MetricsUtils();
    public Utilities utilities = new Utilities();

    public ExtractMethodCandidate(MyRange range, ArrayList<PsiStatement> nodes, PsiMethod method, FileMetrics metrics, PsiJavaFile sourceFile) {
        this.range = range;
        this.nodes = nodes;
        this.method = method;
        this.metrics = new FileMetrics(metrics);
        this.sourceFile = sourceFile;
        this.numberOfStatementsToExtract = 0;
        for (PsiStatement node : nodes) {
            numberOfStatementsToExtract += PsiTreeUtil.findChildrenOfType(node, PsiStatement.class).size();
        }
        this.oldNumberStatements = refactorUtils.getAllStatements(this.method).size();
        //this.calculateScore();
        this.predictMetrics();
    }

    private void calculateScore(){
        List<PsiStatement> newStatements = refactorUtils.getAllStatements(this.method);
        Set<Integer> indexes = new HashSet<>();
        for (PsiStatement newStatement : newStatements) {
            for (PsiStatement node : this.nodes) {
                if(newStatement.getText().equals(node.getText())) {
                    indexes.add(newStatements.indexOf(newStatement));
                }
            }
        }

        for (Integer index : indexes) {
            newStatements.remove(index);
        }

        Set<String>[] dependenciesCandidates = getDependencies(this.nodes);
        Set<String>[] dependenciesNewMethod = getDependencies(new ArrayList(newStatements));

        Set<String> setA1 = new HashSet<>(dependenciesCandidates[0]);
        setA1.retainAll(dependenciesNewMethod[0]);

        Set<String> setA2 = new HashSet<>(dependenciesCandidates[1]);
        setA2.retainAll(dependenciesNewMethod[1]);

        Set<String> setA3 = new HashSet<>(dependenciesCandidates[2]);
        setA3.retainAll(dependenciesNewMethod[2]);

        Set<String> setB1 = Utilities.exclusiveDependencies(dependenciesCandidates[0], dependenciesNewMethod[0]);
        Set<String> setB2 = Utilities.exclusiveDependencies(dependenciesCandidates[1], dependenciesNewMethod[1]);
        Set<String> setB3 = Utilities.exclusiveDependencies(dependenciesCandidates[2], dependenciesNewMethod[2]);

        Set<String> setC1 = Utilities.exclusiveDependencies(dependenciesNewMethod[0], dependenciesCandidates[0]);
        Set<String> setC2 = Utilities.exclusiveDependencies(dependenciesNewMethod[1], dependenciesCandidates[1]);
        Set<String> setC3 = Utilities.exclusiveDependencies(dependenciesNewMethod[2], dependenciesCandidates[2]);

        int a1 = setA1.size(), b1 = setB1.size(), c1 = setC1.size();
        int a2 = setA2.size(), b2 = setB2.size(), c2 = setC2.size();
        int a3 = setA3.size(), b3 = setB3.size(), c3 = setC3.size();

        double distanceVar = (a1 == 0 && (b1 == 0 || c1 == 0)) ? 0 :  (double)1 - ((1/2) * ((a1/(a1 + b1)) + (a1/(a1 + c1))));
        double distanceTypes = (a2 == 0 && (b2 == 0 || c2 == 0)) ? 0 : (double)1 - ((1/2) * ((a2/(a2 + b2)) + (a2/(a2 + c2))));
        double distancePacks = (a3 == 0 && (b3 == 0 || c3 == 0)) ? 0 : (double)1 - ((1/2) * ((a3/(a3 + b3)) + (a3/(a3 + c3))));

        this.score = (1/3) * distanceVar + (1/3) * distanceTypes + (1/3) * distancePacks;
    }

    private Set<String>[] getDependencies(ArrayList<PsiStatement> nodes){
        Set<String> vars = new HashSet<>();
        Set<String> types = new HashSet<>();
        Set<String> packs = new HashSet<>();

        for (PsiStatement node : nodes) {
            for (PsiDeclarationStatement psiDeclarationStatement : PsiTreeUtil.findChildrenOfType(node, PsiDeclarationStatement.class)) {
                vars.add("Declares - " + psiDeclarationStatement.getDeclaredElements()[0].getText());
                types.add("Declares - " + psiDeclarationStatement.getDeclaredElements()[0].getClass().getName());
                packs.add("Package - " + psiDeclarationStatement.getDeclaredElements()[0].getClass().getPackage().getName());
            }

            for (PsiAssignmentExpression psiAssignmentExpression : PsiTreeUtil.findChildrenOfType(node, PsiAssignmentExpression.class)) {
                vars.add("Assigns - " + psiAssignmentExpression.getLExpression());
            }

            for (PsiMethodCallExpression psiMethodCallExpression : PsiTreeUtil.findChildrenOfType(node, PsiMethodCallExpression.class)) {
                PsiMethod method = psiMethodCallExpression.resolveMethod();
                if (method != null && !method.getContainingFile().getName().contains(".class")) {
                    if (method.getReturnType() != null)
                        if (!method.getReturnType().equals(PsiType.VOID)) {
                            types.add("Uses - " + method.getReturnType().toString());
                            packs.add("Package - " + method.getClass().getPackage().getName());
                        }
                }
            }

            for (PsiCatchSection psiCatchSection : PsiTreeUtil.findChildrenOfType(node, PsiCatchSection.class)) {
                types.add("Catches - " + Objects.requireNonNull(psiCatchSection.getCatchType()));
                packs.add("Package - " + psiCatchSection.getCatchType().getClass().getPackage().getName());
            }

            for (PsiElement element : PsiTreeUtil.findChildrenOfType(node, PsiElement.class)) {
                for (PsiParameter parameter : this.method.getParameterList().getParameters()) {
                    if(element.getText().trim().equals(parameter.getName()))
                        vars.add("Reads - " + parameter.getName());
                }
            }

            if(this.method.getThrowsTypes().length > 0){
                for (JvmReferenceType throwsType : this.method.getThrowsTypes()) {
                    types.add("Throws - " + throwsType.getName());
                    packs.add("Package - " + throwsType.getClass().getPackage().getName());
                }
            }
        }

        Set<String>[] dependencies = new Set[3];
        dependencies[0] = vars;
        dependencies[1] = types;
        dependencies[2] = packs;

        return dependencies;
    }

    private void predictMetrics() {
        PsiClass parentClass = this.method.getContainingClass();
        if (parentClass != null) {
            this.lcom = metricsUtils.initializeLCOM(parentClass);
            this.createNewMethodMetrics();
            //this.updateMethodMetrics();
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
                "\nRange: " + this.range.start + " -> " + this.range.end + "\n";
    }
}
