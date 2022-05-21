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

        int maxOrigMethodPercentage, minNumStatements;

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
            }

            maxOrigMethodPercentage = Integer.parseInt(wrapper.textField_maxOrigMethodPercentage.getText());
            minNumStatements = Integer.parseInt(wrapper.textField_minNumStatements.getText());


            if (maxOrigMethodPercentage >= 100)
                maxOrigMethodPercentage = 100;

            if (minNumStatements < 1)
                minNumStatements = 1;


            ThresholdsCandidates thresholds = new ThresholdsCandidates(0,
                    maxOrigMethodPercentage, minNumStatements, 0, 0, "", false);

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

    private static class MyDialogWrapper extends DialogWrapper {
        private final JRadioButton selectExtractMethod = new JRadioButton();
        private final JRadioButton selectExtractVariable = new JRadioButton();
        private final JRadioButton selectExtractClass = new JRadioButton();
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

            textField_minNumExtractedMethods.setText(Integer.toString(ThresholdsCandidates.minNumExtractedMethods));
            textField_maxOrigMethodPercentage.setText(Integer.toString(ThresholdsCandidates.maxOrigMethodPercentage));
            textField_minNumStatements.setText(Integer.toString(ThresholdsCandidates.minNumStatements));
            textField_minLengthExtraction.setText(Integer.toString(ThresholdsCandidates.minLengthExtraction));

            JPanel panel2 = new JPanel(new GridLayout(2, 2));
            panel2.add(label_maxOrigMethodPercentage, BorderLayout.WEST);
            panel2.add(textField_maxOrigMethodPercentage, BorderLayout.EAST);
            panel2.add(label_minNumStatements, BorderLayout.WEST);
            panel2.add(textField_minNumStatements, BorderLayout.EAST);
            panel2.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), "Extract Method"));

            if(SelectedRefactorings.selectedRefactorings.size() > 0)
                for (Refactorings selectedRefactoring : SelectedRefactorings.selectedRefactorings) {
                    if(selectedRefactoring == Refactorings.ExtractClass)
                        selectExtractClass.setSelected(true);
                    else if(selectedRefactoring == Refactorings.ExtractMethod) {
                        selectExtractMethod.setSelected(true);
                    }
                    else if(selectedRefactoring == Refactorings.ExtractVariable)
                        selectExtractVariable.setSelected(true);
                }
            else
                selectAll.setSelected(true);

            boxWithExecutors.add(panel2);
            box.add(boxWithExecutors);

            return box;
        }
    }
}