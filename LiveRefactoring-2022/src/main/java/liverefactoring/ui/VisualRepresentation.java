package liverefactoring.ui;

import liverefactoring.analysis.candidates.*;
import liverefactoring.analysis.candidates.*;
import liverefactoring.core.Gutter;
import liverefactoring.core.Severity;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiStatement;
import com.intellij.psi.util.PsiTreeUtil;
import liverefactoring.utils.UtilitiesOverall;
import liverefactoring.utils.importantValues.Values;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;

public class VisualRepresentation {
    public ArrayList<Gutter> gutters;
    public UtilitiesOverall utils = new UtilitiesOverall();

    public VisualRepresentation() {
        this.gutters = new ArrayList<>();
        for (RangeHighlighter rangeHighlighter : Values.gutters) {
            rangeHighlighter.setGutterIconRenderer(null);
        }
    }

    public void startVisualAnalysis(Editor editor, ArrayList<Severity> severities) throws IOException {

        //Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
        for (Severity severity : severities) {
            if (severity.candidate instanceof ExtractMethodCandidate) {
                ExtractMethodCandidate extract = (ExtractMethodCandidate) severity.candidate;
                for (PsiStatement node : extract.nodes) {
                    int lineStart = editor.offsetToLogicalPosition(node.getTextRange().getStartOffset()).line;
                    int lineEnd = editor.offsetToLogicalPosition(node.getTextRange().getEndOffset()).line;
                    String description = "Extract Method Candidate - It extracts this block of code to a new method and replaces the old code with a call to the method";
                    Icon icon = utils.getIcon(severity.indexColorGutter);

                    for (int line = lineStart; line <= lineEnd; line++) {
                        Gutter gutter = new Gutter(line, icon, severity.severity, description, node);
                        if (this.gutters.isEmpty()) {
                            this.gutters.add(gutter);
                        } else {
                            boolean found = false;
                            for (Gutter g : this.gutters) {
                                if (g.line == gutter.line) {
                                    found = true;
                                    if(!g.description.contains("Extract Method") && !g.description.contains("Extract Variable") && !g.description.contains("String Comparison")) {
                                        if (gutter.severity > g.severity) {
                                            this.gutters.set(this.gutters.indexOf(g), gutter);
                                        }
                                    }
                                    else{
                                        if (gutter.severity < g.severity) {
                                            this.gutters.set(this.gutters.indexOf(g), gutter);
                                        }
                                    }
                                }
                            }

                            if (!found)
                                this.gutters.add(gutter);
                        }
                    }
                }
            }
            else if (severity.candidate instanceof ExtractClassCandidate) {
                ExtractClassCandidate extract = (ExtractClassCandidate) severity.candidate;
                for (PsiElement targetEntity : extract.targetEntities) {
                    String description = "Extract Class Candidate - It creates a new class and places the fields and methods responsible for the relevant functionality in it";
                    Icon icon = utils.getIcon(severity.indexColorGutter);
                    Gutter gutter;
                    if (targetEntity instanceof PsiMethod) {
                        for (PsiStatement element : PsiTreeUtil.findChildrenOfType((PsiMethod)targetEntity, PsiStatement.class)) {
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
            }
            else if (severity.candidate instanceof ExtractVariableCandidate) {
                ExtractVariableCandidate extract = (ExtractVariableCandidate) severity.candidate;
                int line = extract.range.start.line;
                String description = "Extract Variable Candidate - It places the result of the expression or its parts in separate variables that are self-explanatory";
                Icon icon = utils.getIcon(severity.indexColorGutter);
                
                Gutter gutter = new Gutter(line, icon, severity.severity, description, extract.node);

                if (this.gutters.isEmpty()) {
                    this.gutters.add(gutter);
                } else {
                    boolean found = false;
                    for (Gutter g : this.gutters) {
                        if (g.line == gutter.line) {
                            found = true;
                            if(g.description.equals(gutter.description) || !g.description.contains("Extract Method")) {
                                if (gutter.severity > g.severity) {
                                    this.gutters.set(this.gutters.indexOf(g), gutter);
                                }
                            }
                            else{
                                if (gutter.severity < g.severity) {
                                    this.gutters.set(this.gutters.indexOf(g), gutter);
                                }
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
                String description = "Introduce Parameter Object Candidate - It replaces these parameters with an object";
                Icon icon = utils.getIcon(severity.indexColorGutter);

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
            else if (severity.candidate instanceof MoveMethodCandidate) {
                MoveMethodCandidate move = (MoveMethodCandidate) severity.candidate;
                PsiElement node = PsiTreeUtil.findChildrenOfType(move.method, PsiElement.class).iterator().next();
                int line = editor.offsetToLogicalPosition(node.getTextRange().getStartOffset()).line;
                String description = "Move Method Candidate - It creates a new method in the class that uses the method the most, then moves code from the old method to there. It turns the code of the original method into a reference to the new method in the other class or else it removes it entirely";
                Icon icon = utils.getIcon(severity.indexColorGutter);

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
            else if (severity.candidate instanceof StringComparisonCandidate) {
                StringComparisonCandidate extract = (StringComparisonCandidate) severity.candidate;
                int line = extract.range.start.line;
                String description = "String Comparison Candidate - Replace the binary expression with '==' operator and string operands with 'equals()'";
                Icon icon = utils.getIcon(severity.indexColorGutter);

                Gutter gutter = new Gutter(line, icon, severity.severity, description, extract.node);

                if (this.gutters.isEmpty()) {
                    this.gutters.add(gutter);
                } else {
                    boolean found = false;
                    for (Gutter g : this.gutters) {
                        if (g.line == gutter.line) {
                            found = true;
                            if(g.description.equals(gutter.description) || !g.description.contains("Extract Method")) {
                                if (gutter.severity > g.severity) {
                                    this.gutters.set(this.gutters.indexOf(g), gutter);
                                }
                            }
                            else{
                                if (gutter.severity < g.severity) {
                                    this.gutters.set(this.gutters.indexOf(g), gutter);
                                }
                            }
                        }
                    }

                    if (!found)
                        this.gutters.add(gutter);
                }
            }
            else if (severity.candidate instanceof InheritanceToDelegationCandidate) {
                InheritanceToDelegationCandidate extract = (InheritanceToDelegationCandidate) severity.candidate;
                PsiElement targetEntity = (PsiElement) PsiTreeUtil.findChildrenOfType(extract._class, PsiElement.class).toArray()[0];
                String description = "Inheritance To Delegation - It creates a field and puts a superclass object in it, delegates methods to the superclass object, and gets rid of the inheritance.";
                Icon icon = utils.getIcon(severity.indexColorGutter);
                Gutter gutter;
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

        for (Gutter gutter : this.gutters) {
            System.out.print("\n\nGutter " + gutter.line + " " + gutter.severity + " " + gutter.icon.toString()+"\n");
        }
        ColoringGutters coloring = new ColoringGutters(this.gutters, editor, severities);
        coloring.initGutters();
    }
}
