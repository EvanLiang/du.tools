package du.tools.main.windows;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import com.jediterm.pty.PtyProcessTtyConnector;
import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.TtyConnectorWaitFor;
import com.jediterm.terminal.ui.JediTermWidget;
import com.jediterm.terminal.ui.TerminalSession;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.pty4j.PtyProcess;
import du.swingx.JETabbedPane;
import du.tools.main.ConfigAccessor;
import du.tools.main.commons.utils.CommonUtil;
import du.tools.main.widgets.console.pty.PtySettingsProvider;
import du.tools.main.widgets.console.pty.unix.RUnixPtyProcess;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.Executors;

public class WinTerminalUnix extends JFrame {

    private static Logger log = LoggerFactory.getLogger(WinTerminalUnix.class);
    private String user = ConfigAccessor.getInstance().getUnixUser();
    private String pwd = ConfigAccessor.getInstance().getUnixPassword();

    private JETabbedPane consoleTabPane;

    public static void main(String[] ars) {
        URL url = CommonUtil.getURLInClasspath("log4j.xml");
        DOMConfigurator.configure(url);
        try {
            UIManager.setLookAndFeel(new PlasticLookAndFeel());
            new WinTerminalUnix().setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("App Error", e);
        }
    }

    public WinTerminalUnix() {
        initialise();
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
                String host = JOptionPane.showInputDialog(WinTerminalUnix.this, "Give me your host name");
                openSession(host, user, pwd);
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
            log.info("Closed Session {} - {}", new String[]{consoleTabPane.getTitleAt(index), pwd});
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean openSession(String host, String user, String pwd) {
        if (StringUtils.isNotBlank(host)) {
            try {
                PtySettingsProvider mySettingsProvider = new PtySettingsProvider();
                JediTermWidget terminal = new JediTermWidget(new PtySettingsProvider());
                if (terminal.canOpenSession()) {
                    PtyProcess process = new RUnixPtyProcess(host, user, pwd);
                    TtyConnector ttyConnector = new PtyProcessTtyConnector(process, Charset.defaultCharset());
                    TerminalSession session = terminal.createTerminalSession(ttyConnector);
                    session.start();

                    consoleTabPane.addTab(user + "@" + host, terminal);
                    consoleTabPane.setSelectedIndex(consoleTabPane.getTabCount() - 1);

                    regExistAction(mySettingsProvider, ttyConnector, terminal);

                    log.info("Opened Session {}@{} - {}", new String[]{user, host, pwd});
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

    private void regExistAction(final PtySettingsProvider mySettingsProvider, final TtyConnector ttyConnector, final JediTermWidget terminal) {
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
