package com.analysis.refactorings;

import com.analysis.candidates.InheritanceToDelegationCandidate;
import com.analysis.metrics.ClassMetrics;
import com.core.LastRefactoring;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.inheritanceToDelegation.InheritanceToDelegationDialog;
import com.intellij.refactoring.util.classMembers.MemberInfo;
import com.utils.RefactorUtils;
import com.utils.importantValues.ThresholdsCandidates;
import com.utils.importantValues.Values;

import java.util.*;
import java.util.stream.Stream;

public class InheritanceToDelegation {
    public RefactorUtils refactorUtils = new RefactorUtils();
    public PsiJavaFile psiJavaFile = null;
    public Editor editor;

    public InheritanceToDelegation(Editor editor, PsiJavaFile file) {
        this.editor = editor;
        this.psiJavaFile = file;
    }

    public InheritanceToDelegation(Editor editor) {
        this.editor = editor;
    }

    public void inheritanceToDelegation(InheritanceToDelegationCandidate candidate, double severity, int index){
        Values.isRefactoring = true;
        ArrayList<MemberInfo> memberInfos = new ArrayList<>();
        for (PsiMember psiMember : PsiTreeUtil.findChildrenOfType(candidate.target, PsiMember.class)) {
            MemberInfo info = new MemberInfo(psiMember);
            memberInfos.add(info);
        }
        PsiClass[] targets = new PsiClass[]{candidate.target};
        HashMap<PsiClass, Collection<MemberInfo>> targetMembers = new HashMap<>();
        targetMembers.put(candidate.target, memberInfos);
        InheritanceToDelegationDialog dialog = new InheritanceToDelegationDialog(this.editor.getProject(), candidate._class, targets, targetMembers);
        dialog.setTitle("Inheritance To Delegation Class " + candidate._class.getName());
        ApplicationManager.getApplication().invokeAndWait(dialog::show);


        PsiField[] fields = candidate._class.getFields();
        PsiMethod[] methods = candidate._class.getMethods();

        ArrayList<PsiElement> elementsAux = new ArrayList<>();

        Collections.addAll(elementsAux, fields);
        Collections.addAll(elementsAux, methods);

        PsiElement[] elements = new PsiElement[elementsAux.size()];

        for (int i=0; i<elementsAux.size(); i++) {
            elements[i] = elementsAux.get(i);
        }

        Values.lastRefactoring = new LastRefactoring(candidate._class, elements, Values.currentFile, severity, index, "Inheritance To Delegation");
        Values.allID.add(candidate);
        System.out.println("============ Inheritance To Delegation Done!!! ============");
    }

    public void run() {
        Values.inheritanceDelegation = new ArrayList<>();
        PsiClass[] classes = psiJavaFile.getClasses();

        for (PsiClass aClass : classes) {
            if (!aClass.isEnum() && !aClass.isInterface() && aClass.getText().contains("extends")) {
                for (ClassMetrics classMetric : Values.currentFile.classMetrics) {
                    if (classMetric.targetClass.equals(aClass)) {
                        PsiClass superClass = aClass.getSuperClass();
                        if (superClass != null && !superClass.equals(aClass)) {
                            if(refactorUtils.getAllClasses().contains(superClass.getName())){
                                int counterOverrideMethods = 0;
                                int counterInheriteMethods = 0;
                                Set<PsiMethod> inheriteMethods = new HashSet<>();

                                for (PsiMethod method : superClass.getMethods()) {
                                    for (PsiMethod aClassMethod : aClass.getMethods()) {
                                        if (method.getName().equals(aClassMethod.getName()) && method.getParameterList().getParametersCount() == aClassMethod.getParameterList().getParametersCount())
                                            counterOverrideMethods++;
                                    }
                                }
                                for (PsiMethod aClassMethod : aClass.getMethods()) {
                                    if (aClassMethod.isConstructor()) {
                                        for (PsiSuperExpression psiSuper : PsiTreeUtil.findChildrenOfType(aClassMethod, PsiSuperExpression.class))
                                            counterInheriteMethods++;
                                    }
                                }

                                for (PsiMethodCallExpression psiMethodCallExpression : PsiTreeUtil.findChildrenOfType(aClass, PsiMethodCallExpression.class)) {
                                    PsiMethod methodCalled = psiMethodCallExpression.resolveMethod();
                                    if (methodCalled != null)
                                        if (methodCalled.getContainingClass() != null)
                                            if (methodCalled.getContainingClass().equals(superClass))
                                                inheriteMethods.add(methodCalled);
                                }

                                counterInheriteMethods += inheriteMethods.size();

                                if((classMetric.numProtectedFields + classMetric.numProtectedMethods) >= (ThresholdsCandidates.protectedFields + ThresholdsCandidates.protectedMethods)){
                                    if (checkMetrics(counterInheriteMethods, counterOverrideMethods, aClass)) {
                                        Values.inheritanceDelegation.add(new InheritanceToDelegationCandidate(aClass, superClass, Values.currentFile, psiJavaFile,
                                                (counterInheriteMethods / aClass.getMethods().length), (counterOverrideMethods / aClass.getMethods().length)));
                                    }
                                }
                                else if((classMetric.numPublicAttributes + classMetric.numPublicMethods) >= (ThresholdsCandidates.protectedFields + ThresholdsCandidates.protectedMethods)){
                                    if (checkMetrics(counterInheriteMethods, counterOverrideMethods, aClass)) {
                                        Values.inheritanceDelegation.add(new InheritanceToDelegationCandidate(aClass, superClass, Values.currentFile, psiJavaFile,
                                                (counterInheriteMethods / aClass.getMethods().length), (counterOverrideMethods / aClass.getMethods().length)));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        List<InheritanceToDelegationCandidate> copy = new ArrayList<>();
        for (InheritanceToDelegationCandidate elem : Values.inheritanceDelegation) {
            boolean found = false;
            for (InheritanceToDelegationCandidate candidate : copy) {
                if(candidate.toString().equals(elem.toString())) {
                    found = true;
                    break;
                }
            }
            if(!found) copy.add(elem);
        }

        Stream<InheritanceToDelegationCandidate> streams =  copy.stream().sorted((a, b) -> {
            int value1 = Double.compare(b.inherited, a.inherited);
            if(value1 == 0) return Double.compare(b.override, a.override);
            return value1;
        });

        Values.inheritanceDelegation = new ArrayList<>();
        streams.forEachOrdered(Values.inheritanceDelegation::add);
        System.out.println("Inheritance To Delegation Candidates: " + Values.inheritanceDelegation.size());
    }

    public boolean checkMetrics(int counterInheriteMethods, int counterOverrideMethods, PsiClass aClass){
        if (((double) (counterInheriteMethods / aClass.getMethods().length) < (ThresholdsCandidates.inheriteMethods / 100.00) ||
                ((double) (counterOverrideMethods / aClass.getMethods().length) < (ThresholdsCandidates.overrideMethods / 100.00)))) {
            return true;}
        return false;
    }
}
