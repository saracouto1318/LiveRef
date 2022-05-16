package com.ui;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.*;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiStatement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.xdebugger.ui.DebuggerColors;
import com.refactorings.*;
import com.refactorings.candidates.*;
import com.refactorings.candidates.utils.Severity;
import com.utils.Utilities;
import com.utils.Values;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;

public class ColoringGutters {
    public Editor editor;
    public ArrayList<Gutter> gutters;
    public ArrayList<Severity> severities;

    public ColoringGutters(ArrayList<Gutter> gutters, Editor editor, ArrayList<Severity> severities) {
        this.editor = editor;
        this.gutters = gutters;
        this.severities = severities;
    }

    public void initGutters() {
        ArrayList<RangeHighlighter> highlithers = new ArrayList<>();
        for (Gutter gutter : this.gutters) {
            MarkupModel markupModel = this.editor.getMarkupModel();
            RangeHighlighter rangeHighlighter = markupModel.addLineHighlighter(gutter.line, HighlighterLayer.FIRST, null);
            this.addGutterIcon(rangeHighlighter, gutter.node, gutter.icon, gutter.description);
            highlithers.add(rangeHighlighter);
        }

        Values.gutters = highlithers;
    }

    public void addGutterIcon(RangeHighlighter rangeHighlighter,
                              final PsiElement element, Icon icon, String description) {
        rangeHighlighter.setGutterIconRenderer(new GutterIconRenderer() {
            public @NotNull Icon getIcon() {
                return icon;
            }

            public String getTooltipText() {
                return description;
            }

            public boolean isNavigateAction() {
                return true;
            }

            @Override
            public boolean equals(Object obj) {
                return false;
            }

            @Override
            public int hashCode() {
                return 0;
            }

            public AnAction getClickAction() {
                ArrayList<Severity> clickedCandidates = new ArrayList<>();
                Utilities utilities = new Utilities();

                if (Values.isActive) {
                    if (editor != Values.editor) {
                        Values.editor = editor;
                    }

                    int line = editor.offsetToLogicalPosition(rangeHighlighter.getStartOffset()).line;

                    for (Severity severity : severities) {
                        if (severity.candidate instanceof ExtractMethodCandidate) {
                            ExtractMethodCandidate extract = (ExtractMethodCandidate) severity.candidate;
                            //for (PsiStatement node : extract.nodes) {
                                if(extract.range.start.line <= line && line <= extract.range.end.line){
                                //if (editor.offsetToLogicalPosition(node.getTextRange().getStartOffset()).line == line) {
                                    if (utilities.containsInArray(editor, severity, clickedCandidates))
                                        clickedCandidates.add(severity);
                                }
                            //}
                        } else if (severity.candidate instanceof ExtractVariableCandidate) {
                            ExtractVariableCandidate extract = (ExtractVariableCandidate) severity.candidate;
                            if (editor.offsetToLogicalPosition(extract.node.getTextRange().getStartOffset()).line == line) {
                                if (utilities.containsInArray(editor, severity, clickedCandidates))
                                    clickedCandidates.add(severity);
                            }
                        } else if (severity.candidate instanceof ExtractClassCandidate) {
                            ExtractClassCandidate extract = (ExtractClassCandidate) severity.candidate;
                            for (PsiElement targetEntity : extract.targetEntities) {
                                if (editor.offsetToLogicalPosition(targetEntity.getTextRange().getStartOffset()).line <= line &&
                                        editor.offsetToLogicalPosition(targetEntity.getTextRange().getEndOffset()).line >= line) {
                                    if (utilities.containsInArray(editor, severity, clickedCandidates))
                                        clickedCandidates.add(severity);
                                }
                            }
                        }
                        if (severity.candidate instanceof MoveMethodCandidate) {
                            MoveMethodCandidate move = (MoveMethodCandidate) severity.candidate;
                            //for (PsiStatement node : extract.nodes) {
                            if(move.range.start.line <= line && line <= move.range.end.line){
                                //if (editor.offsetToLogicalPosition(node.getTextRange().getStartOffset()).line == line) {
                                if (utilities.containsInArray(editor, severity, clickedCandidates))
                                    clickedCandidates.add(severity);
                            }
                            //}
                        }
                        else if (severity.candidate instanceof IntroduceParamObjCandidate) {
                            IntroduceParamObjCandidate paramObj = (IntroduceParamObjCandidate) severity.candidate;
                            if (editor.offsetToLogicalPosition(paramObj.method.getTextRange().getStartOffset()).line == line) {
                                if (utilities.containsInArray(editor, severity, clickedCandidates))
                                    clickedCandidates.add(severity);
                            }
                        }
                    }

                    Values.candidates = clickedCandidates;

                    RefactoringPanel panel = new RefactoringPanel(clickedCandidates, editor);
                    panel.show();

                    if (panel.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
                        Severity severity = (Severity) panel.selectedCandidate();
                        Object candidate = severity.candidate;
                        if (candidate instanceof ExtractMethodCandidate) {
                            ExtractMethod extractMethod = new ExtractMethod(editor);
                            extractMethod.extractMethod((ExtractMethodCandidate) candidate, severity.severity, severity.indexColorGutter);
                        } else if (candidate instanceof ExtractVariableCandidate) {
                            ExtractVariable extractVariable = new ExtractVariable(editor);
                            extractVariable.extractVariable((ExtractVariableCandidate) candidate, severity.severity, severity.indexColorGutter);
                        } else if (candidate instanceof ExtractClassCandidate) {
                            ExtractClass extractClass = new ExtractClass();
                            extractClass.extractClass((ExtractClassCandidate) candidate, severity.severity, severity.indexColorGutter);
                        } else if (candidate instanceof IntroduceParamObjCandidate) {
                            IntroduceParameterObject paramObj = new IntroduceParameterObject(editor);
                            paramObj.introduceParamObj((IntroduceParamObjCandidate) candidate, severity.severity, severity.indexColorGutter);
                        } else if (candidate instanceof MoveMethodCandidate) {
                            MoveMethod move = new MoveMethod(editor);
                            move.moveMethod((MoveMethodCandidate) candidate, severity.severity, severity.indexColorGutter);
                        }
                    }
                }

                return null;
            }
        });
    }

}
