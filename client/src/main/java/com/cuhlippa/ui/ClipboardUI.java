package com.cuhlippa.ui;

import javax.imageio.ImageIO;
import javax.swing.*;

import com.cuhlippa.client.storage.LocalDatabase;
import com.cuhlippa.client.clipboard.ClipboardItem;
import com.cuhlippa.client.clipboard.ItemType;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.List;

public class ClipboardUI extends JFrame {
    private final transient LocalDatabase db;
    private final DefaultListModel<ClipboardItem> listModel;
    private final JList<ClipboardItem> itemList;
    private final JTextArea detailArea;
    private final JLabel imageLabel;
    private final JPanel detailPanel;
    private final JScrollPane detailScrollPane;

    public ClipboardUI(LocalDatabase db) {
        super("Clipboard History");
        this.db = db;

        listModel = new DefaultListModel<>();
        itemList = new JList<>(listModel);
        detailArea = new JTextArea(10, 40);
        detailArea.setLineWrap(true);
        detailArea.setWrapStyleWord(true);
        detailArea.setEditable(false);

        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        setLayout(new BorderLayout());

        JScrollPane listScrollPane = new JScrollPane(itemList);

        detailPanel = new JPanel(new CardLayout());
        detailScrollPane = new JScrollPane(detailArea);
        JScrollPane imageScrollPane = new JScrollPane(imageLabel);

        detailPanel.add(detailScrollPane, "TEXT");
        detailPanel.add(imageScrollPane, "IMAGE");

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadItems());

        itemList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        itemList.addListSelectionListener(e -> showSelectedItemDetail());

        add(listScrollPane, BorderLayout.CENTER);
        add(detailPanel, BorderLayout.SOUTH);
        add(refreshButton, BorderLayout.NORTH);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        loadItems();
    }


    private void loadItems() {
        listModel.clear();
        List<ClipboardItem> items = db.getAllItems();
        for (ClipboardItem item : items) {
            listModel.addElement(item);
        }
    }

    private void showSelectedItemDetail() {
        ClipboardItem selected = itemList.getSelectedValue();
        CardLayout cardLayout = (CardLayout) detailPanel.getLayout();

        if (selected == null) {
            detailArea.setText("");
            imageLabel.setIcon(null);
            return;
        }

        if (selected.getType() == ItemType.TEXT || selected.getType() == ItemType.FILE_PATH) {
            detailArea.setText(new String(selected.getContent()));
            cardLayout.show(detailPanel, "TEXT");
        } else if (selected.getType() == ItemType.IMAGE) {
            try {
                byte[] imageData = selected.getContent();
                BufferedImage image  = ImageIO.read(new ByteArrayInputStream(imageData));

                if (image != null) {
                    int maxWidth = 400;
                    int maxHeight = 300;

                    if (image.getWidth() > maxWidth || image.getHeight() > maxHeight) {
                        Image scaledImage = image.getScaledInstance(maxWidth, maxHeight, Image.SCALE_SMOOTH);
                        imageLabel.setIcon(new ImageIcon(scaledImage));
                    } else {
                        imageLabel.setIcon(new ImageIcon(image));
                    }

                    cardLayout.show(detailPanel, "IMAGE");
                }
            } catch (Exception e) {
                detailArea.setText("Error loading image: " + e.getMessage());
                cardLayout.show(detailPanel, "TEXT");
            }
        }
    }
}
