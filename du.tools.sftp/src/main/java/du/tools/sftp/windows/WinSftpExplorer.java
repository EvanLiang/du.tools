package du.tools.sftp.windows;

import du.swingx.JETabbedPane;
import du.tools.commons.Utils;
import du.tools.sftp.unix.RemoteService;
import du.tools.sftp.widgets.unix.LExplorerPanel;
import du.tools.sftp.widgets.unix.RExplorerPanel;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Properties;

public class WinSftpExplorer extends JFrame {

    private static final long serialVersionUID = 1L;
    private static Logger log = LoggerFactory.getLogger(WinSftpExplorer.class);
    private static final String PATTERN = "(.*):(.*)@([\\.0-9]*):?(.*)?";

    private Properties prop;

    private JETabbedPane unixExplorers;
    private JTabbedPane windowsExplorer;
    private LExplorerPanel lExplorerPanel;

    public WinSftpExplorer() throws IOException {
        loadConfig();
        initialise();
    }

    private void loadConfig() throws IOException {
        prop = new Properties();
        File pFile = new File("sftp.properties");
        if (!pFile.exists()) {
            prop.setProperty("connection.xx", "user:pwd@host:/home");
            prop.store(new FileOutputStream(pFile), "Configuration of your remote host");
            return;
        }
        prop.load(new InputStreamReader(new FileInputStream(pFile)));
    }

    private void initialise() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Unix Explorer Tool");
        setSize(1200, 800);
        setLocationRelativeTo(null);

        windowsExplorer = new JTabbedPane();
        lExplorerPanel = new LExplorerPanel("D:/");
        windowsExplorer.addTab("Local Windows", lExplorerPanel);

        unixExplorers = new JETabbedPane();
        unixExplorers.setTabCloseListener(new JETabbedPane.TabCloseListener() {
            public boolean actionPerformed(int index) {
                return closeSession(index);
            }
        });

        addMenu();
        setVisible(true);

        final JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT, windowsExplorer, unixExplorers);
        getContentPane().add(splitPane);

        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                splitPane.setDividerLocation(getWidth() / 3);
            }
        });
    }

    private void addMenu() {
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        menuBar.setPreferredSize(new Dimension(0, 25));

        JMenu menu = new JMenu("Host name");
        menuBar.add(menu);

        for (Object key : prop.keySet()) {
            final String connection = prop.getProperty(key.toString());
            JMenuItem item = new JMenuItem(connection);
            menu.add(item);
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    openSession(connection);
                }
            });
        }

        JMenuItem item = new JMenuItem("Other host");
        menu.add(item);
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String connection = JOptionPane.showInputDialog(WinSftpExplorer.this, "Give me your host name");
                openSession(connection);
            }
        });

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                for (int i = 0; i < unixExplorers.getTabCount(); i++) {
                    unixExplorers.closeTabAt(i);
                }
                log.info("Window CLOSED");
            }
        });
    }

    private boolean closeSession(int index) {
        try {
            RExplorerPanel panel = (RExplorerPanel) unixExplorers
                    .getComponentAt(index);
            panel.close();
            log.info("Closed Session {} - {}", new Object[]{unixExplorers.getTitleAt(index)});
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean openSession(String connStr) {
        String[] connection = Utils.parseUnixConnection(connStr);
        if (connection != null) {
            return openSession(connection[0], connection[1], connection[2], connection[3]);
        } else {
            JOptionPane.showMessageDialog(WinSftpExplorer.this, PATTERN);
            return false;
        }
    }

    private boolean openSession(String host, String user, String pwd, String dir) {
        if (StringUtils.isNotBlank(host)) {
            try {
                RemoteService rs = new RemoteService();
                rs.connect(host, user, pwd);
                unixExplorers.addTab(user + "@" + host, new RExplorerPanel(rs, dir, lExplorerPanel));
                log.info("Opened Session {}:{}@{}", user, pwd, host);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public JTabbedPane getWindowsExplorer() {
        return windowsExplorer;
    }
}
