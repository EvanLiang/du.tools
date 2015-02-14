package du.tools.explorer.widgets.unix;

import du.swingx.JETreeTable;
import du.tools.explorer.unix.RemoteService;

import javax.swing.*;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class RExplorerPanel extends DExplorerPanel<RFileNode> {

	private LExplorerPanel lExplorerPanel;
    private RemoteService remoteService;

    public RExplorerPanel(RemoteService remoteService, String rootPath, LExplorerPanel lExplorerPanel) {
        super(new JETreeTable(new RFileModel(new RFileNode(remoteService.new RFile(rootPath)))));
        this.remoteService = remoteService;
        this.lExplorerPanel = lExplorerPanel; 
    }

    protected java.util.List<JMenuItem> getActions() {
        java.util.List<JMenuItem> actions = new ArrayList<>();
        JMenuItem mntmDownload = new JMenuItem("Download to local");
        mntmDownload.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                try {
                    RFileNode rnode = getSelectedNode();
                    LFileNode lnode = lExplorerPanel.getSelectedNode();
                    remoteService.getFile(rnode.getFile().getAbsolutePath(), lnode.getFile().getAbsolutePath());
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
