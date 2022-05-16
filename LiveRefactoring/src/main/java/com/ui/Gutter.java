package com.ui;

import com.intellij.psi.PsiElement;

import javax.swing.*;

public class Gutter {
    public int line;
    public ImageIcon icon;
    public double severity;
    public String description;
    public PsiElement node;

    public Gutter(int line, ImageIcon icon, double severity, String description, PsiElement node) {
        this.line = line;
        this.icon = icon;
        this.severity = severity;
        this.description = description;
        this.node = node;
    }
}
