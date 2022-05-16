package com.utils;

import com.intellij.openapi.editor.LogicalPosition;

public class MyRange {
    public LogicalPosition start;
    public LogicalPosition end;

    public MyRange(LogicalPosition start, LogicalPosition end) {
        this.start = start;
        this.end = end;
    }

    public boolean equals(MyRange b) {
        return this.start.line == b.start.line && this.start.column == b.start.column &&
                this.end.line == b.end.line && this.end.column == b.end.column;
    }

    public String toString(){
        return "(" + this.start.line + ", " + this.start.column + ") -> (" + this.end.line + ", " + this.end.column + ")";
    }
}
