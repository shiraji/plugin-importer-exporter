package com.github.shiraji.pluginimporterexporter.action;

import com.github.shiraji.pluginimporterexporter.config.PluginImporterExporterConfig;
import com.github.shiraji.pluginimporterexporter.model.PluginNodeModel;
import com.github.shiraji.pluginimporterexporter.model.PluginNodeModelFactory;
import com.github.shiraji.pluginimporterexporter.view.PluginImporterExporterPanel;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;

public class PluginExporterAction extends AnAction {

    Logger mLogger = Logger.getLogger(PluginExporterAction.class.getName());

    private PluginImporterExporterPanel mPluginImporterExporterPanel;

    @Override
    public void actionPerformed(AnActionEvent e) {
        DialogBuilder dialogBuilder = new DialogBuilder(e.getProject());
        initDialog(dialogBuilder);
        dialogBuilder.show();
    }

    private void initDialog(final DialogBuilder dialogBuilder) {
        initPanel();

        dialogBuilder.setCenterPanel(mPluginImporterExporterPanel.createExporterComponent());
        dialogBuilder.setTitle("Save plugins information");
        dialogBuilder.addOkAction().setText("Export");

        // TODO this operation is super awkward.
        dialogBuilder.setOkOperation(new Runnable() {
            @Override
            public void run() {
                saveSettings();
                final String filePath = mPluginImporterExporterPanel.mTextFieldWithBrowseButton.getText();
                final File file = new File(filePath);

                if (file.exists()) {
                    setNewOkOperation(file);
                    setErrorText(file);
                } else {
                    doExport(file);
                }
            }

            private void setNewOkOperation(final File oldFile) {
                dialogBuilder.setOkOperation(new Runnable() {
                    @Override
                    public void run() {
                        saveSettings();
                        String newFilePath = mPluginImporterExporterPanel.mTextFieldWithBrowseButton.getText();
                        File newFile = new File(newFilePath);
                        if (!newFile.exists() || oldFile.getAbsolutePath().equals(newFilePath)) {
                            doExport(newFile);
                        } else {
                            setNewOkOperation(newFile);
                            setErrorText(newFile);
                        }
                    }
                });
            }

            private void setErrorText(File file) {
                dialogBuilder.setErrorText("'" + file
                        .getAbsolutePath() + "' exists. Click 'Export' to overwrite");
            }

            private void doExport(File file) {
                try {
                    writePluginInfo(file);
                    Notifications.Bus.notify(new Notification("Plugin Importer+Exporter",
                            "Plugin Importer+Exporter",
                            "Successfully exported plugin information", NotificationType.INFORMATION));
                    dialogBuilder.getDialogWrapper().close(DialogWrapper.OK_EXIT_CODE, false);
                } catch (IOException e1) {
                    Messages.showErrorDialog("Cannot write file " + file.getAbsolutePath(), "");
                }
            }
        });

        addCancelButton(dialogBuilder);
    }

    private void addCancelButton(final DialogBuilder dialogBuilder) {
        dialogBuilder.addCancelAction().setText("Cancel");
        dialogBuilder.setCancelOperation(new Runnable() {
            @Override
            public void run() {
                dialogBuilder.getDialogWrapper().close(DialogWrapper.CANCEL_EXIT_CODE, false);
            }
        });
    }

    private void initPanel() {
        PluginImporterExporterConfig config = PluginImporterExporterConfig.getInstance();

        mPluginImporterExporterPanel = new PluginImporterExporterPanel();
        mPluginImporterExporterPanel.mTextFieldWithBrowseButton.setText(
                config.getPluginSettingFilePath());
        mPluginImporterExporterPanel.mSaveDisabledPluginCheckBox.setSelected(
                config.isSaveDisablePlugin());
    }

    private void saveSettings() {
        PluginImporterExporterConfig config = PluginImporterExporterConfig.getInstance();

        config.setPluginSettingFilePath(
                mPluginImporterExporterPanel.mTextFieldWithBrowseButton.getText());
        config.setIsSaveDisablePlugin(
                mPluginImporterExporterPanel.mSaveDisabledPluginCheckBox.isSelected());
    }

    private void writePluginInfo(File jsonFile) throws IOException {
        PluginNodeModel model = PluginNodeModelFactory.newInstance(PluginManager.getPlugins());

        FileWriter fileWriter = null;
        BufferedWriter writer = null;

        try {
            fileWriter = new FileWriter(jsonFile);
            writer = new BufferedWriter(fileWriter);
            writer.write(model.toJsonString());
            writer.flush();
        } finally {
            if (fileWriter != null) {
                fileWriter.close();
            }

            if (writer != null) {
                writer.close();
            }
        }
    }
}
