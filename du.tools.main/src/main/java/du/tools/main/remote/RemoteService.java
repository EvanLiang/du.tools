package du.tools.main.remote;

import com.jcraft.jsch.*;
import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class RemoteService {

    private static Logger log = LoggerFactory.getLogger(RemoteService.class);

    private Session session;
    private ChannelSftp sftp;

    public static void main(String[] args) throws Exception {
        try {
            final URL url = RemoteService.class.getResource("log4j.xml");
            System.out.println(url);
            DOMConfigurator.configure(url);

            RemoteService rs = new RemoteService();
            rs.connect("", "", "");

            List<ScpFile> l = new ArrayList<>();
            l.add(new ScpFile("E:\\test.txt", "/home/git/test.txt", ScpFile.ScpType.UPLOAD));
            rs.scp(l);

            RFile[] list = rs.ls("/home/git");
            System.out.println(list.length);
            for (RFile f : list) {
                System.out.println(f.getName());
                if ("test.txt".equals(f.getName())) {
                    System.out.println(f.delete());
                }
            }

            System.out.println(rs.sshexec("/home/git/test.ksh"));

            rs.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    public void connect(String host, String user, String password) throws Exception {
        JSch jsch = new JSch();
        session = jsch.getSession(user, host, 22);
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();
        log.debug("[JSch] connect");
    }

    public void disconnect() {
        if (sftp != null) {
            sftp.disconnect();
            log.debug("[scp] disconnect");
        }
        session.disconnect();
        log.debug("[JSch] disconnect");
    }

    public void scp(List<ScpFile> files) throws Exception {
        for (ScpFile file : files) {
            if (file.ind == ScpFile.ScpType.DOWNLOAD) {
                getSftp().get(file.src, file.des);
                log.debug("[scp] get file from remote<{}> to local<{}>", new Object[]{file.src, file.des});
            }
            if (file.ind == ScpFile.ScpType.UPLOAD) {
                getSftp().put(file.src, file.des);
                log.debug("[scp] put file from local<{}> to remote<{}>", new Object[]{file.src, file.des});
            }
        }
    }

    public String sshexec(String command) {
        try {
            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setPty(true);//Importance
            channel.setCommand(command);
            channel.setInputStream(null);
            channel.setErrStream(System.err);
            InputStream in = channel.getInputStream();
            channel.connect();
            log.debug("[sshexec] connect");
            log.debug("[sshexec] cmd:{}", command);
            StringBuilder sb = new StringBuilder();
            byte[] tmp = new byte[1024];
            while (true) {
                while (in.available() > 0) {
                    int i = in.read(tmp);
                    if (i < 0) break;
                    sb.append(new String(tmp, 0, i));
                }
                if (channel.isClosed()) {
//                    System.out.println("exit-status: " + channel.getExitStatus());
                    break;
                }
                Thread.sleep(100);
            }
            channel.disconnect();
            log.debug("[sshexec] disconnect");
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private ChannelSftp getSftp() throws JSchException {
        if (sftp == null) {
            log.debug("[scp] connect");
            sftp = (ChannelSftp) session.openChannel("sftp");
            sftp.connect();
        }
        return sftp;
    }

    public RFile[] ls(String path) {
        try {
            final java.util.Vector<ChannelSftp.LsEntry> v = new Vector<>();
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
                log.debug("[ls] {}", path);
                return list.toArray(new RFile[list.size()]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public class RFile {
        private String path;
        private String name;
        private RFile parent;
        private ChannelSftp.LsEntry entry;

        public RFile(ChannelSftp.LsEntry entry, String path) {
            this.entry = entry;
            this.path = path;
            if (entry.getFilename().equals(".")) {
                int index = path.lastIndexOf("/");
                name = path.substring(index + 1);
            }
        }

        public RFile(RFile parent, ChannelSftp.LsEntry entry) {
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
            return ls(getAbsolutePath());
        }

        public int getPermissions() {
            return entry.getAttrs().getPermissions();
        }

        public boolean setPermissions(int permissions) {
            try {
                sftp.chmod(permissions, getAbsolutePath());
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        public boolean makdir() {
            try {
                sftp.mkdir(getAbsolutePath());
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
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
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    public static class ScpFile {
        public enum ScpType {
            UPLOAD, DOWNLOAD
        }

        private String src;
        private String des;
        private ScpType ind;

        public ScpFile(String src, String des, ScpType ind) {
            this.src = src;
            this.des = des;
            this.ind = ind;
        }
    }

}
