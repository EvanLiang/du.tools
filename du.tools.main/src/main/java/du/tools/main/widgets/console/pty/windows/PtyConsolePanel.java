package du.tools.main.widgets.console.pty.windows;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jediterm.pty.PtyProcessTtyConnector;
import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.ui.JediTermWidget;
import com.jediterm.terminal.ui.TerminalAction;
import com.jediterm.terminal.ui.TerminalSession;
import com.jediterm.terminal.ui.settings.SettingsProvider;
import com.pty4j.PtyProcess;
import du.tools.main.ConfigAccessor;
import du.tools.main.widgets.console.pty.PtySettingsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.KeyEvent;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

public class PtyConsolePanel extends JediTermWidget {

    private static Logger log = LoggerFactory.getLogger(PtyConsolePanel.class);
    private SettingsProvider settingsProvider;

    public PtyConsolePanel(String workingDir) {
        this(new PtySettingsProvider(), workingDir);
    }

    public PtyConsolePanel(SettingsProvider settingsProvider, String workingDir) {
        super(settingsProvider);
        this.settingsProvider = settingsProvider;
        if (workingDir == null) {
            workingDir = ConfigAccessor.getInstance().getLocalRepo();
        }
        log.info("PtyConsolePanel - session.start()");
        TerminalSession session = createTerminalSession(createTtyConnector(workingDir));
        session.start();
    }

    private TtyConnector createTtyConnector(String workingDir) {
        try {
            log.info("createTtyConnector()");
            Map<String, String> envs = Maps.newHashMap(System.getenv());
            envs.put("TERM", "xterm");

            String[] command = new String[]{ConfigAccessor.getInstance().getTerminalPath()};
            PtyProcess process = PtyProcess.exec(command, envs, workingDir);
            log.info("createTtyConnector(): " + process.toString());
            return new PtyProcessTtyConnector(process, Charset.defaultCharset());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

//    @Override
//    protected JScrollBar createScrollBar() {
//        JScrollBar scrollBar = super.createScrollBar();
//        scrollBar.setBackground(new Color(60, 63, 65));
//        scrollBar.setBorder(null);
//        return scrollBar;
//    }

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
                        //WinTerminalWindows.dispose();
                        return true;
                    }
                });
        return Lists.newArrayList(action);
    }

}
