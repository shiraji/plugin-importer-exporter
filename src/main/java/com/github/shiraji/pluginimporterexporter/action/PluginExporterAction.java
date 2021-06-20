package com.github.shiraji.pluginimporterexporter.action;

import com.github.shiraji.pluginimporterexporter.config.PluginImporterExporterConfig;
import com.github.shiraji.pluginimporterexporter.model.PluginNodeEntity;
import com.github.shiraji.pluginimporterexporter.model.PluginNodeModel;
import com.github.shiraji.pluginimporterexporter.model.PluginNodeModelFactory;
import com.github.shiraji.pluginimporterexporter.view.PluginImporterExporterPanel;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.DialogWrapper;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.jsfr.json.Collector;
import org.jsfr.json.GsonParser;
import org.jsfr.json.JsonSurfer;
import org.jsfr.json.ValueBox;
import org.jsfr.json.compiler.JsonPathCompiler;
import org.jsfr.json.path.JsonPath;
import org.jsfr.json.provider.GsonProvider;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.nio.charset.StandardCharsets.UTF_8;

public class PluginExporterAction extends AnAction {

    Logger mLogger = Logger.getLogger(PluginExporterAction.class.getName());
    JsonSurfer surfer = new JsonSurfer(GsonParser.INSTANCE, GsonProvider.INSTANCE);
    CloseableHttpClient httpClient = HttpClientBuilder.create().build();
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
                            writePluginInfo(file);
                        } catch (IOException e) {
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

    private void writePluginInfo(File jsonFile) throws IOException {
        PluginNodeModel model = PluginNodeModelFactory.newInstance(PluginManager.getPlugins());

        try (FileWriter fileWriter = new FileWriter(jsonFile); BufferedWriter writer = new BufferedWriter(fileWriter)) {

            for (PluginNodeEntity plugin : model.getPluignNodeEntities()) {
                final String pluginName = plugin.getPluginName();
                try {
                    final int id = getId(pluginName);
                    final String fileName = getFileName(id, plugin.getVersion());
                    download(jsonFile, fileName);
                } catch (Exception e) {
                    mLogger.fine(String.format("download file %s failed: %s", pluginName, e.getMessage()));
                }
            }

            writer.write(model.toJsonString());
            writer.flush();
        }
    }

    private int getId(String pluginName) throws IOException {
        HttpGet get = new HttpGet("https://plugins.jetbrains.com/api/searchPlugins?excludeTags=theme&max=12&offset=0&search="
                + URLEncoder.encode(pluginName.replaceAll("([A-Z]+)", " $1"), UTF_8.name()));
        final CloseableHttpResponse response = httpClient.execute(get);
        final String json = EntityUtils.toString(response.getEntity(), UTF_8);
        final Collector collector = surfer.collector(json);
        final JsonPath compiledPath = JsonPathCompiler.compile("$.plugins[?(@.name=='" + pluginName + "')].id");
        final ValueBox<Integer> id = collector.collectOne(compiledPath, Integer.class);
        collector.exec();
        return id.get();
    }

    private String getFileName(int id, String version) throws IOException {
        HttpGet get = new HttpGet("https://plugins.jetbrains.com/api/plugins/" + id + "/updates");
        final CloseableHttpResponse response = httpClient.execute(get);
        final String json = EntityUtils.toString(response.getEntity(), UTF_8);
        final Collector collector = surfer.collector(json);
        final JsonPath compiledPath = JsonPathCompiler.compile("$[?(@.version=='" + version + "')].file");
        final ValueBox<String> name = collector.collectOne(compiledPath, String.class);
        collector.exec();
        return name.get();
    }

    private void download(File jsonFile, String fileName) {
        final File parent = new File(jsonFile.getParentFile().getAbsolutePath() + File.separator + "/plugins");
        if (!parent.exists()) {
            parent.mkdirs();
        }
        final File file = new File(parent, Paths.get(fileName).getFileName().toString());
        try {
            Request.Get("https://plugins.jetbrains.com/files/" + fileName).execute().saveContent(file);
        } catch (IOException e) {
            mLogger.log(Level.FINER, String.format("download file '{%s}'failed", fileName), e);
        }
    }
}
