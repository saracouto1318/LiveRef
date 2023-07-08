package com.core;

import com.intellij.openapi.editor.LogicalPosition;

public class MyFocus {
    public LogicalPosition start;
    public LogicalPosition end;
    public int startOffset;
    public int endOffset;

    public MyFocus(LogicalPosition start, LogicalPosition end, int startOffset, int endOffset){
        this.start = start;
        this.end = end;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }
}
