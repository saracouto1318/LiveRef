package liverefactoring.utils.firebase.util;

public class FyreLogger {
    private String tag;

    public FyreLogger() {
    }

    public FyreLogger(String tag) {
        this.tag = tag;
    }

    public void log(String text) {
        System.out.println("##\t" + (tag != null ? tag + ": " : "Fyre:  ") + text + "\t##\t");
    }

    public void log(Object object) {
        System.out.println("##\t" + (tag != null ? tag + ": " : "Fyre:  ") + object.toString() + "\t##\t");
    }
}
