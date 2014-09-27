package du.tools.main.widgets;

import org.jdesktop.swingx.treetable.DefaultTreeTableModel;
import du.tools.main.ConfigAccessor;
import du.tools.main.git.status.Status;

import java.io.File;
import java.util.Map;

public class PViewModel extends DefaultTreeTableModel {
    protected String[] cNames = {"Name", "Modified", "UnStaged"};
    protected Class<?>[] cTypes = {String.class, String.class, String.class};
    protected Map<String, Status> status;

    public PViewModel(PViewNode root, Map<String, Status> status) {
        super(root);
        this.status = status;
    }

    public int getColumnCount() {
        return cNames.length;
    }

    public String getColumnName(int column) {
        return cNames[column];
    }

    public Class<?> getColumnClass(int column) {
        return cTypes[column];
    }

    public Object getValueAt(Object node, int column) {
        //File file = getFile(node);
        PViewNode pnode = ((PViewNode) node);
        switch (column) {
            case 0:
                return pnode.getName();
            case 1:
                return modified(pnode);
            case 2:
                return unStaged(pnode);
            default:
                return "Column " + column;
        }
    }

    private String modified(PViewNode node) {
        Status status = getStatus(node);
        if (status != null) {
            if (status.isModified()) {
                return "M";
            } else if (status.isAdded()) {
                return "A";
            } else if (status.isRenamed()) {
                return "R";
            } else if (status.isDeleted()) {
                return "D";
            }
        }
        return "";
    }

    private String unStaged(PViewNode node) {
        Status status = getStatus(node);
        if (status != null) {
            return status.isStaged() ? "" : "Y";
        }
        return "";
    }

    private Status getStatus(PViewNode node) {
        File file = new File(ConfigAccessor.getInstance().getLocalRepo(), node.getFilePath());
        if (file.isDirectory()) {
            return null;
        } else {
            String key;
            if (node.getFilePath().length() > root.toString().length()) {
                key = node.getFilePath().substring(root.toString().length() + 1);
            } else {
                key = node.getFilePath();
            }
            return status.get(key);
        }
    }
}
