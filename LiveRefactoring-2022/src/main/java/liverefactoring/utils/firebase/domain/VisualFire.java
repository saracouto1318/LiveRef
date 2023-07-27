package liverefactoring.utils.firebase.domain;


import liverefactoring.utils.firebase.data.DataManagerImpl;
import liverefactoring.utils.firebase.data.FirebaseManager;
import liverefactoring.utils.firebase.model.FireNode;
import liverefactoring.utils.firebase.model.ObserveContract;
import liverefactoring.utils.firebase.model.protocol.UpdateType;
import liverefactoring.utils.firebase.util.ClipboardManager;
import liverefactoring.utils.firebase.util.FireDataJSONConverter;
import liverefactoring.utils.firebase.util.FyreLogger;

import java.awt.*;
import java.io.IOException;
import java.util.Map;

public class VisualFire extends ObserveContract.FireObservable implements ObserveContract.FireObserver {
    private static VisualFire instance;
    private DataManager dataManager;
    private FyreLogger fyreLogger;
    private boolean initialized;
    private FireDataJSONConverter jsonConverter;
    private ClipboardManager clipboardManager;

    public VisualFire(FyreLogger fyreLogger) {
        this(new DataManagerImpl(new FirebaseManager(fyreLogger)), fyreLogger,
                new FireDataJSONConverter(),
                new ClipboardManager(Toolkit.getDefaultToolkit().getSystemClipboard()));
    }

    public VisualFire(DataManager dataManager, FyreLogger fyreLogger, FireDataJSONConverter jsonConverter, ClipboardManager clipboardManager) {
        this.fyreLogger = fyreLogger;
        this.dataManager = dataManager;
        this.jsonConverter = jsonConverter;
        this.clipboardManager = clipboardManager;
    }

    public static VisualFire getInstance() {
        if (instance == null)
            instance = new VisualFire(new FyreLogger());
        return instance;
    }

    public void setUp(String pathToCredentials) {
        try {
            this.dataManager.getObservable().addObserver(this);
            this.dataManager.configureFirebase(pathToCredentials);
            initialized = true;
        } catch (IOException e) {
            fyreLogger.log(e.getMessage());
            initialized = false;
        }
    }

    public void load() {
        this.dataManager.getRoot();
    }

    public FireNode getData(String node) {
        return this.dataManager.getNode(node);
    }

    public void insert(String path, Map<String, Object> value) {
        this.dataManager.addNode(path, value);
    }

    public FireNode updateData(String path, String value) {
        return this.dataManager.updateNode(path, value);
    }

    @Override
    public void update(UpdateType type, FireNode data) {
        if (type != UpdateType.FIREBASE_INIT_SUCCESS)
            this.updateAll(type, data);
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void convertNodeToJson(FireNode node) {
        this.clipboardManager.setContent(this.jsonConverter.convertFireNodeToJson(node));
    }
}
