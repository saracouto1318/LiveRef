package com.ui.metrics;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.jcef.JBCefBrowser;
import org.cef.CefApp;

import javax.swing.*;

public class MetricsWindow {
    public Project project;
    public JBCefBrowser browser;
    public JComponent content;

    public MetricsWindow(Project project){
        this.project = project;
        this.browser = this.createWebView();
        this.content = this.browser.getComponent();
    }

    private JBCefBrowser createWebView(){
        JBCefBrowser newBrowser = new JBCefBrowser();
        registerAppSchemeHandler();
        newBrowser.loadURL("http://myapp/index.html");
        Disposer.register(project, newBrowser);
        return newBrowser;
    }

    private void registerAppSchemeHandler(){
        CefApp.getInstance().registerSchemeHandlerFactory(
                "http",
                "myapp",
                new CustomSchemeHandlerFactory());
    }
}
