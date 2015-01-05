package du.tools.main.windows;


import du.tools.main.Constants;
import du.tools.main.stash.Stash;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class WinStash extends JFrame {

    private static Logger log = LoggerFactory.getLogger(WinStash.class);

    private TextArea ta = new TextArea();

    public WinStash() throws Exception {
        setSize(500, 50);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setAlwaysOnTop(true);
        setLocationRelativeTo(null);
        ta.setEditable(false);
        getContentPane().add(new JScrollPane(ta));
        setVisible(true);
        loadProjectXml();
        setVisible(false);
    }

    private void loadProjectXml() throws Exception {
        File tmpDir = new File(Constants.APP_TMP_DIR);
        if (!tmpDir.exists()) {
            if (!tmpDir.mkdirs()) {
                log.error("Make dir error:" + tmpDir.getAbsolutePath());
                return;
            }
        }
        File ptFile = new File(Constants.PROJECT_TREE_XML);
        if (!ptFile.exists()) {
            String xml = new Stash(this).getProjectsXml();
            FileUtils.writeStringToFile(ptFile, xml);
        }
    }

    public void showMsg(String msg) {
        ta.setText(msg);
    }
}
