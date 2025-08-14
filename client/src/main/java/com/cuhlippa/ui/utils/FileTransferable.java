package com.cuhlippa.ui.utils;

import java.awt.datatransfer.*;
import java.io.File;
import java.util.List;

public class FileTransferable implements Transferable {
    private final List<File> files;
    
    public FileTransferable(List<File> files) {
        this.files = files;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{DataFlavor.javaFileListFlavor};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return DataFlavor.javaFileListFlavor.equals(flavor);
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
        if (!isDataFlavorSupported(flavor)) throw new UnsupportedFlavorException(flavor);
        return files;
    }
}
