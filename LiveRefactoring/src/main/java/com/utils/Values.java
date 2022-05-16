package com.utils;

import com.google.firebase.database.DatabaseReference;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.metrics.ClassMetrics;
import com.metrics.FileMetrics;
import com.metrics.MethodMetrics;
import com.refactorings.candidates.utils.LastRefactoring;
import com.refactorings.candidates.utils.Severity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;

public class Values {
    public static Editor editor = null;
    public static AnActionEvent event = null;
    public static boolean isActive = false;
    public static boolean isRefactoring = true;
    public static ArrayList<RangeHighlighter> gutters = new ArrayList<>();
    public static LastRefactoring lastRefactoring = null;
    public static ArrayList<LastRefactoring> allRefactorings = new ArrayList<>();
    public static FileMetrics currentFile = null;
    public static DatabaseReference db = null;
    public static ArrayList<Severity> candidates = new ArrayList<>();
    public static FileMetrics before = null;
    public static FileMetrics after = null;
    public static MethodMetrics newMethod = null;
    public static ClassMetrics newClass = null;
    public static Instant startTime = null;
    public static Instant betweenRefactorings = null;
    public static Instant betweenScrolls = null;
    public static boolean withColors = true;
    public static ArrayList<FileMetrics> openedFiles = new ArrayList<>();
    public static HashMap<String, ArrayList<Severity>> openedRefactorings = new HashMap<>();

    public Values(Editor editor, AnActionEvent event, DatabaseReference db) {
        Values.editor = editor;
        Values.event = event;
        Values.isActive = true;
        Values.db = db;
    }

    public Values() {

    }
}
