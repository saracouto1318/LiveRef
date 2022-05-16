package com.utils;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.BalloonBuilder;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.awt.RelativePoint;
import com.refactorings.ExtractMethod;
import com.refactorings.candidates.*;
import com.refactorings.candidates.utils.Severity;
import com.ui.VisualRepresentation;
import org.apache.commons.io.IOUtils;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.List;

public class Utilities {
    ArrayList<String> variableFeatures = new ArrayList<>();
    ArrayList<String> structureFeatures = new ArrayList<>();
    Map<String, Integer> variableCountMap = new HashMap<>();
    Map<Integer, Integer> levelCountMap = new HashMap<>();

    public Utilities() {

    }

    /**
     * Insert any number of arrays between 2 columns of a matrix. Size of the arrays must
     * equal number of rows in the matrix.
     * Example:<br>
     * <code>
     * double[][] a = {{0,1,2,3,4},{1,7,8,9,10},{2,13,14,15,16},{3,19,20,21,22},{4,23,24,25,26}};<br>
     * double[] b = {00,11,22,33,44}, c = {55,66,77,88,99};<br>
     * double[][] z = insertColumns(a, 2, b, c);<br>
     * input matrix is:<br>
     * 0   1   2   3   4<br>
     * 1   7   8   9  10<br>
     * 2  13  14  15  16<br>
     * 3  19  20  21  22<br>
     * 4  23  24  25  26<br>
     * result is:<br>
     * 0   1   0  55   2   3   4<br>
     * 1   7  11  66   8   9  10<br>
     * 2  13  22  77  14  15  16<br>
     * 3  19  33  88  20  21  22<br>
     * 4  23  44  99  24  25  26<br>
     * </code>
     *
     * @param x Input m x n matrix.
     * @param J Index of column before which the new columns will be inserted.
     * @param y The arrays to be inserted
     * @return New matrix with added columns.
     */
    public static double[][] insertColumns(double[][] x, int J, double[]... y) {
        return transpose(insertRows(transpose(x), J, y));
    }

    /**
     * Insert any number of arrays between 2 rows of a matrix. Size of the arrays must
     * equal number of columns in the matrix.
     * Example:<br>
     * <code>
     * double[][] a = {{0,1,2,3,4},{1,7,8,9,10},{2,13,14,15,16},{3,19,20,21,22}};<br>
     * double[] b = {0,11,22,33,44}, c = {55,66,77,88,99};<br>
     * double[][] z = insertRows(a, 1, b, c);<br>
     * result is:<br>
     * 0   1   2   3   4<br>
     * 0  11  22  33  44<br>
     * 55  66  77  88  99<br>
     * 1   7   8   9  10<br>
     * 2  13  14  15  16<br>
     * 3  19  20  21  22<br>
     * </code>
     *
     * @param x Input m x n matrix.
     * @param I Index of row before which the new rows will be inserted.
     * @param y The arrays to be inserted
     * @return New matrix with added rows.
     */
    public static double[][] insertRows(double[][] x, int I, double[]... y) {
        if(x.length > 0){
            double[][] array = new double[x.length + y.length][x[0].length];
            for (int i = 0; i < I; i++)
                System.arraycopy(x[i], 0, array[i], 0, x[i].length);
            for (int i = 0; i < y.length; i++)
                System.arraycopy(y[i], 0, array[i + I], 0, y[i].length);
            for (int i = 0; i < x.length - I; i++)
                System.arraycopy(x[i + I], 0, array[i + I + y.length], 0, x[i].length);
            return array;
        }
        return x;
    }

    /**
     * Deletes a list of columns from a matrix.
     * Example:<br>
     * <code>
     * double[][] a = {{0,1,2,3,4},{1,7,8,9,10},{2,13,14,15,16},{3,19,20,21,22},{4,23,24,25,26}};<br>
     * double[][] z = deleteColumns(a, 1, 3);<br>
     * result is:<br>
     * 0  2   4<br>
     * 1  8  10<br>
     * 2  14 16<br>
     * 3  20 22<br>
     * 4  24 26<br>
     * </code>
     *
     * @param x The input matrix
     * @param J The indices of the columns to be deleted. There must be no more indices listed
     *          than there are columns in the input matrix.
     * @return The reduced matrix.
     */
    public static double[][] deleteColumns(double[][] x, int... J) {
        // TODO improve efficiency here
        return transpose(deleteRows(transpose(x), J));
    }

    /**
     * Deletes a list of rows from a matrix.
     * Example:<br>
     * <code>
     * double[][] a = {{0,1,2,3,4},{1,7,8,9,10},{2,13,14,15,16},{3,19,20,21,22},{4,23,24,25,26}};<br>
     * double[][] z = deleteRows(a, 1, 3);<br>
     * result is:<br>
     * 0   1   2   3   4<br>
     * 2  13  14  15  16<br>
     * 4  23  24  25  26<br>
     * </code>
     *
     * @param x The input matrix
     * @param I The indices of the rows to delete.
     * @return The reduced matrix.
     */
    public static double[][] deleteRows(double[][] x, int... I) {
        if(x.length > 0) {
            double[][] array = new double[x.length - I.length][x[0].length];
            int i2 = 0;
            for (int i = 0; i < x.length; i++) {
                if (!into(i, I)) {
                    System.arraycopy(x[i], 0, array[i2], 0, x[i].length);
                    i2++;
                }
            }
            return array;
        }
        return x;
    }

    /**
     * Determines if a value is within an array
     *
     * @param i Value to be searched for.
     * @param I array to be searched
     * @return true if found, false if not.
     */
    private static boolean into(int i, int[] I) {
        boolean in = false;
        for (int value : I) {
            in = in || (i == value);
        }
        return in;
    }


    /**
     * Transposes an mxn matrix into an nxm matrix. Each row of the input matrix becomes a column in the
     * output matrix.
     *
     * @param M Input matrix.
     * @return Transposed version of M.
     */
    public static double[][] transpose(double[][] M) {
        if(M.length > 0){
            double[][] tM = new double[M[0].length][M.length];
            for (int i = 0; i < tM.length; i++) {
                for (int j = 0; j < tM[0].length; j++) {
                    tM[i][j] = M[j][i];
                }
            }
            return tM;
        }
        return M;
    }

    /*public static int maxInteger(ArrayList<Integer> t) {
        int maximum = t.get(0);   // start with the first value
        for (int i = 1; i < t.size(); i++) {
            if (t.get(i) > maximum) {
                maximum = t.get(i);   // new maximum
            }
        }
        return maximum;
    }*/

    public static double hypot(double var0, double var2) {
        double var4;
        if (Math.abs(var0) > Math.abs(var2)) {
            var4 = var2 / var0;
            var4 = Math.abs(var0) * Math.sqrt(1.0D + var4 * var4);
        } else if (var2 != 0.0D) {
            var4 = var0 / var2;
            var4 = Math.abs(var2) * Math.sqrt(1.0D + var4 * var4);
        } else {
            var4 = 0.0D;
        }

        return var4;
    }

    public Set<String> getEntitiesStatement(PsiStatement statement) {
        ArrayList<String> varStrct = getStructureAndVariableFeatures(statement);

        return new HashSet<>(varStrct);
    }

    public ArrayList<String> getVariableFeatures(PsiStatement statement){
        PsiStatement[] array = new PsiStatement[1];
        array[0] = statement;
        this._variableFeaturesRecursive(array);
        return variableFeatures;
    }

    public ArrayList<String> getStructureFeatures(PsiStatement statement){
        PsiStatement[] array = new PsiStatement[1];
        array[0] = statement;
        this._structureFeaturesRecursive(array);
        return structureFeatures;
    }

    public ArrayList<String> getStructureAndVariableFeatures(PsiStatement statement){
        ArrayList<String> structureVariableFeatures = new ArrayList<>();
        ArrayList<String> variableFeatures = this.getVariableFeatures(statement);
        ArrayList<String> structureFeatures = this.getStructureFeatures(statement);

        for(int i = 0; i < variableFeatures.size(); i++){
            structureVariableFeatures.add(variableFeatures.get(i));
            if(i < structureFeatures.size())
                structureVariableFeatures.add(structureFeatures.get(i));
        }

        return structureVariableFeatures;
    }

    public void _variableFeaturesRecursive(PsiElement[] statements){
        for (PsiElement statement : statements) {
            ArrayList<String> variableNames = new ArrayList<>();
            for (PsiVariable psiVariable : PsiTreeUtil.findChildrenOfType(statement, PsiVariable.class)) {
                String varName = psiVariable.getName();

                if (!variableCountMap.containsKey(varName))
                    variableCountMap.put(varName, 1);
                if (statement instanceof PsiDeclarationStatement) {
                    variableCountMap.replace(varName, variableCountMap.get(varName) + 1);
                }

                variableNames.add(varName + "_" + variableCountMap.get(varName));
            }

            variableFeatures.addAll(variableNames);

            if((statement instanceof PsiForStatement || statement instanceof PsiIfStatement ||
                    statement instanceof PsiSwitchStatement || statement instanceof PsiTryStatement) &&
                    PsiTreeUtil.findChildrenOfType(statement, PsiCodeBlock.class).size() > 0){
                this._variableFeaturesRecursive(statement.getChildren());
            }
        }
    }

    public void _structureFeaturesRecursive(PsiElement[] statements){
        for (PsiElement stm : statements) {
            if(stm instanceof PsiTryStatement) {
                PsiStatement statement = (PsiStatement) stm;

                int level;

                if (PsiTreeUtil.findChildrenOfType(statement, PsiCodeBlock.class).size() > 0) {
                    level = 1;

                    if (levelCountMap.containsKey(level))
                        levelCountMap.replace(level, levelCountMap.get(level) + 1);
                    else levelCountMap.replace(level, 1);

                    structureFeatures.addAll(this.buildFeaturesUntilLevel(level, levelCountMap));

                    this._structureFeaturesRecursive(statement.getChildren());

                } else {
                    level = 0;

                    structureFeatures.addAll(this.buildFeaturesUntilLevel(level, levelCountMap));
                }
            }
        }
    }

    public ArrayList<String> buildFeaturesUntilLevel(int level, Map<Integer, Integer> levelCountMap){
        ArrayList<String> features = new ArrayList<>();
        for(int i = 1; i <= level; i++){
            features.add("level-" + i + "_" + levelCountMap.get(i));
        }

        return features;
    }

    public Set<String> getEntities(Object entity) {
        Set<String> names = new HashSet<>();

        if (entity instanceof PsiMethod) {
            PsiMethod method = (PsiMethod) entity;
            names.add(method.getName());
            PsiField[] fields = Objects.requireNonNull(method.getContainingClass()).getFields();
            for (PsiStatement statement : PsiTreeUtil.findChildrenOfType(method, PsiStatement.class)) {
                for (PsiField field : fields) {
                    if (statement.getText().contains(field.getName())) {
                        names.add(statement.getText());
                    }
                }
            }

            for (PsiMethodCallExpression psiMethodCallExpression : PsiTreeUtil.findChildrenOfType(method, PsiMethodCallExpression.class)) {
                names.add(psiMethodCallExpression.getText());
            }
        } else if (entity instanceof PsiField) {
            PsiField field = (PsiField) entity;
            names.add(field.getName());
            RefactorUtils utils = new RefactorUtils();
            for (PsiMethod method : utils.getMethods(Objects.requireNonNull(field.getContainingClass()))) {
                for (PsiStatement statement : PsiTreeUtil.findChildrenOfType(method, PsiStatement.class)) {
                    if (statement.getText().contains(field.getName())) {
                        names.add(statement.getText());
                    }
                }
            }
        }

        return names;
    }

    public <T> T[] append(T[] arr, T element) {
        final int N = arr.length;
        arr = Arrays.copyOf(arr, N + 1);
        arr[N] = element;
        return arr;
    }

    /*public void listFilesForFolder(final File folder) {
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
                files.add(fileEntry);
            }
        }
    }*/

    public double dot(ArrayList<Double> array1, ArrayList<Double> array2) {
        double result = 0.0;
        if (array1.size() >= array2.size()) {
            for (int i = 0; i < array1.size(); i++) {
                if (i <= array2.size() - 1) {
                    result += array1.get(i) * array2.get(i);
                } else
                    result += array1.get(i);
            }
        } else {
            for (int i = 0; i < array2.size(); i++) {
                if (i <= array1.size() - 1) {
                    result += array1.get(i) * array2.get(i);
                } else
                    result += array2.get(i);
            }
        }

        return result;
    }

    public List<List<PsiMethod>> generateCombinations(List<PsiMethod> list) {
        List<List<PsiMethod>> result = new ArrayList<>();
        for (int i = 0, max = 1 << list.size(); i < max; ++i) {
            List<PsiMethod> comb = new ArrayList<>();
            for (int j = 0, k = i; k > 0; ++j, k >>= 1)
                if ((k & 1) == 1)
                    comb.add(list.get(j));
            result.add(comb);
        }
        return result;
    }

    public boolean areArraysEqual(ArrayList<String> array1, ArrayList<String> array2) {
        if (array1.size() != array2.size()) {
            return false;
        }

        Collections.sort(array1);
        Collections.sort(array2);
        for (int i = 0; i < array1.size(); i++) {
            if (array1.get(i).equals(array2.get(i))) {
                return false;
            }
        }
        return true;
    }

    /*public Project getActiveProject() {
        Project[] projects = ProjectManager.getInstance().getOpenProjects();
        Project activeProject = null;
        for (Project project : projects) {
            Window window = WindowManager.getInstance().suggestParentWindow(project);
            if (window != null && window.isActive()) {
                activeProject = project;
            }
        }
        return activeProject;
    }*/

    public boolean isPsiFileInProject(Project project, PsiFile psiFile) {
        boolean inProject = ProjectRootManager.getInstance(project)
                .getFileIndex().isInContent(psiFile.getVirtualFile());
        if (!inProject) {
            System.out.println("File " + psiFile + " not in current project " + project);
        }
        return inProject;
    }

    public String getURL() {
        String aux = PathManager.getPluginsPath();
        StringBuilder path = new StringBuilder();

        String[] splits = aux.split("/");
        for (String split : splits) {
            if (split.equals("build")) {
                break;
            } else {
                path.append(split).append("/");
            }
        }

        return path.toString();
    }

    public ImageIcon getIcon(int index, String type) throws IOException {
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
        ImageIcon image = null;
        InputStream inputStream = null;
        String result = "";
        String[] lines =  null;

        if(!ThresholdsCandidates.colorBlind){
            if(SelectedRefactorings.selectedRefactoring == Refactorings.All || SelectedRefactorings.selectedRefactorings.size() > 1) {
                switch (type) {
                    case "EV":
                        inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("images/colors/colorsEV.txt");
                        result = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                        lines = result.split("\n");
                        break;
                    case "EM":
                        inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("images/colors/colorsEM.txt");
                        result = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                        lines = result.split("\n");
                        break;
                    case "EC":
                        inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("images/colors/colorsEC.txt");
                        result = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                        lines = result.split("\n");
                        break;
                    case "MM":
                        inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("images/colors/colorsMM.txt");
                        result = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                        lines = result.split("\n");
                        break;
                    case "IPO":
                        inputStream =Thread.currentThread().getContextClassLoader().getResourceAsStream("images/colors/colorsIPO.txt");
                        result = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                        lines = result.split("\n");
                        break;
                }
            }
            else{
                inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("images/colors/colors.txt");
                result = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                lines = result.split("\n");
            }
        }
        else {
            switch (type) {
                case "EV":
                    inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("images/colors/colorBlind/colorsEV.txt");
                    result = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                    lines = result.split("\n");
                    break;
                case "EM":
                    inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("images/colors/colorBlind/colorsEM.txt");
                    result = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                    lines = result.split("\n");
                    break;
                case "EC":
                    inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("images/colors/colorBlind/colorsEC.txt");
                    result = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                    lines = result.split("\n");
                    break;
                case "MM":
                    inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("images/colors/colorBlind/colorsEM.txt");
                    result = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                    lines = result.split("\n");
                    break;
                case "IPO":
                    inputStream =Thread.currentThread().getContextClassLoader().getResourceAsStream("images/colors/colorBlind/colorsEC.txt");
                    result = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                    lines = result.split("\n");
                    break;
            }
        }

        for (int i = 0; i < lines.length; i++) {
            if ((i + 1) == index) {
                String url = (ThresholdsCandidates.colorBlind) ? "images/colors/colorBlind/" : "images/colors/";
                url += lines[i].split(" ")[0] + ".png";
                image = new ImageIcon(Thread.currentThread().getContextClassLoader().getResource(url));
                break;
            }
        }

        return image;
    }

    public ImageIcon getIconCircle(int index, String type) throws IOException {
        InputStream inputStream = null;
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
        String[] lines = null;
        String result = "";
        ImageIcon image = null;

        if(!ThresholdsCandidates.colorBlind){
            if(SelectedRefactorings.selectedRefactoring == Refactorings.All || SelectedRefactorings.selectedRefactorings.size() > 1) {
                switch (type) {
                    case "EV":
                        inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("images/colors/colorsEV.txt");
                        result = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                        lines = result.split("\n");
                        break;
                    case "EM":
                        inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("images/colors/colorsEM.txt");
                        result = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                        lines = result.split("\n");
                        break;
                    case "EC":
                        inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("images/colors/colorsEC.txt");
                        result = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                        lines = result.split("\n");
                        break;
                    case "MM":
                        inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("images/colors/colorsMM.txt");
                        result = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                        lines = result.split("\n");
                        break;
                    case "IPO":
                        inputStream =Thread.currentThread().getContextClassLoader().getResourceAsStream("images/colors/colorsIPO.txt");
                        result = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                        lines = result.split("\n");
                        break;
                }
            }
            else{
                inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("images/colors/colors.txt");
                result = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                lines = result.split("\n");
            }

            for (int i = 0; i < lines.length; i++) {
                if ((i + 1) == index) {
                    String url = "images/colors/circles/" + lines[i].split(" ")[0] + ".png";
                    image = new ImageIcon(Thread.currentThread().getContextClassLoader().getResource(url));
                    break;
                }
            }
        }
        else{
            String url = "";
            switch (type) {
                case "EV":
                    url = "images/colors/colorBlind/EV.png";
                    image = new ImageIcon(Thread.currentThread().getContextClassLoader().getResource(url));
                    break;
                case "EM":
                    url = "images/colors/colorBlind/EM.png";
                    image = new ImageIcon(Thread.currentThread().getContextClassLoader().getResource(url));
                    break;
                case "EC":
                    url = "images/colors/colorBlind/EC.png";
                    image = new ImageIcon(Thread.currentThread().getContextClassLoader().getResource(url));
                    break;
                case "MM":
                    url = "images/colors/colorBlind/EM.png";
                    image = new ImageIcon(Thread.currentThread().getContextClassLoader().getResource(url));
                    break;
                case "IPO":
                    url = "images/colors/colorBlind/EV.png";
                    image = new ImageIcon(Thread.currentThread().getContextClassLoader().getResource(url));
                    break;
            }
        }

        return image;
    }

    /*public Color getColor(int index) throws IOException {
        String icon = this.getURLImage(index);
        String colorName = icon.split("/")[icon.split("/").length - 1];
        Color color = null;
        String url = this.getURLImage();
        List<String> lines = Files.readAllLines(Paths.get(url + "colors.txt"));

        for (String line : lines) {
            String[] lineSplit = line.split(" ");
            if (colorName.contains(lineSplit[0])) {
                color = new Color(Integer.parseInt(lineSplit[1]), Integer.parseInt(lineSplit[2]), Integer.parseInt(lineSplit[3]));
                break;
            }
        }

        return color;
    }*/

    public boolean containsInArray(Editor editor, Severity severity, ArrayList<Severity> clickedCandidates) {
        Object severityCandidate = severity.candidate;
        for (Severity clickedCandidate : clickedCandidates) {
            if (clickedCandidate.severity == severity.severity && clickedCandidate.indexColorGutter == severity.indexColorGutter) {
                Object candidate = clickedCandidate.candidate;
                if (severityCandidate instanceof ExtractMethodCandidate && candidate instanceof ExtractMethodCandidate) {
                    if (((ExtractMethodCandidate) severityCandidate).range.equals(((ExtractMethodCandidate) candidate).range))
                        return false;
                }
                if (severityCandidate instanceof ExtractVariableCandidate && candidate instanceof ExtractVariableCandidate) {
                    if (((ExtractVariableCandidate) severityCandidate).range.equals(((ExtractVariableCandidate) candidate).range))
                        return false;
                }
                if (severityCandidate instanceof ExtractClassCandidate && candidate instanceof ExtractClassCandidate) {
                    LogicalPosition start = editor.offsetToLogicalPosition(((ExtractClassCandidate) severityCandidate).targetEntities.get(0).getTextRange().getStartOffset());
                    LogicalPosition end = editor.offsetToLogicalPosition(((ExtractClassCandidate) severityCandidate).targetEntities.get(((ExtractClassCandidate) severityCandidate).targetEntities.size() - 1).getTextRange().getEndOffset());

                    LogicalPosition startCandidate = editor.offsetToLogicalPosition(((ExtractClassCandidate) candidate).targetEntities.get(0).getTextRange().getStartOffset());
                    LogicalPosition endCandidate = editor.offsetToLogicalPosition(((ExtractClassCandidate) candidate).targetEntities.get(((ExtractClassCandidate) candidate).targetEntities.size() - 1).getTextRange().getEndOffset());

                    if (((ExtractClassCandidate) severityCandidate).targetEntities.size() == ((ExtractClassCandidate) candidate).targetEntities.size() &&
                            ((ExtractClassCandidate) severityCandidate).avgMethods == ((ExtractClassCandidate) candidate).avgMethods) {
                        if (Objects.equals(((ExtractClassCandidate) severityCandidate).targetClass.getName(), ((ExtractClassCandidate) candidate).targetClass.getName())) {
                            MyRange range1 = new MyRange(start, end);
                            MyRange range2 = new MyRange(startCandidate, endCandidate);

                            if (range1.equals(range2))
                                return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public void startActions(PsiJavaFile psiJavaFile) {
        try {
            Values.startTime = Instant.now();
            Candidates candidates = new Candidates();
            ArrayList<Severity> severities = candidates.getCandidates(Values.editor, psiJavaFile);
            Values.candidates = severities;
            if(!Values.metricsFile.containsKey(psiJavaFile.getName())) {
                ArrayList<ArrayList<Double>> array = new ArrayList<>();
                ArrayList<Double> metrics = new ArrayList<>();
                metrics.add(Values.before.lines);
                array.add(metrics);
                metrics = new ArrayList<>();
                metrics.add(Values.before.complexity);
                array.add(metrics);
                metrics = new ArrayList<>();
                metrics.add(Values.before.cognitiveComplexity);
                array.add(metrics);
                metrics = new ArrayList<>();
                metrics.add(Values.before.halsteadVolume);
                array.add(metrics);
                metrics = new ArrayList<>();
                metrics.add(Values.before.halsteadEffort);
                array.add(metrics);
                metrics = new ArrayList<>();
                metrics.add(Values.before.halsteadDifficulty);
                array.add(metrics);
                metrics = new ArrayList<>();
                metrics.add(Values.before.halsteadMaintainability);
                array.add(metrics);

                Values.metricsFile.put(psiJavaFile.getName(), array);
            }

            /*Date dateNow = new Date();
            String date = dateNow.toString();

            String endPoint = Values.editor.getProject().getName() + "/version 1/" + date + "/";
            HashMap<String, Object> numRefactorings = new HashMap<>();
            numRefactorings.put("Num Refactorings", Values.candidates.size());
            Values.db.child(endPoint).setValueAsync(numRefactorings);*/

            boolean found = false;
            if(Values.openedRefactorings.size() == 0)
                Values.openedRefactorings.put(psiJavaFile.getName(), Values.candidates);
            else {
                for (String javaFile : Values.openedRefactorings.keySet()) {
                    if (javaFile.equals(psiJavaFile.getName())) {
                        Values.openedRefactorings.replace(javaFile, Values.candidates);
                        found = true;
                        break;
                    }
                }
                if (!found)
                    Values.openedRefactorings.put(psiJavaFile.getName(), Values.candidates);
            }

            /*if(Values.candidates.size() > 0) {
                CaretModel caretModel = Values.editor.getCaretModel();
                //Moving caret to line number
                ExtractMethodCandidate candidate = ((ExtractMethodCandidate) Values.candidates.get(0).candidate);
                caretModel.moveToLogicalPosition(new LogicalPosition(candidate.range.start.line - 1, 0));

                //Scroll to the caret
                ScrollingModel scrollingModel = Values.editor.getScrollingModel();
                scrollingModel.scrollToCaret(ScrollType.CENTER);
                Values.withColors = true;
                //version 8
                //Values.withColors = false;
                VisualRepresentation representation = new VisualRepresentation();
                representation.startVisualAnalysis(Values.editor, severities);
            }
            else{
                JBPopupFactory factory = JBPopupFactory.getInstance();

                BalloonBuilder builder =
                        factory.createHtmlTextBalloonBuilder("<b>No refactoring candidates found...</b>", MessageType.INFO, null);
                Balloon b = builder.createBalloon();

                b.show(RelativePoint.getSouthEastOf(WindowManager.getInstance().getStatusBar(Values.editor.getProject()).getComponent()), Balloon.Position.above);
                MarkupModel markupModel = Values.editor.getMarkupModel();
                markupModel.removeAllHighlighters();
            }*/

            try {
                if(Values.candidates.size() > 0) {
                    Values.withColors = true;
                    //version 8
                    //Values.withColors = false;
                    VisualRepresentation representation = new VisualRepresentation();
                    representation.startVisualAnalysis(Values.editor, severities);
                }
                else
                {
                    JBPopupFactory factory = JBPopupFactory.getInstance();

                    BalloonBuilder builder =
                            factory.createHtmlTextBalloonBuilder("<b>No refactoring candidates found...</b>", MessageType.INFO, null);
                    Balloon b = builder.createBalloon();

                    b.show(RelativePoint.getSouthEastOf(WindowManager.getInstance().getStatusBar(Values.editor.getProject()).getComponent()), Balloon.Position.above);
                    MarkupModel markupModel = Values.editor.getMarkupModel();
                    markupModel.removeAllHighlighters();
                }

            } catch (IOException ioException) {
                ioException.printStackTrace();
            }

            String pathName = Values.editor.getProject().getBasePath()+"/index.html";
            System.out.println(pathName);
            File f = new File(pathName);
            try {
                f.createNewFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            PrintWriter writer = null;
            try {
                writer = new PrintWriter(f);
                writer.print("");
                writer.close();
                writer = new PrintWriter(f);
                writer.println("<html>\n <style>\n" +
                        "  .styled-table {\n" +
                        "    border-collapse: collapse;\n" +
                        "    margin: 25px 0;\n" +
                        "    font-size: 0.9em;\n" +
                        "    font-family: sans-serif;\n" +
                        "    min-width: 400px;\n" +
                        "    box-shadow: 0 0 20px rgba(0, 0, 0, 0.15);\n" +
                        "  }\n" +
                        ".styled-table thead tr {\n" +
                        "    background-color: #009879;\n" +
                        "    color: #ffffff;\n" +
                        "    text-align: left;\n" +
                        "}\n" +
                        "styled-table th,\n" +
                        ".styled-table td {\n" +
                        "    padding: 12px 15px;\n" +
                        "}\n.styled-table tbody tr {\n" +
                        "    border-bottom: 1px solid #dddddd;\n" +
                        "}\n" +
                        "\n" +
                        ".styled-table tbody tr:nth-of-type(even) {\n" +
                        "    background-color: #f3f3f3;\n" +
                        "}\n" +
                        "\n" +
                        ".styled-table tbody tr:last-of-type {\n" +
                        "    border-bottom: 2px solid #009879;\n" +
                        "}" +
                        " </style>\n<body><h1>Refactoring candidates</h1>");
                writer.println("<table class='styled-table'>\n" +
                        "  <thead><tr>\n" +
                        "    <th>Refactoring</th>\n" +
                        "    <th>Lines</th>\n" +
                        "    <th>Severity</th>\n" +
                        "    <th>Elements</th>\n" +
                        "  </tr></thead><tbody>\n");

                for (Severity candidate : Values.candidates) {
                    String type = "";

                    if(candidate.candidate instanceof ExtractMethodCandidate) {
                        type = "Extract Method";
                        ExtractMethodCandidate em = (ExtractMethodCandidate) candidate.candidate;
                        writer.println("<tr>\n" +
                                "    <td>"+type+"</td>\n" +
                                "    <td>"+em.range+"</td> \n" +
                                "    <td>"+candidate.severity+"</td> \n" +
                                "    <td>"+em.numberOfStatementsToExtract+"</td> \n" +
                                "  </tr>");
                    }
                    else if(candidate.candidate instanceof ExtractVariableCandidate) {
                        type = "Extract Variable";
                        ExtractVariableCandidate ev = (ExtractVariableCandidate) candidate.candidate;
                        writer.println("<tr>\n" +
                                "    <td>"+type+"</td>\n" +
                                "    <td>"+ev.range+"</td> \n" +
                                "    <td>"+candidate.severity+"</td> \n" +
                                "    <td>1</td> \n" +
                                "  </tr>");
                    }
                    else if(candidate.candidate instanceof ExtractClassCandidate) {
                        type = "Extract Class";
                        ExtractClassCandidate ec = (ExtractClassCandidate) candidate.candidate;
                        writer.println("<tr>\n" +
                                "    <td>"+type+"</td>\n" +
                                "    <td>-</td> \n" +
                                "    <td>"+candidate.severity+"</td> \n" +
                                "    <td>"+ec.targetEntities+"</td> \n" +
                                "  </tr>");
                    }
                    else if(candidate.candidate instanceof MoveMethodCandidate) {
                        type = "Move Method";
                        MoveMethodCandidate mm = (MoveMethodCandidate) candidate.candidate;
                        writer.println("<tr>\n" +
                                "    <td>"+type+"</td>\n" +
                                "    <td>"+mm.range+"</td> \n" +
                                "    <td>"+candidate.severity+"</td> \n" +
                                "    <td>"+mm.nodes.size()+"</td> \n" +
                                "  </tr>");
                    }
                    else if(candidate.candidate instanceof IntroduceParamObjCandidate){
                        type = "Introduce Parameter Object";
                        IntroduceParamObjCandidate intro = (IntroduceParamObjCandidate) candidate.candidate;
                        int start = Values.editor.offsetToLogicalPosition(intro.method.getTextRange().getStartOffset()).line;
                        int end = Values.editor.offsetToLogicalPosition(intro.method.getTextRange().getEndOffset()).line;
                        writer.println("<tr>\n" +
                                "    <td>"+type+"</td>\n" +
                                "    <td>("+start + " -> " + end +")</td> \n" +
                                "    <td>"+candidate.severity+"</td> \n" +
                                "    <td>"+intro.originalParameters.size()+"</td> \n" +
                                "  </tr>");
                    }
                }
                writer.println("</tbody></table>");
                writer.println("</body></html>");
                writer.close();

            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
        } catch (Throwable t) {
            System.out.println("Error:" + t);
        }
    }

    public static Set<String> exclusiveDependencies(Set<String> a, Set<String> b){
        for (String s : b) {
            a.remove(s);
        }

        return a;
    }
}
