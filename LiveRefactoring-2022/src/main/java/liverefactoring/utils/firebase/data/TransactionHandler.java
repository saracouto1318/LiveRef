package com.utils.firebase.data;

import com.google.firebase.database.*;
import com.utils.firebase.util.FyreLogger;

import java.util.HashMap;
import java.util.Map;

public class TransactionHandler implements Transaction.Handler {
    private String oldValue;
    private String newValue;
    private FyreLogger logger;
    private String pathOfParent;
    private FirebaseManager firebaseManager;

    public TransactionHandler(String oldValue, String newValue, FyreLogger logger, String pathOfParent, FirebaseManager firebaseManager) {
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.logger = logger;
        this.pathOfParent = pathOfParent;
        this.firebaseManager = firebaseManager;
    }


    private void mergeTrees(MutableData oldData, MutableData newData) {
        for (MutableData mergingData : oldData.getChildren()) {
            newData.child(mergingData.getKey()).setValue(mergingData.getValue());
        }
    }

    @Override
    public Transaction.Result doTransaction(MutableData currentData) {
        MutableData mutableOldChild = currentData.child(oldValue);
        if (mutableOldChild.hasChildren()) {
            MutableData mutableNewChild = currentData.child(newValue);
            mergeTrees(mutableOldChild, mutableNewChild);
            mutableOldChild.setValue(null);
        } else {
            try {
                String snapShotValue = currentData.child(oldValue).getValue().toString();
                currentData.child(newValue).setValue(snapShotValue);
                currentData.child(oldValue).setValue(null);

            } catch (Exception e) {
                if (currentData.getValue() == null) {
                    // we're editing a root node, not a leaf value
                    // currentData.child(newValue).setValue(mutableOldChild);
                    mergeTrees(currentData, currentData.child(newValue));
                } else
                    currentData.child(oldValue).setValue(newValue);
            }
        }

        return Transaction.success(currentData);
    }

    @Override
    public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {
        if (error != null)
            logger.log(error.getMessage());
        if (error != null && !committed && pathOfParent.equals("")) {
                    /* we're handling a root value. unfortunately the firebase SDK does not support a transaction on a root node
                       and grab direct nodes. Workaround:
                     */
            firebaseManager.getDatabase().getReference().child(oldValue).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot oldDataSnapshot) {
                    // We have to grab the values
                    // 1. map all the children to a hashmap
                    // 2.insert them iterately to the new value
                    if (oldDataSnapshot.hasChildren()) {
                        Map<String, Object> valueMap = new HashMap<>();

                        for (DataSnapshot child : oldDataSnapshot.getChildren()) {
                            updateTree(valueMap, child);
                        }

                        firebaseManager.getDatabase().getReference().child(newValue).setValue(valueMap, (error12, ref) -> {
                            // remove old branch
                            firebaseManager.getDatabase().getReference().child(oldValue).setValueAsync(null);

                        });
                    } else {
                        firebaseManager.getDatabase().getReference().child(newValue).setValue(oldDataSnapshot.getValue(),
                                (error1, ref) -> firebaseManager.getDatabase().getReference().child(oldValue).setValueAsync(null));
                    }

                }

                @Override
                public void onCancelled(DatabaseError error) {
                    logger.log("Received an Error: " + error.getMessage());

                }
            });
        }
    }

    private void updateTree(Map<String, Object> valueMap, DataSnapshot currentSnapshot) {
        String newPath = currentSnapshot.getKey();
        valueMap.put(newPath, currentSnapshot.getValue());
    }
}