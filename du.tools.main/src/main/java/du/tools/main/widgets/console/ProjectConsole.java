package du.tools.main.widgets.console;

import lib.common.utils.CommonUtil;
import lib.common.utils.IconUtil;
import org.apache.commons.lang.StringUtils;
import du.tools.main.ConfigAccessor;
import du.tools.main.widgets.PView;
import du.tools.main.widgets.console.pty.PtyTextPane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;

public class ProjectConsole extends JPanel {

    private Color staticFg = new Color(60, 63, 65);
    private Color staticBg = new Color(183, 206, 228);
    private Color hoverFg = new Color(255, 255, 255);
    private Color hoverBg = new Color(152, 172, 193);

    private JPanel envPanel;
    private JPanel btnPanel;
    private PtyTextPane console;
    private PView parent;

    public ProjectConsole(PView parent) {
        this.parent = parent;
        initialize();
    }

    private void initialize() {
        setLayout(new BorderLayout());

        createEnvButtons();
        createActionButtons();

        console = new PtyTextPane();
        add(new JScrollPane(console));
    }

    private void createEnvButtons() {
        FlowLayout layout = new FlowLayout(FlowLayout.LEFT);
        layout.setVgap(0);
        layout.setHgap(0);

        envPanel = new JPanel();
        envPanel.setLayout(layout);
        envPanel.setBackground(staticBg);
        add(envPanel, BorderLayout.NORTH);

        Image iIcon = IconUtil.generate("X", Color.RED, staticBg);
        Image rIcon = IconUtil.generate("X", Color.RED, hoverBg);
        IconButton btnClose = new IconButton("Close Console", iIcon, rIcon);
        btnClose.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                parent.closeConsole();
            }
        });
        envPanel.add(btnClose);

        java.util.List<String> envs = ConfigAccessor.getInstance().getConsoleEnvButtons();
        for (String env : envs) {
            IconButton button = new IconButton(env, ConfigAccessor.getInstance().getConsoleEnvButtonDescription(env));
            envPanel.add(button);
            button.addActionListener(new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    for (Component c : envPanel.getComponents()) {
                        if (c instanceof IconButton) {
                            IconButton btn = (IconButton) c;
                            btn.setSelected(false);
                        }
                    }
                    IconButton btn = (IconButton) e.getSource();
                    btn.setSelected(true);
                }
            });
        }
    }

    private void createActionButtons() {
        FlowLayout layout = new FlowLayout(FlowLayout.LEFT);
        layout.setVgap(0);
        layout.setHgap(0);

        btnPanel = new JPanel();
        this.add(btnPanel, BorderLayout.SOUTH);
        btnPanel.setLayout(layout);
        btnPanel.setBackground(staticBg);

        Image iIcon = IconUtil.generate("S", Color.RED, staticBg);
        Image rIcon = IconUtil.generate("S", Color.RED, hoverBg);
        IconButton btnStop = new IconButton("Stop execution", iIcon, rIcon);
        btnStop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (console.isRunning()) {
                    console.destroy();
                    console.appendWithColor("\nExecution stopped!", Color.RED);
                }
            }
        });
        btnPanel.add(btnStop);

        java.util.List<String> buttons = ConfigAccessor.getInstance().getConsoleButtonNames();
        if (buttons != null) {
            for (String button : buttons) {
                String toolTip = ConfigAccessor.getInstance().getConsoleButtonDescription(button);
                IconButton btn = new IconButton(button, toolTip);
                btnPanel.add(btn);
                btn.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        console.setText("");
                        // Highlight the button clicked
                        for (Component c : btnPanel.getComponents()) {
                            if (c instanceof IconButton) {
                                IconButton btn = (IconButton) c;
                                btn.setSelected(false);
                            }
                        }
                        IconButton btn = (IconButton) e.getSource();
                        btn.setSelected(true);

                        java.util.List<String> commands = new ArrayList<>();
                        File workingDir = parent.getRootDir();

                        java.util.List<String> cmds = ConfigAccessor.getInstance().getConsoleInitCommands();
                        if (cmds != null) {
                            commands.addAll(cmds);
                        }

                        // Get env command
                        for (Component c : envPanel.getComponents()) {
                            if (c instanceof IconButton) {
                                IconButton ib = (IconButton) c;
                                if (ib.isSelected()) {
                                    String env = ib.getName();
                                    cmds = ConfigAccessor.getInstance().getConsoleEnvButtonCommands(env);
                                    commands.addAll(cmds);
                                    break;
                                }
                            }
                        }

                        // Matching, to decide the domain/server
                        int count = ConfigAccessor.getInstance().countConsoleBefore();
                        for (int i = 1; i <= count; i++) {
                            java.util.List<String> dirMatches = ConfigAccessor.getInstance().getConsoleBeforeDirMatches(i);
                            for (String match : dirMatches) {
                                if (CommonUtil.toSlash(parent.getRootDir().getAbsolutePath()).matches(match)) {
                                    String cmd = ConfigAccessor.getInstance().getConsoleBeforeCommand(i);
                                    commands.add(cmd);
                                }
                            }
                        }

                        final String dir = ConfigAccessor.getInstance().getConsoleButtonWorkDir(btn.getName());
                        if (StringUtils.isNotBlank(dir)) {
                            File[] files = parent.getRootDir().listFiles(new FileFilter() {
                                public boolean accept(File file) {
                                    return file.isDirectory() && file.getName().matches(dir);
                                }
                            });
                            if (files == null || files.length == 0) {
                                console.appendWithColor("\nWorking directory does not exist, please check: " + dir, Color.RED);
                                return;
                            }
                            workingDir = files[0];
                        }

                        // The action command of button
                        cmds = ConfigAccessor.getInstance().getConsoleButtonCommands(btn.getName());
                        if (cmds == null || cmds.size() == 0) {
                            console.appendWithColor("\nNo action command to be run, please check!.", Color.RED);
                            return;
                        }
                        commands.addAll(cmds);

                        // Execute commands
                        try {
                            if (commands.size() > 1) {
                                console.runCommand(workingDir, commands.toArray(new String[]{""}));
                            }
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
            }
        }
    }


    public void destroy() {
        console.destroy();
    }

    class IconButton extends JButton {
        private String name;
        private String toolTip;
        private ImageIcon iIcon;
        private ImageIcon rIcon;
        private boolean selected;

        public IconButton(String name, String toolTip) {
            this.name = name;
            this.toolTip = toolTip;
            Image iImg = IconUtil.generate(name, staticFg, staticBg);
            Image rImg = IconUtil.generate(name, hoverFg, hoverBg);
            init(iImg, rImg);
        }

        public IconButton(String toolTip, Image iImg, Image rImg) {
            this.toolTip = toolTip;
            init(iImg, rImg);
        }

        public String getName() {
            return name;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
            if (selected) {
                setIcon(rIcon);
            } else {
                setIcon(iIcon);
            }
        }

        public boolean isSelected() {
            return selected;
        }

        private void init(Image iImg, Image rImg) {
            iIcon = new ImageIcon(iImg);
            rIcon = new ImageIcon(rImg);
            setIcon(iIcon);
            setRolloverIcon(rIcon);
            setPressedIcon(rIcon);
            setBorderPainted(false);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setFocusable(true);
            setMargin(new Insets(0, 0, 0, 0));
            setToolTipText(toolTip);
            setBorder(null);
        }
    }
}