package du.tools.main.windows;


import org.apache.log4j.xml.DOMConfigurator;
import du.tools.main.widgets.PMainPanel;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;

public class WinMain {

    public static JFrame frame = null;

    static {
        try {
//            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//            UIManager.setLookAndFeel(new com.jgoodies.looks.plastic.Plastic3DLookAndFeel());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        URL url = WinMain.class.getClassLoader().getResource("log4j.xml");
        DOMConfigurator.configure(url);
        new WinMain();
    }

    public WinMain() throws Exception {
        frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                System.exit(0);
            }
        });
        createComponents();

        frame.pack();
        frame.setLocationRelativeTo(null);

        frame.setVisible(true);
    }

    private void createComponents() {
        PMainPanel PMainPanel = new PMainPanel();
        frame.getContentPane().add(PMainPanel);
    }
}
