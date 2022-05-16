package com.refactorings;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.*;
import com.metrics.ClassMetrics;
import com.metrics.FileMetrics;
import com.refactorings.candidates.ExtractClassCandidate;
import com.refactorings.candidates.utils.LastRefactoring;
import com.ui.ClassWrapper;
import com.ui.ExtractClassRefactoring;
import com.utils.*;

import java.util.*;
import java.util.stream.Stream;

public class ExtractClass implements Runnable {

    public RefactorUtils refactorUtils = new RefactorUtils();
    public Utilities utilities = new Utilities();
    public DistanceCalculator distanceCalculator = new DistanceCalculator();
    public PsiJavaFile sourceFile;
    public List<ExtractClassCandidate> candidates;
    public Editor editor;
    public int version = 0;

    public ExtractClass(PsiJavaFile sourceFile, Editor editor, int version) {
        this.sourceFile = sourceFile;
        this.editor = editor;
        this.candidates = new ArrayList<>();
        this.version = version;
    }

    public ExtractClass() {
        this.candidates = new ArrayList<>();
    }

    public void extractClass(ExtractClassCandidate candidate, double severity, int index) {
        Runnable runnable = () -> {
            ClassWrapper wrapper = new ClassWrapper();
            wrapper.show();
            String className;

            if (wrapper.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
                className = wrapper.textField_classname.getText();

                Set<PsiField> fields = new HashSet<>(candidate.targetAttributes);
                Set<PsiMethod> methods = new HashSet<>(candidate.targetMethods);

                PsiElement[] elements = new PsiElement[candidate.targetEntities.size()];
                for (int i = 0; i < elements.length; i++) {
                    elements[i] = candidate.targetEntities.get(i);
                }

                Values.lastRefactoring = new LastRefactoring(candidate.targetClass, elements, severity, index);

                ExtractClassRefactoring extract = new ExtractClassRefactoring(candidate.file, candidate.targetClass,
                        fields, methods, new HashSet<>(), className);

                extract.apply();
                System.out.println("============ Extract Class Done!!! ============");
                Values.isRefactoring = true;
            }
        };

        Application application = ApplicationManager.getApplication();

        if (application.isDispatchThread()) {

            application.runWriteAction(runnable);

        } else {
            application.invokeLater(() -> application.runWriteAction(runnable));
        }
    }

    @Override
    public void run() {
        FileMetrics metrics = null;
        if(Values.before != null) {
            metrics = Values.after;
            Values.before = metrics;
        }
        else{
            boolean exists = false;
            for(int i=0; i < Values.openedFiles.size(); i++){
                if(Values.openedFiles.get(i).fileName.equals(sourceFile.getName())){
                    metrics = Values.openedFiles.get(i);
                    break;
                }
            }
            if(!exists) {
                metrics = new FileMetrics(editor, sourceFile);
                Values.openedFiles.add(metrics);
            }
            Values.before = metrics;
        }
        Values.currentFile = metrics;

        List<ExtractClassCandidate> candidatesAux = new ArrayList<>();
        int minimumNumberOfMethods = ThresholdsCandidates.minNumExtractedMethods;
        double percentageOriginalMethods = ThresholdsCandidates.maxOrigMethodPercentage / 100.00;

        for (PsiClass c : sourceFile.getClasses()) {
            List<PsiMethod> methods = refactorUtils.getMethods(c);
            ClassMetrics classMetrics = new ClassMetrics(c);
            if(classMetrics.numLinesCode >= 300 && classMetrics.numLongMethods >= 5){
                List<List<PsiMethod>> methodCombinations = utilities.generateCombinations(methods);
                ArrayList<ArrayList<String>> methodsToBeExtractedNames = new ArrayList<>();

                for (ArrayList<PsiMethod> listOfMethods : refactorUtils.getMethodsToBeExtracted(classMetrics.targetClass, classMetrics.weightMatrix)) {

                    ArrayList<String> names = new ArrayList<>();
                    for (PsiMethod listOfMethod : listOfMethods) {
                        names.add(listOfMethod.getName());
                    }

                    methodsToBeExtractedNames.add(names);
                }

                for (List<PsiMethod> ms : methodCombinations) {
                    if (ms.size() <= methods.size() - minimumNumberOfMethods) {
                        ArrayList<PsiElement> entities = new ArrayList<>(ms);

                        ExtractClassCandidate newCandidate = new ExtractClassCandidate(sourceFile, c, entities, classMetrics, sourceFile.getPackageName());
                        ArrayList<String> candidateMethodNames = new ArrayList<>();
                        for (PsiMethod target : newCandidate.targetMethods) {
                            candidateMethodNames.add(target.getName());
                        }

                        for (ArrayList<String> mn : methodsToBeExtractedNames) {
                            if (utilities.areArraysEqual(mn, candidateMethodNames)) {
                                candidatesAux.add(newCandidate);
                            }
                        }
                    }
                }
            }
        }

        Stream<ExtractClassCandidate> streams =  candidatesAux.stream().sorted(new Comparator<ExtractClassCandidate>() {
            @Override
            public int compare(ExtractClassCandidate a, ExtractClassCandidate b) {
                int value1 = b.targetMethods.size() - a.targetMethods.size();
                if(value1 == 0){
                    int value2 = b.classMetrics.numLinesCode - a.classMetrics.numLinesCode;
                    if(value2 == 0){
                        int value3 = b.classMetrics.numLongMethods - a.classMetrics.numLongMethods;
                        if(value3 == 0){
                            int value4 = (int)(b.classMetrics.lackOfCohesion-a.classMetrics.lackOfCohesion);
                            if(value4 == 0){
                                return (int)(b.classMetrics.complexity - a.classMetrics.complexity);
                            }
                        }
                        return value3;
                    }
                    return value2;
                }
                return value1;
            }
        });

        candidatesAux = new ArrayList<>();
        streams.forEachOrdered(candidatesAux::add);

        List<ExtractClassCandidate> candidatesTemp = new ArrayList<>();
        candidates = new ArrayList<>();
        for (ExtractClassCandidate aux : candidatesAux) {
            boolean found = false;
            for (ExtractClassCandidate candidate : candidatesTemp) {
                if(candidate.toString().equals(aux)) {
                    found = true;
                    break;
                }
            }
            if(!found)
                candidatesTemp.add(aux);
        }

        for (ExtractClassCandidate candidate : candidatesTemp) {
            int numMethods = refactorUtils.getMethods(candidate.targetClass).size();
            if (candidate.targetMethods.size() >= minimumNumberOfMethods && (numMethods - candidate.targetMethods.size()) > 0 && (numMethods - candidate.targetMethods.size()) <= (percentageOriginalMethods * numMethods)) {
                candidates.add(candidate);
            }
        }

        System.out.println("Extract Class: " + this.candidates.size());
    }
}


