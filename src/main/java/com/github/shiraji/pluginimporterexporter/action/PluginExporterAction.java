package com.github.shiraji.pluginimporterexporter.action;

import com.github.shiraji.pluginimporterexporter.config.PluginImporterExporterConfig;
import com.github.shiraji.pluginimporterexporter.model.json.PluginNodeEntity;
import com.github.shiraji.pluginimporterexporter.model.json.PluginNodeModel;
import com.github.shiraji.pluginimporterexporter.model.json.PluginNodeModelFactory;
import com.github.shiraji.pluginimporterexporter.model.xml.IdeaVersion;
import com.github.shiraji.pluginimporterexporter.model.xml.Plugin;
import com.github.shiraji.pluginimporterexporter.model.xml.Plugins;
import com.github.shiraji.pluginimporterexporter.view.PluginImporterExporterPanel;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.ide.plugins.marketplace.MarketplaceRequests;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.PermanentInstallationID;
import com.intellij.openapi.application.impl.ApplicationInfoImpl;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.util.Urls;
import com.sun.xml.bind.marshaller.CharacterEscapeHandler;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class PluginExporterAction extends AnAction {
    Logger mLogger = Logger.getLogger(PluginExporterAction.class.getName());
    private static final NotificationGroup NOTIFICATION_GROUP =
            new NotificationGroup("com.github.shiraji", NotificationDisplayType.BALLOON, true);

    private PluginImporterExporterPanel mPluginImporterExporterPanel;

    @Override
    public void actionPerformed(AnActionEvent e) {
        DialogBuilder dialogBuilder = new DialogBuilder(e.getProject());
        initDialog(dialogBuilder, e.getProject());
        dialogBuilder.show();
    }

    private void initDialog(final DialogBuilder dialogBuilder, Project project) {
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
                final Task.Backgroundable task = new Task.Backgroundable(project, "Downloading plugin package", false) {
                    @Override
                    public void run(@NotNull ProgressIndicator indicator) {
                        try {
                            writePluginInfo(file, indicator);
                        } catch (IOException | JAXBException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public void onSuccess() {
                        super.onSuccess();
                        NOTIFICATION_GROUP.createNotification("Successfully exported plugin information",
                                NotificationType.INFORMATION)
                                .notify(project);
                    }

                    @Override
                    public void onThrowable(@NotNull Throwable error) {
                        super.onThrowable(error);
                        NOTIFICATION_GROUP.createNotification("exported plugin information failed", NotificationType.ERROR)
                                .notify(project);
                    }
                };
                ProgressManager.getInstance().runProcessWithProgressAsynchronously(task,
                        new BackgroundableProcessIndicator(task));

                dialogBuilder.getDialogWrapper().close(DialogWrapper.OK_EXIT_CODE, false);
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

    private void writePluginInfo(File jsonFile, @NotNull ProgressIndicator indicator) throws IOException, JAXBException {
        PluginNodeModel model = PluginNodeModelFactory.newInstance(PluginManager.getPlugins());

        try (FileWriter jsonFileWriter = new FileWriter(jsonFile);
             BufferedWriter jsonWriter = new BufferedWriter(jsonFileWriter);
        ) {
            jsonWriter.write(model.toJsonString());
            jsonWriter.flush();
        }

        if (mPluginImporterExporterPanel.mExportAsPluginRepositoryCheckBox.isSelected()) {
            exportAsRepository(jsonFile, indicator, model);
        }
    }

    private void exportAsRepository(File jsonFile, @NotNull ProgressIndicator indicator, PluginNodeModel model)
            throws IOException, JAXBException {
        try (FileWriter xmlFileWriter = new FileWriter(new File(jsonFile.getParentFile(), "updatePlugins.xml"));
             BufferedWriter xmlWriter = new BufferedWriter(xmlFileWriter);) {
            Plugins plugins = new Plugins();

            for (PluginNodeEntity plugin : model.getPluginNodeEntities()) {
                final String pluginName = plugin.getPluginName();
                try {
                    File file = downloadPluginFile(jsonFile, indicator, plugin, pluginName);

                    String url = mPluginImporterExporterPanel.mRepositoryURL.getText();
                    if (!url.endsWith("/")) {
                        url += "/";
                    }
                    Plugin p = new Plugin(plugin.getPluginIdString(),
                            url + file.getName(),
                            plugin.getVersion(),
                            pluginName);
                    p.setDescription(plugin.getDescription());
                    p.setChangeNotes(plugin.getChangeNotes());
                    IdeaVersion ideaVersion = new IdeaVersion(plugin.getSinceBuild(), plugin.getUntilBuild());
                    p.setIdeaVersion(ideaVersion);

                    plugins.add(p);
                } catch (Exception e) {
                    mLogger.fine(String.format("download file %s failed: %s", pluginName, e.getMessage()));
                }

            }
            JAXBContext context = JAXBContext.newInstance(Plugins.class);
            Marshaller mar = context.createMarshaller();
            mar.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            mar.setProperty(CharacterEscapeHandler.class.getName(),
                    (CharacterEscapeHandler)(ac, i, j, flag, writer) -> writer.write(ac, i, j));
            mar.marshal(plugins, xmlWriter);
            xmlWriter.flush();
        }
    }

    private File downloadPluginFile(File jsonFile, @NotNull ProgressIndicator indicator, PluginNodeEntity plugin, String pluginName)
            throws IOException {
        final MarketplaceRequests myMarketplaceRequests = MarketplaceRequests.getInstance();

        final Map<String, String> parameters = new HashMap<>();
        parameters.put("id", plugin.getPluginIdString());
        parameters.put("build", myMarketplaceRequests.getBuildForPluginRepositoryRequests());
        parameters.put("uuid", PermanentInstallationID.get());
        final String url = Urls
                .newFromEncoded(ApplicationInfoImpl.getShadowInstance().getPluginsDownloadUrl())
                .addParameters(parameters)
                .toExternalForm();

        final File file = myMarketplaceRequests.download(url, indicator);
        final File destFile = new File(jsonFile.getParentFile(), file.getName());

        FileUtils.moveFile(file, destFile);

        return destFile;
    }

}
