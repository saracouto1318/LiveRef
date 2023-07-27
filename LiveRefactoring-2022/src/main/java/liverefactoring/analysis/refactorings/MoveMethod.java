package liverefactoring.analysis.refactorings;

import liverefactoring.analysis.candidates.MoveMethodCandidate;
import liverefactoring.analysis.metrics.MethodMetrics;
import liverefactoring.core.LastRefactoring;
import liverefactoring.core.MyRange;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.psi.*;
import com.intellij.psi.util.PropertyUtil;
import com.intellij.psi.util.PsiTreeUtil;
import liverefactoring.ui.MyMoveInstanceMethodDialog;
import liverefactoring.utils.JDeodorant.utils.PsiUtils;
import liverefactoring.utils.RefactorUtils;
import liverefactoring.utils.importantValues.MoveObject;
import liverefactoring.utils.importantValues.Values;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MoveMethod{

    public PsiJavaFile psiJavaFile = null;
    public Editor editor;

    public MoveMethod(Editor editor, PsiJavaFile file) {
        this.editor = editor;
        this.psiJavaFile = file;

    }

    public MoveMethod(Editor editor) {
        this.editor = editor;
        Values.moveMethod = new ArrayList<>();

    }

    public ArrayList<MoveObject> classesIncludingMethod(PsiMethod method){
        ArrayList<MoveObject> objects = new ArrayList<>();
        RefactorUtils utils = new RefactorUtils();
        List<PsiJavaFile> allClasses = utils.getAllClassesFiles();
        ArrayList<PsiVariable> vars = new ArrayList<>();

        for (PsiJavaFile psiFile : allClasses) {
            for (PsiClass aClass : psiFile.getClasses()) {
                int counter = 0;
                for (PsiMethodCallExpression psiMethodCallExpression : PsiTreeUtil.findChildrenOfType(aClass, PsiMethodCallExpression.class)) {
                    if(psiMethodCallExpression.resolveMethod() != null){
                        PsiMethod callingMethod = psiMethodCallExpression.resolveMethod();
                        if(callingMethod.getName().equals(method.getName())){
                            if(callingMethod.getContainingClass() != null){
                                if(callingMethod.getContainingClass().getName().equals(method.getContainingClass().getName()) && !callingMethod.getContainingClass().getName().equals(aClass.getName())){
                                    counter++;
                                }
                            }
                        }
                    }
                }
                objects.add(new MoveObject(aClass, counter, new ArrayList<>()));
            }
        }

        for (PsiParameter parameter : method.getParameterList().getParameters()) {
            if(parameter.getType() instanceof PsiClassType){
                if(!vars.contains(parameter));
                vars.add(parameter);
            }
        }

        for (PsiField field : method.getContainingClass().getFields()) {
            if(field.getType() instanceof PsiClassType){
                if(!vars.contains(field));
                vars.add(field);
            }
        }

        for (MoveObject object : objects) {
            ArrayList<PsiVariable> aux =  new ArrayList<>();
            for (PsiVariable var : vars) {
                if(((PsiClassType) var.getType()).resolve() != null) {
                    if (object._class.getName().equals(((PsiClassType) var.getType()).resolve().getName())) {
                        aux.add(var);
                    }
                }
            }
            object.variables = aux;
        }

        return objects;
    }

    public HashMap<PsiClass, ArrayList<PsiVariable>> classesIncluded(PsiMethod method){
        HashMap<PsiClass, ArrayList<PsiVariable>>classes = new HashMap<>();
        ArrayList<PsiVariable> vars = new ArrayList<>();
        RefactorUtils utils = new RefactorUtils();
        ArrayList<String> allClasses = utils.getAllClasses();

        for (PsiParameter parameter : method.getParameterList().getParameters()) {
            if(parameter.getType() instanceof PsiClassType){
                if(!vars.contains(parameter));
                vars.add(parameter);
                if(((PsiClassType) parameter.getType()).resolve() != null) {
                    if (allClasses.contains(((PsiClassType) parameter.getType()).resolve().getName()))
                        classes.put(((PsiClassType) parameter.getType()).resolve(), new ArrayList<>());
                }
            }
        }

        for (PsiField field : method.getContainingClass().getFields()) {
            if(field.getType() instanceof PsiClassType){
                if(!vars.contains(field));
                vars.add(field);
                if(((PsiClassType) field.getType()).resolve() != null) {
                    if (allClasses.contains(((PsiClassType) field.getType()).resolve().getName()))
                        classes.put(((PsiClassType) field.getType()).resolve(), new ArrayList<>());
                }
            }
        }

        for (PsiClass psiClass : classes.keySet()) {
            ArrayList<PsiVariable> aux =  new ArrayList<>();
            for (PsiVariable var : vars){
                if(((PsiClassType) var.getType()).resolve() != null) {
                    if (psiClass.getName().equals(((PsiClassType) var.getType()).resolve().getName())) {
                        aux.add(var);
                    }
                }
            }
            classes.put(psiClass,aux);
        }

        return classes;
    }

    public boolean isGetterSetter(PsiMethod method){
        for (PsiField field : method.getContainingClass().getFields()) {
            PsiMethod psiMethodSetter = PropertyUtil.findPropertySetter(field.getContainingClass(), field.getName(), false, false);
            PsiMethod psiMethodGetter = PropertyUtil.findPropertyGetter(field.getContainingClass(), field.getName(), false, false);
            if(psiMethodSetter != null)
                if(psiMethodSetter.getName().equals(method.getName()))
                    return true;
            if(psiMethodGetter != null)
                if(psiMethodGetter.getName().equals(method.getName()))
                    return true;
        }

        return false;
    }

    public void runOther() {
        ArrayList<MoveMethodCandidate> firstCandidates = new ArrayList<>();
        Values.moveMethod = new ArrayList<>();

        for (MethodMetrics methodMetric : Values.currentFile.methodMetrics) {
            for (PsiClass aClass : psiJavaFile.getClasses()) {
                if (!aClass.isEnum() && !aClass.isInterface()) {
                    for (PsiMethod method : aClass.getMethods()) {
                        if(!method.isConstructor() || isGetterSetter(method)) {
                            if (methodMetric.methodName.equals(method.getName()) && methodMetric._class != null && methodMetric._class.getName().equals(aClass.getName())) {
                                ArrayList<MoveObject> objects = classesIncludingMethod(method);
                                MoveObject ownClass = null;
                                for (MoveObject object : objects) {
                                    if(object._class.getName().equals(method.getContainingClass().getName()))
                                        ownClass = object;
                                }

                                for (MoveObject object : objects) {
                                    if(object.counterOccurrences != 0){
                                        if(ownClass == null){
                                            if(!object._class.getName().equals(method.getContainingClass().getName())){
                                                LogicalPosition start = editor.offsetToLogicalPosition(method.getTextRange().getStartOffset());
                                                LogicalPosition end = editor.offsetToLogicalPosition(method.getTextRange().getEndOffset());
                                                PsiVariable[] variables = new PsiVariable[object.variables.size()];
                                                for (int i=0; i<object.variables.size(); i++) {
                                                    variables[i] = object.variables.get(i);
                                                }
                                                firstCandidates.add(new MoveMethodCandidate(new MyRange(start, end), method, aClass, object._class, object.counterOccurrences, this.psiJavaFile, variables));
                                            }
                                        }
                                        else{
                                           if(object.counterOccurrences >= ownClass.counterOccurrences) {
                                               if(!object._class.getName().equals(method.getContainingClass().getName())){
                                                   LogicalPosition start = editor.offsetToLogicalPosition(method.getTextRange().getStartOffset());
                                                   LogicalPosition end = editor.offsetToLogicalPosition(method.getTextRange().getEndOffset());
                                                   PsiVariable[] variables = new PsiVariable[object.variables.size()];
                                                   for (int i=0; i<object.variables.size(); i++) {
                                                       variables[i] = object.variables.get(i);
                                                   }
                                                   firstCandidates.add(new MoveMethodCandidate(new MyRange(start, end), method, aClass, object._class, object.counterOccurrences, this.psiJavaFile, variables));
                                               }
                                           }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }


        List<MoveMethodCandidate> secondCandidates = new ArrayList<>();
        for (MoveMethodCandidate elem : firstCandidates) {
            boolean found = false;
            for (MoveMethodCandidate candidate : secondCandidates) {
                if(candidate.toString().equals(elem.toString())) {
                    found = true;
                    break;
                }
            }
            if(!found) {
                secondCandidates.add(elem);
            }
        }


        secondCandidates.sort((a, b) -> Double.compare(b.distance, a.distance));
        Values.moveMethod.addAll(secondCandidates);
        System.out.println("Move Method Candidates: " + Values.moveMethod.size());
    }

    public void run() {
        ArrayList<MoveMethodCandidate> firstCandidates = new ArrayList<>();
        Values.moveMethod = new ArrayList<>();

        for (MethodMetrics methodMetric : Values.currentFile.methodMetrics) {
            for (PsiClass aClass : psiJavaFile.getClasses()) {
                if (!aClass.isEnum() && !aClass.isInterface()) {
                    for (PsiMethod method : aClass.getMethods()) {
                        if(!method.isConstructor()) {
                            if (methodMetric.methodName.equals(method.getName()) && methodMetric._class != null && methodMetric._class.getName().equals(aClass.getName())) {
                                HashMap<PsiClass, Integer> codeReferences = new HashMap<>();
                                HashMap<PsiClass, ArrayList<PsiVariable>> classes = classesIncluded(method);
                                for (PsiClass allClass : classes.keySet()) {
                                    codeReferences.put(allClass, 0);
                                }
                                codeReferences.put(aClass, 0);

                                for (PsiMethodCallExpression psiMethodCallExpression : PsiTreeUtil.findChildrenOfType(method, PsiMethodCallExpression.class)) {
                                    if (psiMethodCallExpression.resolveMethod() != null) {
                                        if (psiMethodCallExpression.resolveMethod().getContainingClass() != null) {
                                            if (codeReferences.containsKey(psiMethodCallExpression.resolveMethod().getContainingClass())) {
                                                codeReferences.put(psiMethodCallExpression.resolveMethod().getContainingClass(), codeReferences.get(psiMethodCallExpression.resolveMethod().getContainingClass()) + 1);
                                            }
                                        }
                                    }
                                }

                                for (PsiReferenceExpression psiReferenceExpression : PsiTreeUtil.findChildrenOfType(method, PsiReferenceExpression.class)) {
                                    if (psiReferenceExpression.getType() instanceof PsiClassType) {
                                        if (((PsiClassType) psiReferenceExpression.getType()).resolve() != null) {
                                            if (codeReferences.containsKey(((PsiClassType) psiReferenceExpression.getType()).resolve())) {
                                                codeReferences.put(((PsiClassType) psiReferenceExpression.getType()).resolve(), codeReferences.get(((PsiClassType) psiReferenceExpression.getType()).resolve()) + 1);
                                            }
                                        }
                                    }
                                }

                                double ownClassDistance = codeReferences.get(aClass);

                                for (PsiClass psiClass : codeReferences.keySet()) {
                                    if (codeReferences.get(psiClass) != 0.0 && codeReferences.get(psiClass) >= ownClassDistance && !psiClass.getName().equals(aClass.getName())) {
                                        LogicalPosition start = editor.offsetToLogicalPosition(method.getTextRange().getStartOffset());
                                        LogicalPosition end = editor.offsetToLogicalPosition(method.getTextRange().getEndOffset());
                                        PsiVariable[] variables = new PsiVariable[classes.get(psiClass).size()];
                                        for (int i=0; i<classes.get(psiClass).size(); i++) {
                                            variables[i] = classes.get(psiClass).get(i);
                                        }
                                        firstCandidates.add(new MoveMethodCandidate(new MyRange(start, end), method, aClass, psiClass, codeReferences.get(psiClass), this.psiJavaFile, variables));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }


        List<MoveMethodCandidate> secondCandidates = new ArrayList<>();
        for (MoveMethodCandidate elem : firstCandidates) {
            boolean found = false;
            for (MoveMethodCandidate candidate : secondCandidates) {
                if(candidate.toString().equals(elem.toString())) {
                    found = true;
                    break;
                }
            }
            if(!found) {
                secondCandidates.add(elem);
            }
        }


        secondCandidates.sort((a, b) -> Double.compare(b.distance, a.distance));
        Values.moveMethod.addAll(secondCandidates);
        System.out.println("Move Method Candidates: " + Values.moveMethod.size());
}

    public void moveMethod(MoveMethodCandidate candidate, double severity, int index){
        Values.isRefactoring = true;
        PsiVariable[] available = candidate.variables;
        if (available.length == 0) {
            throw new IllegalStateException("Cannot move instance method");
        }
        MyMoveInstanceMethodDialog dialog = new MyMoveInstanceMethodDialog(candidate.method, available);
        dialog.setTitle("Move Instance Method " + PsiUtils.calculateSignature(candidate.method));
        ApplicationManager.getApplication().invokeAndWait(dialog::show);

        PsiElement[] elements = new PsiElement[candidate.method.getBody().getStatementCount()];
        for (int i = 0; i < candidate.method.getBody().getStatements().length; i++) {
            elements[i] = candidate.method.getBody().getStatements()[i];
        }
        Values.lastRefactoring = new LastRefactoring(candidate.method, candidate.originalClass, elements, Values.currentFile, severity, index, "Move Method");
        Values.allMM.add(candidate);
        System.out.println("============ Move Method Done!!! ============");
    }
}
