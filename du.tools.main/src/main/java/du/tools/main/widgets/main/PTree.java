package du.tools.main.widgets.main;

import du.swingx.JETree;
import du.tools.main.ConfigAccessor;
import du.tools.main.Constants;
import du.tools.main.commons.utils.CommonUtil;
import du.tools.main.commons.utils.UiUtil;
import du.tools.main.commons.utils.XPathUtil;
import du.tools.main.git.GitCommand;
import du.tools.main.windows.WinMain;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;

public class PTree extends JPanel {

    private PMainPanel parent;
    private JETree proTree;

    public PTree(PMainPanel parent) {
        this.parent = parent;
        initialize();
    }

    private void initialize() {
        setLayout(new BorderLayout());
        proTree = new JETree();

        PTreeNode root = new PTreeNode(XPathUtil.getDocument(Constants.PROJECT_TREE_XML), "/Projects");
        PTreeModel proTreeModel = new PTreeModel(root);
        proTree.setModel(proTreeModel);
//        proTree.setFont(proTree.getFont().deriveFont(14F));
//        proTree.setCellRenderer(new PTreeRenderer());

        this.add(new JScrollPane(proTree, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER));

        addPopup();
    }

    private synchronized void viewProject(PTreeNode node, String path) {
        if (node.isRepo()) {
            PViews pViews = parent.getpViews();
            for (int i = 0; i < pViews.getTabCount(); i++) {
                String title = pViews.getTitleAt(i);
                if (title.equalsIgnoreCase(path)) {
                    pViews.setSelectedIndex(i);
                    return;
                }
            }
            File pDir = new File(ConfigAccessor.getInstance().getLocalRepo(), path);
            boolean ind = true;
            if (!pDir.exists()) {
                ind = cloneProject(path, false);
                File xmlFile = new File(Constants.PROJECT_VIEW_DIR, path + ".xml");
                if (!xmlFile.delete()) {
                    System.out.println("Delete file failed:" + xmlFile.getAbsoluteFile());
                }
            }
            if (ind) {
                PView pView = new PView(path);
                pViews.add(path, pView);
                pViews.setSelectedIndex(pViews.getTabCount() - 1);
            }
        }
    }

    private synchronized boolean cloneProject(String path, boolean force) {
        String remoteRepo = ConfigAccessor.getInstance().getHostName() + ":" + path + ".git";
        String localRepo = ConfigAccessor.getInstance().getLocalRepo();
        localRepo = CommonUtil.toBackSlash(localRepo);
        localRepo = localRepo + CommonUtil.toBackSlash(path);

        if (!force) {
            String message = "From: " + remoteRepo;
            message += "\n     To: " + localRepo;
            int answer = JOptionPane.showConfirmDialog(WinMain.frame, message, "Clone repo to local", JOptionPane.YES_NO_OPTION);
            if (answer != JOptionPane.OK_OPTION) {
                return false;
            }
        }

        File project = new File(localRepo);
        if (project.exists()) {
            if (!force) {
                int answer = JOptionPane.showConfirmDialog(WinMain.frame, "Exists in local, do you want to delete the existing repo?", "", JOptionPane.OK_CANCEL_OPTION);
                if (answer != JOptionPane.OK_OPTION) {
                    return false;
                }
            }

            try {
                FileUtils.forceDelete(project);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(WinMain.frame, "Delete directory failed, please delete manually.\n" + e.getMessage());
                return false;
            }
        }

        if (!project.getParentFile().exists()) {
            if (!project.getParentFile().mkdirs()) {
                setVisible(false);
                JOptionPane.showMessageDialog(WinMain.frame, "Creates the directory failed:" + project.getParentFile().getAbsoluteFile());
                return false;
            }
        }

        String msg = new GitCommand().clone(project.getParentFile(), remoteRepo);
        if (msg.contains("fatal:")) {
            if (!force) {
                JOptionPane.showMessageDialog(WinMain.frame, msg);
            }
            return false;
        } else {
            return true;
        }
    }

    public PTreeNode getSelectedNode() {
        TreePath treePath = proTree.getSelectionPath();
        if (treePath != null) {
            return (PTreeNode) treePath.getLastPathComponent();
        }
        return null;
    }

    private void addPopup() {
        final JMenuItem mntmClone = new JMenuItem("Git clone");
        mntmClone.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                final PTreeNode node = getSelectedNode();
                UiUtil.exe(new UiUtil.Callable() {
                    public void call() {
                        if (cloneProject(node.getRepoPath(), false)) {
                            viewProject(node, node.getRepoPath());
                        }
                    }
                });
            }
        });

        final JMenuItem mntmExpand = new JMenuItem("Expand All");
        mntmExpand.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                TreePath treePath = proTree.getSelectionPath();
                UiUtil.expandAll(proTree, treePath);
            }
        });

        final JMenuItem mntmCollapse = new JMenuItem("Collapse All");
        mntmCollapse.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                TreePath treePath = proTree.getSelectionPath();
                UiUtil.collapseAll(proTree, treePath);
            }
        });

        final JMenuItem mntmCloneAll = new JMenuItem("Clone All");
        mntmCloneAll.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                final PTreeNode node = getSelectedNode();
                UiUtil.exe(new UiUtil.Callable() {
                    public void call() {
                        String errs = "Clone failures:\n";
                        Enumeration enumeration = node.getChildren().elements();
                        while (enumeration.hasMoreElements()) {
                            PTreeNode n = (PTreeNode) enumeration.nextElement();
                            if (!cloneProject(n.getRepoPath(), true)) {
                                errs += n.getRepoPath() + "\n";
                            }
                        }
                        if (errs.length() > "Clone failures:\n".length()) {
                            JOptionPane.showMessageDialog(WinMain.frame, errs);
                        }
                    }
                });
            }
        });

        proTree.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                    final PTreeNode node = getSelectedNode();
                    if (node.isRepo()) {
                        UiUtil.exe(new UiUtil.Callable() {
                            public void call() {
                                viewProject(node, node.getRepoPath());
                            }
                        });
                    }
                }
            }

            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    if (proTree.getSelectionRows().length > 0) {
                        showMenu(e);
                    }
                }
            }

            private void showMenu(MouseEvent e) {
                JPopupMenu popupMenu = new JPopupMenu();
                PTreeNode node = getSelectedNode();
                if (node.isProj()) {
                    popupMenu.add(mntmCloneAll);
                }
                if (node.isRepo()) {
                    popupMenu.add(mntmClone);
                }
                popupMenu.add(mntmExpand);
                popupMenu.add(mntmCollapse);

                proTree.grabFocus();
                popupMenu.show(proTree, e.getX(), e.getY());
            }
        });
    }
}