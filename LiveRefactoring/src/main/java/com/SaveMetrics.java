package com;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.metrics.FileMetrics;
import com.utils.MetricsUtils;
import com.utils.ThresholdsCandidates;
import com.utils.Utilities;
import com.utils.Values;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class SaveMetrics extends AnAction {
    private Utilities utils = new Utilities();
    //private final String urlFirebase = this.utils.getURL() + "firebaseConfig/ServiceAccount.json";
    private DatabaseReference database = null;
    public int version = 1;

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);

        if(!Values.alreadyDone) {
            activateFirebase();
            Values.editor = editor;
            Values.db = database;
            Values.event = e;
            Values.alreadyDone = true;
        }

        if(Values.db != null) {

            PsiDocumentManager documentManager = PsiDocumentManager.getInstance(Objects.requireNonNull(e.getProject()));
            final PsiFile psiFile = documentManager.getCachedPsiFile(Values.editor.getDocument());
            final PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;

            Date dateNow = new Date();
            String date = dateNow.toString();
            Instant now = Instant.now();
            Duration timeElapsed = Duration.between(Values.startTime, now);
            String projectName = editor.getProject().getName();
            String endPoint = ThresholdsCandidates.username + "/" + projectName + "/version " + version + "/" + removeExtension(psiJavaFile.getName()) + "/" + date + "/Coding";

            MetricsUtils metricsUtils = new MetricsUtils();
            HashMap<String, Object> itemsBefore = metricsUtils.getValuesMetrics(Values.before);
            Values.after = new FileMetrics(Values.editor, psiJavaFile);
            HashMap<String, Object> itemsAfter = metricsUtils.getValuesMetrics(Values.after);

            HashMap<String, Object> time = new HashMap<>();
            Values.db.child(endPoint + "/before").setValueAsync(itemsBefore);
            Values.db.child(endPoint + "/after").setValueAsync(itemsAfter);
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
    }

    private String removeExtension(String fileName){
        fileName = fileName.substring(0, fileName.lastIndexOf('.'));
        return fileName;
    }
}
