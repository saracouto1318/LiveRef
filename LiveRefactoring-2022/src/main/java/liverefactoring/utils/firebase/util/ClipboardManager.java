package liverefactoring.utils.firebase.util;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

public class ClipboardManager {
    private Clipboard clipboard;

    public ClipboardManager(Clipboard clipboard) {
        this.clipboard = clipboard;

    }

    public void setContent(String content) {
        StringSelection selection = new StringSelection(content);
        clipboard.setContents(selection, selection);
    }
}
