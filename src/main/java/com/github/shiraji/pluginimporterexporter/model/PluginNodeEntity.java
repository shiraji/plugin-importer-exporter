package com.github.shiraji.pluginimporterexporter.model;

import com.github.shiraji.pluginimporterexporter.config.PluginImporterExporterConfig;
import com.google.gson.annotations.SerializedName;
import com.intellij.ide.plugins.IdeaPluginDependency;
import com.intellij.ide.plugins.IdeaPluginDescriptor;

import java.util.ArrayList;
import java.util.List;

public class PluginNodeEntity {
    @SerializedName("pluginId")
    private String mPluginIdString;

    @SerializedName("pluginName")
    private String mPluginName;

    @SerializedName("downloadUrl")
    private String mDownloadUrl;

    @SerializedName("dependencyPluginIds")
    private List<String> mDependencyPluginIds;

    @SerializedName("optionalDependencyPluginIds")
    private List<String> mOptionalDependencyPluginIds;

    @SerializedName("isBundle")
    private boolean mIsBundle;

    @SerializedName("isEnable")
    private boolean mIsEnable;

    @SerializedName("version")
    private String version;

    public static PluginNodeEntity newInstance(IdeaPluginDescriptor
                                                       ideaPluginDescriptor) {
        PluginNodeEntity entity = new PluginNodeEntity();
        entity.setPluginIdString(ideaPluginDescriptor.getPluginId().toString());
        entity.setPluginName(ideaPluginDescriptor.getName());
        entity.setDownloadUrl(ideaPluginDescriptor.getUrl());
        entity.setIsBundle(ideaPluginDescriptor.isBundled());
        entity.setIsEnable(ideaPluginDescriptor.isEnabled());
        entity.setVersion(ideaPluginDescriptor.getVersion());

        List<String> idStrings2 = new ArrayList<String>();
        List<String> idStrings = new ArrayList<String>();

        List<IdeaPluginDependency> dependentPlugins = ideaPluginDescriptor.getDependencies();
        for (IdeaPluginDependency dependentPluginId : dependentPlugins) {
            idStrings.add(dependentPluginId.getPluginId().getIdString());
            if (dependentPluginId.isOptional()) {
                idStrings2.add(dependentPluginId.getPluginId().getIdString());
            }
        }
        entity.setDependencyPluginIds(idStrings);
        entity.setOptionalDependencyPluginIds(idStrings2);

        return entity;
    }

    public void setIsBundle(boolean isBundle) {
        mIsBundle = isBundle;
    }

    public void setIsEnable(boolean isEnable) {
        mIsEnable = isEnable;
    }

    public String getPluginIdString() {
        return mPluginIdString;
    }

    public void setPluginIdString(String pluginIdString) {
        mPluginIdString = pluginIdString;
    }

    public String getPluginName() {
        return mPluginName;
    }

    public void setPluginName(String pluginName) {
        mPluginName = pluginName;
    }

    public String getDownloadUrl() {
        return mDownloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        mDownloadUrl = downloadUrl;
    }

    public List<String> getDependencyPluginIds() {
        return mDependencyPluginIds;
    }

    public void setDependencyPluginIds(List<String> dependencyPluginIds) {
        mDependencyPluginIds = dependencyPluginIds;
    }

    public List<String> getOptionalDependencyPluginIds() {
        return mOptionalDependencyPluginIds;
    }

    public void setOptionalDependencyPluginIds(List<String> optionalDependencyPluginIds) {
        mOptionalDependencyPluginIds = optionalDependencyPluginIds;
    }

    public boolean isBundle() {
        return mIsBundle;
    }

    public boolean isEnable() {
        return mIsEnable;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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
                "mPluginIdString='" + mPluginIdString + '\'' +
                ", mPluginName='" + mPluginName + '\'' +
                ", mDownloadUrl='" + mDownloadUrl + '\'' +
                ", mDependencyPluginIds=" + mDependencyPluginIds +
                ", mOptionalDependencyPluginIds=" + mOptionalDependencyPluginIds +
                ", mIsBundle=" + mIsBundle +
                ", mIsEnable=" + mIsEnable +
                '}';
    }
}
