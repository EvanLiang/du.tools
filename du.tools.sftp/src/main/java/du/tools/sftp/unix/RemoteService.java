package du.tools.sftp.unix;

import com.jcraft.jsch.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class RemoteService {

    private static Logger log = LoggerFactory.getLogger(RemoteService.class);

    private Session session;
    private ChannelSftp sftp;

    public void connect(String host, String user, String password) throws Exception {
        JSch jsch = new JSch();
        session = jsch.getSession(user, host, 22);
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");
        log.debug("[JSch] Connecting to " + host);
        session.connect();
    }

    public void disconnect() {
        if (sftp != null) {
            sftp.disconnect();
            log.debug("[sftp] Disconnect.");
        }
        session.disconnect();
        log.debug("[JSch] Disconnect.");
    }

    public void getFile(String src, String des) throws Exception {
        log.debug("[sftp] Get file from <{}> to <{}>", new Object[]{src, des});
        getSftp().get(src, des);
        log.debug("[sftp] Done.");
    }

    public void putFile(String src, String des) throws Exception {
        log.debug("[sftp] Put file from <{}> to <{}>", new Object[]{src, des});
        getSftp().put(src, des);
        log.debug("[sftp] Done.");
    }

    public String sshexec(String command) throws Exception {
        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setPty(true);//Important, run as a terminal
        channel.setCommand(command);
        channel.setInputStream(null);
        channel.setErrStream(System.err);
        OutputStream out = channel.getOutputStream();
        InputStream in = channel.getInputStream();
        log.debug("[sshexec] Connecting");
        channel.connect();
        log.debug("[sshexec] cmd: {}", command);
        StringBuilder sb = new StringBuilder();
        byte[] tmp = new byte[1024];
        while (true) {
            while (in.available() > 0) {
                int i = in.read(tmp);
                if (i < 0) break;
                sb.append(new String(tmp, 0, i));
                //System.out.println(new String(tmp, 0, i));
                out.write("ls\n".getBytes());
                out.flush();
            }
            if (channel.isClosed()) {
//                    System.out.println("exit-status: " + channel.getExitStatus());
                break;
            }
            Thread.sleep(100);
        }
        log.debug("[sshexec] Disconnect");
        channel.disconnect();
        return sb.toString();
    }

    private ChannelSftp getSftp() throws JSchException {
        if (sftp == null) {
            log.debug("[sftp] Connecting");
            sftp = (ChannelSftp) session.openChannel("sftp");
            sftp.connect();
        }
        return sftp;
    }

    public RFile[] ls(String path) throws JSchException, SftpException {
        final Vector<ChannelSftp.LsEntry> v = new Vector<>();
        getSftp().ls(path, new ChannelSftp.LsEntrySelector() {
            public int select(ChannelSftp.LsEntry entry) {
                if (!entry.getAttrs().isLink()
                    //&& !entry.getAttrs().isReg()
                    //&& !entry.getAttrs().isBlk()
                    //&& !entry.getAttrs().isFifo()
                    //&& !entry.getAttrs().isChr()
                    //&& !entry.getAttrs().isSock()
                        ) {
                    v.addElement(entry);
                }
                return 0;
            }
        });
        if (v.size() > 0) {
            RFile parent = null;
            for (ChannelSftp.LsEntry e : v) {
                if (e.getFilename().equals(".")) {
                    parent = new RFile(e, path);
                    break;
                }
            }
            List<RFile> list = new ArrayList<>();
            for (ChannelSftp.LsEntry e : v) {
                if (!e.getFilename().startsWith(".")) {
                    list.add(new RFile(parent, e));
                }
            }
            log.debug("[sfpt] ls {}", path);
            return list.toArray(new RFile[list.size()]);
        }
        return null;
    }

    private ChannelSftp.LsEntry getFileEntry(String path) {
        try {
            final Vector<ChannelSftp.LsEntry> v = new Vector<>();
            getSftp().ls(path, new ChannelSftp.LsEntrySelector() {
                public int select(ChannelSftp.LsEntry entry) {
                    if (!entry.getAttrs().isLink()) {
                        v.addElement(entry);
                    }
                    return 0;
                }
            });
            if (v.size() > 0) {
                for (ChannelSftp.LsEntry e : v) {
                    if (e.getFilename().equals(".")) {
                        return e;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public class RFile extends File {
        private String path;
        private String name;
        private RFile parent;
        private ChannelSftp.LsEntry entry;

        public RFile(String path) {
            this(getFileEntry(path), path);
        }

        public RFile(ChannelSftp.LsEntry entry, String path) {
            super("");
            this.entry = entry;
            this.path = path;
            if (entry.getFilename().equals(".")) {
                if (path.length() == 1) {
                    name = path;
                } else {
                    int index = path.lastIndexOf("/");
                    name = path.substring(index + 1);
                }
            }
        }

        public RFile(RFile parent, ChannelSftp.LsEntry entry) {
            super("");
            this.parent = parent;
            this.entry = entry;
            this.name = entry.getFilename();
        }

        public String getName() {
            return name;
        }

        public String getAbsolutePath() {
            if (parent != null) {
                return parent.getAbsolutePath() + "/" + getName();
            } else {
                return path;
            }
        }

        public RFile getParentFile() {
            return parent;
        }

        public String getParent() {
            return parent.getName();
        }

        public boolean isFile() {
            return !entry.getAttrs().isDir();
        }

        public long length() {
            return entry.getAttrs().getSize();
        }

        public boolean isDirectory() {
            return entry.getAttrs().isDir();
        }

        public RFile[] listFiles() {
            try {
                return ls(getAbsolutePath());
            } catch (JSchException | SftpException e) {
                e.printStackTrace();
                return null;
            }
        }

        public long lastModified() {
            return entry.getAttrs().getMTime();
        }

        public boolean delete() {
            try {
                if (isDirectory()) {
                    sftp.rmdir(getAbsolutePath());
                } else {
                    sftp.rm(getAbsolutePath());
                }
                return true;
            } catch (SftpException e) {
                e.printStackTrace();
                return false;
            }
        }

        public int getPermissions() {
            return entry.getAttrs().getPermissions();
        }

        public void setPermissions(int permissions) throws SftpException {
            sftp.chmod(permissions, getAbsolutePath());
        }

        public void makdir() throws SftpException {
            sftp.mkdir(getAbsolutePath());
        }
    }

}
