package com;

import com.analysis.metrics.FileMetrics;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandEvent;
import com.intellij.openapi.command.CommandListener;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.util.messages.MessageBus;
import com.utils.UtilitiesOverall;
import com.utils.importantValues.Values;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StartTool extends AnAction {
    public boolean enable = false;
    public boolean started = false;
    private final UtilitiesOverall utils = new UtilitiesOverall();
    private boolean done = false;
    private DatabaseReference database = null;
    public int active = 0;
    public boolean undoActivated = false;
    public Instant startCoding = null;
    public int lastActive = 0;

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if(FileEditorManager.getInstance(project).getSelectedEditor() != null) {
            Values.editor = ((TextEditor) FileEditorManager.getInstance(project).getSelectedEditor()).getEditor();
        }

            if (!done) {
                getGitInfo(project);
                activateFirebase();
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime now = LocalDateTime.now();
                Values.token = dtf.format(now);

                Runnable liveAnalysis = new Runnable() {

                    public void run() {
                        if (e.getPresentation().getText().equals("Stop Analysis") && Values.isActive) {
                            if (active != 0 && active != lastActive) {
                                lastActive = active;
                                ApplicationManager.getApplication().runReadAction(() -> {
                                    try {
                                        if (!Values.isRefactoring) {
                                            if(FileEditorManager.getInstance(e.getProject()).getSelectedEditor() != null){
                                                Values.editor = ((TextEditor) FileEditorManager.getInstance(e.getProject()).getSelectedEditor()).getEditor();
                                                PsiDocumentManager documentManager = PsiDocumentManager.getInstance(Objects.requireNonNull(e.getProject()));
                                                PsiFile psiFile = documentManager.getCachedPsiFile(Values.editor.getDocument());
                                                if (psiFile instanceof PsiJavaFile) {
                                                    System.out.print("\n=========== New Event (After) ===========\n");
                                                    Values.after = new FileMetrics(Values.editor, (PsiJavaFile) psiFile);
                                                    Values.isFirst = false;
                                                    active = 0;
                                                    lastActive = 0;
                                                    utils.startActions((PsiJavaFile) psiFile);
                                                }
                                            }
                                        }
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                });
                            }
                        }
                    }
                };

                ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
                executor.scheduleAtFixedRate(liveAnalysis, 0, Values.numSeconds, TimeUnit.SECONDS);

                //undos
                CommandListener listener = new CommandListener() {
                    @Override
                    public void commandFinished(@NotNull CommandEvent event) {
                        if (event != null && Values.isActive && e.getPresentation().getText().equals("Stop Analysis")) {
                            CommandListener.super.commandFinished(event);
                            if (event.getCommandName().equals("Undo last operation") || event.getCommandName().contains("Undo")) {
                                if (Values.db != null && Values.currentFile != null && Values.afterRefactoring && Values.afterActivated) {
                                    undoActivated = true;
                                    Values.afterActivated = false;
                                    Values.afterRefactoring = false;
                                    System.out.print("\nSave Undo Action\n");
                                    if (Values.lastRefactoring != null) {
                                        Values.lastRefactoring = Values.allRefactorings.get(Values.allRefactorings.size() - 1);
                                        if (Values.allRefactorings.size() > 0) {
                                            String type = Values.allRefactorings.get(Values.allRefactorings.size() - 1).type;
                                            if (type == "Extract Class") Values.allEC.remove(Values.allEC.size() - 1);
                                            else if (type == "Extract Method")
                                                Values.allEM.remove(Values.allEM.size() - 1);
                                            else if (type == "Extract Variable")
                                                Values.allEV.remove(Values.allEV.size() - 1);
                                            else if (type == "Move Method")
                                                Values.allMM.remove(Values.allMM.size() - 1);
                                            else if (type == "Introduce Parameter Object")
                                                Values.allIPO.remove(Values.allIPO.size() - 1);
                                            else if (type == "Inheritance to delegation")
                                                Values.allID.remove(Values.allID.size() - 1);
                                            else if (type == "String Comparison")
                                                Values.allSC.remove(Values.allSC.size() - 1);
                                            Values.allRefactorings.remove(Values.allRefactorings.size() - 1);
                                        }

                                        Instant now = Instant.now();
                                        String endPoint = Values.projectName + "/" + Values.username + "/" + Values.token + "/Undo/Refactoring " + Values.refactoringCounter + "/" +
                                                Values.lastRefactoring.type;
                                        Duration betweenCoding = Duration.between(Values.endRefactoring, now);
                                        HashMap<String, Object> codingTime = new HashMap<>();
                                        codingTime.put("Seconds Since Refactoring", betweenCoding.getSeconds());
                                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                                        LocalDateTime nowDate = LocalDateTime.now();
                                        codingTime.put("Date Hour", dtf.format(nowDate));
                                        Values.db.child(endPoint + "/Time").setValueAsync(codingTime);

                                        PsiDocumentManager manager = PsiDocumentManager.getInstance(event.getProject());
                                        Editor selectedEditor = ((TextEditor) Objects.requireNonNull(FileEditorManager.getInstance(event.getProject()).getSelectedEditor())).getEditor();
                                        Values.editor = selectedEditor;
                                        PsiFile psiFile = manager.getCachedPsiFile(selectedEditor.getDocument());

                                        boolean exists = false;
                                        for (int i = 0; i < Values.openedFiles.size(); i++) {
                                            if (Values.openedFiles.get(i).fileName.equals(Values.after.fileName)) {
                                                exists = true;
                                                Values.openedFiles.set(i, new FileMetrics(Values.after));
                                                break;
                                            }
                                        }
                                        if (!exists)
                                            Values.openedFiles.add(new FileMetrics(Values.after));

                                        Values.isFirst = true;
                                        try {
                                            if (psiFile instanceof PsiJavaFile) {
                                                Values.before = new FileMetrics(Values.editor, (PsiJavaFile) psiFile);
                                                Values.after = null;
                                                utils.startActions((PsiJavaFile) psiFile);
                                            }
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                    }
                };
                CommandProcessor.getInstance().addCommandListener(listener);

                done = true;
            }

            if (!enable) {
                enable = true;
                e.getPresentation().setText("Stop Analysis");
                UtilitiesOverall utils = new UtilitiesOverall();

                if (!started) {
                    PsiManager.getInstance(project).addPsiTreeChangeListener(new PsiTreeChangeListener() {
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
                            active++;
                        }

                        @Override
                        public void childMoved(@NotNull PsiTreeChangeEvent event) {
                        }

                        @Override
                        public void propertyChanged(@NotNull PsiTreeChangeEvent event) {

                        }
                    });

                    MessageBus messageBus = Objects.requireNonNull(project).getMessageBus();
                    messageBus.connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerAdapter() {
                        @Override
                        public void selectionChanged(@NotNull FileEditorManagerEvent event) {
                            System.out.print("\n---------------Selection Changed---------------\n");

                            for (RangeHighlighter rangeHighlighter : Values.gutters) {
                                rangeHighlighter.setGutterIconRenderer(null);
                            }
                            if (enable) {
                                PsiDocumentManager manager = PsiDocumentManager.getInstance(event.getManager().getProject());
                                if(FileEditorManager.getInstance(event.getManager().getProject()).getSelectedEditor() != null) {
                                    Editor selectedEditor = ((TextEditor) FileEditorManager.getInstance(project).getSelectedEditor()).getEditor();
                                    Values.editor = selectedEditor;
                                    PsiFile psiFile = manager.getCachedPsiFile(selectedEditor.getDocument());
                                    if (psiFile instanceof PsiJavaFile) {
                                        PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
                                        Values.before = null;
                                        Values.after = null;
                                        active = 0;
                                        Values.isRefactoring = false;
                                        Values.betweenRefactorings = null;
                                        Values.lastRefactoring = null;
                                        Values.isFirst = true;
                                        Values.afterActivated = false;
                                        Values.afterRefactoring = false;
                                        undoActivated = false;
                                        startCoding = null;
                                        Values.endRefactoring = null;
                                        boolean exists = false;
                                        try {
                                            Values.before = new FileMetrics(Values.editor, (PsiJavaFile) psiFile);
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                        }
                                        for (int i = 0; i < Values.openedFiles.size(); i++) {
                                            if (Values.openedFiles.get(i).fileName.equals(psiJavaFile.getName())) {
                                                exists = true;
                                                Values.openedFiles.set(i, Values.before);
                                                break;
                                            }
                                        }
                                        if (!exists) {
                                            Values.openedFiles.add(Values.before);
                                        }

                                        Values.isFirst = true;
                                        try {
                                            Values.after = null;
                                            utils.startActions((PsiJavaFile) psiFile);
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                    });

                    started = true;
                }

                PsiDocumentManager documentManager = PsiDocumentManager.getInstance(Objects.requireNonNull(e.getProject()));
                Values.isActive = true;
                Values.event = e;
                Values.db = database;
                if(Values.editor != null) {
                    PsiFile psiFile = documentManager.getCachedPsiFile(Values.editor.getDocument());
                    if (psiFile instanceof PsiJavaFile) {
                        PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
                        Values.isFirst = true;
                        utils.startActions(psiJavaFile);
                    }
                }

            } else {
                e.getPresentation().setText("Start Analysis");
                for (RangeHighlighter rangeHighlighter : Values.gutters) {
                    rangeHighlighter.setGutterIconRenderer(null);
                }
                enable = false;
                Values.isActive = false;
            }
    }

    private void getGitInfo(Project project) {
        String dir = project.getBasePath();
        final String username = randomString();
        try {
            Process process = Runtime.getRuntime().exec("git config user.name");
            process.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            Values.username = reader.readLine();
            if(Values.username.length() == 0 || Values.username.equals("username"))
                Values.username = username;
        } catch (Exception err) {
            Values.username = username;
        }

        if(Values.username.length() == 0 || Values.username.equals("username"))
            Values.username = username;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(dir))) {
            boolean foundConfig = false;
            for (Path path : stream) {
                if (Files.isDirectory(path)) {
                    File file = new File(path.toUri());
                    File[] files = file.listFiles();
                    for (File f : files) {
                        if(f.getName().equals("config")) {
                            InputStream inputStream = new FileInputStream(f.getAbsolutePath());
                            String contents = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                            for (String line : contents.split("\n")) {
                                if(line.contains("url = ")) {
                                    Values.projectURL = line.split("=")[1].trim();
                                    String projName = Values.projectURL.split("/")[Values.projectURL.split("/").length-2];
                                    if(projName.contains("git@github"))
                                        projName = projName.split(":")[1];
                                    projName += "-" + Values.projectURL.split("/")[Values.projectURL.split("/").length-1].substring(0, Values.projectURL.split("/")[Values.projectURL.split("/").length-1].lastIndexOf('.'));
                                    Values.projectName = projName;
                                    break;
                                }
                            }
                            foundConfig = true;
                            break;
                        }
                    }
                }

                if(foundConfig)
                    break;
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        if(Values.projectName.equals("unknown"))
            Values.projectName = Values.username + "-" + project.getName();

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
}
