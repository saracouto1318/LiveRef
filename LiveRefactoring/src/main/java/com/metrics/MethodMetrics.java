package com.metrics;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.utils.MetricsUtils;
import com.utils.RefactorUtils;

import java.util.ArrayList;
import java.util.Objects;

public class MethodMetrics {
    public PsiMethod method;
    public PsiClass _class;
    public String methodName;
    public String className;
    public boolean isConstructor;
    public int complexityOfMethod = 1;
    public int cognitiveComplexity = 1;
    public double cognitiveComplexityPercentage = 0.0;
    public int numberOfStatements = 0;
    public double lackOfCohesionInMethod = 0.0;
    public int numberLinesOfCode = 0;
    public int numberComments = 0;
    public int numberBlankLines = 0;
    public int numParameters = 0;
    public boolean isLong = false;
    public boolean longParameterList = false;
    public int halsteadLength = 0;
    public int halsteadVocabulary = 0;
    public double halsteadVolume = 0.0;
    public double halsteadDifficulty = 0.0;
    public double halsteadEffort = 0.0;
    public double halsteadLevel = 0.0;
    public double halsteadTime = 0.0;
    public double halsteadBugsDelivered = 0.0;
    public double halsteadMaintainability = 0.0;
    private final MetricsUtils metricUtils = new MetricsUtils();
    private final RefactorUtils utils = new RefactorUtils();

    public MethodMetrics(PsiClass _class, PsiMethod method, boolean isConstructor) {
        this._class = _class;
        this.method = method;
        this.className = (_class ==  null) ? "" : _class.getName();
        this.methodName = method.getName();
        this.isConstructor = isConstructor;

        this.initializeMetrics();
    }

    public MethodMetrics(MethodMetrics copy){
        method = copy.method;
        _class = copy._class;
        methodName = copy.methodName;
        className = copy.className;
        isConstructor = copy.isConstructor;
        complexityOfMethod = copy.complexityOfMethod;
        cognitiveComplexity = copy.cognitiveComplexity;
        cognitiveComplexityPercentage = copy.cognitiveComplexityPercentage;
        numberOfStatements = copy.numberOfStatements;
        lackOfCohesionInMethod = copy.lackOfCohesionInMethod;
        numberLinesOfCode = copy.numberLinesOfCode;
        numberComments = copy.numberComments;
        numberBlankLines = copy.numberBlankLines;
        numParameters = copy.numParameters;
        isLong = copy.isLong;
        longParameterList = copy.longParameterList;
        halsteadLength = copy.halsteadLength;
        halsteadVocabulary = copy.halsteadVocabulary;
        halsteadVolume = copy.halsteadVolume;
        halsteadDifficulty = copy.halsteadDifficulty;
        halsteadEffort = copy.halsteadEffort;
        halsteadLevel = copy.halsteadLevel;
        halsteadTime = copy.halsteadTime;
        halsteadBugsDelivered = copy.halsteadBugsDelivered;
        halsteadMaintainability = copy.halsteadMaintainability;
    }

    public void setMetrics(int complexityOfMethod, int numberOfStatements, double lackOfCohesionInMethod) {
        this.complexityOfMethod = complexityOfMethod;
        this.numberOfStatements = numberOfStatements;
        this.lackOfCohesionInMethod = lackOfCohesionInMethod;
    }

    private void initializeMetrics() {
        int loc = 0;
        int comments = 0;
        int blankLines = 0;
        String regexComment = "//.*|(\"(?:\\\\[^\"]|\\\\\"|.)*?\")|(?s)/\\*.*?\\*/";
        String[] lines;

        if (this.method != null) {
            if(this.method.getBody() != null) {
                lines = Objects.requireNonNull(this.method.getBody()).getText().split("\n");
                for (String s : lines) {
                    String line = s.trim();
                    if (line.length() == 0)
                        blankLines++;
                    else if (line.matches(regexComment))
                        comments++;
                    else
                        loc++;
                }
            }

            this.numberLinesOfCode = loc;
            this.numberComments = comments;
            this.numberBlankLines = blankLines;
            this.lackOfCohesionInMethod = (this._class == null) ? 0.0 : this.metricUtils.initializeLCOM(this._class);

            if (this.numberLinesOfCode > 20)
                this.isLong = true;
            if(this.method.getParameters().length > 5)
                this.longParameterList = true;

            this.complexityOfMethod += this.metricUtils.increasesCyclomaticComplexity(this.method);
            this.cognitiveComplexity += this.metricUtils.increaseCognitiveComplexity(this.method);
            this.cognitiveComplexityPercentage = (double)this.cognitiveComplexity * 100.00 / 80.00;
            this.numberOfStatements = utils.getAllStatements(this.method).size();
            this.numParameters = this.method.getParameters().length;
            this.metricUtils.initializeHalsteadMetrics(this, this.method);
        }
    }

    public boolean equals(MethodMetrics metrics) {
        return this.complexityOfMethod == metrics.complexityOfMethod &&
                this.numberOfStatements == metrics.numberOfStatements &&
                this.lackOfCohesionInMethod == metrics.lackOfCohesionInMethod;
    }

    public String toString() {

        return this.methodName + "\nClass: " + this.className + "\nNum. Lines Code: " +
                this.numberLinesOfCode + "\nNum. Comments: " + this.numberComments + "\nNum. Parameters: " +
                this.numParameters + "\nNum. Statements: " + this.numberOfStatements + "\nIs Long: " +
                this.isLong + "\nLack of Cohesion: " + this.lackOfCohesionInMethod + "\nLength: " +
                this.halsteadLength + "\nVocabulary: " + this.halsteadVocabulary + "\nVolume: " +
                this.halsteadVolume + "\nDifficulty: " + this.halsteadDifficulty + "\nEffort: " +
                this.halsteadEffort + "\nTime: " + this.halsteadTime + "\nBugs Delivered: " +
                this.halsteadBugsDelivered + "\nMaintainability: " + this.halsteadMaintainability +
                "\nComplexity: " + this.complexityOfMethod +
                "\nCog. Complexity: " + this.cognitiveComplexity + "\n\n\n";
    }

}
