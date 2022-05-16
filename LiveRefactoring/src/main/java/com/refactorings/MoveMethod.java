package com.refactorings;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiVariable;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.move.moveInstanceMethod.MoveInstanceMethodDialog;
import com.metrics.FileMetrics;
import com.metrics.MethodMetrics;
import com.refactorings.candidates.MoveMethodCandidate;
import com.refactorings.candidates.utils.LastRefactoring;
import com.refactorings.candidates.utils.MyClass;
import com.refactorings.candidates.utils.MyMethod;
import com.utils.MyRange;
import com.utils.RefactorUtils;
import com.utils.Values;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MoveMethod implements Runnable{
    public RefactorUtils refactorUtils = new RefactorUtils();
    public PsiJavaFile psiJavaFile = null;
    public Editor editor;
    public List<MoveMethodCandidate> candidates;

    public MoveMethod(Editor editor, PsiJavaFile file) {
        this.editor = editor;
        this.psiJavaFile = file;
        this.candidates = new ArrayList<>();
    }

    public MoveMethod(Editor editor) {
        this.editor = editor;
        this.candidates = new ArrayList<>();
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
                if(Values.openedFiles.get(i).fileName.equals(psiJavaFile.getName())){
                    metrics = Values.openedFiles.get(i);
                    break;
                }
            }
            if(!exists) {
                metrics = new FileMetrics(editor, psiJavaFile);
                Values.openedFiles.add(metrics);
            }
            Values.before = metrics;
        }
        Values.currentFile = metrics;

        for (PsiClass aClass : this.psiJavaFile.getClasses()) {
            if(!aClass.isEnum() && !aClass.isInterface()) {
                MyClass myClass = new MyClass(aClass, this.psiJavaFile);
                for (Object o : myClass.entities.keySet()) {
                    if (o instanceof MyMethod) {
                        LogicalPosition cursor = editor.getCaretModel().getLogicalPosition();
                        LogicalPosition startMethod = editor.offsetToLogicalPosition(((MyMethod) o).method.getTextRange().getStartOffset());
                        LogicalPosition endMethod = editor.offsetToLogicalPosition(((MyMethod) o).method.getTextRange().getEndOffset());
                        if(startMethod.line <= cursor.line && cursor.line <= endMethod.line) {
                            System.out.println("entrei");
                            MethodMetrics metricsMethod = Values.before.getMethodMetrics(((MyMethod) o).method);
                            int foreignData = myClass.accessedMethodsOut.get(o).size() + myClass.accessedAttributesOut.get(o).size();
                            int ownData = myClass.accessedMethodsIn.get(o).size() + myClass.accessedAttributesIn.get(o).size();
                            //if (metricsMethod.isLong && metricsMethod.lackOfCohesionInMethod > 2 && foreignData > 2 && foreignData > ownData) {
                            System.out.println(metricsMethod.isLong + " " + metricsMethod.lackOfCohesionInMethod);
                            if (metricsMethod.isLong && metricsMethod.lackOfCohesionInMethod > 2) {
                                System.out.println("método: " + metricsMethod.methodName);
                                HashMap<PsiClass, Double> auxPossibleTargets = myClass.accessosMethod.get(o);
                                HashMap<PsiClass, Double> possibleTargets = new HashMap<>();
                                double minDistance = Double.MAX_VALUE;
                                PsiClass currentClass = null;
                                for (PsiClass psiClass : auxPossibleTargets.keySet()) {
                                    if(auxPossibleTargets.get(psiClass) <= minDistance){
                                        minDistance = auxPossibleTargets.get(psiClass);
                                        currentClass = psiClass;
                                    }
                                }
                                if(!currentClass.getName().equals(((MyMethod) o).method.getContainingClass().getName())){
                                    for (PsiClass psiClass : auxPossibleTargets.keySet()) {
                                        if(!psiClass.getName().equals(((MyMethod) o).method.getContainingClass().getName())){
                                            possibleTargets.put(psiClass, auxPossibleTargets.get(psiClass));
                                        }
                                    }
                                    ArrayList<MoveMethodCandidate> auxCandidates = new ArrayList<>();
                                    for (PsiClass psiClass : possibleTargets.keySet()) {
                                        LogicalPosition start = editor.offsetToLogicalPosition(((MyMethod) o).method.getTextRange().getStartOffset());
                                        LogicalPosition end = editor.offsetToLogicalPosition(((MyMethod) o).method.getTextRange().getEndOffset());
                                        auxCandidates.add(new MoveMethodCandidate(new MyRange(start, end), ((MyMethod) o).method, aClass, psiClass, possibleTargets.get(psiClass), this.psiJavaFile));
                                    }

                                    auxCandidates.sort((o1, o2) -> Double.compare(o1.distance, o2.distance));
                                    this.candidates = new ArrayList<>(auxCandidates);
                                }

                                /*Set<Object> set1 = new HashSet<>(myClass.entities.get(o));
                                Set<Object> set2 = new HashSet<>(myClass.classEntities);
                                DistanceCalculator calculator = new DistanceCalculator();
                                double distance = calculator.getDistanceEntities(set1, set2);
                                System.out.println("Distance: " + distance);
                                if (distance > 0.5) {
                                    HashMap<PsiClass, Double> possibleTargets = new HashMap<>();
                                    //talvez fazer contagem de entities de um metodo dentro de uma classe vs fora dessa classe (e guardar valores por classe)
                                    //ordenar 60% 40% (do menos distante para o mais distante)
                                    //se 1º é a própria class OK -> se não refactoring até encontrar no set a própria classe (outros à frente da própria, não interessam)
                                    for (Object o1 : myClass.entities.get(o)) {
                                        if (o1 instanceof MyMethod) {
                                            MyMethod method = (MyMethod) o1;
                                            MyClass targetClass = new MyClass(method.method.getContainingClass(), this.psiJavaFile);
                                            if (!targetClass._class.getName().equals(((MyMethod) o).method.getContainingClass().getName())) {
                                                set2 = new HashSet<>(targetClass.classEntities);
                                                distance = calculator.getDistanceEntities(set1, set2);
                                                System.out.println("Distance2: " + distance);
                                                if (distance <= 0.5)
                                                    possibleTargets.put(targetClass._class, distance);
                                            }
                                        } else if (o1 instanceof MyAttribute) {
                                            MyAttribute attribute = (MyAttribute) o1;
                                            MyClass targetClass = new MyClass(attribute.attribute.getContainingClass(), this.psiJavaFile);
                                            if (!targetClass._class.getName().equals(((MyMethod) o).method.getContainingClass().getName())) {
                                                set2 = new HashSet<>(targetClass.classEntities);
                                                distance = calculator.getDistanceEntities(set1, set2);
                                                System.out.println("Distance3: " + distance);
                                                if (distance <= 0.5)
                                                    possibleTargets.put(targetClass._class, distance);
                                            }
                                        }
                                    }

                                    ArrayList<MoveMethodCandidate> auxCandidates = new ArrayList<>();
                                    for (PsiClass psiClass : possibleTargets.keySet()) {
                                        LogicalPosition start = editor.offsetToLogicalPosition(((MyMethod) o).method.getTextRange().getStartOffset());
                                        LogicalPosition end = editor.offsetToLogicalPosition(((MyMethod) o).method.getTextRange().getEndOffset());
                                        auxCandidates.add(new MoveMethodCandidate(new MyRange(start, end), ((MyMethod) o).method, aClass, psiClass, possibleTargets.get(psiClass)));
                                    }

                                    auxCandidates.sort((o1, o2) -> Double.compare(o1.distance, o2.distance));
                                    this.candidates = new HashSet<>(auxCandidates);
                                }*/
                            }
                        }
                    }
                }
            }
        }

        System.out.println("Move Methods: " + this.candidates.size());
    }

    public void moveMethod(MoveMethodCandidate candidate, double severity, int index){
        PsiVariable[] variables = new PsiVariable[candidate.method.getParameterList().getParameters().length];
        for (int i = 0; i < candidate.method.getParameterList().getParameters().length; i++) {
            for (PsiVariable psiVariable : PsiTreeUtil.findChildrenOfType(candidate.method.getParameterList().getParameters()[i], PsiVariable.class)) {
                variables[i] = psiVariable;
            }
        }

        MoveInstanceMethodDialog dialog = new MoveInstanceMethodDialog(candidate.method, variables);
        dialog.show();

        PsiElement[] elements = new PsiElement[candidate.method.getBody().getStatementCount()];
        for (int i = 0; i < candidate.method.getBody().getStatements().length; i++) {
            elements[i] = candidate.method.getBody().getStatements()[i];
        }

        Values.lastRefactoring = new LastRefactoring(candidate.method, "Move Method", elements, severity, index);
        System.out.println("============ Move Method Done!!! ============");
        Values.isRefactoring = true;
    }
}
