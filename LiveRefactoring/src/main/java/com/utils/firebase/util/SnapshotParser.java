package com.utils.firebase.util;

import com.google.firebase.database.DataSnapshot;

import java.util.HashMap;
import java.util.Map;

public class SnapshotParser {

    public Map<String, Object> parseDataSnapshotToMap(DataSnapshot snapshot) {
        HashMap<String, Object> result = new HashMap<>();
        result.put(snapshot.getKey(), snapshot.getValue());
        if (snapshot.hasChildren()) {
            snapshot.getChildren().forEach(snapshot1 -> result.put(snapshot1.getKey(), snapshot1.getValue()));
        }
        return result;
    }
}