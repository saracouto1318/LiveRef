package com.ui;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiTreeChangeEvent;
import com.intellij.psi.PsiTreeChangeListener;
import com.metrics.FileMetrics;
import com.utils.Values;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class WindowFactory implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        MetricsWindow metricsWindow = ServiceManager.getService(project, MetricsWindowService.class).window;
        JComponent component = toolWindow.getComponent();
        component.getParent().add(metricsWindow.content);
        PsiManager.getInstance(project).addPsiTreeChangeListener(new PsiTreeChangeListener() {
            @Override
            public void beforeChildAddition(@NotNull PsiTreeChangeEvent event) {
            }

            @Override
            public void beforeChildRemoval(@NotNull PsiTreeChangeEvent event) {

            }

            @Override
            public void beforeChildReplacement(@NotNull PsiTreeChangeEvent event) {

            }

            @Override
            public void beforeChildMovement(@NotNull PsiTreeChangeEvent event) {
            }

            @Override
            public void beforeChildrenChange(@NotNull PsiTreeChangeEvent event) {
            }

            @Override
            public void beforePropertyChange(@NotNull PsiTreeChangeEvent event) {

            }

            @Override
            public void childAdded(@NotNull PsiTreeChangeEvent event) {

            }

            @Override
            public void childRemoved(@NotNull PsiTreeChangeEvent event) {

            }

            @Override
            public void childReplaced(@NotNull PsiTreeChangeEvent event) {

            }

            @Override
            public void childrenChanged(@NotNull PsiTreeChangeEvent event) {
                ArrayList<Double> loc = new ArrayList<>();
                ArrayList<Double> cc = new ArrayList<>();
                ArrayList<Double> cog = new ArrayList<>();
                ArrayList<Double> volume = new ArrayList<>();
                ArrayList<Double> effort = new ArrayList<>();
                ArrayList<Double> difficulty = new ArrayList<>();
                ArrayList<Double> maintainability = new ArrayList<>();

                if(Values.editor != null){
                    try {
                        TimeUnit.SECONDS.sleep(5);
                        if(Values.metricsFile.containsKey(event.getFile().getName())){
                            ArrayList<ArrayList<Double>> metrics = Values.metricsFile.get(event.getFile().getName());
                            loc = metrics.get(0);
                            cc = metrics.get(1);
                            cog = metrics.get(2);
                            volume = metrics.get(3);
                            effort = metrics.get(4);
                            difficulty = metrics.get(5);
                            maintainability = metrics.get(6);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    Editor editor = ((TextEditor) Objects.requireNonNull(FileEditorManager.getInstance(event.getFile().getProject()).getSelectedEditor())).getEditor();
                    PsiJavaFile file = (PsiJavaFile)event.getFile();
                    FileMetrics metrics = new FileMetrics(editor, file);
                    loc.add(metrics.lines);
                    cc.add(metrics.complexity);
                    cog.add(metrics.cognitiveComplexity);
                    volume.add(metrics.halsteadVolume);
                    effort.add(metrics.halsteadEffort);
                    difficulty.add(metrics.halsteadDifficulty);
                    maintainability.add(metrics.halsteadMaintainability);
                }

                String pathName = Values.editor.getProject().getBasePath()+"/index.html";
                System.out.println(pathName);
                File f = new File(pathName);
                try {
                    f.createNewFile();
                    PrintWriter writer = null;
                    writer = new PrintWriter(f);
                    writer.print("");
                    writer.close();
                    writer = new PrintWriter(f);
                    writer.println("<html>\n");

                    if(loc.size() == 1){
                        writer.println("<body>\n" +
                                "<h1>"+((PsiJavaFile)event.getFile()).getName()+"</h1>\n" +
                                "<h3>Current Code Quality Metrics</h3>\n" +
                                "<ul>\n" +
                                "    <li>Lines of Code: </li>\n" +
                                "    <li>Cyclomatic Complexity</li>\n" +
                                "    <li>Cognitive Complexity</li>\n" +
                                "    <li>Halstead Volume</li>\n" +
                                "    <li>Halstead Effort</li>\n" +
                                "    <li>Halstead Difficulty</li>\n" +
                                "    <li>Halstead Maintainability</li>\n" +
                                "</ul>\n" +
                                "</body>");
                    }else{
                        String write = "<head><script type=\"text/javascript\"><script type=\"text/javascript\">\n" +
                                "  window.onload = function () {\n" +
                                "    var chart = new CanvasJS.Chart(\"chartContainer\",\n" +
                                "    {\n" +
                                "\n" +
                                "      title:{\n" +
                                "      text: \"Lines of Code\"\n" +
                                "      },\n" +
                                "       data: [\n" +
                                "      {\n" +
                                "        type: \"line\",\n" +
                                "\n" +
                                "        dataPoints: [\n";

                        int x = 0;
                        for (Double aDouble : loc) {
                            x++;
                            write += "        { x: " + x + ", y: " + aDouble + " }";
                            if(loc.indexOf(aDouble) != loc.size() - 1) {
                                write += ",\n";
                            }
                        }
                        write += "        ]\n" +
                                "      }\n" +
                                "      ]\n" +
                                "    });\n" +
                                "\n" +
                                "    chart.render();\n" +
                                "  }";

                        write += "    var chart2 = new CanvasJS.Chart(\"chartContainer2\",\n" +
                                "    {\n" +
                                "\n" +
                                "      title:{\n" +
                                "      text: \"Cyclomatic Complexity\"\n" +
                                "      },\n" +
                                "       data: [\n" +
                                "      {\n" +
                                "        type: \"line\",\n" +
                                "\n" +
                                "        dataPoints: [\n";

                        x = 0;
                        for (Double aDouble : cc) {
                            x++;
                            write += "        { x: " + x + ", y: " + aDouble + " }";
                            if(cc.indexOf(aDouble) != cc.size() - 1) {
                                write += ",\n";
                            }
                        }
                        write += "        ]\n" +
                                "      }\n" +
                                "      ]\n" +
                                "    });\n" +
                                "\n" +
                                "    chart2.render();\n" +
                                "  }";

                        write += "    var chart3 = new CanvasJS.Chart(\"chartContainer3\",\n" +
                                "    {\n" +
                                "\n" +
                                "      title:{\n" +
                                "      text: \"Cognitive Complexity\"\n" +
                                "      },\n" +
                                "       data: [\n" +
                                "      {\n" +
                                "        type: \"line\",\n" +
                                "\n" +
                                "        dataPoints: [\n";

                        x = 0;
                        for (Double aDouble : cog) {
                            x++;
                            write += "        { x: " + x + ", y: " + aDouble + " }";
                            if(cog.indexOf(aDouble) != cog.size() - 1) {
                                write += ",\n";
                            }
                        }
                        write += "        ]\n" +
                                "      }\n" +
                                "      ]\n" +
                                "    });\n" +
                                "\n" +
                                "    chart3.render();\n" +
                                "  }";

                        write += "    var chart4 = new CanvasJS.Chart(\"chartContainer4\",\n" +
                                "    {\n" +
                                "\n" +
                                "      title:{\n" +
                                "      text: \"Halstead Volume\"\n" +
                                "      },\n" +
                                "       data: [\n" +
                                "      {\n" +
                                "        type: \"line\",\n" +
                                "\n" +
                                "        dataPoints: [\n";

                        x = 0;
                        for (Double aDouble : volume) {
                            x++;
                            write += "        { x: " + x + ", y: " + aDouble + " }";
                            if(volume.indexOf(aDouble) != volume.size() - 1) {
                                write += ",\n";
                            }
                        }
                        write += "        ]\n" +
                                "      }\n" +
                                "      ]\n" +
                                "    });\n" +
                                "\n" +
                                "    chart4.render();\n" +
                                "  }";

                        write += "    var chart5 = new CanvasJS.Chart(\"chartContainer5\",\n" +
                                "    {\n" +
                                "\n" +
                                "      title:{\n" +
                                "      text: \"Halstead Effort\"\n" +
                                "      },\n" +
                                "       data: [\n" +
                                "      {\n" +
                                "        type: \"line\",\n" +
                                "\n" +
                                "        dataPoints: [\n";

                        x = 0;
                        for (Double aDouble : effort) {
                            x++;
                            write += "        { x: " + x + ", y: " + aDouble + " }";
                            if(effort.indexOf(aDouble) != effort.size() - 1) {
                                write += ",\n";
                            }
                        }
                        write += "        ]\n" +
                                "      }\n" +
                                "      ]\n" +
                                "    });\n" +
                                "\n" +
                                "    chart5.render();\n" +
                                "  }";

                        write += "    var chart6 = new CanvasJS.Chart(\"chartContainer6\",\n" +
                                "    {\n" +
                                "\n" +
                                "      title:{\n" +
                                "      text: \"Halstead Difficulty\"\n" +
                                "      },\n" +
                                "       data: [\n" +
                                "      {\n" +
                                "        type: \"line\",\n" +
                                "\n" +
                                "        dataPoints: [\n";

                        x = 0;
                        for (Double aDouble : difficulty) {
                            x++;
                            write += "        { x: " + x + ", y: " + aDouble + " }";
                            if(difficulty.indexOf(aDouble) != difficulty.size() - 1) {
                                write += ",\n";
                            }
                        }
                        write += "        ]\n" +
                                "      }\n" +
                                "      ]\n" +
                                "    });\n" +
                                "\n" +
                                "    chart6.render();\n" +
                                "  }";

                        write += "    var chart7 = new CanvasJS.Chart(\"chartContainer7\",\n" +
                                "    {\n" +
                                "\n" +
                                "      title:{\n" +
                                "      text: \"Halstead Maintainability\"\n" +
                                "      },\n" +
                                "       data: [\n" +
                                "      {\n" +
                                "        type: \"line\",\n" +
                                "\n" +
                                "        dataPoints: [\n";

                        x = 0;
                        for (Double aDouble : maintainability) {
                            x++;
                            write += "        { x: " + x + ", y: " + aDouble + " }";
                            if(maintainability.indexOf(aDouble) != maintainability.size() - 1) {
                                write += ",\n";
                            }
                        }
                        write += "        ]\n" +
                                "      }\n" +
                                "      ]\n" +
                                "    });\n" +
                                "\n" +
                                "    chart7.render();\n" +
                                "  }";

                        write += "}\n" +
                                "  </script>\n" +
                                "    <script type=\"text/javascript\" src=\"https://canvasjs.com/assets/script/canvasjs.min.js\"></script></head>\n" +
                                "<body>\n" +
                                "<h1>"+((PsiJavaFile)event.getFile()).getName()+"</h1>\n" +
                                "<h3>Code Quality Metrics</h3>\n" +
                                "<div id=\"chartContainer\" style=\"height: 300px; width: 100%;\">"+
                                "<div id=\"chartContainer2\" style=\"height: 300px; width: 100%;\">"+
                                "<div id=\"chartContainer3\" style=\"height: 300px; width: 100%;\">"+
                                "<div id=\"chartContainer4\" style=\"height: 300px; width: 100%;\">"+
                                "<div id=\"chartContainer5\" style=\"height: 300px; width: 100%;\">"+
                                "<div id=\"chartContainer6\" style=\"height: 300px; width: 100%;\">"+
                                "<div id=\"chartContainer7\" style=\"height: 300px; width: 100%;\">"+
                                "</div>\n" +
                                        "</body>\n" +
                                        "</html>";

                        writer.println(write);
                        writer.close();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

                MetricsWindow newMetricsWindow = ServiceManager.getService(Values.editor.getProject(), MetricsWindowService.class).window;
                component.getParent().remove(0);
                System.out.println(component.getParent().getComponentCount());
                component.getParent().add(newMetricsWindow.content);
                System.out.println(component.getParent().getComponentCount());
            }

            @Override
            public void childMoved(@NotNull PsiTreeChangeEvent event) {

            }

            @Override
            public void propertyChanged(@NotNull PsiTreeChangeEvent event) {

            }
        });
    }
}
