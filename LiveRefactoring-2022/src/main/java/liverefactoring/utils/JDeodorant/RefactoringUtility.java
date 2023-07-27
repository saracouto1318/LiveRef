package liverefactoring.utils.JDeodorant;

import com.intellij.psi.*;

public class RefactoringUtility {
    public static PsiClassType generateTypeFromTypeBinding(PsiClass psiClass, PsiElementFactory factory) {
        return factory.createType(psiClass);
    }

    public static PsiField findFieldDeclaration(AbstractVariable variable, PsiClass typeDeclaration) {
        for (PsiField fieldDeclaration : typeDeclaration.getFields()) {
            if (variable.getOrigin().equals(fieldDeclaration)) {
                return fieldDeclaration;
            }
        }
        //fragment was not found in typeDeclaration
        PsiClass superclassType = typeDeclaration.getSuperClass();
        for (PsiField fieldDeclaration : superclassType.getFields()) {
            if (variable.getOrigin().equals(fieldDeclaration)) {
                return fieldDeclaration;
            }
        }

        for (PsiClass allInnerClass : superclassType.getAllInnerClasses()) {
            for (PsiField fieldDeclaration : allInnerClass.getFields()) {
                if (variable.getOrigin().equals(fieldDeclaration)) {
                    return fieldDeclaration;
                }
            }
        }

        return null;
    }

    public static PsiClass findDeclaringTypeDeclaration(PsiField variableBinding, PsiClass typeDeclaration) {
        if (variableBinding == null) {
            return null;
        }

        if (typeDeclaration.equals(variableBinding.getContainingClass())) {
            return typeDeclaration;
        }
        //fragment was not found in typeDeclaration
        PsiClass superclassType = typeDeclaration.getSuperClass();
        if (superclassType.equals(variableBinding.getContainingClass()))
            return superclassType;

        for (PsiClass allInnerClass : superclassType.getAllInnerClasses()) {
            if (allInnerClass.equals(variableBinding.getContainingClass()))
                return allInnerClass;
        }

        return null;
    }

    public static PsiClass findDeclaringTypeDeclaration(PsiMethod methodBinding, PsiClass typeDeclaration) {
        if (typeDeclaration.equals(methodBinding.getContainingClass())) {
            return typeDeclaration;
        }
        //method was not found in typeDeclaration
        PsiClass superclassType = typeDeclaration.getSuperClass();
        if (superclassType.equals(methodBinding.getContainingClass()))
            return superclassType;

        for (PsiClass allInnerClass : superclassType.getAllInnerClasses()) {
            if (allInnerClass.equals(methodBinding.getContainingClass()))
                return allInnerClass;
        }
        return null;
    }

    public static boolean needsQualifier(PsiReferenceExpression simpleName) {
        return (!(simpleName instanceof PsiQualifiedExpression) || isArrayLengthQualifiedName(simpleName)) &&
                !isEnumConstantInSwitchCaseExpression(simpleName);
    }

    private static boolean isArrayLengthQualifiedName(PsiReferenceExpression simpleName) {
        return simpleName instanceof PsiQualifiedExpression && ((PsiQualifiedExpression) simpleName).getQualifier().getText().equals("length");
    }

    private static boolean isEnumConstantInSwitchCaseExpression(PsiReferenceExpression simpleName) {
        PsiElement binding = simpleName.resolve();
        if (binding instanceof PsiVariable) {
            PsiVariable variableBinding = (PsiVariable) binding;
            if (variableBinding instanceof PsiField) {
                return simpleName instanceof PsiSwitchLabeledRuleStatement && ((PsiField) variableBinding).getContainingClass().isEnum();
            }
        }
        return false;
    }
}
