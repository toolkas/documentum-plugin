package ru.toolkas.idea.plugins.documentum.ui.commons;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.util.ArrayList;
import java.util.List;

public class SimpleTableModelBuilder {
    private List<String> columns = new ArrayList<String>();
    private List<String[]> values = new ArrayList<String[]>();

    public SimpleTableModelBuilder(List<String> columns, List<String[]> values) {
        this.columns = columns;
        this.values = values;
    }

    public TableModel createTableModel() {
        return new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Object getValueAt(int row, int column) {
                String[] values = SimpleTableModelBuilder.this.values.get(row);
                return values[column];
            }

            @Override
            public int getRowCount() {
                return values.size();
            }

            @Override
            public String getColumnName(int column) {
                return columns.get(column);
            }

            @Override
            public int getColumnCount() {
                return columns.size();
            }
        };
    }
}
