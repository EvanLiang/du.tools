package du.tools.main.widgets.console.pty;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

public class PtyTextArea extends JTextArea implements Terminal {

    private PtyConnector pty;
    private boolean exit = false;
    private boolean finish = false;
    private boolean running = false;
    private File dir;

    public PtyTextArea() {
        setFont(new Font("monospaced", 0, 14));
        setEditable(false);
        pty = new PtyConnector();
    }

    private void setPtySize() {
        int columns = getWidth() / getColumnWidth();
        pty.resize(columns - 1, 100);
    }

    public void runCommand(File dir, String... commands) throws IOException {
        if (!dir.exists()) {
            throw new IllegalArgumentException("Working dir does not exist: " + dir.getAbsolutePath());
        }
        if (isRunning()) {
            System.out.println("Current command is not finished yet.");
            return;
        }
        setText("");
        System.out.println("startCommand");
        this.dir = dir;
        exit = false;
        finish = false;
        running = true;

        pty.connect(dir);
        setPtySize();
        startOutputThread();
        startInputThread(commands);
    }

    public boolean isRunning() {
        return pty.isRunning();
    }

    public void stopCommand() throws IOException {
        exit = true;
        pty.disconnect();
        System.out.println("stopCommand");
    }

    private void startInputThread(final String... commands) {
        Runnable runnable = new Runnable() {
            public void run() {
                try {
                    int i = 0;
                    //Execute command one by one, continue on finish last command
                    while (!exit && i < commands.length) {
                        if (!running) {
                            pty.sendString(commands[i]);
                            running = true;
                            i++;
                        } else {
                            Thread.sleep(100);
                        }
                    }
                    //Indicated the whole commands are execute, so that can destroy the process
                    finish = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        new Thread(runnable).start();
    }

    private void startOutputThread() {
        Runnable runnable = new Runnable() {
            public void run() {
                XtermProcessor runner = new XtermProcessor(pty, PtyTextArea.this);
                try {
                    while (!exit) {
                        runner.process();
                    }
                    System.out.println("Thread finished.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        running = true;
        new Thread(runnable).start();
    }

    public void deleteLine() {
        try {
            int lastLine = getLineCount() - 1;
            int index = getLineStartOffset(lastLine);
            getDocument().remove(index, getDocument().getLength() - index);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public void carriageReturn() {
        setCaretPosition(getDocument().getLength());
    }

    public void append(final String text) {
        if (text.equals(dir.getAbsolutePath() + ">")) {
            running = false;
            if (finish) {
                try {
                    stopCommand();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        super.append(text);
    }

    public static void main(String args[]) throws IOException, InterruptedException {
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        f.setSize(1000, 600);
        f.setLocationRelativeTo(null);
        f.getContentPane().setLayout(new BorderLayout());

        final PtyTextArea c = new PtyTextArea();
        f.getContentPane().add(new JScrollPane(c, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);

        JButton jb = new JButton("EXEC");
        jb.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    c.runCommand(new File("E:\\WS\\Project\\du.swingx\\"), "set env=DEV1", "mvn clean install");
                } catch (IOException e1) {
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