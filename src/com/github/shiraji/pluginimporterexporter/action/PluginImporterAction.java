package com.github.shiraji.pluginimporterexporter.action;

import com.github.shiraji.pluginimporterexporter.config.PluginImporterExporterConfig;
import com.github.shiraji.pluginimporterexporter.model.PluginNodeModel;
import com.github.shiraji.pluginimporterexporter.model.PluginNodeModelFactory;
import com.github.shiraji.pluginimporterexporter.view.PluginImporterExporterPanel;
import com.google.gson.JsonParseException;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.ide.plugins.PluginManagerMain;
import com.intellij.ide.plugins.PluginNode;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class PluginImporterAction extends AnAction {
    Logger mLogger = Logger.getLogger(PluginImporterAction.class.toString());

    private PluginImporterExporterPanel mPluginImporterExporterPanel;

    @Override
    public void actionPerformed(final AnActionEvent event) {
        final DialogBuilder dialogBuilder = new DialogBuilder(event.getProject());

        Runnable okOperation = new Runnable() {
            @Override
            public void run() {
                saveSettings();
                String filePath = mPluginImporterExporterPanel.mTextFieldWithBrowseButton.getText();
                try {
                    doImportPlugins(event.getProject(), new File(filePath));
                    dialogBuilder.getDialogWrapper().close(DialogWrapper.OK_EXIT_CODE, false);
                } catch (IOException e) {
                    Messages.showErrorDialog("Cannot read file " + filePath, "");
                } catch (JsonParseException e) {
                    Messages.showErrorDialog("Cannot not parse " + filePath, "");
                }
            }
        };

        Runnable cancelOperation = new Runnable() {
            @Override
            public void run() {
                dialogBuilder.getDialogWrapper().close(DialogWrapper.CANCEL_EXIT_CODE, false);
            }
        };

        initDialog(dialogBuilder, okOperation, cancelOperation);
        dialogBuilder.show();
    }

    private void initDialog(final DialogBuilder dialogBuilder, Runnable okOperation, Runnable cancelOperation) {
        initPanel();

        dialogBuilder.setCenterPanel(mPluginImporterExporterPanel.createImporterComponent());
        dialogBuilder.setTitle("Install Plugins");
        dialogBuilder.addOkAction().setText("Install Plugins");
        dialogBuilder.setOkOperation(okOperation);
        dialogBuilder.addCancelAction().setText("Cancel");
        dialogBuilder.setCancelOperation(cancelOperation);
    }

    private void initPanel() {
        PluginImporterExporterConfig config = PluginImporterExporterConfig.getInstance();

        mPluginImporterExporterPanel = new PluginImporterExporterPanel();
        mPluginImporterExporterPanel.mTextFieldWithBrowseButton.setText(
                config.getPluginSettingFilePath());
    }

    private void saveSettings() {
        PluginImporterExporterConfig config = PluginImporterExporterConfig.getInstance();

        config.setPluginSettingFilePath(
                mPluginImporterExporterPanel.mTextFieldWithBrowseButton.getText());
    }

    public void doImportPlugins(final Project project, File file) throws IOException {
        final PluginNodeModel model = PluginNodeModelFactory.newInstance(file);

        Runnable onCleanUpRunnable = new Runnable() {
            @Override
            public void run() {
                disablePlugins(model.getDisabledPluginNodeList());
                PluginManagerMain.notifyPluginsUpdated(project);
            }
        };

        downloadPlugins(model.getDownloadPluginNodeList(), onCleanUpRunnable);
    }

    private void disablePlugins(List<PluginNode> list) {
        for (PluginNode pluginNode : list) {
            if (pluginNode.isEnabled()) {
                continue;
            }

            PluginManager.disablePlugin(pluginNode.getPluginId().getIdString());
        }
    }

    private void downloadPlugins(List<PluginNode> list, Runnable onCleanUpRunnable)
            throws IOException {
        Runnable onSuccessRunnable = new Runnable() {
            @Override
            public void run() {
            }
        };

        PluginManagerMain.downloadPlugins(list, null,
                onSuccessRunnable, onCleanUpRunnable);
    }
}
