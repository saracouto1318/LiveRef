package com;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.ui.components.JBTextField;
import com.utils.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Objects;

public class ConfigureTool extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        ConfigureTool.MyDialogWrapper wrapper = new ConfigureTool.MyDialogWrapper();
        wrapper.show();

        int minNumExtractedMethods, maxOrigMethodPercentage, minNumStatements, minLengthExtraction, minValueParameters;
        String username;
        boolean colorBlind = false;

        if (wrapper.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
            SelectedRefactorings.selectedRefactoring = null;
            SelectedRefactorings.selectedRefactorings.clear();

            if (wrapper.selectAll.isSelected()) {
                SelectedRefactorings.selectedRefactoring = Refactorings.All;
            } else {
                if (wrapper.selectExtractMethod.isSelected()) {
                    SelectedRefactorings.selectedRefactorings.add(Refactorings.ExtractMethod);
                }
                if (wrapper.selectExtractClass.isSelected()) {
                    SelectedRefactorings.selectedRefactorings.add(Refactorings.ExtractClass);
                }
                if (wrapper.selectExtractVariable.isSelected()) {
                    SelectedRefactorings.selectedRefactorings.add(Refactorings.ExtractVariable);
                }
                if (wrapper.selectMoveMethod.isSelected()) {
                    SelectedRefactorings.selectedRefactorings.add(Refactorings.ExtractClass);
                }
                if (wrapper.selectIPO.isSelected()) {
                    SelectedRefactorings.selectedRefactorings.add(Refactorings.ExtractVariable);
                }
            }

            if(wrapper.selectedColorBlindYes.isSelected()){
                colorBlind = true;
            }

            minNumExtractedMethods = Integer.parseInt(wrapper.textField_minNumExtractedMethods.getText());
            maxOrigMethodPercentage = Integer.parseInt(wrapper.textField_maxOrigMethodPercentage.getText());
            minNumStatements = Integer.parseInt(wrapper.textField_minNumStatements.getText());
            minLengthExtraction = Integer.parseInt(wrapper.textField_minLengthExtraction.getText());
            minValueParameters = Integer.parseInt(wrapper.textField_minValParameters.getText());
            if (wrapper.textField_username.getText().length() == 0) {
                Date date = new Date();
                String user = "username" + date;
                username = this.calculateHash(user);
            } else
                username = this.calculateHash(wrapper.textField_username.getText());

            if (minNumExtractedMethods < 2)
                minNumExtractedMethods = 2;

            if (maxOrigMethodPercentage >= 100)
                maxOrigMethodPercentage = 100;

            if (minNumStatements < 1)
                minNumStatements = 1;

            if (minLengthExtraction < 1)
                minLengthExtraction = 1;

            if (minValueParameters < 1)
                minValueParameters = 1;

            ThresholdsCandidates thresholds = new ThresholdsCandidates(minNumExtractedMethods,
                    maxOrigMethodPercentage, minNumStatements, minLengthExtraction,minValueParameters, username, colorBlind);

            if(Values.isActive){
                for (RangeHighlighter rangeHighlighter : Values.gutters) {
                    rangeHighlighter.setGutterIconRenderer(null);
                }

                Editor editor = e.getData(PlatformDataKeys.EDITOR);
                PsiDocumentManager documentManager = PsiDocumentManager.getInstance(Objects.requireNonNull(e.getProject()));
                Utilities utils = new Utilities();
                Values.editor = editor;
                final PsiFile psiFile = documentManager.getCachedPsiFile(Values.editor.getDocument());
                final PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
                utils.startActions(psiJavaFile);
            }
        }
    }

    private String calculateHash(String name) {
        String hashedString;
        MessageDigest md;

        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Invalid algorithm!");
            return "";
        }

        byte[] hash = md.digest(name.getBytes());
        hashedString = new String(hash);

        return hashedString;
    }

    private static class MyDialogWrapper extends DialogWrapper {
        private final JRadioButton selectExtractMethod = new JRadioButton();
        private final JRadioButton selectExtractVariable = new JRadioButton();
        private final JRadioButton selectExtractClass = new JRadioButton();
        private final JRadioButton selectMoveMethod = new JRadioButton();
        private final JRadioButton selectIPO = new JRadioButton();
        private final JRadioButton selectAll = new JRadioButton();

        private final JLabel label_minNumExtractedMethods = new JLabel("Min. Num. Methods to extract");
        private final JBTextField textField_minNumExtractedMethods = new JBTextField();
        private final JLabel label_maxOrigMethodPercentage = new JLabel("Max. Percentage Original Methods");
        private final JBTextField textField_maxOrigMethodPercentage = new JBTextField();
        private final JLabel label_minNumStatements = new JLabel("Min. Num. Statements to extract");
        private final JBTextField textField_minNumStatements = new JBTextField();
        private final JLabel label_minLengthExtraction = new JLabel("Min. Length of Expressions to be considered");
        private final JBTextField textField_minLengthExtraction = new JBTextField();
        private final JLabel label_username = new JLabel("Username to be used on the experiments");
        private final JBTextField textField_username = new JBTextField(20);
        private final JLabel label_colorBlind = new JLabel("Are you color blinded?");
        private final JRadioButton selectedColorBlindYes = new JRadioButton();
        private final JRadioButton selectedColorBlindNo = new JRadioButton();
        private final JBTextField textField_minValParameters = new JBTextField();
        private final JLabel label_minValParameters = new JLabel("Min. Num. Parameters");

        public MyDialogWrapper() {
            super(false);
            init();
            setTitle("Configure Tool");
        }

        @Nullable
        @Override
        protected JComponent createCenterPanel() {
            Box boxWithExecutors = Box.createVerticalBox();
            Box box = Box.createHorizontalBox();
            //box.setPreferredSize(new Dimension(200, 200));
            //box.setPreferredSize(new Dimension(200, 200));

            JPanel panel = new JPanel(new GridLayout(4, 2));

            /*textField_minNumExtractedMethods.setText(Integer.toString(ThresholdsCandidates.minNumExtractedMethods));
            textField_maxOrigMethodPercentage.setText(Integer.toString(ThresholdsCandidates.maxOrigMethodPercentage));
            textField_minNumStatements.setText(Integer.toString(ThresholdsCandidates.minNumStatements));
            textField_minLengthExtraction.setText(Integer.toString(ThresholdsCandidates.minLengthExtraction));*/

            JPanel panel1 = new JPanel(new GridLayout(1, 2));
            panel1.add(label_minNumExtractedMethods, BorderLayout.WEST);
            panel1.add(textField_minNumExtractedMethods, BorderLayout.EAST);
            panel1.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), "Extract Class"));

            JPanel panel2 = new JPanel(new GridLayout(2, 2));
            panel2.add(label_maxOrigMethodPercentage, BorderLayout.WEST);
            panel2.add(textField_maxOrigMethodPercentage, BorderLayout.EAST);
            panel2.add(label_minNumStatements, BorderLayout.WEST);
            panel2.add(textField_minNumStatements, BorderLayout.EAST);
            panel2.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), "Extract Method"));

            JPanel panel3 = new JPanel(new GridLayout(1, 2));
            panel3.add(label_minLengthExtraction, BorderLayout.WEST);
            panel3.add(textField_minLengthExtraction, BorderLayout.EAST);
            panel3.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), "Extract Variable"));

            JPanel panel3a = new JPanel(new GridLayout(1, 2));
            panel3a.add(label_minValParameters, BorderLayout.WEST);
            panel3a.add(label_minValParameters, BorderLayout.EAST);
            panel3a.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), "Introduce Parameter Object"));

            JPanel panel3b = new JPanel(new GridLayout(1, 2));
            panel3b.add(label_minValParameters, BorderLayout.WEST);
            panel3b.add(label_minValParameters, BorderLayout.EAST);
            panel3b.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), "Move Method"));

            ChangeListener listener = event -> {
                JRadioButton source = (JRadioButton) event.getSource();
                panel1.setVisible(false);
                panel2.setVisible(false);
                panel3.setVisible(false);
                panel3a.setVisible(false);
                panel3b.setVisible(false);

                if(selectAll.isSelected()){
                    if(!source.equals(selectAll)){
                        selectAll.setSelected(false);
                    }
                    else{
                        panel1.setVisible(true);
                        panel2.setVisible(true);
                        panel3.setVisible(true);
                        panel3a.setVisible(true);
                        panel3b.setVisible(true);
                    }
                }

                if(selectExtractVariable.isSelected() || selectExtractClass.isSelected() ||
                        selectExtractMethod.isSelected() || selectMoveMethod.isSelected() ||
                    selectIPO.isSelected()){
                    if(source.equals(selectAll)){
                        selectAll.setSelected(true);
                        selectExtractClass.setSelected(false);
                        selectExtractMethod.setSelected(false);
                        selectExtractVariable.setSelected(false);
                        selectMoveMethod.setSelected(false);
                        selectIPO.setSelected(false);
                    }
                }

                if(source.getText().equals("Extract Method") || selectExtractMethod.isSelected()){
                    panel2.setVisible(true);
                }
                if(source.getText().equals("Extract Class") || selectExtractClass.isSelected()){
                    panel1.setVisible(true);
                }
                if(source.getText().equals("Extract Variable") || selectExtractVariable.isSelected()){
                    panel3.setVisible(true);
                }
                if(source.getText().equals("Introduce Parameter Object") || selectIPO.isSelected()){
                    panel3a.setVisible(true);
                }
                if(source.getText().equals("Move Method") || selectMoveMethod.isSelected()){
                    panel3b.setVisible(true);
                }
                if(source.getText().equals("All Refactorings")){
                    panel1.setVisible(true);
                    panel2.setVisible(true);
                    panel3.setVisible(true);
                }
            };

            selectExtractMethod.addChangeListener(listener);
            selectExtractClass.addChangeListener(listener);
            selectExtractVariable.addChangeListener(listener);
            selectIPO.addChangeListener(listener);
            selectMoveMethod.addChangeListener(listener);
            selectAll.addChangeListener(listener);

            if(SelectedRefactorings.selectedRefactorings.size() > 0)
                for (Refactorings selectedRefactoring : SelectedRefactorings.selectedRefactorings) {
                    if(selectedRefactoring == Refactorings.ExtractClass)
                        selectExtractClass.setSelected(true);
                    else if(selectedRefactoring == Refactorings.ExtractMethod) {
                        selectExtractMethod.setSelected(true);
                    }
                    else if(selectedRefactoring == Refactorings.ExtractVariable)
                        selectExtractVariable.setSelected(true);
                    else if(selectedRefactoring == Refactorings.MoveMethod)
                        selectMoveMethod.setSelected(true);
                    else if(selectedRefactoring == Refactorings.IntroduceParamObj)
                        selectIPO.setSelected(true);
                }
            else
                selectAll.setSelected(true);

            selectExtractMethod.setText("Extract Method");
            panel.add(selectExtractMethod, BorderLayout.WEST);

            selectExtractClass.setText("Extract Class");
            panel.add(selectExtractClass, BorderLayout.WEST);

            selectExtractVariable.setText("Extract Variable");
            panel.add(selectExtractVariable, BorderLayout.WEST);

            selectMoveMethod.setText("Move Method");
            panel.add(selectMoveMethod, BorderLayout.WEST);

            selectIPO.setText("Introduce Parameter Object");
            panel.add(selectIPO, BorderLayout.WEST);

            selectAll.setText("All Refactorings");
            panel.add(selectAll, BorderLayout.WEST);

            //panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Included Refactorings"));

            boxWithExecutors.add(panel);
           // boxWithExecutors.add(panel1);
            boxWithExecutors.add(panel2);
            //boxWithExecutors.add(panel3);

            JPanel panel4 = new JPanel(new GridLayout(2, 2));

            panel4.add(label_colorBlind, BorderLayout.WEST);

            selectedColorBlindYes.setText("Yes");
            selectedColorBlindNo.setText("No");
            selectedColorBlindNo.setSelected(true);

            ChangeListener changeListener = event -> {
                JRadioButton source = (JRadioButton) event.getSource();
                if(source.getText().equals("Yes")){
                    selectedColorBlindNo.setSelected(false);
                }
                else if(source.getText().equals("No")){
                    selectedColorBlindYes.setSelected(false);
                }
            };

            selectedColorBlindYes.addChangeListener(changeListener);
            selectedColorBlindNo.addChangeListener(changeListener);

            panel4.add(selectedColorBlindYes);
            panel4.add(selectedColorBlindNo);

            panel4.add(label_username, BorderLayout.WEST);
            panel4.add(textField_username, BorderLayout.EAST);

            panel4.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), "Further Details"));
            boxWithExecutors.add(panel4);
            box.add(boxWithExecutors);

            return box;
        }
    }
}
