package du.tools.main.windows;

import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.ui.JediTermWidget;
import com.jediterm.terminal.ui.TerminalSession;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import du.swingx.JETabbedPane;
import du.tools.main.ConfigAccessor;
import du.tools.main.commons.utils.CommonUtil;
import du.tools.main.widgets.console.pty.PtySettingsProvider;
import du.tools.main.widgets.console.pty.unix.ShellRemoteTtyConnector;
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
                JediTermWidget terminal = new JediTermWidget(new PtySettingsProvider());
                if (terminal.canOpenSession()) {
                    TtyConnector ttyConnector = new ShellRemoteTtyConnector(host, user, pwd, Charset.defaultCharset());
                    TerminalSession session = terminal.createTerminalSession(ttyConnector);
                    session.start();

                    consoleTabPane.addTab(user + "@" + host, terminal);
                    consoleTabPane.setSelectedIndex(consoleTabPane.getTabCount() - 1);

                    log.info("Opened Session {}@{} - {}", new String[]{user, host, pwd});
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
