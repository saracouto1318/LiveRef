package com.refactorings.candidates;

import com.intellij.lang.jvm.JvmModifier;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.metrics.ClassMetrics;
import com.utils.RefactorUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class ExtractClassCandidate {
    public List<PsiMethod> targetMethods = new ArrayList<>();
    public List<PsiField> targetAttributes = new ArrayList<>();
    public PsiClass targetClass;
    public List<PsiElement> targetEntities;
    public ClassMetrics classMetrics;
    public int avgMethods = 0;
    public String packageName;
    public RefactorUtils utils = new RefactorUtils();
    public PsiJavaFile file;

    public ExtractClassCandidate(PsiJavaFile file, PsiClass targetClass, List<PsiElement> targetEntities, ClassMetrics classMetrics, String packageName) {
        this.targetClass = targetClass;
        this.targetEntities = targetEntities;
        this.classMetrics = classMetrics;
        this.packageName = packageName;
        this.file = file;

        this.setTarget();
    }

    public boolean isApplicable() {
        int methodCounter = 0;
        for (PsiElement entity : targetEntities) {
            if (entity instanceof PsiMethod) {
                PsiMethod method = (PsiMethod) entity;
                methodCounter++;
                if (this.isSynchronized(method) || this.containsSuperMethodInvocation(method) ||
                        this.overridesMethod(method) || method.hasModifier(JvmModifier.ABSTRACT) || this.containsFieldAccessOfEnclosingClass(method) ||
                        this.isReadObject(method) || this.isWriteObject(method))
                    return false;
            } else if (entity instanceof PsiField) {
                PsiField field = (PsiField) entity;
                if (Objects.requireNonNull(field.getModifierList()).hasModifierProperty(PsiModifier.PROTECTED) || field.getModifierList().hasModifierProperty(PsiModifier.PRIVATE))
                    return false;
            }
        }

        return targetEntities.size() > 2 && methodCounter != 0;
    }

    private boolean isReadObject(PsiMethod method) {
        return method.getText().contains("readObject") && method.getParameters().length == 1 &&
                method.getParameters()[0].toString().contains("ObjectInputStream");
    }

    private boolean isWriteObject(PsiMethod method) {
        return method.getText().contains("writeObject") && method.getParameters().length == 1
                && method.getParameters()[0].toString().contains("ObjectOutputStream");
    }

    private boolean overridesMethod(PsiMethod method) {
        return method.getText().contains("@Override");
    }

    private boolean containsSuperMethodInvocation(PsiMethod method) {
        return Objects.requireNonNull(method.getBody()).getText().contains("super.") || method.getBody().getText().contains("super(");
    }

    private boolean validRemainingMethodsInSourceClass() {
        for (PsiMethod sourceMethod : this.utils.getMethods(this.targetClass)) {
            if (!this.targetMethods.contains(sourceMethod)) {
                if (!sourceMethod.hasModifier(JvmModifier.STATIC) && !sourceMethod.hasModifier(JvmModifier.ABSTRACT) &&
                        !isGetter(sourceMethod) && !isSetter(sourceMethod) &&
                        !isReadObject(sourceMethod) && !isWriteObject(sourceMethod) && !isEquals(sourceMethod) &&
                        !isHashCode(sourceMethod) && !isClone(sourceMethod) && !isCompareTo(sourceMethod) &&
                        !isToString(sourceMethod)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean containsFieldAccessOfEnclosingClass(PsiMethod method) {
        if (Objects.requireNonNull(method.getBody()).getText().length() == 0) {
            return false;
        }

        Collection<PsiExpression> fieldAccesses = PsiTreeUtil.findChildrenOfType(method, PsiExpression.class);
        for (PsiExpression expression : fieldAccesses) {
            if (expression instanceof PsiReferenceExpression) {
                PsiReferenceExpression fieldReference = (PsiReferenceExpression) expression;
                Collection<PsiElement> psiElements = PsiTreeUtil.findChildrenOfType(fieldReference, PsiThisExpression.class);

                for (PsiElement thisExpressionElement : psiElements) {
                    PsiThisExpression thisExpression = (PsiThisExpression) thisExpressionElement;
                    if (thisExpression.getQualifier() != null) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isGetter(PsiMethod method) {
        for (String s : method.getText().split("\n")) {
            if (s.contains("get")) {
                for (PsiField field : this.targetClass.getFields()) {
                    if (s.contains(field.getName()))
                        return true;
                }
            }
        }
        return false;
    }

    private boolean isSetter(PsiMethod method) {
        for (String s : method.getText().split("\n")) {
            if (s.contains("set")) {
                for (PsiField field : this.targetClass.getFields()) {
                    if (s.contains(field.getName()))
                        return true;
                }
            }
        }
        return false;
    }

    private boolean isSynchronized(PsiMethod method) {
        return method.getText().split("\n")[0].contains("synchronized ");
    }

    private boolean isEquals(PsiMethod methodObject) {
        PsiParameter[] parameterTypeList = methodObject.getParameterList().getParameters();
        return methodObject.getName().equals("equals") && Objects.equals(methodObject.getReturnType(), PsiType.BOOLEAN) &&
                parameterTypeList.length == 1 && parameterTypeList[0].getType().equals("java.lang.Object");
    }

    private boolean isHashCode(PsiMethod methodObject) {
        PsiParameter[] parameterTypeList = methodObject.getParameterList().getParameters();
        return methodObject.getName().equals("hashCode") && methodObject.getReturnType().equals(PsiType.INT)
                && parameterTypeList.length == 0;
    }

    private boolean isToString(PsiMethod methodObject) {
        PsiParameter[] parameterTypeList = methodObject.getParameterList().getParameters();
        return methodObject.getName().equals("toString") && methodObject.getReturnType().equals("java.lang.String")
                && parameterTypeList.length == 0;
    }

    private boolean isClone(PsiMethod methodObject) {
        PsiParameter[] parameterTypeList = methodObject.getParameterList().getParameters();
        return methodObject.getName().equals("clone") && methodObject.getReturnType().equals("java.lang.Object")
                && parameterTypeList.length == 0;
    }

    private boolean isCompareTo(PsiMethod methodObject) {
        PsiParameter[] parameterTypeList = methodObject.getParameterList().getParameters();
        return methodObject.getName().equals("compareTo") && Objects.equals(methodObject.getReturnType(), PsiType.INT)
                && parameterTypeList.length == 1;
    }

    public void setTarget() {
        this.targetEntities.forEach(entity -> {
            if (entity instanceof PsiMethod) {
                PsiMethod method = (PsiMethod) entity;
                this.targetMethods.add(method);
            } else if (entity instanceof PsiField) {
                PsiField attribute = (PsiField) entity;
                this.targetAttributes.add(attribute);
            }
        });

        for (PsiMethod targetMethod : this.targetMethods) {
            this.avgMethods += Objects.requireNonNull(targetMethod.getBody()).getStatementCount();
        }

        if (this.targetMethods.size() > 0)
            this.avgMethods = this.avgMethods / this.targetMethods.size();
        else
            this.avgMethods = 0;
    }

    public String toString() {
        StringBuilder info = new StringBuilder("Extract Class:\n\nClass: " + this.targetClass.getName());

        if (this.targetEntities.size() > 0) {
            info.append("\n----------- Entities -----------\n");
            for (Object entity : this.targetEntities) {
                if (entity instanceof PsiMethod) {
                    info.append("   Method: ").append(((PsiMethod) entity).getName());
                } else if (entity instanceof PsiField) {
                    info.append("   Field: ").append(((PsiField) entity).getName());
                }
            }
        }

        return info.toString();
    }
}
