package liverefactoring;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandEvent;
import com.intellij.openapi.command.CommandListener;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.BalloonBuilder;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.*;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.messages.MessageBus;
import liverefactoring.analysis.candidates.*;
import liverefactoring.analysis.metrics.FileMetrics;
import liverefactoring.analysis.refactorings.*;
import liverefactoring.core.Severity;
import liverefactoring.ui.ConfigureTool;
import liverefactoring.ui.MarkerProvider;
import liverefactoring.ui.RefactoringPanel;
import liverefactoring.ui.VisualRepresentation;
import liverefactoring.utils.UtilitiesOverall;
import liverefactoring.utils.importantValues.Values;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
