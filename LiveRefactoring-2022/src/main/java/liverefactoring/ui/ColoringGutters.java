package liverefactoring.ui;

import liverefactoring.analysis.candidates.*;
import liverefactoring.analysis.refactorings.*;
import liverefactoring.analysis.candidates.*;
import liverefactoring.analysis.refactorings.*;
import liverefactoring.core.Gutter;
import liverefactoring.core.Severity;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.PsiElement;
import liverefactoring.utils.UtilitiesOverall;
import liverefactoring.utils.importantValues.Values;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
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
        ArrayList<RangeHighlighter> highlighters = new ArrayList<>();
        for (Gutter gutter : this.gutters) {
            int lineEnd = this.editor.offsetToLogicalPosition(Values.currentFile.javaFile.getTextRange().getEndOffset()).line;
            if(gutter.line <= lineEnd) {
                MarkupModel markupModel = this.editor.getMarkupModel();
                RangeHighlighter rangeHighlighter = markupModel.addLineHighlighter(gutter.line, HighlighterLayer.FIRST, null);
                this.addGutterIcon(rangeHighlighter, gutter.icon, gutter.description);
                highlighters.add(rangeHighlighter);
            }
        }

        Values.gutters = highlighters;
    }

    public void addGutterIcon(RangeHighlighter rangeHighlighter, Icon icon, String description) {
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
                UtilitiesOverall utilitiesOverall = new UtilitiesOverall();

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
                                    if (utilitiesOverall.containsInArray(editor, severity, clickedCandidates))
                                        clickedCandidates.add(severity);
                                }
                            //}
                        }
                        else if (severity.candidate instanceof ExtractVariableCandidate) {
                            ExtractVariableCandidate extract = (ExtractVariableCandidate) severity.candidate;
                            if (editor.offsetToLogicalPosition(extract.node.getTextRange().getStartOffset()).line == line) {
                                if (utilitiesOverall.containsInArray(editor, severity, clickedCandidates))
                                    clickedCandidates.add(severity);
                            }
                        }
                        else if (severity.candidate instanceof ExtractClassCandidate) {
                            ExtractClassCandidate extract = (ExtractClassCandidate) severity.candidate;
                            for (PsiElement targetEntity : extract.targetEntities) {
                                if (editor.offsetToLogicalPosition(targetEntity.getTextRange().getStartOffset()).line == line) {
                                    if (utilitiesOverall.containsInArray(editor, severity, clickedCandidates))
                                        clickedCandidates.add(severity);
                                }
                            }
                        }
                        if (severity.candidate instanceof MoveMethodCandidate) {
                            MoveMethodCandidate move = (MoveMethodCandidate) severity.candidate;
                            //for (PsiStatement node : extract.nodes) {
                            if(move.range.start.line == line){
                                //if (editor.offsetToLogicalPosition(node.getTextRange().getStartOffset()).line == line) {
                                if (utilitiesOverall.containsInArray(editor, severity, clickedCandidates))
                                    clickedCandidates.add(severity);
                            }
                            //}
                        }
                        else if (severity.candidate instanceof IntroduceParamObjCandidate) {
                            IntroduceParamObjCandidate paramObj = (IntroduceParamObjCandidate) severity.candidate;
                            if (editor.offsetToLogicalPosition(paramObj.method.getTextRange().getStartOffset()).line == line) {
                                if (utilitiesOverall.containsInArray(editor, severity, clickedCandidates))
                                    clickedCandidates.add(severity);
                            }
                        }
                        else if (severity.candidate instanceof StringComparisonCandidate) {
                            StringComparisonCandidate comparison = (StringComparisonCandidate) severity.candidate;
                            if (editor.offsetToLogicalPosition(comparison.node.getTextRange().getStartOffset()).line == line) {
                                if (utilitiesOverall.containsInArray(editor, severity, clickedCandidates))
                                    clickedCandidates.add(severity);
                            }
                        }
                        else if (severity.candidate instanceof InheritanceToDelegationCandidate) {
                            InheritanceToDelegationCandidate refused = (InheritanceToDelegationCandidate) severity.candidate;
                            int lineStart = editor.offsetToLogicalPosition(refused._class.getTextRange().getStartOffset()).line;
                            if(lineStart == line){
                                if (utilitiesOverall.containsInArray(editor, severity, clickedCandidates))
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
                        } else if (candidate instanceof StringComparisonCandidate) {
                            StringComparison comparison = new StringComparison(editor);
                            comparison.stringComparison((StringComparisonCandidate) candidate, severity.severity, severity.indexColorGutter);
                        } else if (candidate instanceof InheritanceToDelegationCandidate) {
                            InheritanceToDelegation inheritance = new InheritanceToDelegation(editor);
                            inheritance.inheritanceToDelegation((InheritanceToDelegationCandidate) candidate, severity.severity, severity.indexColorGutter);
                        }
                    }
                }

                return null;
            }
        });
    }

}
