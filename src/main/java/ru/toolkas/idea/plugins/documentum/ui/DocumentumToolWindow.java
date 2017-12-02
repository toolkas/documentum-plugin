package ru.toolkas.idea.plugins.documentum.ui;

import com.documentum.fc.common.DfException;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.ComboBoxAction;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;
import ru.toolkas.idea.plugins.documentum.components.application.DocumentumPlugin;
import ru.toolkas.idea.plugins.documentum.docbase.Docbase;
import ru.toolkas.idea.plugins.documentum.docbase.DocbaseManager;
import ru.toolkas.idea.plugins.documentum.icons.IconManager;
import ru.toolkas.idea.plugins.documentum.ui.actions.AddTabAction;
import ru.toolkas.idea.plugins.documentum.ui.panel.PanelManager;
import ru.toolkas.idea.plugins.documentum.ui.dialog.GlobalSettingsDialog;
import ru.toolkas.idea.plugins.documentum.ui.status.GlobalStatusManager;
import ru.toolkas.idea.plugins.documentum.utils.ConcurrentUtils;

import javax.swing.*;
import java.awt.*;

public class DocumentumToolWindow extends JPanel {
    private final Application application;
    private final Project project;

    private volatile boolean connecting = false;

    public DocumentumToolWindow(Application application, Project project) {
        this.application = application;
        this.project = project;

        setLayout(new BorderLayout());

        JTabbedPane tabs = new JTabbedPane();
        tabs.setTabPlacement(JTabbedPane.BOTTOM);

        add(createToolBar(), BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);

        add(GlobalStatusManager.getInstance().init(new JProgressBar()), BorderLayout.SOUTH);

        PanelManager.getInstance().setTabs(tabs);
        PanelManager.getInstance().addTab(PanelManager.PanelType.DQL, application, project);
    }

    private JComponent createToolBar() {
        DocbaseManager docbaseManager = application.getComponent(DocumentumPlugin.class).getDocbaseManager();

        DefaultActionGroup actionGroup = new DefaultActionGroup();
        actionGroup.add(new OpenFileAction());
        actionGroup.add(new SaveAction());
        actionGroup.addSeparator();
        actionGroup.add(new AddTabAction(PanelManager.PanelType.DQL, application, project));
        actionGroup.add(new AddTabAction(PanelManager.PanelType.API, application, project));
        actionGroup.addSeparator();
        actionGroup.add(new SelectDocbaseListAction(docbaseManager));
        actionGroup.addSeparator();
        actionGroup.add(new GlobalSettingsAction(docbaseManager));
        actionGroup.addSeparator();
        actionGroup.add(new ConnectAction(docbaseManager));
        actionGroup.add(new DisconnectAction(docbaseManager));
        actionGroup.addSeparator();
        actionGroup.add(new AddTabAction(PanelManager.PanelType.SCHEME, application, project, IconManager.getIcon("browser.png"), false));
        //actionGroup.add(new AddTabAction(PanelManager.PanelType.FILE_MANAGER, application, project, IconManager.getIcon("file_manager.png"), false));

        ActionManager actionManager = ActionManager.getInstance();
        ActionToolbar toolbar = actionManager.createActionToolbar("DocumentumPlugin.ToolWindow", actionGroup, true);
        return toolbar.getComponent();
    }

    private class OpenFileAction extends AnAction {
        private OpenFileAction() {
            super("Add");

            getTemplatePresentation().setIcon(IconManager.getIcon("open.png"));
        }

        @Override
        public void actionPerformed(AnActionEvent event) {
            Messages.showMessageDialog("not implements", "Dev Message", null);
        }
    }

    private class SaveAction extends AnAction {
        private SaveAction() {
            super("Save");

            getTemplatePresentation().setIcon(IconManager.getIcon("save.png"));
        }

        @Override
        public void actionPerformed(AnActionEvent event) {
            Messages.showMessageDialog("not implements", "Dev Message", null);
        }
    }

    private class SelectDocbaseListAction extends ComboBoxAction {
        private DocbaseManager docbaseManager;

        private SelectDocbaseListAction(DocbaseManager docbaseManager) {
            this.docbaseManager = docbaseManager;
        }

        @Override
        public void update(AnActionEvent event) {
            super.update(event);

            Presentation presentation = event.getPresentation();

            if (docbaseManager.getCurrentDocbase() == null) {
                presentation.setText("<Select docbase configuration>");
            } else {
                presentation.setText(docbaseManager.getCurrentDocbase().getName());
            }
        }

        @NotNull
        @Override
        protected DefaultActionGroup createPopupActionGroup(JComponent jComponent) {
            DefaultActionGroup actionGroup = new DefaultActionGroup();

            for (Docbase docbase : docbaseManager.getDocbases()) {
                actionGroup.add(new SelectDocbaseAction(docbaseManager, docbase));
            }
            actionGroup.addSeparator();
            actionGroup.add(new GlobalSettingsAction(docbaseManager));
            return actionGroup;
        }
    }

    private class SelectDocbaseAction extends AnAction {
        private final DocbaseManager docbaseManager;
        private final Docbase docbase;

        private SelectDocbaseAction(DocbaseManager docbaseManager, Docbase docbase) {
            super(docbase.getName());
            this.docbaseManager = docbaseManager;
            this.docbase = docbase;

            getTemplatePresentation().setIcon(IconManager.getIcon("docbase.gif"));
        }

        @Override
        public void actionPerformed(AnActionEvent event) {
            docbaseManager.setCurrentDocbase(docbase);
        }
    }

    private class GlobalSettingsAction extends AnAction {
        private final DocbaseManager docbaseManager;

        private GlobalSettingsAction(DocbaseManager docbaseManager) {
            super("Settings");

            this.docbaseManager = docbaseManager;

            getTemplatePresentation().setIcon(IconManager.getIcon("settings.png"));
        }

        @Override
        public void actionPerformed(AnActionEvent event) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        DialogWrapper wrapper = new GlobalSettingsDialog(project, docbaseManager);
                        wrapper.show();
                    } catch (DfException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            });
        }
    }

    private class ConnectAction extends AnAction {
        private final DocbaseManager docbaseManager;

        private ConnectAction(DocbaseManager docbaseManager) {
            super("Connect");

            this.docbaseManager = docbaseManager;
            getTemplatePresentation().setIcon(IconManager.getIcon("connect.png"));
        }

        @Override
        public void update(AnActionEvent event) {
            super.update(event);

            event.getPresentation().setEnabled(docbaseManager.getCurrentDocbase() != null && !docbaseManager.getCurrentDocbase().isConnected() && !connecting);
        }

        @Override
        public void actionPerformed(AnActionEvent event) {
            ConcurrentUtils.execute(new Runnable() {
                @Override
                public void run() {
                    if (!connecting) {
                        try {
                            connecting = true;
                            GlobalStatusManager.getInstance().showStatusText("Connecting...");
                            Docbase docbase = docbaseManager.getCurrentDocbase();
                            docbase.connect();
                        } catch (DfException ex) {
                            throw new RuntimeException(ex);
                        } finally {
                            connecting = false;
                            GlobalStatusManager.getInstance().hideStatusText();
                        }
                    }
                }
            });
        }
    }

    private class DisconnectAction extends AnAction {
        private final DocbaseManager docbaseManager;

        private DisconnectAction(DocbaseManager docbaseManager) {
            super("Disconnect");

            this.docbaseManager = docbaseManager;
            getTemplatePresentation().setIcon(IconManager.getIcon("disconnect.png"));
        }

        @Override
        public void actionPerformed(AnActionEvent event) {
            docbaseManager.getCurrentDocbase().disconnect();
        }

        @Override
        public void update(AnActionEvent event) {
            super.update(event);

            event.getPresentation().setEnabled(docbaseManager.getCurrentDocbase() != null && docbaseManager.getCurrentDocbase().isConnected() && !connecting);
        }
    }
}
