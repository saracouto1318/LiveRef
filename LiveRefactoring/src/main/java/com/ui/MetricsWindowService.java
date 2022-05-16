package com.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.CustomizeColoredTreeCellRenderer;
import com.intellij.ui.jcef.JBCefBrowser;
import org.cef.CefApp;

public class MetricsWindowService {
    public MetricsWindow window;

    public MetricsWindowService(Project project){
        this.window = new MetricsWindow(project);
    }
}
