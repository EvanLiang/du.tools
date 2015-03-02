package du.tools.sftp.widgets.unix;

import du.swingx.JETreeTable;

import javax.swing.*;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;

public class LExplorerPanel extends DExplorerPanel<LFileNode> {

	private RExplorerPanel rExplorerPanel;

	public LExplorerPanel(String rootPath) {
		this(rootPath, null);
	}

	public LExplorerPanel(String rootPath, RExplorerPanel rExplorerPanel) {
		super(new JETreeTable(new LFileModel(new LFileNode(new File(rootPath)))));
		this.rExplorerPanel = rExplorerPanel;
	}

	protected java.util.List<JMenuItem> getActions() {
		java.util.List<JMenuItem> actions = super.getActions();
		JMenuItem mntmDownload = new JMenuItem("Upload to remote");
		mntmDownload.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				try {
					LFileNode lnode = getSelectedNode();
					RFileNode rnode = rExplorerPanel.getSelectedNode();
					rExplorerPanel.getRemoteService().putFile(lnode.getFile().getAbsolutePath(), rnode.getFile().getAbsolutePath());
					rExplorerPanel.refreshNode(rnode);
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(LExplorerPanel.this, "Upload failed: " + ex.getMessage());
				}
			}
		});
		actions.add(mntmDownload);
		return actions;
	}

	public RExplorerPanel getrExplorerPanel() {
		return rExplorerPanel;
	}

	public void setrExplorerPanel(RExplorerPanel rExplorerPanel) {
		this.rExplorerPanel = rExplorerPanel;
	}
}
