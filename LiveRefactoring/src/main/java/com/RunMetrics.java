package com;

import com.analysis.metrics.FileMetrics;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiJavaFile;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.Scanner;

public class RunMetrics extends AnAction {
    Project project = null;
    String URLProject = "";
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        project = e.getProject();
        String URL = "/Users/sarafernandes/Sara/Universidade/PhD/Experiência/Nova Experiência/Código Análise Resultados/Projetos";
        int[] numGroups = new int[]{10, 7, 8, 8, 9, 8, 10, 8, 7, 8, 8, 8, 6, 8, 8, 8};

        for(int i=0; i<numGroups.length; i++){
            String classNumber = "";
            int classes = i+1;
            if (classes < 10) classNumber = "0"+classes;
            else classNumber = Integer.toString(classes);

            for(int j=1; j<=numGroups[i]; j++){
                String groupNumber = "";
                if (j < 10) groupNumber = "0"+ j;
                else groupNumber = Integer.toString(j);

                String projectName = "project-l"+classNumber+"gr"+groupNumber;
                URLProject = URL + "/" + projectName;
                System.out.println(URLProject);

                File f = new File(URLProject + "/metrics.txt");
                if (f.exists())
                    System.out.println("Metrics already measured");
                else
                    listFilesForFolder(new File(URLProject));

                String fileLogs = URLProject + "/logs-cleaned.txt";
                File myObj = new File(fileLogs);
                Scanner myReader = null;
                String URLCopy = URLProject;
                try {
                    myReader = new Scanner(myObj);
                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                }
                while (myReader.hasNextLine()) {
                    String data = myReader.nextLine();
                    if(!data.equals("Logs") && data.contains("-")){
                        String commitToken = data.split("-")[0].trim();

                        URLProject = URLCopy + "-" + commitToken;
                        System.out.println(URLProject);
                        File f2 = new File(URLProject + "/metrics.txt");
                        if (f2.exists())
                            System.out.println("Metrics already measured");
                        else
                            listFilesForFolder(new File(URLProject));
                    }
                }
                myReader.close();
            }
        }
    }

    public void listFilesForFolder(final File folder) {
        if (folder != null) {
            if(folder.listFiles() != null) {
                for (final File fileEntry : folder.listFiles()) {
                    if (fileEntry.isDirectory()) {
                        listFilesForFolder(fileEntry);
                    } else {
                        if (fileEntry.getPath().contains("/src/") && fileEntry.getName().contains(".java")) {
                            Scanner myReader = null;
                            try {
                                myReader = new Scanner(fileEntry);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                            String text = "";
                            myReader.useDelimiter("\\Z");
                            if (myReader.hasNext()) {
                                text = myReader.next();
                                PsiFile newFile = PsiFileFactory.getInstance(project).createFileFromText(fileEntry.getName(), text);
                                if (newFile instanceof PsiJavaFile) {
                                    PsiJavaFile newJavaFile = (PsiJavaFile) newFile;
                                    try {
                                        FileMetrics metrics = new FileMetrics(newJavaFile);
                                        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(URLProject + "/metrics.txt", true)));
                                        out.println("============ File " + metrics.fileName + " ============");
                                        out.println(metrics.toString());
                                        out.close();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    myReader.close();
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
