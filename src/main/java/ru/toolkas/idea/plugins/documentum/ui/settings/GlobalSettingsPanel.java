package ru.toolkas.idea.plugins.documentum.ui.settings;

import javax.swing.*;

public abstract class GlobalSettingsPanel extends JPanel {
    public abstract String getTitle();

    public abstract void apply();
}
