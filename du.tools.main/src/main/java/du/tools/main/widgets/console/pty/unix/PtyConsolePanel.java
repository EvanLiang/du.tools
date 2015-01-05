package du.tools.main.widgets.console.pty.unix;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.ui.JediTermWidget;
import com.jediterm.terminal.ui.TerminalAction;
import com.jediterm.terminal.ui.TerminalSession;
import com.jediterm.terminal.ui.settings.SettingsProvider;
import du.tools.main.widgets.console.pty.PtySettingsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.nio.charset.Charset;
import java.util.List;

public class PtyConsolePanel extends JediTermWidget {

    private static Logger log = LoggerFactory.getLogger(PtyConsolePanel.class);
    private SettingsProvider settingsProvider;
    private JFrame ui;

    public PtyConsolePanel(JFrame ui, String host, String user, String passwd) {
        this(new PtySettingsProvider(), ui, host, user, passwd);
    }

    private PtyConsolePanel(SettingsProvider settingsProvider, JFrame ui, String host, String user, String passwd) {
        super(settingsProvider);
        this.settingsProvider = settingsProvider;
        this.ui = ui;
        log.info("PtyConsolePanel - session.start()");
        TerminalSession session = createTerminalSession(createTtyConnector(host, user, passwd));
        session.start();
    }

    private TtyConnector createTtyConnector(String host, String user, String passwd) {
        try {
            return new ShellRemoteTtyConnector(host, user, passwd, Charset.defaultCharset());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public void execute(String cmd) {
        if (!isSessionRunning()) {
            return;
        }
        if (!cmd.endsWith("\r\n")) {
            cmd += "\r\n";
        }
        getTerminalStarter().sendString(cmd);
    }

    public List<TerminalAction> getActions() {
        TerminalAction action = new TerminalAction("Close Session",
                settingsProvider.getCloseSessionKeyStrokes(),
                new Predicate<KeyEvent>() {
                    public boolean apply(KeyEvent keyEvent) {
                        System.out.println("ACTION");
                        close();
                        ui.setVisible(false);
                        return true;
                    }
                });
        return Lists.newArrayList(action);
    }

}
