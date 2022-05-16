package com.ui;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiStatement;
import com.intellij.psi.util.PsiTreeUtil;
import com.refactorings.candidates.*;
import com.refactorings.candidates.utils.Severity;
import com.utils.Utilities;
import com.utils.Values;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;

public class VisualRepresentation {
    public ArrayList<Gutter> gutters;
    public Utilities utilities = new Utilities();

    public VisualRepresentation() {
        this.gutters = new ArrayList<>();
        for (RangeHighlighter rangeHighlighter : Values.gutters) {
            rangeHighlighter.setGutterIconRenderer(null);
        }
    }

    public void startVisualAnalysis(Editor editor, ArrayList<Severity> severities) throws IOException {
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
        for (Severity severity : severities) {
            if (severity.candidate instanceof ExtractMethodCandidate) {
                ExtractMethodCandidate extract = (ExtractMethodCandidate) severity.candidate;
                for (PsiStatement node : extract.nodes) {
                    int lineStart = editor.offsetToLogicalPosition(node.getTextRange().getStartOffset()).line;
                    int lineEnd = editor.offsetToLogicalPosition(node.getTextRange().getEndOffset()).line;
                    String description = "Extract Method Candidate - Extract this block of code to a new method and replace the old code with a call to the method";
                    ImageIcon icon = null;
                    if(Values.withColors) {
                        icon = utilities.getIcon(severity.indexColorGutter, "EM");

                        for(int line = lineStart; line <= lineEnd; line++) {
                            Gutter gutter = new Gutter(line, icon, severity.severity, description, node);
                            if (this.gutters.isEmpty()) {
                                this.gutters.add(gutter);
                            } else {
                                boolean found = false;
                                for (Gutter g : this.gutters) {
                                    if (g.line == gutter.line) {
                                        found = true;
                                        if (gutter.severity > g.severity) {
                                            this.gutters.set(this.gutters.indexOf(g), gutter);
                                        }
                                    }
                                }

                                if (!found)
                                    this.gutters.add(gutter);
                            }
                        }
                    }
                    else {
                        icon = new ImageIcon(Thread.currentThread().getContextClassLoader().getResource("images/colors/exclamation.png"));
                        Gutter gutter = new Gutter(lineStart, icon, severity.severity, description, node);

                        if (this.gutters.isEmpty()) {
                            this.gutters.add(gutter);
                        } else {
                            boolean found = false;
                            for (Gutter g : this.gutters) {
                                if (g.line == gutter.line) {
                                    found = true;
                                    if (gutter.severity > g.severity) {
                                        this.gutters.set(this.gutters.indexOf(g), gutter);
                                    }
                                }
                            }

                            if (!found)
                                this.gutters.add(gutter);
                        }
                    }
                }
            } else if (severity.candidate instanceof ExtractClassCandidate) {
                ExtractClassCandidate extract = (ExtractClassCandidate) severity.candidate;
                for (PsiElement targetEntity : extract.targetEntities) {
                    String description = "Extract Class Candidate - Create a new class and place the fields and methods responsible for the relevant functionality in it";
                    ImageIcon icon = null;

                    if(Values.withColors)
                        icon = utilities.getIcon(severity.indexColorGutter, "EC");
                    else {
                        icon = new ImageIcon(Thread.currentThread().getContextClassLoader().getResource("images/colors/exclamation.png"));
                    }

                    Gutter gutter;
                    if (targetEntity instanceof PsiMethod) {
                        for (PsiStatement element : PsiTreeUtil.findChildrenOfType(targetEntity, PsiStatement.class)) {
                            int line = editor.offsetToLogicalPosition(targetEntity.getTextRange().getStartOffset()).line;
                            gutter = new Gutter(line, icon, severity.severity, description, element);

                            if (this.gutters.isEmpty()) {
                                this.gutters.add(gutter);
                            } else {
                                boolean found = false;
                                for (Gutter g : this.gutters) {
                                    if (g.line == line) {
                                        found = true;
                                        if (severity.severity > g.severity) {
                                            this.gutters.set(this.gutters.indexOf(g), gutter);
                                        }
                                    }
                                }
                                if (!found)
                                    this.gutters.add(gutter);
                            }
                        }
                    } else if (targetEntity instanceof PsiField) {
                        int line = editor.offsetToLogicalPosition(targetEntity.getTextRange().getStartOffset()).line;
                        gutter = new Gutter(line, icon, severity.severity, description, targetEntity);

                        if (this.gutters.isEmpty()) {
                            this.gutters.add(gutter);
                        } else {
                            boolean found = false;
                            for (Gutter g : this.gutters) {
                                if (g.line == line) {
                                    found = true;
                                    if (severity.severity > g.severity) {
                                        this.gutters.set(this.gutters.indexOf(g), gutter);
                                    }
                                }
                            }

                            if (!found)
                                this.gutters.add(gutter);
                        }
                    }
                }
            } else if (severity.candidate instanceof ExtractVariableCandidate) {
                ExtractVariableCandidate extract = (ExtractVariableCandidate) severity.candidate;
                int line = extract.range.start.line;
                String description = "Extract Variable Candidate - Place the result of the expression or its parts in separate variables that are self-explanatory";
                ImageIcon icon = null;

                if(Values.withColors)
                    icon = utilities.getIcon(severity.indexColorGutter, "EV");
                else {
                    icon = new ImageIcon(Thread.currentThread().getContextClassLoader().getResource("images/colors/exclamation.png"));
                }
                
                Gutter gutter = new Gutter(line, icon, severity.severity, description, extract.node);

                if (this.gutters.isEmpty()) {
                    this.gutters.add(gutter);
                } else {
                    boolean found = false;
                    for (Gutter g : this.gutters) {
                        if (g.line == gutter.line) {
                            found = true;
                            if (gutter.severity > g.severity) {
                                this.gutters.set(this.gutters.indexOf(g), gutter);
                            }
                        }
                    }

                    if (!found)
                        this.gutters.add(gutter);
                }
            }
            else if (severity.candidate instanceof IntroduceParamObjCandidate) {
                IntroduceParamObjCandidate parameterObject = (IntroduceParamObjCandidate) severity.candidate;
                int line = editor.offsetToLogicalPosition(parameterObject.method.getTextRange().getStartOffset()).line;
                String description = "Introduce Parameter Object Candidate - Replace these parameters with an object";
                ImageIcon icon = null;

                if(Values.withColors)
                    icon = utilities.getIcon(severity.indexColorGutter, "IPO");
                else {
                    icon = new ImageIcon(Thread.currentThread().getContextClassLoader().getResource("images/colors/exclamation.png"));
                }

                Gutter gutter = new Gutter(line, icon, severity.severity, description, parameterObject.method.getParameterList());

                if (this.gutters.isEmpty()) {
                    this.gutters.add(gutter);
                } else {
                    boolean found = false;
                    for (Gutter g : this.gutters) {
                        if (g.line == gutter.line) {
                            found = true;
                            if (gutter.severity > g.severity) {
                                this.gutters.set(this.gutters.indexOf(g), gutter);
                            }
                        }
                    }

                    if (!found)
                        this.gutters.add(gutter);
                }
            }
            if (severity.candidate instanceof MoveMethodCandidate) {
                MoveMethodCandidate move = (MoveMethodCandidate) severity.candidate;
                for (PsiStatement node : move.method.getBody().getStatements()) {
                    int lineStart = editor.offsetToLogicalPosition(node.getTextRange().getStartOffset()).line;
                    int lineEnd = editor.offsetToLogicalPosition(node.getTextRange().getEndOffset()).line;
                    String description = "Move Method Candidate - Create a new method in the class that uses the method the most, then move code from the old method to there. Turn the code of the original method into a reference to the new method in the other class or else remove it entirely";
                    ImageIcon icon = null;
                    if(Values.withColors) {
                        icon = utilities.getIcon(severity.indexColorGutter, "MM");

                        for(int line = lineStart; line <= lineEnd; line++) {
                            Gutter gutter = new Gutter(line, icon, severity.severity, description, node);

                            if (this.gutters.isEmpty()) {
                                this.gutters.add(gutter);
                            } else {
                                boolean found = false;
                                for (Gutter g : this.gutters) {
                                    if (g.line == gutter.line) {
                                        found = true;
                                        if (gutter.severity > g.severity) {
                                            this.gutters.set(this.gutters.indexOf(g), gutter);
                                        }
                                    }
                                }

                                if (!found)
                                    this.gutters.add(gutter);
                            }
                        }
                    }
                    else {
                        icon = new ImageIcon(Thread.currentThread().getContextClassLoader().getResource("images/colors/exclamation.png"));

                        Gutter gutter = new Gutter(lineStart, icon, severity.severity, description, node);
                        if (this.gutters.isEmpty()) {
                            this.gutters.add(gutter);
                        } else {
                            boolean found = false;
                            for (Gutter g : this.gutters) {
                                if (g.line == gutter.line) {
                                    found = true;
                                    if (gutter.severity > g.severity) {
                                        this.gutters.set(this.gutters.indexOf(g), gutter);
                                    }
                                }
                            }

                            if (!found)
                                this.gutters.add(gutter);
                        }
                    }
                }
            }
        }

        ColoringGutters coloring = new ColoringGutters(this.gutters, editor, severities);
        coloring.initGutters();
    }
}
