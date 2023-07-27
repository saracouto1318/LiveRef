package liverefactoring.analysis.refactorings;

import liverefactoring.analysis.candidates.ExtractMethodCandidate;
import liverefactoring.analysis.metrics.MethodMetrics;
import liverefactoring.core.Fragment;
import liverefactoring.core.LastRefactoring;
import liverefactoring.core.MyRange;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.HelpID;
import com.intellij.refactoring.extractMethod.ExtractMethodHandler;
import com.intellij.refactoring.extractMethod.ExtractMethodProcessor;
import com.intellij.refactoring.extractMethod.PrepareFailedException;
import liverefactoring.utils.RefactorUtils;
import liverefactoring.utils.importantValues.ThresholdsCandidates;
import liverefactoring.utils.importantValues.Values;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

public class ExtractMethod{
    public RefactorUtils refactorUtils = new RefactorUtils();
    public PsiJavaFile psiJavaFile = null;
    public Editor editor;

    public ExtractMethod(Editor editor, PsiJavaFile file) {
        this.editor = editor;
        this.psiJavaFile = file;
    }

    public ExtractMethod(Editor editor) {
        this.editor = editor;
    }

    public ArrayList<PsiStatement> getChildrenStatements(PsiStatement statement){
        ArrayList<PsiStatement>statements = new ArrayList<>();
        for (PsiStatement child : PsiTreeUtil.findChildrenOfType(statement, PsiStatement.class)) {
            if (child instanceof PsiForStatement || child instanceof PsiSwitchStatement || child instanceof PsiForeachStatement ||
                    child instanceof PsiDoWhileStatement || child instanceof PsiWhileStatement || child instanceof PsiIfStatement) {
                statements.add(child);
            }
        }
        return statements;
    }

    public ArrayList<Fragment> getExtractableFragments(PsiJavaFile sourceFile) {
        ArrayList<Fragment> fragments = new ArrayList<>();

        for (MethodMetrics metrics : Values.before.methodMetrics) {
            if(!sourceFile.getName().contains(metrics.methodName)) {
                int totalLines = metrics.numberBlankLines + metrics.numberComments + metrics.numberLinesOfCode;
                if (metrics.isLong || totalLines >= ThresholdsCandidates.extractMethodLines || metrics.complexityOfMethod >= ThresholdsCandidates.extractMethodComplexity ||
                        metrics.halsteadEffort >= ThresholdsCandidates.extractMethodEffort) {
                    if(metrics.method.getBody() != null) {
                        PsiStatement[] statements = metrics.method.getBody().getStatements();
                        if (refactorUtils.getAllStatements(metrics.method).size() >= 2 * ThresholdsCandidates.minNumStatements) {
                            for (PsiStatement statement : statements) {
                                if (!(statement instanceof PsiReturnStatement) && !refactorUtils.containsBreakOrContinueOrReturn(statement)) {
                                    ArrayList<PsiStatement> children = getChildrenStatements(statement);
                                    if (children.size() == 0) fragments.add(addFragments(statement, metrics));
                                    else {
                                        for (PsiStatement child : children) {
                                            fragments.add(addFragments(child, metrics));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return fragments;
    }

    public Fragment addFragments(PsiStatement statement, MethodMetrics metrics){
        LogicalPosition start = editor.offsetToLogicalPosition(statement.getTextRange().getStartOffset());
        LogicalPosition end = editor.offsetToLogicalPosition(statement.getTextRange().getEndOffset());
        return new Fragment(statement, new MyRange(start, end), metrics.method);
    }

    public void extractMethod(ExtractMethodCandidate candidate, double severity, int index) {
        Values.isRefactoring = true;
        PsiElement[] elements = getElements(candidate);
        ExtractMethodProcessor processor = new ExtractMethodProcessor(editor.getProject(),
                editor, elements, null,
                "Extract Method applied to " + candidate.method.getName(), candidate.method.getName(), HelpID.EXTRACT_METHOD);
        try {
            if (processor.prepare()) {
                Values.lastRefactoring = new LastRefactoring(candidate.method, "Extract Method", elements, Values.currentFile, severity, index);
                ExtractMethodHandler.invokeOnElements(editor.getProject(), processor, candidate.sourceFile, true);
                Values.allEM.add(candidate);
                System.out.println("============ Extract Method Done!!! ============");
            }else Values.isRefactoring = false;
        } catch (PrepareFailedException e) {
            e.printStackTrace();
        }
    }

    public boolean areConsecutive(PsiStatement node1, PsiStatement node2) {
        MyRange node1Range = new MyRange(this.editor.offsetToLogicalPosition(node1.getTextRange().getStartOffset()),
                this.editor.offsetToLogicalPosition(node1.getTextRange().getEndOffset()));
        MyRange node2Range = new MyRange(this.editor.offsetToLogicalPosition(node2.getTextRange().getStartOffset()),
                this.editor.offsetToLogicalPosition(node2.getTextRange().getEndOffset()));

        return node1Range.end.line == (node2Range.start.line - 1);
    }

    public void run() {
        ArrayList<PsiStatement> _nodes = new ArrayList<>();
        ArrayList<MyRange> _ranges = new ArrayList<>();
        ArrayList<PsiMethod> _methods = new ArrayList<>();
        ArrayList<ExtractMethodCandidate> firstCandidates = new ArrayList<>();
        ArrayList<ExtractMethodCandidate> secondRoundCandidates = new ArrayList<>();
        Values.extractMethod = new ArrayList<>();

        ArrayList<Fragment> fragments = getExtractableFragments(this.psiJavaFile);
        fragments.forEach(fragment -> {
            _nodes.add(fragment.node);
            _ranges.add(fragment.range);
            _methods.add(fragment.method);
        });

        for (int i = 0; i < _ranges.size(); i++) {
            ArrayList<PsiStatement> nodesAux = new ArrayList<>();
            nodesAux.add(_nodes.get(i));
            firstCandidates.add(new ExtractMethodCandidate(_ranges.get(i), nodesAux, _methods.get(i), Values.currentFile, this.psiJavaFile));
        }

        for (int i = 0; i < firstCandidates.size() - 1; i++) {
            for (int j = 0; j < firstCandidates.size(); j++) {
                if (areConsecutive(firstCandidates.get(i).nodes.get(firstCandidates.get(i).nodes.size() - 1), firstCandidates.get(j).nodes.get(0))) {
                    MyRange range = new MyRange(firstCandidates.get(i).range.start, firstCandidates.get(j).range.end);
                    ArrayList<PsiStatement> nodesAux = new ArrayList<>(firstCandidates.get(i).nodes);
                    nodesAux.addAll(firstCandidates.get(j).nodes);
                    ExtractMethodCandidate newCandidate = new ExtractMethodCandidate(range, nodesAux, firstCandidates.get(i).method, Values.currentFile, this.psiJavaFile);

                    boolean found = false;
                    for (ExtractMethodCandidate candidate : firstCandidates) {
                        if (candidate.toString().equals(newCandidate)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) firstCandidates.add(newCandidate);
                }
            }
        }

        double minimumOriginalMethodPercentage = ThresholdsCandidates.minOrigMethodPercentageEM / 100.00;

        for (ExtractMethodCandidate candidate : firstCandidates) {
            int startLine = editor.offsetToLogicalPosition(candidate.nodes.get(0).getTextRange().getStartOffset()).line;
            int endLine = editor.offsetToLogicalPosition(candidate.nodes.get(candidate.nodes.size()-1).getTextRange().getEndOffset()).line + 1;
            int linesCandidate = endLine - startLine;

            String regex = "//.*|(\"(?:\\\\[^\"]|\\\\\"|.)*?\")|(?s)/\\*.*?\\*/";
            String[] lines = candidate.method.getBody().getText().split("\n");
            int linesMethod = 0;
            for (String s : lines) {
                String line = s.trim();
                if (!(line.length() == 0) && !line.matches(regex))
                    linesMethod++;
            }
            linesMethod-=2;

            if(candidate.numberOfStatementsToExtract >= ThresholdsCandidates.minNumStatements &&
                    candidate.numberOfStatementsToExtract <= minimumOriginalMethodPercentage*candidate.oldNumberStatements &&
                            linesCandidate <= minimumOriginalMethodPercentage*linesMethod) {
               //if(!checkLastRefactorings(candidate) && canBeExtracted(candidate) && !refactorUtils.isCandidateOnlyVariableStatements(candidate)){
                   if(!checkLastRefactorings(candidate) && !refactorUtils.isCandidateOnlyVariableStatements(candidate)){
                       boolean found = false;
                    for (ExtractMethodCandidate aux : secondRoundCandidates) {
                        if(candidate.toString().equals(aux.toString())) {
                            found = true;
                            break;
                        }
                    }
                    if(!found) secondRoundCandidates.add(candidate);
                }
           }
        }

        Stream<ExtractMethodCandidate> streams =  secondRoundCandidates.stream().sorted((a, b) -> {
            int value1 = (b.range.end.line-b.range.start.line) - (a.range.end.line-a.range.start.line) + b.numberOfStatementsToExtract - a.numberOfStatementsToExtract;
            if(value1 == 0){
                int value2 = b.highestMethodComplexity - a.highestMethodComplexity;
                if (value2 == 0) return Double.compare(b.highestLCOM, a.highestLCOM);
                return value2;
            }
            return value1;
        });

        Values.extractMethod = new ArrayList<>();
        streams.forEachOrdered(Values.extractMethod ::add);
        System.out.println("Extract Method Candidates: " + Values.extractMethod.size());
    }

    public boolean checkLastRefactorings(ExtractMethodCandidate candidate){
        for (ExtractMethodCandidate extractMethodCandidate : Values.allEM) {
            if(candidate.metrics.fileName.equals(extractMethodCandidate.metrics.fileName)) {
                if(Arrays.equals(candidate.nodes.toArray(), extractMethodCandidate.nodes.toArray()))
                    return true;
            }
        }

        return false;
    }

    private boolean canBeExtracted(ExtractMethodCandidate candidate) {
        PsiElement[] elements = getElements(candidate);
        ExtractMethodProcessor processor = new ExtractMethodProcessor(editor.getProject(),
                editor, elements, null,
                "Extract Method applied to " + candidate.method.getName(), candidate.method.getName() + "2", HelpID.EXTRACT_METHOD);

        processor.setShowErrorDialogs(false);
        try {
            return processor.prepare();
        } catch (PrepareFailedException e) {
            e.printStackTrace();
        }
        return false;
    }

    public PsiElement[] getElements(ExtractMethodCandidate candidate) {
        ArrayList<PsiElement> elements = new ArrayList<>(candidate.nodes);

        PsiElement[] psiElements = new PsiElement[elements.size()];
        for (int i = 0; i < psiElements.length; i++)
            psiElements[i] = elements.get(i);

        return psiElements;
    }
}
