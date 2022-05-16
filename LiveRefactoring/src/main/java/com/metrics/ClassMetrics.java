package com.metrics;

import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.utils.*;

import java.text.DecimalFormat;
import java.util.*;

public class ClassMetrics {
    private static final double SSM_WEIGHT = 0.2;
    private static final double CDM_WEIGHT = 0.1;
    private static final double CSM_WEIGHT = 0.7;
    private static final double THRESHOLD_MULTIPLIER = 0.15;
    public PsiClass targetClass;
    public String className;
    public int numProperties = 0;
    public int numPublicAttributes = 0;
    public int numLongMethods = 0;
    public int numLinesCode = 0;
    public double lackOfCohesion = 0.0;
    public double complexity = 0.0;
    public double cognitiveComplexity = 0.0;
    public double cognitiveComplexityPercentage = 0.0;
    public ArrayList<ArrayList<Double>> ssmMatrix = new ArrayList<>();
    public ArrayList<ArrayList<Double>> cdmMatrix = new ArrayList<>();
    public ArrayList<ArrayList<Double>> csmMatrix = new ArrayList<>();
    public ArrayList<ArrayList<Double>> weightMatrix = new ArrayList<>();
    public ArrayList<ArrayList<Double>> vocabularyOccurrencesMatrix = new ArrayList<>();
    public List<PsiMethod> methods = new ArrayList<>();
    public Set<String> vocabularyDictionary = new HashSet<>();
    public double threshold = 0.0;
    public int numMethods = 0;
    public int numConstructors = 0;
    public ArrayList<MethodMetrics> methodMetrics = new ArrayList<>();
    public RefactorUtils refactorUtils = new RefactorUtils();
    public MetricsUtils metricsUtils = new MetricsUtils();
    public Utilities utilities = new Utilities();
    private MyMatrix decomposedMatrixV;

    public int longParameterList = 0;
    public double halsteadLength = 0.0;
    public double halsteadVocabulary = 0.0;
    public double halsteadVolume = 0.0;
    public double halsteadDifficulty = 0.0;
    public double halsteadEffort = 0.0;
    public double halsteadLevel = 0.0;
    public double halsteadTime = 0.0;
    public double halsteadBugsDelivered = 0.0;
    public double halsteadMaintainability = 0.0;

    public ClassMetrics(PsiClass targetClass) {
        this.targetClass = targetClass;
        this.className = targetClass.getName();

        List<PsiMethod> auxMethods = refactorUtils.getMethods(this.targetClass);
        PsiMethod[] constructors = this.targetClass.getConstructors();

        for (PsiMethod auxMethod : auxMethods) {
            boolean found = false;
            for (PsiMethod constructor : constructors) {
                if (auxMethod.getName().equals(constructor.getName()) &&
                        auxMethod.getText().equals(constructor.getText())) {
                    found = true;
                }
            }
            if (!found)
                this.methods.add(auxMethod);
        }

        this.buildSSMMatrix();
        this.buildCDMMatrix();
        this.buildVocabularyDictionary();
        this.buildVocabularyOccurrencesMatrix();

        if (this.vocabularyOccurrencesMatrix.size() > 0) {
            MyMatrix a = new MyMatrix(this.vocabularyOccurrencesMatrix);
            SingularValueDecomposition svd = new SingularValueDecomposition(a);
            this.decomposedMatrixV = svd.getV();

            this.buildCSMMatrix();
            this.buildFinalWeightMatrix();
            this.calculateThreshold();
        }

        this.getMetricsPerClass();
    }

    private void getMetricsPerClass() {
        String regexComment = "//.*|/\\*((.|\\n)(?!=*/))+\\*/";
        String[] lines = this.targetClass.getText().split("\n");

        for (String line : lines) {
            if (!line.matches(regexComment) && line.trim().length() != 0) {
                this.numLinesCode++;
            }
        }

        for (PsiMethod method : this.methods) {
            MethodMetrics metrics = new MethodMetrics(this.targetClass, method, false);
            this.methodMetrics.add(metrics);
            this.numMethods++;
            if (metrics.isLong)
                this.numLongMethods++;
            if (metrics.longParameterList)
                this.longParameterList++;
        }

        for (PsiMethod method : this.targetClass.getConstructors()) {
            MethodMetrics metrics = new MethodMetrics(this.targetClass, method, true);
            this.methodMetrics.add(metrics);
            this.numConstructors++;
            if (metrics.isLong)
                this.numLongMethods++;
            if(metrics.longParameterList)
                this.longParameterList++;
        }
        int numMethods = this.numMethods + this.numConstructors;

        double lcom = 0.0;
        for (MethodMetrics metrics : this.methodMetrics) {
            lcom += metrics.lackOfCohesionInMethod;
            this.complexity += metrics.complexityOfMethod;
            this.cognitiveComplexity += metrics.cognitiveComplexity;
            this.cognitiveComplexityPercentage += metrics.cognitiveComplexityPercentage;
            this.halsteadLength += metrics.halsteadLength;
            this.halsteadVocabulary += metrics.halsteadVocabulary;
            this.halsteadVolume += metrics.halsteadVolume;
            this.halsteadDifficulty += metrics.halsteadDifficulty;
            this.halsteadEffort += metrics.halsteadEffort;
            this.halsteadLevel += metrics.halsteadLevel;
            this.halsteadTime += metrics.halsteadTime;
            this.halsteadBugsDelivered += metrics.halsteadBugsDelivered;
            this.halsteadMaintainability += metrics.halsteadMaintainability;
        }

        this.complexity = (double)this.complexity / this.methodMetrics.size();
        this.cognitiveComplexity = (double)this.cognitiveComplexity / this.methodMetrics.size();
        this.cognitiveComplexityPercentage = (double)this.cognitiveComplexityPercentage / this.methodMetrics.size();
        this.halsteadLength = (double)this.halsteadLength/this.methodMetrics.size();
        this.halsteadVocabulary = (double)this.halsteadVocabulary/this.methodMetrics.size();
        this.halsteadVolume = (double)this.halsteadVolume/this.methodMetrics.size();
        this.halsteadDifficulty = (double)this.halsteadDifficulty/this.methodMetrics.size();
        this.halsteadEffort = (double)this.halsteadEffort/this.methodMetrics.size();
        this.halsteadLevel = (double)this.halsteadLevel/this.methodMetrics.size();
        this.halsteadTime = (double)this.halsteadTime/this.methodMetrics.size();
        this.halsteadBugsDelivered = (double)this.halsteadBugsDelivered/this.methodMetrics.size();
        this.halsteadMaintainability = (double)this.halsteadMaintainability/this.methodMetrics.size();

        this.lackOfCohesion = numMethods > 0 ? lcom / numMethods : 0;
        PsiField[] fields = this.targetClass.getFields();
        this.numProperties = fields.length;
        for (PsiField fieldDeclaration : fields)
            if (!Objects.requireNonNull(fieldDeclaration.getModifierList()).hasModifierProperty(PsiModifier.PRIVATE) &&
                    !fieldDeclaration.getModifierList().hasModifierProperty(PsiModifier.PROTECTED))
                this.numPublicAttributes++;
    }

    public void addMethod(MethodMetrics method) {
        if (!this.methodMetrics.contains(method)) {
            this.methodMetrics.add(method);
            this.numMethods++;
        } else {
            int index = this.methodMetrics.indexOf(method);
            this.methodMetrics.set(index, method);
        }
    }

    private void buildSSMMatrix() {
        List<PsiField> fields = Arrays.asList(this.targetClass.getFields());

        for (int i = 0; i < this.methods.size(); i++) {
            ArrayList<Double> matrixRow = new ArrayList<>();
            for (int j = 0; j < this.methods.size(); j++) {
                double ssm = this.calculateSSM(fields, this.methods.get(i), this.methods.get(j));
                if (i == j) {
                    matrixRow.add(1.0);
                } else if (j < i) {
                    matrixRow.add(ssm);
                } else {
                    matrixRow.add(ssm);
                }
            }
            this.ssmMatrix.add(matrixRow);
        }
    }

    private void buildCDMMatrix() {
        for (int i = 0; i < this.methods.size(); i++) {
            ArrayList<Double> matrixRow = new ArrayList<>();
            for (int j = 0; j < this.methods.size(); j++) {
                double cdm = Math.max(this.calculateCDM(this.methods, this.methods.get(i), this.methods.get(j)), this.calculateCDM(methods, methods.get(j), methods.get(i)));
                if (i == j) {
                    matrixRow.add(1.0);
                } else if (j < i) {
                    matrixRow.add(cdm);
                } else {
                    matrixRow.add(cdm);
                }
            }
            this.cdmMatrix.add(matrixRow);
        }
    }

    private void buildCSMMatrix() {
        if (this.methods.size() > 1) {
            ArrayList<Double> firstColumn = new ArrayList<>();
            ArrayList<Double> secondColumn = new ArrayList<>();

            for (int i = 0; i < this.decomposedMatrixV.m; i++) {
                firstColumn.add(this.decomposedMatrixV.get(i, 0));
                secondColumn.add(this.decomposedMatrixV.get(i, 1));
            }

            ArrayList<ArrayList<Double>> methodMatrix = new ArrayList<>();

            for (int i = 0; i < this.methods.size(); i++) {
                ArrayList<Double> aux = new ArrayList<>();
                aux.add(firstColumn.get(i));
                aux.add(secondColumn.get(i));
                methodMatrix.add(aux);
            }

            for (int i = 0; i < this.methods.size(); i++) {
                ArrayList<Double> matrixRow = new ArrayList<>();
                for (int j = 0; j < this.methods.size(); j++) {
                    double csm = this.calculateCSM(methodMatrix.get(i), methodMatrix.get(j));
                    if (i == j) {
                        matrixRow.add(1.0);
                    } else if (j < i) {
                        matrixRow.add(csm);
                    } else {
                        matrixRow.add(csm);
                    }
                }
                this.csmMatrix.add(matrixRow);
            }
        }
    }

    private void buildFinalWeightMatrix() {
        if (this.methods.size() > 1) {
            for (int i = 0; i < this.methods.size(); i++) {
                ArrayList<Double> matrixRow = new ArrayList<>();
                for (int j = 0; j < this.methods.size(); j++) {
                    double weight = this.calculateFinalWeight(this.ssmMatrix.get(i).get(j), this.cdmMatrix.get(i).get(j), this.csmMatrix.get(i).get(j));
                    if (i == j) {
                        matrixRow.add(1.0);
                    } else if (j < i) {
                        matrixRow.add(weight);
                    } else {
                        matrixRow.add(weight);
                    }
                }
                this.weightMatrix.add(matrixRow);
            }
        } else {
            ArrayList<Double> aux = new ArrayList<>();
            aux.add(1.0);
            this.weightMatrix.add(aux);
        }
    }

    private double calculateSSM(List<PsiField> fields, PsiMethod method1, PsiMethod method2) {
        Set<PsiField> sharedOccurrences = new HashSet();
        Set<PsiField> totalFieldOccurrences = new HashSet();

        for (PsiField field : fields) {
            if (metricsUtils.isFieldUsedOnMethod(field, method1) && metricsUtils.isFieldUsedOnMethod(field, method2)) {
                sharedOccurrences.add(field);
            }

            if (metricsUtils.isFieldUsedOnMethod(field, method1) || metricsUtils.isFieldUsedOnMethod(field, method2)) {
                totalFieldOccurrences.add(field);
            }
        }

        if (totalFieldOccurrences.size() == 0) {
            return 0;
        }

        DecimalFormat df2 = new DecimalFormat("#.##");
        String result = df2.format(sharedOccurrences.size() / totalFieldOccurrences.size());
        return Double.parseDouble(result);
    }

    private double calculateCDM(List<PsiMethod> methods, PsiMethod method1, PsiMethod method2) {
        double callsDoneToMethod2ByMethod1 = metricsUtils.amountOfMethodCalls(method1, method2);
        double totalCallsDoneToMethod2 = 0.0;

        for (PsiMethod method : methods) {
            totalCallsDoneToMethod2 += metricsUtils.amountOfMethodCalls(method, method2);
        }

        if (totalCallsDoneToMethod2 == 0) {
            return 0;
        }

        DecimalFormat df2 = new DecimalFormat("#.##");
        String result = df2.format(callsDoneToMethod2ByMethod1 / totalCallsDoneToMethod2);
        return Double.parseDouble(result);
    }

    private double calculateCSM(ArrayList<Double> method1Vector, ArrayList<Double> method2Vector) {
        double result = utilities.dot(method1Vector, method2Vector) / (this.norm(method1Vector) * this.norm(method2Vector));

        return Math.max(result, 0.0);
    }

    private double calculateFinalWeight(double ssmValue, double cdmValue, double csmValue) {
        DecimalFormat df2 = new DecimalFormat("#.##");
        String result = df2.format(ClassMetrics.SSM_WEIGHT * ssmValue + ClassMetrics.CDM_WEIGHT * cdmValue + ClassMetrics.CSM_WEIGHT * csmValue);
        return Double.parseDouble(result);
    }

    private void calculateThreshold() {
        ArrayList<Double> filteredValues = new ArrayList<>();

        for (int i = 0; i < this.weightMatrix.size(); i++) {
            for (int j = i; j < this.weightMatrix.get(i).size(); j++)
                this.weightMatrix.get(i).remove(j);

            for (Double aDouble : this.weightMatrix.get(i)) {
                filteredValues.add(aDouble);
            }
        }

        System.out.println(weightMatrix.get(0).size());
        System.out.println(filteredValues.size());
        double max = filteredValues.size() > 0 ? Collections.max(filteredValues) : 0;
        double min = filteredValues.size() > 0 ? Collections.min(filteredValues): 0;

        this.threshold = (max - min) * ClassMetrics.THRESHOLD_MULTIPLIER + min;
    }

    private double norm(ArrayList<Double> vector) {
        return Math.sqrt(vector.get(0) * vector.get(0) + vector.get(1) * vector.get(1));
    }

    private void buildVocabularyDictionary() {
        Set<String> vocabularyDictionary = new HashSet<>();

        for (PsiMethod m : this.methods) {
            for (PsiIdentifier psiIdentifier : PsiTreeUtil.findChildrenOfType(m, PsiIdentifier.class)) {
                vocabularyDictionary.add(psiIdentifier.getText());
            }
        }

        this.vocabularyDictionary = vocabularyDictionary;
    }

    private void buildVocabularyOccurrencesMatrix() {
        for (PsiMethod m : this.methods) {
            ArrayList<Double> matrixRow = new ArrayList<>();
            for (String vocabulary : this.vocabularyDictionary) {
                matrixRow.add((double) metricsUtils.amountOfDictionaryOccurrencesInMethod(vocabulary, m));
            }
            this.vocabularyOccurrencesMatrix.add(matrixRow);
        }
    }

    public boolean equals(ClassMetrics c) {
        if (Objects.requireNonNull(this.targetClass.getName()).equals(c.targetClass.getName())) {
            return this.numProperties == c.numProperties &&
                    this.numPublicAttributes == c.numPublicAttributes;
        }
        return false;
    }

    public String toString() {
        StringBuilder info = new StringBuilder(this.className + "\nNum. Properties: " + this.numProperties + "\nNum. Public Attributes: " +
                this.numPublicAttributes + "\nNum. Lines Code: " + this.numLinesCode + "\nLack of Cohesion: " +
                this.lackOfCohesion + "\nCyclomatic Complexity: " + this.complexity + "\nNum. Methods: " +
                this.numMethods + "\nNum. Constructors: " + this.numConstructors + "\nNum. Long Methods: " +
                this.numLongMethods + "\n\n\n");

        if (this.methodMetrics.size() > 0) {
            info.append("\n------------------- Methods/Constructors -------------------\n");
            for (MethodMetrics m : this.methodMetrics) {
                info.append(m.toString());
            }
        }

        return info.toString();
    }
}
