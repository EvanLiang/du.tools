package du.tools.sftp.widgets.unix;

import du.swingx.JETreeTable;

import javax.swing.*;
import javax.swing.table.TableColumn;
import javax.swing.tree.TreePath;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public abstract class DExplorerPanel<T extends DFileNode> extends JPanel {

	protected JETreeTable fileView;
	protected JPopupMenu popupMenu;

	public DExplorerPanel(JETreeTable fileView) {
		this.fileView = fileView;

		fileView.setRowHeight(20);
		fileView.setRootVisible(true);
		fileView.setEditable(false);

		add(new JScrollPane(fileView), BorderLayout.CENTER);

		TableColumn column = fileView.getColumnModel().getColumn(2);
		column.setPreferredWidth(120);
		column.setMaxWidth(200);

		column = fileView.getColumnModel().getColumn(1);
		column.setPreferredWidth(120);
		column.setMaxWidth(200);

		setLayout(new BorderLayout());
		add(new JScrollPane(fileView), BorderLayout.CENTER);

		addPopup();
	}

	protected void refreshSelectedNode() {
		T node = getSelectedNode();
		refreshNode(node);
	}

	protected void refreshNode(T node) {
		if (node.getFile().isFile()) {
			((DFileNode) node.getParent()).refresh();
		} else {
			node.refresh();
		}
		fileView.updateUI();
	}

	private void addPopup() {
		popupMenu = new JPopupMenu();
		
		for (JMenuItem item : getActions()) {
			popupMenu.add(item);
		}
		
		fileView.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {

				}
			}

			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					int row = fileView.rowAtPoint(e.getPoint());
					fileView.getSelectionModel().setSelectionInterval(row, row);
					for (int i = 0; i < fileView.getColumnCount(); i++) {
						Rectangle rect = fileView.getCellRect(row, i, true);
						if (rect.contains(e.getX(), e.getY())) {
							showMenu(e);
						}
					}
				}
			}

			private void showMenu(MouseEvent e) {
				popupMenu.show(fileView, e.getX(), e.getY());
			}
		});
	}

	protected java.util.List<JMenuItem> getActions() {
		java.util.List<JMenuItem> actions = new ArrayList<>();
		JMenuItem mntmRefresh = new JMenuItem("Refresh");
		ActionListener refresh = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshSelectedNode();
			}
		};
		mntmRefresh.addActionListener(refresh);
		actions.add(mntmRefresh);
		
		// TODO use getActionMap() to register Keyboard action
		fileView.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_F5) {
					refreshSelectedNode();
				}
			}
		});
		return actions;
	}

	public T getSelectedNode() {
		TreePath treePath = fileView.getPathForRow(fileView.getSelectedRow());
		return (T) treePath.getLastPathComponent();
	}
}
