package ru.toolkas.idea.plugins.documentum.ui.panel;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.project.Project;
import ru.toolkas.idea.plugins.documentum.components.application.DocumentumPlugin;
import ru.toolkas.idea.plugins.documentum.docbase.DocbaseManager;
import ru.toolkas.idea.plugins.documentum.ui.status.IStatusManager;

import javax.swing.*;
import java.awt.*;

public abstract class AbstractPanel extends JPanel implements IStatusManager {
    private final Application application;
    private final Project project;

    private JProgressBar progress = new JProgressBar();

    public AbstractPanel(Application application, Project project) {
        this.application = application;
        this.project = project;
    }

    protected void init() {
        setLayout(new BorderLayout());

        progress.setIndeterminate(true);
        progress.setStringPainted(true);
        progress.setVisible(false);

        add(createToolBar(), BorderLayout.WEST);
        add(createMainComponent(), BorderLayout.CENTER);
        add(progress, BorderLayout.SOUTH);
    }

    protected JComponent createToolBar() {
        DefaultActionGroup actionGroup = new DefaultActionGroup("DocumentumPlugin." + getClass().getName(), false);

        addToolBarActions(actionGroup);

        ActionManager actionManager = ActionManager.getInstance();
        ActionToolbar toolbar = actionManager.createActionToolbar("DocumentumPlugin" + getClass().getName(), actionGroup, false);
        return toolbar.getComponent();
    }

    protected void addToolBarActions(DefaultActionGroup actionGroup) {

    }

    protected abstract JComponent createMainComponent();

    public void showStatusText(final String message) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                progress.setString(message != null ? message : "");
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

    protected DocbaseManager getDocbaseManager() {
        return application.getComponent(DocumentumPlugin.class).getDocbaseManager();
    }

    public Application getApplication() {
        return application;
    }

    protected Project getProject() {
        return project;
    }

    protected boolean isDocbaseSelectedAndConnected() {
        return getDocbaseManager().isCurrentDocbaseConnected();
    }
}
