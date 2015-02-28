package du.tools.mq.main;

import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import du.tools.sftp.windows.WinExplorer;
import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.net.URL;

public class App {
    private static Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String[] ars) {
        URL url = ClassLoader.getSystemClassLoader().getResource("log4j.xml");
        DOMConfigurator.configure(url);
        try {
            UIManager.setLookAndFeel(new PlasticLookAndFeel());
            new WinMQueue().setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("App Error", e);
        }
    }
}
