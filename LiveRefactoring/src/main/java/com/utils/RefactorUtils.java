package com.utils;

import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.metrics.MethodMetrics;
import com.refactorings.candidates.ExtractMethodCandidate;
import org.mozilla.javascript.ast.BreakStatement;
import org.mozilla.javascript.ast.ContinueStatement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.utils.ThresholdsCandidates.lowerValue;
import static com.utils.ThresholdsCandidates.upperValue;

public class RefactorUtils {
    public RefactorUtils() {
    }

    public List<PsiField> getFieldsInitializedInConstructor(PsiMethod[] constructors, PsiClass _class) {
        List<PsiField> parameters = new ArrayList<>();
        boolean foundPublicOrPrivateKeyword = false;

        for (PsiField fieldDeclaration : _class.getFields()) {
            if (Objects.requireNonNull(fieldDeclaration.getModifierList()).hasModifierProperty(PsiModifier.PUBLIC) ||
                    fieldDeclaration.getModifierList().hasModifierProperty(PsiModifier.PRIVATE) ||
                    fieldDeclaration.getModifierList().hasModifierProperty(PsiModifier.PROTECTED))
                foundPublicOrPrivateKeyword = true;
            for (PsiMethod constructor : constructors) {
                if (Objects.requireNonNull(constructor.getBody()).toString().contains(fieldDeclaration.getName()) && foundPublicOrPrivateKeyword) {
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

        return parameters;
    }

    public ArrayList<PsiMethodCallExpression> getCallExpressions(PsiMethod method) {
        ArrayList<PsiMethodCallExpression> calls = new ArrayList<>();

        for (PsiMethodCallExpression psiCallExpression : PsiTreeUtil.findChildrenOfType(method, PsiMethodCallExpression.class)) {
            if (!psiCallExpression.getText().contains("return ")) {
                boolean found = false;
                for (PsiBinaryExpression psiBinaryExpression : PsiTreeUtil.findChildrenOfType(method, PsiBinaryExpression.class)) {
                    for (PsiMethodCallExpression callExpression : PsiTreeUtil.findChildrenOfType(psiBinaryExpression, PsiMethodCallExpression.class)) {
                        if (callExpression.getText().equals(psiCallExpression.getText())) {
                            found = true;
                            break;
                        }
                    }

                    if (found)
                        break;
                }

                if (!found)
                    calls.add(psiCallExpression);
            }
        }

        return calls;
    }

    public List<PsiMethod> getMethods(PsiClass _class) {
        PsiMethod[] auxMethods = _class.getMethods();
        PsiMethod[] constructors = _class.getConstructors();
        List<PsiMethod> methods = new ArrayList<>();

        for (PsiMethod auxMethod : auxMethods) {
            boolean found = false;
            for (PsiMethod constructor : constructors) {
                if (auxMethod.getName().equals(constructor.getName())) {
                    found = true;
                }
            }
            if (!found)
                methods.add(auxMethod);
        }

        return methods;
    }

    public boolean doesClassExtendOrImplement(PsiClass targetClass) {
        return targetClass.getImplementsListTypes().length > 0 || targetClass.getExtendsListTypes().length > 0;
    }

    public ArrayList<PsiStatement> getAllStatements(PsiMethod method) {
        ArrayList<PsiStatement> statements = new ArrayList<>();
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
                if (matrix.get(j) >= lowerValue && matrix.get(j) <= upperValue) {
                    methodsInRow.add(methods.get(j));
                }
            }

            if (methodsInRow.size() > 0) {
                methodsToBeExtracted.add(methodsInRow);
            }
        }

        return methodsToBeExtracted;
    }

    public boolean containsBreakOrContinueOrReturn(PsiStatement statement) {
        boolean found = false;
        for (PsiElement child : statement.getChildren()) {
            if(child instanceof PsiReturnStatement || child instanceof BreakStatement ||
                    child instanceof ContinueStatement)
                found = true;
        }
        return found;
    }

    public MethodMetrics getMetricsByMethodName(String name, ArrayList<MethodMetrics> metrics) {
        for (MethodMetrics metric : metrics) {
            if (metric.methodName.equals(name))
                return metric;
        }

        return null;
    }
}
