package com.ui;

import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.refactorings.candidates.ExtractClassCandidate;
import com.refactorings.candidates.ExtractMethodCandidate;
import com.refactorings.candidates.ExtractVariableCandidate;
import com.refactorings.candidates.utils.Severity;
import com.utils.Utilities;
import com.utils.Values;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class RefactoringWindow extends SimpleToolWindowPanel {
    public Utilities utils = new Utilities();

    public RefactoringWindow(ToolWindow toolWindow) {
        super(true, false);
        final Content content = ContentFactory.SERVICE.getInstance().createContent(this, "", false);
        this.setContentWindow(toolWindow, content);
    }

    public void setContentWindow(ToolWindow toolWindow, Content content){
        JPanel panel = new JPanel();
        for (Severity candidate : Values.candidates) {
            ImageIcon icon = null;
            String name = "";
            String elements = "";

            if(candidate.candidate instanceof ExtractMethodCandidate){
                icon = getImageIconByType(candidate.indexColorGutter, "EM");
                name = "Extract Method";
                elements = "(" + ((ExtractMethodCandidate) candidate.candidate).nodes.size() +
                        (((ExtractMethodCandidate) candidate.candidate).nodes.size() > 1 ? " elements)" : " element)");
            }
            else if(candidate.candidate instanceof ExtractClassCandidate){
                icon = getImageIconByType(candidate.indexColorGutter, "EC");
                name = "Extract Class";
                elements = "(" + (((ExtractClassCandidate) candidate.candidate).targetEntities.size()) +
                        ((((ExtractClassCandidate) candidate.candidate).targetEntities.size()) > 1 ? " elements)" : " element)") ;
            }
            else if(candidate.candidate instanceof ExtractVariableCandidate){
                icon = getImageIconByType(candidate.indexColorGutter, "EV");
                name = "Extract Variable";
                elements = "(1 element)";
            }

            JLabel label = new JLabel(name + " " + elements);
            label.setIcon(icon);
            panel.add(label);
        }

        this.add(panel);
        toolWindow.getContentManager().addContent(content);
    }

    public ImageIcon getImageIconByType(int index, String type){
        ImageIcon icon = null;
        try {
            icon = utils.getIconCircle(index, type);
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert icon != null;
        Image img = icon.getImage();
        Image resizedImage = img.getScaledInstance(10, 10, java.awt.Image.SCALE_SMOOTH);

        icon = new ImageIcon(resizedImage);

        return icon;
    }
}
