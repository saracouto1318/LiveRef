package com.refactorings;

import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.introduceparameterobject.IntroduceParameterObjectDialog;
import com.metrics.FileMetrics;
import com.metrics.MethodMetrics;
import com.refactorings.candidates.IntroduceParamObjCandidate;
import com.refactorings.candidates.utils.LastRefactoring;
import com.utils.ThresholdsCandidates;
import com.utils.Values;

import java.util.ArrayList;
import java.util.List;

public class IntroduceParameterObject implements Runnable{
    public Editor editor;
    public PsiJavaFile file;
    public List<IntroduceParamObjCandidate> candidates;

    public IntroduceParameterObject(Editor editor) {
        this.editor = editor;
        this.candidates = new ArrayList<>();
    }

    public IntroduceParameterObject(Editor editor, PsiJavaFile file){
        this.editor = editor;
        this.file = file;
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
                if(Values.openedFiles.get(i).fileName.equals(file.getName())){
                    metrics = Values.openedFiles.get(i);
                    break;
                }
            }
            if(!exists) {
                metrics = new FileMetrics(editor, file);
                Values.openedFiles.add(metrics);
            }
            Values.before = metrics;
        }
        Values.currentFile = metrics;

        for (PsiMethod psiMethod : PsiTreeUtil.findChildrenOfType(file, PsiMethod.class)) {
            if(psiMethod.getText().contains("@Override")){
                if(psiMethod.getParameterList().getParametersCount() >= ThresholdsCandidates.minValueParameters){
                    this.candidates.add(new IntroduceParamObjCandidate(psiMethod, editor, file));
                }
            }
        }

        this.candidates.sort((a, b) -> b.originalParameters.size() - a.originalParameters.size());


        System.out.println("Introduce Parameter Object: " + this.candidates.size());
    }

    public void introduceParamObj(IntroduceParamObjCandidate candidate, double severity, int index){
        /*PsiClass _class = candidate.method.getContainingClass();
        PsiPackage pack = JavaPsiFacade.getInstance(this.editor.getProject()).findPackage(this.file.getPackageName());
        PackageWrapper wrapper = new PackageWrapper(pack);
        AutocreatingSingleSourceRootMoveDestination destination = new AutocreatingSingleSourceRootMoveDestination(wrapper, this.file.getVirtualFile());

        ParameterInfoImpl[] parameters = new ParameterInfoImpl[candidate.originalParameters.size()];
        for (int i = 0; i < candidate.originalParameters.size(); i++) {
            parameters[i] = new ParameterInfoImpl(i, candidate.originalParameters.get(i).getName(), candidate.originalParameters.get(i).getType());
        }

        JavaIntroduceParameterObjectClassDescriptor descriptor =
                new JavaIntroduceParameterObjectClassDescriptor(_class.getName(), this.file.getPackageName(), destination,
                        true, true, "public", parameters, candidate.method, true);

        IntroduceParameterObjectProcessor processor = new IntroduceParameterObjectProcessor(candidate.method,
                descriptor, candidate.originalParameters, true);*/
        IntroduceParameterObjectDialog dialog = new IntroduceParameterObjectDialog(candidate.method);
        dialog.show();

        PsiElement[] elements = new PsiElement[candidate.originalParameters.size()];
        for (int i = 0; i < candidate.originalParameters.size(); i++) {
            elements[i] = candidate.originalParameters.get(i);
        }

        Values.lastRefactoring = new LastRefactoring(candidate.method, "Introduce Parameter Object", elements, severity, index);
        System.out.println("============ Introduce Parameter Object Done!!! ============");
        Values.isRefactoring = true;
    }
}
