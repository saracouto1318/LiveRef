package com;

import com.analysis.candidates.*;
import com.analysis.metrics.FileMetrics;
import com.analysis.refactorings.*;
import com.core.LastRefactoring;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.FirebaseDatabase;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerAdapter;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.BalloonBuilder;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.messages.MessageBus;
import com.ui.JDeodorant.ExtractClassRefactoring;
import com.utils.MetricsUtils;
import com.utils.UtilitiesOverall;
import com.utils.importantValues.Values;
import org.jetbrains.annotations.NotNull;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

public class Bot implements StartupActivity {

    Project project = null;
    boolean refactoring = false;
    int numRefactorings = 0;
    ArrayList<Integer> lastNumberRefactorings = new ArrayList<>();

    String URL = "/Users/sarafernandes/Desktop/Crawler/Prints/";

    @Override
    public void runActivity(@NotNull Project project) {
        if(!Values.isActive) {
            activateFirebase();
            Values.isActive = true;
        }

        this.project = project;

        if(FileEditorManager.getInstance(this.project).getSelectedEditor() != null) {
            Values.editor = ((TextEditor) FileEditorManager.getInstance(project).getSelectedEditor()).getEditor();
        }

        System.setOut(new PrintStream(System.out) {
            public void println(String s) {
                super.println(s);
                if (Values.isActive) {
                    String directoryPath = URL + project.getName();
                    Path directory = Paths.get(directoryPath);
                    if (!Files.exists(directory)) {
                        try {
                            // Create the directory
                            Files.createDirectories(directory);
                        } catch (IOException e) {
                            System.err.println("Failed to create directory: " + e.getMessage());
                        }
                    }
                    String fileName = directoryPath + "/" + Values.currentFile.fileName.split("\\.")[0] + ".txt";
                    try (FileWriter fileWriter = new FileWriter(fileName, true)) {
                        fileWriter.write(s+"\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (s.contains("Candidates:")) {
                        Values.counterCrawler++;
                        int count = 0;
                        for (Integer lastNumberRefactoring : lastNumberRefactorings) {
                            if(lastNumberRefactoring != 0)
                                count++;
                        }
                        if ((Values.counterCrawler == 7 && !refactoring) || (Values.counterCrawler == count && !refactoring)) {
                            refactoring = true;
                            numRefactorings = Values.extractMethod.size() + Values.extractVariable.size() + Values.extractClass.size() +
                                    Values.moveMethod.size() + Values.introduceParam.size() + Values.stringComp.size() + Values.inheritanceDelegation.size();

                            if (numRefactorings > 0) {
                                if(Values.refactoringCounter == 0){
                                    lastNumberRefactorings.add(Values.extractMethod.size());
                                    lastNumberRefactorings.add(Values.extractVariable.size());
                                    lastNumberRefactorings.add(Values.extractClass.size());
                                    lastNumberRefactorings.add(Values.moveMethod.size());
                                    lastNumberRefactorings.add(Values.introduceParam.size());
                                    lastNumberRefactorings.add(Values.inheritanceDelegation.size());
                                    lastNumberRefactorings.add(Values.stringComp.size());
                                }
                                else{
                                    if(lastNumberRefactorings.get(0) != 0)
                                        lastNumberRefactorings.set(0, Values.extractMethod.size());
                                    if(lastNumberRefactorings.get(1) != 0)
                                        lastNumberRefactorings.set(1, Values.extractVariable.size());
                                    if(lastNumberRefactorings.get(2) != 0)
                                        lastNumberRefactorings.set(2, Values.extractClass.size());
                                    if(lastNumberRefactorings.get(3) != 0)
                                        lastNumberRefactorings.set(3, Values.moveMethod.size());
                                    if(lastNumberRefactorings.get(4) != 0)
                                        lastNumberRefactorings.set(4, Values.introduceParam.size());
                                    if(lastNumberRefactorings.get(5) != 0)
                                        lastNumberRefactorings.set(5, Values.inheritanceDelegation.size());
                                    if(lastNumberRefactorings.get(6) != 0)
                                        lastNumberRefactorings.set(6, Values.stringComp.size());
                                }
                                System.out.println("Start Refactoring code...");
                                int number = generateRandomNumber(0);
                                applyRefactorings(number);
                            } else {
                                System.out.println("No more refactorings.");
                                ApplicationManager.getApplication().invokeLater(() -> {
                                    JBPopupFactory factory = JBPopupFactory.getInstance();

                                    BalloonBuilder builder =
                                            factory.createHtmlTextBalloonBuilder("<b>No refactoring candidates found...</b>", MessageType.INFO, null);
                                    builder.setFadeoutTime(0);
                                    builder.setAnimationCycle(0);
                                    builder.setShadow(false);
                                    builder.setHideOnClickOutside(true);
                                    builder.setHideOnKeyOutside(false);
                                    builder.setHideOnAction(false);
                                    builder.setCloseButtonEnabled(true);

                                    Balloon b = builder.createBalloon();

                                    b.show(RelativePoint.getNorthEastOf(WindowManager.getInstance().getStatusBar(Values.editor.getProject()).getComponent()), Balloon.Position.below);
                                    MarkupModel markupModel = Values.editor.getMarkupModel();
                                    markupModel.removeAllHighlighters();
                                });
                            }
                        }
                    }
                    if (s.equals("Finished Refactoring")){
                        saveMetrics();
                        numRefactorings--;
                        if (numRefactorings > 0) {
                            if (FileEditorManager.getInstance(project).getSelectedEditor() != null) {
                                Values.editor = ((TextEditor) FileEditorManager.getInstance(project).getSelectedEditor()).getEditor();
                                PsiDocumentManager documentManager = PsiDocumentManager.getInstance(Objects.requireNonNull(project));
                                PsiFile psiFile = documentManager.getCachedPsiFile(Values.editor.getDocument());
                                if (psiFile instanceof PsiJavaFile) {
                                    PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
                                    getRefactorings(psiJavaFile);
                                }
                            }
                        } else {
                            System.out.println("No more refactorings.");
                            ApplicationManager.getApplication().invokeLater(() -> {
                                JBPopupFactory factory = JBPopupFactory.getInstance();

                                BalloonBuilder builder =
                                        factory.createHtmlTextBalloonBuilder("<b>No refactoring candidates found...</b>", MessageType.INFO, null);
                                builder.setFadeoutTime(0);
                                builder.setAnimationCycle(0);
                                builder.setShadow(false);
                                builder.setHideOnClickOutside(true);
                                builder.setHideOnKeyOutside(false);
                                builder.setHideOnAction(false);
                                builder.setCloseButtonEnabled(true);

                                Balloon b = builder.createBalloon();

                                b.show(RelativePoint.getNorthEastOf(WindowManager.getInstance().getStatusBar(Values.editor.getProject()).getComponent()), Balloon.Position.below);
                                MarkupModel markupModel = Values.editor.getMarkupModel();
                                markupModel.removeAllHighlighters();
                            });
                        }
                    }

                }
            }
        });

        MessageBus messageBus = Objects.requireNonNull(this.project).getMessageBus();
        messageBus.connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerAdapter() {
            @Override
            public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
                super.fileOpened(source, file);
                System.out.print("\n---------------File Opened---------------\n");
                if (Values.isActive) {
                    PsiDocumentManager manager = PsiDocumentManager.getInstance(source.getProject());
                    if (source.getSelectedEditor() != null) {
                        Editor selectedEditor = ((TextEditor)  source.getSelectedEditor()).getEditor();
                        Values.editor = selectedEditor;
                        PsiFile psiFile = manager.getCachedPsiFile(selectedEditor.getDocument());
                        if (psiFile instanceof PsiJavaFile) {
                            PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
                            Values.before = null;
                            Values.after = null;
                            Values.lastRefactoring = null;
                            Values.afterActivated = false;
                            Values.afterRefactoring = false;
                            Values.refactoringCounter = 0;
                            lastNumberRefactorings = new ArrayList<>();
                            getRefactorings(psiJavaFile);
                        }
                    }
                }
            }
        });
    }

    public void getRefactorings(PsiJavaFile psiJavaFile){
        refactoring = false;
        Values.counterCrawler = 0;
        System.out.print("\n*** Getting candidates " + psiJavaFile.getName() + " ***\n");
        getMetrics(psiJavaFile);

        boolean valid = true;
        PsiClass[] classes = psiJavaFile.getClasses();
        for (PsiClass psiClass : classes) {
            if (psiClass.isEnum() || psiClass.isInterface()) {
                valid = false;
                break;
            }
        }

        //if(valid) {
            if (Values.currentFile.complexity >= 10 || Values.currentFile.halsteadEffort >= 300 || Values.currentFile.halsteadMaintainability <= 50) {

                Values.extractMethod = new ArrayList<>();
                Values.extractVariable = new ArrayList<>();
                Values.extractClass = new ArrayList<>();
                Values.moveMethod = new ArrayList<>();
                Values.introduceParam = new ArrayList<>();
                Values.stringComp = new ArrayList<>();
                Values.inheritanceDelegation = new ArrayList<>();

                ExecutorService service = Executors.newFixedThreadPool(7);

                List<Callable<String>> callableTasks = new ArrayList<>();

                Callable<String> callableTaskEM = () -> {
                    ApplicationManager.getApplication().executeOnPooledThread(() -> {
                        ApplicationManager.getApplication().runReadAction(() -> {
                            if(lastNumberRefactorings.size() == 0) {
                                System.out.println("****EM****");
                                ExtractMethod extractMethod = new ExtractMethod(Values.editor, psiJavaFile);
                                extractMethod.run();
                            }
                            else{
                                if(lastNumberRefactorings.get(0) != 0) {
                                    System.out.println("****EM****");
                                    ExtractMethod extractMethod = new ExtractMethod(Values.editor, psiJavaFile);
                                    extractMethod.run();
                                }
                            }
                        });
                    });
                    return "DONE EXTRACT METHOD";
                };
                callableTasks.add(callableTaskEM);
                Callable<String> callableTaskEV = () -> {
                    ApplicationManager.getApplication().executeOnPooledThread(() -> {
                        ApplicationManager.getApplication().runReadAction(() -> {
                            if(lastNumberRefactorings.size() == 0) {
                                System.out.println("****EV****");
                                ExtractVariable extractVariable = new ExtractVariable(psiJavaFile, Values.editor);
                                extractVariable.run();
                            }
                            else{
                                if(lastNumberRefactorings.get(1) != 0) {
                                    System.out.println("****EV****");
                                    ExtractVariable extractVariable = new ExtractVariable(psiJavaFile, Values.editor);
                                    extractVariable.run();
                                }
                            }
                        });
                    });
                    return "DONE EXTRACT VARIABLE";
                };
                callableTasks.add(callableTaskEV);
                Callable<String> callableTaskEC = () -> {
                    ApplicationManager.getApplication().executeOnPooledThread(() -> {
                        ApplicationManager.getApplication().runReadAction(() -> {
                            if(lastNumberRefactorings.size() == 0) {
                                System.out.println("****EC****");
                                ExtractClass extractClass = new ExtractClass(psiJavaFile, Values.editor);
                                extractClass.run();
                            }
                            else{
                                if(lastNumberRefactorings.get(2) != 0) {
                                    System.out.println("****EC****");
                                    ExtractClass extractClass = new ExtractClass(psiJavaFile, Values.editor);
                                    extractClass.run();
                                }
                            }
                        });
                    });
                    return "DONE EXTRACT CLASS";
                };
                callableTasks.add(callableTaskEC);
                Callable<String> callableTaskMM = () -> {
                    ApplicationManager.getApplication().executeOnPooledThread(() -> {
                        ApplicationManager.getApplication().runReadAction(() -> {
                            if(lastNumberRefactorings.size() == 0) {
                                System.out.println("****MM****");
                                MoveMethod moveMethod = new MoveMethod(Values.editor, psiJavaFile);
                                moveMethod.run();
                            }
                            else{
                                if(lastNumberRefactorings.get(3) != 0) {
                                    System.out.println("****MM****");
                                    MoveMethod moveMethod = new MoveMethod(Values.editor, psiJavaFile);
                                    moveMethod.run();
                                }
                            }
                        });
                    });
                    return "DONE MOVE METHOD";
                };
                callableTasks.add(callableTaskMM);
                Callable<String> callableTaskIPO = () -> {
                    ApplicationManager.getApplication().executeOnPooledThread(() -> {
                        ApplicationManager.getApplication().runReadAction(() -> {
                            if(lastNumberRefactorings.size() == 0) {
                                System.out.println("****IPO****");
                                IntroduceParameterObject introduceParameterObject = new IntroduceParameterObject(Values.editor, psiJavaFile);
                                introduceParameterObject.run();
                            }
                            else{
                                if(lastNumberRefactorings.get(4) != 0) {
                                    System.out.println("****IPO****");
                                    IntroduceParameterObject introduceParameterObject = new IntroduceParameterObject(Values.editor, psiJavaFile);
                                    introduceParameterObject.run();
                                }
                            }
                        });
                    });
                    return "DONE INTRO PARAMS OBJ";
                };
                callableTasks.add(callableTaskIPO);
                Callable<String> callableTaskID = () -> {
                    ApplicationManager.getApplication().executeOnPooledThread(() -> {
                        ApplicationManager.getApplication().runReadAction(() -> {
                            if(lastNumberRefactorings.size() == 0) {
                                System.out.println("****ID****");
                                InheritanceToDelegation inheritanceToDelegation = new InheritanceToDelegation(Values.editor, psiJavaFile);
                                inheritanceToDelegation.run();
                            }
                            else{
                                if(lastNumberRefactorings.get(5) != 0) {
                                    System.out.println("****ID****");
                                    InheritanceToDelegation inheritanceToDelegation = new InheritanceToDelegation(Values.editor, psiJavaFile);
                                    inheritanceToDelegation.run();
                                }
                            }
                        });
                    });
                    return "DONE INHERITANCE TO DELEGATION";
                };
                callableTasks.add(callableTaskID);
                Callable<String> callableTaskSC = () -> {
                    ApplicationManager.getApplication().executeOnPooledThread(() -> {
                        ApplicationManager.getApplication().runReadAction(() -> {
                            if(lastNumberRefactorings.size() == 0) {
                                System.out.println("****SC****");
                                StringComparison stringComparison = new StringComparison(psiJavaFile, Values.editor);
                                stringComparison.run();
                            }
                            else{
                                if(lastNumberRefactorings.get(5) != 0) {
                                    System.out.println("****SC****");
                                    StringComparison stringComparison = new StringComparison(psiJavaFile, Values.editor);
                                    stringComparison.run();
                                }
                            }
                        });
                    });
                    return "DONE STRING COMPARISON";
                };
                callableTasks.add(callableTaskSC);

                try {
                    service.invokeAll(callableTasks);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("No more refactorings.");
                ApplicationManager.getApplication().invokeLater(() -> {
                    JBPopupFactory factory = JBPopupFactory.getInstance();

                    BalloonBuilder builder =
                            factory.createHtmlTextBalloonBuilder("<b>No refactoring candidates found...</b>", MessageType.INFO, null);
                    builder.setFadeoutTime(0);
                    builder.setAnimationCycle(0);
                    builder.setShadow(false);
                    builder.setHideOnClickOutside(true);
                    builder.setHideOnKeyOutside(false);
                    builder.setHideOnAction(false);
                    builder.setCloseButtonEnabled(true);

                    Balloon b = builder.createBalloon();

                    b.show(RelativePoint.getNorthEastOf(WindowManager.getInstance().getStatusBar(Values.editor.getProject()).getComponent()), Balloon.Position.below);
                    MarkupModel markupModel = Values.editor.getMarkupModel();
                    markupModel.removeAllHighlighters();
                });
            }
        //}
        /*else {
            System.out.println("No more refactorings.");
            ApplicationManager.getApplication().invokeLater(() -> {
                JBPopupFactory factory = JBPopupFactory.getInstance();

                BalloonBuilder builder =
                        factory.createHtmlTextBalloonBuilder("<b>No refactoring candidates found...</b>", MessageType.INFO, null);
                builder.setFadeoutTime(0);
                builder.setAnimationCycle(0);
                builder.setShadow(false);
                builder.setHideOnClickOutside(true);
                builder.setHideOnKeyOutside(false);
                builder.setHideOnAction(false);
                builder.setCloseButtonEnabled(true);

                Balloon b = builder.createBalloon();

                b.show(RelativePoint.getNorthEastOf(WindowManager.getInstance().getStatusBar(Values.editor.getProject()).getComponent()), Balloon.Position.below);
                MarkupModel markupModel = Values.editor.getMarkupModel();
                markupModel.removeAllHighlighters();
            });
        }*/
    }

    private void saveMetrics(){
        Values.refactoringCounter++;

        PsiDocumentManager documentManager = PsiDocumentManager.getInstance(Objects.requireNonNull(project));
        if (FileEditorManager.getInstance(project).getSelectedEditor() != null) {
            Values.editor = ((TextEditor) FileEditorManager.getInstance(project).getSelectedEditor()).getEditor();
            PsiFile psiFile = documentManager.getCachedPsiFile(Values.editor.getDocument());

            if (psiFile instanceof PsiJavaFile) {
                PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
                UtilitiesOverall utils = new UtilitiesOverall();
                Values.projectName = project.getName();

                if (Values.db != null) {
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    LocalDateTime now = LocalDateTime.now();

                    String endPoint = Values.projectName + "/" + dtf.format(now) + "/Refactoring/" + utils.removeExtension(psiJavaFile.getName()) +
                            "/Refactoring " + Values.refactoringCounter + "/" + Values.lastRefactoring.type;

                    if (Values.lastRefactoring != null) {
                        HashMap<String, Object> dateHour = new HashMap<>();
                        DateTimeFormatter dtfRefactoring = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        LocalDateTime dateHourNow = LocalDateTime.now();
                        dateHour.put("Moment", dtfRefactoring.format(dateHourNow));
                        Values.db.child(endPoint + "/dateHour").setValueAsync(dateHour);

                        endPoint += "/metrics";

                        MetricsUtils metricsUtils = new MetricsUtils();
                        switch (Values.lastRefactoring.type) {
                            case "Extract Method": {
                                HashMap<String, Object> itemsFileBefore = metricsUtils.getValuesMetricsFile(Values.before);
                                Values.db.child(endPoint + "/FileBefore").setValueAsync(itemsFileBefore);
                                HashMap<String, Object> itemsBefore = metricsUtils.getValuesMetrics(Values.before);
                                Values.db.child(endPoint + "/before").setValueAsync(itemsBefore);
                                try {
                                    Values.after = new FileMetrics(Values.editor, psiJavaFile);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                                HashMap<String, Object> itemsAfter = metricsUtils.getValuesMetrics(Values.after);
                                Values.db.child(endPoint + "/after").setValueAsync(itemsAfter);
                                HashMap<String, Object> itemsFileAfter = metricsUtils.getValuesMetricsFile(Values.after);
                                Values.db.child(endPoint + "/FileAfter").setValueAsync(itemsFileAfter);
                                break;
                            }
                            case "Extract Variable":
                            case "Introduce Parameter Object":
                            case "String Comparison": {
                                HashMap<String, Object> itemsFileBefore = metricsUtils.getValuesMetricsFile(Values.before);
                                Values.db.child(endPoint + "/FileBefore").setValueAsync(itemsFileBefore);
                                HashMap<String, Object> itemsBefore = metricsUtils.getValuesMetrics(Values.before);
                                Values.db.child(endPoint + "/before").setValueAsync(itemsBefore);
                                try {
                                    Values.after = new FileMetrics(Values.editor, psiJavaFile);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                                HashMap<String, Object> itemsAfter = metricsUtils.getValuesMetrics(Values.after);
                                Values.db.child(endPoint + "/after").setValueAsync(itemsAfter);
                                HashMap<String, Object> itemsFileAfter = metricsUtils.getValuesMetricsFile(Values.after);
                                Values.db.child(endPoint + "/FileAfter").setValueAsync(itemsFileAfter);
                                break;
                            }
                            case "Extract Class":
                            case "Move Method":
                            case "Inheritance To Delegation": {
                                HashMap<String, Object> itemsClassBefore = metricsUtils.getValuesMetricsOldClass(Values.lastRefactoring.metrics);
                                Values.db.child(endPoint + "/ClassBefore").setValueAsync(itemsClassBefore);
                                HashMap<String, Object> itemsFileBefore = metricsUtils.getValuesMetricsFile(Values.before);
                                Values.db.child(endPoint + "/FileBefore").setValueAsync(itemsFileBefore);
                                try {
                                    Values.after = new FileMetrics(Values.editor, psiJavaFile);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }

                                HashMap<String, Object> itemsClassAfter = metricsUtils.getValuesMetricsOldClass(Values.after);
                                Values.db.child(endPoint + "/ClassAfter").setValueAsync(itemsClassAfter);
                                HashMap<String, Object> itemsFileAfter = metricsUtils.getValuesMetricsFile(Values.after);
                                Values.db.child(endPoint + "/FileAfter").setValueAsync(itemsFileAfter);
                                break;
                            }
                        }
                    } else {
                        try {
                            Values.after = new FileMetrics(Values.editor, psiJavaFile);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        Values.before = new FileMetrics(Values.after);
                    }
                }
            }
        }
    }

    public void getMetrics(PsiJavaFile psiJavaFile){
        FileMetrics metrics = null;

        if(Values.before != null) {
            if(Values.after != null){
                if(Values.after.fileName.equals(psiJavaFile.getName()))
                    metrics = new FileMetrics(Values.after);
                else {
                    try {
                        metrics = new FileMetrics(Values.editor, psiJavaFile);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            else {
                if(Values.before.fileName.equals(psiJavaFile.getName()))
                    metrics = new FileMetrics(Values.before);
                else {
                    try {
                        metrics = new FileMetrics(Values.editor, psiJavaFile);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
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
                try {
                    metrics = new FileMetrics(Values.editor, psiJavaFile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Values.openedFiles.add(metrics);
            }
        }

        if(metrics != null) {
            Values.before = new FileMetrics(metrics);
            Values.currentFile = Values.before;
        }

    }

    private static String generateRandomString() {
        int length = 10; // Length of the random string
        String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(characters.length());
            char randomChar = characters.charAt(randomIndex);
            sb.append(randomChar);
        }

        return sb.toString();
    }

    private static int generateRandomNumber(int diff){
        int number = diff;
        while(number == diff){
            Random random = new Random();
            number = random.nextInt(7) + 1;
        }
        return number;
    }

    private void applyRefactorings(int number){
        boolean done = true;
        if(number == 1){
            if(Values.extractMethod.size() > 0) extractMethod(Values.extractMethod.get(0));
            else done = false;
        }
        if(number == 2){
            if(Values.extractVariable.size() > 0) extractVariable(Values.extractVariable.get(0));
            else done = false;
        }
        if(number == 3){
            if(Values.extractClass.size() > 0) extractClass(Values.extractClass.get(0));
            else done = false;
        }
        if(number == 4){
            if(Values.moveMethod.size() > 0) moveMethod(Values.moveMethod.get(0));
            else done = false;
        }
        if(number == 5){
            if(Values.introduceParam.size() > 0) introduceParamterObject(Values.introduceParam.get(0));
            else done = false;
        }
        if(number == 6){
            if(Values.inheritanceDelegation.size() > 0) inheritanceToDelegation(Values.inheritanceDelegation.get(0));
            else done = false;
        }
        if(number == 7){
            if(Values.stringComp.size() > 0) stringComparison(Values.stringComp.get(0));
            else done = false;
        }

        if(!done){
            int newNumber = generateRandomNumber(number);
            applyRefactorings(newNumber);
        }
    }

    private PsiElement[] getElements(ExtractMethodCandidate candidate) {
        ArrayList<PsiElement> elements = new ArrayList<>(candidate.nodes);

        PsiElement[] psiElements = new PsiElement[elements.size()];
        for (int i = 0; i < psiElements.length; i++)
            psiElements[i] = elements.get(i);

        return psiElements;
    }

    private void extractMethod(ExtractMethodCandidate candidate){
        Application application = ApplicationManager.getApplication();
        if (application.isDispatchThread()) applyExtractMethod(candidate);
        else {
            application.invokeLater(() -> {
                WriteCommandAction.runWriteCommandAction(project, () -> {
                    applyExtractMethod(candidate);
                });
            });
        }
    }

    private void extractVariable(ExtractVariableCandidate candidate){
        Application application = ApplicationManager.getApplication();
        if (application.isDispatchThread()) applyExtractVariable(candidate);
        else {
            application.invokeLater(() -> {
                WriteCommandAction.runWriteCommandAction(project, () -> {
                    applyExtractVariable(candidate);
                });
            });
        }
    }

    private void extractClass(ExtractClassCandidate candidate) {
        Application application = ApplicationManager.getApplication();
        if (application.isDispatchThread()) applyExtractClass(candidate);
        else {
            application.invokeLater(() -> {
                WriteCommandAction.runWriteCommandAction(project, () -> {
                    applyExtractClass(candidate);
                });
            });
        }
    }

    private void moveMethod(MoveMethodCandidate candidate){
        Application application = ApplicationManager.getApplication();
        if (application.isDispatchThread()) applyMoveMethod(candidate);
        else {
            application.invokeLater(() -> {
                WriteCommandAction.runWriteCommandAction(project, () -> {
                    applyMoveMethod(candidate);
                });
            });
        }
    }

    private void introduceParamterObject(IntroduceParamObjCandidate candidate){
        Application application = ApplicationManager.getApplication();
        if (application.isDispatchThread()) applyIntroParamObj(candidate);
        else {
            application.invokeLater(() -> {
                WriteCommandAction.runWriteCommandAction(project, () -> {
                    applyIntroParamObj(candidate);
                });
            });
        }
    }

    private void inheritanceToDelegation(InheritanceToDelegationCandidate candidate){
        Application application = ApplicationManager.getApplication();
        if (application.isDispatchThread()) applyReplaceInheritance(candidate);
        else {
            application.invokeLater(() -> {
                WriteCommandAction.runWriteCommandAction(project, () -> {
                    applyReplaceInheritance(candidate);
                });
            });
        }
    }

    private void stringComparison(StringComparisonCandidate candidate){
        Application application = ApplicationManager.getApplication();
        if (application.isDispatchThread()) applyStringComparison(candidate);
        else {
            application.invokeLater(() -> {
                WriteCommandAction.runWriteCommandAction(project, () -> {
                    applyStringComparison(candidate);
                });
            });
        }
    }

    public void applyExtractMethod(ExtractMethodCandidate candidate){
        System.out.println("Applying Extract Method");

        PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
        Document document = Values.editor.getDocument();
        PsiFile psiFile = psiDocumentManager.getPsiFile(document);

        PsiElement[] elements = getElements(candidate);
        // Step 2: Create a new method
        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(project);
        String name = generateRandomString();
        PsiMethod newMethod = elementFactory.createMethod(name, PsiType.VOID);

        for (PsiStatement node : candidate.nodes) {
            newMethod.getBody().add(node);
        }

        if (psiDocumentManager.getCachedDocument(psiFile) != null) {
            psiDocumentManager.commitDocument(psiDocumentManager.getCachedDocument(psiFile));
        }

        if(candidate.method.getContainingClass() != null){

                candidate.method.getContainingClass().add(newMethod);

            if (psiDocumentManager.getCachedDocument(psiFile) != null) {
                psiDocumentManager.commitDocument(psiDocumentManager.getCachedDocument(psiFile));
            }

                PsiStatement methodCallStatement = elementFactory.createStatementFromText(name + "();", null);

                candidate.nodes.get(0).replace(methodCallStatement);
                for (int i = 1; i < candidate.nodes.size(); i++) {
                    candidate.nodes.get(i).delete();
                }

            if (psiDocumentManager.getCachedDocument(psiFile) != null) {
                psiDocumentManager.commitDocument(psiDocumentManager.getCachedDocument(psiFile));
            }

            PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(candidate.sourceFile.getViewProvider().getDocument());
            Values.lastRefactoring = new LastRefactoring(candidate.method, "Extract Method", elements, Values.currentFile, 10, 0);
                System.out.println("Finished Refactoring");

        }
    }

    public void applyExtractVariable(ExtractVariableCandidate candidate){
        System.out.println("Applying Extract Variable");
        PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
        Document document = Values.editor.getDocument();
        PsiFile psiFile = psiDocumentManager.getPsiFile(document);

        for (PsiMethodCallExpression psiExpression : PsiTreeUtil.findChildrenOfType(candidate.originalMethod, PsiMethodCallExpression.class)) {
            System.out.println(psiExpression.getText());
            System.out.println(candidate.node.getText());
            if(psiExpression.getText().contains(candidate.node.getText())){
                System.out.println("olha entrei aqui");
                PsiType variableType = psiExpression.getMethodExpression().resolve() != null ? ((PsiMethod)(psiExpression.getMethodExpression().resolve())).getReturnType() : PsiType.BOOLEAN;
                String variableName = generateRandomString();

                String variableDeclaration = variableType.getCanonicalText() + " " + variableName + " = " + psiExpression.getText() + ";";

                    String text = psiFile.getText();
                    int line = Values.editor.offsetToLogicalPosition(psiExpression.getTextOffset()).line;
                    String newText = "";
                    for (int i = 0; i < text.split("\n").length; i++) {
                        String s = text.split("\n")[i];
                        if (i != line) newText += s + "\n";
                        else {
                            newText += variableDeclaration + "\n";
                            newText += s.replace(psiExpression.getText(), variableName) + "\n";
                        }
                    }

                    psiFile.getViewProvider().getDocument().setText(newText);
                    PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(psiFile.getViewProvider().getDocument());
                    Values.lastRefactoring = new LastRefactoring(candidate.originalMethod, "Extract Variable", new PsiElement[]{candidate.node}, Values.currentFile, 10, 0);
                    System.out.println("Finished Refactoring");
            }
        }
    }

    public void applyExtractClass(ExtractClassCandidate candidate){
        System.out.println("Applying Extract Class");
        Set<PsiField> fields = new HashSet<>(candidate.targetAttributes);
        Set<PsiMethod> methods = new HashSet<>(candidate.targetMethods);

        String name = generateRandomString();
        ExtractClassRefactoring extract = new ExtractClassRefactoring(candidate.file, candidate.targetClass,
                fields, methods, new HashSet<>(), name);

        extract.apply();

        PsiElement[] elements = new PsiElement[candidate.targetEntities.size()];
        for (int i = 0; i < elements.length; i++) {
            elements[i] = candidate.targetEntities.get(i);
        }

        PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(candidate.file.getViewProvider().getDocument());
        Values.lastRefactoring = new LastRefactoring(candidate.targetClass, elements, Values.currentFile, 10, 0, "Extract Class");
        System.out.println("Finished Refactoring");
    }

    public void applyMoveMethod(MoveMethodCandidate candidate){
        System.out.println("Applying Move Method");
        PsiElementFactory factory = PsiElementFactory.getInstance(project);

        // Create a new method in the target class
        PsiMethod methodCopy = factory.createMethod(candidate.method.getName(), candidate.method.getReturnType());
        methodCopy.getModifierList().replace(candidate.method.getModifierList());
        methodCopy.getParameterList().replace(candidate.method.getParameterList());
        if(candidate.method.getThrowsList()!=null)
            methodCopy.getThrowsList().replace(candidate.method.getThrowsList());
        methodCopy.getBody().replace(candidate.method.getBody());
        candidate.method.delete();
        candidate.targetClass.add(methodCopy);

        for (PsiMethodCallExpression methodCall : PsiTreeUtil.findChildrenOfType(candidate.originalClass, PsiMethodCallExpression.class)) {
            PsiReferenceExpression methodReference = methodCall.getMethodExpression();
            PsiMethod resolvedMethod = (PsiMethod) methodReference.resolve();

            if (resolvedMethod != null && resolvedMethod.equals(methodCopy)) {
                PsiExpression classReference = methodReference.getQualifierExpression();

                if (classReference != null) {
                    classReference.delete();
                }

                PsiReferenceExpression newReference = factory.createReferenceExpression(candidate.targetClass);
                newReference.getQualifierExpression().replace(classReference);

                methodReference.replace(newReference);
            }
        }

        PsiElement[] elements = new PsiElement[candidate.method.getBody().getStatementCount()];
        for (int i = 0; i < candidate.method.getBody().getStatements().length; i++) {
            elements[i] = candidate.method.getBody().getStatements()[i];
        }

        Values.lastRefactoring = new LastRefactoring(candidate.method, candidate.originalClass, elements, Values.currentFile, 10, 0, "Move Method");
        System.out.println("Finished Refactoring");
    }

    public void applyIntroParamObj(IntroduceParamObjCandidate candidate){
        System.out.println("Applying Introduce Parameter Object");

        PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
        Document document = Values.editor.getDocument();
        PsiFile psiFile = psiDocumentManager.getPsiFile(document);

        List<PsiParameter> parametersToReplace = candidate.originalParameters;
        PsiParameterList parameterList = candidate.method.getParameterList();

        PsiElementFactory factory = JavaPsiFacade.getElementFactory(project); // 'project' represents the current project

        String parameterName = generateRandomString();
        PsiClass parameterObjectClass = factory.createClass(parameterName);
        PsiClassType parameterObjectType = factory.createType(parameterObjectClass);

        for (PsiParameter parameter : parametersToReplace) {
            // Create fields in the parameter object class corresponding to the replaced parameters
            PsiField field = factory.createField(parameter.getName(), parameter.getType());
            parameterObjectClass.add(field);
        }
        if (psiDocumentManager.getCachedDocument(psiFile) != null) {
            psiDocumentManager.commitDocument(psiDocumentManager.getCachedDocument(psiFile));
        }

        PsiParameter parameterObject = factory.createParameter(parameterName, parameterObjectType);
        parameterList.add(parameterObject);

        if (psiDocumentManager.getCachedDocument(psiFile) != null) {
            psiDocumentManager.commitDocument(psiDocumentManager.getCachedDocument(psiFile));
        }

        // Replace the occurrences of individual parameters with the parameter object references
        for (PsiParameter parameter : parametersToReplace) {
            parameter.delete(); // Remove the individual parameter declaration
            // Replace the occurrences within the method body with the parameter object reference
            PsiElement methodBody = candidate.method.getBody();
            if (methodBody != null) {
                methodBody.accept(new JavaRecursiveElementVisitor() {
                    @Override
                    public void visitReferenceExpression(PsiReferenceExpression expression) {
                        super.visitReferenceExpression(expression);
                        PsiElement referent = expression.resolve();
                        if (referent instanceof PsiParameter && referent.equals(parameter)) {
                            PsiElement replacement = factory.createExpressionFromText(parameterName + "." + parameter.getName(), parameter);
                            expression.replace(replacement);
                        }
                    }
                });
            }
        }

        if (psiDocumentManager.getCachedDocument(psiFile) != null) {
            psiDocumentManager.commitDocument(psiDocumentManager.getCachedDocument(psiFile));
        }

        PsiFile file = candidate.method.getContainingFile();
        PsiClass topLevelClass = PsiTreeUtil.getChildOfType(file, PsiClass.class);

        if (topLevelClass != null) {
            topLevelClass.add(parameterObjectClass);
        } else {
            file.add(parameterObjectClass);
        }

        if (psiDocumentManager.getCachedDocument(psiFile) != null) {
            psiDocumentManager.commitDocument(psiDocumentManager.getCachedDocument(psiFile));
        }

        PsiElement[] elements = new PsiElement[candidate.originalParameters.size()];
        for (int i = 0; i < candidate.originalParameters.size(); i++) {
            elements[i] = candidate.originalParameters.get(i);
        }

        Values.lastRefactoring = new LastRefactoring(candidate.method, "Introduce Parameter Object", elements, Values.currentFile, 10, 0);
        System.out.println("Finished Refactoring");
    }

    public void applyReplaceInheritance(InheritanceToDelegationCandidate candidate){
        System.out.println("Applying Inheritance to Delegation");

        PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
        Document document = Values.editor.getDocument();
        PsiFile psiFile = psiDocumentManager.getPsiFile(document);

        PsiClass targetClass = candidate._class;
        PsiClass superClass = candidate.target;
        PsiMethod[] superMethods = superClass.getMethods();

        PsiElementFactory factory = JavaPsiFacade.getElementFactory(project); // 'project' represents the current project
        // Generate a field type for the superclass instance
        PsiClassType superClassType = factory.createType(superClass);

        // Generate a field in the target class to hold the instance of the superclass
        PsiField field = factory.createField(superClass.getName().toLowerCase(Locale.ROOT), superClassType);
        targetClass.add(field);

        if (psiDocumentManager.getCachedDocument(psiFile) != null) {
            psiDocumentManager.commitDocument(psiDocumentManager.getCachedDocument(psiFile));
        }

        for (PsiMethod superMethod : superMethods) {

            if (!superMethod.isConstructor()){
                PsiMethod delegateMethod = factory.createMethod(superMethod.getName(), superMethod.getReturnType());
            PsiCodeBlock methodBody = delegateMethod.getBody();
            if (methodBody != null) {
                PsiStatement returnStatement = factory.createStatementFromText("return " + field.getName() + "." + superMethod.getName() + "();", delegateMethod);
                methodBody.add(returnStatement);
            }
            if (psiDocumentManager.getCachedDocument(psiFile) != null) {
                psiDocumentManager.commitDocument(psiDocumentManager.getCachedDocument(psiFile));
            }
            targetClass.add(delegateMethod);
            if (psiDocumentManager.getCachedDocument(psiFile) != null) {
                psiDocumentManager.commitDocument(psiDocumentManager.getCachedDocument(psiFile));
            }
        }
        }

        // Remove the inheritance
        if (superClass != null) {
            PsiJavaCodeReferenceElement superClassReference = targetClass.getExtendsList().getReferenceElements()[0];
            superClassReference.delete();
            if (psiDocumentManager.getCachedDocument(psiFile) != null) {
                psiDocumentManager.commitDocument(psiDocumentManager.getCachedDocument(psiFile));
            }
        }

        PsiField[] fields = candidate._class.getFields();
        PsiMethod[] methods = candidate._class.getMethods();

        ArrayList<PsiElement> elementsAux = new ArrayList<>();

        Collections.addAll(elementsAux, fields);
        Collections.addAll(elementsAux, methods);
        PsiElement[] elements = new PsiElement[elementsAux.size()];
        for (int i=0; i<elementsAux.size(); i++) {
            elements[i] = elementsAux.get(i);
        }
        Values.lastRefactoring = new LastRefactoring(candidate._class, elements, Values.currentFile, 10, 0, "Inheritance To Delegation");
        System.out.println("Finished Refactoring");
    }

    public void applyStringComparison(StringComparisonCandidate candidate){
        System.out.println("Applying String Comparison");
        PsiBinaryExpression binaryExpression = candidate.node;
        PsiExpression lExpr = binaryExpression.getLOperand();
        PsiExpression rExpr = binaryExpression.getROperand();

        PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
        String newText = lExpr.getText() + ".equals(" + rExpr.getText() + ")";
        PsiMethodCallExpression equalsCall =
                (PsiMethodCallExpression) factory.createExpressionFromText(newText, null);

        equalsCall.getMethodExpression().getQualifierExpression().replace(lExpr);
        equalsCall.getArgumentList().getExpressions()[0].replace(rExpr);
        binaryExpression.replace(equalsCall);

        Values.lastRefactoring = new LastRefactoring(candidate.originalMethod, "String Comparison", new PsiElement[]{binaryExpression}, Values.currentFile, 10, 0);
        System.out.println("Finished Refactoring");
    }

    public void activateFirebase(){
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
        InputStream serviceAccount =Thread.currentThread().getContextClassLoader().getResourceAsStream("firebaseConfig/bot.json");

        FirebaseOptions options = null;
        try {
            options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://bot-liverefactoring-default-rtdb.firebaseio.com")
                    .build();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        FirebaseApp.initializeApp(options);
        Values.db = FirebaseDatabase.getInstance().getReference();
    }
}
