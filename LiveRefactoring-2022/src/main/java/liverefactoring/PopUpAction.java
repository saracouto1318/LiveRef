package liverefactoring;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandEvent;
import com.intellij.openapi.command.CommandListener;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.BalloonBuilder;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.pom.Navigatable;
import com.intellij.psi.*;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.messages.MessageBus;
import liverefactoring.analysis.metrics.FileMetrics;
import liverefactoring.ui.JDeodorant.ClassWrapper;
import liverefactoring.ui.PopUpDialog;
import liverefactoring.ui.VisualRepresentation;
import liverefactoring.utils.UtilitiesOverall;
import liverefactoring.utils.importantValues.Values;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PopUpAction implements StartupActivity {

    @Override
    public void runActivity(@NotNull Project project) {
        if (FileEditorManager.getInstance(project).getSelectedEditor() != null){
            Editor editor = ((TextEditor) Objects.requireNonNull(FileEditorManager.getInstance(project).getSelectedEditor())).getEditor();
            if(editor != null){
                PopUpDialog wrapper = new PopUpDialog();
                wrapper.show();

                if (wrapper.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
                    Values.activationStatus = true;
                    Values.enable = true;
                    UtilitiesOverall utils = new UtilitiesOverall();
                    utils.activateTool(project);
                }
                else Values.activationStatus = false;
            }
        }
        else Values.activationStatus = false;

    }
}
