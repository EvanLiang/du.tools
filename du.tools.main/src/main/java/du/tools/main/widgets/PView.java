package du.tools.main.widgets;

import du.swingx.JETreeTable;
import lib.common.utils.FileToXml;
import lib.common.utils.XPathUtil;
import org.apache.commons.lang.StringUtils;
import du.tools.main.ConfigAccessor;
import du.tools.main.git.GitCommand;
import du.tools.main.git.status.Status;
import du.tools.main.git.status.StatusReader;
import du.tools.main.widgets.console.ProjectConsole;
import du.tools.main.windows.WinCodeViewer;

import javax.swing.*;
import javax.swing.table.TableColumn;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Enumeration;
import java.util.Map;

public class PView extends JSplitPane {

    private ProjectConsole projectConsole;
    private JETreeTable projectTable;
    private File rootDir;
    private String xmlPath;
    //    private Map<String, Difference> diffs;

    public PView(File rootDir, String xmlPath) {
        super(JSplitPane.VERTICAL_SPLIT);
        this.rootDir = rootDir;
        this.xmlPath = xmlPath;
        initialize();
//        diffs = DiffReader.getDiffs(rootDir);
    }

    private void initialize() {
        setDividerSize(5);
        JPanel jPanel = new JPanel();
        jPanel.setPreferredSize(new Dimension(0, 300));
        jPanel.setLayout(new BorderLayout());

        createBranchList(jPanel);
        createJTreeTable(jPanel);

        setTopComponent(jPanel);
    }

    private void createJTreeTable(JPanel parent) {
        File xml = new File(xmlPath);
        if (xml.exists()) {
            xml.delete();
        }
        FileToXml.toXmlFile(rootDir, true, xml);

        String statusText = new GitCommand().status(rootDir);
        Map<String, Status> status = StatusReader.getStatus(rootDir, statusText);

        PViewNode rootNode = new PViewNode(XPathUtil.getDocument(xmlPath), "/File", ConfigAccessor.getInstance().getProjectSkips(), status);

        PViewModel proViewModel = new PViewModel(rootNode, status);
        projectTable = new JETreeTable(proViewModel);
        projectTable.setRowHeight(20);
        projectTable.setRootVisible(true);
        projectTable.setTreeCellRenderer(new PViewRenderer());
        projectTable.setEditable(false);

        TableColumn column = projectTable.getColumnModel().getColumn(1);
        column.setPreferredWidth(100);
        column.setMaxWidth(100);
        column = projectTable.getColumnModel().getColumn(2);
        column.setPreferredWidth(100);
        column.setMaxWidth(100);

        addPopup(projectTable);

        parent.add(new JScrollPane(projectTable), BorderLayout.CENTER);
    }

    private void createBranchList(final JPanel parent) {
        JPanel gitPanel = new JPanel();
        gitPanel.setLayout(null);
        gitPanel.setPreferredSize(new Dimension(0, 35));

        JLabel label1 = new JLabel("Branch:");
        label1.setBounds(5, 5, 50, 20);
        gitPanel.add(label1);
        final JComboBox<String> jcbBranch = new JComboBox<>();
        jcbBranch.setBounds(55, 5, 200, 20);
        gitPanel.add(jcbBranch);

        JButton fetch = new JButton("fetch");
        fetch.setBounds(260, 5, 70, 20);
        gitPanel.add(fetch);
        fetch.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                GitCommand git = new GitCommand();
                String result = git.fetch(rootDir);
                if (result.contains("fatal:")) {
                    JOptionPane.showMessageDialog(PView.this, result);
                }
            }
        });

        final GitCommand git = new GitCommand();
        String result = git.branch(rootDir);
        if (StringUtils.isBlank(result)) {
            String branch = "master";
            jcbBranch.addItem(branch);
            jcbBranch.setSelectedItem(branch);
            result = git.branchAll(rootDir);
            String[] bs = result.split("\r\n");
            for (String b : bs) {
                b = b.replace("  remotes/origin/", "");
                jcbBranch.addItem(b);
            }
        } else {
            String[] bs = result.split("\r\n");
            for (String b : bs) {
                b = b.substring(2);
                jcbBranch.addItem(b);
                if (b.startsWith("* ")) {
                    jcbBranch.setSelectedItem(b);
                }
            }
        }

        jcbBranch.addItemListener(new ItemListener() {
            private Object item;

            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.DESELECTED) {
                    item = e.getItem();
                }
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    String branch = e.getItem().toString();
                    String result = git.checkout(rootDir, branch);
                    if (result.contains("Switched to")) {
                        parent.removeAll();
                        createBranchList(parent);
                        createJTreeTable(parent);
                        parent.updateUI();
                    } else {
                        JOptionPane.showMessageDialog(PView.this, result);
                        jcbBranch.removeItemListener(this);
                        jcbBranch.setSelectedItem(item);
                        jcbBranch.addItemListener(this);
                    }
                }
            }
        });

        parent.add(gitPanel, BorderLayout.NORTH);
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

    private void addPopup(final JETreeTable projectTable) {
        final JMenuItem mntmExpand = new JMenuItem("Expand All");
        mntmExpand.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                TreePath treePath = projectTable.getPathForRow(projectTable.getSelectedRow());
                expandCollapseAll(projectTable, treePath, true);
            }
        });

        final JMenuItem mntmCollapse = new JMenuItem("Collapse All");
        mntmCollapse.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                TreePath treePath = projectTable.getPathForRow(projectTable.getSelectedRow());
                expandCollapseAll(projectTable, treePath, false);
            }
        });

        final JMenuItem mntmConsole = new JMenuItem("Open Console");
        mntmConsole.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                openConsole();
            }
        });

        final JMenuItem mntmPHistory = new JMenuItem("Show project history");
        mntmPHistory.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                showPHistory();
            }
        });

        projectTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                    openFile();
                }
            }

            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int row = projectTable.rowAtPoint(e.getPoint());
                    projectTable.getSelectionModel().setSelectionInterval(row, row);
                    for (int i = 0; i < projectTable.getColumnCount(); i++) {
                        Rectangle rect = projectTable.getCellRect(row, i, true);
                        if (rect.contains(e.getX(), e.getY())) {
                            showMenu(e);
                        }
                    }
                }
            }

            private void showMenu(MouseEvent e) {
                JPopupMenu popupMenu = new JPopupMenu();

                TreePath treePath = projectTable.getPathForRow(projectTable.getSelectedRow());
                PViewNode node = (PViewNode) treePath.getLastPathComponent();
                if (node.isDirectory()) {
                    popupMenu.add(mntmExpand);
                    popupMenu.add(mntmCollapse);
                }

                if (projectTable.getSelectedRow() == 0) {
                    //popupMenu.add(mntmPHistory);
                    popupMenu.add(mntmConsole);
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
                popupMenu.show(projectTable, e.getX(), e.getY());
            }
        });
    }

    private void showPHistory() {
        String result = new GitCommand().log(rootDir);
        System.out.println(result);
    }

//    private void showDifference() {
//        TreePath treePath = projectTable.getPathForRow(projectTable.getSelectedRow());
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
        TreePath treePath = projectTable.getPathForRow(projectTable.getSelectedRow());
        PViewNode node = (PViewNode) treePath.getLastPathComponent();
        if (node.isDirectory()) {
            return null;
        }
        return new File(rootDir.getParent(), node.getFilePath());
    }

    public void openConsole() {
        if (projectConsole == null) {
            projectConsole = new ProjectConsole(this);
            setBottomComponent(projectConsole);
        }
        projectConsole.grabFocus();
    }

    public void closeConsole() {
        if (projectConsole != null) {
            setBottomComponent(null);
            projectConsole.destroy();
            projectConsole = null;
        }
    }

    public JETreeTable getProjectTable() {
        return projectTable;
    }

    public void destroy() {
        closeConsole();
    }

    public File getRootDir() {
        return rootDir;
    }
}
