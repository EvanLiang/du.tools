package du.tools.main.widgets;

import du.swingx.JETree;
import lib.common.utils.CommonUtil;
import lib.common.utils.UiUtil;
import lib.common.utils.XPathUtil;
import du.tools.main.ConfigAccessor;
import du.tools.main.Constants;
import du.tools.main.git.GitCommand;
import du.tools.main.windows.WinMain;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

public class PTree extends JPanel {

    private PMainPanel parent;

    public PTree(PMainPanel parent) {
        this.parent = parent;
        initialize();
    }

    private void initialize() {
        setLayout(new BorderLayout());
        JETree proTree = new JETree();
        proTree.setFont(proTree.getFont().deriveFont(14F));
        PTreeModel proTreeModel = new PTreeModel(new PTreeNode(XPathUtil.getDocument(Constants.PROJECT_TREE_DIR + "/project.xml"), "/Projects"));
        proTree.setModel(proTreeModel);
        proTree.setCellRenderer(new PTreeRenderer());
        setPreferredSize(new Dimension(250, 300));

        this.add(new JScrollPane(proTree, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER));

        addPopup(proTree);
    }

    private void viewProject(PTreeNode root, String path) {
        PViews pViews = parent.getpViews();
        for (int i = 0; i < pViews.getTabCount(); i++) {
            String title = pViews.getTitleAt(i);
            if (title.equalsIgnoreCase(root.toString())) {
                pViews.setSelectedIndex(i);
                return;
            }
        }
        File dir = new File(ConfigAccessor.getInstance().getLocalRepo(), path);
        boolean cloneOK = true;
        if (!dir.exists()) {
            cloneOK = cloneProject(path);
        }
        if (cloneOK) {
            File xml = new File(Constants.PROJECT_VIEW_DIR, path + ".xml");
            File parent = xml.getParentFile();
            if (!parent.exists()) {
                if (!parent.mkdirs()) {
                    System.out.println("Creates the directory failed:" + parent.getAbsoluteFile());
                }
            }
            PView pView = new PView(dir, xml.getAbsolutePath());
            pViews.add(root.toString(), pView);
            pViews.setSelectedIndex(pViews.getTabCount() - 1);
        }
    }

    private boolean cloneProject(String path) {
        String remoteRepo = ConfigAccessor.getInstance().getHostName() + ":" + path + ".git";
        String localRepo = ConfigAccessor.getInstance().getLocalRepo();
        localRepo = CommonUtil.toBackSlash(localRepo);
        localRepo = localRepo + CommonUtil.toBackSlash(path);

        String message = "From: " + remoteRepo;
        message += "\n     To: " + localRepo;
        int answer = JOptionPane.showConfirmDialog(WinMain.frame, message, "Clone repo to local", JOptionPane.YES_NO_OPTION);

        if (answer == JOptionPane.OK_OPTION) {
            File project = new File(localRepo);
            GitCommand git = new GitCommand();

            if (project.exists()) {
                answer = JOptionPane.showConfirmDialog(WinMain.frame, "Exists in local, do you want to delete the existing repo?");
                if (answer == JOptionPane.OK_OPTION) {
                    if (!project.delete()) {
                        JOptionPane.showMessageDialog(WinMain.frame, "Delete directory failed, please delete manually.");
                        return false;
                    }
                }
            }

            if (!project.getParentFile().exists()) {
                if (!project.getParentFile().mkdirs()) {
                    setVisible(false);
                    JOptionPane.showMessageDialog(WinMain.frame, "Creates the directory failed:" + project.getParentFile().getAbsoluteFile());
                    return false;
                }
            }

            String msg = git.clone(project.getParentFile(), remoteRepo);
            if (msg.contains("fatal:")) {
                JOptionPane.showMessageDialog(WinMain.frame, msg);
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    private void addPopup(final JTree proTree) {
        final JMenuItem mntmClone = new JMenuItem("Git clone");
        mntmClone.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                TreePath treePath = proTree.getSelectionPath();
                PTreeNode node = (PTreeNode) treePath.getLastPathComponent();
                cloneProject(node.getRepoPath());
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

        proTree.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                    if (isLeaf()) {
                        TreePath treePath = proTree.getSelectionPath();
                        PTreeNode node = (PTreeNode) treePath.getLastPathComponent();
                        viewProject(node, node.getRepoPath());
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
                if (isLeaf()) {
                    popupMenu.add(mntmClone);
                }
                popupMenu.add(mntmExpand);
                popupMenu.add(mntmCollapse);

                proTree.grabFocus();
                popupMenu.show(proTree, e.getX(), e.getY());
            }

            private boolean isLeaf() {
                TreePath treePath = proTree.getSelectionPath();
                PTreeNode node = (PTreeNode) treePath.getLastPathComponent();
                return node.getChildren() == null || node.getChildren().size() == 0;
            }
        });
    }
}