package com.utils.firebase.data;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.FirebaseDatabase;
import com.utils.firebase.util.FyreLogger;

import java.io.FileInputStream;
import java.io.IOException;

@SuppressWarnings("SpellCheckingInspection")
public class FirebaseManager {
    private final String TAG = "FirebaseManager";
    private FyreLogger fyreLogger;

    public FirebaseManager() {
    }

    public FirebaseManager(FyreLogger fyreLogger) {
        this.fyreLogger = fyreLogger;
    }

    public void init(String configPath, String databaseUrl) throws IOException {
        FileInputStream serviceAccount =
                new FileInputStream(configPath);

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl("https://livemr-b5eb2-default-rtdb.firebaseio.com/")
                .build();

        FirebaseApp.initializeApp(options);

        if (this.fyreLogger != null)
            fyreLogger.log("Firebase initialized");
    }

    public FirebaseDatabase getDatabase() {
        return FirebaseDatabase.getInstance();
    }

}
