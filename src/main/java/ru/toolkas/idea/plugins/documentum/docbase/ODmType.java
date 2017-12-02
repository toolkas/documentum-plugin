package ru.toolkas.idea.plugins.documentum.docbase;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

public class ODmType {
    private final Set<String> children = new TreeSet<String>();
    private String name;

    public ODmType(String name) {
        this.name = name;
    }

    public void addChild(String name) {
        children.add(name);
    }

    public Collection<String> getChildren() {
        return children;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
