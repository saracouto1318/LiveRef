package com.ui.JDeodorant;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBTextField;
import com.utils.RefactorUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

public class ClassWrapper extends DialogWrapper {
    public JLabel label_classname = new JLabel("New Class Name");
    public JBTextField textField_classname = new JBTextField(10);
    private final JLabel warning = new JLabel("");
    public ArrayList<String> classes;

    public ClassWrapper() {
        super(false);
        RefactorUtils utils = new RefactorUtils();
        this.classes = utils.getAllClasses();
        init();
        setTitle("Creating a New Class");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        Box boxWithExecutors = Box.createVerticalBox();
        warning.setVisible(true);

        textField_classname.addKeyListener(new KeyAdapter()
        {
            public void keyPressed(KeyEvent ke)
            {
                for (String aClass : classes) {
                    int size = aClass.length();
                    if(aClass.contains(textField_classname.getText()) &&
                            textField_classname.getText().length() >= 0.5*size){
                        warning.setText("Your project has a class with a similar name....");
                        break;
                    }
                    else
                        warning.setText("");

                    if(aClass.equals(textField_classname.getText())) {
                        warning.setText("Your project has a class with a similar name....");
                    }
                }
            }
        });

        JPanel panel4 = new JPanel(new GridLayout(1, 2));
        JPanel panel = new JPanel(new GridLayout(1,1));
        panel.add(warning);
        panel4.add(label_classname, BorderLayout.WEST);
        panel4.add(textField_classname, BorderLayout.EAST);


        boxWithExecutors.add(panel);
        boxWithExecutors.add(panel4);

        Box box = Box.createHorizontalBox();
        box.add(boxWithExecutors);

        return box;
    }
}
