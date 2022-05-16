package com.ui;


import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import org.jetbrains.annotations.NotNull;

public class MyToolWindowFactory implements ToolWindowFactory {
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        //final MyToolWindowPanel panel = new MyToolWindowPanel(toolWindow);
        //final RefactoringWindow panel = new RefactoringWindow(toolWindow);
        //final ToolMainPanel panel = new ToolMainPanel(toolWindow, project);
    }
}