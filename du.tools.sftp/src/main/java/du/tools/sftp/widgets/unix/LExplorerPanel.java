package du.tools.sftp.widgets.unix;

import du.swingx.JETreeTable;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;

public class LExplorerPanel extends DExplorerPanel<LFileNode> {

    public LExplorerPanel(String rootPath) {
        super(new JETreeTable(new LFileModel(new LFileNode(new File(rootPath)))));
    }

    protected java.util.List<JMenuItem> getActions() {
        java.util.List<JMenuItem> actions = new ArrayList<>();
        JMenuItem mntmDownload = new JMenuItem("Upload to remote");
        mntmDownload.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                try {
                    LFileNode node = getSelectedNode();
//                    remoteService.getFile(node.getFile().getAbsolutePath(), "C:\\BEA92");
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
        actions.add(mntmDownload);
        return actions;
    }
}
