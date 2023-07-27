package liverefactoring.utils.firebase.plugin.forms;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import liverefactoring.utils.firebase.domain.VisualFire;
import liverefactoring.utils.firebase.plugin.configs.PluginConfigs;

import javax.swing.*;


public class VFContent {
    private JTabbedPane tabPane;
    private JPanel databasePanel;
    private JTree dataTree;
    private JTextField treeSearchInput;
    private JButton setPathButton;
    private JCheckBox realtimeUpdateCheckBox;
    private JButton addButton;
    private JButton refreshButton;
    private JLabel statusText;
    private JPanel contentPanel;
    private JLabel pathLabel;
    private JTextField keyInput;
    private JTextField valueInput;
    private JButton copyJSONButton;
    private JButton button1;
    private JTextField newNodeInput;
    private Project project;
    private boolean pluginConfigured = false;
    private VisualFire app;
    private GUIActionListener guiActionListener;

    public VFContent(Project p, VisualFire app) {
        this.project = p;
        pluginConfigured = PluginConfigs.getInstance(p).getConfigFilePath() != null;
        if (pluginConfigured) {
            pathLabel.setText(PluginConfigs.getInstance(p).getConfigFilePath());
            statusText.setVisible(false);
            dataTree.setVisible(true);
        }

        setPathButton.addActionListener(e -> {
            VirtualFile file = FileChooser.chooseFile(new FileChooserDescriptor(true, false, false, false, false, false), project, null);
            if (file != null) {
                PluginConfigs.getInstance(project).setConfigFilePath(file.getPath());
                app.setUp(file.getPath());
                pathLabel.setText(PluginConfigs.getInstance(p).getConfigFilePath());
                statusText.setVisible(false);
                dataTree.setVisible(true);
                pluginConfigured = true;
                app.load();
            }
        });

        refreshButton.addActionListener(e -> {
            if (app.isInitialized())
                app.load();
        });
    }

    private void createUIComponents() {

    }

    public JTabbedPane getTabPane() {
        return tabPane;
    }

    public void setTabPane(JTabbedPane tabPane) {
        this.tabPane = tabPane;
    }

    public JPanel getDatabasePanel() {
        return databasePanel;
    }

    public void setDatabasePanel(JPanel databasePanel) {
        this.databasePanel = databasePanel;
    }

    public JTree getDataTree() {
        return dataTree;
    }

    public void setDataTree(JTree dataTree) {
        this.dataTree = dataTree;
    }

    public JTextField getTreeSearchInput() {
        return treeSearchInput;
    }

    public void setTreeSearchInput(JTextField treeSearchInput) {
        this.treeSearchInput = treeSearchInput;
    }

    public JButton getSetPathButton() {
        return setPathButton;
    }

    public void setSetPathButton(JButton setPathButton) {
        this.setPathButton = setPathButton;
    }

    public JCheckBox getRealtimeUpdateCheckBox() {
        return realtimeUpdateCheckBox;
    }

    public void setRealtimeUpdateCheckBox(JCheckBox realtimeUpdateCheckBox) {
        this.realtimeUpdateCheckBox = realtimeUpdateCheckBox;
    }

    public JButton getAddButton() {
        return addButton;
    }

    public void setAddButton(JButton addButton) {
        this.addButton = addButton;
    }

    public JButton getRefreshButton() {
        return refreshButton;
    }

    public void setRefreshButton(JButton refreshButton) {
        this.refreshButton = refreshButton;
    }

    public JLabel getStatusText() {
        return statusText;
    }

    public void setStatusText(JLabel statusText) {
        this.statusText = statusText;
    }

    public JPanel getContentPanel() {
        return contentPanel;
    }

    public void setContentPanel(JPanel contentPanel) {
        this.contentPanel = contentPanel;
    }

    public JLabel getPathLabel() {
        return pathLabel;
    }

    public void setPathLabel(JLabel pathLabel) {
        this.pathLabel = pathLabel;
    }

    public JTextField getKeyInput() {
        return keyInput;
    }

    public void setKeyInput(JTextField keyInput) {
        this.keyInput = keyInput;
    }

    public JTextField getValueInput() {
        return valueInput;
    }

    public void setValueInput(JTextField valueInput) {
        this.valueInput = valueInput;
    }

    public JTextField getNewNodeInput() {
        return newNodeInput;
    }

    public void setNewNodeInput(JTextField newNodeInput) {
        this.newNodeInput = newNodeInput;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public boolean isPluginConfigured() {
        return pluginConfigured;
    }

    public void setPluginConfigured(boolean pluginConfigured) {
        this.pluginConfigured = pluginConfigured;
    }

    public VisualFire getApp() {
        return app;
    }

    public void setApp(VisualFire app) {
        this.app = app;
    }

    public GUIActionListener getGuiActionListener() {
        return guiActionListener;
    }

    public void setGuiActionListener(GUIActionListener guiActionListener) {
        this.guiActionListener = guiActionListener;

        addButton.addActionListener((e) -> {
                    if (keyInput.getText().length() > 0 && valueInput.getText().length() > 0)
                        guiActionListener.onAddNode(keyInput.getText(), valueInput.getText());
                }
        );

        copyJSONButton.addActionListener((e) -> {
            this.guiActionListener.copyNodeAsJsonToClipboard();
        });
    }

    public interface GUIActionListener {
        void onAddNode(String key, String value);

        void onDeleteNode(String path);

        void copyNodeAsJsonToClipboard();
    }
}
