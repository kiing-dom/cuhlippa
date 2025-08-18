package com.cuhlippa.ui.utils;

import javax.swing.*;
import java.awt.*;
import java.util.Set;
import java.util.HashSet;

import com.cuhlippa.client.clipboard.ClipboardItem;
import com.cuhlippa.client.storage.LocalDatabase;

public class TagEditDialog extends JDialog {
    private final transient ClipboardItem item;
    private final transient LocalDatabase db;
    private JTextField tagField;
    private JComboBox<String> categoryCombo;
    private JPanel tagsPanel;
    private final Set<String> currentTags;

    public TagEditDialog(JFrame parent, ClipboardItem item, LocalDatabase db) {
        super(parent, "Edit Tags and Category");
        this.item = item;
        this.db = db;
        this.currentTags = new HashSet<>(item.getTags());

        initializeComponents();
        setupLayout();
        loadCurrentData();
        pack();
        setLocationRelativeTo(parent);
    }

    private void initializeComponents() {
        Set<String> categories = db.getAllCategories();
        categories.add("General");
        categories.add("Work");
        categories.add("Personal");
        categories.add("Code");
        categories.add("Images");

        categoryCombo = new JComboBox<>(categories.toArray(new String[0]));
        categoryCombo.setEditable(true);

        tagsPanel = new JPanel();
        tagsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(categoryCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JLabel("Add Tag:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JPanel tagInputPanel = new JPanel(new BorderLayout());
        tagInputPanel.add(tagField, BorderLayout.CENTER);

        JButton addTagButton = new JButton("Add");
        addTagButton.addActionListener(e -> addTag());
        tagInputPanel.add(addTagButton, BorderLayout.EAST);

        mainPanel.add(tagInputPanel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        mainPanel.add(new JLabel("Current Tags:"), gbc);

        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        JScrollPane tagsScrollPane = new JScrollPane(tagsPanel);
        tagsScrollPane.setPreferredSize(new Dimension(400, 100));
        mainPanel.add(tagsScrollPane, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> saveChanges());
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        tagField.addActionListener(e -> addTag());
    }

    private void loadCurrentData() {
        categoryCombo.setSelectedItem(item.getCategory());
        updateTagsDisplay();
    }

    private void addTag() {
        String tagText = tagField.getText().trim();
        if (!tagText.isEmpty() && !currentTags.contains(tagText.toLowerCase())) {
            currentTags.add(tagText.toLowerCase());
            tagField.setText("");
            updateTagsDisplay();
        }
    }

    private void updateTagsDisplay() {
        tagsPanel.removeAll();

        for (String tag : currentTags) {
            JPanel tagChip = createTagChip(tag);
            tagsPanel.add(tagChip);
        }

        tagsPanel.revalidate();
        tagsPanel.repaint();
    }

    private JPanel createTagChip(String tag) {
        JPanel chip = new JPanel(new BorderLayout());
        chip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createRaisedBevelBorder(),
                BorderFactory.createEmptyBorder(2, 6, 2, 6)));
        chip.setBackground(new Color(230, 230, 250));

        JLabel tagLabel = new JLabel(tag);
        JButton removeButton = new JButton("×");
        removeButton.setPreferredSize(new Dimension(20, 20));
        removeButton.setMargin(new Insets(0, 0, 0, 0));
        removeButton.addActionListener(e -> {
            currentTags.remove(tag);
            updateTagsDisplay();
        });

        chip.add(tagLabel, BorderLayout.CENTER);
        chip.add(removeButton, BorderLayout.EAST);

        return chip;
    }

    private void saveChanges() {
        item.setCategory((String) categoryCombo.getSelectedItem());
        item.setTags(currentTags);
        db.saveItem(item);
        dispose();
    }
}