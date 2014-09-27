package du.tools.main.widgets.console.jediterm;

import com.google.common.collect.Maps;
import com.jediterm.pty.PtyProcessTtyConnector;
import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.ui.JediTermWidget;
import com.jediterm.terminal.ui.TerminalSession;
import com.pty4j.PtyProcess;

import java.awt.*;
import java.nio.charset.Charset;
import java.util.Map;

public class ConsolePanel extends JediTermWidget {

    public ConsolePanel() {
        this(System.getProperty("user.dir"));
    }

    public ConsolePanel(String workingDir) {
        super(new SettingsProvider());
        setPreferredSize(new Dimension(300, 0));
        TerminalSession session = createTerminalSession(createTtyConnector(workingDir));
        session.start();
    }

    private TtyConnector createTtyConnector(String workingDir) {
        try {
            Map<String, String> envs = Maps.newHashMap(System.getenv());
            envs.put("TERM", "xterm");

            String[] command = new String[]{"cmd.exe"};
            PtyProcess process = PtyProcess.exec(command, envs, workingDir);
            return new PtyProcessTtyConnector(process, Charset.defaultCharset());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

}