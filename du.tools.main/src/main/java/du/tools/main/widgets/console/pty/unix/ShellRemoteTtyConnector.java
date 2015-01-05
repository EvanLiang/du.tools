package du.tools.main.widgets.console.pty.unix;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jediterm.terminal.Questioner;
import com.jediterm.terminal.TtyConnector;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;

public class ShellRemoteTtyConnector implements TtyConnector {
    protected final InputStream myInputStream;
    protected final OutputStream myOutputStream;
    protected InputStreamReader myReader;
    protected Charset myCharset;
    private Dimension myPendingTermSize;
    private Dimension myPendingPixelSize;

    private String myHost;
    private Session mySession;
    private ChannelShell myShell;

    public ShellRemoteTtyConnector(String host, String user, String passwd, Charset charset) throws JSchException, IOException {
        myHost = host;
        myCharset = charset;
        JSch jsch = new JSch();
        mySession = jsch.getSession(user, host, 22);
        mySession.setPassword(passwd);
        mySession.setConfig("StrictHostKeyChecking", "no");
        mySession.connect();

        myShell = (ChannelShell) mySession.openChannel("shell");
        myShell.setEnv("TERM", "xterm");
        myShell.connect();
        myOutputStream = myShell.getOutputStream();
        myInputStream = myShell.getInputStream();
        myReader = new InputStreamReader(myInputStream, charset);
    }

    @Override
    public void resize(Dimension termSize, Dimension pixelSize) {
        setPendingTermSize(termSize);
        setPendingPixelSize(pixelSize);
        if (isConnected()) {
            resizeImmediately();
            setPendingTermSize(null);
            setPendingPixelSize(null);
        }
    }

    protected void resizeImmediately() {
        if (getPendingTermSize() != null && getPendingPixelSize() != null) {
            myShell.setPtySize(getPendingTermSize().width, getPendingTermSize().height,
                    getPendingPixelSize().width, getPendingPixelSize().height);
        }
    }

    public String getName() {
        return myHost;
    }

    public int read(char[] buf, int offset, int length) throws IOException {
        return myReader.read(buf, offset, length);
        //return myInputStream.read(buf, offset, length);
    }

    public void write(byte[] bytes) throws IOException {
        myOutputStream.write(bytes);
        myOutputStream.flush();
    }

    @Override
    public boolean isConnected() {
        return myShell.isConnected();
    }

    @Override
    public void write(String string) throws IOException {
        myOutputStream.write(string.getBytes(myCharset));
        myOutputStream.flush();
    }

    protected void setPendingTermSize(Dimension pendingTermSize) {
        this.myPendingTermSize = pendingTermSize;
    }

    protected void setPendingPixelSize(Dimension pendingPixelSize) {
        this.myPendingPixelSize = pendingPixelSize;
    }

    protected Dimension getPendingTermSize() {
        return myPendingTermSize;
    }

    protected Dimension getPendingPixelSize() {
        return myPendingPixelSize;
    }

    @Override
    public boolean init(Questioner q) {
        return isConnected();
    }

    @Override
    public void close() {
        myShell.disconnect();
        mySession.disconnect();
    }

    /**
     * Not use for SHELL
     *
     * @throws InterruptedException
     */
    @Override
    public int waitFor() throws InterruptedException {
        return 0;
    }
}
