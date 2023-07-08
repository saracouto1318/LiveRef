package com.analysis;

import com.analysis.metrics.ClassMetrics;
import com.analysis.metrics.FileMetrics;
import com.analysis.metrics.MethodMetrics;
import com.analysis.refactorings.*;
import com.core.LastRefactoring;
import com.core.Refactorings;
import com.core.Severity;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.BalloonBuilder;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.awt.RelativePoint;
import com.utils.MetricsUtils;
import com.utils.UtilitiesOverall;
import com.utils.importantValues.SelectedRefactorings;
import com.utils.importantValues.ThresholdsCandidates;
import com.utils.importantValues.Values;

import java.io.PrintStream;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class Candidates {
    public ArrayList<Severity> severities;
    public UtilitiesOverall utils = new UtilitiesOverall();
    public long start = 0;
    public ClassMetrics oldClass = null;
    public ClassMetrics newClass = null;
    public MethodMetrics oldMethod = null;
    public MethodMetrics newMethod = null;

    public Candidates() {
        severities = new ArrayList<>();
    }

    public void getCandidates(PsiJavaFile psiJavaFile) {
        System.out.print("\n*** Getting candidates ***\n");
        this.start = System.nanoTime();

        FileMetrics metrics = null;

        if(Values.before != null) {
            if(Values.after != null){
                if(Values.after.fileName.equals(psiJavaFile.getName()))
                    metrics = new FileMetrics(Values.after);
                else {
                    try {
                        metrics = new FileMetrics(Values.editor, psiJavaFile);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            else {
                if(Values.before.fileName.equals(psiJavaFile.getName()))
                    metrics = new FileMetrics(Values.before);
                else {
                    try {
                        metrics = new FileMetrics(Values.editor, psiJavaFile);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        else{
            boolean exists = false;
            for(int i=0; i < Values.openedFiles.size(); i++){
                if(Values.openedFiles.get(i).fileName.equals(psiJavaFile.getName())){
                    metrics = new FileMetrics(Values.openedFiles.get(i));
                    exists = true;
                    break;
                }
            }
            if(!exists) {
                try {
                    metrics = new FileMetrics(Values.editor, psiJavaFile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Values.openedFiles.add(metrics);
            }
        }

        if(metrics != null) {
            Values.before = new FileMetrics(metrics);
            Values.currentFile = Values.before;
        }

        if(SelectedRefactorings.selectedRefactoring == null && SelectedRefactorings.selectedRefactorings.size() == 0) {
            utils.includeAllRefactorings();
        }

        createThreads(psiJavaFile);
    }


    public void createThreads(PsiJavaFile psiJavaFile){
        System.out.print("\n*** Creating threads ***\n");
        Values.extractMethod = new ArrayList<>();
        Values.extractVariable = new ArrayList<>();
        Values.extractClass = new ArrayList<>();
        Values.moveMethod = new ArrayList<>();
        Values.introduceParam = new ArrayList<>();
        Values.stringComp = new ArrayList<>();
        Values.inheritanceDelegation = new ArrayList<>();
        int size = SelectedRefactorings.selectedRefactorings.size();

        ExecutorService service = Executors.newFixedThreadPool(size);
        CountDownLatch latch = new CountDownLatch(size);
        List<Callable<String>> callableTasks = new ArrayList<>();
            for (Refactorings selectedRefactoring : SelectedRefactorings.selectedRefactorings) {
                Callable<String> callableTask = () -> {
                    ApplicationManager.getApplication().executeOnPooledThread(() -> {
                        ApplicationManager.getApplication().runReadAction(() -> {
                            if (selectedRefactoring == Refactorings.ExtractMethod) {
                                ExtractMethod extractMethod = new ExtractMethod(Values.editor, psiJavaFile);
                                extractMethod.run();
                            } else if (selectedRefactoring == Refactorings.ExtractVariable) {
                                ExtractVariable extractVariable = new ExtractVariable(psiJavaFile, Values.editor);
                                extractVariable.run();
                            } else if (selectedRefactoring == Refactorings.ExtractClass) {
                                ExtractClass extractClass = new ExtractClass(psiJavaFile, Values.editor);
                                extractClass.run();
                            } else if (selectedRefactoring == Refactorings.MoveMethod) {
                                MoveMethod moveMethod = new MoveMethod(Values.editor, psiJavaFile);
                                moveMethod.run();
                            } else if (selectedRefactoring == Refactorings.IntroduceParamObj) {
                                IntroduceParameterObject introParam = new IntroduceParameterObject(Values.editor, psiJavaFile);
                                introParam.run();
                            } else if (selectedRefactoring == Refactorings.InheritanceToDelegation) {
                                InheritanceToDelegation refusedBequest = new InheritanceToDelegation(Values.editor, psiJavaFile);
                                refusedBequest.run();
                            } else if (selectedRefactoring == Refactorings.StringComparison) {
                                StringComparison stringComp = new StringComparison(psiJavaFile, Values.editor);
                                stringComp.run();
                            }
                        });
                    });
                    /*if(!Values.metricsFile.containsKey(psiJavaFile.getName())){
                        ArrayList<Object> loc = new ArrayList<>();
                        ArrayList<Object> cc = new ArrayList<>();
                        ArrayList<Object> cog = new ArrayList<>();
                        ArrayList<Object> lcom = new ArrayList<>();
                        ArrayList<Object> volume = new ArrayList<>();
                        ArrayList<Object> effort = new ArrayList<>();
                        ArrayList<Object> difficulty = new ArrayList<>();
                        ArrayList<Object> maintainability = new ArrayList<>();
                        loc.add(Values.before.numberOfCodeLines);
                        cc.add(Values.before.complexity);
                        cog.add(Values.before.cognitiveComplexity);
                        lcom.add(Values.before.lackOfCohesion);
                        volume.add(Values.before.halsteadVolume);
                        effort.add(Values.before.halsteadEffort);
                        difficulty.add(Values.before.halsteadDifficulty);
                        maintainability.add(Values.before.halsteadMaintainability);
                        ArrayList<ArrayList<Object>> arrays = new ArrayList<>();
                        arrays.add(loc);
                        arrays.add(cc);
                        arrays.add(cog);
                        arrays.add(lcom);
                        arrays.add(volume);
                        arrays.add(effort);
                        arrays.add(difficulty);
                        arrays.add(maintainability);
                        System.out.println("Java file:" + psiJavaFile.getName());
                        Values.metricsFile.put(psiJavaFile.getName(), arrays);
                    }*/
                    return "DONE";
                };
                callableTasks.add(callableTask);
            }


        try {
            System.setOut(new PrintStream(System.out) {
                public void println(String s) {
                    super.println(s);
                    if(Values.isActive) {
                        if (s.contains("Candidates:")) {
                            latch.countDown();
                            ApplicationManager.getApplication().invokeLater(() -> {
                                continueAnalysis(psiJavaFile, latch);
                            });
                        }
                        if (Values.isRefactoring && s.contains("Done!!!")) {
                            Values.isRefactoring = false;
                            saveMetrics(Values.editor.getProject());
                        }
                    }
                }
            });
            service.invokeAll(callableTasks);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void continueAnalysis(PsiJavaFile psiJavaFile, CountDownLatch latch){
        if (ThresholdsCandidates.maxNumberRefactorings != Integer.MAX_VALUE){
            Values.extractMethod = Values.extractMethod.stream().limit(ThresholdsCandidates.maxNumberRefactorings).collect(Collectors.toList());
            Values.extractVariable = Values.extractVariable.stream().limit(ThresholdsCandidates.maxNumberRefactorings).collect(Collectors.toList());
            Values.extractClass = Values.extractClass.stream().limit(ThresholdsCandidates.maxNumberRefactorings).collect(Collectors.toList());
            Values.moveMethod = Values.moveMethod.stream().limit(ThresholdsCandidates.maxNumberRefactorings).collect(Collectors.toList());
            Values.introduceParam = Values.introduceParam.stream().limit(ThresholdsCandidates.maxNumberRefactorings).collect(Collectors.toList());
            Values.stringComp = Values.stringComp.stream().limit(ThresholdsCandidates.maxNumberRefactorings).collect(Collectors.toList());
            Values.inheritanceDelegation = Values.inheritanceDelegation.stream().limit(ThresholdsCandidates.maxNumberRefactorings).collect(Collectors.toList());
        }

        long end = System.nanoTime();
        long elapsedTime = end - this.start;
        System.out.print("\nTime: " + elapsedTime +"\n");

        Values.candidates = getSeverities();

        if(Values.candidates.size() == 0 && latch.getCount() == 0){
            ApplicationManager.getApplication().invokeLater(() -> {
                JBPopupFactory factory = JBPopupFactory.getInstance();

                BalloonBuilder builder =
                        factory.createHtmlTextBalloonBuilder("<b>No refactoring candidates found...</b>", MessageType.INFO, null);
                builder.setFadeoutTime(0);
                builder.setAnimationCycle(0);
                builder.setShadow(false);
                builder.setHideOnClickOutside(true);
                builder.setHideOnKeyOutside(false);
                builder.setHideOnAction(false);
                builder.setCloseButtonEnabled(true);

                Balloon b = builder.createBalloon();

                b.show(RelativePoint.getNorthEastOf(WindowManager.getInstance().getStatusBar(Values.editor.getProject()).getComponent()), Balloon.Position.below);
                MarkupModel markupModel = Values.editor.getMarkupModel();
                markupModel.removeAllHighlighters();
            });

        }
        else{
            utils.checkOpenFile(psiJavaFile);
            utils.displayRefactorings(psiJavaFile);
        }
    }

    public ArrayList<Severity> getSeverities() {
        ArrayList<List<Object>> candidates = new ArrayList<>();
            if(Values.extractMethod.size() > 0) {
                List<Object> obj2 = new ArrayList<>(Values.extractMethod);
                candidates.add(obj2);
            }
            if(Values.extractClass.size() > 0) {
                List<Object> obj3 = new ArrayList<>(Values.extractClass);
                candidates.add(obj3);
            }

            if(Values.extractVariable.size() > 0) {
                List<Object> obj = new ArrayList<>(Values.extractVariable);
                candidates.add(obj);
            }

            if(Values.moveMethod.size() > 0) {
                List<Object> obj4 = new ArrayList<>(Values.moveMethod);
                candidates.add(obj4);
            }

            if(Values.introduceParam.size() > 0) {
                List<Object> obj5 = new ArrayList<>(Values.introduceParam);
                candidates.add(obj5);
            }

            if(Values.stringComp.size() > 0) {
                List<Object> obj6 = new ArrayList<>(Values.stringComp);
                candidates.add(obj6);
            }

            if(Values.inheritanceDelegation.size() > 0) {
                List<Object> obj8 = new ArrayList<>(Values.inheritanceDelegation);
                candidates.add(obj8);
            }
        return this.calculateSeverities(candidates);
    }

    public ArrayList<Severity> calculateSeverities(ArrayList<List<Object>> candidates) {
        severities = new ArrayList<>();
        if(candidates.size() > 1){
            for (List<Object> candidate : candidates) {
                severities.addAll(calculateSeveritiesByType(candidate));
            }
        }
        else if(candidates.size() == 1){
            severities.addAll(calculateSeveritiesByType(candidates.get(0)));
        }

        return severities;
    }

    public ArrayList<Severity> calculateSeveritiesByType(List<Object> candidates){
        ArrayList<Integer> aux = new ArrayList<>();
        ArrayList<Severity> auxSeverity = new ArrayList<>();
        for (int i = (candidates.size() - 1); i >= 0; i--)
            aux.add(i);

        if(aux.size() == 1){
           auxSeverity.add(new Severity(candidates.get(0), Values.numColors, 1));
        }
        else {
            for (int i = 0; i < aux.size(); i++) {
                int scaledMax = Collections.max(aux);
                int scaledMin = Collections.min(aux);
                double severity = normalize(aux.get(i), scaledMin, scaledMax, 1, Values.numColors);
                auxSeverity.add(new Severity(candidates.get(i), severity, 0));
            }
            if (auxSeverity.size() > Values.numColors) {
                int numPerColor = (int) Math.ceil((double)auxSeverity.size() / Values.numColors);
                int i = 1;
                int numTries = 0;

                for (Severity severity : auxSeverity) {
                    if (numTries < numPerColor) {
                        severity.indexColorGutter = i;
                        numTries++;
                    }

                    if (numTries == numPerColor) {
                        numTries = 0;
                        i++;
                    }
                }
            } else {
                for (Severity severity : auxSeverity) {
                    severity.indexColorGutter = auxSeverity.indexOf(severity) + 1;
                }
            }
        }

        return auxSeverity;
    }

    public double normalize(double value, int min, int max, int a, int b) {
        return (b - a) * ((value - min) / (max - min)) + a;
    }

    private void saveMetrics(Project project){
        Values.refactoringCounter++;
        for (RangeHighlighter rangeHighlighter : Values.gutters) {
            rangeHighlighter.setGutterIconRenderer(null);
        }

        PsiDocumentManager documentManager = PsiDocumentManager.getInstance(Objects.requireNonNull(project));
        if(FileEditorManager.getInstance(project).getSelectedEditor() != null){
            Values.editor = ((TextEditor) FileEditorManager.getInstance(project).getSelectedEditor()).getEditor();
            PsiFile psiFile = documentManager.getCachedPsiFile(Values.editor.getDocument());

            if (psiFile instanceof PsiJavaFile) {
                PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
                UtilitiesOverall utils = new UtilitiesOverall();

                if (Values.db != null) {
                    String endPoint = Values.projectName + "/" + Values.username + "/" + Values.token + "/Refactoring/" + utils.removeExtension(psiJavaFile.getName()) +
                            "/Refactoring " + Values.refactoringCounter + "/" + Values.lastRefactoring.type;

                    Instant now = Instant.now();

                    if (Values.lastRefactoring != null) {
                        Values.allRefactorings.add(Values.lastRefactoring);
                        Values.afterActivated = true;
                        Values.afterRefactoring = true;
                        Duration timeElapsed = Duration.between(Values.startTime, now);
                        if (Values.betweenRefactorings == null)
                            Values.betweenRefactorings = now;
                        Duration betweenTime = Duration.between(Values.betweenRefactorings, now);

                        HashMap<String, Object> time = new HashMap<>();
                        time.put("Seconds", timeElapsed.getSeconds());
                        Values.db.child(endPoint + "/timeElapsed").setValueAsync(time);
                        HashMap<String, Object> timeBetween = new HashMap<>();
                        time.put("Seconds", betweenTime.getSeconds());
                        Values.db.child(endPoint + "/timeBetween").setValueAsync(timeBetween);
                        HashMap<String, Object> dateHour = new HashMap<>();
                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        LocalDateTime dateHourNow = LocalDateTime.now();
                        dateHour.put("Moment", dtf.format(dateHourNow));
                        Values.db.child(endPoint + "/dateHour").setValueAsync(dateHour);
                        Values.betweenRefactorings = now;

                        HashMap<String, Object> projectInfo = new HashMap<>();
                        projectInfo.put("URL", Values.projectURL);
                        Values.db.child(endPoint + "/Git").setValueAsync(projectInfo);

                        endPoint += "/metrics";

                        MetricsUtils metricsUtils = new MetricsUtils();
                        if (Values.lastRefactoring.type.equals("Extract Method")) {
                            HashMap<String, Object> itemsFileBefore = metricsUtils.getValuesMetricsFile(Values.before);
                            Values.db.child(endPoint + "/FileBefore").setValueAsync(itemsFileBefore);
                            HashMap<String, Object> itemsBefore = metricsUtils.getValuesMetrics(Values.before);
                            Values.db.child(endPoint + "/before").setValueAsync(itemsBefore);
                            try {
                                changeMetrics(Values.lastRefactoring, psiJavaFile);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            HashMap<String, Object> itemsAfter = metricsUtils.getValuesMetrics(Values.after);
                            Values.db.child(endPoint + "/after").setValueAsync(itemsAfter);
                            HashMap<String, Object> itemsFileAfter = metricsUtils.getValuesMetricsFile(Values.after);
                            Values.db.child(endPoint + "/FileAfter").setValueAsync(itemsFileAfter);
                        }
                        else if (Values.lastRefactoring.type.equals("Extract Variable") || Values.lastRefactoring.type.equals("Introduce Parameter Object") || Values.lastRefactoring.type.equals("String Comparison")) {
                            HashMap<String, Object> itemsFileBefore = metricsUtils.getValuesMetricsFile(Values.before);
                            Values.db.child(endPoint + "/FileBefore").setValueAsync(itemsFileBefore);
                            HashMap<String, Object> itemsBefore = metricsUtils.getValuesMetrics(Values.before);
                            Values.db.child(endPoint + "/before").setValueAsync(itemsBefore);
                            try {
                                changeMetrics(Values.lastRefactoring, psiJavaFile);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            HashMap<String, Object> itemsAfter = metricsUtils.getValuesMetrics(Values.after);
                            Values.db.child(endPoint + "/after").setValueAsync(itemsAfter);
                            HashMap<String, Object> itemsFileAfter = metricsUtils.getValuesMetricsFile(Values.after);
                            Values.db.child(endPoint + "/FileAfter").setValueAsync(itemsFileAfter);
                        }
                        else if (Values.lastRefactoring.type.equals("Extract Class") || Values.lastRefactoring.type.equals("Move Method") || Values.lastRefactoring.type.equals("Inheritance To Delegation")) {
                            HashMap<String, Object> itemsClassBefore = metricsUtils.getValuesMetricsOldClass(Values.lastRefactoring.metrics);
                            Values.db.child(endPoint + "/ClassBefore").setValueAsync(itemsClassBefore);
                            HashMap<String, Object> itemsFileBefore = metricsUtils.getValuesMetricsFile(Values.before);
                            Values.db.child(endPoint + "/FileBefore").setValueAsync(itemsFileBefore);
                            try {
                                changeMetrics(Values.lastRefactoring, psiJavaFile);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }

                            HashMap<String, Object> itemsClassAfter = metricsUtils.getValuesMetricsOldClass(Values.after);
                            Values.db.child(endPoint + "/ClassAfter").setValueAsync(itemsClassAfter);
                            HashMap<String, Object> itemsFileAfter = metricsUtils.getValuesMetricsFile(Values.after);
                            Values.db.child(endPoint + "/FileAfter").setValueAsync(itemsFileAfter);
                        }
                    } else {
                        try {
                            Values.after = new FileMetrics(Values.editor, psiJavaFile);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        Values.before = new FileMetrics(Values.after);
                    }

                    /*if (Values.metricsFile.containsKey(psiJavaFile.getName())) {
                        ArrayList<ArrayList<Object>> arrayMetrics = Values.metricsFile.get(psiJavaFile.getName());
                        arrayMetrics.get(0).add(Values.after.numberOfCodeLines);
                        arrayMetrics.get(1).add(Values.after.complexity);
                        arrayMetrics.get(2).add(Values.after.cognitiveComplexity);
                        arrayMetrics.get(3).add(Values.after.lackOfCohesion);
                        arrayMetrics.get(4).add(Values.after.halsteadVolume);
                        arrayMetrics.get(5).add(Values.after.halsteadEffort);
                        arrayMetrics.get(6).add(Values.after.halsteadDifficulty);
                        arrayMetrics.get(7).add(Values.after.halsteadMaintainability);
                    }*/

                    boolean exists = false;
                    for (int i = 0; i < Values.openedFiles.size(); i++) {
                        if (Values.openedFiles.get(i).fileName.equals(Values.after.fileName)) {
                            exists = true;
                            Values.openedFiles.set(i, Values.after);
                            break;
                        }
                    }
                    if (!exists)
                        Values.openedFiles.add(Values.after);

                    Values.endRefactoring = now;
                }

                Values.isFirst = true;
                utils.startActions(psiJavaFile);
            }
        }
    }

    public void changeMetrics(LastRefactoring lastRefactoring, PsiJavaFile newFile) throws Exception {
        Values.after = new FileMetrics(Values.before);
        switch (lastRefactoring.type) {
            case "Extract Method": {
                MethodMetrics oldMethod = null;
                MethodMetrics newMethod = null;


                for (PsiMethod method : PsiTreeUtil.findChildrenOfType(newFile, PsiMethod.class)) {
                    boolean exists = false;
                    for (MethodMetrics methodMetric : Values.after.methodMetrics) {
                        if (method.getName().equals(methodMetric.methodName) &&
                                method.getContainingClass().getName().equals(methodMetric.method.getContainingClass().getName())) {
                            exists = true;
                            if (methodMetric.methodName.equals(lastRefactoring.method.getName())
                                    && methodMetric.method.getContainingClass().getName().equals(lastRefactoring.method.getContainingClass().getName())) {
                                int index = Values.after.methodMetrics.indexOf(methodMetric);
                                int indexClass = Values.after.classMetrics.indexOf(Values.after.getClassMetrics(methodMetric.method.getContainingClass()));
                                oldMethod = new MethodMetrics(methodMetric.method.getContainingClass(), method, false);
                                Values.after.methodMetrics.set(index, oldMethod);
                                Values.after.classMetrics.set(indexClass, new ClassMetrics(method.getContainingClass()));
                                break;
                            }
                        }
                    }

                    if (!exists) {
                        newMethod = new MethodMetrics(method.getContainingClass(), method, false);
                        Values.newMethod = newMethod;
                        Values.after.addMethod(newMethod);
                    }
                }

                Values.after.setMetrics(Values.editor.getDocument());
                break;
            }
            case "Extract Variable":
            case "String Comparison": {
                boolean found = false;
                for (MethodMetrics methodMetric : Values.after.methodMetrics) {
                    if (methodMetric.methodName.equals(lastRefactoring.method.getName())) {
                        for (PsiMethod method : PsiTreeUtil.findChildrenOfType(newFile, PsiMethod.class)) {
                            if (method.getName().equals(methodMetric.methodName) &&
                                    methodMetric.numParameters == lastRefactoring.method.getParameterList().getParametersCount()) {
                                Values.after.methodMetrics.set(Values.after.methodMetrics.indexOf(methodMetric),
                                        new MethodMetrics(Objects.requireNonNull(method.getContainingClass()), method, methodMetric.isConstructor));
                                int indexClass = Values.after.classMetrics.indexOf(Values.after.getClassMetrics(methodMetric.method.getContainingClass()));
                                Values.after.classMetrics.set(indexClass, new ClassMetrics(method.getContainingClass()));
                                found = true;
                                break;
                            }
                        }
                    }

                    if (found)
                        break;
                }
                Values.after.setMetrics(Values.editor.getDocument());
                break;
            }
            case "Introduce Parameter Object": {
                int index = 0;
                for (PsiMethod method : PsiTreeUtil.findChildrenOfType(newFile, PsiMethod.class)) {
                    for (int i = 0; i < Values.after.methodMetrics.size(); i++) {
                        MethodMetrics methodMetric = Values.after.methodMetrics.get(i);
                        if (method.getName().equals(methodMetric.methodName)) {
                            if (methodMetric.methodName.equals(lastRefactoring.method.getName())
                                    && methodMetric.className == lastRefactoring.method.getContainingClass().getName()) {
                                index = Values.after.methodMetrics.indexOf(methodMetric);
                                newMethod = methodMetric;
                                newMethod.numParameters = 1;
                                newMethod.longParameterList = false;
                                Values.after.methodMetrics.remove(index);
                                Values.after.methodMetrics.add(newMethod);
                                int indexClass = Values.after.classMetrics.indexOf(Values.after.getClassMetrics(methodMetric.method.getContainingClass()));
                                try {
                                    Values.after.classMetrics.set(indexClass, new ClassMetrics(method.getContainingClass()));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                break;
                            }
                        }
                    }
                }

                Values.after.setMetrics(Values.editor.getDocument());
                break;
            }
            case "Extract Class":
            case "Move Method":
            case "Inheritance To Delegation": {
                for (PsiClass psiClass : PsiTreeUtil.findChildrenOfType(newFile, PsiClass.class)) {
                    for (ClassMetrics classMetric : Values.after.classMetrics) {
                        if (Objects.requireNonNull(classMetric.targetClass.getName()).equals(lastRefactoring._class.getName())
                                && Objects.requireNonNull(psiClass.getName()).equals(lastRefactoring._class.getName())) {
                            oldClass = new ClassMetrics(psiClass);
                            Values.after.classMetrics.set(Values.after.classMetrics.indexOf(classMetric), oldClass);
                            break;
                        }
                    }
                }
                Values.after.setMetrics(Values.editor.getDocument());
                break;
            }
        }

        Values.before = new FileMetrics(Values.after);
    }
}
