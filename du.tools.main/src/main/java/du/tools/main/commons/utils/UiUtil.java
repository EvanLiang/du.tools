package du.tools.main.commons.utils;

import du.tools.main.windows.WinMain;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.Enumeration;

public class UiUtil {

    public static interface Callable {
        void call();
    }

    public static void exe(final Callable callable) {
        Thread t = new Thread(new Runnable() {
            public void run() {
                WinMain.busy.setVisible(true);
                try {
                    callable.call();
                } finally {
                    WinMain.busy.setVisible(false);
                }
            }
        });
        t.start();
    }

    public static void expandAll(JTree tree, TreePath parent) {
        expandCollapseAll(tree, parent, true);
    }

    public static void collapseAll(JTree tree, TreePath parent) {
        expandCollapseAll(tree, parent, false);
    }

    private static void expandCollapseAll(JTree tree, TreePath parent, boolean expand) {
        TreeNode node = (TreeNode) parent.getLastPathComponent();
        if (node.getChildCount() > 0) {
            Enumeration children = node.children();
            while (children.hasMoreElements()) {
                Object o = children.nextElement();
                TreePath path = parent.pathByAddingChild(o);
                expandCollapseAll(tree, path, expand);
            }
        }
        if (expand) {
            tree.expandPath(parent);
        } else {
            tree.collapsePath(parent);
        }
    }

}
