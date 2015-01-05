package du.tools.main.widgets.main;

import du.swingx.JETreeTable;
import du.tools.main.ConfigAccessor;
import du.tools.main.Constants;
import du.tools.main.commons.utils.FileToXml;
import du.tools.main.commons.utils.UiUtil;
import du.tools.main.commons.utils.XPathUtil;
import du.tools.main.git.GitCommand;
import du.tools.main.widgets.console.ProjectConsole;
import du.tools.main.windows.WinCodeViewer;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.api.Git;

import javax.swing.*;
import javax.swing.table.TableColumn;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;

public class PView extends JSplitPane {

    private ProjectConsole pConsole;
    private JETreeTable pTreeTable;
    private JButton btnConsole;
    private JComboBox<String> pBranch;
    private File rootDir;
    private File xmlFile;
    org.eclipse.jgit.api.Status status = null;

    public PView(String path) {
        super(JSplitPane.VERTICAL_SPLIT);
        rootDir = new File(ConfigAccessor.getInstance().getLocalRepo(), path);
        xmlFile = new File(Constants.PROJECT_VIEW_DIR, path + ".xml");
        initialize();
    }

    private void initialize() {
        setDividerSize(5);
        setDividerLocation(300);
        JPanel container = new JPanel();
        container.setLayout(new BorderLayout());

        container.add(createBranchList(), BorderLayout.NORTH);
        createJTreeTable();
        pTreeTable.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_F5) {
                    UiUtil.exe(new UiUtil.Callable() {
                        public void call() {
                            refreshJTreeTable();
                        }
                    });
                }
            }
        });
        container.add(new JScrollPane(pTreeTable), BorderLayout.CENTER);
        setTopComponent(container);
        openConsole();
    }

    private void createJTreeTable() {
        pTreeTable = new JETreeTable();
        pTreeTable.setRowHeight(20);
        pTreeTable.setRootVisible(true);
        pTreeTable.setEditable(false);
//        pTreeTable.setFont(pTreeTable.getFont().deriveFont(14F));
        pTreeTable.setTreeCellRenderer(new PViewTreeRenderer());
        updateJTreeTable();

        addPopup(pTreeTable);
    }

    private synchronized void refreshJTreeTable() {
        deleteFileTreeXml();
        updateJTreeTable();
    }

    private void updateJTreeTable() {
        generateFileTree();
        try {
            Git git = Git.open(rootDir);
            status = git.status().call();
            git.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        PViewNode root = new PViewNode(XPathUtil.getDocument(xmlFile), "/File", ConfigAccessor.getInstance().getProjectSkips(), status);
        PViewModel pViewModel = new PViewModel(root);
        pTreeTable.setTreeTableModel(pViewModel);

        TableColumn column = pTreeTable.getColumnModel().getColumn(1);
        column.setCellRenderer(new PViewCellRenderer());
        column.setPreferredWidth(100);
        column.setMaxWidth(100);
    }

    private JPanel createBranchList() {
        JPanel toolPanel = new JPanel();
        toolPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 3));
        toolPanel.setPreferredSize(new Dimension(0, 30));

        btnConsole = new JButton("Run");
        toolPanel.add(btnConsole);
        btnConsole.setEnabled(false);
        btnConsole.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                openConsole();
            }
        });

        JLabel label1 = new JLabel("Branch:");
        toolPanel.add(label1);
        pBranch = new JComboBox<>();
        listBranches();
        pBranch.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    String branch = e.getItem().toString();
                    branch = branch.replace("remotes/origin/", "");
                    GitCommand git = new GitCommand();
                    String result = git.checkout(rootDir, branch);
                    if (result.contains("Switched to")) {
                        pBranch.removeItemListener(this);
                        listBranches();
                        refreshJTreeTable();
                        pBranch.addItemListener(this);
                    } else {
                        JOptionPane.showMessageDialog(PView.this, result);
                    }
                }
            }
        });
        toolPanel.add(pBranch);

        JButton fetch = new JButton("fetch");
        toolPanel.add(fetch);
        fetch.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                GitCommand git = new GitCommand();
                String result = git.fetch(rootDir);
                if (result.contains("fatal:")) {
                    JOptionPane.showMessageDialog(PView.this, result);
                } else {
                    refreshJTreeTable();
                }
            }
        });
        return toolPanel;
    }

    private void listBranches() {
        pBranch.removeAllItems();
        String result = new GitCommand().branchAll(rootDir);
        if (StringUtils.isNotBlank(result)) {
            String[] bs = result.split("\r\n");
            for (String b : bs) {
                if (b.contains("/HEAD ")) {
                    continue;
                }
                String item = b.substring(2);
                pBranch.addItem(item);
                if (b.startsWith("* ")) {
                    pBranch.setSelectedItem(item);
                }
            }
        } else {
            pBranch.addItem("master");
        }
    }

    private void deleteFileTreeXml() {
        if (xmlFile.exists()) {
            if (!xmlFile.delete()) {
                System.out.println("Delete file failed:" + xmlFile.getAbsoluteFile());
            }
        }
    }

    private void generateFileTree() {
        if (!xmlFile.exists()) {
            File dir = xmlFile.getParentFile();
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    System.out.println("Creates the directory failed:" + dir.getAbsoluteFile());
                    return;
                }
            }
            FileToXml.toXmlFile(rootDir, true, xmlFile);
        }
    }

    private void expandCollapseAll(JETreeTable tree, TreePath parent, boolean expand) {
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

    private void addPopup(final JETreeTable pTreeTable) {
        final JMenuItem mntmExpand = new JMenuItem("Expand All");
        mntmExpand.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                TreePath treePath = pTreeTable.getPathForRow(pTreeTable.getSelectedRow());
                expandCollapseAll(pTreeTable, treePath, true);
            }
        });

        final JMenuItem mntmCollapse = new JMenuItem("Collapse All");
        mntmCollapse.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                TreePath treePath = pTreeTable.getPathForRow(pTreeTable.getSelectedRow());
                expandCollapseAll(pTreeTable, treePath, false);
            }
        });

        final JMenuItem mntmPHistory = new JMenuItem("Show project history");
        mntmPHistory.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                showPHistory();
            }
        });

        final JMenuItem mntmIDEOpen = new JMenuItem("Open project in IDE");
        mntmIDEOpen.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                File file = getSelectedFile();
                if (file != null) {
                    if (file.exists()) {
                        try {
                            Runtime.getRuntime().exec(ConfigAccessor.getInstance().getIdeEex()
                                    + " \"" + file.getAbsolutePath() + "\"");
                        } catch (IOException e1) {
                            JOptionPane.showMessageDialog(PView.this, "Open failed: " + e1.getMessage());
                        }
                    } else {
                        JOptionPane.showMessageDialog(PView.this, "Working file not exists.");
                    }
                }
            }
        });

        final JMenuItem mntmShowExplorer = new JMenuItem("Show In Explorer");
        mntmShowExplorer.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                File file = getSelectedDir();
                if (file != null) {
                    try {
                        Runtime.getRuntime().exec("explorer \"" + file.getAbsolutePath() + "\"");
                    } catch (IOException e1) {
                        JOptionPane.showMessageDialog(PView.this, "Open failed: " + e1.getMessage());
                    }
                }
            }
        });

        pTreeTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                    openFile();
                }
            }

            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int row = pTreeTable.rowAtPoint(e.getPoint());
                    pTreeTable.getSelectionModel().setSelectionInterval(row, row);
                    for (int i = 0; i < pTreeTable.getColumnCount(); i++) {
                        Rectangle rect = pTreeTable.getCellRect(row, i, true);
                        if (rect.contains(e.getX(), e.getY())) {
                            showMenu(e);
                        }
                    }
                }
            }

            private void showMenu(MouseEvent e) {
                JPopupMenu popupMenu = new JPopupMenu();

                TreePath treePath = pTreeTable.getPathForRow(pTreeTable.getSelectedRow());
                PViewNode node = (PViewNode) treePath.getLastPathComponent();
                if (node.isDirectory()) {
                    popupMenu.add(mntmExpand);
                    popupMenu.add(mntmCollapse);
                }

                //Parent pom
                if (treePath.getPathCount() == 2 && node.isFile() && node.getName().equals("pom.xml")) {
                    popupMenu.add(mntmIDEOpen);
                }

                if (pTreeTable.getSelectedRow() == 0) {
                    //popupMenu.add(mntmPHistory);
                }

//                if (node.isFile()) {
//                    String key = node.getFilePath().substring(rootNode.toString().length() + 1);
//                    Difference diff = diffs.get(key);
//                    if (diff != null) {
//                        if (diff.isTextFile()) {
//                                popupMenu.add(mntmDifference);
//                        }
//                    }
//                }

                popupMenu.addSeparator();
                popupMenu.add(mntmShowExplorer);

                popupMenu.show(pTreeTable, e.getX(), e.getY());
            }
        });
    }

    private void showPHistory() {
        String result = new GitCommand().log(rootDir);
        System.out.println(result);
    }

//    private void showDifference() {
//        TreePath treePath = pTreeTable.getPathForRow(pTreeTable.getSelectedRow());
//        PViewNode node = (PViewNode) treePath.getLastPathComponent();
//        String key = node.getFilePath().substring(rootNode.toString().length() + 1);
//        Difference diff = diffs.get(key);
//        if (diff != null) {
//            if (diff.isTextFile()) {
//                File file = new File(ConfigAccessor.getInstance().getLocalRepo(), node.getFilePath());
//                DiffWindow diffWindow = new DiffWindow(file, diff);
//                diffWindow.open();
//            } else {
//                JOptionPane.showMessageDialog(this, "It's not a text file: " + node.getFilePath());
//            }
//        }
//    }

    private void openFile() {
        File file = getSelectedFile();
        if (file != null) {
            if (file.exists()) {
                new WinCodeViewer(file).setVisible(true);
//                String path = file.getAbsolutePath();
                //new CmdExecutor().exec("notepad \"" + path + "\"");
            } else {
                JOptionPane.showMessageDialog(this, "Working file not exists.");
            }
        }
    }

    private File getSelectedFile() {
        TreePath treePath = pTreeTable.getPathForRow(pTreeTable.getSelectedRow());
        PViewNode node = (PViewNode) treePath.getLastPathComponent();
        if (node.isDirectory()) {
            return null;
        }
        return new File(rootDir, node.getFilePath());
    }

    private File getSelectedDir() {
        TreePath treePath = pTreeTable.getPathForRow(pTreeTable.getSelectedRow());
        PViewNode node = (PViewNode) treePath.getLastPathComponent();
        if (node.isFile()) {
            return new File(rootDir, node.getFilePath()).getParentFile();
        }
        return new File(rootDir, node.getFilePath());
    }

    public void openConsole() {
        if (pConsole == null) {
            pConsole = new ProjectConsole(this);
            setBottomComponent(pConsole);
        }
        pConsole.grabFocus();
        btnConsole.setEnabled(false);
    }

    public void closeConsole() {
        if (pConsole != null) {
            setBottomComponent(null);
            pConsole.destroy();
            pConsole = null;
        }
        btnConsole.setEnabled(true);
    }

    public JETreeTable getpTreeTable() {
        return pTreeTable;
    }

    public void destroy() {
        closeConsole();
    }

    public File getRootDir() {
        return rootDir;
    }
}
