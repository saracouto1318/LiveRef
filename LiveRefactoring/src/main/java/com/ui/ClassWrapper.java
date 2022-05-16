package com.ui;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBTextField;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class ClassWrapper extends DialogWrapper {
    public JLabel label_classname = new JLabel("New Class Name");
    public JBTextField textField_classname = new JBTextField(10);

    public ClassWrapper() {
        super(false);
        init();
        setTitle("Creating a new Class");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        Box boxWithExecutors = Box.createVerticalBox();

        JPanel panel4 = new JPanel(new GridLayout(1, 2));
        panel4.add(label_classname, BorderLayout.WEST);
        panel4.add(textField_classname, BorderLayout.EAST);

        boxWithExecutors.add(panel4);

        Box box = Box.createHorizontalBox();
        box.add(boxWithExecutors);

        return box;
    }
}
