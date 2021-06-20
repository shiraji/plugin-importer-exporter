package com.github.shiraji.pluginimporterexporter.view;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentWithBrowseButton;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;

import static com.github.shiraji.pluginimporterexporter.config.PluginImporterExporterConfig.DEFAULT_FILE_NAME;

public class PluginImporterExporterPanel {
    public JPanel mRootPanel;
    public TextFieldWithBrowseButton mTextFieldWithBrowseButton;
    public JCheckBox mSaveDisabledPluginCheckBox;
    private JLabel mSelectFileLabel;

    public JComponent createExporterComponent() {
        FileChooserDescriptor fileChooserDescriptor = new FileChooserDescriptor(true, true, false, false, false, false);

        final BrowseFolderListener listener = new BrowseFolderListener("", "Export plugin setting file path",
                mTextFieldWithBrowseButton,
                null, fileChooserDescriptor, TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT);

        mTextFieldWithBrowseButton.addActionListener(listener);

        mSelectFileLabel.setText("Select a file to export plugin information");
        mSaveDisabledPluginCheckBox.setVisible(true);
        return mRootPanel;
    }

    public JComponent createImporterComponent() {
        FileChooserDescriptor fileChooserDescriptor = new FileChooserDescriptor(true, false, false, false, false, false);
        mTextFieldWithBrowseButton.addBrowseFolderListener("", "Import plugin setting file path",
                null, fileChooserDescriptor, TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT);
        mSelectFileLabel.setText("Select a file to import plugins");
        mSaveDisabledPluginCheckBox.setVisible(false);
        return mRootPanel;
    }

    private static class BrowseFolderListener extends ComponentWithBrowseButton.BrowseFolderActionListener {

        public BrowseFolderListener(@Nullable @NlsContexts.DialogTitle String title, @Nullable @NlsContexts.Label String description,
                                    @Nullable ComponentWithBrowseButton textField, @Nullable Project project,
                                    FileChooserDescriptor fileChooserDescriptor, TextComponentAccessor accessor) {
            super(title, description, textField, project, fileChooserDescriptor, accessor);
        }

        @Override
        protected @NotNull String chosenFileToResultingText(@NotNull VirtualFile chosenFile) {
            final String text = super.chosenFileToResultingText(chosenFile);
            if (chosenFile.isDirectory()) {
                return text + File.separator + DEFAULT_FILE_NAME;
            } else {
                return text;
            }
        }
    }
}
