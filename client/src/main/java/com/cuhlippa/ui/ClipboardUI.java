package com.cuhlippa.ui;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.cuhlippa.client.clipboard.ClipboardItem;
import com.cuhlippa.client.clipboard.ClipboardListener;
import com.cuhlippa.client.config.Settings;
import com.cuhlippa.client.storage.LocalDatabase;
import com.cuhlippa.ui.utils.ClipboardItemRenderer;
import com.cuhlippa.ui.utils.ExportImportDialog;
import com.cuhlippa.ui.utils.FileTransferable;
import com.cuhlippa.ui.utils.ImageSelection;
import com.cuhlippa.ui.utils.ImageUtils;
import com.cuhlippa.ui.utils.SettingsDialog;
import com.cuhlippa.ui.utils.TagEditDialog;
import com.cuhlippa.ui.utils.UserFriendlyErrors;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
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
    private final transient Settings settings;
    private DefaultListModel<ClipboardItem> listModel;
    private JList<ClipboardItem> itemList;
    private transient List<ClipboardItem> allItems;
    private JTextArea detailArea;
    private JLabel imageLabel;
    private JLabel statusBar;
    private JPanel detailPanel;
    private JTextField searchField;
      // Demo mode fields
    private boolean demoMode = false;
    private String demoDeviceName = null;
    private com.cuhlippa.client.clipboard.DemoClipboardManager demoClipboardManager = null;

    public ClipboardUI(LocalDatabase db, Settings settings) {
        super("Cuhlippa");
        this.db = db;
        this.settings = settings;
        initializeComponents();
        setupLayout();
        setupMenuBar();
        configureEventListeners();
        configureWindow();
        applyTheme();
        loadItems();
    }
      /**
     * Enable demo mode with enhanced UI indicators
     */
    public void setDemoMode(boolean demoMode, String deviceName) {
        this.demoMode = demoMode;
        this.demoDeviceName = deviceName;
        
        if (demoMode) {
            // Update window title with demo indicator
            setTitle("🎬 Cuhlippa DEMO - " + deviceName);
            
            // Add demo mode indicator to status bar
            showStatusMessage("🎬 DEMO MODE: " + deviceName + " - Virtual clipboard active");
            
            // Add demo controls to the UI
            addDemoControls();
        }
    }
    
    /**
     * Set the demo clipboard manager for demo mode
     */
    public void setDemoClipboardManager(com.cuhlippa.client.clipboard.DemoClipboardManager demoClipboardManager) {
        this.demoClipboardManager = demoClipboardManager;
    }

    private void initializeComponents() {
        listModel = new DefaultListModel<>();
        itemList = new JList<>(listModel);
        itemList.setCellRenderer(new ClipboardItemRenderer(settings));

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
            public void insertUpdate(DocumentEvent e) {
                filterItems();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterItems();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterItems();
            }
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
            showCard(IMAGE_CARD);        } catch (Exception e) {
            detailArea.setText("Cannot display this image");
            UserFriendlyErrors.logError("Image display failed", "Error loading image: " + e.getMessage());
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
    }    private void showContextMenu(MouseEvent e) {
        int index = itemList.locationToIndex(e.getPoint());
        if (index >= 0) {
            itemList.setSelectedIndex(index);
            ClipboardItem selectedItem = itemList.getSelectedValue();

            JPopupMenu contextMenu = new JPopupMenu();
            JMenuItem copyItem = new JMenuItem("Copy to Clipboard");
            JMenuItem editTagsItem = new JMenuItem("Edit Tags");
            
            // Pin/Unpin menu item
            boolean isPinned = selectedItem != null && selectedItem.isPinned();
            JMenuItem pinItem = new JMenuItem(isPinned ? "Unpin Item" : "Pin Item");
            
            JMenuItem deleteItem = new JMenuItem("Delete Item");
            JMenuItem deleteAll = new JMenuItem("Delete All");

            copyItem.addActionListener(evt -> handleDoubleClick());
            editTagsItem.addActionListener(evt -> showTagEditDialog());
            pinItem.addActionListener(evt -> toggleItemPin());
            deleteItem.addActionListener(evt -> deleteSelectedItem());
            deleteAll.addActionListener(evt -> deleteAllItems());

            contextMenu.add(copyItem);
            contextMenu.add(editTagsItem);
            contextMenu.add(pinItem);
            contextMenu.addSeparator();
            contextMenu.add(deleteItem);
            contextMenu.add(deleteAll);

            contextMenu.show(itemList, e.getX(), e.getY());
        }
    }

    private void showTagEditDialog() {
        ClipboardItem selected = itemList.getSelectedValue();
        if (selected != null) {
            TagEditDialog dialog = new TagEditDialog(this, selected, db);
            dialog.setModal(true);
            dialog.setVisible(true);
            // Dialog is modal, so this code runs after it's closed
            loadItems();
            showStatusMessage("Tags updated for item");
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
                    "Delete this clipboard item?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION);

            if (result == JOptionPane.YES_OPTION) {
                boolean deleted = db.deleteItemByHash(selected.getHash());
                if (deleted) {
                    allItems.remove(selected);
                    listModel.removeElement(selected);
                    clearItemDisplay();
                    showStatusMessage("Deleted item.");                } else {
                    showStatusMessage("Could not delete item.");
                    UserFriendlyErrors.logError("Delete operation failed", "Failed to delete clipboard item from database");
                }
            }
        }
    }

    private void deleteAllItems() {
        if (allItems.isEmpty()) {
            showStatusMessage("No items to delete.");
            return;
        }

        int result = JOptionPane.showConfirmDialog(
                this,
                "Delete all clipboard history? This cannot be undone!",
                "Confirm Delete All",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            boolean allDeleted = db.deleteAllItems();
            if (allDeleted) {
                allItems.clear();
                listModel.clear();
                clearItemDisplay();
                showStatusMessage("Deleted all items.");            } else {
                showStatusMessage("Could not delete all items");
                UserFriendlyErrors.logError("Clear all operation failed", "Failed to delete all items from database");
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
                    if (img != null)
                        transferable = new ImageSelection(img);
                    showStatusMessage("Image copied to clipboard");
                    break;
                case FILE_PATH:
                    String path = new String(item.getContent());
                    File file = new File(path);
                    if (file.exists()) {
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

    private void applyTheme() {
        ThemeColors colors = getThemeColors();
        applyMainComponentColors(colors);
        applyContainerColors(colors);
        applyMenuBarColors(colors);

        SwingUtilities.updateComponentTreeUI(this);
        repaint();
    }

    private ThemeColors getThemeColors() {
        if ("dark".equals(settings.getTheme())) {
            return new ThemeColors(
                    new Color(45, 45, 45),
                    Color.WHITE,
                    new Color(60, 60, 60),
                    new Color(70, 70, 70));
        } else {
            return new ThemeColors(
                    Color.WHITE,
                    Color.BLACK,
                    new Color(245, 245, 245),
                    new Color(230, 230, 230));
        }
    }

    private void applyMainComponentColors(ThemeColors colors) {
        getContentPane().setBackground(colors.background);

        statusBar.setBackground(colors.panel);
        statusBar.setForeground(colors.foreground);
        statusBar.setOpaque(true);

        itemList.setBackground(colors.background);
        itemList.setForeground(colors.foreground);
        itemList.setSelectionBackground(colors.panel);
        itemList.setSelectionForeground(colors.foreground);

        detailArea.setBackground(colors.background);
        detailArea.setForeground(colors.foreground);
        detailArea.setCaretColor(colors.foreground);

        searchField.setBackground(colors.background);
        searchField.setForeground(colors.foreground);
        searchField.setCaretColor(colors.foreground);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(colors.panel, 1),
                BorderFactory.createEmptyBorder(2, 5, 2, 5)));

        detailPanel.setBackground(colors.background);
    }

    private void applyContainerColors(ThemeColors colors) {
        Container contentPane = getContentPane();
        for (Component comp : contentPane.getComponents()) {
            if (comp instanceof Container container) {
                container.setBackground(colors.background);
                setComponentColors(container, colors);
            }
        }
    }

    private void applyMenuBarColors(ThemeColors colors) {
        if (getJMenuBar() != null) {
            getJMenuBar().setBackground(colors.panel);
            getJMenuBar().setOpaque(true);
            for (int i = 0; i < getJMenuBar().getMenuCount(); i++) {
                JMenu menu = getJMenuBar().getMenu(i);
                menu.setBackground(colors.panel);
                menu.setForeground(colors.foreground);
                menu.setOpaque(true);
            }
        }
    }

    private void setComponentColors(Container container, ThemeColors colors) {
        for (Component comp : container.getComponents()) {
            comp.setBackground(colors.background);
            comp.setForeground(colors.foreground);

            if (comp instanceof JButton button) {
                button.setBackground(colors.button);
                button.setOpaque(true);
                button.setBorderPainted(false);
                button.setFocusPainted(false);
            } else if (comp instanceof JLabel label) {
                label.setOpaque(true);
            } else if (comp instanceof JScrollPane scrollPane) {
                scrollPane.getViewport().setBackground(colors.background);
                if (scrollPane.getVerticalScrollBar() != null) {
                    scrollPane.getVerticalScrollBar().setBackground(colors.background);
                }
                if (scrollPane.getHorizontalScrollBar() != null) {
                    scrollPane.getHorizontalScrollBar().setBackground(colors.background);
                }
            }

            if (comp instanceof Container container1) {
                setComponentColors(container1, colors);
            }
        }
    }

    private static class ThemeColors {
        final Color background;
        final Color foreground;
        final Color panel;
        final Color button;

        ThemeColors(Color background, Color foreground, Color panel, Color button) {
            this.background = background;
            this.foreground = foreground;
            this.panel = panel;
            this.button = button;
        }
    }

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem exportImportItem = new JMenuItem("Export/Import...");
        exportImportItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK));
        exportImportItem.addActionListener(e -> showExportImportDialog());

        fileMenu.add(exportImportItem);
        menuBar.add(fileMenu);

        JMenu settingsMenu = new JMenu("Settings");
        JMenuItem preferencesItem = new JMenuItem("Preferences...");
        preferencesItem.addActionListener(e -> showSettingsDialog());

        settingsMenu.add(preferencesItem);
        menuBar.add(settingsMenu);
        setJMenuBar(menuBar);
    }

    private void showSettingsDialog() {
        new SettingsDialog(this, settings).setVisible(true);
        applyTheme();
        itemList.setCellRenderer(new ClipboardItemRenderer(settings));
        repaint();
    }

    private void showExportImportDialog() {
        ExportImportDialog dialog = new ExportImportDialog(this, db);
        dialog.setVisible(true);
        loadItems();
        showStatusMessage("Export/Import dialog closed");
    }

    private void toggleItemPin() {
        ClipboardItem selected = itemList.getSelectedValue();
        if (selected != null) {
            boolean success = db.toggleItemPin(selected.getHash());
            if (success) {
                // Update the item's pin status in memory
                selected.setPinned(!selected.isPinned());
                
                String action = selected.isPinned() ? "pinned" : "unpinned";
                showStatusMessage("Item " + action + " successfully");
                
                // Refresh the list to show updated pin status
                itemList.repaint();            } else {
                showStatusMessage("Could not update pin status");
                UserFriendlyErrors.logError("Pin toggle failed", "Failed to toggle pin status in database");
            }
        }
    }
    
    /**
     * Add demo controls for simulating clipboard operations
     */
    private void addDemoControls() {
        // Create demo control panel
        JPanel demoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        demoPanel.setBorder(BorderFactory.createTitledBorder("🎬 Demo Controls"));
        
        // Demo text input
        JTextField demoTextInput = new JTextField(20);
        demoTextInput.setToolTipText("Enter text to copy to virtual clipboard");
        
        JButton copyTextButton = new JButton("📝 Copy Text");
        copyTextButton.addActionListener(e -> {
            String text = demoTextInput.getText().trim();
            if (!text.isEmpty()) {
                simulateCopyText(text);
                demoTextInput.setText("");
            } else {
                showStatusMessage("⚠️ Please enter some text to copy");
            }
        });
        
        JButton copyImageButton = new JButton("🖼️ Copy Sample Image");
        copyImageButton.addActionListener(e -> simulateCopyImage());
        
        JButton pasteButton = new JButton("📋 Paste from Virtual Clipboard");
        pasteButton.addActionListener(e -> simulatePaste());
        
        demoPanel.add(new JLabel("Text:"));
        demoPanel.add(demoTextInput);
        demoPanel.add(copyTextButton);
        demoPanel.add(copyImageButton);
        demoPanel.add(pasteButton);
        
        // Add demo panel to the top of the window
        Container contentPane = getContentPane();
        if (contentPane.getLayout() instanceof BorderLayout) {
            JPanel topPanel = new JPanel(new BorderLayout());
            Component northComponent = ((BorderLayout) contentPane.getLayout()).getLayoutComponent(BorderLayout.NORTH);
            if (northComponent != null) {
                topPanel.add(northComponent, BorderLayout.NORTH);
            }
            topPanel.add(demoPanel, BorderLayout.SOUTH);
            contentPane.add(topPanel, BorderLayout.NORTH);
        }
        
        // Allow Enter key to trigger copy text
        demoTextInput.addActionListener(e -> copyTextButton.doClick());
        
        revalidate();
        repaint();
    }
      /**
     * Simulate copying text to virtual clipboard in demo mode
     */
    private void simulateCopyText(String text) {
        if (demoMode && demoClipboardManager != null) {
            try {
                // Use the demo clipboard manager to properly process the text
                String enrichedText = "[" + demoDeviceName + "] " + text;
                demoClipboardManager.copyTextToVirtualClipboard(enrichedText);
                showStatusMessage("📝 [" + demoDeviceName + "] Copied: " + text);
                  } catch (Exception ex) {
                showStatusMessage("❌ [" + demoDeviceName + "] Copy failed");
                UserFriendlyErrors.logError("Demo text copy failed", "Demo copy failed: " + ex.getMessage());
            }
        } else if (demoMode) {
            showStatusMessage("❌ Demo clipboard manager not available");
        }
    }
      /**
     * Simulate copying a sample image in demo mode
     */
    private void simulateCopyImage() {
        if (demoMode && demoClipboardManager != null) {
            try {
                // Create a simple sample image (colored rectangle with device name)
                java.awt.image.BufferedImage sampleImage = new java.awt.image.BufferedImage(200, 100, java.awt.image.BufferedImage.TYPE_INT_RGB);
                java.awt.Graphics2D g2d = sampleImage.createGraphics();
                
                // Fill with a gradient
                g2d.setColor(demoDeviceName.contains("Device-A") ? Color.BLUE : Color.GREEN);
                g2d.fillRect(0, 0, 200, 100);
                
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 16));
                g2d.drawString(demoDeviceName, 10, 30);
                g2d.drawString("Sample Image", 10, 50);
                g2d.drawString(java.time.LocalTime.now().toString().substring(0, 8), 10, 70);
                g2d.dispose();
                
                // Use the demo clipboard manager to properly process the image
                demoClipboardManager.copyImageToVirtualClipboard(sampleImage);
                showStatusMessage("🖼️ [" + demoDeviceName + "] Copied sample image");
                  } catch (Exception ex) {
                showStatusMessage("❌ [" + demoDeviceName + "] Image copy failed");
                UserFriendlyErrors.logError("Demo image copy failed", "Demo image copy failed: " + ex.getMessage());
            }
        } else if (demoMode) {
            showStatusMessage("❌ Demo clipboard manager not available");
        }
    }
    
    /**
     * Simulate pasting from virtual clipboard in demo mode
     */
    private void simulatePaste() {
        if (demoMode && !allItems.isEmpty()) {
            ClipboardItem latestItem = allItems.get(0);
            if (latestItem.getType() == com.cuhlippa.client.clipboard.ItemType.TEXT) {
                String content = new String(latestItem.getContent());
                showStatusMessage("📋 [" + demoDeviceName + "] Pasted: " + content);
            } else {
                showStatusMessage("📋 [" + demoDeviceName + "] Pasted " + latestItem.getType().toString().toLowerCase());
            }
        } else {
            showStatusMessage("📋 No items to paste");
        }
    }
}
