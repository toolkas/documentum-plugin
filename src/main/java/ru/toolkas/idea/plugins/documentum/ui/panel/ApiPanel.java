package ru.toolkas.idea.plugins.documentum.ui.panel;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfList;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.project.Project;
import org.apache.commons.lang.StringUtils;
import ru.toolkas.idea.plugins.documentum.docbase.Docbase;
import ru.toolkas.idea.plugins.documentum.icons.IconManager;
import ru.toolkas.idea.plugins.documentum.ui.actions.AsyncAction;
import ru.toolkas.idea.plugins.documentum.ui.actions.ClosePanelAction;
import ru.toolkas.idea.plugins.documentum.utils.ConcurrentUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.StringTokenizer;

public class ApiPanel extends AbstractPanel {
    private final JTextField commandField = new JTextField();
    private final JTextArea resultTextArea = new JTextArea();

    private volatile boolean running = false;

    public ApiPanel(Application application, Project project) {
        super(application, project);

        init();
    }

    @Override
    protected JComponent createMainComponent() {
        commandField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent event) {
                if (event.getKeyChar() == '\n' && StringUtils.isNotBlank(commandField.getText()) && !running) {
                    final Docbase docbase = getDocbaseManager().getCurrentDocbase();
                    if (docbase != null && docbase.isConnected()) {
                        ConcurrentUtils.execute(new CommandExecutor());
                    }
                }
            }
        });

        JPanel main = new JPanel(new BorderLayout(0, 5));
        main.add(commandField, BorderLayout.NORTH);
        main.add(new JScrollPane(resultTextArea), BorderLayout.CENTER);
        return main;
    }

    @Override
    protected void addToolBarActions(DefaultActionGroup actionGroup) {
        super.addToolBarActions(actionGroup);

        actionGroup.add(new AsyncAction("Run") {
            {
                getTemplatePresentation().setIcon(IconManager.getIcon("execute.gif"));
            }

            @Override
            protected void doAction(AnActionEvent event) throws Exception {
                getDocbaseManager().getCurrentDocbase().execute(new Docbase.Action() {
                    @Override
                    public void execute(IDfSession session) throws DfException {
                        String api = commandField.getText();
                        executeCommand(session, api);
                    }
                });
            }

            @Override
            protected void onStart() {
                showStatusText("Executing command...");
            }

            @Override
            protected void onFinish() {
                hideStatusText();
            }

            @Override
            protected boolean isEnabled() {
                return super.isEnabled() & isDocbaseSelectedAndConnected() && StringUtils.isNotBlank(commandField.getText());
            }
        });
        actionGroup.add(new ClosePanelAction(this));
    }

    private class CommandExecutor implements Runnable {
        @Override
        public void run() {
            try {
                getDocbaseManager().getCurrentDocbase().execute(new Docbase.Action() {
                    @Override
                    public void execute(IDfSession session) throws DfException {
                        String api = commandField.getText();
                        executeCommand(session, api);
                    }
                });
            } catch (DfException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private void executeCommand(final IDfSession session, final String line) throws DfException {
        if (!running) {
            SwingUtilities.invokeLater(new UpdateRunning(true));

            StringTokenizer tokenizer = new StringTokenizer(line, ",");
            String command = tokenizer.nextToken();

            IDfList list = session.apiDesc(command);
            int cmdCallType = list.getInt(2);

            //убираем атрибут текущей сессии
            if (tokenizer.hasMoreTokens()) {
                tokenizer.nextToken();
            }

            String value = null;
            StringBuilder builder = new StringBuilder();
            StringBuilder arguments = new StringBuilder();
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();

                builder.append(token);
                if (tokenizer.hasMoreTokens()) {
                    arguments.append(token).append(",");
                    builder.append(",");
                } else {
                    value = token;
                }
            }

            try {
                doExecuteCommand(session, line, command, cmdCallType, builder, arguments, value);
            } catch (DfException e) {
                SwingUtilities.invokeLater(new ResultAppender("Error: " + e.getMessage() + "\n\n"));
            } finally {
                SwingUtilities.invokeLater(new UpdateRunning(false));
            }
        }
    }

    private void doExecuteCommand(IDfSession session, String line, String command, int cmdCallType, StringBuilder builder, StringBuilder arguments, String value) throws DfException {
        try {
            showStatusText("Executing command...");
            switch (cmdCallType) {
                case IDfSession.DM_GET:
                    SwingUtilities.invokeLater(new ResultAppender("GET: " + line + '\n'));

                    String returnValue = session.apiGet(command, builder.toString());
                    SwingUtilities.invokeLater(new ResultAppender("Result: " + returnValue + "\n\n"));
                    break;
                case IDfSession.DM_SET:
                    SwingUtilities.invokeLater(new ResultAppender("SET: " + line + '\n'));
                    boolean setReturnValue = session.apiSet(command, arguments.toString(), value);
                    SwingUtilities.invokeLater(new ResultAppender("Result: " + (setReturnValue ? "T" : "F") + "\n\n"));
                    break;
                case IDfSession.DM_EXEC:
                    SwingUtilities.invokeLater(new ResultAppender("EXEC: " + line + '\n'));
                    boolean execReturnValue = session.apiExec(command, builder.toString());
                    SwingUtilities.invokeLater(new ResultAppender("Result: " + (execReturnValue ? "T" : "F") + "\n\n"));
                    break;
                case -1:
                    SwingUtilities.invokeLater(new ResultAppender("UNKNOWN: " + line + '\n'));
                    break;
            }
        } finally {
            hideStatusText();
        }
    }

    private class ResultAppender implements Runnable {
        private final String message;

        private ResultAppender(String message) {
            this.message = message;
        }

        @Override
        public void run() {
            if (message != null) {
                resultTextArea.append(message);
                resultTextArea.setCaretPosition(resultTextArea.getDocument().getLength());
            }
        }
    }

    private class UpdateRunning implements Runnable {
        private boolean value;

        private UpdateRunning(boolean value) {
            this.value = value;
        }

        @Override
        public void run() {
            running = value;
        }
    }
}
