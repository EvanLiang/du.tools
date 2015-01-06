package du.tools.main.widgets.console.pty.unix;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.pty4j.PtyProcess;
import com.pty4j.WinSize;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class RUnixPtyProcess extends PtyProcess {

    private InputStream inputStream;
    private OutputStream outputStream;
    private InputStream errStream;

    private Session session;
    private ChannelShell channel;

    public RUnixPtyProcess(String host, String user, String pwd) throws JSchException, IOException {
        JSch jsch = new JSch();
        session = jsch.getSession(user, host, 22);
        session.setPassword(pwd);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();

        channel = (ChannelShell) session.openChannel("shell");
        channel.setEnv("TERM", "xterm");
        channel.connect();

        outputStream = channel.getOutputStream();
        inputStream = channel.getInputStream();
    }

    @Override
    public boolean isRunning() {
        return channel.getExitStatus() < 0 && channel.isConnected();
    }

    @Override
    public void setWinSize(WinSize winSize) {
        channel.setPtySize(winSize.ws_col, winSize.ws_row, winSize.ws_xpixel, winSize.ws_ypixel);
    }

    @Override
    public WinSize getWinSize() throws IOException {
        return null;
    }

    @Override
    public InputStream getInputStream() {
        return inputStream;
    }

    @Override
    public OutputStream getOutputStream() {
        return outputStream;
    }

    @Override
    public InputStream getErrorStream() {
        return errStream;
    }

    @Override
    public int waitFor() throws InterruptedException {
        while (isRunning())
            Thread.sleep(100);
        return exitValue();
    }

    @Override
    public int exitValue() {
        if (isRunning())
            throw new IllegalStateException();
        return channel.getExitStatus();
    }

    @Override
    public void destroy() {
        if (channel.isConnected()) {
            channel.disconnect();
        }
        if (session.isConnected()) {
            session.disconnect();
        }
    }
}
