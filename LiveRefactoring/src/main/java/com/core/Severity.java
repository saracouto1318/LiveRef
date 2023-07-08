package com.core;

public class Severity {
    public Object candidate;
    public double severity;
    public int indexColorGutter;

    public Severity(Object candidate, double severity, int indexColorGutter) {
        this.candidate = candidate;
        this.severity = severity;
        this.indexColorGutter = indexColorGutter;
    }
}
