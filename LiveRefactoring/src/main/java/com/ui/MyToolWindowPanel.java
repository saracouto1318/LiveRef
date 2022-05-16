package com.ui;

import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;

import javax.swing.*;

public class MyToolWindowPanel extends SimpleToolWindowPanel {
    public MyToolWindowPanel(ToolWindow toolWindow) {
        super(true, false);
        final Content content = ContentFactory.SERVICE.getInstance().createContent(this, "", false);

        SimpleToolWindowPanel simpleToolWindowPanel=new SimpleToolWindowPanel(true,true);
        JPanel panel = new JPanel();
        this.add(panel);

        toolWindow.getContentManager().addContent(content);
    }
}
