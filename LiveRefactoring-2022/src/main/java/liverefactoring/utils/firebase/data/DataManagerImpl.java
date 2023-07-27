package liverefactoring.utils.firebase.data;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import liverefactoring.utils.firebase.domain.DataManager;
import liverefactoring.utils.firebase.model.FireNode;
import liverefactoring.utils.firebase.model.ObserveContract;
import liverefactoring.utils.firebase.model.protocol.UpdateType;
import liverefactoring.utils.firebase.util.FyreLogger;
import liverefactoring.utils.firebase.util.PathExtractor;
import liverefactoring.utils.firebase.util.SnapshotParser;

import java.io.IOException;
import java.util.Map;

public class DataManagerImpl extends ObserveContract.FireObservable implements DataManager {
    private FirebaseManager firebaseManager;
    private FyreLogger logger;
    private ValueEventListener rootListener;
    private boolean rootListenerAttached = false;
    private DataSnapshot lastRootSnapshot;
    private PathExtractor pathExtractor;
    private SnapshotParser snapshotParser;

    public DataManagerImpl(FirebaseManager firebaseManager) {
        this(new FyreLogger("DataManagerImpl"), firebaseManager, new SnapshotParser(), new PathExtractor());
    }

    public DataManagerImpl(FyreLogger logger, FirebaseManager firebaseManager, SnapshotParser snapshotParser, PathExtractor pathExtractor) {
        this.logger = logger;
        this.firebaseManager = firebaseManager;
        this.snapshotParser = snapshotParser;
        this.pathExtractor = pathExtractor;
        rootListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                logger.log("Received a root!");
                FireNode root = new FireNode("Root");
                buildFireTree(root, snapshot);
                updateAll(UpdateType.ROOT_DATA_LOADED, root);
                lastRootSnapshot = snapshot;
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        };
    }

    public void configureFirebase(String pathToConfig) throws IOException {
        this.firebaseManager.init(pathToConfig, "https://livemr-b5eb2-default-rtdb.firebaseio.com/");
        this.update(UpdateType.FIREBASE_INIT_SUCCESS, null);
    }

    @Override
    public ObserveContract.FireObservable getObservable() {
        return this;
    }

    private void buildFireTree(FireNode parent, DataSnapshot snapshot) {
        FireNode node;
        if (!snapshot.hasChildren()) {
            parent.setValue(snapshot.getValue().toString());
        }

        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
            node = new FireNode(dataSnapshot.getKey());
            parent.addChild(node);
            this.buildFireTree(node, dataSnapshot);
        }
    }

    @Override
    public FireNode getRoot() {
        DatabaseReference ref = this.firebaseManager.getDatabase().getReference();
        logger.log("Getting Root!");
        if (!rootListenerAttached)
            ref.addValueEventListener(rootListener);
        else {
            rootListener.onDataChange(lastRootSnapshot);
        }
        return null;
    }

    @Override
    public FireNode getNode(String node) {

        return null;
    }

    @Override
    public FireNode updateNode(String pathToNode, String newValue) {
        String pathOfParent = pathExtractor.removeLastPath(pathToNode);
        String oldValue = pathExtractor.getLastPath(pathToNode);
        DatabaseReference transactionReference = pathOfParent.equals("") ? this.firebaseManager.getDatabase().getReference("")
                : this.firebaseManager.getDatabase().getReference(pathOfParent);

        transactionReference.runTransaction(new TransactionHandler(oldValue, newValue, new FyreLogger("TransactionHandler"),
                pathOfParent, this.firebaseManager), false);

        return null;
    }

    @Override
    public void addNode(String pathToParent, Map<String, Object> value) {
        firebaseManager.getDatabase().getReference(pathToParent).updateChildren(value, (error, ref) ->
                update(error != null ? UpdateType.ADD_NODE_FAIL : UpdateType.ADD_NODE_SUCCESS, null)
        );
    }

    @Override
    public void deleteNode(String pathToNode) {
        firebaseManager.getDatabase().getReference(pathToNode).setValue(null, (error, ref) ->
                update(error != null ? UpdateType.DELETE_NODE_FAIL : UpdateType.DELETE_NODE_SUCCESS, null)
        );
    }

    @Override
    public void moveNode(String from, String to) {
        firebaseManager.getDatabase().getReference(from).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                firebaseManager.getDatabase().getReference(to).updateChildren(snapshotParser.parseDataSnapshotToMap(snapshot),
                        (error, ref) -> {

                        });
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }

    @Override
    public void update(UpdateType type, FireNode data) {
        this.updateAll(type, data);
    }

}
