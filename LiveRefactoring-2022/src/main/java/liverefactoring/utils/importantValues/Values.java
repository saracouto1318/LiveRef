package com.utils.importantValues;

import com.analysis.candidates.*;
import com.analysis.metrics.ClassMetrics;
import com.analysis.metrics.FileMetrics;
import com.analysis.metrics.MethodMetrics;
import com.core.LastRefactoring;
import com.core.Severity;
import com.google.firebase.database.DatabaseReference;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.RangeHighlighter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Values {
    public static Editor editor = null;
    public static int counterCrawler = 0;
    public static AnActionEvent event = null;
    public static boolean isActive = false;
    public static boolean isRefactoring = false;
    public static ArrayList<RangeHighlighter> gutters = new ArrayList<>();
    public static LastRefactoring lastRefactoring = null;
    public static ArrayList<LastRefactoring> allRefactorings = new ArrayList<>();
    public static FileMetrics currentFile = null;
    public static DatabaseReference db = null;
    public static ArrayList<Severity> candidates = new ArrayList<>();
    public static FileMetrics before = null;
    public static FileMetrics after = null;
    public static String projectURL = "unknown";
    public static MethodMetrics newMethod = null;
    public static ClassMetrics newClass = null;
    public static Instant startTime = null;
    public static Instant betweenRefactorings = null;
    public static Instant betweenScrolls = null;
    public static boolean isFirst = false;
    public static ArrayList<FileMetrics> openedFiles = new ArrayList<>();
    public static HashMap<String, ArrayList<Severity>> openedRefactorings = new HashMap<>();
    public static HashMap<String, ArrayList<ArrayList<Object>>> metricsFile = new HashMap<>();
    public static boolean colorBlind = false;
    public static String username = "username";
    public static final int numColors = 10;
    public static List<ExtractMethodCandidate> extractMethod = new ArrayList<>();
    public static List<ExtractClassCandidate> extractClass = new ArrayList<>();
    public static List<ExtractVariableCandidate> extractVariable = new ArrayList<>();
    public static List<MoveMethodCandidate> moveMethod = new ArrayList<>();
    public static List<IntroduceParamObjCandidate> introduceParam= new ArrayList<>();
    public static List<StringComparisonCandidate> stringComp = new ArrayList<>();
    public static List<InheritanceToDelegationCandidate> inheritanceDelegation = new ArrayList<>();
    public static List<ExtractMethodCandidate> allEM = new ArrayList<>();
    public static List<ExtractClassCandidate> allEC = new ArrayList<>();
    public static List<ExtractVariableCandidate> allEV = new ArrayList<>();
    public static List<MoveMethodCandidate> allMM = new ArrayList<>();
    public static List<StringComparisonCandidate> allSC = new ArrayList<>();
    public static List<IntroduceParamObjCandidate> allIPO = new ArrayList<>();
    public static List<InheritanceToDelegationCandidate> allID = new ArrayList<>();
    public static String token = "";
    public static boolean afterActivated = false;
    public static int refactoringCounter = 0;
    public static Instant endRefactoring = Instant.now();
    public static boolean afterRefactoring = false;
    public static int numSeconds = 5;
    public static String projectName = "unknown";


    public void setLastRefactoring(LastRefactoring refactoring) {
        Values.lastRefactoring = refactoring;
    }
}
