package du.tools.main.widgets.unix;

import du.swingx.JETreeTable;
import du.tools.main.unix.RemoteService;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class RExplorerPanel extends DExplorerPanel<RFileNode> {

    private RemoteService remoteService;

    public RExplorerPanel(RemoteService remoteService, String rootPath) {
        super(new JETreeTable(new RFileModel(new RFileNode(remoteService.new RFile(rootPath)))));
        this.remoteService = remoteService;
    }

    protected java.util.List<JMenuItem> getActions() {
        java.util.List<JMenuItem> actions = new ArrayList<>();
        JMenuItem mntmDownload = new JMenuItem("Download to local");
        mntmDownload.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                try {
                    RFileNode node = getSelectedNode();
                    remoteService.getFile(node.getFile().getAbsolutePath(), "C:\\BEA92");
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
        actions.add(mntmDownload);
        return actions;
    }

    public void close() {
        remoteService.disconnect();
    }
}
