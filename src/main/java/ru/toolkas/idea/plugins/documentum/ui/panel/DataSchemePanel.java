package ru.toolkas.idea.plugins.documentum.ui.panel;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfType;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfAttr;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.project.Project;
import ru.toolkas.idea.plugins.documentum.docbase.Docbase;
import ru.toolkas.idea.plugins.documentum.docbase.LDmType;
import ru.toolkas.idea.plugins.documentum.docbase.ODmType;
import ru.toolkas.idea.plugins.documentum.ui.actions.ClosePanelAction;
import ru.toolkas.idea.plugins.documentum.ui.commons.SimpleTableModelBuilder;
import ru.toolkas.idea.plugins.documentum.ui.commons.TreeModelWithoutLeafs;
import ru.toolkas.idea.plugins.documentum.ui.status.GlobalStatusManager;
import ru.toolkas.idea.plugins.documentum.utils.ConcurrentUtils;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class DataSchemePanel extends AbstractPanel {
    private static final List<String> COLUMNS = Arrays.asList("Attribute", "Data type", "Repeating", "Length");
    private static final String DOCBASE_TYPES_TREE_ROOT = "Docbase types";
    private final java.util.List<String[]> values = new ArrayList<String[]>();

    public DataSchemePanel(Application application, Project project) {
        super(application, project);

        init();
    }

    @Override
    protected JComponent createMainComponent() {
        final JTable table = new JTable();
        final JTree tree = createTree();
        tree.addTreeSelectionListener(new DocbaseTypesTreeListener(tree, table));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setOneTouchExpandable(true);
        split.setTopComponent(new JScrollPane(tree));
        split.setBottomComponent(new JScrollPane(table));
        split.setDividerLocation(300);

        JPanel main = new JPanel(new BorderLayout());
        main.add(split, BorderLayout.CENTER);

        return main;
    }

    @Override
    protected void addToolBarActions(DefaultActionGroup actionGroup) {
        super.addToolBarActions(actionGroup);
        actionGroup.add(new ClosePanelAction(this));
    }

    private JTree createTree() {
        final DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(DOCBASE_TYPES_TREE_ROOT);
        initRootNode(rootNode);
        final JTree tree = new JTree(new TreeModelWithoutLeafs(rootNode));
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        return tree;
    }

    private void initRootNode(DefaultMutableTreeNode rootNode) {
        try {
            GlobalStatusManager.getInstance().showStatusText("Fetch docbase types");
            if (isDocbaseSelectedAndConnected()) {
                LDmType dmType = getDocbaseManager().getCurrentDocbase().getDmType();

                ODmType root = dmType.getDmType("");
                addNodes(dmType, rootNode, root);
            }
        } catch (DfException ex) {
            throw new RuntimeException(ex);
        } finally {
            GlobalStatusManager.getInstance().hideStatusText();
        }
    }

    private void addNodes(LDmType dmType, final DefaultMutableTreeNode node, ODmType type) {
        for (String name : type.getChildren()) {
            ODmType child = dmType.getDmType(name);
            final DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(child);
            addNodes(dmType, childNode, child);
            node.add(childNode);
        }
    }

    private class DocbaseTypesTreeListener implements TreeSelectionListener {
        private final JTree tree;
        private final JTable table;

        private DocbaseTypesTreeListener(JTree tree, JTable table) {
            this.tree = tree;
            this.table = table;
        }

        @Override
        public void valueChanged(TreeSelectionEvent e) {
            values.clear();
            if (tree.getSelectionCount() > 0 && isDocbaseSelectedAndConnected()) {
                TreePath path = tree.getSelectionPath();
                Object object = ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();
                if (!DOCBASE_TYPES_TREE_ROOT.equals(object)) {
                    ODmType type = (ODmType) object;
                    updateTypeProperties(type);
                }
            }
            table.setModel(new SimpleTableModelBuilder(COLUMNS, values).createTableModel());
        }
    }

    private void updateTypeProperties(final ODmType oDmType) {
        ConcurrentUtils.execute(new DocbaseTypeAttributesUpdater(oDmType));
    }

    private class DocbaseTypeAttributesUpdater implements Runnable {
        private final ODmType oDmType;

        private DocbaseTypeAttributesUpdater(ODmType oDmType) {
            this.oDmType = oDmType;
        }

        @Override
        public void run() {
            try {
                showStatusText("Fetch type attributes");
                getDocbaseManager().getCurrentDocbase().execute(new Docbase.Action() {
                    @Override
                    public void execute(IDfSession session) throws DfException {
                        IDfType type = session.getType(oDmType.getName());

                        for (int index = 0; index < type.getTypeAttrCount(); index++) {
                            IDfAttr attribute = type.getTypeAttr(index);

                            String dataType;
                            switch (attribute.getDataType()) {
                                case IDfAttr.DM_BOOLEAN:
                                    dataType = "boolean";
                                    break;
                                case IDfAttr.DM_INTEGER:
                                    dataType = "integer";
                                    break;
                                case IDfAttr.DM_STRING:
                                    dataType = "string";
                                    break;
                                case IDfAttr.DM_ID:
                                    dataType = "id";
                                    break;
                                case IDfAttr.DM_TIME:
                                    dataType = "time";
                                    break;
                                case IDfAttr.DM_DOUBLE:
                                    dataType = "double";
                                    break;
                                default:
                                    dataType = "default";
                            }

                            String[] values = new String[]{attribute.getName(), dataType, attribute.isRepeating() ? "T" : "F", String.valueOf(attribute.getLength())};
                            DataSchemePanel.this.values.add(values);
                        }
                    }
                });
            } catch (DfException ex) {
                throw new RuntimeException(ex);
            } finally {
                hideStatusText();
            }
        }
    }
}
