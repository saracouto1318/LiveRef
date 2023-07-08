package com.ui;

import com.analysis.candidates.*;
import com.core.MyRange;
import com.core.Severity;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.VisualPosition;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.psi.PsiParameterList;
import com.intellij.psi.PsiStatement;
import com.intellij.psi.util.PsiTreeUtil;
import com.utils.UtilitiesOverall;
import com.utils.importantValues.Values;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.util.ArrayList;

public class RefactoringPanel extends DialogWrapper {
    public ArrayList<Severity> candidates;
    public Editor editor;
    public JTable refactorings = new JTable();
    public String name = "";
    public int row = 0;
    public int column = 0;
    public DefaultTableModel model = null;
    public int length = 0;
    UtilitiesOverall utils = new UtilitiesOverall();

    private class MyCustomAction extends DialogWrapperAction {
        protected MyCustomAction() {
            super("More Info");
            putValue(Action.NAME, "More Info");
        }

        @Override
        protected void doAction(ActionEvent e) {
            Severity severity = (Severity) selectedCandidate();
            MetricsTable table = new MetricsTable(severity, editor);
            table.show();
        }
    }

    public RefactoringPanel(ArrayList<Severity> candidates, Editor editor) {
        super(true);
        this.editor = editor;
        this.candidates = candidates;
        setTitle("Live Refactoring Candidates");
        init();
    }

    /*@NotNull
    protected Action[] createLeftSideActions() {
        MyCustomAction action = new MyCustomAction();
        return new Action[] { action };
    }*/

    @Override
    protected @Nullable JComponent createCenterPanel() {
        Box boxWithExecutors = Box.createVerticalBox();

        JPanel main = new JPanel(new GridLayout(1, 1));
        JPanel refactoringPanel = new JPanel(new GridLayout(1, 0));
        JPanel panelTable = new JPanel();
        panelTable.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Metrics Evolution", TitledBorder.CENTER, TitledBorder.TOP));
        //JPanel visibilityAndName = new JPanel(new GridLayout(1, 0));

        Border border = BorderFactory.createTitledBorder("Refactoring Opportunities");
        refactoringPanel.setBorder(border);

        ArrayList<String> columnNamesAux = new ArrayList<>();
        columnNamesAux.add("Refactoring");
        columnNamesAux.add("Lines");
        columnNamesAux.add("Severity");
        columnNamesAux.add("Number of Elements");

        String[] columnNames = new String[columnNamesAux.size()];
        for(int i = 0; i < columnNamesAux.size(); i++)
            columnNames[i] = columnNamesAux.get(i);

        this.length = columnNames.length;
        this.model = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                if (getRowCount() > 0) {
                    Object value = getValueAt(0, column);
                    if (value != null) {
                        return getValueAt(0, column).getClass();
                    }
                }

                return super.getColumnClass(column);
            }
        };

        for (Severity candidate : this.candidates) {

            if (candidate.candidate instanceof ExtractMethodCandidate) {
                MyRange range = ((ExtractMethodCandidate) candidate.candidate).range;

                int statements = 0;
                for (PsiStatement node : ((ExtractMethodCandidate) candidate.candidate).nodes) {
                    statements += PsiTreeUtil.findChildrenOfType(node, PsiStatement.class).size();
                }
                Object[] obj = new Object[]{"Extract Method", range.start.line + " -> " + range.end.line,
                        String.format("%.2f", candidate.severity), statements
                };
                model.addRow(obj);
            }
            else if (candidate.candidate instanceof ExtractVariableCandidate) {
                MyRange range = ((ExtractVariableCandidate) candidate.candidate).range;

                /*LogicalPosition start = ((ExtractVariableCandidate) candidate.candidate).range.start;
                LogicalPosition end = ((ExtractVariableCandidate) candidate.candidate).range.end;*/
                Object[] obj = new Object[]{"Extract Variable", "(" + range.start.line + ", " + range.start.column + ") -> (" + range.end.line + ", " + range.end.column + ")",
                        String.format("%.2f", candidate.severity), "1"};

                model.addRow(obj);
            }
            else if (candidate.candidate instanceof ExtractClassCandidate) {

                /*LogicalPosition start = editor.offsetToLogicalPosition(((ExtractClassCandidate) candidate.candidate).targetEntities.get(0).getTextRange().getStartOffset());
                LogicalPosition end = editor.offsetToLogicalPosition(((ExtractClassCandidate) candidate.candidate).targetEntities.get(((ExtractClassCandidate) candidate.candidate).targetEntities.size() - 1).getTextRange().getEndOffset());*/
                Object[] obj = new Object[]{"Extract Class", "-",
                        String.format("%.2f", candidate.severity), Integer.toString(((ExtractClassCandidate) candidate.candidate).targetMethods.size())};

                model.addRow(obj);
            }
            else if (candidate.candidate instanceof MoveMethodCandidate) {
                MyRange range = ((MoveMethodCandidate) candidate.candidate).range;

                int statements = PsiTreeUtil.findChildrenOfType(((MoveMethodCandidate) candidate.candidate).method, PsiStatement.class).size();
                Object[] obj = new Object[]{"Move Method (" + ((MoveMethodCandidate) candidate.candidate).originalClass.getName() + "-> " +
                        ((MoveMethodCandidate) candidate.candidate).targetClass.getName() + ")", range.start.line + " -> " + range.end.line,
                        String.format("%.2f", candidate.severity), statements
                };

                model.addRow(obj);
            }
            else if (candidate.candidate instanceof IntroduceParamObjCandidate) {
                int line = editor.offsetToLogicalPosition(((IntroduceParamObjCandidate) candidate.candidate).method.getTextRange().getStartOffset()).line;
                /*LogicalPosition start = ((ExtractVariableCandidate) candidate.candidate).range.start;
                LogicalPosition end = ((ExtractVariableCandidate) candidate.candidate).range.end;*/
                Object[] obj = new Object[]{"Introduce Parameter Obj", line,
                        String.format("%.2f", candidate.severity), "1"};

                model.addRow(obj);
            }
            else if (candidate.candidate instanceof StringComparisonCandidate) {
                MyRange range = ((StringComparisonCandidate) candidate.candidate).range;
                /*LogicalPosition start = ((ExtractVariableCandidate) candidate.candidate).range.start;
                LogicalPosition end = ((ExtractVariableCandidate) candidate.candidate).range.end;*/
                Object[] obj = new Object[]{"String Comparison", range.start.line,
                        String.format("%.2f", candidate.severity), "1"};

                model.addRow(obj);
            }
            else if (candidate.candidate instanceof InheritanceToDelegationCandidate) {
                int lineStart = editor.offsetToLogicalPosition(((InheritanceToDelegationCandidate)candidate.candidate)._class.getTextRange().getStartOffset()).line;
                int lineEnd = editor.offsetToLogicalPosition(((InheritanceToDelegationCandidate)candidate.candidate)._class.getTextRange().getEndOffset()).line;
                Object[] obj = new Object[]{"Inheritance To Delegation", "-",
                        String.format("%.2f", candidate.severity), "1"};

                model.addRow(obj);
            }
        }

        this.refactorings.setModel(model);
        this.refactorings.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        if(!Values.colorBlind){
            this.refactorings.getColumnModel().getColumn(0).setPreferredWidth(160);
            this.refactorings.getColumnModel().getColumn(1).setPreferredWidth(60);
            this.refactorings.getColumnModel().getColumn(2).setPreferredWidth(1);
            this.refactorings.getColumnModel().getColumn(3).setPreferredWidth(6);
        }
        else {
            this.refactorings.getColumnModel().getColumn(0).setPreferredWidth(160);
            this.refactorings.getColumnModel().getColumn(1).setPreferredWidth(60);
            this.refactorings.getColumnModel().getColumn(2).setPreferredWidth(1);
            this.refactorings.getColumnModel().getColumn(3).setPreferredWidth(6);
        }

        refactorings.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int selectedRow = refactorings.rowAtPoint(evt.getPoint());
                int selectedCol = refactorings.columnAtPoint(evt.getPoint());
                if (selectedRow >= 0 && selectedCol >= 0) {
                    row = selectedRow;
                    column = selectedCol;
                    Object[] objects = getObjects(row);
                    name = getName(objects);
                    createHighlight(objects);
                }
            }
        });

        JScrollPane scrollableArea = new JScrollPane(this.refactorings);
        this.refactorings.doLayout();

        refactoringPanel.add(scrollableArea);

        //visibilityAndName.add(refactoringPanel, BorderLayout.CENTER);

        main.add(refactoringPanel, BorderLayout.CENTER);

        setOKButtonText("Refactor");
        setOKActionEnabled(true);
        boxWithExecutors.add(main);

        Box box = Box.createHorizontalBox();
        int height;

        if(this.candidates.size() <= 2)
            height = 100;
        else if(this.candidates.size() < 5)
            height = this.candidates.size() * 36;
        else
            height = 200;

        box.setPreferredSize(new Dimension(600, height));
        box.add(main);
        return box;
    }

    public Object[] getObjects(int row) {
        Object[] object = new Object[this.length];
        for (int i = 0; i < this.length; i++)
            object[i] = this.model.getValueAt(row, i);

        return object;
    }

    public String getName(Object[] option) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < option.length; i++) {
            if (i != 2)
                result.append(option[i]).append(" ");
        }

        return result.toString().trim();
    }

    public Severity createHighlight(Object[] option) {
        for (Severity candidate : candidates) {
            if (((String) option[0]).contains("Extract Method") && candidate.candidate instanceof ExtractMethodCandidate) {
                int lineStart = ((ExtractMethodCandidate) candidate.candidate).range.start.line;
                int columnStart = ((ExtractMethodCandidate) candidate.candidate).range.start.column;
                int lineEnd = ((ExtractMethodCandidate) candidate.candidate).range.end.line;
                int columnEnd = ((ExtractMethodCandidate) candidate.candidate).range.end.column;

                int statements = 0;
                for (PsiStatement node : ((ExtractMethodCandidate) candidate.candidate).nodes) {
                    statements += PsiTreeUtil.findChildrenOfType(node, PsiStatement.class).size();
                }

                if (((String) option[1]).contains(Integer.toString(lineStart)) &&
                        ((String) option[1]).contains(Integer.toString(lineEnd)) &&
                        (option[3]).toString().equals(Integer.toString(statements))) {
                    VisualPosition positionStart = new VisualPosition(lineStart, columnStart);
                    VisualPosition positionEnd = new VisualPosition(lineEnd, columnEnd);
                    int startOffSet = ((ExtractMethodCandidate) candidate.candidate).nodes.get(0).getTextRange().getStartOffset();
                    int endOffSet = ((ExtractMethodCandidate) candidate.candidate).nodes.get(((ExtractMethodCandidate) candidate.candidate).nodes.size() - 1).getTextRange().getEndOffset();

                    editor.getSelectionModel().setSelection(positionStart, startOffSet, positionEnd, endOffSet);
                    IdeFocusManager.getInstance(editor.getProject()).requestFocus(editor.getSelectionModel().getEditor().getContentComponent(), true);
                    return candidate;
                }
            }
            else if (((String) option[0]).contains("Extract Variable") && candidate.candidate instanceof ExtractVariableCandidate) {
                int lineStart = ((ExtractVariableCandidate) candidate.candidate).range.start.line;
                int columnStart = ((ExtractVariableCandidate) candidate.candidate).range.start.column;
                int lineEnd = ((ExtractVariableCandidate) candidate.candidate).range.end.line;
                int columnEnd = ((ExtractVariableCandidate) candidate.candidate).range.end.column;

                if ((option[1].toString()).contains(Integer.toString(lineStart)) && (option[1].toString()).contains(Integer.toString(columnStart)) &&
                        (option[1].toString()).contains(Integer.toString(lineEnd)) && (option[1].toString()).contains(Integer.toString(columnEnd)) && (option[3].toString()).equals("1")) {

                    VisualPosition positionStart = new VisualPosition(lineStart, columnStart);
                    VisualPosition positionEnd = new VisualPosition(lineEnd, columnEnd);
                    int startOffSet = ((ExtractVariableCandidate) candidate.candidate).node.getTextRange().getStartOffset();
                    int endOffSet = ((ExtractVariableCandidate) candidate.candidate).node.getTextRange().getEndOffset();

                    editor.getSelectionModel().setSelection(positionStart, startOffSet, positionEnd, endOffSet);
                    IdeFocusManager.getInstance(editor.getProject()).requestFocus(editor.getSelectionModel().getEditor().getContentComponent(), true);
                    return candidate;
                }
            }
            else if (((String) option[0]).contains("Introduce Parameter Obj") && candidate.candidate instanceof IntroduceParamObjCandidate) {
                IntroduceParamObjCandidate param = (IntroduceParamObjCandidate)candidate.candidate;
                PsiParameterList list = param.method.getParameterList();
                int lineStart = editor.offsetToLogicalPosition(list.getTextRange().getStartOffset()).line;
                int columnStart = editor.offsetToLogicalPosition(list.getTextRange().getStartOffset()).column;
                int lineEnd = editor.offsetToLogicalPosition(list.getTextRange().getEndOffset()).line;
                int columnEnd = editor.offsetToLogicalPosition(list.getTextRange().getEndOffset()).column;

                if (((String)option[1]).contains(Integer.toString(lineStart)) &&
                        option[3].toString().equals("1")) {

                    VisualPosition positionStart = new VisualPosition(lineStart, columnStart);
                    VisualPosition positionEnd = new VisualPosition(lineEnd, columnEnd);
                    int startOffSet = ((ExtractVariableCandidate) candidate.candidate).node.getTextRange().getStartOffset();
                    int endOffSet = ((ExtractVariableCandidate) candidate.candidate).node.getTextRange().getEndOffset();

                    editor.getSelectionModel().setSelection(positionStart, startOffSet, positionEnd, endOffSet);
                    IdeFocusManager.getInstance(editor.getProject()).requestFocus(editor.getSelectionModel().getEditor().getContentComponent(), true);
                    return candidate;
                }

            }
            else if (((String) option[0]).contains("Move Method") && candidate.candidate instanceof MoveMethodCandidate) {
                int lineStart = ((MoveMethodCandidate) candidate.candidate).range.start.line;
                int columnStart = ((MoveMethodCandidate) candidate.candidate).range.start.column;
                int lineEnd = ((MoveMethodCandidate) candidate.candidate).range.end.line;
                int columnEnd = ((MoveMethodCandidate) candidate.candidate).range.end.column;

                int statements = PsiTreeUtil.findChildrenOfType(((MoveMethodCandidate) candidate.candidate).method, PsiStatement.class).size();
                if ((option[1].toString()).contains(Integer.toString(lineStart)) &&
                        (option[1].toString()).contains(Integer.toString(lineEnd)) &&
                        (option[3]).toString().equals(Integer.toString(statements))) {

                    VisualPosition positionStart = new VisualPosition(lineStart, columnStart);
                    VisualPosition positionEnd = new VisualPosition(lineEnd, columnEnd);
                    int startOffSet = ((MoveMethodCandidate) candidate.candidate).method.getTextRange().getStartOffset();
                    int endOffSet = ((MoveMethodCandidate) candidate.candidate).method.getTextRange().getEndOffset();

                    editor.getSelectionModel().setSelection(positionStart, startOffSet, positionEnd, endOffSet);
                    IdeFocusManager.getInstance(editor.getProject()).requestFocus(editor.getSelectionModel().getEditor().getContentComponent(), true);
                    return candidate;
                }
            }
            else if (((String) option[0]).contains("String Comparison") && candidate.candidate instanceof StringComparisonCandidate) {
                int lineStart = ((StringComparisonCandidate) candidate.candidate).range.start.line;
                int columnStart = ((StringComparisonCandidate) candidate.candidate).range.start.column;
                int lineEnd = ((StringComparisonCandidate) candidate.candidate).range.end.line;
                int columnEnd = ((StringComparisonCandidate) candidate.candidate).range.end.column;

                if (((String)option[1]).contains(Integer.toString(lineStart)) &&
                        option[3].toString().equals("1")) {

                    VisualPosition positionStart = new VisualPosition(lineStart, columnStart);
                    VisualPosition positionEnd = new VisualPosition(lineEnd, columnEnd);
                    int startOffSet = ((StringComparisonCandidate) candidate.candidate).node.getTextRange().getStartOffset();
                    int endOffSet = ((StringComparisonCandidate) candidate.candidate).node.getTextRange().getEndOffset();

                    editor.getSelectionModel().setSelection(positionStart, startOffSet, positionEnd, endOffSet);
                    IdeFocusManager.getInstance(editor.getProject()).requestFocus(editor.getSelectionModel().getEditor().getContentComponent(), true);
                    return candidate;
                }

            }
            else if (((String) option[0]).contains("Inheritance To Delegation") && candidate.candidate instanceof InheritanceToDelegationCandidate) {
                InheritanceToDelegationCandidate candidateRefusedBequest = (InheritanceToDelegationCandidate)candidate.candidate;
                int lineStart = editor.offsetToLogicalPosition(candidateRefusedBequest._class.getTextRange().getStartOffset()).line;
                int columnStart = editor.offsetToLogicalPosition(candidateRefusedBequest._class.getTextRange().getStartOffset()).column;
                int lineEnd = editor.offsetToLogicalPosition(candidateRefusedBequest._class.getTextRange().getEndOffset()).line;
                int columnEnd = editor.offsetToLogicalPosition(candidateRefusedBequest._class.getTextRange().getEndOffset()).column;

                if (((String)option[1]).contains(Integer.toString(lineStart)) &&
                        ((String)option[1]).contains(Integer.toString(lineEnd)) &&
                        (option[3]).toString().equals("1")) {

                    VisualPosition positionStart = new VisualPosition(lineStart, columnStart);
                    VisualPosition positionEnd = new VisualPosition(lineEnd, columnEnd);
                    int startOffSet = candidateRefusedBequest._class.getTextRange().getStartOffset();
                    int endOffSet = candidateRefusedBequest._class.getTextRange().getEndOffset();

                    editor.getSelectionModel().setSelection(positionStart, startOffSet, positionEnd, endOffSet);
                    IdeFocusManager.getInstance(editor.getProject()).requestFocus(editor.getSelectionModel().getEditor().getContentComponent(), true);
                    return candidate;
                }

            }
        }

        return null;
    }

    public Object selectedCandidate() {
        return candidates.get(row);
    }
}
