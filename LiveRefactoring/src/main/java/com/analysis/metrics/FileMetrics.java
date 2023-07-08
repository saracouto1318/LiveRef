package com.analysis.metrics;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import com.utils.RefactorUtils;
import com.utils.UtilitiesOverall;

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

    public UtilitiesOverall utilitiesOverall = new UtilitiesOverall();
    public RefactorUtils refactorUtilities = new RefactorUtils();

    public Editor editor;
    public PsiJavaFile javaFile;

    public FileMetrics(FileMetrics copy){
        this.editor = copy.editor;
        this.javaFile = copy.javaFile;
        this.utilitiesOverall = copy.utilitiesOverall;
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

        this.methodMetrics = copy.methodMetrics;
        this.classMetrics = copy.classMetrics;
    }

    public FileMetrics(Editor editor, PsiJavaFile sourceFile) throws Exception {
        Document document = editor.getDocument();
        this.javaFile = sourceFile;
        this.fileName = sourceFile.getName();
        this.editor = editor;

        this.initializeLineMetrics(document);
        this.initializeComponents(sourceFile);
        this.measureLCOM();
    }

    public FileMetrics(PsiJavaFile sourceFile) throws Exception {
        this.javaFile = sourceFile;
        this.fileName = sourceFile.getName();

        this.initializeLineMetrics();
        this.initializeComponents(sourceFile);
        this.measureLCOM();
    }

    public void initializeLineMetrics() {
        this.numberOfLines = this.javaFile.getText().split("\n").length;

        this.numberOfBlankLines = 0;
        this.numberOfCommentLines = 0;
        this.numberOfCodeLines = 0;
        this.numLongMethods = 0;

        String regexComment = "//.*|(\"(?:\\\\[^\"]|\\\\\"|.)*?\")|(?s)/\\*.*?\\*/";

        String[] lines = this.javaFile.getText().split("\n");
        while (lines.length != this.numberOfLines)
            lines = utilitiesOverall.append(lines, " ");

        for (String line : lines) {
            if (line.length() == 0)
                this.numberOfBlankLines++;
            else if (line.matches(regexComment))
                this.numberOfCommentLines++;
            else
                this.numberOfCodeLines++;
        }
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
            lines = utilitiesOverall.append(lines, " ");

        for (String line : lines) {
            if (line.length() == 0)
                this.numberOfBlankLines++;
            else if (line.matches(regexComment))
                this.numberOfCommentLines++;
            else
                this.numberOfCodeLines++;
        }
    }

    public void initializeComponents(PsiJavaFile sourceFile) throws Exception {
        for (PsiClass aClass : sourceFile.getClasses()) {
            this.classMetrics.add(new ClassMetrics(aClass));
        }

        for (ClassMetrics classMetric : this.classMetrics) {
            this.methodMetrics.addAll(classMetric.methodMetrics);
        }

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

        for (ClassMetrics classMetrics : this.classMetrics) {
            this.complexity += classMetrics.complexity;
            this.cognitiveComplexity += classMetrics.cognitiveComplexity;
            this.cognitiveComplexityPercentage += classMetrics.cognitiveComplexityPercentage;
            this.halsteadLength += classMetrics.halsteadLength;
            this.halsteadVocabulary += classMetrics.halsteadVocabulary;
            this.halsteadVolume += classMetrics.halsteadVolume;
            this.halsteadDifficulty += classMetrics.halsteadDifficulty;
            this.halsteadEffort += classMetrics.halsteadEffort;
            this.halsteadLevel += classMetrics.halsteadLevel;
            this.halsteadTime += classMetrics.halsteadTime;
            this.halsteadBugsDelivered += classMetrics.halsteadBugsDelivered;
            this.halsteadMaintainability += classMetrics.halsteadMaintainability;
        }

        this.complexity = this.complexity/this.classMetrics.size();
        this.cognitiveComplexity = this.cognitiveComplexity / this.classMetrics.size();
        this.cognitiveComplexityPercentage = this.cognitiveComplexityPercentage / this.classMetrics.size();
        this.halsteadLength = this.halsteadLength/this.classMetrics.size();
        this.halsteadVocabulary = this.halsteadVocabulary/this.classMetrics.size();
        this.halsteadVolume = this.halsteadVolume/this.classMetrics.size();
        this.halsteadDifficulty = this.halsteadDifficulty/this.classMetrics.size();
        this.halsteadEffort = this.halsteadEffort/this.classMetrics.size();
        this.halsteadLevel = this.halsteadLevel/this.classMetrics.size();
        this.halsteadTime = this.halsteadTime/this.classMetrics.size();
        this.halsteadBugsDelivered = this.halsteadBugsDelivered/this.classMetrics.size();
        this.halsteadMaintainability = Math.max(0.0, (171 - 5.2 * Math.log(this.halsteadVolume) - 0.23 * this.complexity - 16.2 * Math.log(this.numberOfCodeLines)) * 100 / (double) 171);
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
            int indexClass = this.classMetrics.indexOf(this.getClassMetrics(method._class));
            try {
                this.classMetrics.set(indexClass, new ClassMetrics(method._class));
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.methodMetrics.add(method);
            this.numberOfMethods++;
        } else {
            int index = this.methodMetrics.indexOf(method);
            this.methodMetrics.set(index, method);
            int indexClass = this.classMetrics.indexOf(this.getClassMetrics(method._class));
            try {
                this.classMetrics.set(indexClass, new ClassMetrics(method._class));
            } catch (Exception e) {
                e.printStackTrace();
            }
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

    public ClassMetrics getClassMetrics(PsiClass _class){
        for (ClassMetrics classMetrics : this.classMetrics) {
            if(classMetrics.className.equals(_class.getName()) &&
                    classMetrics.targetClass.getText().equals(_class.getText()))
                return classMetrics;
        }
        return null;
    }

    public String toString() {
        StringBuilder info = new StringBuilder(this.fileName + "\n" + "Num. Lines: " + this.numberOfLines + "\nNum. Code Lines: " +
                this.numberOfCodeLines + "\nNum. Classes: " + this.numberOfClasses + "\nNum. Methods: " +
                this.numberOfMethods + "\nNum. Long Methods: " + this.numLongMethods + "\nLack of Cohesion: " +
                this.lackOfCohesion + "\nCC: " + this.complexity + "\nCog: " + this.cognitiveComplexity + "\nLength: " +
                this.halsteadLength + "\nDifficulty: " + this.halsteadDifficulty + "\nVolume: " +
                this.halsteadVolume + "\nEffort: " + this.halsteadEffort + "\nTime: " +
                this.halsteadTime + "\nBuggs: " + this.halsteadBugsDelivered + "\nMaintainability: " +
                this.halsteadMaintainability);

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
