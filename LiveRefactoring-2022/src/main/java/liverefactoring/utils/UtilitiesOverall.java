package com.utils;

import com.analysis.Candidates;
import com.analysis.candidates.*;
import com.core.MyRange;
import com.core.Refactorings;
import com.core.Severity;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.ui.VisualRepresentation;
import com.ui.icons.IconRenderer;
import com.utils.importantValues.SelectedRefactorings;
import com.utils.importantValues.Values;

import javax.swing.*;
import java.io.IOException;
import java.time.Instant;
import java.util.*;

public class UtilitiesOverall {
    ArrayList<String> variableFeatures = new ArrayList<>();
    ArrayList<String> structureFeatures = new ArrayList<>();
    Map<String, Integer> variableCountMap = new HashMap<>();
    Map<Integer, Integer> levelCountMap = new HashMap<>();
    public static ArrayList<PsiElement> children = new ArrayList<>();

    public UtilitiesOverall() {

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
            if(method.getContainingClass() != null) {
                PsiField[] fields = method.getContainingClass().getFields();
                for (PsiStatement statement : PsiTreeUtil.findChildrenOfType(method, PsiStatement.class)) {
                    for (PsiField field : fields) {
                        if (statement.getText().contains(field.getName())) {
                            names.add(statement.getText());
                        }
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
            if(field.getContainingClass() != null) {
                for (PsiMethod method : utils.getMethods(field.getContainingClass())) {
                    for (PsiStatement statement : PsiTreeUtil.findChildrenOfType(method, PsiStatement.class)) {
                        if (statement.getText().contains(field.getName())) {
                            names.add(statement.getText());
                        }
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

    public double dot(double[] array1, double[] array2) {
        double result = 0.0;
        if (array1.length >= array2.length) {
            for (int i = 0; i < array1.length; i++) {
                if (i <= array2.length - 1) {
                    result += array1[i] * array2[i];
                } else
                    result += array1[i];
            }
        } else {
            for (int i = 0; i < array2.length; i++) {
                if (i <= array1.length - 1) {
                    result += array1[i] * array2[i];
                } else
                    result += array2[i];
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
            if (!array1.get(i).equals(array2.get(i))) {
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
            System.out.print("\nFile " + psiFile + " not in current project " + project+ "\n");
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

    public Icon getIcon(int index){
        Icon icon = null;
        if (index == 1) icon = !Values.colorBlind ? IconRenderer.gutter1 : IconRenderer.colorBlind1;
        else if (index == 2) icon = !Values.colorBlind ? IconRenderer.gutter2 : IconRenderer.colorBlind2;
        else if (index == 3) icon = !Values.colorBlind ? IconRenderer.gutter3 : IconRenderer.colorBlind3;
        else if (index == 4) icon = !Values.colorBlind ? IconRenderer.gutter4 : IconRenderer.colorBlind4;
        else if (index == 5) icon = !Values.colorBlind ? IconRenderer.gutter5 : IconRenderer.colorBlind5;
        else if (index == 6) icon = !Values.colorBlind ? IconRenderer.gutter6 : IconRenderer.colorBlind6;
        else if (index == 7) icon = !Values.colorBlind ? IconRenderer.gutter7 : IconRenderer.colorBlind7;
        else if (index == 8) icon = !Values.colorBlind ? IconRenderer.gutter8 : IconRenderer.colorBlind8;
        else if (index == 9) icon = !Values.colorBlind ? IconRenderer.gutter9 : IconRenderer.colorBlind9;
        else if (index == 10) icon = !Values.colorBlind ? IconRenderer.gutter10 : IconRenderer.colorBlind10;

        return icon;
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
                if (severityCandidate instanceof StringComparisonCandidate && candidate instanceof StringComparisonCandidate) {
                    if (((StringComparisonCandidate) severityCandidate).range.equals(((StringComparisonCandidate) candidate).range))
                        return false;
                }
                if (severityCandidate instanceof MoveMethodCandidate && candidate instanceof MoveMethodCandidate) {
                    if (((MoveMethodCandidate) severityCandidate).range.equals(((MoveMethodCandidate) candidate).range))
                        return false;
                }
                if (severityCandidate instanceof IntroduceParamObjCandidate && candidate instanceof IntroduceParamObjCandidate) {
                    if (((IntroduceParamObjCandidate) severityCandidate).range.equals(((IntroduceParamObjCandidate) candidate).range))
                        return false;
                }
                if (severityCandidate instanceof InheritanceToDelegationCandidate && candidate instanceof InheritanceToDelegationCandidate) {
                    LogicalPosition start = editor.offsetToLogicalPosition(((InheritanceToDelegationCandidate) severityCandidate)._class.getTextRange().getStartOffset());
                    LogicalPosition end = editor.offsetToLogicalPosition(((InheritanceToDelegationCandidate) severityCandidate)._class.getTextRange().getEndOffset());

                    LogicalPosition startCandidate = editor.offsetToLogicalPosition(((InheritanceToDelegationCandidate) candidate)._class.getTextRange().getStartOffset());
                    LogicalPosition endCandidate = editor.offsetToLogicalPosition(((InheritanceToDelegationCandidate) candidate)._class.getTextRange().getEndOffset());
                    MyRange range1 = new MyRange(start, end);
                    MyRange range2 = new MyRange(startCandidate, endCandidate);

                    if (range1.equals(range2))
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

    public void includeAllRefactorings() {
        SelectedRefactorings.selectedRefactoring = Refactorings.All;
        SelectedRefactorings.selectedRefactorings.add(Refactorings.ExtractMethod);
        SelectedRefactorings.selectedRefactorings.add(Refactorings.ExtractClass);
        SelectedRefactorings.selectedRefactorings.add(Refactorings.ExtractVariable);
        SelectedRefactorings.selectedRefactorings.add(Refactorings.MoveMethod);
        SelectedRefactorings.selectedRefactorings.add(Refactorings.IntroduceParamObj);
        SelectedRefactorings.selectedRefactorings.add(Refactorings.StringComparison);
        SelectedRefactorings.selectedRefactorings.add(Refactorings.InheritanceToDelegation);
    }

    public void checkOpenFile(PsiJavaFile psiJavaFile){
        boolean found = false;
        if (Values.openedRefactorings.size() == 0)
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
    }

    public void startActions(PsiJavaFile psiJavaFile) {
        System.out.print("\n*** Starting analysis ***\n");
        if (Values.isFirst)
            Values.startTime = Instant.now();
        Candidates candidates = new Candidates();
        try {
            candidates.getCandidates(psiJavaFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String removeExtension(String fileName){
        fileName = fileName.substring(0, fileName.lastIndexOf('.'));
        return fileName;
    }

    public static Set<String> exclusiveDependencies(Set<String> a, Set<String> b){
        for (String s : b) {
            a.remove(s);
        }

        return a;
    }

    public void getChildren(PsiElement element){
        if(element.getChildren().length == 0)
            return;
        for (PsiElement child : element.getChildren()) {
            children.add(child);
            getChildren(child);
        }
    }

    public void displayRefactorings(PsiJavaFile psiJavaFile) {
        try {
            if (Values.candidates.size() > 0) {
                VisualRepresentation representation = new VisualRepresentation();
                representation.startVisualAnalysis(Values.editor, Values.candidates);
            }

        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}
