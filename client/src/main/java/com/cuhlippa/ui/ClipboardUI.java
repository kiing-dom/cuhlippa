package com.cuhlippa.ui;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

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
import java.awt.event.ActionEvent;
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
    private static final int PREFERRED_WINDOW_WIDTH = 800;
    private static final int PREFERRED_WINDOW_HEIGHT = 600;

    private final transient LocalDatabase db;
    private DefaultListModel<ClipboardItem> listModel;
    private JList<ClipboardItem> itemList;
    private transient List<ClipboardItem> allItems;
    private JTextArea detailArea;
    private JLabel imageLabel;
    private JLabel statusBar;
    private JPanel detailPanel;
    private JTextField searchField;

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

        statusBar = new JLabel(" Ready");
        statusBar.setBorder(BorderFactory.createLoweredBevelBorder());
        statusBar.setPreferredSize(new Dimension(0, 20));

        searchField = new JTextField(20);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filterItems(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filterItems(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filterItems(); }
        });
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        JScrollPane listScrollPane = new JScrollPane(itemList);
        JScrollPane detailScrollPane = new JScrollPane(detailArea);
        JScrollPane imageScrollPane = new JScrollPane(imageLabel);
        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        detailPanel.add(detailScrollPane, TEXT_CARD);
        detailPanel.add(imageScrollPane, IMAGE_CARD);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadItems());

        searchPanel.add(new JLabel("Search"));
        searchPanel.add(searchField);
        add(listScrollPane, BorderLayout.CENTER);
        topPanel.add(searchPanel, BorderLayout.CENTER);
        topPanel.add(refreshButton, BorderLayout.EAST);
        bottomPanel.add(detailPanel, BorderLayout.CENTER);
        bottomPanel.add(statusBar, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        add(bottomPanel, BorderLayout.SOUTH);

    }

    private void filterItems() {
        String query = searchField.getText().toLowerCase().trim();
        listModel.clear();

        if (query.isEmpty()) {
            allItems.forEach(listModel::addElement);
        } else {
            allItems.stream()
                .filter(item -> matchesSearch(item, query))
                .forEach(listModel::addElement);
        }

        showStatusMessage("Found " + listModel.size() + " items");
    }

    private boolean matchesSearch(ClipboardItem item, String query) {
        String content = new String(item.getContent()).toLowerCase();
        return content.contains(query) ||
            item.getType().toString().toLowerCase().contains(query);
    }

    private void configureEventListeners() {
        itemList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        itemList.addListSelectionListener(e -> showSelectedItemDetail());
        setupMouseListener();
        setupKeyboardShortcuts();
    }

    private void configureWindow() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(PREFERRED_WINDOW_WIDTH, PREFERRED_WINDOW_HEIGHT));
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void loadItems() {
        allItems = db.getAllItems();
        listModel.clear();
        allItems.forEach(listModel::addElement);
        searchField.setText("");
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
            allItems.add(0, item);
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
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    showContextMenu(e);
                }
            }
        });
    }

    private void setupKeyboardShortcuts() {
        itemList.getInputMap().put(KeyStroke.getKeyStroke("DELETE"), "delete");
        itemList.getActionMap().put("delete", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSelectedItem();
            }
        });
        
        itemList.getInputMap().put(KeyStroke.getKeyStroke("ctrl shift DELETE"), "deleteAll");
        itemList.getActionMap().put("deleteAll", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteAllItems();
            }
        });
    }

    private void showContextMenu(MouseEvent e) {
        int index = itemList.locationToIndex(e.getPoint());
        if (index >= 0) {
            itemList.setSelectedIndex(index);

            JPopupMenu contextMenu = new JPopupMenu();
            JMenuItem copyItem = new JMenuItem("Copy to Clipboard");
            JMenuItem deleteItem = new JMenuItem("Delete Item");
            JMenuItem deleteAll = new JMenuItem("Delete All");

            copyItem.addActionListener(evt -> handleDoubleClick());
            deleteItem.addActionListener(evt -> deleteSelectedItem());
            deleteAll.addActionListener(evt -> deleteAllItems());

            contextMenu.add(copyItem);
            contextMenu.addSeparator();
            contextMenu.add(deleteItem);
            contextMenu.add(deleteAll);

            contextMenu.show(itemList, e.getX(), e.getY());
        }
    }

    private void handleDoubleClick() {
        ClipboardItem selected = itemList.getSelectedValue();
        if (selected != null) {
            copyItemToClipboard(selected);
        }
    }

    private void deleteSelectedItem() {
        ClipboardItem selected = itemList.getSelectedValue();
        if (selected != null) {
            int result = JOptionPane.showConfirmDialog(
                this,
                "Delete this clipboardItem",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
            );

            if (result == JOptionPane.YES_OPTION) {
                boolean deleted = db.deleteItemByHash(selected.getHash());
                if (deleted) {
                    allItems.remove(selected);
                    listModel.removeElement(selected);
                    clearItemDisplay();
                    showStatusMessage("Deleted item.");
                } else {
                    showStatusMessage("Failed to delete item.");
                }
            }
        }
    }

    private void deleteAllItems() {
        if(allItems.isEmpty()) {
            showStatusMessage("No items to delete.");
            return;
        }

        int result = JOptionPane.showConfirmDialog(
            this,
            "Delete all clipboard history? This cannot be undone!",
            "Confirm Delete All",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (result == JOptionPane.YES_OPTION) {
            boolean allDeleted = db.deleteAllItems();
            if (allDeleted) {
                allItems.clear();
                listModel.clear();
                clearItemDisplay();
                showStatusMessage("Deleted all items.");
            } else {
                showStatusMessage("Failed to delete all items");
            }
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
                    showStatusMessage("Image copied to clipboard");
                    break;
                case FILE_PATH:
                    String path = new String(item.getContent());
                    File file = new File(path);
                    if(file.exists()) {
                        transferable = new FileTransferable(Collections.singletonList(file));
                    }
                        showStatusMessage("File path copied to clipboard");
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
        statusBar.setText(" " + message);

        Timer timer = new Timer(3000, e -> statusBar.setText(" Ready"));
        timer.setRepeats(false);
        timer.start();
    }
}
