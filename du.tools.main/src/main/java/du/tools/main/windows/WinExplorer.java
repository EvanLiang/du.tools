package du.tools.main.windows;

import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import du.swingx.JETabbedPane;
import du.tools.main.ConfigAccessor;
import du.tools.main.commons.utils.CommonUtil;
import du.tools.main.unix.RemoteService;
import du.tools.main.widgets.unix.LExplorerPanel;
import du.tools.main.widgets.unix.RExplorerPanel;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;

public class WinExplorer extends JFrame {

    private static Logger log = LoggerFactory.getLogger(WinExplorer.class);
    private String user = ConfigAccessor.getInstance().getUnixUser();
    private String pwd = ConfigAccessor.getInstance().getUnixPassword();

    private JETabbedPane unixExplorers;
    private JTabbedPane windowsExplorer;

    public static void main(String[] ars) {
        URL url = CommonUtil.getURLInClasspath("log4j.xml");
        DOMConfigurator.configure(url);
        try {
            UIManager.setLookAndFeel(new PlasticLookAndFeel());
            new WinExplorer().setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("App Error", e);
        }
    }

    public WinExplorer() {
        initialise();
    }

    private void initialise() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Unix Explorer Tool");
        setSize(1200, 800);
        setLocationRelativeTo(null);

        windowsExplorer = new JTabbedPane();
        windowsExplorer.addTab("Local Windows", new LExplorerPanel("C:/BEA92"));

        unixExplorers = new JETabbedPane();
        unixExplorers.setTabCloseListener(new JETabbedPane.TabCloseListener() {
            public boolean actionPerformed(int index) {
                return closeSession(index);
            }
        });

        addMenu();
        setVisible(true);

        final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, windowsExplorer, unixExplorers);
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
        for (final String host : ConfigAccessor.getInstance().getUnixHosts()) {
            JMenuItem item = new JMenuItem(host);
            menu.add(item);
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    openSession(host, user, pwd);
                }
            });
        }
        JMenuItem item = new JMenuItem("Other host");
        menu.add(item);
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String host = JOptionPane.showInputDialog(WinExplorer.this, "Give me your host name");
                openSession(host, user, pwd);
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
            RExplorerPanel panel = (RExplorerPanel) unixExplorers.getComponentAt(index);
            panel.close();
            log.info("Closed Session {} - {}", new String[]{unixExplorers.getTitleAt(index), pwd});
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean openSession(String host, String user, String pwd) {
        if (StringUtils.isNotBlank(host)) {
            try {
                RemoteService rs = new RemoteService();
                rs.connect(host, user, pwd);
                unixExplorers.addTab(user + "@" + host, new RExplorerPanel(rs, "/"));
                log.info("Opened Session {}@{} - {}", new String[]{user, host, pwd});
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
