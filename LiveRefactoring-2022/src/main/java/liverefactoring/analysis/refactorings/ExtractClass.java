package liverefactoring.analysis.refactorings;

import liverefactoring.analysis.candidates.ExtractClassCandidate;
import liverefactoring.analysis.metrics.ClassMetrics;
import liverefactoring.core.LastRefactoring;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.java.PsiReferenceExpressionImpl;
import com.intellij.psi.util.PsiTreeUtil;
import liverefactoring.ui.JDeodorant.ClassWrapper;
import liverefactoring.ui.JDeodorant.ExtractClassRefactoring;
import liverefactoring.utils.RefactorUtils;
import liverefactoring.utils.UtilitiesOverall;
import liverefactoring.utils.importantValues.ThresholdsCandidates;
import liverefactoring.utils.importantValues.Values;

import java.util.*;
import java.util.stream.Stream;

public class ExtractClass{

    public RefactorUtils refactorUtils = new RefactorUtils();
    public UtilitiesOverall utilitiesOverall = new UtilitiesOverall();
    public PsiJavaFile sourceFile;
    public Editor editor;

    public ExtractClass(PsiJavaFile sourceFile, Editor editor) {
        this.sourceFile = sourceFile;
        this.editor = editor;
    }

    public ExtractClass() {
        Values.extractClass = new ArrayList<>();
    }

    public void extractClass(ExtractClassCandidate candidate, double severity, int index) {
        Values.isRefactoring = true;
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

                Values.lastRefactoring = new LastRefactoring(candidate.targetClass, elements, Values.currentFile, severity, index, "Extract Class");

                ExtractClassRefactoring extract = new ExtractClassRefactoring(candidate.file, candidate.targetClass,
                        fields, methods, new HashSet<>(), className);

                extract.apply();
                Values.isRefactoring = true;
                Values.allEC.add(candidate);
                System.out.println("============ Extract Class Done!!! ============");
            }
            else Values.isRefactoring = false;
        };

        Application application = ApplicationManager.getApplication();

        if (application.isDispatchThread()) {

            application.runWriteAction(runnable);

        } else {
            application.invokeLater(() -> application.runWriteAction(runnable));
        }
    }

    public void run() {
        List<ExtractClassCandidate> firstCandidates = new ArrayList<>();
        double percentageOriginalMethods = ThresholdsCandidates.minOrigMethodPercentageEC / 100.00;
        double lcom = ThresholdsCandidates.extractClassLackCohesion / 100.00;

        for (PsiClass c : sourceFile.getClasses()) {
            List<PsiMethod> methods = refactorUtils.getMethods(c);
            ClassMetrics classMetrics = Values.currentFile.getClassMetrics(c);
            System.out.println(Values.currentFile.classMetrics.size());
            for (ClassMetrics classMetric : Values.currentFile.classMetrics) {
                System.out.println(classMetric.className);
            }
            classMetrics.runMetricsExtractClass();
            boolean foreign = false;
            for (Integer checkForeignDatum : this.checkForeignData()) {
                if(checkForeignDatum >= ThresholdsCandidates.foreignData) {
                    foreign = true;
                    break;
                }
            }

            if(classMetrics.numMethods >= ThresholdsCandidates.numMethodsEC && (classMetrics.lackOfCohesion < lcom || foreign)){
                List<List<PsiMethod>> methodCombinations = utilitiesOverall.generateCombinations(methods);
                ArrayList<ArrayList<String>> methodsToBeExtractedNames = new ArrayList<>();
                ArrayList<ArrayList<PsiMethod>> methodsToBeExtracted = refactorUtils.getMethodsToBeExtracted(classMetrics.targetClass, classMetrics.weightMatrix);
                for (ArrayList<PsiMethod> listOfMethods : methodsToBeExtracted) {
                    ArrayList<String> names = new ArrayList<>();
                    for (PsiMethod listOfMethod : listOfMethods) {
                        names.add(listOfMethod.getName());
                    }

                    methodsToBeExtractedNames.add(names);
                }

                for (List<PsiMethod> ms : methodCombinations) {
                    ArrayList<PsiElement> entities = new ArrayList<>(ms);
                    ExtractClassCandidate newCandidate = new ExtractClassCandidate(sourceFile, c, entities, classMetrics, sourceFile.getPackageName());
                    ArrayList<String> candidateMethodNames = new ArrayList<>();
                    for (PsiMethod target : newCandidate.targetMethods) {
                        candidateMethodNames.add(target.getName());
                    }

                    for (ArrayList<String> mn : methodsToBeExtractedNames) {
                        if (utilitiesOverall.areArraysEqual(mn, candidateMethodNames)) {
                            firstCandidates.add(newCandidate);
                        }
                    }
                }
            }
        }

        List<ExtractClassCandidate> secondCandidates = new ArrayList<>();
        Values.extractClass = new ArrayList<>();
        for (ExtractClassCandidate aux : firstCandidates) {
            int numMethods = refactorUtils.getMethods(aux.targetClass).size();
            if (aux.targetMethods.size() >= ThresholdsCandidates.minNumExtractedMethods &&
                    aux.targetMethods.size() <= ((1-percentageOriginalMethods) * numMethods) && aux.isApplicable()) {
                boolean found = false;
                for (ExtractClassCandidate candidate : secondCandidates) {
                    if (candidate.toString().equals(aux.toString())) {
                        found = true;
                        break;
                    }
                }
                if (!found)
                    secondCandidates.add(aux);
            }
        }

        Stream<ExtractClassCandidate> streams =  secondCandidates.stream().sorted((a, b) -> {
            int value1 = b.targetMethods.size() - a.targetMethods.size();
            if(value1 == 0){
                int value2 = Double.compare(b.classMetrics.lackOfCohesion, a.classMetrics.lackOfCohesion);
                if(value2 == 0){
                    int value3 = Double.compare(b.classMetrics.complexity, a.classMetrics.complexity);
                    if(value3 == 0) return b.classMetrics.numLongMethods - a.classMetrics.numLongMethods;
                }
                return value2;
            }
            return value1;
        });


        Values.extractClass = new ArrayList<>();
        streams.forEachOrdered(Values.extractClass::add);

        System.out.println("Extract Class Candidates: " + Values.extractClass.size());
    }

    public ArrayList<Integer> checkForeignData(){
        ArrayList<Integer> foreignData = new ArrayList<>();
        List<PsiClass>allClasses = refactorUtils.getAllClasses(this.editor, this.sourceFile);

        for (PsiClass aClass : sourceFile.getClasses()) {
            if (!aClass.isEnum() && !aClass.isInterface()) {
                HashMap<PsiClass, Integer[]> auxClasses = new HashMap<>();
                for (PsiClass allClass : allClasses) {
                    auxClasses.put(allClass, new Integer[]{0,0});
                }
                for (PsiMethod method : aClass.getMethods()) {
                    if (!method.isConstructor()) {
                        for (PsiReferenceExpressionImpl psiReferenceExpression : PsiTreeUtil.findChildrenOfType(method, PsiReferenceExpressionImpl.class)) {
                            String qualifiedName = psiReferenceExpression.getQualifiedName().trim();
                            if (qualifiedName.contains(".")) {
                                if (qualifiedName.contains("this.")) {
                                    String entity = qualifiedName.split("\\.")[1].trim();
                                    if (!entity.contains("(")) {
                                        for (PsiField field : aClass.getFields()) {
                                            if (field.getName().equals(entity)) {
                                                auxClasses.get(aClass)[1] += 1;
                                            }
                                        }
                                    } else {
                                        for (PsiMethod aClassMethod : aClass.getMethods()) {
                                            if (!aClassMethod.isConstructor() && entity.contains(aClassMethod.getName())) {
                                                auxClasses.get(aClass)[0] += 1;
                                            }
                                        }
                                    }
                                }
                                else {
                                    LogicalPosition start = editor.offsetToLogicalPosition(psiReferenceExpression.getTextRange().getStartOffset());
                                    String fullEntity = sourceFile.getText().split("\n")[start.line].trim();

                                    if (qualifiedName.split("\\.").length == 2) {
                                        String var = qualifiedName.split("\\.")[0];
                                        String entityName = qualifiedName.split("\\.")[1];
                                        int index = fullEntity.indexOf(entityName);
                                        if (index != -1) {
                                            String entity = fullEntity.substring(index, fullEntity.length() - 1);

                                            for (PsiDeclarationStatement psiDeclarationStatement : PsiTreeUtil.findChildrenOfType(aClass, PsiDeclarationStatement.class)) {
                                                String varDecl = psiDeclarationStatement.getText().split("=")[0].trim();
                                                if (varDecl.split(" ")[1].equals(var)) {
                                                    for (PsiClass psiClass : allClasses) {
                                                        if (psiClass.getName().equals(varDecl.split(" ")[0])) {
                                                            if (!entity.contains("(")) {
                                                                for (PsiField field : psiClass.getFields()) {
                                                                    if (field.getName().equals(entity)) {
                                                                        auxClasses.get(psiClass)[1] += 1;
                                                                    }
                                                                }
                                                            } else {
                                                                for (PsiMethod aClassMethod : psiClass.getMethods()) {
                                                                    if (!aClassMethod.isConstructor()) {
                                                                        if (entity.equals(aClassMethod.getName().concat("()"))) {
                                                                            auxClasses.get(psiClass)[0] += 1;
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
                                    else{
                                        String _class = qualifiedName.split("\\.")[qualifiedName.split("\\.").length-1];
                                        for (PsiClass psiClass : allClasses) {
                                            if (psiClass.getName().equals(_class)) {
                                                if (!fullEntity.contains("(")) {
                                                    for (PsiField field : psiClass.getFields()) {
                                                        if (fullEntity.contains(_class + "." + field.getName())) {
                                                            auxClasses.get(psiClass)[1] += 1;
                                                        }
                                                    }
                                                }
                                                else {
                                                    for (PsiMethod aClassMethod : psiClass.getMethods()) {
                                                        if (!aClassMethod.isConstructor()) {
                                                            if (fullEntity.contains(_class + "." + aClassMethod.getName())) {
                                                                auxClasses.get(psiClass)[0] += 1;
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            else {
                                LogicalPosition start = editor.offsetToLogicalPosition(psiReferenceExpression.getTextRange().getStartOffset());
                                String fullEntity = this.sourceFile.getText().split("\n")[start.line].trim();
                                int index = fullEntity.indexOf(qualifiedName);
                                if(index != -1){
                                    String entity = fullEntity.substring(index, fullEntity.length()-1);
                                    if(!entity.contains("(")){
                                        for (PsiField field : aClass.getFields()) {
                                            if(field.getName().equals(qualifiedName)){
                                                auxClasses.get(aClass)[1] += 1;
                                            }
                                        }
                                    }
                                    else{
                                        for (PsiMethod aClassMethod : aClass.getMethods()) {
                                            if(!aClassMethod.isConstructor() && qualifiedName.contains(aClassMethod.getName().concat("()"))){
                                                auxClasses.get(aClass)[0] += 1;
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        for (PsiClass psiClass : auxClasses.keySet()) {
                            if(auxClasses.get(psiClass)[0] + auxClasses.get(psiClass)[1] >= auxClasses.get(aClass)[0] + auxClasses.get(aClass)[1] && !psiClass.getName().equals(aClass.getName())){
                                foreignData.add(auxClasses.get(psiClass)[0] + auxClasses.get(psiClass)[1]);
                            }
                        }
                    }
                }
            }
        }

        return foreignData;
    }

    public PsiElement[] getElements(ExtractClassCandidate candidate) {

        ArrayList<PsiElement> elements = new ArrayList<>(candidate.targetEntities);

        PsiElement[] psiElements = new PsiElement[elements.size()];
        for (int i = 0; i < psiElements.length; i++)
            psiElements[i] = elements.get(i);

        return psiElements;
    }
}

