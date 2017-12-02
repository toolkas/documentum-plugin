package ru.toolkas.idea.plugins.documentum.ui.status;

import javax.swing.*;

public class GlobalStatusManager implements IStatusManager {
    private static final GlobalStatusManager instance = new GlobalStatusManager();

    private JProgressBar progress = null;

    private GlobalStatusManager() {
    }

    public JProgressBar init(JProgressBar progress) {
        this.progress = progress;
        progress.setStringPainted(true);
        progress.setVisible(false);
        return progress;
    }

    public void showStatusText(final String message) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                progress.setIndeterminate(true);
                progress.setString(message);
                progress.setVisible(true);
            }
        });
    }

    public void hideStatusText() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                progress.setString("");
                progress.setVisible(false);
            }
        });
    }

    public static GlobalStatusManager getInstance() {
        return instance;
    }
}
