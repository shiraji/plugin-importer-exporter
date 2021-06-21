package com.github.shiraji.pluginimporterexporter.model.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.intellij.ide.plugins.PluginInstaller;
import com.intellij.ide.plugins.PluginNode;
import com.intellij.openapi.extensions.PluginId;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PluginNodeModel {
    @SerializedName("plugins")
    private List<PluginNodeEntity> mPluginNodeEntities;

    public List<PluginNodeEntity> getPluginNodeEntities() {
        return mPluginNodeEntities;
    }

    public void setPluginNodeEntities(List<PluginNodeEntity> plugins) {
        mPluginNodeEntities = plugins;
    }

    public String toJsonString() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }

    @NotNull
    public List<PluginNode> getDownloadPluginNodeList() {
        List<PluginNode> list = new ArrayList<PluginNode>();
        List<PluginNodeEntity> pluginNodeEntities = getPluginNodeEntities();
        for (PluginNodeEntity pluginNodeEntity : pluginNodeEntities) {
            if (pluginNodeEntity == null || !pluginNodeEntity.isValidIdString() || pluginNodeEntity.isBundle()) {
                continue;
            }

            list.add(convertToPluginNode(pluginNodeEntity));
        }
        return list;
    }

    @NotNull
    public List<PluginNode> getDisabledPluginNodeList() {
        List<PluginNode> list = new ArrayList<PluginNode>();
        List<PluginNodeEntity> pluginNodeEntities = getPluginNodeEntities();
        for (PluginNodeEntity pluginNodeEntity : pluginNodeEntities) {
            if (pluginNodeEntity == null || !pluginNodeEntity.isValidIdString() || pluginNodeEntity.isEnable()) {
                continue;
            }

            list.add(convertToPluginNode(pluginNodeEntity));
        }
        return list;
    }

    @NotNull
    private PluginNode convertToPluginNode(PluginNodeEntity pluginNodeEntity) {
        PluginNode pluginNode = createPluginNode(pluginNodeEntity);
        List<PluginId> depends = createDepends(pluginNodeEntity);
        PluginId[] optionals = createOptionals(pluginNodeEntity);
        pluginNode.setDepends(depends, optionals);
        return pluginNode;
    }

    @NotNull
    private PluginId[] createOptionals(PluginNodeEntity pluginNodeEntity) {
        int size = pluginNodeEntity
                .getOptionalDependencyPluginIds().size();
        PluginId[] optionals = new PluginId[size];
        for (int i = 0; i < size; i++) {
            optionals[i] = PluginId.getId(pluginNodeEntity
                    .getOptionalDependencyPluginIds().get(i));
        }
        return optionals;
    }

    @NotNull
    private List<PluginId> createDepends(PluginNodeEntity pluginNodeEntity) {
        List<PluginId> depends = new ArrayList<PluginId>();
        for (String dependencyPluginId : pluginNodeEntity.getDependencyPluginIds()) {
            depends.add(PluginId.getId(dependencyPluginId));
        }
        return depends;
    }

    @NotNull
    private PluginNode createPluginNode(PluginNodeEntity pluginNodeEntity) {
        PluginId pluginId = PluginId.getId(pluginNodeEntity.getPluginIdString());
        PluginNode pluginNode = new PluginNode(pluginId);
        pluginNode.setName(pluginNodeEntity.getPluginName());
        pluginNode.setEnabled(pluginNodeEntity.isEnable());
        pluginNode.setSize("-1");
        pluginNode.setRepositoryName(PluginInstaller.UNKNOWN_HOST_MARKER);
        return pluginNode;
    }

    @Override
    public String toString() {
        return "PluginNodeModel{" +
                "mPluginNodeEntities=" + mPluginNodeEntities +
                '}';
    }
}
