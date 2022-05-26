package com.metrics;

import com.intellij.lang.jvm.JvmModifier;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.thoughtworks.qdox.library.JavaClassContext;
import com.utils.RefactorUtils;
import com.utils.Utilities;

import java.util.ArrayList;

public class FileMetrics {
    public String fileName;

    public int numberOfLines = 0;
    public int numberOfBlankLines = 0;
    public int numberOfCodeLines = 0;
    public int numberOfCommentLines = 0;
    public int numLongMethods = 0;
    public double lines = 0.0;

    public int numberOfClasses = 0;
    public int numberOfMethods = 0;

    public double lackOfCohesion = 0.0;
    public double complexity = 0.0;
    public double cognitiveComplexity = 0.0;
    public double cognitiveComplexityPercentage = 0.0;

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

    public ArrayList<MethodMetrics> methodMetrics = new ArrayList<>();
    public ArrayList<ClassMetrics> classMetrics = new ArrayList<>();

    public Utilities utilities = new Utilities();
    public RefactorUtils refactorUtilities = new RefactorUtils();

    public Editor editor;
    public PsiJavaFile javaFile;

    public FileMetrics(FileMetrics copy){
        this.editor = copy.editor;
        this.javaFile = copy.javaFile;
        this.utilities = copy.utilities;
        this.refactorUtilities = copy.refactorUtilities;

        this.fileName = copy.fileName;
        this.numberOfLines = copy.numberOfLines;
        this.numberOfBlankLines = copy.numberOfBlankLines;
        this.numberOfCodeLines = copy.numberOfCodeLines;
        this.numberOfCommentLines = copy.numberOfCommentLines;
        this.lines = copy.lines;
        this.lackOfCohesion = copy.lackOfCohesion;
        this.complexity = copy.complexity;
        this.cognitiveComplexity = copy.cognitiveComplexity;
        this.cognitiveComplexityPercentage = copy.cognitiveComplexityPercentage;
        this.methodMetrics = copy.methodMetrics;
        this.classMetrics = copy.classMetrics;
        this.numberOfMethods = copy.numberOfMethods;
        this.numberOfClasses = copy.numberOfClasses;
        this.numLongMethods = copy.numLongMethods;
        this.longParameterList = copy.longParameterList;
        this.halsteadLength = copy.halsteadLength;
        this.halsteadVocabulary = copy.halsteadVocabulary;
        this.halsteadVolume = copy.halsteadVolume;
        this.halsteadDifficulty = copy.halsteadDifficulty;
        this.halsteadEffort = copy.halsteadEffort;
        this.halsteadLevel = copy.halsteadLevel;
        this.halsteadTime = copy.halsteadTime;
        this.halsteadBugsDelivered = copy.halsteadBugsDelivered;
        this.halsteadMaintainability = copy.halsteadMaintainability;
    }

    public FileMetrics(Editor editor, PsiJavaFile sourceFile) {
        Document document = editor.getDocument();
        this.javaFile = sourceFile;
        this.fileName = sourceFile.getName();
        this.editor = editor;

        this.initializeLineMetrics(document);
        this.initializeComponents(sourceFile);
        this.measureLCOM();
    }

    public void initializeLineMetrics(Document document) {
        this.numberOfLines = document.getLineCount();

        this.numberOfBlankLines = 0;
        this.numberOfCommentLines = 0;
        this.numberOfCodeLines = 0;
        this.numLongMethods = 0;

        String regexComment = "//.*|(\"(?:\\\\[^\"]|\\\\\"|.)*?\")|(?s)/\\*.*?\\*/";

        String[] lines = document.getText().split("\n");
        while (lines.length != this.numberOfLines)
            lines = utilities.append(lines, " ");

        for (String line : lines) {
            if (line.length() == 0)
                this.numberOfBlankLines++;
            else if (line.matches(regexComment))
                this.numberOfCommentLines++;
            else
                this.numberOfCodeLines++;
        }
    }

    public void initializeComponents(PsiJavaFile sourceFile) {

       for (PsiMethod psiMethod : PsiTreeUtil.findChildrenOfType(sourceFile, PsiMethod.class)) {
           this.methodMetrics.add(new MethodMetrics(psiMethod.getContainingClass(), psiMethod,
                       sourceFile.getName().equals(psiMethod.getName() + ".java") ?  true : false));
       }

        /*for (PsiClass psiClass : PsiTreeUtil.findChildrenOfType(sourceFile, PsiClass.class)) {
            this.classMetrics.add(new ClassMetrics(psiClass));
        }*/

        this.numberOfClasses = this.classMetrics.size();
        this.numberOfMethods = this.methodMetrics.size();

        measurePrimeMetrics();
    }

    public void measurePrimeMetrics(){
        this.complexity = 0;
        this.cognitiveComplexity = 0;
        this.cognitiveComplexityPercentage = 0;
        this.halsteadLength = 0;
        this.halsteadVocabulary = 0;
        this.halsteadVolume = 0;
        this.halsteadDifficulty = 0;
        this.halsteadEffort = 0;
        this.halsteadLevel = 0;
        this.halsteadTime = 0;
        this.halsteadBugsDelivered = 0;
        this.halsteadMaintainability = 0;

        for (MethodMetrics methodMetric : this.methodMetrics) {
            this.complexity += methodMetric.complexityOfMethod;
            this.cognitiveComplexity += methodMetric.cognitiveComplexity;
            this.cognitiveComplexityPercentage += methodMetric.cognitiveComplexityPercentage;
            this.halsteadLength += methodMetric.halsteadLength;
            this.halsteadVocabulary += methodMetric.halsteadVocabulary;
            this.halsteadVolume += methodMetric.halsteadVolume;
            this.halsteadDifficulty += methodMetric.halsteadDifficulty;
            this.halsteadEffort += methodMetric.halsteadEffort;
            this.halsteadLevel += methodMetric.halsteadLevel;
            this.halsteadTime += methodMetric.halsteadTime;
            this.halsteadBugsDelivered += methodMetric.halsteadBugsDelivered;
            this.halsteadMaintainability += methodMetric.halsteadMaintainability;
        }

        this.complexity = this.complexity/this.methodMetrics.size();
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
        this.halsteadMaintainability = (double) Math.max(0.0, (171 - 5.2 * Math.log(this.halsteadVolume) - 0.23 * this.complexity - 16.2 * Math.log(this.numberOfCodeLines)) * 100 / (double) 171);
        this.lines = (double) this.numberOfCodeLines / this.numberOfMethods;
        this.methodMetrics.forEach(method -> {
            if (method.isLong)
                this.numLongMethods++;
            if(method.longParameterList)
                this.longParameterList++;
        });
    }

    public void measureLCOM() {
        double lcom = 0.0;
        if (this.methodMetrics.size() != 0) {
            for (MethodMetrics methodMetric : this.methodMetrics) lcom += methodMetric.lackOfCohesionInMethod;
            lcom = lcom / this.methodMetrics.size();
        }
        this.lackOfCohesion = lcom;
    }

    public void setMetrics(Document document){
        this.initializeLineMetrics(document);
        this.measurePrimeMetrics();
        this.measureLCOM();
    }

    public void addClass(ClassMetrics _class) {
        if (!this.classMetrics.contains(_class)) {
            this.classMetrics.add(_class);
            this.numberOfClasses++;
        } else {
            int index = this.classMetrics.indexOf(_class);
            this.classMetrics.set(index, _class);
        }
    }

    public void addMethod(MethodMetrics method) {
        if (!this.methodMetrics.contains(method)) {
            this.methodMetrics.add(method);
            this.numberOfMethods++;
        } else {
            int index = this.methodMetrics.indexOf(method);
            this.methodMetrics.set(index, method);
        }
    }

    public MethodMetrics getMethodMetrics(PsiMethod method){
        for (MethodMetrics methodMetric : this.methodMetrics) {
            if(methodMetric.methodName.equals(method.getName()) &&
                    methodMetric.method.getContainingClass().getName().equals(method.getContainingClass().getName()) &&
                    methodMetric.method.getBody().getText().equals(method.getBody().getText()))
                return methodMetric;
        }
        return null;
    }

    public String toString() {
        StringBuilder info = new StringBuilder(this.fileName + "\n" + "Num. Lines: " + this.numberOfLines + "\nNum. Code Lines: " +
                this.numberOfCodeLines + "\nNum. Classes: " + this.numberOfClasses + "\nNum. Methods: " +
                this.numberOfMethods + "\nNum. Long Methods: " + this.numLongMethods + "\nLack of Cohesion: " +
                this.lackOfCohesion);

        if (this.classMetrics.size() > 0) {
            info.append("\n\n\n------------------- Classes -------------------\n");
            for (ClassMetrics c : this.classMetrics) {
                info.append(c.toString());
            }
        }

        if (this.methodMetrics.size() > 0) {
            info.append("\n\n\n------------------- Methods -------------------\n");
            for (MethodMetrics c : this.methodMetrics) {
                info.append(c.toString());
            }
        }

        return info.toString();
    }
}
