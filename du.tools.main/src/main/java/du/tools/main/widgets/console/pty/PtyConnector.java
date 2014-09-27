package du.tools.main.widgets.console.pty;

import com.pty4j.PtyProcess;
import com.pty4j.WinSize;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class PtyConnector {

    private static final String ENTER = "\r\n";

    private PtyProcess ptyProcess;
    private OutputStream out;
    private InputStreamReader in;

    public void connect(File dir) throws IOException {
        Map<String, String> envs = new HashMap<>(System.getenv());
        envs.put("TERM", "xterm");
        String[] command = new String[]{"cmd.exe"};
        ptyProcess = PtyProcess.exec(command, envs, dir.getAbsolutePath());
        in = new InputStreamReader(ptyProcess.getInputStream());
        out = ptyProcess.getOutputStream();
    }

    public void disconnect() throws IOException {
        if (ptyProcess != null) {
            sendBytes(new byte[]{9});
            sendString("EXIT");
            ptyProcess.destroy();
        }
    }

    public void resize(int columns, int rows) {
        if (ptyProcess != null && ptyProcess.isRunning()) {
            ptyProcess.setWinSize(new WinSize(columns, rows));
        }
    }

    public void sendString(String str) throws IOException {
        out.write(str.getBytes());
        if (!str.endsWith(ENTER)) {
            out.write(ENTER.getBytes());
        }
        out.flush();
    }

    public void sendBytes(byte[] bytes) throws IOException {
        out.write(bytes);
        out.flush();
    }

    public int read(char[] buffer) throws IOException {
        return in.read(buffer);
    }

    public boolean isRunning() {
        return ptyProcess != null && ptyProcess.isRunning();
    }
}
