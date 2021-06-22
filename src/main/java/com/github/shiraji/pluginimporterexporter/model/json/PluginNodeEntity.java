package com.github.shiraji.pluginimporterexporter.model.json;

import com.github.shiraji.pluginimporterexporter.config.PluginImporterExporterConfig;
import com.intellij.ide.plugins.IdeaPluginDependency;
import com.intellij.ide.plugins.IdeaPluginDescriptor;

import java.util.ArrayList;
import java.util.List;

public class PluginNodeEntity {
    private String pluginIdString;
    private String pluginName;
    private String downloadUrl;
    private List<String> dependencyPluginIds;
    private List<String> optionalDependencyPluginIds;
    private boolean isBundle;
    private boolean isEnable;
    private String version;
    private String sinceBuild;
    private String untilBuild;
    private String description;
    private String changeNotes;

    private transient IdeaPluginDescriptor ideaPluginDescriptor;

    public static PluginNodeEntity newInstance(IdeaPluginDescriptor ideaPluginDescriptor) {
        PluginNodeEntity entity = new PluginNodeEntity();
        entity.setPluginIdString(ideaPluginDescriptor.getPluginId().toString());
        entity.setPluginName(ideaPluginDescriptor.getName());
        entity.setDownloadUrl(ideaPluginDescriptor.getUrl());
        entity.setIsBundle(ideaPluginDescriptor.isBundled());
        entity.setIsEnable(ideaPluginDescriptor.isEnabled());
        entity.setVersion(ideaPluginDescriptor.getVersion());
        entity.setSinceBuild(ideaPluginDescriptor.getSinceBuild());
        entity.setUntilBuild(ideaPluginDescriptor.getUntilBuild());
        entity.setDescription(ideaPluginDescriptor.getDescription());
        entity.setChangeNotes(ideaPluginDescriptor.getChangeNotes());
        entity.setIdeaPluginDescriptor(ideaPluginDescriptor);

        List<String> optionalDependencyPluginIds = new ArrayList<>();
        List<String> dependencyPluginIds = new ArrayList<>();

        List<IdeaPluginDependency> dependentPlugins = ideaPluginDescriptor.getDependencies();
        for (IdeaPluginDependency dependentPluginId : dependentPlugins) {
            dependencyPluginIds.add(dependentPluginId.getPluginId().getIdString());
            if (dependentPluginId.isOptional()) {
                optionalDependencyPluginIds.add(dependentPluginId.getPluginId().getIdString());
            }
        }
        entity.setDependencyPluginIds(dependencyPluginIds);
        entity.setOptionalDependencyPluginIds(optionalDependencyPluginIds);

        return entity;
    }

    public void setIsBundle(boolean isBundle) {
        this.isBundle = isBundle;
    }

    public void setIsEnable(boolean isEnable) {
        this.isEnable = isEnable;
    }

    public String getPluginIdString() {
        return pluginIdString;
    }

    public void setPluginIdString(String pluginIdString) {
        this.pluginIdString = pluginIdString;
    }

    public String getPluginName() {
        return pluginName;
    }

    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public List<String> getDependencyPluginIds() {
        return dependencyPluginIds;
    }

    public void setDependencyPluginIds(List<String> dependencyPluginIds) {
        this.dependencyPluginIds = dependencyPluginIds;
    }

    public List<String> getOptionalDependencyPluginIds() {
        return optionalDependencyPluginIds;
    }

    public void setOptionalDependencyPluginIds(List<String> optionalDependencyPluginIds) {
        this.optionalDependencyPluginIds = optionalDependencyPluginIds;
    }

    public boolean isBundle() {
        return isBundle;
    }

    public boolean isEnable() {
        return isEnable;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getSinceBuild() {
        return sinceBuild;
    }

    public void setSinceBuild(String sinceBuild) {
        this.sinceBuild = sinceBuild;
    }

    public String getUntilBuild() {
        return untilBuild;
    }

    public void setUntilBuild(String untilBuild) {
        this.untilBuild = untilBuild;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getChangeNotes() {
        return changeNotes;
    }

    public void setChangeNotes(String changeNotes) {
        this.changeNotes = changeNotes;
    }

    public IdeaPluginDescriptor getIdeaPluginDescriptor() {
        return ideaPluginDescriptor;
    }

    public void setIdeaPluginDescriptor(IdeaPluginDescriptor ideaPluginDescriptor) {
        this.ideaPluginDescriptor = ideaPluginDescriptor;
    }

    public boolean isValidEntity() {
        if (!isValidIdString()) {
            return false;
        }

        // disabled plugin should be save if the user requires
        if (!isEnable()) {
            return PluginImporterExporterConfig.getInstance().isSaveDisablePlugin();
        }

        // non-bundle plugin should be save
        return !isBundle();
    }

    public boolean isValidIdString() {
        String pluginIdString = getPluginIdString();
        // id should not be null
        if (pluginIdString == null) {
            return false;
        }

        // IDEA CORE should be ignore
        if ("com.intellij".equals(pluginIdString)) {
            return false;
        }

        // ignore this plugin
        return !"com.github.shiraji.pluginimporterexporter".equals(pluginIdString);
    }

    @Override
    public String toString() {
        return "PluginNodeEntity{" +
                "mPluginIdString='" + pluginIdString + '\'' +
                ", mPluginName='" + pluginName + '\'' +
                ", mDownloadUrl='" + downloadUrl + '\'' +
                ", mDependencyPluginIds=" + dependencyPluginIds +
                ", mOptionalDependencyPluginIds=" + optionalDependencyPluginIds +
                ", mIsBundle=" + isBundle +
                ", mIsEnable=" + isEnable +
                '}';
    }
}
