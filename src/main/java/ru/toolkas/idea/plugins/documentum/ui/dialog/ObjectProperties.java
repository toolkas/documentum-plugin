package ru.toolkas.idea.plugins.documentum.ui.dialog;

import com.documentum.fc.client.IDfFormat;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;


import org.apache.commons.lang.StringUtils;
import ru.toolkas.idea.plugins.documentum.docbase.Docbase;
import ru.toolkas.idea.plugins.documentum.docbase.DocbaseManager;
import ru.toolkas.idea.plugins.documentum.ui.commons.SimpleTableModelBuilder;
import ru.toolkas.idea.plugins.documentum.ui.status.IStatusManager;
import ru.toolkas.idea.plugins.documentum.ui.utils.JTableUtils;
import ru.toolkas.idea.plugins.documentum.utils.ConcurrentUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.*;
import java.util.List;

public class ObjectProperties extends DialogWrapper {
    private static final List<String> COLUMNS = Arrays.asList("Property", "Value");

    private JScrollPane pane;

    private IDfPersistentObject object = null;
    private File content = null;

    private final IStatusManager statusManager;

    public ObjectProperties(final DocbaseManager docbaseManager, final Project project, final IStatusManager statusManager, final IDfPersistentObject object) throws DfException {
        super(project, true);
        setModal(false);

        this.statusManager = statusManager;
        this.object = object;

        final JTable table = new JTable();

        pane = new JScrollPane(table);
        pane.setPreferredSize(new Dimension(500, 500));

        final List<String[]> values = new ArrayList<String[]>();

        setTitle(object.getType().getName() + '[' + object.getObjectId() + ']');

        final Set<String> names = new TreeSet<String>();
        for (int index = 0; index < object.getAttrCount(); index++) {
            names.add(object.getAttr(index).getName());
        }

        for (String name : names) {
            if (object.isAttrRepeating(name)) {
                for (int index = 0; index < object.getValueCount(name); index++) {
                    values.add(new String[]{name + '[' + index + ']', object.getRepeatingString(name, index)});
                }
            } else {
                values.add(new String[]{name, object.getString(name)});
            }
        }

        table.setModel(new SimpleTableModelBuilder(COLUMNS, values).createTableModel());
        JTableUtils.openObjectPropertiesOnDoubleClick(table, docbaseManager, project, statusManager);

        init();
    }

    private boolean isContentAccessible() {
        try {
            return object != null && object instanceof IDfSysObject && ((IDfSysObject) object).getContentSize() > 0 && StringUtils.isNotBlank(((IDfSysObject) object).getContentType());
        } catch (DfException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    protected JComponent createCenterPanel() {
        return pane;
    }

    @Override
    protected Action[] createActions() {
        return new Action[]{new SaveContentAction(), new ViewContentAction(), new CloseAction()};
    }

    public static void open(final DocbaseManager docbaseManager, final Project project, final IStatusManager statusManager, final String objectId) {
        if (DfId.isObjectId(objectId)) {
            ConcurrentUtils.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (docbaseManager.isCurrentDocbaseConnected()) {
                            docbaseManager.getCurrentDocbase().execute(new Docbase.Action() {
                                @Override
                                public void execute(IDfSession session) throws DfException {
                                    try {
                                        if (statusManager != null) {
                                            statusManager.showStatusText("Fetch object [" + objectId + "] properties");
                                        }
                                        final IDfPersistentObject object = session.getObject(new DfId(objectId));
                                        SwingUtilities.invokeLater(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    new ObjectProperties(docbaseManager, project, statusManager, object).show();
                                                } catch (DfException ex) {
                                                    throw new RuntimeException(ex);
                                                }
                                            }
                                        });
                                    } finally {
                                        if (statusManager != null) {
                                            statusManager.hideStatusText();
                                        }
                                    }
                                }
                            });
                        }
                    } catch (DfException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            });
        }
    }

    private class SaveContentAction extends AbstractAction {
        private SaveContentAction() {
            super("Save content");
            setEnabled(isContentAccessible());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                final IDfSysObject sysObject = (IDfSysObject) object;
                String formatName = sysObject.getContentType();
                IDfFormat format = sysObject.getSession().getFormat(formatName);
                final String ext = format.getDOSExtension();

                final JFileChooser chooser = new JFileChooser();
                chooser.setFileFilter(new FileNameExtensionFilter("*.".concat(ext), ext));

                int returnValue = chooser.showSaveDialog(pane);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    ConcurrentUtils.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (statusManager != null) {
                                    statusManager.showStatusText("Downloading object[" + object.getObjectId() + "] content");
                                }

                                File file = chooser.getSelectedFile();
                                if (!file.getName().endsWith("." + ext)) {
                                    file = new File(file.getParentFile(), file.getName().concat(".").concat(ext));
                                }
                                sysObject.getFile(file.getAbsolutePath());
                            } catch (DfException ex) {
                                throw new RuntimeException(ex);
                            } finally {
                                if (statusManager != null) {
                                    statusManager.hideStatusText();
                                }
                            }
                        }
                    });
                }
            } catch (DfException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private class ViewContentAction extends AbstractAction {
        private ViewContentAction() {
            super("View content");
            setEnabled(isContentAccessible());
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            try {
                if (content == null || !content.exists()) {
                    ConcurrentUtils.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (statusManager != null) {
                                    statusManager.showStatusText("Downloading object[" + object.getObjectId() + "] content");
                                }

                                IDfSysObject sysObject = (IDfSysObject) object;
                                String formatName = sysObject.getContentType();
                                IDfFormat format = sysObject.getSession().getFormat(formatName);

                                String ext = '.' + format.getDOSExtension();
                                String fileName = sysObject.getObjectName();
                                if (!fileName.endsWith(ext)) {
                                    fileName += ext;
                                }

                                content = new File(System.getProperty("java.io.tmpdir"), fileName);
                                content = new File(sysObject.getFile(content.getAbsolutePath()));
                                Desktop.getDesktop().open(content);
                            } catch (Exception ex) {
                                throw new RuntimeException(ex);
                            } finally {
                                if (statusManager != null) {
                                    statusManager.hideStatusText();
                                }
                            }
                        }
                    });
                } else {
                    Desktop.getDesktop().open(content);
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private class CloseAction extends AbstractAction {
        private CloseAction() {
            super("Close");
            putValue(DEFAULT_ACTION, true);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (content != null && content.exists()) {
                content.delete();
            }
            dispose();
        }
    }
}
