package com.ui.metrics;

import com.intellij.openapi.project.Project;

public class MetricsWindowService {
    public MetricsWindow window;

    public MetricsWindowService(Project project){
        this.window = new MetricsWindow(project);
    }
}
