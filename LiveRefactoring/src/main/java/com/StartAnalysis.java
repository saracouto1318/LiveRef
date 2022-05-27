package com;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.BalloonBuilder;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.*;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.messages.MessageBus;
import com.metrics.ClassMetrics;
import com.metrics.FileMetrics;
import com.metrics.MethodMetrics;
import com.refactorings.candidates.utils.LastRefactoring;
import com.ui.VisualRepresentation;
import com.utils.MetricsUtils;
import com.utils.ThresholdsCandidates;
import com.utils.Utilities;
import com.utils.Values;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class StartAnalysis extends AnAction {
    public boolean enable = false;
    public boolean started = false;
    private final Utilities utils = new Utilities();
    private boolean done = false;
    private DatabaseReference database = null;
    private int codingCounter = 0;
    public int refactoringCounter = 0;
    public int counter = 0;
    public int active = 0;
    public ClassMetrics oldClass = null;
    public ClassMetrics newClass = null;
    public MethodMetrics oldMethod = null;
    public MethodMetrics newMethod = null;
    public boolean afterActivated = false;
    public boolean undoActivated = false;
    public Instant startCoding = null;
    public Instant endCoding = null;
    public Instant endRefactoring = null;
    public String version = "test";

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);

        if (!done) {

            activateFirebase();
            ThresholdsCandidates.username = randomString();
            done = true;
        }

        if (!enable) {
            enable = true;
            e.getPresentation().setText("Stop Analysis");
            Utilities utils = new Utilities();

            if (!started) {
                PsiManager.getInstance(editor.getProject()).addPsiTreeChangeListener(new PsiTreeChangeListener() {
                    public Project project = getActiveProject();

                    @Override
                    public void beforeChildAddition(@NotNull PsiTreeChangeEvent event) {
                    }

                    @Override
                    public void beforeChildRemoval(@NotNull PsiTreeChangeEvent event) {

                    }

                    @Override
                    public void beforeChildReplacement(@NotNull PsiTreeChangeEvent event) {

                    }

                    @Override
                    public void beforeChildMovement(@NotNull PsiTreeChangeEvent event) {
                    }

                    @Override
                    public void beforeChildrenChange(@NotNull PsiTreeChangeEvent event) {
                    }

                    @Override
                    public void beforePropertyChange(@NotNull PsiTreeChangeEvent event) {

                    }

                    @Override
                    public void childAdded(@NotNull PsiTreeChangeEvent event) {

                    }

                    @Override
                    public void childRemoved(@NotNull PsiTreeChangeEvent event) {

                    }

                    @Override
                    public void childReplaced(@NotNull PsiTreeChangeEvent event) {

                    }

                    @Override
                    public void childrenChanged(@NotNull PsiTreeChangeEvent event) {
                        if(Values.lastRefactoring != null && Values.isRefactoring){
                            active++;
                        }

                        if(active == 8 && Values.lastRefactoring != null && Values.isRefactoring) {
                            if (project != null && enable && !undoActivated) {
                                System.out.println("=========== New Event (After Refactoring) ===========");
                                System.out.println("A refactoring was applied!!!");
                                refactoringCounter++;
                                for (RangeHighlighter rangeHighlighter : Values.gutters) {
                                    rangeHighlighter.setGutterIconRenderer(null);
                                }

                                PsiDocumentManager documentManager = PsiDocumentManager.getInstance(Objects.requireNonNull(project));
                                Values.editor = ((TextEditor) Objects.requireNonNull(FileEditorManager.getInstance(project).getSelectedEditor())).getEditor();
                                PsiFile psiFile = documentManager.getCachedPsiFile(Values.editor.getDocument());
                                if (psiFile instanceof PsiJavaFile) {
                                    PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
                                    Utilities utils = new Utilities();
                                    if (utils.isPsiFileInProject(project, psiFile)) {
                                        if (Values.db != null) {
                                            String projectName = editor.getProject().getName();
                                            String endPoint = projectName + "/version " + version + "/" + removeExtension(psiJavaFile.getName()) + "/" + ThresholdsCandidates.username + "/" + Values.lastRefactoring.type + " " + refactoringCounter;
                                            Instant now = Instant.now();

                                            if(codingCounter > 0){
                                                endCoding = now;
                                                Duration betweenCoding = Duration.between(startCoding, endCoding);
                                                HashMap<String, Object> codingTime = new HashMap<>();
                                                codingTime.put("Seconds until now", betweenCoding.getSeconds());
                                                Values.db.child(endPoint + "/Coding Actions/" + "Coding " + codingCounter).setValueAsync(codingTime);
                                            }

                                            if (Values.lastRefactoring != null) {
                                                Values.allRefactorings.add(Values.lastRefactoring);
                                                afterActivated = true;
                                                Duration timeElapsed = Duration.between(Values.startTime, now);
                                                if (Values.betweenRefactorings == null)
                                                    Values.betweenRefactorings = now;
                                                Duration betweenTime = Duration.between(Values.betweenRefactorings, now);

                                                HashMap<String, Object> time = new HashMap<>();
                                                time.put("Seconds", timeElapsed.getSeconds());
                                                Values.db.child(endPoint + "/timeElapsed").setValueAsync(time);
                                                HashMap<String, Object> timeBetween = new HashMap<>();
                                                time.put("Seconds", betweenTime.getSeconds());
                                                Values.db.child(endPoint + "/timeBetween").setValueAsync(timeBetween);
                                                Values.betweenRefactorings = now;

                                                endPoint += "/metrics";

                                                MetricsUtils metricsUtils = new MetricsUtils();

                                                if (Values.lastRefactoring.type == "Extract Method") {
                                                    HashMap<String, Object> itemsFileBefore = metricsUtils.getValuesMetricsFile(Values.before);
                                                    Values.db.child(endPoint + "/FileBefore").setValueAsync(itemsFileBefore);
                                                    HashMap<String, Object> itemsBefore = metricsUtils.getValuesMetrics(Values.before);
                                                    Values.db.child(endPoint + "/before").setValueAsync(itemsBefore);
                                                    changeMetrics(Values.lastRefactoring, psiJavaFile);
                                                    HashMap<String, Object> itemsAfter = metricsUtils.getValuesMetrics(Values.after);
                                                    Values.db.child(endPoint + "/after").setValueAsync(itemsAfter);
                                                    HashMap<String, Object> itemsFileAfter = metricsUtils.getValuesMetricsFile(Values.after);
                                                    Values.db.child(endPoint + "/FileAfter").setValueAsync(itemsFileAfter);
                                                    HashMap<String, Object> itemsNew = metricsUtils.getValuesMetricsNewMethod(Values.newMethod);
                                                    Values.db.child(endPoint + "/new").setValueAsync(itemsNew);
                                                } else if (Values.lastRefactoring.type == "Extract Variable") {
                                                    HashMap<String, Object> itemsFileBefore = metricsUtils.getValuesMetricsFile(Values.before);
                                                    Values.db.child(endPoint + "/FileBefore").setValueAsync(itemsFileBefore);
                                                    HashMap<String, Object> itemsBefore = metricsUtils.getValuesMetrics(Values.before);
                                                    Values.db.child(endPoint + "/before").setValueAsync(itemsBefore);
                                                    changeMetrics(Values.lastRefactoring, psiJavaFile);
                                                    HashMap<String, Object> itemsAfter = metricsUtils.getValuesMetrics(Values.after);
                                                    Values.db.child(endPoint + "/after").setValueAsync(itemsAfter);
                                                    HashMap<String, Object> itemsFileAfter = metricsUtils.getValuesMetricsFile(Values.after);
                                                    Values.db.child(endPoint + "/FileAfter").setValueAsync(itemsFileAfter);
                                                } else if (Values.lastRefactoring.type == "Extract Class") {
                                                    HashMap<String, Object> itemsFileBefore = metricsUtils.getValuesMetricsFile(Values.before);
                                                    Values.db.child(endPoint + "/FileBefore").setValueAsync(itemsFileBefore);
                                                    HashMap<String, Object> itemsBefore = metricsUtils.getValuesMetrics(Values.before);
                                                    Values.db.child(endPoint + "/before").setValueAsync(itemsBefore);
                                                    changeMetrics(Values.lastRefactoring, psiJavaFile);
                                                    HashMap<String, Object> itemsAfter = metricsUtils.getValuesMetrics(Values.after);
                                                    Values.db.child(endPoint + "/after").setValueAsync(itemsAfter);
                                                    HashMap<String, Object> itemsFileAfter = metricsUtils.getValuesMetricsFile(Values.after);
                                                    Values.db.child(endPoint + "/FileAfter").setValueAsync(itemsFileAfter);
                                                } else if (Values.lastRefactoring.type == "Move Method") {
                                                    HashMap<String, Object> itemsFileBefore = metricsUtils.getValuesMetricsFile(Values.before);
                                                    Values.db.child(endPoint + "/FileBefore").setValueAsync(itemsFileBefore);
                                                    HashMap<String, Object> itemsBefore = metricsUtils.getValuesMetrics(Values.before);
                                                    Values.db.child(endPoint + "/before").setValueAsync(itemsBefore);
                                                    changeMetrics(Values.lastRefactoring, psiJavaFile);
                                                    HashMap<String, Object> itemsAfter = metricsUtils.getValuesMetrics(Values.after);
                                                    Values.db.child(endPoint + "/after").setValueAsync(itemsAfter);
                                                    HashMap<String, Object> itemsFileAfter = metricsUtils.getValuesMetricsFile(Values.after);
                                                    Values.db.child(endPoint + "/FileAfter").setValueAsync(itemsFileAfter);
                                                    HashMap<String, Object> itemsNew = metricsUtils.getValuesMetricsNewMethod(Values.newMethod);
                                                    Values.db.child(endPoint + "/newMethod").setValueAsync(itemsNew);
                                                    HashMap<String, Object> itemsTarget = metricsUtils.getValuesMetricsNewClass(Values.newClass);
                                                    Values.db.child(endPoint + "/targetClass").setValueAsync(itemsTarget);
                                                } else if (Values.lastRefactoring.type == "Introduce Parameter Object") {
                                                    HashMap<String, Object> itemsFileBefore = metricsUtils.getValuesMetricsFile(Values.before);
                                                    Values.db.child(endPoint + "/FileBefore").setValueAsync(itemsFileBefore);
                                                    HashMap<String, Object> itemsBefore = metricsUtils.getValuesMetrics(Values.before);
                                                    Values.db.child(endPoint + "/before").setValueAsync(itemsBefore);
                                                    changeMetrics(Values.lastRefactoring, psiJavaFile);
                                                    HashMap<String, Object> itemsAfter = metricsUtils.getValuesMetrics(Values.after);
                                                    Values.db.child(endPoint + "/after").setValueAsync(itemsAfter);
                                                    HashMap<String, Object> itemsFileAfter = metricsUtils.getValuesMetricsFile(Values.after);
                                                    Values.db.child(endPoint + "/FileAfter").setValueAsync(itemsFileAfter);
                                                    HashMap<String, Object> itemsNew = metricsUtils.getValuesMetricsNewClass(Values.newClass);
                                                    Values.db.child(endPoint + "/newClass").setValueAsync(itemsNew);
                                                }
                                            } else {
                                                Values.after = new FileMetrics(Values.editor, psiJavaFile);
                                                Values.before = new FileMetrics(Values.after);
                                            }

                                            boolean exists = false;
                                            for (int i = 0; i < Values.openedFiles.size(); i++) {
                                                if (Values.openedFiles.get(i).fileName.equals(Values.after.fileName)) {
                                                    exists = true;
                                                    Values.openedFiles.set(i, Values.after);
                                                    break;
                                                }
                                            }
                                            if (!exists)
                                                Values.openedFiles.add(Values.after);

                                            endRefactoring = now;
                                            utils.startActions(psiJavaFile);
                                        }
                                    }
                                }
                            }
                            active = 0;
                            codingCounter = 0;
                            counter = 0;
                            startCoding = null;
                            Values.isRefactoring = false;
                            Values.lastRefactoring = null;
                        }
                        else if((Values.lastRefactoring == null || !Values.isRefactoring) && active != 8){
                            System.out.println("=========== New Event (After) ===========");
                            if(startCoding == null) {
                                startCoding = Instant.now();
                            }
                            counter++;
                            if(counter % 10 == 0) {
                                codingCounter++;
                            }

                            Values.editor = ((TextEditor) Objects.requireNonNull(FileEditorManager.getInstance(event.getFile().getProject()).getSelectedEditor())).getEditor();
                            Values.after = new FileMetrics(Values.editor, (PsiJavaFile)event.getFile());

                            for (RangeHighlighter rangeHighlighter : Values.gutters) {
                                rangeHighlighter.setGutterIconRenderer(null);
                            }

                            utils.startActions((PsiJavaFile) event.getFile());
                        }
                    }

                    @Override
                    public void childMoved(@NotNull PsiTreeChangeEvent event) {
                    }

                    @Override
                    public void propertyChanged(@NotNull PsiTreeChangeEvent event) {

                    }
                });

                MessageBus messageBus = Objects.requireNonNull(editor.getProject()).getMessageBus();
                messageBus.connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerAdapter() {

                    @Override
                    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
                        System.out.println("---------------Selection Changed---------------");

                        for (RangeHighlighter rangeHighlighter : Values.gutters) {
                            rangeHighlighter.setGutterIconRenderer(null);
                        }
                        if (enable) {
                            PsiDocumentManager manager = PsiDocumentManager.getInstance(event.getManager().getProject());
                            Editor selectedEditor = ((TextEditor) Objects.requireNonNull(FileEditorManager.getInstance(event.getManager().getProject()).getSelectedEditor())).getEditor();
                            Values.editor = selectedEditor;
                            PsiFile psiFile = manager.getCachedPsiFile(selectedEditor.getDocument());
                            PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
                            Values.before = null;
                            Values.after = null;
                            active = 0;
                            codingCounter = 0;
                            counter = 0;
                            refactoringCounter = 0;
                            Values.isRefactoring = false;
                            Values.betweenRefactorings = null;
                            Values.lastRefactoring = null;
                            afterActivated = false;
                            undoActivated = false;
                            startCoding = null;
                            endCoding = null;
                            endRefactoring = null;
                            if (Values.openedRefactorings.containsKey(psiJavaFile.getName())) {
                                if (Values.openedRefactorings.get(psiJavaFile.getName()).size() > 0) {
                                    VisualRepresentation representation = new VisualRepresentation();
                                    try {
                                        representation.startVisualAnalysis(Values.editor, Values.openedRefactorings.get(psiJavaFile.getName()));
                                    } catch (IOException ex) {
                                        ex.printStackTrace();
                                    }
                                } else {
                                    JBPopupFactory factory = JBPopupFactory.getInstance();

                                    BalloonBuilder builder =
                                            factory.createHtmlTextBalloonBuilder("<b>No refactoring candidates found...</b>", MessageType.INFO, null);
                                    Balloon b = builder.createBalloon();

                                    b.show(RelativePoint.getSouthEastOf(WindowManager.getInstance().getStatusBar(Values.editor.getProject()).getComponent()), Balloon.Position.above);

                                    Values.editor.getMarkupModel().removeAllHighlighters();
                                }
                            } else {
                                utils.startActions(psiJavaFile);
                            }
                        }
                    }
                });

                started = true;
            }
            PsiDocumentManager documentManager = PsiDocumentManager.getInstance(Objects.requireNonNull(e.getProject()));
            Values.isActive = true;
            Values.editor = editor;
            Values.event = e;
            Values.db = database;
            PsiFile psiFile = documentManager.getCachedPsiFile(Values.editor.getDocument());
            PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
            utils.startActions(psiJavaFile);

        } else {
            e.getPresentation().setText("Start Analysis");
            for (RangeHighlighter rangeHighlighter : Values.gutters) {
                rangeHighlighter.setGutterIconRenderer(null);
            }
            enable = false;
            Values.isActive = false;
        }
    }

    public Project getActiveProject() {
        Project[] projects = ProjectManager.getInstance().getOpenProjects();
        Project activeProject = null;
        for (Project project : projects) {
            Window window = WindowManager.getInstance().suggestParentWindow(project);
            if (window != null && window.isActive()) {
                activeProject = project;
            }
        }
        return activeProject;
    }

    public void activateFirebase(){
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
        //Won't work, because we didn't publish our private service account
        InputStream serviceAccount =Thread.currentThread().getContextClassLoader().getResourceAsStream("firebaseConfig/ServiceAccount.json");

        FirebaseOptions options = null;
        try {
            options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://phd-live-refactoring-default-rtdb.firebaseio.com/")
                    .build();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        FirebaseApp.initializeApp(options);

        database = FirebaseDatabase.getInstance().getReference();
    }

    public void changeMetrics(LastRefactoring lastRefactoring, PsiJavaFile newFile) {
        Values.after = new FileMetrics(Values.before);

        switch (lastRefactoring.type) {
            case "Extract Method": {
                MethodMetrics old = null;
                MethodMetrics oldMethod = null;
                MethodMetrics newMethod = null;


                for (PsiMethod method : PsiTreeUtil.findChildrenOfType(newFile, PsiMethod.class)) {
                    boolean exists = false;
                    for (MethodMetrics methodMetric : Values.after.methodMetrics) {
                        if (method.getName().equals(methodMetric.methodName)) {

                            exists = true;
                            if (methodMetric.methodName.equals(lastRefactoring.method.getName())
                                    && methodMetric.numParameters == lastRefactoring.method.getParameterList().getParametersCount()) {
                                int index = Values.after.methodMetrics.indexOf(methodMetric);
                                old = methodMetric;
                                oldMethod = new MethodMetrics(method.getContainingClass(), method, false);
                                Values.after.methodMetrics.set(index, oldMethod);
                                break;
                            }
                        }
                    }

                    if (!exists) {
                        newMethod = new MethodMetrics(method.getContainingClass(), method, false);
                        Values.newMethod = newMethod;
                        Values.after.addMethod(newMethod);
                    }
                }

                for (ClassMetrics classMetric : Values.after.classMetrics) {
                    if (Objects.requireNonNull(classMetric.targetClass.getName()).equals(oldMethod.className) && Objects.requireNonNull(classMetric.targetClass.getName()).equals(newMethod.className)) {
                        classMetric.methodMetrics.set(classMetric.methodMetrics.indexOf(old), oldMethod);
                        classMetric.methodMetrics.add(newMethod);
                        break;
                    }
                }

                Values.after.setMetrics(Values.editor.getDocument());
                break;
            }
            case "Extract Class": {
                int foundBoth = 0;

                for (PsiClass psiClass : PsiTreeUtil.findChildrenOfType(newFile, PsiClass.class)) {
                    boolean found = false;
                    for (ClassMetrics classMetric : Values.after.classMetrics) {
                        if (Objects.requireNonNull(classMetric.targetClass.getName()).equals(lastRefactoring._class.getName())
                                && Objects.requireNonNull(psiClass.getName()).equals(lastRefactoring._class.getName())) {
                            oldClass = new ClassMetrics(psiClass);
                            Values.after.classMetrics.set(Values.after.classMetrics.indexOf(classMetric), oldClass);
                            foundBoth++;
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        newClass = new ClassMetrics(psiClass);
                        Values.newClass = newClass;
                        Values.after.addClass(newClass);
                        Values.lastRefactoring.setClass(newClass.targetClass);
                        foundBoth++;
                    }

                    if (foundBoth == 2)
                        break;
                }
                Values.after.setMetrics(Values.editor.getDocument());
                break;
            }
            case "Extract Variable": {
                boolean found = false;
                for (MethodMetrics methodMetric : Values.after.methodMetrics) {
                    if (methodMetric.methodName.equals(lastRefactoring.method.getName())) {
                        for (PsiMethod method : PsiTreeUtil.findChildrenOfType(newFile, PsiMethod.class)) {
                            if (method.getName().equals(methodMetric.methodName) &&
                                    methodMetric.numParameters == lastRefactoring.method.getParameterList().getParametersCount()) {
                                Values.after.methodMetrics.set(Values.after.methodMetrics.indexOf(methodMetric),
                                        new MethodMetrics(Objects.requireNonNull(method.getContainingClass()), method, methodMetric.isConstructor));
                                found = true;
                                break;
                            }
                        }
                    }

                    if (found)
                        break;
                }
                Values.after.setMetrics(Values.editor.getDocument());
                break;
            }
            case "Move Method": {
                int index = 0;
                for (PsiMethod method : PsiTreeUtil.findChildrenOfType(newFile, PsiMethod.class)) {
                    for (int i = 0; i < Values.after.methodMetrics.size(); i++) {
                        MethodMetrics methodMetric = Values.after.methodMetrics.get(i);
                        if (method.getName().equals(methodMetric.methodName)) {
                            if (methodMetric.methodName.equals(lastRefactoring.method.getName())
                                    && methodMetric.numParameters == lastRefactoring.method.getParameterList().getParametersCount()) {
                                index = Values.after.methodMetrics.indexOf(methodMetric);
                                oldMethod = new MethodMetrics(method.getContainingClass(), method, false);
                                Values.after.methodMetrics.remove(index);
                                Values.after.methodMetrics.add(oldMethod);
                                break;
                            }
                        }
                    }
                }

                for (ClassMetrics classMetric : Values.after.classMetrics) {
                    if (Objects.requireNonNull(classMetric.targetClass.getName()).equals(oldMethod.className) && Objects.requireNonNull(classMetric.targetClass.getName()).equals(newMethod.className)) {
                        classMetric.methodMetrics.set(index, oldMethod);
                        oldClass = classMetric;
                        Values.lastRefactoring._class = oldClass.targetClass;
                        break;
                    }
                }

                PsiClass[] classes = getAllClasses();
                for (PsiClass aClass : classes) {
                    for (PsiMethod method : aClass.getMethods()) {
                        PsiElement[] elements = new PsiElement[method.getBody().getStatementCount()];
                        for (int i = 0; i < method.getBody().getStatements().length; i++) {
                            elements[i] = method.getBody().getStatements()[i];
                        }
                        if (elements.length == Values.lastRefactoring.nodes.length) {
                            int counter = 0;
                            for (int i = 0; i < Values.lastRefactoring.nodes.length; i++) {
                                if (elements[i].getText().equals(Values.lastRefactoring.nodes[i]))
                                    counter++;
                            }
                            if (counter == elements.length) {
                                newMethod = new MethodMetrics(aClass, method, false);
                                Values.lastRefactoring.setMethod(method);
                                Values.newMethod = newMethod;
                                newClass = new ClassMetrics(aClass);
                                Values.lastRefactoring.setClass(aClass);
                                Values.newClass = newClass;
                            }
                        }
                    }
                }
                Values.after.setMetrics(Values.editor.getDocument());
                break;
            }
            case "Introduce Parameter Object": {
                int index = 0;
                for (PsiMethod method : PsiTreeUtil.findChildrenOfType(newFile, PsiMethod.class)) {
                    for (int i = 0; i < Values.after.methodMetrics.size(); i++) {
                        MethodMetrics methodMetric = Values.after.methodMetrics.get(i);
                        if (method.getName().equals(methodMetric.methodName)) {
                            if (methodMetric.methodName.equals(lastRefactoring.method.getName())
                                    && methodMetric.numParameters == lastRefactoring.method.getParameterList().getParametersCount()) {
                                index = Values.after.methodMetrics.indexOf(methodMetric);
                                oldMethod = methodMetric;
                                newMethod = new MethodMetrics(method.getContainingClass(), method, false);
                                Values.after.methodMetrics.remove(index);
                                Values.after.methodMetrics.add(oldMethod);
                                break;
                            }
                        }
                    }
                }

                for (ClassMetrics classMetric : Values.after.classMetrics) {
                    if (Objects.requireNonNull(classMetric.targetClass.getName()).equals(oldMethod.className) && Objects.requireNonNull(classMetric.targetClass.getName()).equals(newMethod.className)) {
                        classMetric.methodMetrics.set(index, oldMethod);
                        oldClass = classMetric;
                        Values.lastRefactoring._class = oldClass.targetClass;
                        break;
                    }
                }

                Collection<PsiClass> classesFile = PsiTreeUtil.findChildrenOfType(newFile, PsiClass.class);
                boolean exists = false;
                if (classesFile.size() > 1) {
                    for (PsiClass psiClass : classesFile) {
                        boolean found = false;
                        for (ClassMetrics classMetric : Values.after.classMetrics) {
                            if (psiClass.getName().equals(classMetric.className)) {
                                found = true;
                                break;
                            }
                        }

                        if (!found) {
                            ClassMetrics classMetrics = new ClassMetrics(psiClass);
                            newClass = classMetrics;
                            Values.newClass = newClass;
                            Values.lastRefactoring.setClass(psiClass);
                            exists = true;
                        }
                    }
                }

                if (!exists) {
                    PsiClass[] classes = getAllClasses();
                    for (PsiClass aClass : classes) {
                        if (aClass.getFields().length == Values.lastRefactoring.nodes.length) {
                            int counter = 0;
                            for (PsiField field : aClass.getFields()) {
                                String name = field.getName();
                                for (PsiElement node : Values.lastRefactoring.nodes) {
                                    PsiParameter parameter = (PsiParameter) node;
                                    if (parameter.getName().equals(name)) {
                                        counter++;
                                    }
                                }
                            }
                            if (counter == aClass.getFields().length) {
                                newClass = new ClassMetrics(aClass);
                                Values.newClass = newClass;
                                Values.lastRefactoring.setClass(aClass);
                            }
                        }
                    }
                }

                Values.after.setMetrics(Values.editor.getDocument());
                break;
            }
        }

        Values.before = new FileMetrics(Values.after);
    }

    public PsiClass[] getAllClasses() {
        Project project = Values.editor.getProject();
        Collection<VirtualFile> containingFiles = FileBasedIndex.getInstance()
                .getContainingFiles(
                        FileTypeIndex.NAME,
                        JavaFileType.INSTANCE,
                        GlobalSearchScope.projectScope(project));
        for (VirtualFile virtualFile : containingFiles) {
            if (virtualFile.getPath().contains("/src/") && (!virtualFile.getPath().contains("/test/")
                    || !virtualFile.getPath().contains("/resources/"))) {
                PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
                int pos = psiFile.getName().lastIndexOf(".");
                String fileName = psiFile.getName().substring(0, pos);
                if (Values.currentFile.fileName.contains(fileName)) {
                    if (psiFile instanceof PsiJavaFile) {
                        return ((PsiJavaFile) psiFile).getClasses();
                    }
                }
            }
        }

        return null;
    }

    private String randomString(){
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    private String removeExtension(String fileName){
        fileName = fileName.substring(0, fileName.lastIndexOf('.'));
        return fileName;
    }
}
