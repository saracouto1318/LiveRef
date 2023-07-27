package liverefactoring.utils.firebase.plugin.configs;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Class used to persist certain configs such as the path to the firebase-config.json file
 */
@State(name = "VFFireBasePathConfig",
        storages = {
                @Storage("PathConfigs.xml")
        })
public class PluginConfigs implements PersistentStateComponent<PluginConfigs> {
    public boolean realTimeUpdate;
    String configFilePath;

    public static PluginConfigs getInstance(Project project) {
        return ServiceManager.getService(project, PluginConfigs.class);
    }

    public String getConfigFilePath() {
        return configFilePath;
    }

    public void setConfigFilePath(String configFilePath) {
        this.configFilePath = configFilePath;
    }

    @Nullable
    @Override
    public PluginConfigs getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull PluginConfigs state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}
