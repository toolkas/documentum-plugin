package ru.toolkas.idea.plugins.documentum.ui.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import ru.toolkas.idea.plugins.documentum.icons.IconManager;
import ru.toolkas.idea.plugins.documentum.ui.panel.PanelManager;

import java.awt.*;

public class ClosePanelAction extends AnAction {
    private Component component;
    public ClosePanelAction(Component component) {
        super("Close");
        this.component = component;
        getTemplatePresentation().setIcon(IconManager.getIcon("close.png"));
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        PanelManager.getInstance().removeTab(component);
    }
}
