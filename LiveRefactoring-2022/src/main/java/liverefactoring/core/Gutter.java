package com.core;

import com.intellij.psi.PsiElement;

import javax.swing.*;

public class Gutter {
    public int line;
    public Icon icon;
    public double severity;
    public String description;
    public PsiElement node;

    public Gutter(int line, Icon icon, double severity, String description, PsiElement node) {
        this.line = line;
        this.icon = icon;
        this.severity = severity;
        this.description = description;
        this.node = node;
    }
}
