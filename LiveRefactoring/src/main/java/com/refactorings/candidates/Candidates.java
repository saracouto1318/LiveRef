package com.refactorings.candidates;

import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiJavaFile;
import com.refactorings.*;
import com.refactorings.candidates.utils.Severity;
import com.utils.Refactorings;
import com.utils.SelectedRefactorings;
import com.utils.Values;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Candidates {
    public ArrayList<Severity> severities;
    public ExtractVariable extractVariable;
    public ExtractMethod extractMethod;
    public ExtractClass extractClass;
    public MoveMethod moveMethod;
    public IntroduceParameterObject paramObj;

    public Candidates() {
        severities = new ArrayList<>();
        extractVariable = null;
        extractMethod = null;
        extractClass = null;
        moveMethod = null;
        paramObj = null;
    }

    public ArrayList<Severity> getCandidates(Editor editor, PsiJavaFile psiJavaFile) {
        if(SelectedRefactorings.selectedRefactoring == null && SelectedRefactorings.selectedRefactorings.size() == 0)
            SelectedRefactorings.selectedRefactoring = Refactorings.All;

        if (SelectedRefactorings.selectedRefactoring == Refactorings.All) {
            long start = System.nanoTime();
            //version 1
            this.extractMethod = new ExtractMethod(editor, psiJavaFile, 1);
            this.extractMethod.run();

            /*this.extractClass = new ExtractClass(psiJavaFile);
            this.extractClass.run();*/

            /*this.extractVariable = new ExtractVariable(psiJavaFile, editor);
            this.extractVariable.run();*/

            //version 2 - best
            //this.extractMethod.candidates = this.extractMethod.candidates.stream().limit(1).collect(Collectors.toList());

            /*this.extractClass = new ExtractClass(psiJavaFile);
            this.extractClass.run();
            this.extractClass.candidates = this.extractClass.candidates.stream().limit(1).collect(Collectors.toList());*/

            /*this.extractVariable = new ExtractVariable(psiJavaFile, editor);
            this.extractVariable.run();
            this.extractVariable.candidates = this.extractVariable.candidates.stream().limit(1).collect(Collectors.toList());
            */

            //version 3 - 10 best
            //this.extractMethod.candidates = this.extractMethod.candidates.stream().limit(10).collect(Collectors.toList());

            /*this.extractClass = new ExtractClass(psiJavaFile);
            this.extractClass.run();
            this.extractClass.candidates = this.extractClass.candidates.stream().limit(10).collect(Collectors.toList());

            /*this.extractVariable = new ExtractVariable(psiJavaFile, editor);
            this.extractVariable.run();
            this.extractVariable.candidates = this.extractVariable.candidates.stream().limit(10).collect(Collectors.toList());
            */

            //version 4 - cursor
            //this.extractMethod = new ExtractMethod(editor, psiJavaFile, 4);
            //this.extractMethod.run();

            /*this.extractVariable = new ExtractVariable(psiJavaFile, editor, 4);
            this.extractVariable.run();*/

            //version 5 - cursor + around
            //this.extractMethod = new ExtractMethod(editor, psiJavaFile, 5);
            //this.extractMethod.run();

            //version 6 - best cursor
            //this.extractMethod = new ExtractMethod(editor, psiJavaFile, 6);
            //this.extractMethod.run();
            //this.extractMethod.candidates = this.extractMethod.candidates.stream().limit(1).collect(Collectors.toList());

            /*this.extractVariable = new ExtractVariable(psiJavaFile, editor, 6);
            this.extractVariable.run();
            this.extractVariable.candidates = this.extractVariable.candidates.stream().limit(1).collect(Collectors.toList());
             */

            //version 7 - best cursor + around
            //this.extractMethod = new ExtractMethod(editor, psiJavaFile, 7);
            //this.extractMethod.run();



            /*this.moveMethod = new MoveMethod(editor, psiJavaFile);
            this.moveMethod.run();*/

            /*this.paramObj = new IntroduceParameterObject(editor, psiJavaFile);
            this.paramObj.run();*/

          /*this.extractVariable = new ExtractVariable(psiJavaFile, editor);
            this.extractVariable.run();
            this.extractVariable.candidates = this.extractVariable.candidates.stream().limit(Values.numRefactorings).collect(Collectors.toList());
            */
            long end = System.nanoTime();
            long elapsedTime = end - start;
            System.out.println("Time: " + elapsedTime);
        } else if (SelectedRefactorings.selectedRefactorings.size() > 0) {
            long start = System.nanoTime();
            for (Refactorings selectedRefactoring : SelectedRefactorings.selectedRefactorings) {
                if (selectedRefactoring == Refactorings.ExtractVariable) {
                    this.extractVariable = new ExtractVariable(psiJavaFile, editor, 1);
                    this.extractVariable.run();
                    this.extractVariable.candidates = this.extractVariable.candidates.stream().limit(Values.numRefactorings).collect(Collectors.toList());

                } else if (selectedRefactoring == Refactorings.ExtractClass) {
                    this.extractClass = new ExtractClass(psiJavaFile, editor, 1);
                    this.extractClass.run();
                    this.extractClass.candidates = this.extractClass.candidates.stream().limit(Values.numRefactorings).collect(Collectors.toList());

                } else if (selectedRefactoring == Refactorings.ExtractMethod) {
                    this.extractMethod = new ExtractMethod(editor, psiJavaFile, 1);
                    this.extractMethod.run();
                    this.extractMethod.candidates = this.extractMethod.candidates.stream().limit(Values.numRefactorings).collect(Collectors.toList());
                }
            }

            long end = System.nanoTime();
            long elapsedTime = end - start;
            System.out.println("Time: " + elapsedTime);
        }

        return this.getSeverities();
    }

    public ArrayList<Severity> getSeverities() {
        ArrayList<List<Object>> candidates = new ArrayList<>();

        if(this.extractMethod != null)
            if(this.extractMethod.candidates.size() > 0) {
                List<Object> obj2 = new ArrayList<>(extractMethod.candidates);
                candidates.add(obj2);
            }
        if(this.extractClass != null)
            if(this.extractClass.candidates.size() > 0) {
                List<Object> obj3 = new ArrayList<>(extractClass.candidates);
                candidates.add(obj3);
            }
        if(this.extractVariable != null)
            if(this.extractVariable.candidates.size() > 0) {
                List<Object> obj = new ArrayList<>(extractVariable.candidates);
                candidates.add(obj);
            }
        if(this.moveMethod != null)
            if(this.moveMethod.candidates.size() > 0) {
                List<Object> obj4 = new ArrayList<>(moveMethod.candidates);
                candidates.add(obj4);
            }
        if(this.paramObj != null)
            if(this.paramObj.candidates.size() > 0) {
                List<Object> obj5 = new ArrayList<>(paramObj.candidates);
                candidates.add(obj5);
            }


        return this.calculateSeverities(candidates);
    }

    public ArrayList<Severity> calculateSeverities(ArrayList<List<Object>> candidates) {
        if(candidates.size() > 1){
            for (List<Object> candidate : candidates) {
                severities.addAll(calculateSeveritiesByType(candidate));
            }
        }
        else if(candidates.size() == 1){
            severities.addAll(calculateSeveritiesByType(candidates.get(0)));
        }

        return severities;
    }

    public ArrayList<Severity> calculateSeveritiesByType(List<Object> candidates){
        ArrayList<Integer> aux = new ArrayList<>();
        ArrayList<Severity> auxSeverity = new ArrayList<>();
        int maxValue = 10;
        for (int i = (candidates.size() - 1); i >= 0; i--)
            aux.add(i);

        if(aux.size() == 1){
           auxSeverity.add(new Severity(candidates.get(0), maxValue, 1));
        }
        else {
            for (int i = 0; i < aux.size(); i++) {
                int scaledMax = Collections.max(aux);
                int scaledMin = Collections.min(aux);
                double severity = normalize(aux.get(i), scaledMin, scaledMax, 1, maxValue);
                auxSeverity.add(new Severity(candidates.get(i), severity, 0));
            }
            if (auxSeverity.size() > maxValue) {
                int numPerColor = (int) Math.ceil((double)auxSeverity.size() / maxValue);
                int i = 1;
                int numTries = 0;

                for (Severity severity : auxSeverity) {
                    if (numTries < numPerColor) {
                        severity.indexColorGutter = i;
                        numTries++;
                    }

                    if (numTries == numPerColor) {
                        numTries = 0;
                        i++;
                    }
                }
            } else {
                for (Severity severity : auxSeverity) {
                    severity.indexColorGutter = auxSeverity.indexOf(severity) + 1;
                }
            }
        }

        return auxSeverity;
    }

    public double normalize(double value, int min, int max, int a, int b) {
        return (b - a) * ((value - min) / (max - min)) + a;
    }
}
