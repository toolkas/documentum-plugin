package ru.toolkas.idea.plugins.documentum.icons;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

public class IconManager {
    public static Icon getIcon(String name) {
        try {
            return getIcon(IconManager.class.getResourceAsStream(name));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static ImageIcon getIcon(InputStream input) throws IOException {
        byte buffer[] = new byte[input.available()];
        for (int i = 0, n = input.available(); i < n; i++) {
            buffer[i] = (byte) input.read();
        }

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Image image = toolkit.createImage(buffer);
        return new ImageIcon(image);
    }
}
