package ru.toolkas.idea.plugins.documentum.components.application;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.util.NamedJDOMExternalizable;
import ru.toolkas.idea.plugins.documentum.docbase.DocbaseManager;

public interface DocumentumPlugin extends ApplicationComponent, NamedJDOMExternalizable {
    DocbaseManager getDocbaseManager();
}
