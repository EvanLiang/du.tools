package du.tools.main.widgets.console.cmd;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CmdTextArea extends JTextArea {

    private static final String EXIT = "exit";
    private static final String ENTER = "\r\n";

    private Process proConsole;
    private boolean exit = false;
    private boolean running = false;
    private boolean finish = false;
    private File dir;

    public CmdTextArea() {
        setFont(new Font("monospaced", 0, 14));
        setEditable(false);
    }

    private void runCommand(File dir, String... commands) throws Exception {
        this.setText("");
        System.out.println("startCommand");
        this.dir = dir;
        exit = false;
        finish = false;
        running = false;
        proConsole = createProcess(dir);
        startInputThread(proConsole.getOutputStream(), commands);
        startOutputThread(proConsole.getInputStream());
    }

    private void stopCommand() throws IOException, InterruptedException {
        exit = true;
        execute(proConsole.getOutputStream(), EXIT);
        Thread.sleep(1000);
        proConsole.destroy();
        System.out.println("stopCommand");
    }

    private Process createProcess(File dir) throws IOException {
        ProcessBuilder pb = new ProcessBuilder();
        pb.redirectErrorStream(true);
        pb.directory(dir);
        pb.command("cmd.exe");
        return pb.start();
    }

    private void startInputThread(final OutputStream outputStream, final String... commands) {
        Runnable runnable = new Runnable() {
            public void run() {
                try {
                    int i = 0;
                    //Execute command one by one, continue on finish last command
                    while (!exit && i < commands.length) {
                        if (!running) {
                            execute(outputStream, commands[i]);
                            running = true;
                            i++;
                        } else {
                            Thread.sleep(200);
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

    private void execute(OutputStream outputStream, String command) throws IOException {
        System.out.println("execute:" + command);
        outputStream.write(command.getBytes());
        if (!command.endsWith(ENTER)) {
            outputStream.write(ENTER.getBytes());
        }
        outputStream.flush();
    }

    private void startOutputThread(final InputStream inputStream) throws InterruptedException {
        Runnable runnable = new Runnable() {
            public void run() {
                try {
                    StringBuilder sb = new StringBuilder();
                    int skip = 3;
                    while (!exit) {

                        int b = inputStream.read();
                        sb.append((char) b);

                        //To see if command is finished
                        if (b == 62) {//'>'
                            if (sb.toString().equals(dir.getAbsolutePath() + ">")) {
                                running = false;
                                System.out.println("Finish run!");
                                if (finish) {
                                    //Destroy if whole commands are finished
                                    stopCommand();
                                }
                            }
                        }

                        if (b == 10) { //Is a EOL
                            if (sb.toString().startsWith(dir.getAbsolutePath() + ">")) {
                                //Is command text then skip this and next output line
                                skip = 2;
                            } else {
                                //Only skip the next empty line
                                if (skip == 1 && !sb.toString().equals(ENTER)) {
                                    skip = 0;
                                }
                            }

                            if (skip == 0) {
                                //Append line to TextArea
                                CmdTextArea.this.append(sb.toString());
                                CmdTextArea.this.setCaretPosition(CmdTextArea.this.getText().length());
                            }

                            sb = new StringBuilder();
                            if (skip > 0) skip--;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
        thread.join();
    }

    public static void main(String args[]) throws Exception {
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        f.setSize(1000, 600);
        f.setLocationRelativeTo(null);

        CmdTextArea c = new CmdTextArea();

        f.getContentPane().add(new JScrollPane(c));

        f.setVisible(true);

        Thread.sleep(2000);

        String[] cmd = new String[]{"set JAVA_HOME=C:\\BEA92\\env\\Java\\jdk1.7.0_10",
                "set MAVEN_HOME=C:\\BEA92\\env\\apache-maven-3.0.4",
                "set PATH=%JAVA_HOME%\\bin;%MAVEN_HOME%\\bin;%GIT_HOME%\\bin;%PATH%",
                "set env=DEV1", "mvn clean test"};

        //c.runCommand(new File("C:\\BEA92\\INT_BTCH\\MgCommon"), cmd);
        c.runCommand(new File("E:\\WS\\Library\\Common\\"), "set env=DEV1", "mvn clean", "mvn test");
        c.runCommand(new File("E:\\WS\\Library\\Common\\"), "set env=DEV1", "mvn clean", "mvn test");
    }
}