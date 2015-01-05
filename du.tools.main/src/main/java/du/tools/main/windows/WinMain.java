package du.tools.main.windows;


import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import du.tools.main.ConfigAccessor;
import du.tools.main.commons.utils.CommonUtil;
import du.tools.main.widgets.main.PMainPanel;
import du.tools.main.widgets.main.PTreeNode;
import org.apache.log4j.xml.DOMConfigurator;
import org.jdesktop.swingx.JXBusyLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;

public class WinMain {

    private static Logger log = LoggerFactory.getLogger(WinMain.class);

    public static JFrame frame = null;
    public static JXBusyLabel busy = null;

    private PMainPanel mainPanel;

    public static void main(String[] args) {
        URL url = CommonUtil.getURLInClasspath("log4j.xml");
        DOMConfigurator.configure(url);
        try {
            UIManager.setLookAndFeel(new PlasticLookAndFeel());
            new WinMain();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("App Error", e);
        }
    }

    public WinMain() throws Exception {
        new WinStash();
        initialise();
    }

    private void initialise() throws Exception {
        log.info("initialise():Starting");
        frame = new JFrame();
        frame.setTitle("Work Space Tool");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e) {
                System.exit(0);
            }
        });

        mainPanel = new PMainPanel();
        frame.getContentPane().add(mainPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        createMenuBar();
        frame.setVisible(true);

        busy = new JXBusyLabel();
        int x = mainPanel.getWidth() / 2 - 15;
        int y = mainPanel.getHeight() / 2 - 15;
        busy.setBounds(x, y, 30, 30);
        busy.setBusy(true);
        busy.setVisible(false);
        frame.getLayeredPane().add(busy, 99999);

        frame.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                int x = mainPanel.getWidth() / 2 - 15;
                int y = mainPanel.getHeight() / 2 - 15;
                busy.setBounds(x, y, 30, 30);
            }
        });

        log.info("initialise():Finished");
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);
        menuBar.setPreferredSize(new Dimension(0, 25));

        JMenu menu = new JMenu("Tools");
        menuBar.add(menu);

        JMenuItem item = new JMenuItem("Windows Terminal");
        menu.add(item);
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    log.info("Terminal - WinTerminalWindows");
                    PTreeNode node = mainPanel.getpTrees().getSelectedNode();
                    if (node != null) {
                        log.info("WinTerminalWindows:" + node.getRepoPath());
                        new WinTerminalWindows(ConfigAccessor.getInstance().getLocalRepo() + node.getRepoPath());
                    } else {
                        log.info("WinTerminalWindows - ");
                        new WinTerminalWindows();
                    }
                } catch (Exception e1) {
                    log.info("WinTerminalWindows - ERROR");
                    log.error(e1.getMessage(), e1);
                }
            }
        });

        item = new JMenuItem("Unix Terminal");
        menu.add(item);
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                new WinTerminalUnix();
            }
        });

        item = new JMenuItem("MQueue");
        menu.add(item);
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    new WinMQueue();
                } catch (Exception e1) {
                    log.error(e1.getMessage(), e1);
                }
            }
        });

        item = new JMenuItem("Unix Explorer");
        menu.add(item);
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    new WinExplorer();
                } catch (Exception e1) {
                    log.error(e1.getMessage(), e1);
                }
            }
        });

        menu = new JMenu("Settings");
        menuBar.add(menu);
        item = new JMenuItem("ReLoad Config");
        menu.add(item);
        item.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                ConfigAccessor.getInstance().loadConfig();
            }
        });
    }

}
