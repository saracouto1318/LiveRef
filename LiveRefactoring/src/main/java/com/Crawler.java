package com;

import com.analysis.metrics.ClassMetrics;
import com.analysis.metrics.MethodMetrics;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.utils.UtilitiesOverall;
import com.utils.importantValues.Values;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class Crawler extends AnAction {
    Project project = null;
    String URL = "/Users/sarafernandes/Desktop/Crawler/Projects";
    ClassMetrics oldClass = null;
    MethodMetrics newMethod = null;
    boolean refactoring = false;
    int numRefactorings = 0;

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        File[] files = (new File(URL)).listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    ApplicationManager.getApplication().invokeLater(() -> {
                        try {
                            ApplicationManager.getApplication().getMessageBus().connect().subscribe(ProjectManager.TOPIC, new ProjectManagerListener() {
                                @Override
                                public void projectOpened(Project proj) {
                                    project = proj;
                                    System.out.println(proj.getName());
                                    analyzeProject(project);
                                }
                            });
                            project = ProjectManager.getInstance().loadAndOpenProject(file.getPath());
                            analyzeProject(project);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        } catch (JDOMException ex) {
                            ex.printStackTrace();
                        }
                    });
                    break;
                }
            }
        }
    }

    public void analyzeProject(Project project) {
        PsiDirectory rootDirectory = PsiManager.getInstance(project).findDirectory(project.getBaseDir());
        if (rootDirectory != null) processDirectory(rootDirectory);
    }

    private void processDirectory(PsiDirectory rootDirectory) {
        for (PsiFile file : rootDirectory.getFiles()) processFile(file);

        for (PsiDirectory subdirectory : rootDirectory.getSubdirectories()) processDirectory(subdirectory);
    }

    private void processFile(PsiFile file) {
        if(file instanceof PsiJavaFile) {
            while(true) {
                VirtualFile virtualFile = file.getVirtualFile();
                if (virtualFile != null) {
                    Values.refactoringCounter = 0;


                    try {
                        FileEditorManager.getInstance(project).openFile(virtualFile, true);
                        ApplicationManager.getApplication().invokeLater(() -> {
                            Values.editor = ((TextEditor) FileEditorManager.getInstance(project).getSelectedEditor()).getEditor();
                            System.out.println(Values.editor);
                            UtilitiesOverall utils = new UtilitiesOverall();
                            utils.startActions((PsiJavaFile) file);
                        });

                    } catch (IndexNotReadyException e) {
                        System.err.println(e);
                    }
                }
            }
        }
    }
}
