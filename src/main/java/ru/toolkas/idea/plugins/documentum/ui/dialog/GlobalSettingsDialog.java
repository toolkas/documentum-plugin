package ru.toolkas.idea.plugins.documentum.ui.dialog;

import com.documentum.fc.common.DfException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import ru.toolkas.idea.plugins.documentum.docbase.DocbaseManager;
import ru.toolkas.idea.plugins.documentum.ui.settings.DocbasesPanel;
import ru.toolkas.idea.plugins.documentum.ui.settings.GlobalSettingsPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class GlobalSettingsDialog extends DialogWrapper {
    private JTabbedPane tabs;

    public GlobalSettingsDialog(Project project, DocbaseManager docbaseManager) throws DfException {
        super(project, true);
        setTitle("Settings");

        tabs = new JTabbedPane();
        addGlobalSettingsPanel(new DocbasesPanel(docbaseManager));

        init();
    }

    private void addGlobalSettingsPanel(GlobalSettingsPanel panel) {
        tabs.addTab(panel.getTitle(), panel);
    }

    @Override
    protected JComponent createCenterPanel() {
        return tabs;
    }

    @Override
    protected Action[] createActions() {
        return new Action[]{new ApplyAction(), new CancelAction()};
    }

    private class ApplyAction extends AbstractAction {
        private ApplyAction() {
            super("Ok");

            putValue(DEFAULT_ACTION, true);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            for (int index = 0; index < tabs.getTabCount(); index++) {
                GlobalSettingsPanel panel = (GlobalSettingsPanel) tabs.getComponentAt(index);
                panel.apply();
            }

            dispose();
        }
    }

    private class CancelAction extends AbstractAction {
        private CancelAction() {
            super("Cancel");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            dispose();
        }
    }
}
