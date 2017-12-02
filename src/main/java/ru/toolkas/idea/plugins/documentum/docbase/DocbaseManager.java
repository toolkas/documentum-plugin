package ru.toolkas.idea.plugins.documentum.docbase;

import java.util.Collection;

public interface DocbaseManager {
    void clearDocbases();

    void addDocbase(Docbase docbase);

    Collection<Docbase> getDocbases();

    Docbase getCurrentDocbase();

    boolean isCurrentDocbaseConnected();

    void setCurrentDocbase(Docbase docbase);

    void disconnectAll();
}
