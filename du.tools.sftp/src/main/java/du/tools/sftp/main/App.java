package du.tools.sftp.main;

import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import du.tools.commons.Utils;
import du.tools.sftp.windows.WinSftpExplorer;
import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

public class App {
    private static Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String[] ars) {
        DOMConfigurator.configure(Utils.getClasspathURL("log4j.xml"));
        try {
            UIManager.setLookAndFeel(new PlasticLookAndFeel());
            new WinSftpExplorer().setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("App Error", e);
        }
    }
}
