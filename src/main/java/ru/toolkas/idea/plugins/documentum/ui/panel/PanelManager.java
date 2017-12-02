package ru.toolkas.idea.plugins.documentum.ui.panel;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.project.Project;

import javax.swing.*;
import java.awt.*;

public class PanelManager {
    private static final PanelManager instance = new PanelManager();

    private JTabbedPane tabs;
    private int counter = 0;

    private PanelManager() {
    }

    public void setTabs(JTabbedPane tabs) {
        this.tabs = tabs;
    }

    public void addTab(final PanelType type, final Application application, final Project project) {
        Component tab = null;
        switch (type) {
            case SCHEME:
                tab = new DataSchemePanel(application, project);
                break;
            case DQL:
                tab = new DqlPanel(application, project);
                break;
            case API:
                tab = new ApiPanel(application, project);
                break;
        }

        final Component finalTab = tab;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                tabs.add(getTabName(type.name()), finalTab);
                tabs.setSelectedComponent(finalTab);
            }
        });

    }

    public void removeTab(final Component component) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                int index = tabs.indexOfComponent(component);
                if (index != -1) {
                    tabs.removeTabAt(index);
                }
            }
        });
    }

    private String getTabName(String name) {
        return name + ' ' + (++counter);
    }

    public static PanelManager getInstance() {
        return instance;
    }

    public static enum PanelType {
        SCHEME("Data scheme"),
        DQL("DQL"),
        API("API");

        private String label;

        private PanelType(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }
}
