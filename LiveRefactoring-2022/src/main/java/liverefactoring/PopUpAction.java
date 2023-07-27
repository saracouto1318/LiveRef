package liverefactoring;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.ui.DialogWrapper;
import liverefactoring.ui.PopUpDialog;
import liverefactoring.utils.UtilitiesOverall;
import liverefactoring.utils.importantValues.Values;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

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
