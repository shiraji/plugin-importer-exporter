package com.github.shiraji.pluginimporterexporter.model.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intellij.ide.plugins.IdeaPluginDescriptor;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PluginNodeModelFactory {
    public static PluginNodeModel newInstance(IdeaPluginDescriptor[] ideaPluginDescriptors) {
        List<PluginNodeEntity> entities = new ArrayList<PluginNodeEntity>();
        for (IdeaPluginDescriptor ideaPluginDescriptor : ideaPluginDescriptors) {
            PluginNodeEntity entity = PluginNodeEntity.newInstance(ideaPluginDescriptor);
            if (entity != null && entity.isValidEntity()) {
                entities.add(entity);
            }
        }
        PluginNodeModel model = new PluginNodeModel();
        model.setPluginNodeEntities(entities);
        return model;
    }

    public static PluginNodeModel newInstance(File file) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.fromJson(new FileReader(file), PluginNodeModel.class);
    }

}
