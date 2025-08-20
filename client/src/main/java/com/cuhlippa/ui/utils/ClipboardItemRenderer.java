package com.cuhlippa.ui.utils;

import com.cuhlippa.client.clipboard.ClipboardItem;
import com.cuhlippa.client.clipboard.ItemType;
import com.cuhlippa.client.config.Settings;

import java.awt.Color;
import java.awt.Component;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;


public class ClipboardItemRenderer extends JLabel implements ListCellRenderer<ClipboardItem>{
    private final transient Settings settings;
    
    public ClipboardItemRenderer(Settings settings) {
        this.settings = settings;
        setOpaque(true);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
    }    @Override
    public Component getListCellRendererComponent(JList<? extends ClipboardItem> list, ClipboardItem item, int index, boolean isSelected, boolean cellHashFocus) {        
        try {
            String displayText = "";
            
            if (item.getType() == ItemType.IMAGE) {
                displayText = "[IMAGE]";
                int size = settings.getThumbnailSize();
                setIcon(ImageUtils.createScaledImageIcon(item.getContent(), size, size));
            } else if (item.getType() == ItemType.TEXT) {
                setIcon(null);
                displayText = item.toString();
            } else if (item.getType() == ItemType.FILE_PATH) {
                setIcon(null);
                displayText = "[FILE] " + item.toString();
            }
            
            // Add pin indicator
            if (item.isPinned()) {
                displayText = "📌 " + displayText;
            }
            
            setText(displayText);

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
