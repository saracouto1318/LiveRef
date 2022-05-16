package com.utils;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.ide.impl.ProjectUtil;
import com.intellij.lang.jvm.JvmModifier;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.*;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.indexing.FileBasedIndex;
import com.metrics.ClassMetrics;
import com.metrics.FileMetrics;
import com.metrics.MethodMetrics;
import com.refactorings.ExtractMethod;
import com.refactorings.candidates.ExtractMethodCandidate;
import com.refactorings.candidates.utils.LastRefactoring;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Robot extends AnAction {
    public ClassMetrics oldClass = null;
    public ClassMetrics newClass = null;
    public MethodMetrics oldMethod = null;
    public MethodMetrics newMethod = null;
    public int refactoring = 0;
    public DatabaseReference database = null;
    public boolean done = false;

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        /*String[] gitNames = new String[4];
        gitNames[0] = "JHotDraw";
        gitNames[1] = "jgit";
        gitNames[2] = "geometry-api-java";
        gitNames[3] = "voldemort";

        String targetDirectory = "/Users/sarafernandes/Sara/Universidade/PhD/Experiência/Robot/";
        this.directoryPath = targetDirectory + gitNames[0];
        System.out.println(directoryPath);
        Project project = ProjectUtil.openOrImport(Paths.get(this.directoryPath));
        System.out.println(project.getName());
        Thread.sleep(10000);
        */
        if(!done) {
            activateFirebase();
            done = true;
        }
        Project project = getActiveProject();
        Collection<VirtualFile> containingFiles = FileBasedIndex.getInstance()
                .getContainingFiles(
                        FileTypeIndex.NAME,
                        JavaFileType.INSTANCE,
                        GlobalSearchScope.projectScope(project));
        System.out.println("Files: " + containingFiles.size());
        for (VirtualFile virtualFile : containingFiles) {
            PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
            if (psiFile instanceof PsiJavaFile) {
                try {
                    if(psiFile.getTextLength() > 250)
                        readFile((PsiJavaFile)psiFile);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public void activateFirebase(){
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
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
        Values.db = database;
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


    public void readFile(PsiJavaFile file) throws IOException {
        new OpenFileDescriptor(file.getProject(), file.getVirtualFile()).navigate(true);
        Editor editor = ((TextEditor) FileEditorManager.getInstance(file.getProject()).getSelectedEditor()).getEditor();
        System.out.println(file.getName());
        ExtractMethodCandidate lastExtract = null;
        int index = 0;
        while(true){
            /*if(!file.getClasses()[0].isInterface() && !file.getClasses()[0].isEnum() && !file.getClasses()[0].hasModifier(JvmModifier.ABSTRACT)){
                System.out.println("não é interface");*/

                new OpenFileDescriptor(file.getProject(), file.getVirtualFile()).navigate(true);
                editor = ((TextEditor) FileEditorManager.getInstance(file.getProject()).getSelectedEditor()).getEditor();
                Project project = editor.getProject();
                if(Values.before != null && !Values.before.fileName.equals(file.getName()))
                    Values.before = new FileMetrics(editor, file);

                ExtractMethod extractMethod = new ExtractMethod(editor, file, 1);
                extractMethod.run();
                extractMethod.candidates = extractMethod.candidates.stream().collect(Collectors.toList());
                if(extractMethod.candidates.size() != 0) {
                    System.out.println("há refactorings");
                    if(lastExtract == null) {
                        System.out.println("awesome");
                        lastExtract = extractMethod.candidates.get(0);
                    }
                    else{
                        System.out.println("nicas");
                        if(lastExtract.method.getName().equals(extractMethod.candidates.get(index).method.getName())
                            && lastExtract.nodes.size() == extractMethod.candidates.get(index).nodes.size()
                            && lastExtract.methodComplexity == extractMethod.candidates.get(index).methodComplexity
                            && lastExtract.methodCognitiveComplexity == extractMethod.candidates.get(index).methodCognitiveComplexity){
                            if(index + 1 <= extractMethod.candidates.size() - 1){
                                lastExtract = extractMethod.candidates.get(index + 1);
                                index = extractMethod.candidates.indexOf(lastExtract);
                            System.out.println("numa numa numa");}
                            else
                                break;
                        }

                    }
                    extractMethod.extractMethod(lastExtract, 10, 1);

                    PsiDocumentManager documentManager = PsiDocumentManager.getInstance(Objects.requireNonNull(project));
                    Values.editor = ((TextEditor) Objects.requireNonNull(FileEditorManager.getInstance(project).getSelectedEditor())).getEditor();
                    PsiFile psiFile = documentManager.getCachedPsiFile(Values.editor.getDocument());
                    if (psiFile instanceof PsiJavaFile) {
                        System.out.println("é java file");
                        refactoring++;
                        PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
                        Utilities utils = new Utilities();
                        Date date = new Date();
                        if (Values.lastRefactoring != null) {
                        String endPoint = "robot/" + project.getName() + "/" + removeExtension(psiJavaFile.getName()) + "/" + date.toString() + "/" + Values.lastRefactoring.type + " " + refactoring;
                        if (utils.isPsiFileInProject(project, psiFile)) {
                            if (Values.db != null) {

                                    Values.allRefactorings.add(Values.lastRefactoring);

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
                            }

                        }
                    }
                }
                else {
                    break;
                }
            /*}
            else {
                System.out.println("é interface");
                break;
            }*/
        }
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

    public String convertToCSV(String[] data) {
        if(data != null)
            return Stream.of(data)
                .map(this::escapeSpecialCharacters)
                .collect(Collectors.joining(","));
        return "";
    }

    private String removeExtension(String fileName){
        fileName = fileName.substring(0, fileName.lastIndexOf('.'));
        return fileName;
    }

    public String escapeSpecialCharacters(String data) {
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }
}
