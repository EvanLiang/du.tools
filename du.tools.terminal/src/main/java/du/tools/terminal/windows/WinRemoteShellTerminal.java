package du.tools.terminal.windows;

import com.google.common.base.Predicate;
import com.jediterm.ssh.jsch.JSchTtyConnector;
import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.TtyConnectorWaitFor;
import com.jediterm.terminal.ui.JediTermWidget;
import com.jediterm.terminal.ui.TerminalSession;
import com.jediterm.terminal.ui.settings.DefaultTabbedSettingsProvider;
import du.swingx.JETabbedPane;
import du.tools.commons.Utils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.Properties;
import java.util.concurrent.Executors;

public class WinRemoteShellTerminal extends JFrame {

    private static Logger log = LoggerFactory.getLogger(WinRemoteShellTerminal.class);
    private Properties prop;
    private JETabbedPane consoleTabPane;

    public WinRemoteShellTerminal() throws IOException {
        loadConfig();
        initialise();
    }

    private void loadConfig() throws IOException {
        prop = new Properties();
        File pFile = new File("terminal.properties");
        if (!pFile.exists()) {
            prop.setProperty("xx", "user:password@host");
            prop.store(new FileOutputStream(pFile), "Configuration of your remote host");
            return;
        }
        prop.load(new InputStreamReader(new FileInputStream(pFile)));
    }

    private void initialise() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Unix Terminal Tool");
        setSize(1200, 800);
        setLocationRelativeTo(null);

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        menuBar.setPreferredSize(new Dimension(0, 25));

        JMenu menu = new JMenu("Host name");
        menuBar.add(menu);
        for (Object key : prop.keySet()) {
            String connStr = prop.getProperty(key.toString());
            JMenuItem item = new JMenuItem(connStr);
            menu.add(item);
            final String[] connection = Utils.parseUnixConnection(connStr);
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    openSession(connection[0], connection[1], connection[2]);
                }
            });
        }

        JMenuItem item = new JMenuItem("Other host");
        menu.add(item);
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String connStr = JOptionPane.showInputDialog(WinRemoteShellTerminal.this, "Give me your host name");
                final String[] connection = Utils.parseUnixConnection(connStr);
                openSession(connection[0], connection[1], connection[2]);
            }
        });

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                for (int i = 0; i < consoleTabPane.getTabCount(); i++) {
                    consoleTabPane.closeTabAt(i);
                }
                log.info("Window CLOSED");
            }
        });

        consoleTabPane = new JETabbedPane();
        consoleTabPane.setTabCloseListener(new JETabbedPane.TabCloseListener() {
            public boolean actionPerformed(int index) {
                return closeSession(index);
            }
        });
        getContentPane().add(consoleTabPane);

        setVisible(true);
    }

    private boolean closeSession(int index) {
        try {
            JediTermWidget terminal = (JediTermWidget) consoleTabPane.getComponentAt(index);
            terminal.close();
            log.info("Closed Session {}", consoleTabPane.getTitleAt(index));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean openSession(String host, String user, String password) {
        if (StringUtils.isNotBlank(host)) {
            try {
                DefaultTabbedSettingsProvider mySettingsProvider = new DefaultTabbedSettingsProvider();
                JediTermWidget terminal = new JediTermWidget(mySettingsProvider);
                if (terminal.canOpenSession()) {
                    TtyConnector ttyConnector = new JSchTtyConnector(host, user, password);
                    TerminalSession session = terminal.createTerminalSession(ttyConnector);
                    session.start();

                    consoleTabPane.addTab(user + "@" + host, terminal);
                    consoleTabPane.setSelectedIndex(consoleTabPane.getTabCount() - 1);

                    regExistAction(mySettingsProvider, ttyConnector, terminal);

                    log.info("Opened Session {}@{} - {}", user, host, password);
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private int indexOfTab(JediTermWidget terminal) {
        for (int i = 0; i < consoleTabPane.getTabCount(); i++) {
            JediTermWidget t = (JediTermWidget) consoleTabPane.getComponentAt(i);
            if (t == terminal) {
                return i;
            }
        }
        return -1;
    }

    private void regExistAction(final DefaultTabbedSettingsProvider mySettingsProvider, final TtyConnector ttyConnector, final JediTermWidget terminal) {
        TtyConnectorWaitFor waitFor = new TtyConnectorWaitFor(ttyConnector, Executors.newSingleThreadExecutor());
        waitFor.setTerminationCallback(new Predicate<Integer>() {
            public boolean apply(Integer integer) {
                if (mySettingsProvider.shouldCloseTabOnLogout(ttyConnector)) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            int index = indexOfTab(terminal);
                            if (index != -1) {
                                consoleTabPane.closeTabAt(index);
                            }
                        }
                    });
                }
                return true;
            }
        });
    }
}
