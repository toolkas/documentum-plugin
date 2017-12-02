package ru.toolkas.idea.plugins.documentum.components.project;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jetbrains.annotations.NotNull;
import ru.toolkas.idea.plugins.documentum.components.DocumentumPluginConstants;
import ru.toolkas.idea.plugins.documentum.ui.DocumentumToolWindow;
import ru.toolkas.idea.plugins.documentum.icons.IconManager;

public class DfProjectComponent implements ProjectComponent {
    private static final String NAME = DocumentumPluginConstants.NAME.concat(".project");
    public static final String DOCUMENTUM_TOOL_WINDOW = "Documentum";

    private Application application;
    private Project project;

    public DfProjectComponent(Application application, Project project) {
        this.application = application;
        this.project = project;
    }

    @Override
    public void projectOpened() {
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        ToolWindow toolWindow = toolWindowManager.registerToolWindow(DOCUMENTUM_TOOL_WINDOW, new DocumentumToolWindow(application, project), ToolWindowAnchor.BOTTOM);
        toolWindow.setTitle("Console");
        toolWindow.setIcon(IconManager.getIcon("documentum.png"));
    }

    @Override
    public void projectClosed() {
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        toolWindowManager.unregisterToolWindow(DOCUMENTUM_TOOL_WINDOW);
    }

    @Override
    public void initComponent() {
    }

    @Override
    public void disposeComponent() {
    }

    @NotNull
    @Override
    public String getComponentName() {
        return NAME;
    }
}
