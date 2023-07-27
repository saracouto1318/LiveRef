package liverefactoring;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import liverefactoring.ui.ConfigureTool;
import liverefactoring.utils.UtilitiesOverall;
import liverefactoring.utils.importantValues.Values;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class DynamicActionGroup extends ActionGroup {
    private boolean enable = true;
    @Override
    public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
        Project project = e.getProject();
        if (FileEditorManager.getInstance(project).getSelectedEditor() != null)
            Values.editor = ((TextEditor) Objects.requireNonNull(FileEditorManager.getInstance(project).getSelectedEditor())).getEditor();

        if(!Values.activationStatus)
            enable = false;

        if(enable) return new AnAction[]{
                    new AnAction("Stop Analysis") {
                        @Override
                        public void actionPerformed(@NotNull AnActionEvent e) {
                            enable = false;
                            Values.enable = false;
                            for (RangeHighlighter rangeHighlighter : Values.gutters) {
                                rangeHighlighter.setGutterIconRenderer(null);
                            }
                            Values.isActive = false;
                        }
                    },
                    new AnAction("Configure Tool") {

                        @Override
                        public void actionPerformed(@NotNull AnActionEvent e) {
                            ConfigureTool configMenu = new ConfigureTool();
                            configMenu.startMenu();
                        }
                    }
            };
        else return new AnAction[]{
                new AnAction("Start Analysis") {
                    @Override
                    public void actionPerformed(@NotNull AnActionEvent e) {
                        enable = true;
                        Values.enable = true;
                        final UtilitiesOverall utils = new UtilitiesOverall();
                        if(!Values.activationStatus) {
                            Values.activationStatus = true;
                            utils.activateTool(project);
                        }
                        else {
                            PsiDocumentManager documentManager = PsiDocumentManager.getInstance(Objects.requireNonNull(e.getProject()));
                            Values.isActive = true;
                            PsiFile psiFile = documentManager.getCachedPsiFile(Values.editor.getDocument());
                            if (psiFile instanceof PsiJavaFile) {
                                PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
                                Values.isFirst = true;
                                utils.startActions(psiJavaFile);
                            }
                        }
                    }
                },
                new AnAction("Configure Tool") {
                    @Override
                    public void actionPerformed(@NotNull AnActionEvent e) {
                        ConfigureTool configMenu = new ConfigureTool();
                        configMenu.startMenu();
                    }
                }
        };
    }
}
