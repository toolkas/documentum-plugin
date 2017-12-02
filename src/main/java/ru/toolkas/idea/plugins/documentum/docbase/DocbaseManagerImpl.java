package ru.toolkas.idea.plugins.documentum.docbase;

import java.util.*;

public class DocbaseManagerImpl implements DocbaseManager {
    private final List<Docbase> docbases = new ArrayList<Docbase>();
    private Docbase currentDocbase;

    public void clearDocbases() {
        disconnectAll();
        docbases.clear();
    }

    @Override
    public void addDocbase(Docbase docbase) {
        docbases.add(docbase);
    }

    @Override
    public Collection<Docbase> getDocbases() {
        return docbases;
    }

    @Override
    public Docbase getCurrentDocbase() {
        return currentDocbase;
    }

    @Override
    public boolean isCurrentDocbaseConnected() {
        return currentDocbase != null && currentDocbase.isConnected();
    }

    @Override
    public void setCurrentDocbase(Docbase docbase) {
        if (currentDocbase != null) {
            currentDocbase.disconnect();
        }
        currentDocbase = docbase;
    }

    public void disconnectAll() {
        for (Docbase docbase : docbases) {
            docbase.disconnect();
        }
    }
}
