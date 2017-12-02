package ru.toolkas.idea.plugins.documentum.ui.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.project.Project;
import ru.toolkas.idea.plugins.documentum.icons.IconManager;
import ru.toolkas.idea.plugins.documentum.ui.panel.PanelManager;
import ru.toolkas.idea.plugins.documentum.utils.ConcurrentUtils;

import javax.swing.*;

public class AddTabAction extends AnAction {
    private PanelManager.PanelType panelType;
    private Application application;
    private Project project;
    private boolean displayText;

    private boolean running = false;

    public AddTabAction(PanelManager.PanelType panelType, Application application, Project project, Icon icon, boolean displayText) {
        super(panelType.getLabel());
        this.panelType = panelType;
        this.application = application;
        this.project = project;

        if (icon == null) {
            icon = IconManager.getIcon("add.png");
        }
        this.displayText = displayText;

        getTemplatePresentation().setIcon(icon);
    }

    public AddTabAction(PanelManager.PanelType panelType, Application application, Project project) {
        this(panelType, application, project, null, true);
    }

    @Override
    public boolean displayTextInToolbar() {
        return displayText;
    }

    @Override
    public void update(AnActionEvent event) {
        super.update(event);

        event.getPresentation().setEnabled(!running);
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        ConcurrentUtils.execute(new Runnable() {
            @Override
            public void run() {
                if (!running) {
                    try {
                        running = true;
                        PanelManager.getInstance().addTab(panelType, application, project);
                    } finally {
                        running = false;
                    }
                }
            }
        });
    }
}
