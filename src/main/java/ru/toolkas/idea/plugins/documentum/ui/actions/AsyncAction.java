package ru.toolkas.idea.plugins.documentum.ui.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import org.jetbrains.annotations.Nullable;
import ru.toolkas.idea.plugins.documentum.utils.ConcurrentUtils;

import javax.swing.*;

public abstract class AsyncAction extends AnAction {
    private volatile boolean running = false;

    public AsyncAction() {
    }

    public AsyncAction(@Nullable String text) {
        super(text);
    }

    public AsyncAction(String text, @Nullable String description, @Nullable Icon icon) {
        super(text, description, icon);
    }

    @Override
    public void update(AnActionEvent event) {
        super.update(event);

        event.getPresentation().setEnabled(isEnabled());
    }

    protected boolean check() {
        return true;
    }

    @Override
    public void actionPerformed(final AnActionEvent event) {
        final Presentation presentation = event.getPresentation();

        if (isEnabled()) {
            if (check()) {
                onStart();
                presentation.setEnabled(false);
                ConcurrentUtils.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            running = true;
                            doAction(event);
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        } finally {
                            running = false;
                            onFinish();
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    presentation.setEnabled(isEnabled());
                                }
                            });
                        }
                    }
                });
            }
        }
    }

    protected void onStart() {
    }

    protected void onFinish() {
    }

    protected boolean isEnabled() {
        return !running;
    }

    protected abstract void doAction(AnActionEvent event) throws Exception;
}
