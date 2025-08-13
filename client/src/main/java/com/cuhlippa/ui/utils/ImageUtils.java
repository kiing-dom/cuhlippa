package com.cuhlippa.ui.utils;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class ImageUtils {
    private static final int DEFAULT_MAX_WIDTH = 800;
    private static final int DEFAULT_MAX_HEIGHT = 600;

    private ImageUtils() {}

    public static ImageIcon createScaledImageIcon(byte[] imageData) throws IOException {
        return createScaledImageIcon(imageData, DEFAULT_MAX_WIDTH, DEFAULT_MAX_HEIGHT);
    }

    public static ImageIcon createScaledImageIcon(byte[] imageData, int maxWidth, int maxHeight) throws IOException {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData));

        if (image == null) {
            throw new IOException("Unable to read image data");
        }

        if (image.getWidth() > maxWidth || image.getHeight() > maxHeight) {
            Image scaledImage = image.getScaledInstance(maxWidth, maxHeight, Image.SCALE_SMOOTH);
            return new ImageIcon(scaledImage);
        } else {
            return new ImageIcon(image);
        }
    }
}
