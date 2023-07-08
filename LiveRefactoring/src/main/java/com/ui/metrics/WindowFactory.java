package com.ui.metrics;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.utils.importantValues.Values;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WindowFactory implements ToolWindowFactory {
    ArrayList<Object> loc = new ArrayList<>();
    ArrayList<Object> cc = new ArrayList<>();
    ArrayList<Object> cog = new ArrayList<>();
    ArrayList<Object> lcom = new ArrayList<>();
    ArrayList<Object> volume = new ArrayList<>();
    ArrayList<Object> effort = new ArrayList<>();
    ArrayList<Object> difficulty = new ArrayList<>();
    ArrayList<Object> maintainability = new ArrayList<>();

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        MetricsWindow metricsWindow = ServiceManager.getService(project, MetricsWindowService.class).window;
        JComponent component = toolWindow.getComponent();
        component.getParent().add(metricsWindow.content);

        Runnable metrics = new Runnable() {
            public void run() {
                if(Values.isActive){
                    System.out.println("ativou ativou");
                    if(Values.editor != null) {
                        System.out.println("ativou ativou ativou ativou");
                        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
                        URL serviceAccount =Thread.currentThread().getContextClassLoader().getResource("webview/index.html");
                        String pathName = serviceAccount.getPath();
                        System.out.println("Java file2:" + Values.before.fileName);

                        loc = Values.metricsFile.get(Values.before.fileName).get(0);
                        cc = Values.metricsFile.get(Values.before.fileName).get(1);
                        cog = Values.metricsFile.get(Values.before.fileName).get(2);
                        lcom = Values.metricsFile.get(Values.before.fileName).get(3);
                        volume = Values.metricsFile.get(Values.before.fileName).get(4);
                        effort = Values.metricsFile.get(Values.before.fileName).get(5);
                        difficulty = Values.metricsFile.get(Values.before.fileName).get(6);
                        maintainability = Values.metricsFile.get(Values.before.fileName).get(7);
                        System.out.println("Size size:"+loc.size());
                        System.out.println(pathName);
                        File f = new File(pathName);

                        try {
                            f.createNewFile();
                            PrintWriter writer = new PrintWriter(f);
                            writer.println("<html>\n");

                            if (loc.size() == 1) {
                                System.out.println("aqui");
                                String write = "<body>\n" +
                                        "<h1>" + Values.before.fileName + "</h1>\n" +
                                        "<h3>Current Code Quality Metrics</h3>\n" +
                                        "<ul>\n" +
                                        "    <li>Lines of Code: " + loc.get(0) +"</li>\n" +
                                        "    <li>Cyclomatic Complexity: " + cc.get(0) +"</li>\n"+
                                        "    <li>Cognitive Complexity: " + cog.get(0) +"</li>\n" +
                                        "    <li>Lack of Cohesion: " + lcom.get(0) +"</li>\n" +
                                        "    <li>Halstead Volume: " + volume.get(0) +"</li>\n" +
                                        "    <li>Halstead Effort: " + effort.get(0) +"</li>\n" +
                                        "    <li>Halstead Difficulty: " + difficulty.get(0) +"</li>\n" +
                                        "    <li>Halstead Maintainability: " + maintainability.get(0) +"</li>\n" +
                                        "</ul>\n" +
                                        "</body>\n" +
                                        "</html>";
                                System.out.println(write);
                                writer.println(write);
                                writer.close();

                                File f2 = new File(pathName);
                                Scanner myReader = new Scanner(f2);
                                while (myReader.hasNextLine()) {
                                    String data = myReader.nextLine();
                                    System.out.println(data);
                                }
                                myReader.close();
                            } else {
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
                                for (Object aDouble : loc) {
                                    x++;
                                    write += "        { x: " + x + ", y: " + aDouble + " }";
                                    if (loc.indexOf(aDouble) != loc.size() - 1) {
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
                                for (Object aDouble : cc) {
                                    x++;
                                    write += "        { x: " + x + ", y: " + aDouble + " }";
                                    if (cc.indexOf(aDouble) != cc.size() - 1) {
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
                                for (Object aDouble : cog) {
                                    x++;
                                    write += "        { x: " + x + ", y: " + aDouble + " }";
                                    if (cog.indexOf(aDouble) != cog.size() - 1) {
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
                                for (Object aDouble : volume) {
                                    x++;
                                    write += "        { x: " + x + ", y: " + aDouble + " }";
                                    if (volume.indexOf(aDouble) != volume.size() - 1) {
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
                                for (Object aDouble : effort) {
                                    x++;
                                    write += "        { x: " + x + ", y: " + aDouble + " }";
                                    if (effort.indexOf(aDouble) != effort.size() - 1) {
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
                                for (Object aDouble : difficulty) {
                                    x++;
                                    write += "        { x: " + x + ", y: " + aDouble + " }";
                                    if (difficulty.indexOf(aDouble) != difficulty.size() - 1) {
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
                                for (Object aDouble : maintainability) {
                                    x++;
                                    write += "        { x: " + x + ", y: " + aDouble + " }";
                                    if (maintainability.indexOf(aDouble) != maintainability.size() - 1) {
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
                                        "<h1>" + Values.before.fileName + "</h1>\n" +
                                        "<h3>Code Quality Metrics</h3>\n" +
                                        "<div id=\"chartContainer\" style=\"height: 300px; width: 100%;\">" +
                                        "<div id=\"chartContainer2\" style=\"height: 300px; width: 100%;\">" +
                                        "<div id=\"chartContainer3\" style=\"height: 300px; width: 100%;\">" +
                                        "<div id=\"chartContainer4\" style=\"height: 300px; width: 100%;\">" +
                                        "<div id=\"chartContainer5\" style=\"height: 300px; width: 100%;\">" +
                                        "<div id=\"chartContainer6\" style=\"height: 300px; width: 100%;\">" +
                                        "<div id=\"chartContainer7\" style=\"height: 300px; width: 100%;\">" +
                                        "</div>\n" +
                                        "</body>\n" +
                                        "</html>";
                                System.out.println(write);
                                writer.println(write);
                                writer.close();

                                File f2 = new File(pathName);
                                Scanner myReader = new Scanner(f2);
                                while (myReader.hasNextLine()) {
                                    String data = myReader.nextLine();
                                    System.out.println(data);
                                }
                                myReader.close();
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(metrics, 0, Values.numSeconds, TimeUnit.SECONDS);
    }
}
