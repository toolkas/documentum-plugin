package ru.toolkas.idea.plugins.documentum.ui.utils;

import com.intellij.openapi.project.Project;
import ru.toolkas.idea.plugins.documentum.docbase.DocbaseManager;
import ru.toolkas.idea.plugins.documentum.ui.dialog.ObjectProperties;
import ru.toolkas.idea.plugins.documentum.ui.status.IStatusManager;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class JTableUtils {
    private JTableUtils() {
    }

    public static void openObjectPropertiesOnDoubleClick(JTable table, final DocbaseManager docbaseManager, final Project project, final IStatusManager statusManager) {
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    JTable table = (JTable) event.getSource();
                    int rowIndex = table.getSelectedRow();
                    int columnIndex = table.getSelectedColumn();

                    final String value = (String) table.getModel().getValueAt(rowIndex, columnIndex);
                    ObjectProperties.open(docbaseManager, project, statusManager, value);
                }
            }
        });
    }
}
