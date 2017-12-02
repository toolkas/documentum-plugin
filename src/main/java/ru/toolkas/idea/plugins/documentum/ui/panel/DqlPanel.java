package ru.toolkas.idea.plugins.documentum.ui.panel;

import com.documentum.fc.client.IDfTypedObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfAttr;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.project.Project;
import org.apache.commons.lang.StringUtils;
import ru.toolkas.idea.plugins.documentum.docbase.Docbase;
import ru.toolkas.idea.plugins.documentum.icons.IconManager;
import ru.toolkas.idea.plugins.documentum.ui.actions.AsyncAction;
import ru.toolkas.idea.plugins.documentum.ui.actions.ClosePanelAction;
import ru.toolkas.idea.plugins.documentum.ui.actions.ExportAction;
import ru.toolkas.idea.plugins.documentum.ui.commons.SimpleTableModelBuilder;
import ru.toolkas.idea.plugins.documentum.ui.utils.JTableUtils;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class DqlPanel extends AbstractPanel {
    private final List<String[]> values = new ArrayList<String[]>();
    private final List<String> columns = new ArrayList<String>();

    private JTextPane query = new JTextPane();
    private JTable results = new JTable();

    public DqlPanel(Application application, final Project project) {
        super(application, project);

        init();
    }

    @Override
    protected JComponent createMainComponent() {
        query.setEditable(true);

        results.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JTableUtils.openObjectPropertiesOnDoubleClick(results, getDocbaseManager(), getProject(), this);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setOneTouchExpandable(true);
        splitPane.setTopComponent(new JScrollPane(query));
        splitPane.setBottomComponent(new JScrollPane(results));
        splitPane.setDividerLocation(100);

        JPanel main = new JPanel(new BorderLayout());
        main.add(splitPane, BorderLayout.CENTER);

        return main;
    }

    @Override
    protected void addToolBarActions(DefaultActionGroup actionGroup) {
        super.addToolBarActions(actionGroup);

        actionGroup.add(new ExecuteAction());
        actionGroup.add(new ExportAction(this, this, columns, values));
        actionGroup.add(new ClosePanelAction(this));
    }

    private class ExecuteAction extends AsyncAction {
        private ExecuteAction() {
            super("Run");
            getTemplatePresentation().setIcon(IconManager.getIcon("execute.gif"));
        }

        @Override
        protected void doAction(AnActionEvent event) throws Exception {
            execute(query.getText());
        }

        @Override
        protected boolean isEnabled() {
            return super.isEnabled() && isDocbaseSelectedAndConnected() && StringUtils.isNotBlank(query.getText());
        }
    }

    private void execute(final String dql) throws DfException {
        try {
            showStatusText("Running query");
            fetch(getDocbaseManager().getCurrentDocbase(), dql);
        } finally {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    results.setModel(new SimpleTableModelBuilder(columns, values).createTableModel());
                    hideStatusText();
                }
            });
        }
    }

    private void fetch(final Docbase docbase, final String dql) throws DfException {
        columns.clear();
        values.clear();

        docbase.select(dql, new Docbase.Processor() {
            @Override
            public void init(IDfTypedObject object) throws DfException {
                for (int index = 0; index < object.getAttrCount(); index++) {
                    IDfAttr attribute = object.getAttr(index);
                    columns.add(attribute.getName());
                }
            }

            @Override
            public boolean process(IDfTypedObject object) throws DfException {
                String[] values = new String[columns.size()];
                for (int index = 0; index < object.getAttrCount(); index++) {
                    IDfAttr attribute = object.getAttr(index);
                    if (attribute.isRepeating()) {
                        List<String> list = new ArrayList<String>();
                        for (int j = 0; j < object.getValueCount(attribute.getName()); j++) {
                            list.add(object.getRepeatingString(attribute.getName(), j));
                        }
                        values[index] = StringUtils.join(list, ",");
                    } else {
                        values[index] = object.getString(attribute.getName());
                    }
                }

                DqlPanel.this.values.add(values);
                return true;
            }
        });
    }
}
