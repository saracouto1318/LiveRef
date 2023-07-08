package liverefactoring.ui;

import liverefactoring.analysis.candidates.*;
import liverefactoring.analysis.candidates.*;
import liverefactoring.analysis.metrics.ClassMetrics;
import liverefactoring.analysis.metrics.MethodMetrics;
import liverefactoring.core.Severity;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.*;
import liverefactoring.utils.Halstead.Initiator;
import liverefactoring.utils.Halstead.MetricsEvaluator;
import liverefactoring.utils.RefactorUtils;
import liverefactoring.utils.importantValues.Values;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class MetricsTable extends DialogWrapper {
    public Severity severity;
    public Editor editor;

    public MetricsTable(Severity candidate, Editor editor) {
        super(true);
        this.editor = editor;
        this.severity = candidate;
        setTitle("Metrics Evolution");
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        Object candidate = severity.candidate;
        String[][] rec = null;

        if (candidate instanceof ExtractMethodCandidate) {
            ExtractMethodCandidate extractMethod = (ExtractMethodCandidate)candidate;
            PsiClass parentClass = extractMethod.method.getContainingClass();
            PsiMethod newMethod = JavaPsiFacade.getElementFactory(extractMethod.method.getProject()).createMethod("name", PsiType.VOID);
            RefactorUtils refactorUtils = new RefactorUtils();
            for (PsiStatement node : refactorUtils.getAllStatements(extractMethod.method)) {
                for (PsiStatement psiStatement : extractMethod.nodes) {
                    if(!node.getText().equals(psiStatement.getText()))
                        newMethod.add(node);
                }
            }
            try {
                MethodMetrics newMetrics = new MethodMetrics(parentClass, newMethod, extractMethod.method.isConstructor());
                MethodMetrics oldMetrics = extractMethod.metrics.getMethodMetrics(extractMethod.method);
                rec = new String[][]{
                        {"Lines of Code", Integer.toString(oldMetrics.numberLinesOfCode), Integer.toString(newMetrics.numberLinesOfCode)},
                        {"Cyclomatic Complexity", Integer.toString(oldMetrics.complexityOfMethod), Integer.toString(newMetrics.complexityOfMethod)},
                        {"Cognitive Complexity", Integer.toString(oldMetrics.cognitiveComplexity), Integer.toString(newMetrics.cognitiveComplexity)},
                        {"Halstead Volume", String.format("%.2f", oldMetrics.halsteadVolume), String.format("%.2f", newMetrics.halsteadVolume)},
                        {"Halstead Effort", String.format("%.2f", oldMetrics.halsteadEffort), String.format("%.2f", newMetrics.halsteadEffort)},
                        {"Halstead Difficulty", String.format("%.2f", oldMetrics.halsteadDifficulty), String.format("%.2f", newMetrics.halsteadDifficulty)},
                        {"Maintainability Index", String.format("%.2f", oldMetrics.halsteadMaintainability), String.format("%.2f", newMetrics.halsteadMaintainability)},
                        {"Lack of Cohesion", String.format("%.2f", oldMetrics.lackOfCohesionInMethod), String.format("%.2f", newMetrics.lackOfCohesionInMethod)},
                };
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        else if (candidate instanceof ExtractVariableCandidate) {
            ExtractVariableCandidate extractVariable = (ExtractVariableCandidate)candidate;
            MethodMetrics oldMetrics = Values.currentFile.getMethodMetrics(extractVariable.originalMethod);
            int numberLinesOfCode = oldMetrics.numberLinesOfCode++;

            Initiator initiator = new Initiator();
            MetricsEvaluator evaluator = initiator.initiate(extractVariable.originalMethod);

            double halsteadVolume = ((evaluator.PROGRAM_LENGTH+1)*(Math.log(evaluator.PROGRAM_LENGTH+1)/Math.log(2)));
            double halsteadDifficulty = (evaluator.n1/2)*((evaluator.N2+1)/(evaluator.n2+1));
            double halsteadEffort = halsteadVolume*halsteadDifficulty;
            double halsteadMaintainability = Math.max(0.0, ((171 - 5.2 * Math.log(halsteadVolume) - 0.23 * oldMetrics.complexityOfMethod - 16.2 * Math.log(numberLinesOfCode) + 50 * Math.sin(Math.sqrt(2.4 * oldMetrics.numberComments))) * 100 / (double) 171));

            rec = new String[][]{
                    {"Lines of Code", Integer.toString(oldMetrics.numberLinesOfCode), Integer.toString(numberLinesOfCode)},
                    {"Cyclomatic Complexity", Integer.toString(oldMetrics.complexityOfMethod), Integer.toString(oldMetrics.complexityOfMethod)},
                    {"Cognitive Complexity", Integer.toString(oldMetrics.cognitiveComplexity), Integer.toString(oldMetrics.cognitiveComplexity)},
                    {"Halstead Volume", String.format("%.2f", oldMetrics.halsteadVolume), String.format("%.2f", halsteadVolume)},
                    {"Halstead Effort", String.format("%.2f", oldMetrics.halsteadEffort), String.format("%.2f", halsteadEffort)},
                    {"Halstead Difficulty", String.format("%.2f", oldMetrics.halsteadDifficulty), String.format("%.2f", halsteadDifficulty)},
                    {"Maintainability Index", String.format("%.2f", oldMetrics.halsteadMaintainability), String.format("%.2f", halsteadMaintainability)},
                    {"Lack of Cohesion", String.format("%.2f", oldMetrics.lackOfCohesionInMethod), String.format("%.2f", oldMetrics.lackOfCohesionInMethod)},
            };
        }
        else if (candidate instanceof ExtractClassCandidate) {
            ExtractClassCandidate extractClass = (ExtractClassCandidate)candidate;
            double cc = 0;
            double cog = 0;
            double lcom = 0;
            double effort = 0;
            double volume = 0;
            double difficulty = 0;
            double maintainability = 0;
            int counterMethods = 0;

            for (MethodMetrics methodMetric : extractClass.classMetrics.methodMetrics) {
                for (PsiMethod targetMethod : extractClass.targetMethods) {
                    if(!methodMetric.method.equals(targetMethod)){
                        cc += methodMetric.complexityOfMethod;
                        cog += methodMetric.cognitiveComplexity;
                        effort += methodMetric.halsteadEffort;
                        volume += methodMetric.halsteadVolume;
                        difficulty += methodMetric.halsteadDifficulty;
                        maintainability += methodMetric.halsteadMaintainability;
                        lcom += methodMetric.lackOfCohesionInMethod;
                        counterMethods++;
                    }
                }
            }

            cc /= counterMethods;
            cog /= counterMethods;
            effort /= counterMethods;
            difficulty /= counterMethods;
            volume /= counterMethods;
            maintainability /= counterMethods;
            lcom /= counterMethods;

            rec = new String[][]{
                    {"Cyclomatic Complexity", String.format("%.2f", extractClass.classMetrics.complexity), String.format("%.2f", cc)},
                    {"Cognitive Complexity", String.format("%.2f", extractClass.classMetrics.cognitiveComplexity), String.format("%.2f", cog)},
                    {"Halstead Volume", String.format("%.2f", extractClass.classMetrics.halsteadVolume), String.format("%.2f", volume)},
                    {"Halstead Effort", String.format("%.2f", extractClass.classMetrics.halsteadEffort), String.format("%.2f", effort)},
                    {"Halstead Difficulty", String.format("%.2f", extractClass.classMetrics.halsteadDifficulty), String.format("%.2f", difficulty)},
                    {"Maintainability Index", String.format("%.2f", extractClass.classMetrics.halsteadMaintainability), String.format("%.2f", maintainability)},
                    {"Lack of Cohesion", String.format("%.2f", extractClass.classMetrics.lackOfCohesion), String.format("%.2f", lcom)},
            };
        }
        else if (candidate instanceof IntroduceParamObjCandidate) {
            IntroduceParamObjCandidate introParam = (IntroduceParamObjCandidate)candidate;
            MethodMetrics metrics = Values.currentFile.getMethodMetrics(introParam.method);

            rec = new String[][]{
                    {"Lines of Code", Integer.toString(metrics.numberLinesOfCode), Integer.toString(metrics.numberLinesOfCode)},
                    {"Number of Parameters", Integer.toString(metrics.numParameters), "1"},
                    {"Cyclomatic Complexity", Integer.toString(metrics.complexityOfMethod), Integer.toString(metrics.complexityOfMethod)},
                    {"Halstead Volume", String.format("%.2f", metrics.halsteadVolume), String.format("%.2f", metrics.halsteadVolume)},
                    {"Halstead Effort", String.format("%.2f", metrics.halsteadEffort), String.format("%.2f", metrics.halsteadEffort)},
                    {"Halstead Difficulty", String.format("%.2f", metrics.halsteadDifficulty), String.format("%.2f", metrics.halsteadDifficulty)},
                    {"Maintainability Index", String.format("%.2f", metrics.halsteadMaintainability), String.format("%.2f", metrics.halsteadMaintainability)},
                    {"Lack of Cohesion", String.format("%.2f", metrics.lackOfCohesionInMethod), String.format("%.2f", metrics.lackOfCohesionInMethod)}};
        }
        else if (candidate instanceof MoveMethodCandidate) {
            MoveMethodCandidate moveMethod = (MoveMethodCandidate)candidate;
            MethodMetrics methodMetric = Values.currentFile.getMethodMetrics(moveMethod.method);
            double cc;
            double cog;
            double lcom;
            double effort;
            double volume;
            double difficulty;
            double maintainability;

            for (ClassMetrics classMetric : Values.currentFile.classMetrics) {
                if(classMetric.className.equals(moveMethod.originalClass.getName())){
                    cc = (classMetric.complexity * classMetric.methodMetrics.size() - methodMetric.complexityOfMethod) / (classMetric.methodMetrics.size()-1);
                    cog = (classMetric.cognitiveComplexity * classMetric.methodMetrics.size() - methodMetric.cognitiveComplexity) / (classMetric.methodMetrics.size()-1);
                    effort = (classMetric.halsteadEffort * classMetric.methodMetrics.size() - methodMetric.halsteadEffort) / (classMetric.methodMetrics.size()-1);
                    volume = (classMetric.halsteadMaintainability * classMetric.methodMetrics.size() - methodMetric.halsteadMaintainability) / (classMetric.methodMetrics.size()-1);
                    difficulty = (classMetric.halsteadMaintainability * classMetric.methodMetrics.size() - methodMetric.halsteadMaintainability) / (classMetric.methodMetrics.size()-1);
                    maintainability = (classMetric.halsteadMaintainability * classMetric.methodMetrics.size() - methodMetric.halsteadMaintainability) / (classMetric.methodMetrics.size()-1);
                    lcom = (classMetric.lackOfCohesion * classMetric.methodMetrics.size() - methodMetric.lackOfCohesionInMethod) / (classMetric.methodMetrics.size()-1);

                    rec = new String[][]{
                            {"Cyclomatic Complexity", String.format("%.2f", classMetric.complexity), String.format("%.2f", cc)},
                            {"Cognitive Complexity", String.format("%.2f", classMetric.cognitiveComplexity), String.format("%.2f", cog)},
                            {"Halstead Volume", String.format("%.2f", classMetric.halsteadVolume), String.format("%.2f", volume)},
                            {"Halstead Effort", String.format("%.2f", classMetric.halsteadEffort), String.format("%.2f", effort)},
                            {"Halstead Difficulty", String.format("%.2f", classMetric.halsteadDifficulty), String.format("%.2f", difficulty)},
                            {"Maintainability Index", String.format("%.2f", classMetric.halsteadMaintainability), String.format("%.2f", maintainability)},
                            {"Lack of Cohesion", String.format("%.2f", classMetric.lackOfCohesion), String.format("%.2f", lcom)},
                    };
                    break;
                }
            }
        }
        else if (candidate instanceof StringComparisonCandidate) {
            StringComparisonCandidate stringComparison = (StringComparisonCandidate)candidate;
            PsiBinaryExpression binaryExpression = stringComparison.node;
            PsiExpression lExpr = binaryExpression.getLOperand();
            PsiExpression rExpr = binaryExpression.getROperand();

            PsiElementFactory factory = JavaPsiFacade.getInstance(this.editor.getProject()).getElementFactory();
            String newText = lExpr.getText() + ".equals(" + rExpr.getText() + ")";
            PsiMethodCallExpression equalsCall =
                    (PsiMethodCallExpression) factory.createExpressionFromText(newText, null);
            PsiMethod newMethod = JavaPsiFacade.getElementFactory(editor.getProject()).createMethod("name", PsiType.VOID);
            for (PsiStatement statement : stringComparison.originalMethod.getBody().getStatements()) {
                if(!statement.getText().contains(binaryExpression.getText()))
                    newMethod.add(statement);
                else
                    newMethod.add(equalsCall);
            }
            try {
                MethodMetrics newMetrics = new MethodMetrics(stringComparison.originalMethod.getContainingClass(), newMethod, stringComparison.originalMethod.isConstructor());
                MethodMetrics oldMetrics = Values.currentFile.getMethodMetrics(stringComparison.originalMethod);
                rec = new String[][]{
                        {"Lines of Code", Integer.toString(oldMetrics.numberLinesOfCode), Integer.toString(newMetrics.numberLinesOfCode)},
                        {"Cyclomatic Complexity", Integer.toString(oldMetrics.complexityOfMethod), Integer.toString(newMetrics.complexityOfMethod)},
                        {"Cognitive Complexity", Integer.toString(oldMetrics.cognitiveComplexity), Integer.toString(newMetrics.cognitiveComplexity)},
                        {"Halstead Volume", String.format("%.2f", oldMetrics.halsteadVolume), String.format("%.2f", newMetrics.halsteadVolume)},
                        {"Halstead Effort", String.format("%.2f", oldMetrics.halsteadEffort), String.format("%.2f", newMetrics.halsteadEffort)},
                        {"Halstead Difficulty", String.format("%.2f", oldMetrics.halsteadDifficulty), String.format("%.2f", newMetrics.halsteadDifficulty)},
                        {"Maintainability Index", String.format("%.2f", oldMetrics.halsteadMaintainability), String.format("%.2f", newMetrics.halsteadMaintainability)},
                        {"Lack of Cohesion", String.format("%.2f", oldMetrics.lackOfCohesionInMethod), String.format("%.2f", newMetrics.lackOfCohesionInMethod)},
                };
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        else if (candidate instanceof InheritanceToDelegationCandidate){
            InheritanceToDelegationCandidate inheritance = (InheritanceToDelegationCandidate)candidate;
            ClassMetrics originalClass = null;
            ClassMetrics targetClass = null;
            for (ClassMetrics classMetric : inheritance.metrics.classMetrics) {
                if(classMetric.targetClass.equals(inheritance._class))
                    originalClass = classMetric;
                else if(classMetric.targetClass.equals(inheritance.target))
                    targetClass = classMetric;
            }

            if(targetClass == null){
                try {
                    targetClass = new ClassMetrics(inheritance.target);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if(targetClass != null && originalClass != null){
                for(int i=0; i< originalClass.methodMetrics.size(); i++){
                    for(int j=0; j< targetClass.methodMetrics.size(); j++){
                        if(originalClass.methodMetrics.get(i).methodName.equals(targetClass.methodMetrics.get(j).methodName)){
                            originalClass.methodMetrics.set(i, targetClass.methodMetrics.get(j));
                        }
                    }
                }

                double cc = 0;
                double cog = 0;
                double lcom = 0;
                double effort = 0;
                double volume = 0;
                double difficulty = 0;
                double maintainability = 0;
                int numberMethods = originalClass.methodMetrics.size();

                for (MethodMetrics methodMetric : originalClass.methodMetrics) {
                    cc += methodMetric.complexityOfMethod;
                    cog += methodMetric.cognitiveComplexity;
                    effort += methodMetric.halsteadEffort;
                    volume += methodMetric.halsteadVolume;
                    difficulty += methodMetric.halsteadDifficulty;
                    maintainability += methodMetric.halsteadMaintainability;
                    lcom += methodMetric.lackOfCohesionInMethod;
                }

                cc /= numberMethods;
                cog /= numberMethods;
                effort /= numberMethods;
                difficulty /= numberMethods;
                volume /= numberMethods;
                maintainability /= numberMethods;
                lcom /= numberMethods;

                rec = new String[][]{
                        {"Cyclomatic Complexity", String.format("%.2f", originalClass.complexity), String.format("%.2f", cc)},
                        {"Cognitive Complexity", String.format("%.2f", originalClass.cognitiveComplexity), String.format("%.2f", cog)},
                        {"Halstead Volume", String.format("%.2f", originalClass.halsteadVolume), String.format("%.2f", volume)},
                        {"Halstead Effort", String.format("%.2f", originalClass.halsteadEffort), String.format("%.2f", effort)},
                        {"Halstead Difficulty", String.format("%.2f", originalClass.halsteadDifficulty), String.format("%.2f", difficulty)},
                        {"Maintainability Index", String.format("%.2f", originalClass.halsteadMaintainability), String.format("%.2f", maintainability)},
                        {"Lack of Cohesion", String.format("%.2f", originalClass.lackOfCohesion), String.format("%.2f", lcom)},
                        {"Inherited Methods", Integer.toString((int)inheritance.inherited*originalClass.methods.size()), "0"},
                        {"Override Methods", Integer.toString((int)inheritance.override*originalClass.methods.size()), "0"},
                };
            }
        }

        String[] header = { "Metric", "Before", "After" };

        JTable table = new JTable(rec, header);
        JPanel panelTable = new JPanel();
        panelTable.add(new JScrollPane(table));
        panelTable.setVisible(true);
        Box boxVertical = Box.createVerticalBox();
        Box boxHorizontal = Box.createHorizontalBox();
        boxHorizontal.setPreferredSize(new Dimension(400, rec.length*20));
        boxVertical.add(panelTable);
        boxHorizontal.add(boxVertical);
        return boxHorizontal;
    }
}
