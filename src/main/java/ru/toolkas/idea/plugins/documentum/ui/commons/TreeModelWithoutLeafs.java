package ru.toolkas.idea.plugins.documentum.ui.commons;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

public class TreeModelWithoutLeafs extends DefaultTreeModel {
    public TreeModelWithoutLeafs(TreeNode root) {
        super(root);
    }

    public TreeModelWithoutLeafs(TreeNode root, boolean asksAllowsChildren) {
        super(root, asksAllowsChildren);
    }

    @Override
    public boolean isLeaf(Object node) {
        return false;
    }
}
