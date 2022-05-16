package com.utils;

import com.intellij.lang.jvm.JvmModifier;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.metrics.ClassMetrics;
import com.metrics.FileMetrics;
import com.metrics.MethodMetrics;

import java.util.*;

public final class MetricsUtils {

    public RefactorUtils refactorUtils = new RefactorUtils();

    public MetricsUtils() {

    }

    public int increasesCyclomaticComplexity(PsiMethod method) {
        int counter = 0;

        if (method != null) {
            for (PsiSwitchStatement psiSwitchStatement : PsiTreeUtil.findChildrenOfType(method, PsiSwitchStatement.class)) {
                if (Objects.requireNonNull(psiSwitchStatement.getBody()).getStatements().length > 0)
                    counter++;
            }
            counter += PsiTreeUtil.findChildrenOfType(method, PsiDoWhileStatement.class).size();
            counter += PsiTreeUtil.findChildrenOfType(method, PsiForeachStatement.class).size();
            counter += PsiTreeUtil.findChildrenOfType(method, PsiForStatement.class).size();
            counter += PsiTreeUtil.findChildrenOfType(method, PsiWhileStatement.class).size();
            counter += PsiTreeUtil.findChildrenOfType(method, PsiIfStatement.class).size();
            counter += PsiTreeUtil.findChildrenOfType(method, PsiConditionalExpression.class).size();

            for (PsiBinaryExpression psiBinaryExpression : PsiTreeUtil.findChildrenOfType(method, PsiBinaryExpression.class)) {
                if (psiBinaryExpression.getText().contains("&&"))
                    counter++;
            }

        }

        return counter;
    }

    public int increasesCyclomaticComplexity(PsiStatement statement) {
        int counter = 0;

        for (PsiSwitchStatement psiSwitchStatement : PsiTreeUtil.findChildrenOfType(statement, PsiSwitchStatement.class)) {
            if (Objects.requireNonNull(psiSwitchStatement.getBody()).getStatements().length > 0)
                counter++;
        }
        counter += PsiTreeUtil.findChildrenOfType(statement, PsiDoWhileStatement.class).size();
        counter += PsiTreeUtil.findChildrenOfType(statement, PsiForeachStatement.class).size();
        counter += PsiTreeUtil.findChildrenOfType(statement, PsiForStatement.class).size();
        counter += PsiTreeUtil.findChildrenOfType(statement, PsiWhileStatement.class).size();
        counter += PsiTreeUtil.findChildrenOfType(statement, PsiIfStatement.class).size();
        counter += PsiTreeUtil.findChildrenOfType(statement, PsiConditionalExpression.class).size();

        for (PsiBinaryExpression psiBinaryExpression : PsiTreeUtil.findChildrenOfType(statement, PsiBinaryExpression.class)) {
            if (psiBinaryExpression.getText().contains("&&"))
                counter++;
        }

        return counter;
    }

    public double initializeLCOM(PsiClass _class) {
        List<PsiMethod> methods = refactorUtils.getMethods(_class);
        PsiField[] variables = _class.getFields();
        PsiMethod[] constructors = _class.getConstructors();
        List<PsiField> constructorVariables = new ArrayList<>();

        if(_class.isEnum() || _class.isInterface() || _class.isAnnotationType() || _class.hasModifier(JvmModifier.ABSTRACT)){
            return 0.0;
        }

        if (constructors.length > 0) {
            constructorVariables = refactorUtils.getFieldsInitializedInConstructor(constructors, _class);
        }

        int methodCounter = methods.size() + constructors.length;
        int variableCounter = variables.length + constructorVariables.size();
        int methodsWhichAccessVariables = 0;

        for (PsiField v : variables) {
            int numberOfMethodsAccessingVariable = 0;
            for (PsiMethod c : constructors) {
                if (isFieldUsedOnNode(v, c)) {
                    numberOfMethodsAccessingVariable++;
                }
            }
            for (PsiMethod m : methods) {
                if (isFieldUsedOnMethod(v, m)) {
                    numberOfMethodsAccessingVariable++;
                }
            }
            methodsWhichAccessVariables += numberOfMethodsAccessingVariable;
        }

        for (PsiField cv : constructorVariables) {
            int numberOfMethodsAccessingVariable = 0;
            for (PsiMethod c : constructors) {
                if (isIdentifierUsedOnNode(cv.getName(), c)) {
                    numberOfMethodsAccessingVariable++;
                }
            }
            for (PsiMethod m : methods) {
                if (isIdentifierUsedOnMethod(cv.getName(), m)) {
                    numberOfMethodsAccessingVariable++;
                }
            }
            methodsWhichAccessVariables += numberOfMethodsAccessingVariable;
        }

        if (methodCounter <= 1 || variableCounter == 0) {
            return 0;
        } else {
            double result = (double)((1 / variableCounter) * methodsWhichAccessVariables - methodCounter) / (1 - methodCounter);
            return result > 1 ? 1: result;
        }
    }

    public void initializeHalsteadMetrics(MethodMetrics metrics, PsiMethod method) {
        Set<String> uniqueOperators = new HashSet<>();
        Set<String> uniqueOperands = new HashSet<>();
        int operators = 0;
        int operands = 0;

        for (PsiLiteralExpression psiLiteralExpression : PsiTreeUtil.findChildrenOfType(method, PsiLiteralExpression.class)) {
            operands++;
            uniqueOperands.add(psiLiteralExpression.getText());
        }

        for (PsiIdentifier psiIdentifier : PsiTreeUtil.findChildrenOfType(method, PsiIdentifier.class)) {
            operands++;
            uniqueOperands.add(psiIdentifier.getText());
        }

        for (PsiStatement psiStatement : PsiTreeUtil.findChildrenOfType(method, PsiStatement.class)) {
            if (getOperator(psiStatement.getText()) != null) {
                operators++;
                uniqueOperators.add(getOperator(psiStatement.getText()));
            }
        }

        metrics.halsteadLength = operands + operators;
        metrics.halsteadVocabulary = uniqueOperands.size() + uniqueOperators.size();
        metrics.halsteadVolume = metrics.halsteadLength * Math.log(metrics.halsteadVocabulary);
        metrics.halsteadDifficulty = (uniqueOperators.size() / (double) 2) * (operands / (double) uniqueOperands.size());
        metrics.halsteadEffort = metrics.halsteadVolume * metrics.halsteadDifficulty;
        metrics.halsteadLevel = 1 / metrics.halsteadDifficulty;
        metrics.halsteadTime = metrics.halsteadEffort / (double) 18;
        metrics.halsteadBugsDelivered = Math.pow(metrics.halsteadEffort, 2 / 3) / (double) 3000;
        metrics.halsteadMaintainability = Math.max(0.0, (171 - 5.2 * Math.log(metrics.halsteadVolume) - 0.23 * metrics.complexityOfMethod - 16.2 * Math.log(metrics.numberLinesOfCode)) * 100 / (double) 171);
    }

    private boolean isFieldUsedOnNode(PsiField field, PsiMethod constructor) {
        String fieldString = field.getName();
        String constructorString = Objects.requireNonNull(constructor.getBody()).getText();

        return constructorString.contains(fieldString);
    }

    public boolean isFieldUsedOnMethod(PsiField field, PsiMethod method) {
        String fieldString = field.getName();
        String methodString = Objects.requireNonNull(method.getBody()).getText();

        return methodString.contains(fieldString);
    }

    public boolean isIdentifierUsedOnNode(String variable, PsiMethod constructor) {
        String constructorString = Objects.requireNonNull(constructor.getBody()).getText();

        return constructorString.contains(variable);
    }

    public boolean isIdentifierUsedOnMethod(String variable, PsiMethod method) {
        String methodString = Objects.requireNonNull(method.getBody()).getText();

        return methodString.contains(variable);
    }

    private boolean doesMethodCallAnotherMethod(PsiMethod callerMethod, PsiMethod calledMethod) {
        String calledMethodString = calledMethod.getName();
        String callerMethodString = Objects.requireNonNull(callerMethod.getBody()).getText();

        return callerMethodString.contains(calledMethodString);
    }

    public int amountOfMethodCalls(PsiMethod callerMethod, PsiMethod calledMethod) {
        int count = 0;

        for (PsiMethodCallExpression psiMethodCallExpression : PsiTreeUtil.findChildrenOfType(callerMethod, PsiMethodCallExpression.class)) {
            if (psiMethodCallExpression.getText().contains(calledMethod.getName())) {
                count++;
            }
        }

        return count;
    }

    public int amountOfDictionaryOccurrencesInMethod(String vocabulary, PsiMethod method) {
        int count = 0;

        for (PsiElement element : PsiTreeUtil.findChildrenOfType(method, PsiElement.class)) {
            if (method != null && Objects.requireNonNull(method.getBody()).getText().contains(vocabulary))
                count++;
        }

        return count;
    }

    private boolean areMethodMetricsEqual(MethodMetrics[] metrics1, MethodMetrics[] metrics2) {
        if (metrics1.length != metrics2.length) {
            return false;
        } else {
            for (int i = 0; i < metrics1.length; i++)
                if (!metrics1[i].equals(metrics2[i]))
                    return false;
        }

        return true;
    }

    private ArrayList<Integer> getAllMethodsHalsteadLength(MethodMetrics[] methodMetrics) {
        ArrayList<Integer> length = new ArrayList<>();
        for (MethodMetrics mm : methodMetrics) {
            length.add(mm.halsteadLength);
        }

        return length;
    }

    private ArrayList<Integer> getAllMethodsHalsteadVocabulary(MethodMetrics[] methodMetrics) {
        ArrayList<Integer> vocabulary = new ArrayList<>();
        for (MethodMetrics mm : methodMetrics) {
            vocabulary.add(mm.halsteadVocabulary);
        }

        return vocabulary;
    }

    private ArrayList<Double> getAllMethodsHalsteadVolume(MethodMetrics[] methodMetrics) {
        ArrayList<Double> volume = new ArrayList<>();
        for (MethodMetrics mm : methodMetrics) {
            volume.add(mm.halsteadVolume);
        }

        return volume;
    }

    private ArrayList<Double> getAllMethodsHalsteadDifficulty(MethodMetrics[] methodMetrics) {
        ArrayList<Double> difficulty = new ArrayList<>();
        for (MethodMetrics mm : methodMetrics) {
            difficulty.add(mm.halsteadDifficulty);
        }

        return difficulty;
    }

    private ArrayList<Double> getAllMethodsHalsteadEffort(MethodMetrics[] methodMetrics) {
        ArrayList<Double> effort = new ArrayList<>();
        for (MethodMetrics mm : methodMetrics) {
            effort.add(mm.halsteadEffort);
        }

        return effort;
    }

    private ArrayList<Double> getAllMethodsHalsteadLevel(MethodMetrics[] methodMetrics) {
        ArrayList<Double> level = new ArrayList<>();
        for (MethodMetrics mm : methodMetrics) {
            level.add(mm.halsteadLevel);
        }

        return level;
    }

    private ArrayList<Double> getAllMethodsHalsteadTime(MethodMetrics[] methodMetrics) {
        ArrayList<Double> time = new ArrayList<>();
        for (MethodMetrics mm : methodMetrics) {
            time.add(mm.halsteadTime);
        }

        return time;
    }

    private ArrayList<Double> getAllMethodsHalsteadBugsDelivered(MethodMetrics[] methodMetrics) {
        ArrayList<Double> bugs = new ArrayList<>();
        for (MethodMetrics mm : methodMetrics) {
            bugs.add(mm.halsteadBugsDelivered);
        }

        return bugs;
    }

    private ArrayList<Double> getAllMethodsHalsteadMaintainability(MethodMetrics[] methodMetrics) {
        ArrayList<Double> maintainability = new ArrayList<>();
        for (MethodMetrics mm : methodMetrics) {
            maintainability.add(mm.halsteadMaintainability);
        }

        return maintainability;
    }

    private ArrayList<String> getAllMethodNames(MethodMetrics[] methodMetrics) {
        ArrayList<String> names = new ArrayList<>();
        for (MethodMetrics mm : methodMetrics) {
            names.add(mm.methodName);
        }

        return names;
    }

    private ArrayList<Integer> getAllMethodNumberOfStatements(MethodMetrics[] methodMetrics) {
        ArrayList<Integer> statements = new ArrayList<>();
        for (MethodMetrics mm : methodMetrics) {
            statements.add(mm.numberOfStatements);
        }

        return statements;
    }

    private ArrayList<Double> getAllMethodLCOM(MethodMetrics[] methodMetrics) {
        ArrayList<Double> lcom = new ArrayList<>();
        for (MethodMetrics mm : methodMetrics) {
            lcom.add(mm.lackOfCohesionInMethod);
        }

        return lcom;
    }

    private ArrayList<Integer> getAllMethodsComplexity(MethodMetrics[] methodMetrics) {
        ArrayList<Integer> complexity = new ArrayList<>();
        for (MethodMetrics mm : methodMetrics) {
            complexity.add(mm.complexityOfMethod);
        }

        return complexity;
    }

    private ArrayList<String> getAllClassNames(ClassMetrics[] classMetrics) {
        ArrayList<String> names = new ArrayList<>();
        for (ClassMetrics cm : classMetrics) {
            names.add(cm.className);
        }

        return names;
    }

    private ArrayList<Integer> getAllClassNumberOfProperties(ClassMetrics[] classMetrics) {
        ArrayList<Integer> properties = new ArrayList<>();
        for (ClassMetrics cm : classMetrics) {
            properties.add(cm.numProperties);
        }

        return properties;
    }

    private ArrayList<Integer> getAllClassLOC(ClassMetrics[] classMetrics) {
        ArrayList<Integer> loc = new ArrayList<>();
        for (ClassMetrics cm : classMetrics) {
            loc.add(cm.numLinesCode);
        }

        return loc;
    }

    private ArrayList<Double> getAllClassLackOfCohesion(ClassMetrics[] classMetrics) {
        ArrayList<Double> lcom = new ArrayList<>();
        for (ClassMetrics cm : classMetrics) {
            lcom.add(cm.lackOfCohesion);
        }

        return lcom;
    }

    private ArrayList<Double> getAllClassComplexity(ClassMetrics[] classMetrics) {
        ArrayList<Double> complexity = new ArrayList<>();
        for (ClassMetrics cm : classMetrics) {
            complexity.add(cm.complexity);
        }

        return complexity;
    }

    public String getOperator(String value) {
        if (value.contains("="))
            return "=";
        else if (value.contains("+"))
            return "+";
        else if (value.contains("-"))
            return "-";
        else if (value.contains("*"))
            return "*";
        else if (value.contains("/"))
            return "/";
        else if (value.contains("++"))
            return "++";
        else if (value.contains("--"))
            return "--";
        else if (value.contains("%"))
            return "%";
        else if (value.contains("=="))
            return "==";
        else if (value.contains("!="))
            return "!=";
        else if (value.contains(">"))
            return ">";
        else if (value.contains("<"))
            return "<";
        else if (value.contains("<="))
            return "<=";
        else if (value.contains(">="))
            return ">=";
        else if (value.contains("&"))
            return "&";
        else if (value.contains("|"))
            return "|";
        else if (value.contains("^"))
            return "^";
        else if (value.contains("~"))
            return "~";
        else if (value.contains("<<"))
            return "<<";
        else if (value.contains(">>"))
            return ">>";
        else if (value.contains(">>>"))
            return ">>>";
        else if (value.contains("!"))
            return "!";
        else if (value.contains("&&"))
            return "&&";
        else if (value.contains("||"))
            return "||";
        else if (value.contains("+="))
            return "+=";
        else if (value.contains("-="))
            return "-=";
        else if (value.contains("*="))
            return "*=";
        else if (value.contains("/="))
            return "/=";
        else if (value.contains("%="))
            return "%=";
        else if (value.contains("<<="))
            return "<<=";
        else if (value.contains(">>="))
            return ">>=";
        else if (value.contains("&="))
            return "&=";
        else if (value.contains("^="))
            return "^=";
        else if (value.contains("|="))
            return "|=";
        else if (value.contains("?") && value.contains(":"))
            return "?:";
        else if (value.contains(" intanceof "))
            return "instanceof";
        return null;
    }

    public MethodMetrics getMetricsFirebase(FileMetrics metrics){
        for (MethodMetrics methodMetric : metrics.methodMetrics) {
            if(methodMetric.methodName.equals(Values.lastRefactoring.method.getName())){
                return methodMetric;
            }
        }

        return null;
    }

    public HashMap<String, Object> getValuesMetrics(FileMetrics metrics){
        HashMap<String, Object> map = new HashMap<>();

        MethodMetrics m1 = getMetricsFirebase(metrics);
        map.put("MethodName", m1.methodName);
        map.put("LOC", Integer.toString(m1.numberLinesOfCode));
        map.put("isLong", Boolean.toString(m1.isLong));
        map.put("Parameters", Integer.toString(m1.numParameters));
        map.put("LCOM", Double.toString(m1.lackOfCohesionInMethod));
        map.put("CC", Integer.toString(m1.complexityOfMethod));
        map.put("CogC", Integer.toString(m1.cognitiveComplexity));
        map.put("CogCPer", Double.toString(m1.cognitiveComplexityPercentage));
        map.put("Length", Integer.toString(m1.halsteadLength));
        map.put("Difficulty", Double.toString(m1.halsteadDifficulty));
        map.put("Volume", Double.toString(m1.halsteadVolume));
        map.put("Effort", Double.toString(m1.halsteadEffort));
        map.put("Time", Double.toString(m1.halsteadTime));
        map.put("Bugs", Double.toString(m1.halsteadBugsDelivered));
        map.put("Maintainability", Double.toString(m1.halsteadMaintainability));

        return map;
    }

    public HashMap<String, Object> getValuesMetricsFile(FileMetrics metrics){
        HashMap<String, Object> map = new HashMap<>();

        map.put("FileName", metrics.fileName);
        map.put("LOC", Integer.toString(metrics.numberOfCodeLines));
        map.put("LOCAvg", Double.toString(metrics.lines));
        map.put("numLongMethods", Integer.toString(metrics.numLongMethods));
        map.put("numLongParameters", Integer.toString(metrics.longParameterList));
        map.put("LCOM", Double.toString(metrics.lackOfCohesion));
        map.put("CC", Double.toString(metrics.complexity));
        map.put("CogC", Double.toString(metrics.cognitiveComplexity));
        map.put("CogCPer", Double.toString(metrics.cognitiveComplexityPercentage));
        map.put("Length", Double.toString(metrics.halsteadLength));
        map.put("Difficulty", Double.toString(metrics.halsteadDifficulty));
        map.put("Volume", Double.toString(metrics.halsteadVolume));
        map.put("Effort", Double.toString(metrics.halsteadEffort));
        map.put("Time", Double.toString(metrics.halsteadTime));
        map.put("Bugs", Double.toString(metrics.halsteadBugsDelivered));
        map.put("Maintainability", Double.toString(metrics.halsteadMaintainability));

        return map;
    }

    public HashMap<String, Object> getValuesMetricsNewMethod(MethodMetrics m1){
        HashMap<String, Object> map = new HashMap<>();

        map.put("MethodName", m1.methodName);
        map.put("LOC", Integer.toString(m1.numberLinesOfCode));
        map.put("isLong", Boolean.toString(m1.isLong));
        map.put("Parameters", Integer.toString(m1.numParameters));
        map.put("LCOM", Double.toString(m1.lackOfCohesionInMethod));
        map.put("CC", Integer.toString(m1.complexityOfMethod));
        map.put("CogC", Integer.toString(m1.cognitiveComplexity));
        map.put("CogCPer", Double.toString(m1.cognitiveComplexityPercentage));
        map.put("Length", Integer.toString(m1.halsteadLength));
        map.put("Difficulty", Double.toString(m1.halsteadDifficulty));
        map.put("Volume", Double.toString(m1.halsteadVolume));
        map.put("Effort", Double.toString(m1.halsteadEffort));
        map.put("Time", Double.toString(m1.halsteadTime));
        map.put("Bugs", Double.toString(m1.halsteadBugsDelivered));
        map.put("Maintainability", Double.toString(m1.halsteadMaintainability));

        return map;
    }

    public HashMap<String, Object> getValuesMetricsOldClass(FileMetrics metrics){
        HashMap<String, Object> map = new HashMap<>();

        ClassMetrics old = null;
        for (ClassMetrics classMetric : metrics.classMetrics) {
            if(classMetric.className.equals(Values.lastRefactoring._class.getName())){
                old = classMetric;
                break;
            }
        }

        map.put("ClassName", old.className);
        map.put("LOC", Integer.toString(old.numLinesCode));
        map.put("numMethods", Integer.toString(old.numMethods));
        map.put("numProperties", Integer.toString(old.numProperties));
        map.put("numLongMethods", Integer.toString(old.numLongMethods));
        map.put("numLongParameters", Integer.toString(old.longParameterList));
        map.put("LCOM", Double.toString(old.lackOfCohesion));
        map.put("CC", Double.toString(old.complexity));
        map.put("CogC", Double.toString(old.cognitiveComplexity));
        map.put("CogCPer", Double.toString(old.cognitiveComplexityPercentage));
        map.put("Length", Double.toString(old.halsteadLength));
        map.put("Difficulty", Double.toString(old.halsteadDifficulty));
        map.put("Volume", Double.toString(old.halsteadVolume));
        map.put("Effort", Double.toString(old.halsteadEffort));
        map.put("Time", Double.toString(old.halsteadTime));
        map.put("Bugs", Double.toString(old.halsteadBugsDelivered));
        map.put("Maintainability", Double.toString(old.halsteadMaintainability));

        return map;
    }

    public HashMap<String, Object> getValuesMetricsNewClass(ClassMetrics newClass){
        HashMap<String, Object> map = new HashMap<>();

        map.put("ClassName", newClass.className);
        map.put("LOC", Integer.toString(newClass.numLinesCode));
        map.put("numMethods", Integer.toString(newClass.numMethods));
        map.put("numProperties", Integer.toString(newClass.numProperties));
        map.put("numLongParameters", Integer.toString(newClass.longParameterList));
        map.put("LCOM", Double.toString(newClass.lackOfCohesion));
        map.put("CC", Double.toString(newClass.complexity));
        map.put("CogC", Double.toString(newClass.cognitiveComplexity));
        map.put("CogCPer", Double.toString(newClass.cognitiveComplexityPercentage));
        map.put("Length", Double.toString(newClass.halsteadLength));
        map.put("Difficulty", Double.toString(newClass.halsteadDifficulty));
        map.put("Volume", Double.toString(newClass.halsteadVolume));
        map.put("Effort", Double.toString(newClass.halsteadEffort));
        map.put("Time", Double.toString(newClass.halsteadTime));
        map.put("Bugs", Double.toString(newClass.halsteadBugsDelivered));
        map.put("Maintainability", Double.toString(newClass.halsteadMaintainability));

        return map;
    }

    public int increaseCognitiveComplexity(PsiMethod method) {
        int complexity = 0;
        int nesting = 0;

        for (PsiStatement statement : refactorUtils.getAllStatements(method)) {
            if(statement instanceof PsiWhileStatement){
                complexity += nesting + 1;
                nesting++;
            }
            if(statement instanceof PsiDoWhileStatement){
                complexity += nesting + 1;
                nesting++;
            }
            if(statement instanceof PsiForStatement){
                complexity += nesting + 1;
                nesting++;
            }
            if(statement instanceof PsiForeachStatement){
                complexity += nesting + 1;
                nesting++;
            }
            if(statement instanceof PsiIfStatement){
                complexity += nesting + 1;
                nesting++;
                if(((PsiIfStatement) statement).getElseBranch() != null){
                    complexity += nesting + 1;
                    nesting++;
                }
            }
            if(statement instanceof PsiSwitchStatement){
                if(((PsiSwitchStatement) statement).getBody().getStatements().length > 0) {
                    for (PsiStatement stm : ((PsiSwitchStatement) statement).getBody().getStatements()) {
                        complexity += nesting + 1;
                        nesting++;
                    }
                }
            }
            if(statement instanceof PsiCatchSection){
                for (PsiStatement stm : ((PsiCatchSection) statement).getCatchBlock().getStatements()) {
                    complexity += nesting + 1;
                    nesting++;
                }
            }
            if(statement instanceof PsiBinaryExpression){
                if(statement.getText().contains("||"))
                    complexity++;
                else if (statement.getText().contains("&&"))
                    complexity++;
            }
            if(statement instanceof PsiExpression){
                if(statement.getText().contains("?") && statement.getText().contains(":")
                        && statement.getText().toCharArray()[0] != '"' &&
                        statement.getText().toCharArray()[statement.getText().toCharArray().length -1] != '"'){
                    complexity += nesting + 1;
                    nesting++;
                }
            }
            if(statement instanceof PsiBreakStatement){
                complexity++;
            }
            if(statement instanceof PsiContinueStatement){
                complexity++;
            }
            if(statement instanceof PsiLambdaExpression){
                nesting++;
            }
            if(statement instanceof PsiMethodCallExpression){
                PsiMethod methodCalled = ((PsiMethodCallExpression) statement).resolveMethod();

                if(methodCalled != null && methodCalled.getName().equals(method.getName()) &&
                        methodCalled.getParameterList().getParametersCount() == method.getParameterList().getParametersCount()){
                    nesting++;
                }
            }
        }


        return complexity;
    }

    public int increasesCognitiveComplexity(PsiStatement statement) {
        int complexity = 0;
        int nesting = 0;

        if(statement instanceof PsiWhileStatement){
            complexity += nesting + 1;
            nesting++;
        }
        if(statement instanceof PsiDoWhileStatement){
            complexity += nesting + 1;
            nesting++;
        }
        if(statement instanceof PsiForStatement){
            complexity += nesting + 1;
            nesting++;
        }
        if(statement instanceof PsiForeachStatement){
            complexity += nesting + 1;
            nesting++;
        }
        if(statement instanceof PsiIfStatement){
            complexity += nesting + 1;
            nesting++;
            if(((PsiIfStatement) statement).getElseBranch() != null){
                complexity += nesting + 1;
                nesting++;
            }
        }
        if(statement instanceof PsiSwitchStatement){
            if(((PsiSwitchStatement) statement).getBody().getStatements().length > 0) {
                for (PsiStatement stm : ((PsiSwitchStatement) statement).getBody().getStatements()) {
                    complexity += nesting + 1;
                    nesting++;
                }
            }
        }
        if(statement instanceof PsiCatchSection){
            for (PsiStatement stm : ((PsiCatchSection) statement).getCatchBlock().getStatements()) {
                complexity += nesting + 1;
                nesting++;
            }
        }
        if(statement instanceof PsiBinaryExpression){
            if(statement.getText().contains("||"))
                complexity++;
            else if (statement.getText().contains("&&"))
                complexity++;
        }
        if(statement instanceof PsiExpression){
            if(statement.getText().contains("?") && statement.getText().contains(":")
                    && statement.getText().toCharArray()[0] != '"' &&
                    statement.getText().toCharArray()[statement.getText().toCharArray().length -1] != '"'){
                complexity += nesting + 1;
                nesting++;
            }
        }
        if(statement instanceof PsiBreakStatement){
            complexity++;
        }
        if(statement instanceof PsiContinueStatement){
            complexity++;
        }
        if(statement instanceof PsiLambdaExpression){
            nesting++;
        }
        if(statement instanceof PsiMethodCallExpression){
            PsiMethod methodCalled = ((PsiMethodCallExpression) statement).resolveMethod();
            PsiMethod method = (statement instanceof PsiMethod) ? (PsiMethod)statement : PsiTreeUtil.getParentOfType(statement, PsiMethod.class);
            if(methodCalled != null && methodCalled.getName().equals(method.getName()) &&
                    methodCalled.getParameterList().getParametersCount() == method.getParameterList().getParametersCount()){
                nesting++;
            }
        }

        return complexity;
    }
}
