package du.tools.explorer.widgets.unix;

import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jdesktop.swingx.treetable.MutableTreeTableNode;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

public abstract class DFileNode<T extends File> extends DefaultMutableTreeTableNode {

    protected static final String[] COLUMN_NAMES = {"Name", "Size", "Last modify"};
    protected static final Class<?>[] COLUMN_TYPES = {String.class, String.class, String.class};

    private boolean hasChild = true;
    private T file;

    public DFileNode(T file) {
        super(file.getName());
        this.file = file;
    }

    public boolean isLeaf() {
        return getFile() != null && getFile().isFile();
    }

    public int getChildCount() {
        return getChildren().size();
    }

    public Enumeration<? extends MutableTreeTableNode> children() {
        return Collections.enumeration(getChildren());
    }

    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    public Object getValueAt(int column) {
        switch (column) {
            case 0:
                return getFile().getName();
            case 1:
                return getFile() != null && getFile().isFile() ? getFile().length() : "";
            case 2:
                SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-DD HH:mm:ss");
                return getFile() != null && getFile().isFile() ? new Date(getFile().lastModified()).toLocaleString() : "";
            default:
                return "No Column Value At " + column;
        }
    }

    public void refresh() {
        hasChild = true;
        children.clear();
        getChildren();
    }

    private List<MutableTreeTableNode> getChildren() {
        if (hasChild) {
            cacheChildren();
            sort(children, true);
            hasChild = false;
        }
        return children;
    }

    public T getFile() {
        return file;
    }

    protected abstract void cacheChildren();

    private void sort(List list, final boolean dirOnTop) {
        //Sort by type,name
        if (children != null && children.size() > 0) {
            Collections.sort(list, new Comparator<DFileNode>() {
                public int compare(DFileNode f1, DFileNode f2) {
                    String name1 = f1.getFile().getName();
                    String name2 = f2.getFile().getName();
                    name1 = dirOnTop && f1.getFile().isDirectory() ? "A" + name1 : "B" + name1;
                    name2 = dirOnTop && f2.getFile().isDirectory() ? "A" + name2 : "B" + name2;
                    return name1.compareTo(name2);
                }
            });
        }
    }
}
