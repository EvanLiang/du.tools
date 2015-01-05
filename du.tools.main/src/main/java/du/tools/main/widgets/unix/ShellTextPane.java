package du.tools.main.widgets.unix;

import du.swingx.JETextPane;

import javax.swing.*;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;

public class ShellTextPane extends JETextPane {

    private static Font defFont = new Font("monospaced", 0, 14);

    private ShellConnector shell;
    private boolean exit = false;
    private boolean finish = false;
    private boolean running = false;

    public ShellTextPane(String host, String user, String password) {
        setFont(defFont);
        setEditable(false);
        shell = new ShellConnector(host, user, password);
    }

    public void runCommand(String... commands) throws Exception {
        if (isRunning()) {
            System.out.println("Current command is not finished yet.");
            return;
        }
        setText("");
        System.out.println("startCommand");
        exit = false;
        finish = false;
        running = true;

        shell.connect();
        startOutputThread();
        startInputThread(commands);
    }

    public boolean isRunning() {
        return shell.isRunning();
    }

    public void stopCommand() throws IOException {
        exit = true;
        shell.disconnect();
        System.out.println("shell close");
    }

    private void startInputThread(final String... commands) {
        Runnable runnable = new Runnable() {
            public void run() {
                try {
                    int i = 0;
                    //Execute command one by one, continue on finish last command
                    while (!exit && i < commands.length) {
                        if (!running) {
                            shell.sendString(commands[i]);
                            running = true;
                            i++;
                        } else {
                            Thread.sleep(100);
                        }
                    }
                    //Indicated the whole commands are execute, so that can destroy the process
                    finish = true;
                    System.out.println("Input Thread finished.");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        new Thread(runnable).start();
    }

    private void startOutputThread() {
        Runnable runnable = new Runnable() {
            private int length;
            private int offset;
            private char[] buffer = new char[1024];
            private String line = "";

            private char getChar() throws IOException {
                if (offset == length) {
                    fillBuffer();
                }
                return buffer[offset++];
            }

            private void fillBuffer() throws IOException {
                length = shell.read(buffer);
                offset = 0;
            }

            public void run() {
                try {
                    while (!exit) {
                        char ch = getChar();
                        line += ch;
                        if (offset == length) {
                            if (line.equals("$ ") || line.matches("\\[(.*)@(.*) (.*)\\]\\$ ")) {
                                running = false;
                                System.out.println("command end");
                                if (finish) {
                                    stopCommand();
                                }
                            }
                        }
                        if (ch == '\n') {
                            append(line);
                            line = "";
                        }
                    }
                    System.out.println("Output Thread finished.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        running = true;
        new Thread(runnable).start();
    }

    public void carriageReturn() {
        setCaretPosition(getDocument().getLength());
    }

    public void appendWithColor(final String text, Color color) {
        MutableAttributeSet style = new SimpleAttributeSet();
        StyleConstants.setForeground(style, color);
        int offset = getDocument().getLength();
        super.append(text);
        select(offset, offset + text.length());
        setCharacterAttributes(style, false);
        carriageReturn();
    }

    public static void main(String args[]) throws IOException, InterruptedException {
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        f.setSize(1000, 600);
        f.setLocationRelativeTo(null);
        f.getContentPane().setLayout(new BorderLayout());

        final ShellTextPane c = new ShellTextPane("dsrcdev1-adm", "liange", "Nov13w104");
        f.getContentPane().add(new JScrollPane(c, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);

        JButton jb = new JButton("EXEC");
        jb.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    //c.runCommand("who", "su - dtgtdev1", "cd /wl/dtgt/logs", "ls -rlt");
                    c.runCommand("su - dsrcdev1", "ps -ef | grep java | grep weblogic.Server| awk '{print $2}' | xargs kill -9", "cd ~/dc", "./startAdminServer.ksh");
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
        f.getContentPane().add(jb, BorderLayout.SOUTH);

        f.setVisible(true);

//        String[] cmd = new String[]{"set JAVA_HOME=C:\\BEA92\\env\\Java\\jdk1.7.0_10",
//                "set MAVEN_HOME=C:\\BEA92\\env\\apache-maven-3.0.4",
//                "set PATH=%JAVA_HOME%\\bin;%MAVEN_HOME%\\bin;%GIT_HOME%\\bin;%PATH%",
//                "set env=DEV1", "mvn clean test"};
//
//        c.runCommand(new File("C:\\BEA92\\INT_BTCH\\MgCommon"), cmd);


//        Thread.sleep(8000);
//        c.runCommand(new File("E:\\WS\\Library\\Common\\"), "set env=DEV1", "mvn clean install");

    }

    public void destroy() {
        try {
            stopCommand();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
