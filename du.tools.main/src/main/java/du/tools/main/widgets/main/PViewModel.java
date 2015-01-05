package du.tools.main.widgets.main;

import org.jdesktop.swingx.treetable.DefaultTreeTableModel;

public class PViewModel extends DefaultTreeTableModel {
    protected static final String[] COLUMN_NAMES = {"Name", "Status"};
    protected static final Class<?>[] COLUMN_TYPES = {String.class, String.class};

    public PViewModel(PViewNode root) {
        super(root);
    }

    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    public String getColumnName(int column) {
        return COLUMN_NAMES[column];
    }

    public Class<?> getColumnClass(int column) {
        return COLUMN_TYPES[column];
    }

    public Object getValueAt(Object node, int column) {
        //File file = getFile(node);
        PViewNode pnode = ((PViewNode) node);
        switch (column) {
            case 0:
                return pnode.getName();
            case 1:
                return pnode.getStatusCode();
            default:
                return "Column " + column;
        }
    }
}
