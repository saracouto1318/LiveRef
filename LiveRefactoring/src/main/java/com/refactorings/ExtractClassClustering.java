package com.refactorings;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.*;
import com.metrics.ClassMetrics;
import com.refactorings.candidates.ExtractClassCandidate;
import com.refactorings.candidates.utils.LastRefactoring;
import com.refactorings.candidates.utils.clustering.Cluster;
import com.refactorings.candidates.utils.clustering.HierarchicalClustering;
import com.ui.ClassWrapper;
import com.ui.ExtractClassRefactoring;
import com.utils.*;

import java.util.*;

public class ExtractClassClustering implements Runnable {

    public RefactorUtils refactorUtils = new RefactorUtils();
    public Utilities utilities = new Utilities();
    public DistanceCalculator distanceCalculator = new DistanceCalculator();
    public PsiJavaFile sourceFile;
    public Set<ExtractClassCandidate> candidates;

    public ExtractClassClustering(PsiJavaFile sourceFile) {
        this.sourceFile = sourceFile;
        this.candidates = new HashSet<>();
    }

    public ExtractClassClustering() {
        this.candidates = new HashSet<>();
    }

    public void extractClass(ExtractClassCandidate candidate) {
        executingExtractClass(candidate);
    }

    static void executingExtractClass(ExtractClassCandidate candidate) {
        Runnable runnable = () -> {
            ClassWrapper wrapper = new ClassWrapper();
            wrapper.show();
            String className;

            if (wrapper.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
                className = wrapper.textField_classname.getText();

                for (RangeHighlighter rangeHighlighter : Values.gutters) {
                    rangeHighlighter.setGutterIconRenderer(null);
                }

                PsiElement[] elements = new PsiElement[candidate.targetEntities.size()];
                for (int i = 0; i < elements.length; i++) {
                    elements[i] = candidate.targetEntities.get(i);
                }

                Values.lastRefactoring = new LastRefactoring(candidate.targetClass, elements, 0, 0);

                Set<PsiField> fields = new HashSet<>(candidate.targetAttributes);
                Set<PsiMethod> methods = new HashSet<>(candidate.targetMethods);

                ExtractClassRefactoring extract = new ExtractClassRefactoring(candidate.file, candidate.targetClass,
                        fields, methods, new HashSet<>(), className);

                extract.apply();
                System.out.println("============ Extract Class Done!!! ============");
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
        PsiClass[] classIt = sourceFile.getClasses();
        Set<String> classNamesToBeExamined = new HashSet<>();
        ArrayList<PsiClass> oldClasses = new ArrayList<>();
        List<ExtractClassCandidate> candidatesAux = new ArrayList<>();

        for (PsiClass psiClass : classIt) {
            if (!psiClass.isEnum() && !psiClass.isInterface()) {
                classNamesToBeExamined.add(psiClass.getName());
                oldClasses.add(psiClass);
            }
        }

        for (PsiClass sourceClass : oldClasses) {
            if (sourceClass.getMethods().length > 0 && sourceClass.getFields().length > 0) {
                double[][] distanceMatrix = distanceCalculator.getJaccardDistanceMatix(sourceClass);
                HierarchicalClustering clustering = new HierarchicalClustering(distanceMatrix);
                ArrayList<PsiElement> entities = new ArrayList<>();
                entities.addAll(Arrays.asList(sourceClass.getFields()));
                entities.addAll(refactorUtils.getMethods(sourceClass));
                HashSet<Cluster> clusters = clustering.getClusters(entities);

                for (Cluster cluster : clusters) {
                    ClassMetrics classMetrics = new ClassMetrics(sourceClass);
                    ExtractClassCandidate candidate = new ExtractClassCandidate(sourceFile, sourceClass, cluster.getEntities(), classMetrics, sourceFile.getPackageName());
                    if (candidate.isApplicable()) {
                        int numMethods = refactorUtils.getMethods(candidate.targetClass).size();
                        double percentageOriginalMethods = ThresholdsCandidates.maxOrigMethodPercentage / 100.00;
                        if (candidate.targetMethods.size() >= ThresholdsCandidates.minNumExtractedMethods && (numMethods - candidate.targetMethods.size()) > 0 && (numMethods - candidate.targetMethods.size()) <= (percentageOriginalMethods * numMethods))
                            candidatesAux.add(candidate);
                    }
                }
            }
        }

        candidatesAux.sort((a, b) -> (int) (b.targetMethods.size() - a.targetMethods.size() +
                (b.classMetrics.numLinesCode - a.classMetrics.numLinesCode + b.classMetrics.numLongMethods + a.classMetrics.numLongMethods) +
                b.classMetrics.lackOfCohesion - a.classMetrics.lackOfCohesion + b.classMetrics.complexity - a.classMetrics.complexity));

        candidates = new HashSet<>(candidatesAux);

        System.out.println("Extract Class: " + this.candidates.size());
    }
}


