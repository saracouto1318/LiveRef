package liverefactoring.analysis.metrics;

import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import liverefactoring.utils.MetricsUtils;
import liverefactoring.utils.RefactorUtils;
import liverefactoring.utils.UtilitiesOverall;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;

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
    public int numPublicMethods = 0;
    public int numProtectedFields = 0;
    public int numProtectedMethods = 0;
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
    public UtilitiesOverall utilitiesOverall = new UtilitiesOverall();
    private RealMatrix decomposedMatrixV;

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

    public ClassMetrics(PsiClass targetClass) throws Exception {
        this.targetClass = targetClass;
        this.className = targetClass.getName();

        for (PsiMethod method : this.targetClass.getMethods()) {
            this.methods.add(method);
        }

        this.getMetricsPerClass();
    }

    public void runMetricsExtractClass(){
        this.buildSSMMatrix();
        this.buildCDMMatrix();
        this.buildVocabularyDictionary();
        this.buildVocabularyOccurrencesMatrix();
        if (this.vocabularyOccurrencesMatrix.size() > 0) {
            double[][] matrixData = new double[this.vocabularyOccurrencesMatrix.size()][];
            for (int i = 0; i < this.vocabularyOccurrencesMatrix.size(); i++) {
                List<Double> innerList = this.vocabularyOccurrencesMatrix.get(i);
                matrixData[i] = new double[innerList.size()];

                for (int j = 0; j < innerList.size(); j++) {
                    matrixData[i][j] = innerList.get(j);
                }
            }

            RealMatrix matrix = MatrixUtils.createRealMatrix(matrixData);
            SingularValueDecomposition svd = new SingularValueDecomposition(matrix);
            this.decomposedMatrixV = svd.getV();

            this.buildCSMMatrix();
            this.buildFinalWeightMatrix();
            this.calculateThreshold();
        }
    }

    private void getMetricsPerClass() throws Exception {
        String regexComment = "//.*|(\"(?:\\\\[^\"]|\\\\\"|.)*?\")|(?s)/\\*.*?\\*/";
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
            if(method.getModifierList().hasModifierProperty(PsiModifier.PROTECTED))
                this.numProtectedMethods++;
            else if(method.getModifierList().hasModifierProperty(PsiModifier.PUBLIC))
                this.numPublicMethods++;
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

        this.complexity = this.complexity / this.methodMetrics.size();
        this.cognitiveComplexity = this.cognitiveComplexity / this.methodMetrics.size();
        this.cognitiveComplexityPercentage = this.cognitiveComplexityPercentage / this.methodMetrics.size();
        this.halsteadLength = this.halsteadLength/this.methodMetrics.size();
        this.halsteadVocabulary = this.halsteadVocabulary/this.methodMetrics.size();
        this.halsteadVolume = this.halsteadVolume/this.methodMetrics.size();
        this.halsteadDifficulty = this.halsteadDifficulty/this.methodMetrics.size();
        this.halsteadEffort = this.halsteadEffort/this.methodMetrics.size();
        this.halsteadLevel = this.halsteadLevel/this.methodMetrics.size();
        this.halsteadTime = this.halsteadTime/this.methodMetrics.size();
        this.halsteadBugsDelivered = this.halsteadBugsDelivered/this.methodMetrics.size();
        this.halsteadMaintainability = this.halsteadMaintainability/this.methodMetrics.size();

        this.lackOfCohesion = numMethods > 0 ? lcom / numMethods : 0;
        PsiField[] fields = this.targetClass.getFields();
        this.numProperties = fields.length;
        for (PsiField fieldDeclaration : fields) {
            if(fieldDeclaration.getModifierList() != null) {
                if (fieldDeclaration.getModifierList().hasModifierProperty(PsiModifier.PUBLIC))
                    this.numPublicAttributes++;
                if (fieldDeclaration.getModifierList().hasModifierProperty(PsiModifier.PROTECTED))
                    this.numProtectedFields++;
            }
        }
    }

    private void buildSSMMatrix() {
        PsiField[] fields = targetClass.getFields();

        for (int i = 0; i < methods.size(); i++) {
            ArrayList<Double> matrixRow = new ArrayList<>();
            for (int j = 0; j < methods.size(); j++) {
                double ssm = calculateSSM(fields, methods.get(i), methods.get(j));
                if (i == j) {
                    matrixRow.add(1.0);
                } else if (j < i) {
                    matrixRow.add(ssm);
                } else {
                    matrixRow.add(ssm);
                }
            }
            ssmMatrix.add(matrixRow);
        }
    }

    private void buildCDMMatrix() {
        for (int i = 0; i < methods.size(); i++) {
            ArrayList<Double> matrixRow = new ArrayList<>();
            for (int j = 0; j < methods.size(); j++) {
                double cdm = Math.max(calculateCDM(methods, methods.get(i), methods.get(j)), calculateCDM(methods, methods.get(j), methods.get(i)));
                if (i == j) {
                    matrixRow.add(1.0);
                } else if (j < i) {
                    matrixRow.add(cdm);
                } else {
                    matrixRow.add(cdm);
                }
            }
            cdmMatrix.add(matrixRow);
        }
    }

    private void buildCSMMatrix() {
        if (methods.size() > 1) {
            ArrayList<Double> firstColumn = new ArrayList<>();
            ArrayList<Double> secondColumn = new ArrayList<>();
            for (int i = 0; i < this.decomposedMatrixV.getRowDimension(); i++) {
                firstColumn.add(this.decomposedMatrixV.getRow(i)[0]);
                secondColumn.add(this.decomposedMatrixV.getRow(i)[1]);
            }

            double[][] methodMatrix = new double[methods.size()][2];

            for (int i = 0; i < methods.size(); i++) {
                methodMatrix[i] = new double[]{firstColumn.get(i), secondColumn.get(i)};
            }

            for (int i = 0; i < methods.size(); i++) {
                ArrayList<Double> matrixRow = new ArrayList<>();
                for (int j = 0; j < methods.size(); j++) {
                    double csm = calculateCSM(methodMatrix[i], methodMatrix[j]);
                    if (i == j) {
                        matrixRow.add(1.0);
                    } else if (j < i) {
                        matrixRow.add(csm);
                    } else {
                        matrixRow.add(csm);
                    }
                }
                csmMatrix.add(matrixRow);
            }
        } else {
            return;
        }
    }

    private void buildFinalWeightMatrix() {
        if (methods.size() > 1) {
            for (int i = 0; i < methods.size(); i++) {
                ArrayList<Double> matrixRow = new ArrayList<>();
                for (int j = 0; j < methods.size(); j++) {
                    double weight = calculateFinalWeight(ssmMatrix.get(i).get(j), cdmMatrix.get(i).get(j), csmMatrix.get(i).get(j));
                    if (i == j) {
                        matrixRow.add(1.0);
                    } else if (j < i) {
                        matrixRow.add(weight);
                    } else {
                        matrixRow.add(weight);
                    }
                }
                weightMatrix.add(matrixRow);
            }
        } else {
            ArrayList<Double> aux = new ArrayList<>();
            aux.add(1.0);
            weightMatrix.add(aux);
        }
    }

    private double calculateSSM(PsiField[] fields, PsiMethod method1, PsiMethod method2) {
        Set<PsiField> sharedOccurrences = new HashSet<>();
        Set<PsiField> totalFieldOccurrences = new HashSet<>();

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

        return (double) Math.round((sharedOccurrences.size() / totalFieldOccurrences.size())*100)/100;
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

        double result = (double) Math.round((callsDoneToMethod2ByMethod1 / totalCallsDoneToMethod2)*100)/100;
        return result;
    }

    private double calculateCSM(double[] method1Vector, double[] method2Vector) {
        double result = utilitiesOverall.dot(method1Vector, method2Vector) / (this.norm(method1Vector) * this.norm(method2Vector));

        return Math.max(result, 0.0);
    }

    private double calculateFinalWeight(double ssmValue, double cdmValue, double csmValue) {
        double result = (double) Math.round(ClassMetrics.SSM_WEIGHT * ssmValue + ClassMetrics.CDM_WEIGHT * cdmValue + ClassMetrics.CSM_WEIGHT * csmValue * 100) / 100;
        return result;
    }

    private void calculateThreshold() {
        ArrayList<Double> filteredValues = new ArrayList<>();

        for (int i = 0; i < this.weightMatrix.size(); i++) {
            for (int j = i; j < this.weightMatrix.get(i).size(); j++)
                this.weightMatrix.get(i).remove(j);

            filteredValues.addAll(this.weightMatrix.get(i));
        }

        double max = filteredValues.size() > 0 ? Collections.max(filteredValues) : 0;
        double min = filteredValues.size() > 0 ? Collections.min(filteredValues): 0;

        this.threshold = (max - min) * ClassMetrics.THRESHOLD_MULTIPLIER + min;
    }

    private double norm(double[] vector) {
        return Math.sqrt(vector[0] * vector[0] + vector[1] * vector[1]);
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

        int size = 0;
        for (ArrayList<Double> occurrencesMatrix : this.vocabularyOccurrencesMatrix) {
            if(occurrencesMatrix.size() > size)
                size = occurrencesMatrix.size();
        }

        for (ArrayList<Double> occurrencesMatrix : this.vocabularyOccurrencesMatrix) {
            while(occurrencesMatrix.size() < size)
                occurrencesMatrix.add(0.0);
        }
    }

    public boolean equals(ClassMetrics c) {
        if(this.targetClass!=null && this.targetClass.getName() != null) {
            if (this.targetClass.getName().equals(c.targetClass.getName())) {
                return this.numProperties == c.numProperties &&
                        this.numPublicAttributes == c.numPublicAttributes &&
                        this.numProtectedFields == c.numProtectedFields && this.numProtectedMethods == c.numProtectedMethods;
            }
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
