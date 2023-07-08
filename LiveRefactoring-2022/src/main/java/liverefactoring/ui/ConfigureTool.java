package liverefactoring.ui;

import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.wm.WindowManager;
import liverefactoring.core.Refactorings;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import liverefactoring.utils.UtilitiesOverall;
import liverefactoring.utils.importantValues.SelectedRefactorings;
import liverefactoring.utils.importantValues.ThresholdsCandidates;
import liverefactoring.utils.importantValues.Values;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class ConfigureTool {
    UtilitiesOverall utils = new UtilitiesOverall();
    public ConfigureTool(){

    }

    public void startMenu(){
        MyDialogWrapper wrapper = new MyDialogWrapper();
        wrapper.show();

        double minNumExtractedMethods = 0, maxOrigMethodPercentageEC = 0, maxOrigMethodPercentageEM = 0, minNumStatements = 0,
                minLengthExtraction = 0, minValueParameters = 0, minLOCEM = 0, maxMethodsEC = 0, minCycComplexityEM = 0,
                minEffort = 0, minLCOM = 0, maxNumForeignData = 0, maxPercentageInherit = 0,
                maxPercentageOverride = 0;
        int maxNumberRefactorings = 0, seconds = 0;
        boolean colorBlind = false;

        if (wrapper.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
            SelectedRefactorings.selectedRefactoring = null;
            SelectedRefactorings.selectedRefactorings = new ArrayList<>();

            if (wrapper.selectAll.isSelected()) {
                utils.includeAllRefactorings();
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
                    SelectedRefactorings.selectedRefactorings.add(Refactorings.MoveMethod);
                }
                if (wrapper.selectIPO.isSelected()) {
                    SelectedRefactorings.selectedRefactorings.add(Refactorings.IntroduceParamObj);
                }
                if (wrapper.selectString.isSelected()) {
                    SelectedRefactorings.selectedRefactorings.add(Refactorings.StringComparison);
                }
                if (wrapper.selectInheritance.isSelected()) {
                    SelectedRefactorings.selectedRefactorings.add(Refactorings.InheritanceToDelegation);
                }
            }

            if(wrapper.selectedColorBlindYes.isSelected()){
                colorBlind = true;
            }

            minNumExtractedMethods = Double.parseDouble(wrapper.textField_minNumExtractedMethods.getText());
            maxOrigMethodPercentageEC = wrapper.textField_minOrigMethodPercentageEC.getText().contains("%") ?
                    Double.parseDouble(wrapper.textField_minOrigMethodPercentageEC.getText().split("%")[0]) :
                    Double.parseDouble(wrapper.textField_minOrigMethodPercentageEC.getText());
            maxOrigMethodPercentageEM = wrapper.textField_minOrigMethodPercentageEM.getText().contains("%") ?
                    Double.parseDouble(wrapper.textField_minOrigMethodPercentageEM.getText().split("%")[0]) :
                    Double.parseDouble(wrapper.textField_minOrigMethodPercentageEM.getText());
            minNumStatements = Double.parseDouble(wrapper.textField_minNumStatements.getText());
            minLengthExtraction = Double.parseDouble(wrapper.textField_minLengthExtraction.getText());
            minValueParameters = Double.parseDouble(wrapper.textField_minValParameters.getText());
            minLOCEM = Double.parseDouble(wrapper.textField_LOC_EM.getText());
            maxMethodsEC = Double.parseDouble(wrapper.textField_methods_EC.getText());
            minCycComplexityEM = Double.parseDouble(wrapper.textField_CC_EM.getText());
            minEffort = Double.parseDouble(wrapper.textField_effort.getText());
            minLCOM = Double.parseDouble(wrapper.textField_lcom.getText());
            maxNumForeignData = Double.parseDouble(wrapper.textField_foreign.getText());
            maxPercentageInherit = wrapper.textField_inherit.getText().contains("%") ?
                    Double.parseDouble(wrapper.textField_inherit.getText().split("%")[0]) :
                    Double.parseDouble(wrapper.textField_inherit.getText());
            maxPercentageOverride = wrapper.textField_override.getText().contains("%") ?
                    Double.parseDouble(wrapper.textField_override.getText().split("%")[0]) :
                    Double.parseDouble(wrapper.textField_override.getText());

            if (wrapper.textField_number.getText().length() > 0) {
                if(!wrapper.textField_number.getText().equals("All Refactorings")) {
                    maxNumberRefactorings = Integer.parseInt(wrapper.textField_number.getText());
                    if(maxNumberRefactorings <= 0)
                        maxNumberRefactorings = Integer.MAX_VALUE;
                }
                else
                    maxNumberRefactorings = Integer.MAX_VALUE;
            }

            if (wrapper.textField_seconds.getText().length() > 0) {
                seconds = Integer.parseInt(wrapper.textField_seconds.getText());
                if(seconds == 0)
                    seconds = 5;
                else if(seconds >= 60)
                    seconds = 60;
            }

            if (minNumExtractedMethods < 2)
                minNumExtractedMethods = 2;

            if (maxOrigMethodPercentageEC >= 100)
                maxOrigMethodPercentageEC = 100;

            if (maxOrigMethodPercentageEM >= 100)
                maxOrigMethodPercentageEM = 100;

            if (minNumStatements < 1)
                minNumStatements = 1;

            if (minLengthExtraction < 1)
                minLengthExtraction = 1;

            if (minValueParameters < 1)
                minValueParameters = 1;

            if (minLCOM >= 1)
                minLCOM = 1;

            if(maxPercentageInherit >= 100)
                maxPercentageInherit = 100;

            if(maxPercentageOverride >= 100)
                maxPercentageOverride = 100;

            ThresholdsCandidates.minNumExtractedMethods = minNumExtractedMethods;
            ThresholdsCandidates.minOrigMethodPercentageEC = maxOrigMethodPercentageEC;
            ThresholdsCandidates.minOrigMethodPercentageEM = maxOrigMethodPercentageEM;
            ThresholdsCandidates.minNumStatements = minNumStatements;
            ThresholdsCandidates.minLengthExtraction = minLengthExtraction;
            ThresholdsCandidates.minValueParameters = minValueParameters;
            ThresholdsCandidates.extractMethodLinesCode = minLOCEM;
            ThresholdsCandidates.numMethodsEC = maxMethodsEC;
            ThresholdsCandidates.extractMethodComplexity = minCycComplexityEM;
            ThresholdsCandidates.extractMethodEffort = minEffort;
            ThresholdsCandidates.extractClassLackCohesion = minLCOM;
            ThresholdsCandidates.foreignData = maxNumForeignData;
            ThresholdsCandidates.inheriteMethods = maxPercentageInherit;
            ThresholdsCandidates.overrideMethods = maxPercentageOverride;
            Values.numSeconds = seconds;

            Values.colorBlind = colorBlind;
            ThresholdsCandidates.maxNumberRefactorings = maxNumberRefactorings;

            if(Values.isActive){
                for (RangeHighlighter rangeHighlighter : Values.gutters) {
                    rangeHighlighter.setGutterIconRenderer(null);
                }

                Project project = Values.editor.getProject();
                PsiDocumentManager documentManager = PsiDocumentManager.getInstance(project);
                if(FileEditorManager.getInstance(project).getSelectedEditor() != null){
                    Values.editor = ((TextEditor) FileEditorManager.getInstance(project).getSelectedEditor()).getEditor();
                    if(Values.editor != null) {
                        final PsiFile psiFile = documentManager.getCachedPsiFile(Values.editor.getDocument());
                        if (psiFile instanceof PsiJavaFile) {
                            final PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
                            utils.startActions(psiJavaFile);
                        }
                    }
                }
            }
        }
    }

    public Project getActiveProject() {
        Project[] projects = ProjectManager.getInstance().getOpenProjects();
        Project activeProject = null;
        for (Project project : projects) {
            Window window = WindowManager.getInstance().suggestParentWindow(project);
            if (window != null && window.isActive()) {
                activeProject = project;
            }
        }
        return activeProject;
    }

    private String calculateHash(String name) {
        String hashedString;
        MessageDigest md;

        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            System.out.print("\nInvalid algorithm!\n");
            return "";
        }

        byte[] hash = md.digest(name.getBytes());
        hashedString = new String(hash);

        return hashedString;
    }

    private static class MyDialogWrapper extends DialogWrapper {
        private int counter = 0;

        private final JLabel label_minNumExtractedMethods = new JLabel("Min. Num. Methods to extract");
        private JBTextField textField_minNumExtractedMethods = new JBTextField();
        private final JLabel label_minOrigMethodPercentage = new JLabel("Min. Percentage Original Statements");
        private final JLabel label_minOrigMethodPercentageEM = new JLabel("Min. Percentage Original Methods");
        private JBTextField textField_minOrigMethodPercentageEC = new JBTextField();
        private JBTextField textField_minOrigMethodPercentageEM = new JBTextField();
        private final JLabel label_minNumStatements = new JLabel("Min. Num. Statements to extract");
        private JBTextField textField_minNumStatements = new JBTextField();
        private final JLabel label_minLengthExtraction = new JLabel("Min. Length of Expression");
        private JBTextField textField_minLengthExtraction = new JBTextField();

        private final JLabel label_LOC = new JLabel("Min. Num. Lines of Code");
        private final JBTextField textField_LOC_EM = new JBTextField();
        private final JLabel label_CC_EM = new JLabel("Min. Cyclomatic Complexity");
        private final JBTextField textField_CC_EM = new JBTextField();
        private final JLabel label_effort = new JLabel("Min. Halstead Effort");
        private final JBTextField textField_effort = new JBTextField();

        private final JLabel label_methods_EC = new JLabel("Min. Num. Methods Class");
        private final JBTextField textField_methods_EC = new JBTextField();
        private final JLabel label_lcom = new JLabel("Min. Lack of Cohesion");
        private final JBTextField textField_lcom = new JBTextField();
        private final JLabel label_foreign = new JLabel("Min. Num. Foreign Data");
        private final JBTextField textField_foreign = new JBTextField();

        private final JBTextField textField_inherit = new JBTextField();
        private final JLabel label_inherit = new JLabel("Percentage Inherit Methods");
        private final JBTextField textField_override = new JBTextField();
        private final JLabel label_override = new JLabel("Percentage Override Methods");

        private final JLabel label_number = new JLabel("Max. Num. of Refactorings");
        private JBTextField textField_number = new JBTextField("All Refactorings");
        private final JLabel label_seconds = new JLabel("Seconds to start New Analysis");
        private JBTextField textField_seconds = new JBTextField();
        private JRadioButton selectedColorBlindYes = new JRadioButton();
        private JBTextField textField_minValParameters = new JBTextField();
        private JLabel label_minValParameters = new JLabel("Min. Num. Parameters");
        private final JLabel warning = new JLabel("");
        public final JRadioButton selectExtractMethod = new JRadioButton();
        public final JRadioButton selectExtractVariable = new JRadioButton();
        public final JRadioButton selectExtractClass = new JRadioButton();
        public final JRadioButton selectMoveMethod = new JRadioButton();
        public final JRadioButton selectIPO = new JRadioButton();
        public final JRadioButton selectString = new JRadioButton();
        public final JRadioButton selectInheritance = new JRadioButton();
        public final JRadioButton selectAll = new JRadioButton();
        public JPanel panelEC = new JPanel(new GridLayout(5, 4));
        public JPanel panelEM = new JPanel(new GridLayout(5, 4));
        public JPanel panelEV = new JPanel(new GridLayout(1, 2));
        public JPanel panelIPO = new JPanel(new GridLayout(1, 2));
        public JPanel panelID = new JPanel(new GridLayout(2, 2));

        public HashMap<String,Boolean>selections = new HashMap<>();

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
            Box panelThresholds = Box.createVerticalBox();
            box.setAutoscrolls(true);
            box.setPreferredSize(new Dimension(200,400));

            JPanel panel = new JPanel(new GridLayout(5, 2));
            panel.setPreferredSize(new Dimension(200,75));

            textField_minNumExtractedMethods.setText(Double.toString(ThresholdsCandidates.minNumExtractedMethods));
            textField_minOrigMethodPercentageEC.setText(Double.toString(ThresholdsCandidates.minOrigMethodPercentageEC).concat("%"));
            textField_minOrigMethodPercentageEM.setText(Double.toString(ThresholdsCandidates.minOrigMethodPercentageEM).concat("%"));
            textField_minNumStatements.setText(Double.toString(ThresholdsCandidates.minNumStatements));
            textField_minLengthExtraction.setText(Double.toString(ThresholdsCandidates.minLengthExtraction));
            textField_minValParameters.setText(Double.toString(ThresholdsCandidates.minValueParameters));
            textField_methods_EC.setText(Double.toString(ThresholdsCandidates.numMethodsEC));
            textField_lcom.setText(Double.toString(ThresholdsCandidates.extractClassLackCohesion));
            textField_foreign.setText(Double.toString(ThresholdsCandidates.foreignData));
            textField_LOC_EM.setText(Double.toString(ThresholdsCandidates.extractMethodLinesCode));
            textField_CC_EM.setText(Double.toString(ThresholdsCandidates.extractMethodComplexity));
            textField_effort.setText(Double.toString(ThresholdsCandidates.extractMethodEffort));
            textField_inherit.setText(Double.toString(ThresholdsCandidates.inheriteMethods).concat("%"));
            textField_override.setText(Double.toString(ThresholdsCandidates.overrideMethods).concat("%"));
            textField_seconds.setText(Integer.toString(Values.numSeconds));

            panelEC.add(label_minOrigMethodPercentage, BorderLayout.WEST);
            panelEC.add(textField_minOrigMethodPercentageEC, BorderLayout.EAST);
            panelEC.add(label_minNumExtractedMethods, BorderLayout.WEST);
            panelEC.add(textField_minNumExtractedMethods, BorderLayout.EAST);
            panelEC.add(label_methods_EC, BorderLayout.WEST);
            panelEC.add(textField_methods_EC, BorderLayout.EAST);
            panelEC.add(label_lcom, BorderLayout.WEST);
            panelEC.add(textField_lcom, BorderLayout.EAST);
            panelEC.add(label_foreign, BorderLayout.WEST);
            panelEC.add(textField_foreign, BorderLayout.EAST);
            panelEC.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), "Extract Class"));

            panelEM.add(label_minOrigMethodPercentageEM, BorderLayout.WEST);
            panelEM.add(textField_minOrigMethodPercentageEM, BorderLayout.EAST);
            panelEM.add(label_minNumStatements, BorderLayout.WEST);
            panelEM.add(textField_minNumStatements, BorderLayout.EAST);
            panelEM.add(label_LOC, BorderLayout.WEST);
            panelEM.add(textField_LOC_EM, BorderLayout.EAST);
            panelEM.add(label_CC_EM, BorderLayout.WEST);
            panelEM.add(textField_CC_EM, BorderLayout.EAST);
            panelEM.add(label_effort, BorderLayout.WEST);
            panelEM.add(textField_effort, BorderLayout.EAST);
            panelEM.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), "Extract Method"));

            panelEV.add(label_minLengthExtraction, BorderLayout.WEST);
            panelEV.add(textField_minLengthExtraction, BorderLayout.EAST);
            panelEV.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), "Extract Variable"));

            panelIPO.add(label_minValParameters, BorderLayout.WEST);
            panelIPO.add(textField_minValParameters, BorderLayout.EAST);
            panelIPO.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), "Introduce Parameter Object"));

            panelID.add(label_inherit, BorderLayout.WEST);
            panelID.add(textField_inherit, BorderLayout.EAST);
            panelID.add(label_override, BorderLayout.WEST);
            panelID.add(textField_override, BorderLayout.EAST);
            panelID.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), "Inheritance to Delegation"));

            if(SelectedRefactorings.selectedRefactorings.size() > 0) {
                if (SelectedRefactorings.selectedRefactorings.size() == 7) {
                    selectAll.setSelected(true);
                    selectExtractClass.setSelected(false);
                    selectExtractMethod.setSelected(false);
                    selectExtractVariable.setSelected(false);
                    selectMoveMethod.setSelected(false);
                    selectIPO.setSelected(false);
                    selectString.setSelected(false);
                    selectInheritance.setSelected(false);
                    selections.put("EM", true);
                    selections.put("EC", true);
                    selections.put("EV", true);
                    selections.put("MM", true);
                    selections.put("SC", true);
                    selections.put("IPO", true);
                    selections.put("ID", true);
                    selections.put("All", true);
                    panelEC.setVisible(true);
                    panelEM.setVisible(true);
                    panelEV.setVisible(true);
                    panelIPO.setVisible(true);
                    panelID.setVisible(true);
                    warning.setVisible(true);
                }
                else {
                    selections.put("EM", false);
                    selections.put("EC", false);
                    selections.put("EV", false);
                    selections.put("MM", false);
                    selections.put("SC", false);
                    selections.put("IPO", false);
                    selections.put("ID", false);
                    selections.put("All", false);
                    panelEC.setVisible(false);
                    panelEM.setVisible(false);
                    panelEV.setVisible(false);
                    panelIPO.setVisible(false);
                    panelID.setVisible(false);
                    warning.setVisible(false);

                    for (Refactorings selectedRefactoring : SelectedRefactorings.selectedRefactorings) {
                        if (selectedRefactoring == Refactorings.ExtractClass) {
                            selectExtractClass.setSelected(true);
                            selections.put("EC", true);
                            panelEC.setVisible(true);
                        }
                        else if (selectedRefactoring == Refactorings.ExtractMethod) {
                            selectExtractMethod.setSelected(true);
                            selections.put("EM", true);
                            panelEM.setVisible(true);
                        }
                        else if (selectedRefactoring == Refactorings.ExtractVariable) {
                            selectExtractVariable.setSelected(true);
                            selections.put("EV", true);
                            panelEV.setVisible(true);
                        }
                        else if (selectedRefactoring == Refactorings.MoveMethod) {
                            selectMoveMethod.setSelected(true);
                            selections.put("MM", true);
                        }
                        else if (selectedRefactoring == Refactorings.IntroduceParamObj) {
                            selectIPO.setSelected(true);
                            selections.put("IPO", true);
                            panelIPO.setVisible(true);
                        }
                        else if (selectedRefactoring == Refactorings.StringComparison) {
                            selectString.setSelected(true);
                            selections.put("SC", true);
                        }
                        else if (selectedRefactoring == Refactorings.InheritanceToDelegation) {
                            selectInheritance.setSelected(true);
                            selections.put("ID", true);
                            panelID.setVisible(true);
                        }
                    }

                    if(selections.get("EC") && SelectedRefactorings.selectedRefactorings.size() >= 2){
                        warning.setText("By selecting Extract Class with other refactorings at the same time, you may be decreasing the plugin's performance.");
                        warning.setVisible(true);
                    }

                    if(SelectedRefactorings.selectedRefactorings.size() >= 4){
                        warning.setText("By selecting 4 or more refactorings, you may be decreasing the plugin's performance.");
                        warning.setVisible(true);
                    }
                }
            }
            else
                selectAll.setSelected(true);

            selectExtractMethod.setText("Extract Method");
            selectExtractMethod.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    panelEC.setVisible(selectExtractClass.isSelected());
                    panelEV.setVisible(selectExtractVariable.isSelected());
                    panelIPO.setVisible(selectIPO.isSelected());
                    panelID.setVisible(selectInheritance.isSelected());
                    panelEM.setVisible(selectExtractMethod.isSelected());
                    selectAll.setSelected(false);
                    selections.put("EM", selectExtractMethod.isSelected());
                    if(selectExtractMethod.isSelected()) counter++;
                    else counter--;
                    if(counter >= 4){
                        warning.setText("By selecting 4 or more refactorings, you may be decreasing the plugin's performance.");
                        warning.setVisible(true);
                    }else{
                        warning.setVisible(false);
                    }

                    if(selections.get("EM")){
                        warning.setText("By selecting Extract Method with other refactorings at the same time, you may be decreasing the plugin's performance.");
                        warning.setVisible(true);
                    }

                    if(counter == 0){
                        selectAll.setSelected(true);
                        panelEC.setVisible(true);
                        panelEM.setVisible(true);
                        panelEV.setVisible(true);
                        panelIPO.setVisible(true);
                        panelID.setVisible(true);
                        selections.put("EM", selectAll.isSelected());
                        selections.put("EC", selectAll.isSelected());
                        selections.put("EV", selectAll.isSelected());
                        selections.put("MM", selectAll.isSelected());
                        selections.put("SC", selectAll.isSelected());
                        selections.put("IPO", selectAll.isSelected());
                        selections.put("ID", selectAll.isSelected());
                        selections.put("All", selectAll.isSelected());
                        warning.setVisible(false);
                    }
                }
            });
            panel.add(selectExtractMethod, BorderLayout.WEST);

            selectExtractClass.setText("Extract Class");
            selectExtractClass.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    panelEC.setVisible(selectExtractClass.isSelected());
                    panelEV.setVisible(selectExtractVariable.isSelected());
                    panelIPO.setVisible(selectIPO.isSelected());
                    panelID.setVisible(selectInheritance.isSelected());
                    panelEM.setVisible(selectExtractMethod.isSelected());
                    selections.put("EC", selectExtractClass.isSelected());
                    selectAll.setSelected(false);
                    if(selectExtractClass.isSelected()) counter++;
                    else counter--;
                    if(counter >= 4){
                        warning.setText("By selecting 4 or more refactorings, you may be decreasing the plugin's performance.");
                        warning.setVisible(true);
                    }
                    else warning.setVisible(false);

                    if(selections.get("EC")){
                        warning.setText("By selecting Extract Class with other refactorings at the same time, you may be decreasing the plugin's performance.");
                        warning.setVisible(true);
                    }

                    if(counter == 0){
                        selectAll.setSelected(true);
                        panelEC.setVisible(true);
                        panelEM.setVisible(true);
                        panelEV.setVisible(true);
                        panelIPO.setVisible(true);
                        panelID.setVisible(true);
                        selections.put("EM", selectAll.isSelected());
                        selections.put("EC", selectAll.isSelected());
                        selections.put("EV", selectAll.isSelected());
                        selections.put("MM", selectAll.isSelected());
                        selections.put("SC", selectAll.isSelected());
                        selections.put("IPO", selectAll.isSelected());
                        selections.put("ID", selectAll.isSelected());
                        selections.put("All", selectAll.isSelected());
                        warning.setVisible(false);
                    }
                }
            });
            panel.add(selectExtractClass, BorderLayout.WEST);

            selectExtractVariable.setText("Extract Variable");
            selectExtractVariable.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    panelEC.setVisible(selectExtractClass.isSelected());
                    panelEV.setVisible(selectExtractVariable.isSelected());
                    panelIPO.setVisible(selectIPO.isSelected());
                    panelID.setVisible(selectInheritance.isSelected());
                    panelEM.setVisible(selectExtractMethod.isSelected());
                    selectAll.setSelected(false);
                    selections.put("EV", selectExtractVariable.isSelected());
                    if(selectExtractVariable.isSelected()) counter++;
                    else counter--;
                    if(counter >= 4){
                        warning.setText("By selecting 4 or more refactorings, you may be decreasing the plugin's performance.");
                        warning.setVisible(true);
                    }
                    else warning.setVisible(false);

                    if(counter == 0){
                        selectAll.setSelected(true);
                        panelEC.setVisible(true);
                        panelEM.setVisible(true);
                        panelEV.setVisible(true);
                        panelIPO.setVisible(true);
                        panelID.setVisible(true);
                        selections.put("EM", selectAll.isSelected());
                        selections.put("EC", selectAll.isSelected());
                        selections.put("EV", selectAll.isSelected());
                        selections.put("MM", selectAll.isSelected());
                        selections.put("SC", selectAll.isSelected());
                        selections.put("IPO", selectAll.isSelected());
                        selections.put("ID", selectAll.isSelected());
                        selections.put("All", selectAll.isSelected());
                        warning.setVisible(false);
                    }
                }
            });
            panel.add(selectExtractVariable, BorderLayout.WEST);

            selectMoveMethod.setText("Move Method");
            selectMoveMethod.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    panelEC.setVisible(selectExtractClass.isSelected());
                    panelEV.setVisible(selectExtractVariable.isSelected());
                    panelIPO.setVisible(selectIPO.isSelected());
                    panelID.setVisible(selectInheritance.isSelected());
                    panelEM.setVisible(selectExtractMethod.isSelected());
                    selectAll.setSelected(false);
                    selections.put("MM", selectMoveMethod.isSelected());
                    if(selectExtractClass.isSelected()) counter++;
                    else counter--;
                    if(counter >= 4){
                        warning.setText("By selecting 4 or more refactorings, you may be decreasing the plugin's performance.");
                        warning.setVisible(true);
                    }
                    else warning.setVisible(false);

                    if(counter == 0){
                        selectAll.setSelected(true);
                        panelEC.setVisible(true);
                        panelEM.setVisible(true);
                        panelEV.setVisible(true);
                        panelIPO.setVisible(true);
                        panelID.setVisible(true);
                        selections.put("EM", selectAll.isSelected());
                        selections.put("EC", selectAll.isSelected());
                        selections.put("EV", selectAll.isSelected());
                        selections.put("MM", selectAll.isSelected());
                        selections.put("SC", selectAll.isSelected());
                        selections.put("IPO", selectAll.isSelected());
                        selections.put("ID", selectAll.isSelected());
                        selections.put("All", selectAll.isSelected());
                        warning.setVisible(false);
                    }
                }
            });
            panel.add(selectMoveMethod, BorderLayout.WEST);

            selectIPO.setText("Introduce Parameter Object");
            selectIPO.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    panelEC.setVisible(selectExtractClass.isSelected());
                    panelEV.setVisible(selectExtractVariable.isSelected());
                    panelIPO.setVisible(selectIPO.isSelected());
                    panelID.setVisible(selectInheritance.isSelected());
                    panelEM.setVisible(selectExtractMethod.isSelected());
                    selections.put("IPO", selectIPO.isSelected());
                    selectAll.setSelected(false);
                    if(selectIPO.isSelected()) counter++;
                    else counter--;
                    if(counter >= 4){
                        warning.setText("By selecting 4 or more refactorings, you may be decreasing the plugin's performance.");
                        warning.setVisible(true);
                    }
                    else warning.setVisible(false);

                    if(counter == 0){
                        selectAll.setSelected(true);
                        panelEC.setVisible(true);
                        panelEM.setVisible(true);
                        panelEV.setVisible(true);
                        panelIPO.setVisible(true);
                        panelID.setVisible(true);
                        selections.put("EM", selectAll.isSelected());
                        selections.put("EC", selectAll.isSelected());
                        selections.put("EV", selectAll.isSelected());
                        selections.put("MM", selectAll.isSelected());
                        selections.put("SC", selectAll.isSelected());
                        selections.put("IPO", selectAll.isSelected());
                        selections.put("ID", selectAll.isSelected());
                        selections.put("All", selectAll.isSelected());
                        warning.setVisible(false);
                    }
                }
            });
            panel.add(selectIPO, BorderLayout.WEST);

            selectString.setText("String Comparison");
            selectString.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    panelEC.setVisible(selectExtractClass.isSelected());
                    panelEV.setVisible(selectExtractVariable.isSelected());
                    panelIPO.setVisible(selectIPO.isSelected());
                    panelID.setVisible(selectInheritance.isSelected());
                    panelEM.setVisible(selectExtractMethod.isSelected());
                    selections.put("SC", selectString.isSelected());
                    selectAll.setSelected(false);
                    if(selectString.isSelected()) counter++;
                    else counter--;
                    if(counter >= 4){
                        warning.setText("By selecting 4 or more refactorings, you may be decreasing the plugin's performance.");
                        warning.setVisible(true);
                    }
                    else warning.setVisible(false);

                    if(counter == 0){
                        selectAll.setSelected(true);
                        panelEC.setVisible(true);
                        panelEM.setVisible(true);
                        panelEV.setVisible(true);
                        panelIPO.setVisible(true);
                        panelID.setVisible(true);
                        selections.put("EM", selectAll.isSelected());
                        selections.put("EC", selectAll.isSelected());
                        selections.put("EV", selectAll.isSelected());
                        selections.put("MM", selectAll.isSelected());
                        selections.put("SC", selectAll.isSelected());
                        selections.put("IPO", selectAll.isSelected());
                        selections.put("ID", selectAll.isSelected());
                        selections.put("All", selectAll.isSelected());
                        warning.setVisible(false);
                    }
                }
            });
            panel.add(selectString, BorderLayout.WEST);

            selectInheritance.setText("Inheritance To Delegation");
            selectInheritance.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    panelEC.setVisible(selectExtractClass.isSelected());
                    panelEV.setVisible(selectExtractVariable.isSelected());
                    panelIPO.setVisible(selectIPO.isSelected());
                    panelID.setVisible(selectInheritance.isSelected());
                    panelEM.setVisible(selectExtractMethod.isSelected());
                    selections.put("ID", selectInheritance.isSelected());
                    selectAll.setSelected(false);
                    if(selectInheritance.isSelected()) counter++;
                    else counter--;
                    if(counter >= 4){
                        warning.setText("By selecting 4 or more refactorings, you may be decreasing the plugin's performance.");
                        warning.setVisible(true);
                    }
                    else warning.setVisible(false);

                    if(selections.get("ID")){
                        warning.setText("By selecting Inheritance to Delegation with other refactorings at the same time, you may be decreasing the plugin's performance.");
                        warning.setVisible(true);
                    }

                    if(counter == 0){
                        selectAll.setSelected(true);
                        panelEC.setVisible(true);
                        panelEM.setVisible(true);
                        panelEV.setVisible(true);
                        panelIPO.setVisible(true);
                        panelID.setVisible(true);
                        selections.put("EM", selectAll.isSelected());
                        selections.put("EC", selectAll.isSelected());
                        selections.put("EV", selectAll.isSelected());
                        selections.put("MM", selectAll.isSelected());
                        selections.put("SC", selectAll.isSelected());
                        selections.put("IPO", selectAll.isSelected());
                        selections.put("ID", selectAll.isSelected());
                        selections.put("All", selectAll.isSelected());
                        warning.setVisible(false);
                    }
                }
            });
            panel.add(selectInheritance, BorderLayout.WEST);

            selectAll.setText("All Refactorings");
            selectAll.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    panelEC.setVisible(selectAll.isSelected());
                    panelEM.setVisible(selectAll.isSelected());
                    panelEV.setVisible(selectAll.isSelected());
                    panelIPO.setVisible(selectAll.isSelected());
                    panelID.setVisible(selectAll.isSelected());
                    warning.setText("By selecting all available refactorings, you may be decreasing the plugin's performance.");
                    warning.setVisible(selectAll.isSelected());
                    selections.put("EM", selectAll.isSelected());
                    selections.put("EC", selectAll.isSelected());
                    selections.put("EV", selectAll.isSelected());
                    selections.put("MM", selectAll.isSelected());
                    selections.put("SC", selectAll.isSelected());
                    selections.put("IPO", selectAll.isSelected());
                    selections.put("ID", selectAll.isSelected());
                    selections.put("All", selectAll.isSelected());
                    selectExtractMethod.setSelected(false);
                    selectExtractClass.setSelected(false);
                    selectExtractVariable.setSelected(false);
                    selectMoveMethod.setSelected(false);
                    selectIPO.setSelected(false);
                    selectInheritance.setSelected(false);
                    selectString.setSelected(false);
                    counter = 0;
                }
            });
            panel.add(selectAll, BorderLayout.WEST);

            //panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Included Refactorings"));

            boxWithExecutors.add(panel);
            panelThresholds.add(panelEC);
            panelThresholds.add(panelEM);
            panelThresholds.add(panelEV);
            panelThresholds.add(panelIPO);
            panelThresholds.add(panelID);
            JScrollPane pane = new JBScrollPane(panelThresholds, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            boxWithExecutors.add(pane);
            boxWithExecutors.setPreferredSize(new Dimension(200, 250));

            //JPanel panel4 = new JPanel(new GridLayout(3, 2));
            JPanel panel4 = new JPanel(new GridLayout(2, 2));

            panel4.add(label_number, BorderLayout.WEST);
            panel4.add(textField_number, BorderLayout.EAST);
            /*panel4.add(label_seconds, BorderLayout.WEST);
            panel4.add(textField_seconds, BorderLayout.EAST);*/

            JPanel panel5 = new JPanel(new GridLayout(1, 1));

            selectedColorBlindYes.setText("Color blind");
            selectedColorBlindYes.setSelected(false);

            panel5.add(selectedColorBlindYes);
            panel4.add(panel5);

            panel4.setPreferredSize(new Dimension(200, 75));

            panel4.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), "Further Details"));

            if(!(warning.getText().length() > 0 && warning.getText().contains("Extract Class")))
                warning.setText("By selecting all available refactorings, you may be decreasing the plugin's performance.");
            warning.setVisible(true);
            boxWithExecutors.add(panel4);
            boxWithExecutors.add(warning);
            box.add(boxWithExecutors);

            selections.put("EM", false);
            selections.put("EC", false);
            selections.put("EV", false);
            selections.put("MM", false);
            selections.put("SC", false);
            selections.put("IPO", false);
            selections.put("ID", false);
            selections.put("All", false);
            counter = 0;

            return box;
        }
    }
}
