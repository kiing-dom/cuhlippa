package com.cuhlippa.ui;

import javax.imageio.ImageIO;
import javax.swing.*;

import com.cuhlippa.client.storage.LocalDatabase;
import com.cuhlippa.ui.utils.ClipboardItemRenderer;
import com.cuhlippa.ui.utils.FileTransferable;
import com.cuhlippa.ui.utils.ImageSelection;
import com.cuhlippa.ui.utils.ImageUtils;
import com.cuhlippa.client.clipboard.ClipboardItem;
import com.cuhlippa.client.clipboard.ClipboardListener;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class ClipboardUI extends JFrame implements ClipboardListener {
    private static final String TEXT_CARD = "TEXT";
    private static final String IMAGE_CARD = "IMAGE";
    private static final int DETAIL_AREA_ROWS = 10;
    private static final int DETAIL_AREA_COLS = 40;
    private static final int PREFERRED_WINDOW_WIDTH = 1280;
    private static final int PREFERRED_WINDOW_HEIGHT = 720;

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
        setupMouseListener();
    }

    private void configureWindow() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(PREFERRED_WINDOW_WIDTH, PREFERRED_WINDOW_HEIGHT));
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

    private void setupMouseListener() {
        itemList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    handleDoubleClick();
                }
            }
        });
    }

    private void handleDoubleClick() {
        ClipboardItem selected = itemList.getSelectedValue();
        if (selected != null) {
            copyItemToClipboard(selected);
        }
    }

    private void copyItemToClipboard(ClipboardItem item) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

        try {
            Transferable transferable = null;
            switch (item.getType()) {
                case TEXT:
                    transferable = new StringSelection(new String(item.getContent()));
                    showStatusMessage("Text copied to clipboard");
                    break;
                case IMAGE:
                    ByteArrayInputStream bais = new ByteArrayInputStream(item.getContent());
                    BufferedImage img = ImageIO.read(bais);
                    if (img != null) transferable = new ImageSelection(img);
                    break;
                case FILE_PATH:
                    String path = new String(item.getContent());
                    File file = new File(path);
                    if(file.exists()) {
                        transferable = new FileTransferable(Collections.singletonList(file));
                    }
                    break;
            }
            if (transferable != null) {
                clipboard.setContents(transferable, null);
                System.out.println("Copied item back to system clipboard: " + item.getType());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showStatusMessage(String message) {
        //TODO: add a status bar or tooltip later
        System.out.println(message);
    }
}
