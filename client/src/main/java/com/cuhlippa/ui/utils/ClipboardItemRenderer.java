package com.cuhlippa.ui.utils;

import com.cuhlippa.client.clipboard.ClipboardItem;
import com.cuhlippa.client.clipboard.ItemType;

import java.awt.Color;
import java.awt.Component;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;


public class ClipboardItemRenderer extends JLabel implements ListCellRenderer<ClipboardItem>{
    public ClipboardItemRenderer() {
        setOpaque(true);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends ClipboardItem> list, ClipboardItem item, int index, boolean isSelected, boolean cellHashFocus) {
        try {
            if (item.getType() == ItemType.IMAGE) {
                setText("[IMAGE]");
                setIcon(ImageUtils.createScaledImageIcon(item.getContent(), 64, 64));
            } else if (item.getType() == ItemType.TEXT) {
                setIcon(null);
                setText(item.toString());
            } else if (item.getType() == ItemType.FILE_PATH) {
                setIcon(null);
                setText("[FILE] " + item.toString());
            }

            if  (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
        } catch (IOException e) {
            e.printStackTrace();
            setIcon(null);
            setText("[Error loading content]");
            setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
            setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
        }

        return this;
    }
}
