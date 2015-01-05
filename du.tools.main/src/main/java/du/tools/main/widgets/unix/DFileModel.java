package du.tools.main.widgets.unix;

import org.jdesktop.swingx.treetable.DefaultTreeTableModel;

public class DFileModel<T extends DFileNode> extends DefaultTreeTableModel {

    public DFileModel(T root) {
        super(root);
    }

    public int getColumnCount() {
        return T.COLUMN_NAMES.length;
    }

    public String getColumnName(int column) {
        return T.COLUMN_NAMES[column];
    }

    public Class<?> getColumnClass(int column) {
        return T.COLUMN_TYPES[column];
    }
}
