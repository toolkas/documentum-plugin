package ru.toolkas.idea.plugins.documentum.ui.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.apache.poi.hssf.usermodel.*;
import ru.toolkas.idea.plugins.documentum.icons.IconManager;
import ru.toolkas.idea.plugins.documentum.ui.status.IStatusManager;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;
import java.util.List;

public class ExportAction extends AsyncAction {
    private IStatusManager statusManager;
    private Component component;
    private final List<String> columns;
    private final List<String[]> values;

    private File file;

    public ExportAction(Component component, IStatusManager statusManager, List<String> columns, List<String[]> values) {
        super("Export");
        this.component = component;
        this.statusManager = statusManager;
        this.columns = columns;
        this.values = values;

        getTemplatePresentation().setIcon(IconManager.getIcon("excel.png"));
    }

    @Override
    protected boolean check() {
        final JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileFilter(new FileNameExtensionFilter("*.xls", "xls"));
        boolean ok = chooser.showSaveDialog(component) == JFileChooser.APPROVE_OPTION;
        if (ok) {
            file = chooser.getSelectedFile();
        }
        return ok;
    }

    @Override
    protected void doAction(AnActionEvent event) throws Exception {
        if (statusManager != null) statusManager.showStatusText("Exporting...");
        try {
            saveResults(file);
        } finally {
            if (statusManager != null) statusManager.hideStatusText();
        }

    }

    private void saveResults(File file) throws IOException {
        if (!file.getName().endsWith(".xls")) {
            file = new File(file.getParentFile(), file.getName().concat(".xls"));
        }

        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet();

        int rowIndex = 0;

        HSSFRow header = sheet.createRow(rowIndex++);

        for (int i = 0; i < columns.size(); i++) {
            String name = columns.get(i);
            HSSFCell cell = header.createCell((short) i);
            cell.setCellValue(new HSSFRichTextString(name));
        }

        for (String[] values : this.values) {
            HSSFRow row = sheet.createRow(rowIndex++);
            for (int index = 0; index < values.length; index++) {
                HSSFCell cell = row.createCell((short) index);
                cell.setCellValue(new HSSFRichTextString(values[index]));
            }
        }

        OutputStream output = null;
        try {
            output = new FileOutputStream(file);
            workbook.write(output);
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }
}
