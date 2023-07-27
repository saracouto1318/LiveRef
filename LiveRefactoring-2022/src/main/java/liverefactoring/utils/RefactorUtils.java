package liverefactoring.utils;

import liverefactoring.analysis.candidates.ExtractMethodCandidate;
import liverefactoring.analysis.metrics.MethodMetrics;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import liverefactoring.utils.importantValues.Values;
import liverefactoring.utils.importantValues.ThresholdsCandidates;

import java.util.*;

public class RefactorUtils {
    public RefactorUtils() {
    }

    public ArrayList<String> getAllClasses() {
        ArrayList<String> javaFiles = new ArrayList<>();
        Project project = Values.editor.getProject();
        GlobalSearchScope searchScope = GlobalSearchScope.allScope(project);
        Collection<VirtualFile> filePaths = FileTypeIndex.getFiles(com.intellij.ide.highlighter.JavaFileType.INSTANCE, searchScope);
        for (VirtualFile filePath : filePaths) {
            if(!filePath.getPath().contains("/resources/") && !filePath.getPath().contains("/test/") && filePath.getPath().contains("/src/") ) {
                PsiFile psiFile = PsiManager.getInstance(project).findFile(filePath);
                if(psiFile instanceof PsiJavaFile){
                    for (PsiClass aClass : ((PsiJavaFile) psiFile).getClasses()) {
                        javaFiles.add(aClass.getName());
                    }
                }
            }
        }
        return javaFiles;
    }

    public ArrayList<PsiJavaFile> getAllClassesFiles() {
        ArrayList<PsiJavaFile> javaFiles = new ArrayList<>();
        Project project = Values.editor.getProject();
        GlobalSearchScope searchScope = GlobalSearchScope.allScope(project);
        Collection<VirtualFile> filePaths = FileTypeIndex.getFiles(com.intellij.ide.highlighter.JavaFileType.INSTANCE, searchScope);
        for (VirtualFile filePath : filePaths) {
            if(!filePath.getPath().contains("/resources/") && !filePath.getPath().contains("/test/") && filePath.getPath().contains("/src/") ) {
                PsiFile psiFile = PsiManager.getInstance(project).findFile(filePath);
                if(psiFile instanceof PsiJavaFile){
                    javaFiles.add((PsiJavaFile) psiFile);
                }
            }
        }

        return javaFiles;
    }

    public List<PsiField> getFieldsInitializedInConstructor(PsiMethod[] constructors, PsiClass _class) {
        List<PsiField> parameters = new ArrayList<>();
        boolean foundPublicOrPrivateKeyword = false;

        for (PsiField fieldDeclaration : _class.getFields()) {
            if(fieldDeclaration.getModifierList() != null){
                if (fieldDeclaration.getModifierList().hasModifierProperty(PsiModifier.PUBLIC) ||
                        fieldDeclaration.getModifierList().hasModifierProperty(PsiModifier.PRIVATE) ||
                        fieldDeclaration.getModifierList().hasModifierProperty(PsiModifier.PROTECTED))
                    foundPublicOrPrivateKeyword = true;
            }
            for (PsiMethod constructor : constructors) {
                if(constructor.getBody() != null) {
                    if (constructor.getBody().toString().contains(fieldDeclaration.getName()) && foundPublicOrPrivateKeyword) {
                        for (PsiAssignmentExpression psiAssignmentExpression : PsiTreeUtil.findChildrenOfType(constructor, PsiAssignmentExpression.class)) {
                            if (psiAssignmentExpression.getLExpression().getText().contains(fieldDeclaration.getName())) {
                                if (!parameters.contains(fieldDeclaration)) {
                                    parameters.add(fieldDeclaration);
                                    foundPublicOrPrivateKeyword = false;
                                }
                            }
                        }
                    }
                }
            }
        }

        return parameters;
    }

    public ArrayList<PsiMethodCallExpression> getCallExpressions(PsiMethod method) {
        ArrayList<PsiMethodCallExpression> calls = new ArrayList<>();
        HashMap<String, Integer> declarations = new HashMap<>();
        for (PsiStatement psiStatement : PsiTreeUtil.findChildrenOfType(method, PsiDeclarationStatement.class)) {
            declarations.put(psiStatement.getText(), Values.editor.offsetToLogicalPosition(psiStatement.getTextRange().getStartOffset()).line);
        }

        for (PsiMethodCallExpression psiCallExpression : PsiTreeUtil.findChildrenOfType(method, PsiMethodCallExpression.class)) {
            if (!psiCallExpression.getText().contains("return ")) {
                boolean found = false;
                for (PsiDeclarationStatement psiDeclarationStatement : PsiTreeUtil.findChildrenOfType(method, PsiDeclarationStatement.class)) {
                    for (PsiMethodCallExpression callExpression : PsiTreeUtil.findChildrenOfType(psiDeclarationStatement, PsiMethodCallExpression.class)) {
                        if (callExpression.getText().equals(psiCallExpression.getText()) || psiCallExpression.getParent().getText().contains("=")) {
                            found = true;
                            break;
                        }
                    }

                    if (found)
                        break;
                }

                if (!found) {
                    boolean isDeclarationStatement = false;
                    for (String s : declarations.keySet()) {
                        if(s.contains(psiCallExpression.getText())){
                            int line = Values.editor.offsetToLogicalPosition(psiCallExpression.getTextRange().getStartOffset()).line;
                            if(line == declarations.get(s))
                                isDeclarationStatement = true;
                        }
                    }
                    if(!isDeclarationStatement)
                        calls.add(psiCallExpression);
                }
            }
        }

        return calls;
    }

    public ArrayList<PsiMethod> getMethods(PsiClass _class) {
        ArrayList<PsiMethod> methods = new ArrayList<>();
        for (PsiMethod method : _class.getMethods()) {
            if(!method.isConstructor()){
                methods.add(method);
            }
        }

        return methods;
    }

    public boolean doesClassExtendOrImplement(PsiClass targetClass) {
        return targetClass.getImplementsListTypes().length > 0 || targetClass.getExtendsListTypes().length > 0;
    }

    public ArrayList<PsiStatement> getAllStatements(PsiMethod method) {
        ArrayList<PsiStatement> statements = new ArrayList<>();
        if(method != null && method.getBody() != null && method.getContainingClass() != null)
            for (PsiStatement psiStatement : PsiTreeUtil.findChildrenOfType(method, PsiStatement.class)) {
                statements.add(psiStatement);
            }
        return statements;
    }

    public boolean isCandidateOnlyVariableStatements(ExtractMethodCandidate candidate) {
        for (PsiStatement c : candidate.nodes) {
            for (PsiStatement psiStatement : PsiTreeUtil.findChildrenOfType(c, PsiStatement.class)) {
                if (!(psiStatement instanceof PsiDeclarationStatement)) {
                    return false;
                }
            }
        }

        return true;
    }

    public ArrayList<ArrayList<PsiMethod>> getMethodsToBeExtracted(PsiClass targetClass, ArrayList<ArrayList<Double>> weightMatrix) {
        ArrayList<ArrayList<PsiMethod>> methodsToBeExtracted = new ArrayList<>();
        List<PsiMethod> methods = this.getMethods(targetClass);

        for (ArrayList<Double> matrix : weightMatrix) {
            ArrayList<PsiMethod> methodsInRow = new ArrayList<>();
            for (int j = 0; j < matrix.size(); j++) {
                if(j < methods.size()) {
                    if (matrix.get(j) >= ThresholdsCandidates.lowerValue && matrix.get(j) <= ThresholdsCandidates.upperValue) {
                        methodsInRow.add(methods.get(j));
                    }
                }
            }

            if (methodsInRow.size() > 0) {
                methodsToBeExtracted.add(methodsInRow);
            }
        }

        return methodsToBeExtracted;
    }

    public boolean containsBreakOrContinueOrReturn(PsiStatement statement) {
        return PsiTreeUtil.findChildrenOfType(statement, PsiReturnStatement.class).size() > 0 ||
                PsiTreeUtil.findChildrenOfType(statement, PsiBreakStatement.class).size() > 0 ||
                PsiTreeUtil.findChildrenOfType(statement, PsiContinueStatement.class).size() > 0;
    }

    public MethodMetrics getMetricsByMethodName(String name, ArrayList<MethodMetrics> metrics) {
        for (MethodMetrics metric : metrics) {
            if (metric.methodName.equals(name))
                return metric;
        }

        return null;
    }

    public List<PsiClass> getAllClasses(Editor editor, PsiJavaFile sourceFile){
        List<PsiClass> allClasses = new ArrayList<>();
        Project project = Values.editor.getProject();
        GlobalSearchScope searchScope = GlobalSearchScope.allScope(project);
        Collection<VirtualFile> filePaths = FileTypeIndex.getFiles(com.intellij.ide.highlighter.JavaFileType.INSTANCE, searchScope);
        for (VirtualFile filePath : filePaths) {
            if(!filePath.getPath().contains("/resources/") && !filePath.getPath().contains("/test/") && filePath.getPath().contains("/src/") ) {
                PsiFile psiFile = PsiManager.getInstance(project).findFile(filePath);
                int pos = psiFile.getName().lastIndexOf(".");
                String fileName = psiFile.getName().substring(0,pos);
                if(sourceFile.getText().contains(fileName)){
                    if (psiFile instanceof PsiJavaFile) {
                        PsiClass[] classes = ((PsiJavaFile)psiFile).getClasses();
                        Collections.addAll(allClasses, classes);
                    }
                }
            }
        }
        return allClasses;
    }
}
