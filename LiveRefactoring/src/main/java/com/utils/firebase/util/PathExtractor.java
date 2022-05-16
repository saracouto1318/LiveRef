package com.utils.firebase.util;

public class PathExtractor {

    public String getLastPath(String path) {
        int lastSlashIndex = path.lastIndexOf('/');
        return path.substring(lastSlashIndex + 1, path.length());
    }

    public String removeLastPath(String path) {
        try {
            int lastSlashIndex = path.lastIndexOf('/');
            return path.substring(0, lastSlashIndex);
        } catch (Exception e) {
            return "";
        }
    }
}
