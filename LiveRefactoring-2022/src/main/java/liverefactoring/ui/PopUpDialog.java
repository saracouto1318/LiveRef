package liverefactoring.ui;

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class PopUpDialog extends DialogWrapper {
    public JLabel label = new JLabel("Would you like to activate LiveRef?");

    public PopUpDialog() {
        super(false);
        init();
        setTitle("Activate LiveRef");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        Box boxWithExecutors = Box.createVerticalBox();

        JPanel panel = new JPanel(new GridLayout(1,1));
        panel.add(label);

        boxWithExecutors.add(panel);

        Box box = Box.createHorizontalBox();
        box.add(boxWithExecutors);

        return box;
    }
}
