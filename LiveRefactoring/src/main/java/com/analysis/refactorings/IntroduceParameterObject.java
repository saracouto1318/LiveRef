package com.analysis.refactorings;

import com.analysis.candidates.IntroduceParamObjCandidate;
import com.core.LastRefactoring;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.introduceparameterobject.IntroduceParameterObjectDialog;
import com.utils.importantValues.ThresholdsCandidates;
import com.utils.importantValues.Values;

import java.util.ArrayList;
import java.util.List;

public class IntroduceParameterObject{
    public Editor editor;
    public PsiJavaFile file;

    public IntroduceParameterObject(Editor editor) {
        this.editor = editor;
        Values.introduceParam = new ArrayList<>();
    }

    public IntroduceParameterObject(Editor editor, PsiJavaFile file){
        this.editor = editor;
        this.file = file;
    }

    public void run() {
        Values.introduceParam = new ArrayList<>();

        for (PsiMethod psiMethod : PsiTreeUtil.findChildrenOfType(file, PsiMethod.class)) {
            if(!psiMethod.getText().contains("@Override") && psiMethod.getParameterList().getParametersCount() >= ThresholdsCandidates.minValueParameters)
                Values.introduceParam.add(new IntroduceParamObjCandidate(psiMethod, editor, file));
        }

        List<IntroduceParamObjCandidate> copy = new ArrayList<>();
        for (IntroduceParamObjCandidate elem : Values.introduceParam) {
            boolean found = false;
            for (IntroduceParamObjCandidate candidate : copy) {
                if(candidate.toString().equals(elem.toString())) {
                    found = true;
                    break;
                }
            }
            if(!found) copy.add(elem);
        }

        copy.sort((a, b) -> b.originalParameters.size() - a.originalParameters.size());
        Values.introduceParam = new ArrayList<>();
        Values.introduceParam.addAll(copy);
        System.out.println("Introduce Parameter Object Candidates: " + Values.introduceParam.size());
    }

    public void introduceParamObj(IntroduceParamObjCandidate candidate, double severity, int index){
        Values.isRefactoring = true;
        IntroduceParameterObjectDialog dialog = new IntroduceParameterObjectDialog(candidate.method);
        dialog.show();

        PsiElement[] elements = new PsiElement[candidate.originalParameters.size()];
        for (int i = 0; i < candidate.originalParameters.size(); i++) {
            elements[i] = candidate.originalParameters.get(i);
        }

        Values.lastRefactoring = new LastRefactoring(candidate.method, "Introduce Parameter Object", elements, Values.currentFile, severity, index);
        Values.allIPO.add(candidate);
        System.out.println("============ Introduce Parameter Object Done!!! ============");
    }
}
