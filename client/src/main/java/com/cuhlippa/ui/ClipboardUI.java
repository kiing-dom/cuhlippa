package com.cuhlippa.ui;

import javax.swing.*;

import com.cuhlippa.client.storage.LocalDatabase;
import com.cuhlippa.ui.utils.ClipboardItemRenderer;
import com.cuhlippa.ui.utils.ImageUtils;
import com.cuhlippa.client.clipboard.ClipboardItem;
import com.cuhlippa.client.clipboard.ClipboardListener;

import java.awt.*;
import java.util.List;

public class ClipboardUI extends JFrame implements ClipboardListener {
    private static final String TEXT_CARD = "TEXT";
    private static final String IMAGE_CARD = "IMAGE";
    private static final int DETAIL_AREA_ROWS = 10;
    private static final int DETAIL_AREA_COLS = 40;
    private static final int preferredWindowWidth = 1280;
    private static final int prefferedWindowHeight = 720;

    private final transient LocalDatabase db;
    private DefaultListModel<ClipboardItem> listModel;
    private JList<ClipboardItem> itemList;
    private JTextArea detailArea;
    private JLabel imageLabel;
    private JPanel detailPanel;

    public ClipboardUI(LocalDatabase db) {
        super("Clipboard History");
        this.db = db;
        initializeComponents();
        setupLayout();
        configureEventListeners();
        configureWindow();
        loadItems();
    }

    private void initializeComponents() {
        listModel = new DefaultListModel<>();
        itemList = new JList<>(listModel);
        itemList.setCellRenderer(new ClipboardItemRenderer());
            
        detailArea = new JTextArea(DETAIL_AREA_ROWS, DETAIL_AREA_COLS);
        detailArea.setLineWrap(true);
        detailArea.setWrapStyleWord(true);
        detailArea.setEditable(false);

        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        detailPanel = new JPanel(new CardLayout());
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        JScrollPane listScrollPane = new JScrollPane(itemList);
        JScrollPane detailScrollPane = new JScrollPane(detailArea);
        JScrollPane imageScrollPane = new JScrollPane(imageLabel);

        detailPanel.add(detailScrollPane, TEXT_CARD);
        detailPanel.add(imageScrollPane, IMAGE_CARD);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadItems());

        add(listScrollPane, BorderLayout.CENTER);
        add(detailPanel, BorderLayout.SOUTH);
        add(refreshButton, BorderLayout.NORTH);
    }

    private void configureEventListeners() {
        itemList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        itemList.addListSelectionListener(e -> showSelectedItemDetail());
    }

    private void configureWindow() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(preferredWindowWidth, prefferedWindowHeight));
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void loadItems() {
        listModel.clear();
        List<ClipboardItem> items = db.getAllItems();
        items.forEach(listModel::addElement);
    }

    private void showSelectedItemDetail() {
        ClipboardItem selected = itemList.getSelectedValue();

        if (selected == null) {
            clearItemDisplay();
            return;
        }

        switch (selected.getType()) {
            case TEXT, FILE_PATH:
                displayTextContent(selected);
                break;
            case IMAGE:
                displayImageContent(selected);
                break;
            default:
                displayTextContent(selected);
        }
    }

    private void showCard(String cardName) {
        ((CardLayout) detailPanel.getLayout()).show(detailPanel, cardName);
    }

    private void clearItemDisplay() {
        detailArea.setText("");
        imageLabel.setIcon(null);
    }

    private void displayTextContent(ClipboardItem item) {
        detailArea.setText(new String(item.getContent()));
        showCard(TEXT_CARD);
    }

    private void displayImageContent(ClipboardItem item) {
        try {
            ImageIcon icon = ImageUtils.createScaledImageIcon(item.getContent());
            imageLabel.setIcon(icon);
            showCard(IMAGE_CARD);
        } catch (Exception e) {
            detailArea.setText("Error loading image: " + e.getMessage());
            showCard(TEXT_CARD);
        }
    }

    @Override
    public void onClipboardItemAdded(ClipboardItem item) {
        SwingUtilities.invokeLater(() -> {
            listModel.add(0, item);
            itemList.setSelectedIndex(0);
        });
    }
}
