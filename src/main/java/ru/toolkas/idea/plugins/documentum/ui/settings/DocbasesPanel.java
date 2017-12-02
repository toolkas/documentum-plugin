package ru.toolkas.idea.plugins.documentum.ui.settings;

import com.documentum.fc.common.DfException;
import com.intellij.openapi.actionSystem.*;
import com.intellij.ui.DocumentAdapter;
import org.apache.commons.beanutils.BeanUtils;
import ru.toolkas.idea.plugins.documentum.docbase.Docbase;
import ru.toolkas.idea.plugins.documentum.docbase.DocbaseManager;
import ru.toolkas.idea.plugins.documentum.icons.IconManager;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.Document;
import java.awt.*;

public class DocbasesPanel extends GlobalSettingsPanel {
    private final PropertiesTableModel dfcPropertiesModel = new PropertiesTableModel();

    private final DocbaseConfigurationsModel docbaseConfigurationsModel = new DocbaseConfigurationsModel();
    private final JList docbaseConfigurationsList = new JList();

    private final JTextField configurationNameField = new JTextField();
    private final JTextField docbaseNameField = new JTextField();
    private final JTextField loginField = new JTextField();
    private final JTextField passwordField = new JTextField();

    private Docbase docbase = null;
    private DocbaseManager docbaseManager;

    public DocbasesPanel(DocbaseManager docbaseManager) throws DfException {
        this.docbaseManager = docbaseManager;

        JPanel left = createLeftPanel();
        JPanel right = createRightPanel();

        setLayout(new GridBagLayout());
        add(left, new GridBagConstraints(0, 0, 1, 1, 0.3, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(3, 3, 3, 3), 0, 0));
        add(right, new GridBagConstraints(1, 0, 1, 1, 0.6, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(3, 3, 3, 3), 0, 0));

        updateControlsOnSelectConfiguration();
    }

    private JPanel createLeftPanel() {
        initDocbaseConfigurationsList();

        JScrollPane pnlDatabases = new JScrollPane(docbaseConfigurationsList);
        pnlDatabases.setPreferredSize(new Dimension(300, 500));
        pnlDatabases.setMinimumSize(new Dimension(100, 100));

        JPanel left = new JPanel(new BorderLayout());
        left.add(createToolBar(), BorderLayout.NORTH);
        left.add(pnlDatabases, BorderLayout.CENTER);
        return left;
    }

    private void initDocbaseConfigurationsList() {
        initDocbaseConfigurationsModel();

        docbaseConfigurationsList.setModel(docbaseConfigurationsModel);
        docbaseConfigurationsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        docbaseConfigurationsList.setCellRenderer(new DocbaseRenderer());
        docbaseConfigurationsList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent event) {
                docbase = (Docbase) docbaseConfigurationsList.getSelectedValue();
                updateControlsOnSelectConfiguration();
            }
        });
    }

    private void initDocbaseConfigurationsModel() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    for (Docbase docbase : docbaseManager.getDocbases()) {
                        docbaseConfigurationsModel.addElement(docbase.clone());
                    }
                } catch (CloneNotSupportedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    private JComponent createToolBar() {
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        actionGroup.add(new AddDocbaseAction());
        actionGroup.add(new RemoveDocbaseAction());

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("ConnectionsPanel", actionGroup, true);
        return toolbar.getComponent();
    }

    private JPanel createRightPanel() {
        configurationNameField.getDocument().addDocumentListener(new DocbaseConfigurationFieldListener("name"));
        docbaseNameField.getDocument().addDocumentListener(new DocbaseConfigurationFieldListener("docbaseName"));
        loginField.getDocument().addDocumentListener(new DocbaseConfigurationFieldListener("login"));
        passwordField.getDocument().addDocumentListener(new DocbaseConfigurationFieldListener("password"));

        JPanel panel = new JPanel(new BorderLayout());
        JPanel authentication = new JPanel(new GridBagLayout());
        int i = 0;
        authentication.add(new JLabel("Docbase:"), new GridBagConstraints(0, i, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
        authentication.add(docbaseNameField, new GridBagConstraints(1, i, 1, 1, 0.5, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
        authentication.add(new JLabel("Login:"), new GridBagConstraints(0, ++i, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
        authentication.add(loginField, new GridBagConstraints(1, i, 1, 1, 0.5, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
        authentication.add(new JLabel("Password:"), new GridBagConstraints(0, ++i, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
        authentication.add(passwordField, new GridBagConstraints(1, i, 1, 1, 0.5, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
        panel.add(authentication, BorderLayout.NORTH);


        JTabbedPane tabs = new JTabbedPane();
        tabs.setTabPlacement(JTabbedPane.BOTTOM);
        tabs.setPreferredSize(new Dimension(500, 500));
        tabs.addTab("Authentication", new JScrollPane(panel));
        tabs.addTab("Properties", new JScrollPane(new JTable(dfcPropertiesModel)));

        JPanel pnlProperties = new JPanel(new GridBagLayout());
        pnlProperties.setBorder(new EtchedBorder());
        pnlProperties.add(new JLabel("Configuration:"), new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(7, 4, 2, 4), 0, 0));
        pnlProperties.add(tabs, new GridBagConstraints(0, 3, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(2, 4, 2, 4), 0, 0));

        JPanel right = new JPanel(new GridBagLayout());
        i = 0;
        right.add(new JLabel("Name:"), new GridBagConstraints(0, i, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
        right.add(configurationNameField, new GridBagConstraints(1, i, 1, 1, 0.5, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
        right.add(pnlProperties, new GridBagConstraints(0, ++i, 4, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
        return right;
    }

    private void updateControlsOnSelectConfiguration() {
        configurationNameField.setText(docbase != null ? docbase.getName() : null);
        docbaseNameField.setText(docbase != null ? docbase.getDocbaseName() : null);
        loginField.setText(docbase != null ? docbase.getLogin() : null);
        passwordField.setText(docbase != null ? docbase.getPassword() : null);

        dfcPropertiesModel.fireTableDataChanged();
    }

    private class AddDocbaseAction extends AnAction {
        private AddDocbaseAction() {
            super("Add docbase");

            getTemplatePresentation().setIcon(IconManager.getIcon("add.png"));
        }

        @Override
        public void actionPerformed(AnActionEvent event) {
            Docbase docbase = new Docbase();
            docbase.setName("New docbase");

            docbaseConfigurationsModel.addElement(docbase);
            docbaseConfigurationsList.setSelectedValue(docbase, true);

            updateControlsOnSelectConfiguration();
        }
    }

    private class RemoveDocbaseAction extends AnAction {
        private RemoveDocbaseAction() {
            super("Remove docbase");

            getTemplatePresentation().setIcon(IconManager.getIcon("remove.png"));
        }

        @Override
        public void actionPerformed(AnActionEvent event) {
            docbaseConfigurationsModel.removeElement(docbase);
        }

        @Override
        public void update(AnActionEvent event) {
            super.update(event);

            event.getPresentation().setEnabled(docbase != null);
        }
    }

    private class DocbaseRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Docbase docbase = (Docbase) value;

            setText(docbase.getName());

            if (isSelected) {
                setBackground(new Color(0, 0, 178));
                setForeground(Color.white);
            } else {
                setBackground(Color.white);
                setForeground(Color.black);
            }

            return this;
        }
    }

    private class PropertiesTableModel extends DefaultTableModel {
        private static final String KEY_NAME = "Property";
        private static final int KEY_INDEX = 0;

        private static final String VALUE_NAME = "Value";
        private static final int VALUE_INDEX = 1;

        @Override
        public Object getValueAt(int row, int column) {
            if (docbase != null) {
                switch (column) {
                    case KEY_INDEX:
                        return docbase.getDfcProperties().get(row).getName();
                    case VALUE_INDEX:
                        return docbase.getDfcProperties().get(row).getValue();
                }
            }
            return null;
        }

        @Override
        public void setValueAt(Object object, int row, int column) {
            if (docbase != null) {
                if (column == VALUE_INDEX) {
                    String value = (String) object;
                    java.util.List<Docbase.DfcProperty> properties = docbase.getDfcProperties();
                    Docbase.DfcProperty property = properties.get(row);
                    property.setValue(value);
                }
            }
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            if (column == VALUE_INDEX) {
                java.util.List<Docbase.DfcProperty> properties = docbase.getDfcProperties();
                Docbase.DfcProperty property = properties.get(row);
                return property.isModifiable();
            }
            return false;
        }

        @Override
        public int getRowCount() {
            if (docbase != null) {
                return docbase.getDfcProperties().size();
            }
            return 0;
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
                case KEY_INDEX:
                    return KEY_NAME;
                case VALUE_INDEX:
                    return VALUE_NAME;
            }
            return null;
        }

        @Override
        public int getColumnCount() {
            return 2;
        }
    }

    private class DocbaseConfigurationsModel extends DefaultListModel {
        public void fireContentsChanged(int index) {
            super.fireContentsChanged(this, index, index);
        }
    }

    private class DocbaseConfigurationFieldListener extends DocumentAdapter {
        private final String property;

        private DocbaseConfigurationFieldListener(String property) {
            this.property = property;
        }

        @Override
        protected void textChanged(DocumentEvent event) {
            try {
                Document document = event.getDocument();
                String value = document.getText(0, document.getLength());
                if (docbase != null) {
                    BeanUtils.setProperty(docbase, property, value);
                    int index = docbaseConfigurationsList.getSelectedIndex();
                    docbaseConfigurationsModel.fireContentsChanged(index);
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    @Override
    public String getTitle() {
        return "Docbases";
    }

    @Override
    public void apply() {
        docbaseManager.clearDocbases();
        docbaseManager.setCurrentDocbase(null);
        for (int index = 0; index < docbaseConfigurationsModel.size(); index++) {
            Docbase docbase = (Docbase) docbaseConfigurationsModel.get(index);
            docbaseManager.addDocbase(docbase);
        }
    }
}
