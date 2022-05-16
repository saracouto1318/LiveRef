package com.ui;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.VisualPosition;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameterList;
import com.intellij.psi.PsiStatement;
import com.intellij.psi.util.PsiTreeUtil;
import com.refactorings.candidates.*;
import com.refactorings.candidates.utils.Severity;
import com.utils.MyRange;
import com.utils.ThresholdsCandidates;
import com.utils.Utilities;
import com.utils.Values;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.io.IOException;
import java.util.ArrayList;

public class RefactoringPanel extends DialogWrapper {
    public final JTextArea label = new JTextArea();
    public ArrayList<Severity> candidates;
    public Editor editor;
    public JTable refactorings = new JTable();
    public String name = "";
    public int row = 0;
    public int column = 0;
    public DefaultTableModel model = null;
    public int length = 0;
    Utilities utils = new Utilities();

    public RefactoringPanel(ArrayList<Severity> candidates, Editor editor) {
        super(false);
        this.editor = editor;
        this.candidates = candidates;
        setTitle("Live Refactoring Candidates");
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        Box boxWithExecutors = Box.createVerticalBox();

        JPanel main = new JPanel(new GridLayout(1, 1));
        JPanel refactoringPanel = new JPanel(new GridLayout(1, 0));
        //JPanel visibilityAndName = new JPanel(new GridLayout(1, 0));

        Border border = BorderFactory.createTitledBorder("Refactoring Opportunities");
        refactoringPanel.setBorder(border);

        ArrayList<String> columnNamesAux = new ArrayList<>();
        columnNamesAux.add("Refactoring");
        columnNamesAux.add("Lines");
        if(Values.withColors)
            columnNamesAux.add("Severity");

        if(!ThresholdsCandidates.colorBlind && Values.withColors)
            columnNamesAux.add("Color");
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
            ImageIcon icon;

            if (candidate.candidate instanceof ExtractMethodCandidate) {
                MyRange range = ((ExtractMethodCandidate) candidate.candidate).range;
                icon = getImageIconByType(candidate.indexColorGutter, "EM");

                Object[] obj;
                int statements = 0;
                for (PsiStatement node : ((ExtractMethodCandidate) candidate.candidate).nodes) {
                    statements += PsiTreeUtil.findChildrenOfType(node, PsiStatement.class).size();
                }
                if (!ThresholdsCandidates.colorBlind) {
                    if(Values.withColors)
                        obj = new Object[]{"Extract Method", range.start.line + " -> " + range.end.line,
                                String.format("%.2f", candidate.severity),
                                icon, statements
                        };
                    else
                        obj = new Object[]{"Extract Method", range.start.line + " -> " + range.end.line, statements
                        };
                } else {
                    if(Values.withColors)
                        obj = new Object[]{"Extract Method", range.start.line + " -> " + range.end.line,
                                String.format("%.2f", candidate.severity), statements
                        };
                    else
                        obj = new Object[]{"Extract Method", range.start.line + " -> " + range.end.line, statements};
                }
                model.addRow(obj);
            } else if (candidate.candidate instanceof ExtractVariableCandidate) {
                MyRange range = ((ExtractVariableCandidate) candidate.candidate).range;
                icon = getImageIconByType(candidate.indexColorGutter, "EV");

                /*LogicalPosition start = ((ExtractVariableCandidate) candidate.candidate).range.start;
                LogicalPosition end = ((ExtractVariableCandidate) candidate.candidate).range.end;*/
                Object[] obj;
                if (!ThresholdsCandidates.colorBlind) {
                    if(Values.withColors)
                        obj = new Object[]{"Extract Variable", range.start.line,
                                String.format("%.2f", candidate.severity), icon, "1"};
                    else
                        obj = new Object[]{"Extract Variable", range.start.line, "1"};
                } else {
                    if(Values.withColors)
                        obj = new Object[]{"Extract Variable", range.start.line,
                                String.format("%.2f", candidate.severity), "1"};
                    else
                        obj = new Object[]{"Extract Variable", range.start.line, "1"};
                }

                model.addRow(obj);
            } else if (candidate.candidate instanceof ExtractClassCandidate) {
                icon = getImageIconByType(candidate.indexColorGutter, "EC");

                /*LogicalPosition start = editor.offsetToLogicalPosition(((ExtractClassCandidate) candidate.candidate).targetEntities.get(0).getTextRange().getStartOffset());
                LogicalPosition end = editor.offsetToLogicalPosition(((ExtractClassCandidate) candidate.candidate).targetEntities.get(((ExtractClassCandidate) candidate.candidate).targetEntities.size() - 1).getTextRange().getEndOffset());*/
                Object[] obj;
                if (!ThresholdsCandidates.colorBlind) {
                    if(Values.withColors)
                        obj = new Object[]{"Extract Class", "-",
                                String.format("%.2f", candidate.severity), icon,
                                Integer.toString(((ExtractClassCandidate) candidate.candidate).targetEntities.size())};
                    else
                        obj = new Object[]{"Extract Class", "-",
                                Integer.toString(((ExtractClassCandidate) candidate.candidate).targetEntities.size())};

                } else {
                    if(Values.withColors)
                        obj = new Object[]{"Extract Class", "-",
                                String.format("%.2f", candidate.severity), Integer.toString(((ExtractClassCandidate) candidate.candidate).targetEntities.size())};
                    else
                        obj = new Object[]{"Extract Class", "-", Integer.toString(((ExtractClassCandidate) candidate.candidate).targetEntities.size())};
                }

                model.addRow(obj);
            }
            if (candidate.candidate instanceof MoveMethodCandidate) {
                MyRange range = ((MoveMethodCandidate) candidate.candidate).range;
                icon = getImageIconByType(candidate.indexColorGutter, "MM");

                Object[] obj;
                int statements = 0;
                for (PsiStatement node : ((MoveMethodCandidate) candidate.candidate).nodes) {
                    statements += PsiTreeUtil.findChildrenOfType(node, PsiStatement.class).size();
                }
                if (!ThresholdsCandidates.colorBlind) {
                    if(Values.withColors)
                        obj = new Object[]{"Move Method", range.start.line + " -> " + range.end.line,
                                String.format("%.2f", candidate.severity),
                                icon, statements
                        };
                    else
                        obj = new Object[]{"Move Method", range.start.line + " -> " + range.end.line, statements};
                } else {
                    if(Values.withColors)
                        obj = new Object[]{"Move Method", range.start.line + " -> " + range.end.line,
                                String.format("%.2f", candidate.severity), statements
                        };
                    else
                        obj = new Object[]{"Move Method", range.start.line + " -> " + range.end.line, statements
                        };
                }

                model.addRow(obj);
            }
            else if (candidate.candidate instanceof IntroduceParamObjCandidate) {
                int line = editor.offsetToLogicalPosition(((IntroduceParamObjCandidate) candidate.candidate).method.getTextRange().getStartOffset()).line;
                icon = getImageIconByType(candidate.indexColorGutter, "EV");

                /*LogicalPosition start = ((ExtractVariableCandidate) candidate.candidate).range.start;
                LogicalPosition end = ((ExtractVariableCandidate) candidate.candidate).range.end;*/
                Object[] obj;
                if (!ThresholdsCandidates.colorBlind) {
                    if(Values.withColors)
                        obj = new Object[]{"Introduce Parameter Obj", line,
                                String.format("%.2f", candidate.severity), icon, "1"};
                    else
                        obj = new Object[]{"Introduce Parameter Obj", line, "1"};
                } else {
                    if(Values.withColors)
                        obj = new Object[]{"Introduce Parameter Obj", line,
                            String.format("%.2f", candidate.severity), "1"};
                    else
                        obj = new Object[]{"Introduce Parameter Obj", line, "1"};
                }

                model.addRow(obj);
            }
        }

        this.refactorings.setModel(model);
        this.refactorings.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        if(!ThresholdsCandidates.colorBlind){
            if(Values.withColors) {
                this.refactorings.getColumnModel().getColumn(0).setPreferredWidth(140);
                this.refactorings.getColumnModel().getColumn(1).setPreferredWidth(40);
                this.refactorings.getColumnModel().getColumn(2).setPreferredWidth(1);
                this.refactorings.getColumnModel().getColumn(3).setPreferredWidth(6);
            }
            else {
                this.refactorings.getColumnModel().getColumn(0).setPreferredWidth(140);
                this.refactorings.getColumnModel().getColumn(1).setPreferredWidth(40);
                this.refactorings.getColumnModel().getColumn(2).setPreferredWidth(6);
            }
        }
        else {
            if(Values.withColors) {
                this.refactorings.getColumnModel().getColumn(0).setPreferredWidth(100);
                this.refactorings.getColumnModel().getColumn(1).setPreferredWidth(40);
                this.refactorings.getColumnModel().getColumn(2).setPreferredWidth(1);
                this.refactorings.getColumnModel().getColumn(3).setPreferredWidth(6);
            }
            else{
                this.refactorings.getColumnModel().getColumn(0).setPreferredWidth(100);
                this.refactorings.getColumnModel().getColumn(1).setPreferredWidth(40);
                this.refactorings.getColumnModel().getColumn(2).setPreferredWidth(6);
            }
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
                    //label.setText(createText(objects));
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
        int height = 0;

        if(this.candidates.size() <= 2)
            height = 100;
        else if(this.candidates.size() < 5)
            height = this.candidates.size() * 40;
        else
            height = 180;

        box.setPreferredSize(new Dimension(400, height));
        box.add(boxWithExecutors);

        return box;
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

        result.toString().trim();
        return result.toString();
    }

    public void createHighlight(Object[] option) {
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
                        (option[4]).toString().equals(Integer.toString(statements))) {
                    VisualPosition positionStart = new VisualPosition(lineStart, columnStart);
                    VisualPosition positionEnd = new VisualPosition(lineEnd, columnEnd);
                    int startOffSet = ((ExtractMethodCandidate) candidate.candidate).nodes.get(0).getTextRange().getStartOffset();
                    int endOffSet = ((ExtractMethodCandidate) candidate.candidate).nodes.get(((ExtractMethodCandidate) candidate.candidate).nodes.size() - 1).getTextRange().getEndOffset();

                    editor.getSelectionModel().setSelection(positionStart, startOffSet, positionEnd, endOffSet);
                    IdeFocusManager.getInstance(editor.getProject()).requestFocus(editor.getSelectionModel().getEditor().getContentComponent(), true);

                    break;
                }
            } else if (((String) option[0]).contains("Extract Variable") && candidate.candidate instanceof ExtractVariableCandidate) {
                int lineStart = ((ExtractVariableCandidate) candidate.candidate).range.start.line;
                int columnStart = ((ExtractVariableCandidate) candidate.candidate).range.start.column;
                int lineEnd = ((ExtractVariableCandidate) candidate.candidate).range.end.line;
                int columnEnd = ((ExtractVariableCandidate) candidate.candidate).range.end.column;

                if (((String)option[1]).contains(Integer.toString(lineStart)) &&
                        option[4].toString().equals("1")) {

                    VisualPosition positionStart = new VisualPosition(lineStart, columnStart);
                    VisualPosition positionEnd = new VisualPosition(lineEnd, columnEnd);
                    int startOffSet = ((ExtractVariableCandidate) candidate.candidate).node.getTextRange().getStartOffset();
                    int endOffSet = ((ExtractVariableCandidate) candidate.candidate).node.getTextRange().getEndOffset();

                    editor.getSelectionModel().setSelection(positionStart, startOffSet, positionEnd, endOffSet);
                    IdeFocusManager.getInstance(editor.getProject()).requestFocus(editor.getSelectionModel().getEditor().getContentComponent(), true);

                    break;
                }
            } else if (((String) option[0]).contains("Extract Class") && candidate.candidate instanceof ExtractClassCandidate) {
                LogicalPosition start = editor.offsetToLogicalPosition(((ExtractClassCandidate) candidate.candidate).targetEntities.get(0).getTextRange().getStartOffset());
                LogicalPosition end = editor.offsetToLogicalPosition(((ExtractClassCandidate) candidate.candidate).targetEntities.get(((ExtractClassCandidate) candidate.candidate).targetEntities.size() - 1).getTextRange().getEndOffset());

                if (((String)option[4]).contains(Integer.toString(start.line)) &&
                        ((String)option[1]).contains(Integer.toString(end.line)) &&
                        (option[4]).toString().equals(Integer.toString(((ExtractClassCandidate) candidate.candidate).targetEntities.size()))) {

                    for (PsiField targetEntity : ((ExtractClassCandidate) candidate.candidate).targetAttributes) {
                        int lineStart = editor.offsetToLogicalPosition(targetEntity.getTextRange().getStartOffset()).line;
                        int columnStart = editor.offsetToLogicalPosition((targetEntity).getTextRange().getStartOffset()).column;
                        int lineEnd = editor.offsetToLogicalPosition((targetEntity).getTextRange().getEndOffset()).line;
                        int columnEnd = editor.offsetToLogicalPosition((targetEntity).getTextRange().getEndOffset()).column;
                    }

                    for (PsiMethod targetEntity : ((ExtractClassCandidate) candidate.candidate).targetMethods) {
                        int lineStart = editor.offsetToLogicalPosition((targetEntity).getTextRange().getStartOffset()).line;
                        int columnStart = editor.offsetToLogicalPosition((targetEntity).getTextRange().getStartOffset()).column;
                        int lineEnd = editor.offsetToLogicalPosition((targetEntity).getTextRange().getEndOffset()).line;
                        int columnEnd = editor.offsetToLogicalPosition((targetEntity).getTextRange().getEndOffset()).column;
                    }

                    break;
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
                        option[4].toString().equals("1")) {

                    VisualPosition positionStart = new VisualPosition(lineStart, columnStart);
                    VisualPosition positionEnd = new VisualPosition(lineEnd, columnEnd);
                    int startOffSet = ((ExtractVariableCandidate) candidate.candidate).node.getTextRange().getStartOffset();
                    int endOffSet = ((ExtractVariableCandidate) candidate.candidate).node.getTextRange().getEndOffset();

                    editor.getSelectionModel().setSelection(positionStart, startOffSet, positionEnd, endOffSet);
                    IdeFocusManager.getInstance(editor.getProject()).requestFocus(editor.getSelectionModel().getEditor().getContentComponent(), true);


                    break;
                }
            }
            if (((String) option[0]).contains("Move Method") && candidate.candidate instanceof MoveMethodCandidate) {
                int lineStart = ((MoveMethodCandidate) candidate.candidate).range.start.line;
                int columnStart = ((MoveMethodCandidate) candidate.candidate).range.start.column;
                int lineEnd = ((MoveMethodCandidate) candidate.candidate).range.end.line;
                int columnEnd = ((MoveMethodCandidate) candidate.candidate).range.end.column;

                int statements = 0;
                for (PsiStatement node : ((ExtractMethodCandidate) candidate.candidate).nodes) {
                    statements += PsiTreeUtil.findChildrenOfType(node, PsiStatement.class).size();
                }
                if (((String)option[1]).contains(Integer.toString(lineStart)) &&
                        ((String)option[1]).contains(Integer.toString(lineEnd)) &&
                        (option[4]).toString().equals(Integer.toString(statements))) {

                    VisualPosition positionStart = new VisualPosition(lineStart, columnStart);
                    VisualPosition positionEnd = new VisualPosition(lineEnd, columnEnd);
                    int startOffSet = ((ExtractMethodCandidate) candidate.candidate).nodes.get(0).getTextRange().getStartOffset();
                    int endOffSet = ((ExtractMethodCandidate) candidate.candidate).nodes.get(((ExtractMethodCandidate) candidate.candidate).nodes.size() - 1).getTextRange().getEndOffset();

                    editor.getSelectionModel().setSelection(positionStart, startOffSet, positionEnd, endOffSet);
                    IdeFocusManager.getInstance(editor.getProject()).requestFocus(editor.getSelectionModel().getEditor().getContentComponent(), true);

                    break;
                }
            }
        }
    }

    public Object selectedCandidate() {
        return candidates.get(row);
    }
}
