package du.tools.main.widgets.unix;


import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class ShellConnector {
    private static final String ENTER = "\n";

    private String host;
    private String user;
    private String password;

    Session session;
    ChannelShell channel;
    InputStreamReader in;
    OutputStream out;

    public ShellConnector(String host, String user, String password) {
        this.host = host;
        this.user = user;
        this.password = password;
    }

    public void connect() throws Exception {
        JSch jsch = new JSch();
        session = jsch.getSession(user, host, 22);
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();
        channel = (ChannelShell) session.openChannel("shell");
        in = new InputStreamReader(channel.getInputStream());
        out = channel.getOutputStream();
        channel.connect();
    }

    public boolean isRunning() {
        return channel != null && !channel.isClosed();
    }

    public void disconnect() throws IOException {
        channel.disconnect();
        session.disconnect();
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

    public String getHost() {
        return host;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }
}
