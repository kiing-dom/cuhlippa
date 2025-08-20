package com.cuhlippa.ui.utils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

import com.cuhlippa.client.export.*;
import com.cuhlippa.client.storage.LocalDatabase;

public class ExportImportDialog extends JDialog {
    private final transient LocalDatabase db;
    private final transient ClipboardExportService exportService;

    private JComboBox<String> categoryFilter;
    private JComboBox<String> tagFilter;
    private JCheckBox includeImages;
    private JCheckBox includeText;
    private JCheckBox includeFiles;
    private JCheckBox skipDuplicates;
    private JCheckBox mergeCategories;
    private JCheckBox mergeTags;

    public ExportImportDialog(JFrame parent, LocalDatabase db) {
        super(parent, "Export/Import Clipboard Data", true);
        this.db = db;
        this.exportService = new ClipboardExportService(db);

        initializeComponents();
        setupLayout();
        pack();
        setLocationRelativeTo(parent);
    }    private void initializeComponents() {
        categoryFilter = new JComboBox<>();
        categoryFilter.addItem("All Categories");
        db.getAllCategories().forEach(categoryFilter::addItem);

        tagFilter = new JComboBox<>();
        tagFilter.addItem("All Tags");
        db.getAllTags().forEach(tagFilter::addItem);

        includeImages = new JCheckBox("Include Images", true);
        includeText = new JCheckBox("Include Text", true);
        includeFiles = new JCheckBox("Include Files", true);

        skipDuplicates = new JCheckBox("Skip Duplicates", true);
        mergeCategories = new JCheckBox("Merge Categories", true);
        mergeTags = new JCheckBox("Merge Tags", true);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.add("Export", createExportPanel());
        tabbedPane.add("Import", createImportPanel());

        add(tabbedPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createExportPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Filter Options:"), gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(5, 20, 5, 10);
        panel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(categoryFilter, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Tag:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(tagFilter, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(15, 10, 5, 10);
        panel.add(new JLabel("Content Types:"), gbc);

        gbc.gridy = 4;
        gbc.insets = new Insets(5, 20, 5, 10);
        JPanel contentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        contentPanel.add(includeText);
        contentPanel.add(includeImages);
        contentPanel.add(includeFiles);
        panel.add(contentPanel, gbc);

        gbc.gridy = 5;
        gbc.insets = new Insets(20, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JButton exportButton = new JButton("Export to File...");
        exportButton.setPreferredSize(new Dimension(200, 30));
        exportButton.addActionListener(e -> performExport());
        panel.add(exportButton, gbc);

        return panel;
    }

    private JPanel createImportPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(new JLabel("Import Options:"), gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(5, 20, 5, 10);
        JPanel optionsPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        optionsPanel.add(skipDuplicates);
        optionsPanel.add(mergeCategories);
        optionsPanel.add(mergeTags);
        panel.add(optionsPanel, gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(20, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JButton importButton = new JButton("Import from File...");
        importButton.setPreferredSize(new Dimension(200, 30));
        importButton.addActionListener(e -> performImport());
        panel.add(importButton, gbc);

        return panel;
    }

    private void performExport() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Clipboard Data");
        fileChooser.setSelectedFile(new File(exportService.generateDefaultFilename()));
        fileChooser.setFileFilter(new FileNameExtensionFilter("JSON files (*.json)", "json"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (!selectedFile.getName().toLowerCase().endsWith(".json")) {
                selectedFile = new File(selectedFile.getAbsolutePath() + ".json");
            }

            try {
                ExportOptions options = buildExportOptions();
                exportService.exportToFile(selectedFile, options);

                JOptionPane.showMessageDialog(this,
                        "Export completed successfully!\nFile: " + selectedFile.getName(),
                        "Export Complete",
                        JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Export failed: " + ex.getMessage(),
                        "Export Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void performImport() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Import Clipboard Data");
        fileChooser.setFileFilter(new FileNameExtensionFilter("JSON files (*.json)", "json"));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                ImportOptions options = buildImportOptions();
                ImportResult result = exportService.importFromFile(fileChooser.getSelectedFile(), options);
                showImportResult(result);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Import failed: " + ex.getMessage(),
                        "Import Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private ExportOptions buildExportOptions() {
        ExportOptions options = new ExportOptions();

        String selectedCategory = (String) categoryFilter.getSelectedItem();
        if (!"All Categories".equals(selectedCategory)) {
            options.setCategory(selectedCategory);
        }

        String selectedTag = (String) tagFilter.getSelectedItem();
        if (!"All Tags".equals(selectedTag)) {
            options.setTag(selectedTag);
        }

        options.setIncludeImages(includeImages.isSelected());
        options.setIncludeText(includeText.isSelected());
        options.setIncludeFiles(includeFiles.isSelected());

        return options;
    }

    private ImportOptions buildImportOptions() {
        ImportOptions options = new ImportOptions();
        options.setSkipDuplicates(skipDuplicates.isSelected());
        options.setMergeCategories(mergeCategories.isSelected());
        options.setMergeTags(mergeTags.isSelected());
        return options;
    }

    private void showImportResult(ImportResult result) {
        StringBuilder message = new StringBuilder();
        message.append("Import completed!\n\n");
        message.append(String.format("✓ Imported: %d items\n", result.getImported()));
        message.append(String.format("⚠ Skipped: %d items\n", result.getSkipped()));
        message.append(String.format("✗ Errors: %d items\n", result.getErrors()));
        message.append(String.format("📊 Total processed: %d items", result.getTotalProcessed()));

        if (result.getErrors() > 0 && !result.getErrorMessages().isEmpty()) {
            message.append("\n\nError details:\n");
            for (String error : result.getErrorMessages()) {
                message.append("• ").append(error).append("\n");
            }
        }

        JTextArea textArea = new JTextArea(message.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 250));

        JOptionPane.showMessageDialog(this, scrollPane, "Import Results", JOptionPane.INFORMATION_MESSAGE);
    }
}
