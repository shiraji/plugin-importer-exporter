package com.github.shiraji.pluginimporterexporter.view;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class PluginImporterExporterPanel {
    public JPanel mRootPanel;
    public TextFieldWithBrowseButton mTextFieldWithBrowseButton;
    public JCheckBox mSaveDisabledPluginCheckBox;
    private JLabel mSelectFileLabel;

    public JComponent createExporterComponent() {
        FileChooserDescriptor fileChooserDescriptor = new FileChooserDescriptor(true, false, false, false, false, false);
        mTextFieldWithBrowseButton.addBrowseFolderListener("", "Export plugin setting file path",
                null, fileChooserDescriptor, TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT, false);
        mSelectFileLabel.setText("Select a file to export plugin information");
        mSaveDisabledPluginCheckBox.setVisible(true);
        return mRootPanel;
    }

    public JComponent createImporterComponent() {
        FileChooserDescriptor fileChooserDescriptor = new FileChooserDescriptor(true, false, false, false, false, false);
        mTextFieldWithBrowseButton.addBrowseFolderListener("", "Import plugin setting file path",
                null, fileChooserDescriptor, TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT, false);
        mSelectFileLabel.setText("Select a file to import plugins");
        mSaveDisabledPluginCheckBox.setVisible(false);
        return mRootPanel;
    }

}
