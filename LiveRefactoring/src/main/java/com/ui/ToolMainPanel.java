package com.ui;

import com.intellij.openapi.ui.SimpleToolWindowPanel;

public class ToolMainPanel extends SimpleToolWindowPanel {
    public ToolMainPanel(boolean vertical) {
        super(vertical);
    }
    /*public JSplitPane pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    public JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    public JPanel mainPanel = new JPanel();
    public Utilities utils = new Utilities();
    public boolean alreadyConfig = false;
    public int clicked = 0;
    public Severity currentCandidate = null;

    JRadioButton selectExtractMethod = new JRadioButton();
    JRadioButton selectExtractVariable = new JRadioButton();
    JRadioButton selectExtractClass = new JRadioButton();
    JRadioButton selectAll = new JRadioButton();

    JLabel label_minNumExtractedMethods = new JLabel("Min. Num. Methods");
    JBTextField textField_minNumExtractedMethods = new JBTextField();
    JLabel label_maxOrigMethodPercentage = new JLabel("Max. Percentage Original Methods");
    JBTextField textField_maxOrigMethodPercentage = new JBTextField();
    JLabel label_minNumStatements = new JLabel("Min. Num. Statements");
    JBTextField textField_minNumStatements = new JBTextField();
    JLabel label_minLengthExtraction = new JLabel("Min. Length of Expressions");
    JBTextField textField_minLengthExtraction = new JBTextField();
    JLabel label_colorBlind = new JLabel("Are you color blinded?");
    JRadioButton selectedColorBlindYes = new JRadioButton();
    JRadioButton selectedColorBlindNo = new JRadioButton();

    JLabel label_numRefactorings = new JLabel("Num. Refactorings to show");
    JBTextField textField_numRefactorings = new JBTextField();

    String url = utils.getURL() + "images/icons/";
    Image startImage = new ImageIcon(url + "start.png").getImage();
    Image configImage = new ImageIcon(url + "config.png").getImage();
    Image stopImage = new ImageIcon(url + "stop.png").getImage();

    public boolean started = false;
    private final String urlFirebase = this.utils.getURL() + "firebaseConfig/ServiceAccount.json";
    private DatabaseReference database = null;
    private boolean done = false;

    public ArrayList<Severity> candidates = new ArrayList<>();

    public ToolMainPanel(ToolWindow toolWindow, Project project) {
        super(true, false);
        final Content content = ContentFactory.SERVICE.getInstance().createContent(this, "", true);

        Image newIconStart = startImage.getScaledInstance( 15, 15,  java.awt.Image.SCALE_SMOOTH ) ;
        ImageIcon iconStart = new ImageIcon( newIconStart );
        Image newIconConfig = configImage.getScaledInstance( 15, 15,  java.awt.Image.SCALE_SMOOTH ) ;
        ImageIcon iconConfig = new ImageIcon( newIconConfig );
        Image newIconStop = stopImage.getScaledInstance( 15, 15,  java.awt.Image.SCALE_SMOOTH ) ;
        ImageIcon iconStop = new ImageIcon( newIconStop );

        JButton start = new JButton(iconStart);
        start.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        start.setContentAreaFilled(false);
        JButton config = new JButton(iconConfig);
        config.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        config.setContentAreaFilled(false);

        start.setPreferredSize(new Dimension(25, 25));
        config.setPreferredSize(new Dimension(25, 25));

        start.addActionListener(e -> {
            mainPanel.removeAll();
            mainPanel.revalidate();
            mainPanel.repaint();

            if(!started) {
                started = true;
                start.setIcon(iconStop);
                if(alreadyConfig)
                    saveValues(project);
                activateTool();
                JPanel refactorings = changeRefactoringsPanel(((TextEditor) Objects.requireNonNull(FileEditorManager.getInstance(project).getSelectedEditor())).getEditor());
                mainPanel.add(refactorings);
            }
            else {
                started = false;
                start.setIcon(iconStart);
                deactivateTool();
            }
        });

        config.addActionListener(e -> {
            alreadyConfig = true;
            setConfigureMenu();
        });

        panelButtons.add(start, BorderLayout.EAST);
        panelButtons.add(config, BorderLayout.EAST);

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBounds(50, 30, 300, 50);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        pane.setTopComponent(panelButtons);
        pane.setBottomComponent(scrollPane);
        pane.setDividerLocation(1.0);
        this.add(pane);
        toolWindow.getContentManager().addContent(content);
    }

    public void saveValues(Project project){
        SelectedRefactorings.selectedRefactoring = null;
        SelectedRefactorings.selectedRefactorings.clear();

        if (selectAll.isSelected()) {
            SelectedRefactorings.selectedRefactoring = Refactorings.All;
        } else {
            if (selectExtractMethod.isSelected()) {
                SelectedRefactorings.selectedRefactorings.add(Refactorings.ExtractMethod);
            }
            if (selectExtractClass.isSelected()) {
                SelectedRefactorings.selectedRefactorings.add(Refactorings.ExtractClass);
            }
            if (selectExtractVariable.isSelected()) {
                SelectedRefactorings.selectedRefactorings.add(Refactorings.ExtractVariable);
            }
        }

        boolean colorBlind = selectedColorBlindYes.isSelected();

        int minNumExtractedMethods, maxOrigMethodPercentage, minNumStatements, minLengthExtraction;
        minNumExtractedMethods = Integer.parseInt(textField_minNumExtractedMethods.getText());
        maxOrigMethodPercentage = Integer.parseInt(textField_maxOrigMethodPercentage.getText());
        minNumStatements = Integer.parseInt(textField_minNumStatements.getText());
        minLengthExtraction = Integer.parseInt(textField_minLengthExtraction.getText());

        if (minNumExtractedMethods < 2)
            minNumExtractedMethods = 2;

        if (maxOrigMethodPercentage >= 100)
            maxOrigMethodPercentage = 100;

        if (minNumStatements < 1)
            minNumStatements = 1;

        if (minLengthExtraction < 1)
            minLengthExtraction = 1;

        ThresholdsCandidates thresholds = new ThresholdsCandidates(minNumExtractedMethods,
                maxOrigMethodPercentage, minNumStatements, minLengthExtraction, "username", colorBlind);

        Values.numRefactorings = Integer.parseInt(textField_numRefactorings.getText());

        if(Values.isActive){
            for (RangeHighlighter rangeHighlighter : Values.gutters) {
                rangeHighlighter.setGutterIconRenderer(null);
            }

            Editor editor = ((TextEditor) Objects.requireNonNull(FileEditorManager.getInstance(project).getSelectedEditor())).getEditor();
            PsiDocumentManager documentManager = PsiDocumentManager.getInstance(project);
            Utilities utils = new Utilities();
            Values.editor = editor;
            final PsiFile psiFile = documentManager.getCachedPsiFile(Values.editor.getDocument());
            final PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
            utils.startActions(psiJavaFile);
            this.candidates = Values.candidates;
        }
    }

    public JPanel changeRefactoringsPanel(Editor editor){
        JPanel panel = new JPanel(new GridLayout(this.candidates.size(),1));
        panel.setPreferredSize(new Dimension(pane.getWidth(), this.candidates.size()*20));

        ImageIcon icon = null;
        for (Severity candidate : this.candidates) {
            JLabel label = new JLabel();
            String text = "";
            ArrayList<MyFocus> ranges = new ArrayList<>();

            if(candidate.candidate instanceof ExtractMethodCandidate){
                ExtractMethodCandidate EM = (ExtractMethodCandidate) candidate.candidate;
                icon = getImageIconByType(candidate.indexColorGutter, "EM");
                text = "Extract Method (" + EM.nodes.size() + " statements)";
                currentCandidate = candidate;

                LogicalPosition start = EM.range.start;
                LogicalPosition end = EM.range.end;
                int startOffSet = EM.nodes.get(0).getTextRange().getStartOffset();
                int endOffSet = EM.nodes.get(EM.nodes.size() - 1).getTextRange().getEndOffset();
                ranges.add(new MyFocus(start, end, startOffSet, endOffSet));
            }
            else if(candidate.candidate instanceof ExtractClassCandidate){
                icon = getImageIconByType(candidate.indexColorGutter, "EC");
                text = "Extract Class (" + ((ExtractClassCandidate) candidate.candidate).targetEntities.size() + " elements)";
                currentCandidate = candidate;
                for (PsiElement targetEntity : ((ExtractClassCandidate) candidate.candidate).targetEntities) {
                    int startOffSet = targetEntity.getTextRange().getStartOffset();
                    int endOffSet = targetEntity.getTextRange().getEndOffset();
                    LogicalPosition start = editor.offsetToLogicalPosition(startOffSet);
                    LogicalPosition end = editor.offsetToLogicalPosition(endOffSet);
                    ranges.add(new MyFocus(start, end, startOffSet, endOffSet));
                }
            }
            else if(candidate.candidate instanceof ExtractVariableCandidate){
                ExtractVariableCandidate EV = ((ExtractVariableCandidate) candidate.candidate);
                currentCandidate = candidate;
                icon = getImageIconByType(candidate.indexColorGutter, "EV");
                text = "Extract Variable (" + EV.length + " characters)";

                LogicalPosition start = EV.range.start;
                LogicalPosition end = EV.range.end;
                int startOffSet = EV.node.getTextRange().getStartOffset();
                int endOffSet = EV.node.getTextRange().getEndOffset();
                ranges.add(new MyFocus(start, end, startOffSet, endOffSet));
            }

            label.setText(text);
            label.setIcon(icon);
            label.addMouseListener(new MouseAdapter()
            {
                public void mouseClicked(MouseEvent e)
                {
                for (MyFocus range : ranges) {
                    CaretModel caretModel = editor.getCaretModel();
                    caretModel.moveToLogicalPosition(range.start);
                    ScrollingModel scrollingModel = editor.getScrollingModel();
                    scrollingModel.scrollToCaret(ScrollType.CENTER);
                    if(clicked == 0)
                        clicked++;
                    else if(clicked == 1){
                        clicked = 0;
                        if(currentCandidate.candidate instanceof ExtractMethodCandidate){
                            ExtractMethod extractMethod = new ExtractMethod(editor);
                            extractMethod.extractMethod((ExtractMethodCandidate) currentCandidate.candidate);
                        }
                        else if(currentCandidate.candidate instanceof ExtractClassCandidate){
                            ExtractClass extractClass = new ExtractClass();
                            extractClass.extractClass((ExtractClassCandidate) currentCandidate.candidate);
                        }
                        else if(currentCandidate.candidate instanceof ExtractVariableCandidate){
                            ExtractVariable extractVariable = new ExtractVariable(editor);
                            extractVariable.extractVariable((ExtractVariableCandidate) currentCandidate.candidate);
                        }
                    }
                }
                }
            });

            panel.add(label);
        }

        return panel;
    }

    public ImageIcon getImageIconByType(int index, String type){
        ImageIcon icon = null;
        try {
            icon = utils.getIconCircle(index, type);
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert icon != null;
        Image img = icon.getImage();
        Image resizedImage = img.getScaledInstance(10, 10, java.awt.Image.SCALE_SMOOTH);

        icon = new ImageIcon(resizedImage);

        return icon;
    }

    public void deactivateTool(){
        for (RangeHighlighter rangeHighlighter : Values.gutters) {
                rangeHighlighter.setGutterIconRenderer(null);
        }
        Values.isActive = false;
    }

    public void activateTool(){
        Project project = getActiveProject();
        Editor editor = ((TextEditor) Objects.requireNonNull(FileEditorManager.getInstance(project).getSelectedEditor())).getEditor();

        if(!done){
            this.activateListeners(editor, project);
            done = true;
        }

        PsiDocumentManager documentManager = PsiDocumentManager.getInstance(project);
        AllMetrics allMetrics = new AllMetrics(project.getName());
        Values.isActive = true;
        Values.editor = editor;
        Values.metrics = allMetrics;
        final PsiFile psiFile = documentManager.getCachedPsiFile(Values.editor.getDocument());
        final PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
        utils.startActions(psiJavaFile);
        this.candidates = Values.candidates;
    }

    public void activateListeners(Editor editor, Project project){
        MessageBus messageBus = Objects.requireNonNull(editor.getProject()).getMessageBus();
        messageBus.connect().subscribe(VirtualFileManager.VFS_CHANGES, new BulkFileListener() {
            public FileMetrics before = null;
            public FileMetrics after = null;
            public PsiJavaFile lastFile = null;

            @Override
            public void before(@NotNull java.util.List<? extends VFileEvent> events) {
                if (project != null && started) {
                    System.out.println("=========== New Event (Before) ===========");
                    for (RangeHighlighter rangeHighlighter : Values.gutters) {
                        rangeHighlighter.setGutterIconRenderer(null);
                    }
                    VirtualFile file = events.get(0).getFile();
                    if (file != null) {
                        PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
                        if (psiFile instanceof PsiJavaFile) {
                            PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
                            if (lastFile == null)
                                lastFile = psiJavaFile;

                            if (lastFile.getName().equals(psiJavaFile.getName())) {
                                    if (Values.lastRefactoring != null) {

                                        if (before != null)
                                            before = after;
                                        else
                                            before = new FileMetrics(((TextEditor) Objects.requireNonNull(FileEditorManager.getInstance(project).getSelectedEditor())).getEditor(), psiJavaFile);

                                        Values.metrics.addMetrics(before);

                                }
                            } else {
                                lastFile = psiJavaFile;
                            }
                        }
                    }
                }
            }

            @Override
            public void after(@NotNull List<? extends VFileEvent> events) {
                if (project != null && started) {
                    System.out.println("=========== New Event (After) ===========");
                    for (RangeHighlighter rangeHighlighter : Values.gutters) {
                        rangeHighlighter.setGutterIconRenderer(null);
                    }
                    VirtualFile file = events.get(0).getFile();
                    if (file != null) {
                        PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
                        if (psiFile instanceof PsiJavaFile) {
                            PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
                            Utilities utils = new Utilities();
                            if (utils.isPsiFileInProject(project, psiFile)) {
                                Values.editor = ((TextEditor) Objects.requireNonNull(FileEditorManager.getInstance(project).getSelectedEditor())).getEditor();
                                utils.startActions(psiJavaFile);
                                candidates = Values.candidates;


                                    if (Values.lastRefactoring != null) {
                                        System.out.println("A refactoring was applied!!!");
                                        //after = before.changeMetrics(Values.lastRefactoring, psiJavaFile);
                                        Values.metrics.addMetrics(after);
                                        Values.lastRefactoring = null;
                                    }
                            }
                        }
                    }
                }
            }
        });

        messageBus.connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerAdapter() {
            @Override
            public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
                System.out.println("---------------File Changed---------------");
                if (started) {
                    PsiDocumentManager manager = PsiDocumentManager.getInstance(source.getProject());
                    Editor selectedEditor = ((TextEditor) Objects.requireNonNull(FileEditorManager.getInstance(source.getProject()).getSelectedEditor())).getEditor();
                    Values.editor = selectedEditor;
                    final PsiFile psiFile = manager.getCachedPsiFile(selectedEditor.getDocument());
                    final PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
                    utils.startActions(psiJavaFile);
                    candidates = Values.candidates;
                }
            }

            @Override
            public void selectionChanged(@NotNull FileEditorManagerEvent event) {
                System.out.println("---------------File Changed---------------");
                if (started) {
                    PsiDocumentManager manager = PsiDocumentManager.getInstance(event.getManager().getProject());
                    Editor selectedEditor = ((TextEditor) Objects.requireNonNull(FileEditorManager.getInstance(event.getManager().getProject()).getSelectedEditor())).getEditor();
                    Values.editor = selectedEditor;
                    final PsiFile psiFile = manager.getCachedPsiFile(selectedEditor.getDocument());
                    final PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
                    utils.startActions(psiJavaFile);
                    candidates = Values.candidates;
                }
            }
        });
    }

    public void setConfigureMenu(){
        mainPanel.removeAll();
        mainPanel.revalidate();
        mainPanel.repaint();

        Box boxWithExecutors = Box.createVerticalBox();
        boxWithExecutors.setPreferredSize(new Dimension(pane.getWidth(), pane.getHeight()));

        mainPanel.setPreferredSize(new Dimension(pane.getWidth(), pane.getHeight()));

        JPanel panel = new JPanel(new GridLayout(5,2));
        textField_minNumExtractedMethods.setText(Integer.toString(ThresholdsCandidates.minNumExtractedMethods));
        textField_maxOrigMethodPercentage.setText(Integer.toString(ThresholdsCandidates.maxOrigMethodPercentage));
        textField_minNumStatements.setText(Integer.toString(ThresholdsCandidates.minNumStatements));
        textField_minLengthExtraction.setText(Integer.toString(ThresholdsCandidates.minLengthExtraction));

        JPanel panel1 = new JPanel(new GridLayout(1,2));
        panel1.add(label_minNumExtractedMethods, BorderLayout.WEST);
        panel1.add(textField_minNumExtractedMethods, BorderLayout.WEST);
        panel1.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Extract Class"));

        int width = pane.getWidth();
        int height = 60;

        JPanel panel2 = new JPanel(new GridLayout(2,2));
        panel2.add(label_maxOrigMethodPercentage, BorderLayout.WEST);
        panel2.add(textField_maxOrigMethodPercentage, BorderLayout.EAST);
        panel2.add(label_minNumStatements, BorderLayout.WEST);
        panel2.add(textField_minNumStatements, BorderLayout.EAST);
        panel2.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Extract Method"));

        JPanel panel3 = new JPanel(new GridLayout(1,2));
        panel3.add(label_minLengthExtraction, BorderLayout.WEST);
        panel3.add(textField_minLengthExtraction, BorderLayout.EAST);
        panel3.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Extract Variable"));

        ChangeListener listener = event -> {
            JRadioButton source = (JRadioButton) event.getSource();
            panel1.setVisible(false);
            panel2.setVisible(false);
            panel3.setVisible(false);

            if(selectAll.isSelected()){
                if(!source.equals(selectAll)){
                    selectAll.setSelected(false);
                }
                else{
                    panel1.setVisible(true);
                    panel2.setVisible(true);
                    panel3.setVisible(true);
                }
            }

            if(selectExtractVariable.isSelected() || selectExtractClass.isSelected() ||
                    selectExtractMethod.isSelected()){
                if(source.equals(selectAll)){
                    selectAll.setSelected(true);
                    selectExtractClass.setSelected(false);
                    selectExtractMethod.setSelected(false);
                    selectExtractVariable.setSelected(false);
                }
            }

            if(source.getText().equals("Extract Method") || selectExtractMethod.isSelected()){
                panel2.setVisible(true);
            }
            if(source.getText().equals("Extract Class") || selectExtractClass.isSelected()){
                panel1.setVisible(true);
            }
            if(source.getText().equals("Extract Variable") || selectExtractVariable.isSelected()){
                panel3.setVisible(true);
            }
            if(source.getText().equals("All Refactorings")){
                panel1.setVisible(true);
                panel2.setVisible(true);
                panel3.setVisible(true);
            }
        };

        selectExtractMethod.addChangeListener(listener);
        selectExtractClass.addChangeListener(listener);
        selectExtractVariable.addChangeListener(listener);
        selectAll.addChangeListener(listener);

        if(SelectedRefactorings.selectedRefactorings.size() > 0)
            for (Refactorings selectedRefactoring : SelectedRefactorings.selectedRefactorings) {
                if(selectedRefactoring == Refactorings.ExtractClass)
                    selectExtractClass.setSelected(true);
                else if(selectedRefactoring == Refactorings.ExtractMethod) {
                    selectExtractMethod.setSelected(true);
                }
                else if(selectedRefactoring == Refactorings.ExtractVariable)
                    selectExtractVariable.setSelected(true);
            }
        else
            selectAll.setSelected(true);

        selectExtractMethod.setText("Extract Method");
        panel.add(selectExtractMethod, BorderLayout.WEST);

        selectExtractClass.setText("Extract Class");
        panel.add(selectExtractClass, BorderLayout.WEST);

        selectExtractVariable.setText("Extract Variable");
        panel.add(selectExtractVariable, BorderLayout.WEST);

        selectAll.setText("All Refactorings");
        panel.add(selectAll, BorderLayout.WEST);

        textField_numRefactorings.setText("10");
        panel.add(label_numRefactorings, BorderLayout.WEST);
        panel.add(textField_numRefactorings, BorderLayout.EAST);

        //panel.add(verticalBox);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Included Refactorings"));

        JPanel panel4 = new JPanel(new GridLayout(1,2));

        panel4.add(label_colorBlind, BorderLayout.WEST);

        selectedColorBlindYes.setText("Yes");
        selectedColorBlindNo.setText("No");
        selectedColorBlindNo.setSelected(true);

        ChangeListener changeListener = event -> {
            JRadioButton source = (JRadioButton) event.getSource();
            if(source.getText().equals("Yes")){
                selectedColorBlindNo.setSelected(false);
            }
            else if(source.getText().equals("No")){
                selectedColorBlindYes.setSelected(false);
            }
        };

        selectedColorBlindYes.addChangeListener(changeListener);
        selectedColorBlindNo.addChangeListener(changeListener);

        panel4.add(selectedColorBlindYes);
        panel4.add(selectedColorBlindNo);

        panel4.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Further Details"));

        System.out.println(width + " " + height);
        panel.setPreferredSize(new Dimension(width, height*2));
        panel1.setPreferredSize(new Dimension(width, height));
        panel2.setPreferredSize(new Dimension(width, height*2));
        panel3.setPreferredSize(new Dimension(width, height));
        panel4.setPreferredSize(new Dimension(width, height));

        mainPanel.add(panel);
        mainPanel.add(panel1);
        mainPanel.add(panel2);
        mainPanel.add(panel3);
        mainPanel.add(panel4);
    }

    public Project getActiveProject() {
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
}
