package com.refactorings;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.metrics.FileMetrics;
import com.refactorings.candidates.ExtractMethodCandidate;
import com.refactorings.candidates.utils.Fragment;
import com.refactorings.candidates.utils.clustering.Cluster;
import com.refactorings.candidates.utils.clustering.HierarchicalClustering;
import com.utils.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ExtractMethodClustering implements Runnable {

    public RefactorUtils refactorUtils = new RefactorUtils();
    public PsiJavaFile psiJavaFile = null;
    public Editor editor;

    //public static final DistanceFunction DISTANCE_FUNCTION = DistanceFunction.HammingDistance;
    public static final int RADIUS = 3;
    public static final int MIN_POINTS = 2;

    public static final int MIN_STATEMENTS = ThresholdsCandidates.minNumStatements;
    public static final double MAX_ORIGN_ = (double) ThresholdsCandidates.maxOrigMethodPercentage / 100;

    public Utilities utils = new Utilities();
    public DistanceCalculator distanceCalculator = new DistanceCalculator();

    public Set<ExtractMethodCandidate> candidates;

    public ExtractMethodClustering(Editor editor, PsiJavaFile file) {
        this.editor = editor;
        this.psiJavaFile = file;
        this.candidates = new HashSet<>();
    }

    public ExtractMethodClustering(Editor editor) {
        this.editor = editor;
        this.candidates = new HashSet<>();
    }

    public ArrayList<Fragment> getExtractableFragments(PsiMethod method) {
        ArrayList<Fragment> fragments = new ArrayList<>();

        for (PsiStatement statement : refactorUtils.getAllStatements(method)) {
            if (!(statement instanceof PsiReturnStatement) && refactorUtils.containsBreakOrContinueOrReturn(statement) &&
                    !statement.getText().contains("super(") && !statement.getText().contains("super.")) {
                LogicalPosition start = this.editor.offsetToLogicalPosition(statement.getTextRange().getStartOffset());
                LogicalPosition end = this.editor.offsetToLogicalPosition(statement.getTextRange().getEndOffset());
                Fragment fragment = new Fragment(statement, new MyRange(start, end), method);
                fragments.add(fragment);
            }
        }

        return fragments;
    }

    public boolean areConsecutive(PsiStatement node1, PsiStatement node2) {
        MyRange node1Range = new MyRange(this.editor.offsetToLogicalPosition(node1.getTextRange().getStartOffset()),
                this.editor.offsetToLogicalPosition(node1.getTextRange().getEndOffset()));
        MyRange node2Range = new MyRange(this.editor.offsetToLogicalPosition(node2.getTextRange().getStartOffset()),
                this.editor.offsetToLogicalPosition(node2.getTextRange().getEndOffset()));

        return node1Range.end.line == (node2Range.start.line - 1) &&
                PsiTreeUtil.findChildrenOfType(node1, PsiBinaryExpression.class).size() == 0 &&
                PsiTreeUtil.findChildrenOfType(node2, PsiBinaryExpression.class).size() == 0 &&
                PsiTreeUtil.findChildrenOfType(node1, PsiIfStatement.class).size() == 0 &&
                PsiTreeUtil.findChildrenOfType(node2, PsiIfStatement.class).size() == 0 &&
                !(node1 instanceof PsiBinaryExpression) && !(node2 instanceof PsiBinaryExpression) &&
                !(node1 instanceof PsiIfStatement) && !(node2 instanceof PsiIfStatement);
    }

    public Set<ExtractMethodCandidate> OPTICS(PsiMethod method, ArrayList<Fragment> fragments, FileMetrics metrics){
        Set<ExtractMethodCandidate> candidatesList = new HashSet<>();

        BinaryRepresentation processed = new BinaryRepresentation(fragments);
        ConcurrentHashMap<PsiStatement, ArrayList<Integer>> db = processed.representation;
        List<DBPoint> dataset = new ArrayList<>();
        int numLines = Objects.requireNonNull(method.getBody()).getText().split("\n").length;

        for (PsiStatement statement : db.keySet()) {
            DBPoint point = new DBPoint();
            point.statement = statement;
            point.entities = utils.getEntitiesStatement(point.statement);
            point.values = db.get(statement);
            dataset.add(point);
        }

        OPTICS clustering = new OPTICS(RADIUS, MIN_POINTS);
        clustering.setPoints(dataset);
        clustering.cluster();

        List<DBPoint> listPoints = clustering.order;

        listPoints.sort((a, b) -> {
            int startA = a.statement.getTextRange().getStartOffset();

            int startB = b.statement.getTextRange().getStartOffset();

            return startA - startB;
        });

        for (int i = 0; i < listPoints.size(); i++) {
            DBPoint dbPoint = listPoints.get(i);
            LogicalPosition start = this.editor.offsetToLogicalPosition(dbPoint.statement.getTextRange().getStartOffset());
            LogicalPosition end = this.editor.offsetToLogicalPosition(dbPoint.statement.getTextRange().getEndOffset());
            ArrayList<PsiStatement> statements = new ArrayList<>();

            if(start.line == end.line){
                ArrayList<DBPoint> clusterPoints = new ArrayList<>();
                clusterPoints.add(dbPoint);

                for(int j = i+1; j < listPoints.size(); j++){
                    DBPoint dbPointB = listPoints.get(j);
                    LogicalPosition startB = this.editor.offsetToLogicalPosition(dbPointB.statement.getTextRange().getStartOffset());
                    LogicalPosition lastElemEnd = this.editor.offsetToLogicalPosition(clusterPoints.get(clusterPoints.size()-1).statement.getTextRange().getEndOffset());
                    if(startB.line == (lastElemEnd.line + 1)){
                        clusterPoints.add(dbPointB);
                    }
                }

                LogicalPosition startCandidate = this.editor.offsetToLogicalPosition(clusterPoints.get(0).statement.getTextRange().getStartOffset());
                LogicalPosition endCandidate = this.editor.offsetToLogicalPosition(clusterPoints.get(clusterPoints.size()-1).statement.getTextRange().getEndOffset());

                for (DBPoint clusterPoint : clusterPoints) {
                    statements.add(clusterPoint.statement);
                }

                ExtractMethodCandidate candidate = new ExtractMethodCandidate(new MyRange(startCandidate, endCandidate), statements, method, metrics, psiJavaFile);

                if(!refactorUtils.isCandidateOnlyVariableStatements(candidate) &&
                        endCandidate.line-startCandidate.line >= MIN_STATEMENTS-1 &&
                        numLines-(endCandidate.line-startCandidate.line) <= (MAX_ORIGN_ * numLines))
                    candidatesList.add(candidate);
            }
            else{
                LogicalPosition startCandidate = this.editor.offsetToLogicalPosition(dbPoint.statement.getTextRange().getStartOffset());
                LogicalPosition endCandidate = this.editor.offsetToLogicalPosition(dbPoint.statement.getTextRange().getEndOffset());
                statements.add(dbPoint.statement);
                ExtractMethodCandidate candidate = new ExtractMethodCandidate(new MyRange(startCandidate, endCandidate), statements, method, metrics, psiJavaFile);

                if(!refactorUtils.isCandidateOnlyVariableStatements(candidate) &&
                        endCandidate.line-startCandidate.line >= MIN_STATEMENTS-1 &&
                        numLines-(endCandidate.line-startCandidate.line) <= (MAX_ORIGN_ * numLines))
                    candidatesList.add(candidate);
            }
        }

        return candidatesList;
    }

    public Set<ExtractMethodCandidate> Hierarchical(PsiMethod method, ArrayList<Fragment> fragments, FileMetrics metrics){
        Set<ExtractMethodCandidate> candidatesList = new HashSet<>();

        ArrayList<PsiStatement> _nodes = new ArrayList<>();
        ArrayList<MyRange> _ranges = new ArrayList<>();
        ArrayList<PsiMethod> _methods = new ArrayList<>();
        int numLines = Objects.requireNonNull(method.getBody()).getText().split("\n").length;

        fragments.forEach(fragment -> {
            _nodes.add(fragment.node);
            _ranges.add(fragment.range);
            _methods.add(fragment.method);
        });

        ArrayList<ExtractMethodCandidate> candidates = new ArrayList<>();
        for (int i = 0; i < _ranges.size(); i++) {
            ArrayList<PsiStatement> nodesAux = new ArrayList<>();
            nodesAux.add(_nodes.get(i));
            candidates.add(new ExtractMethodCandidate(_ranges.get(i), nodesAux, _methods.get(i), metrics, this.psiJavaFile));

        }
        for (int i = 0; i < candidates.size() - 1; i++) {
            for (int j = 0; j < candidates.size(); j++) {
                if (areConsecutive(candidates.get(i).nodes.get(candidates.get(i).nodes.size() - 1), candidates.get(j).nodes.get(0))) {
                    MyRange range = new MyRange(candidates.get(i).range.start, candidates.get(j).range.end);
                    ArrayList<PsiStatement> nodesAux = new ArrayList<>(candidates.get(i).nodes);
                    nodesAux.addAll(candidates.get(j).nodes);
                    ExtractMethodCandidate newCandidate = new ExtractMethodCandidate(range, nodesAux, candidates.get(i).method, metrics, this.psiJavaFile);

                    boolean found = false;
                    for (ExtractMethodCandidate candidate : candidates) {
                        if (candidate.range.start.line == newCandidate.range.start.line && candidate.range.end.line == newCandidate.range.end.line) {
                            found = true;
                            break;
                        }
                    }

                    if (!found)
                        candidates.add(newCandidate);
                }
            }
        }

        double[][] distances = distanceCalculator.getJaccardDistanceMatrixByMethod(fragments.toArray());
        HierarchicalClustering clustering = new HierarchicalClustering(distances);
        Set<Cluster> clusters = clustering.getClustersStatements(candidates);
        Iterator<Cluster> itr = clusters.iterator();

        while (itr.hasNext()) {
            ArrayList<PsiStatement> stmCandidates = new ArrayList<>();

            for (PsiElement entity : itr.next().getEntities()) {
                stmCandidates.add((PsiStatement) entity);
            }

            LogicalPosition start = this.editor.offsetToLogicalPosition(stmCandidates.get(0).getTextRange().getStartOffset());
            LogicalPosition end = this.editor.offsetToLogicalPosition(stmCandidates.get(stmCandidates.size() - 1).getTextRange().getEndOffset());
            MyRange range = new MyRange(start, end);
            ExtractMethodCandidate candidate = new ExtractMethodCandidate(range, stmCandidates, method, metrics, this.psiJavaFile);

            if(!refactorUtils.isCandidateOnlyVariableStatements(candidate) &&
                    end.line-start.line >= MIN_STATEMENTS-1 &&
                    numLines-(end.line-start.line) <= (MAX_ORIGN_ * numLines))
                candidatesList.add(candidate);
        }

        return candidatesList;
    }

    @Override
    public void run() {
        FileMetrics metrics = null;
        if(Values.before != null)
            metrics = Values.before;
        else {
            metrics = new FileMetrics(this.editor, this.psiJavaFile);
            Values.before = metrics;
        }
        Values.currentFile = metrics;


        PsiClass[] classes = this.psiJavaFile.getClasses();
        for (PsiClass aClass : classes) {
            PsiMethod[] methods = aClass.getMethods();
            for (PsiMethod method : methods) {
                ArrayList<Fragment> fragments = this.getExtractableFragments(method);
                //candidates = OPTICS(method, fragments, metrics);
                candidates = Hierarchical(method, fragments, metrics);
            }
        }

        List<ExtractMethodCandidate> candidatesAux = new ArrayList<>(candidates);
        candidatesAux.sort(Comparator.comparingInt(a -> a.numberOfStatementsToExtract / a.oldNumberStatements));

        candidates = new HashSet<>(candidatesAux);

        System.out.println("Extract Method: " + this.candidates.size());
    }
}
