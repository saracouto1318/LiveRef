package com.refactorings;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.HelpID;
import com.intellij.refactoring.extractMethod.ExtractMethodHandler;
import com.intellij.refactoring.extractMethod.ExtractMethodProcessor;
import com.intellij.refactoring.extractMethod.PrepareFailedException;
import com.metrics.FileMetrics;
import com.metrics.MethodMetrics;
import com.refactorings.candidates.ExtractMethodCandidate;
import com.refactorings.candidates.utils.Fragment;
import com.refactorings.candidates.utils.LastRefactoring;
import com.utils.MyRange;
import com.utils.RefactorUtils;
import com.utils.ThresholdsCandidates;
import com.utils.Values;

import java.util.*;
import java.util.stream.Stream;

public class ExtractMethod implements Runnable{
    public RefactorUtils refactorUtils = new RefactorUtils();
    public PsiJavaFile psiJavaFile = null;
    public Editor editor;
    public List<ExtractMethodCandidate> candidates;
    public int version = 0;

    public ExtractMethod(Editor editor, PsiJavaFile file, int version) {
        this.editor = editor;
        this.psiJavaFile = file;
        this.candidates = new ArrayList<>();
        this.version = version;
    }

    public ExtractMethod(Editor editor) {
        this.editor = editor;
        this.candidates = new ArrayList<>();
    }

    public ArrayList<Fragment> getExtractableFragments(PsiJavaFile sourceFile) {
        ArrayList<Fragment> fragments = new ArrayList<>();

        for (MethodMetrics metrics : Values.before.methodMetrics) {
            if(!sourceFile.getName().contains(metrics.methodName)) {
                    //if (metrics.isLong || metrics.complexityOfMethod >= 15 || metrics.cognitiveComplexity >= 15 || metrics.halsteadEffort >= 300) {
                    ArrayList<PsiStatement> statements = refactorUtils.getAllStatements(metrics.method);
                if (statements.size() >= 2 * ThresholdsCandidates.minNumStatements) {
                    for (PsiStatement statement : statements) {
                        if (!(statement instanceof PsiReturnStatement) && !refactorUtils.containsBreakOrContinueOrReturn(statement)) {
                            LogicalPosition start = editor.offsetToLogicalPosition(statement.getTextRange().getStartOffset());
                            LogicalPosition end = editor.offsetToLogicalPosition(statement.getTextRange().getEndOffset());
                            Fragment fragment = new Fragment(statement, new MyRange(start, end), metrics.method);
                            fragments.add(fragment);
                        }
                    }
                }
                //}
            }
        }

        return fragments;
    }

    public ArrayList<Fragment> getExtractableFragments(PsiJavaFile sourceFile, Editor editor) {
        ArrayList<Fragment> fragments = new ArrayList<>();
        LogicalPosition cursor = editor.getCaretModel().getLogicalPosition();

        for (PsiClass aClass : sourceFile.getClasses()) {
            if(!aClass.isEnum() && !aClass.isInterface()){
                for (PsiMethod method : refactorUtils.getMethods(aClass)) {
                    LogicalPosition startMethod = editor.offsetToLogicalPosition(method.getTextRange().getStartOffset());
                    LogicalPosition endMethod = editor.offsetToLogicalPosition(method.getTextRange().getEndOffset());
                    if(startMethod.line <= cursor.line && cursor.line <= endMethod.line){
                        MethodMetrics metrics = Values.before.getMethodMetrics(method);
                        if (metrics != null) {
                            if (metrics.isLong || metrics.complexityOfMethod > 5 || metrics.halsteadEffort > 15) {
                                if(method.getBody().getStatementCount() >= 2 * ThresholdsCandidates.minNumStatements) {
                                    for (PsiStatement statement : refactorUtils.getAllStatements(method)) {
                                        if (!(statement instanceof PsiReturnStatement) && !refactorUtils.containsBreakOrContinueOrReturn(statement)) {
                                            LogicalPosition start = editor.offsetToLogicalPosition(statement.getTextRange().getStartOffset());
                                            LogicalPosition end = editor.offsetToLogicalPosition(statement.getTextRange().getEndOffset());
                                            Fragment fragment = new Fragment(statement, new MyRange(start, end), method);
                                            fragments.add(fragment);
                                        }
                                    }
                                }
                            }
                        }
                       break;
                    }
                }
            }
        }
        return fragments;
    }

    public void extractMethod(ExtractMethodCandidate candidate, double severity, int index) {
        PsiElement[] elements = getElements(candidate);
        ExtractMethodProcessor processor = new ExtractMethodProcessor(editor.getProject(),
                editor, elements, null,
                "Extract Method applied to " + candidate.method.getName(), candidate.method.getName(), HelpID.EXTRACT_METHOD);
        try {
            if (processor.prepare()) {
                Values.lastRefactoring = new LastRefactoring(candidate.method, "Extract Method", elements, severity, index);
                ExtractMethodHandler.invokeOnElements(editor.getProject(), processor, candidate.sourceFile, true);

                System.out.println("============ Extract Method Done!!! ============");
                Values.isRefactoring = true;
                //FileDocumentManager.getInstance().saveAllDocuments();

            }
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

    @Override
    public void run() {
        ArrayList<PsiStatement> _nodes = new ArrayList<>();
        ArrayList<MyRange> _ranges = new ArrayList<>();
        ArrayList<PsiMethod> _methods = new ArrayList<>();
        ArrayList<ExtractMethodCandidate> candidatesAux = new ArrayList<>();
        ArrayList<ExtractMethodCandidate> candidatesTemp = new ArrayList<>();
        FileMetrics metrics = null;
        if(Values.before != null) {
            metrics = (Values.after != null) ? new FileMetrics(Values.after) : new FileMetrics(Values.before);
            Values.before = new FileMetrics(metrics);
        }
        else{
            boolean exists = false;
            for(int i=0; i < Values.openedFiles.size(); i++){
                if(Values.openedFiles.get(i).fileName.equals(psiJavaFile.getName())){
                    metrics = new FileMetrics(Values.openedFiles.get(i));
                    exists = true;
                    break;
                }
            }
            if(!exists) {
                metrics = new FileMetrics(editor, psiJavaFile);
                Values.openedFiles.add(metrics);
            }
            Values.before = new FileMetrics(metrics);
        }
        Values.currentFile = new FileMetrics(metrics);

        ArrayList<Fragment> fragments = new ArrayList<>();
        if(this.version < 4) {
            fragments = getExtractableFragments(this.psiJavaFile);
        }
        else {
            fragments = getExtractableFragments(this.psiJavaFile, this.editor);
            if(this.version == 5 || this.version == 7){
                if(fragments.size() > 0) {
                    ArrayList<Fragment> auxFragments = getExtractableFragments(this.psiJavaFile);
                    for (Fragment auxFragment : auxFragments) {
                        if (!fragments.contains(auxFragment))
                            fragments.add(auxFragment);
                    }
                }
            }
        }

        fragments.forEach(fragment -> {
            _nodes.add(fragment.node);
            _ranges.add(fragment.range);
            _methods.add(fragment.method);
        });

        for (int i = 0; i < _ranges.size(); i++) {
            ArrayList<PsiStatement> nodesAux = new ArrayList<>();
            nodesAux.add(_nodes.get(i));
            candidatesAux.add(new ExtractMethodCandidate(_ranges.get(i), nodesAux, _methods.get(i), metrics, this.psiJavaFile));
        }

        for (int i = 0; i < candidatesAux.size() - 1; i++) {
            for (int j = 0; j < candidatesAux.size(); j++) {
                if (areConsecutive(candidatesAux.get(i).nodes.get(candidatesAux.get(i).nodes.size() - 1), candidatesAux.get(j).nodes.get(0))) {
                    MyRange range = new MyRange(candidatesAux.get(i).range.start, candidatesAux.get(j).range.end);
                    ArrayList<PsiStatement> nodesAux = new ArrayList<>(candidatesAux.get(i).nodes);
                    nodesAux.addAll(candidatesAux.get(j).nodes);
                    ExtractMethodCandidate newCandidate = new ExtractMethodCandidate(range, nodesAux, candidatesAux.get(i).method, metrics, this.psiJavaFile);

                    boolean found = false;
                    for (ExtractMethodCandidate candidate : candidatesAux) {
                        if (candidate.range.start.line == newCandidate.range.start.line && candidate.range.end.line == newCandidate.range.end.line) {
                            found = true;
                            break;
                        }
                    }

                    if (!found)
                        candidatesAux.add(newCandidate);
                }
            }
        }

        int minStatements = ThresholdsCandidates.minNumStatements;
        double maximumOriginalMethodPercentage = ThresholdsCandidates.maxOrigMethodPercentage / 100.00;

        for (ExtractMethodCandidate candidate : candidatesAux) {
            if (!refactorUtils.isCandidateOnlyVariableStatements(candidate)){
                candidatesTemp.add(candidate);
            }
        }

        candidates = new ArrayList<>();

        for (ExtractMethodCandidate candidate : candidatesTemp) {
            int oldNumberStatements = Objects.requireNonNull(refactorUtils.getAllStatements(candidate.method)).size();

            int totalNumStatements = 0;
            for (PsiStatement node : candidate.nodes) {
                totalNumStatements += PsiTreeUtil.findChildrenOfType(node, PsiStatement.class).size();
            }

            /*if(candidate.numberOfStatementsToExtract >= minStatements && candidate.numberOfStatementsToExtract <= (double)(maximumOriginalMethodPercentage * candidate.oldNumberStatements)) {
                candidates.add(candidate);
            }*/

            /*int totalNumStatementsMethod = 0;
            for (PsiStatement node : candidate.method.getBody().getStatements()) {
                totalNumStatementsMethod += PsiTreeUtil.findChildrenOfType(node, PsiStatement.class).size();
            }*/
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
           if(candidate.numberOfStatementsToExtract >= minStatements && candidate.numberOfStatementsToExtract <= (double)(maximumOriginalMethodPercentage * candidate.oldNumberStatements) && linesCandidate <= (double)(maximumOriginalMethodPercentage * linesMethod)) {
                candidates.add(candidate);
           }
        }

        candidatesTemp = new ArrayList<>();

        for (ExtractMethodCandidate candidate : candidates) {
            if(candidatesTemp.size() == 0)
                candidatesTemp.add(candidate);
            else{
                boolean found = false;
                for (ExtractMethodCandidate extractMethodCandidate : candidatesTemp) {
                    if(candidate.range.start.line == extractMethodCandidate.range.start.line &&
                            candidate.range.end.line == extractMethodCandidate.range.end.line){
                            found = true;
                        if(candidate.range.start.column <= extractMethodCandidate.range.start.column) {
                            candidatesTemp.set(candidatesTemp.indexOf(extractMethodCandidate), candidate);
                            break;
                        }

                    }
                }
                if(!found)
                    candidatesTemp.add(candidate);
            }
        }

        candidatesAux = new ArrayList<>(candidatesTemp);

        for (ExtractMethodCandidate aux : candidatesAux) {
            if(checkLastRefactorings(aux, "Extract Method")){
                candidatesAux.remove(candidatesAux.indexOf(aux));
                break;
            }
        }

        candidatesTemp = new ArrayList<>();

        for (ExtractMethodCandidate aux : candidatesAux) {
            if(canBeExtracted(aux)) {
                candidatesTemp.add(aux);
            }
        }

        Stream<ExtractMethodCandidate> streams =  candidatesTemp.stream().sorted(new Comparator<ExtractMethodCandidate>() {
            @Override
            public int compare(ExtractMethodCandidate a, ExtractMethodCandidate b) {
                int value1 = b.numberOfStatementsToExtract - a.numberOfStatementsToExtract;
                if(value1 == 0){
                    int value2 = b.methodComplexity - a.methodComplexity;
                    if(value2 == 0){
                        int value3 = b.methodCognitiveComplexity - a.methodCognitiveComplexity;
                        if(value3 == 0){
                            return (int)(b.lcom-a.lcom);
                        }
                        return value3;
                    }
                    return value2;
                }
                return value1;
            }
        });

        candidatesAux = new ArrayList<>();
        streams.forEachOrdered(candidatesAux::add);

        candidates = new ArrayList<>();
        for (ExtractMethodCandidate aux : candidatesAux) {
            boolean found = false;
            for (ExtractMethodCandidate candidate : candidates) {
                if(candidate.toString().equals(aux)) {
                    found = true;
                }
            }
            if(!found) {
                candidates.add(aux);
            }
        }

        if(this.version == 7) {
            LogicalPosition cursor = editor.getCaretModel().getLogicalPosition();
            PsiMethod currentMethod = null;
            for (ExtractMethodCandidate candidate: candidatesAux) {
                if(candidate.range.start.line <= cursor.line && candidate.range.end.line >= cursor.line){
                    if(currentMethod == null)
                        currentMethod = candidate.method;
                    else{
                        if(currentMethod.getName().equals(candidate.method.getName())){
                            this.candidates.remove(candidate);
                        }
                    }
                }
            }
        }


        /*for (ExtractMethodCandidate c : candidates) {
            PsiMethod candidateMethod = c.method;
            if (candidateMethod != null) {
                String nameCandidate = candidateMethod.getName();
                MethodMetrics candidateMetrics = null;
                for (int i = 0; i < c.metrics.methodMetrics.size(); i++) {
                    if (c.metrics.methodMetrics.get(i).methodName == nameCandidate) {
                        candidateMetrics = c.metrics.methodMetrics.get(i);
                    }

                    if (candidateMetrics != null) {
                        if (c.numberOfStatementsToExtract >= minStatements && (c.oldNumberStatements - c.numberOfStatementsToExtract) <= (maximumOriginalMethodPercentage * c.oldNumberStatements))
                            filteredCandidates.add(c);
                    }
                }
            }
        }*/

        System.out.println("Extract Method: " + this.candidates.size() +"\n");
    }

    public int checkRepeated(ExtractMethodCandidate candidate, ArrayList<ExtractMethodCandidate> extracts){
        for (ExtractMethodCandidate extract : extracts) {
            PsiElement[] elementsExtract = getElements(extract);
            PsiElement[] elementsCandidate = getElements(candidate);
            if(elementsExtract.length == elementsCandidate.length) {
                int counter = 0;
                for (int i = 0; i < elementsCandidate.length; i++) {
                    if(elementsCandidate[i].getText().equals(elementsExtract[i].getText()))
                        counter++;
                }
                if(counter == elementsExtract.length)
                    return extracts.indexOf(extract);
            }
        }

        return -1;
    }

    public boolean checkLastRefactorings(ExtractMethodCandidate candidate, String type){
        for (LastRefactoring allRefactoring : Values.allRefactorings) {
            if(allRefactoring.type == type){
                PsiElement[] elementsPast = allRefactoring.nodes;
                PsiElement[] elementsCandidate = getElements(candidate);
                if(elementsPast.length == elementsCandidate.length){
                    int counter = 0;
                    for(int i = 0; i < elementsPast.length; i++) {
                        if (elementsPast[i].getText().equals(elementsCandidate[i].getText())) {
                            counter++;
                        }
                    }
                    if(counter == elementsPast.length)
                        return true;
                }
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
