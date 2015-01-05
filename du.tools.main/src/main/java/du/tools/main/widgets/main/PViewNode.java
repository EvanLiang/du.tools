package du.tools.main.widgets.main;

import du.tools.main.commons.utils.XPathUtil;
import org.eclipse.jgit.api.Status;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jdesktop.swingx.treetable.MutableTreeTableNode;
import org.w3c.dom.Document;

import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;

public class PViewNode extends DefaultMutableTreeTableNode {

    private Document document;
    private String xpath;
    private String filePath;
    private String type;
    private Status status;
    private PViewProvider.StatusCode statusCode;
    private List<String> skips;

    public PViewNode(Document document, String xpath, List<String> skips, Status status) {
        this(document, xpath, skips, "", status);
    }

    private PViewNode(Document document, String xpath, List<String> skips, String filePath, Status status) {
        this.document = document;
        this.xpath = xpath;
        this.skips = skips;
        this.filePath = filePath;
        this.status = status;
        this.type = XPathUtil.getString(document, xpath + "/@type");
        this.userObject = XPathUtil.getString(document, xpath + "/@name");
        this.statusCode = PViewProvider.statusCode(status, this);
    }

    public int getColumnCount() {
        return PViewModel.COLUMN_NAMES.length;
    }

    public int getChildCount() {
        return getChildren() == null ? 0 : getChildren().size();
    }

    public Enumeration<? extends MutableTreeTableNode> children() {
        return Collections.enumeration(getChildren());
    }

    private List<MutableTreeTableNode> getChildren() {
        cachedChildren();
        return children;
    }

    private void cachedChildren() {
        if (children.size() == 0) {
            String cxpath = xpath + "/File";
            int count = XPathUtil.countElement(document, cxpath);
            if (count > 0) {
                for (int i = 0; i < count; i++) {
                    String name = XPathUtil.getString(document, cxpath + "[" + (i + 1) + "]/@name");
                    String p = filePath + "/" + name;
                    if (accept(p)) {
                        PViewNode node = new PViewNode(document, cxpath + "[" + (i + 1) + "]", skips, p, status);
                        insert(node, children.size());
                    }
                }
                if (children.size() > 0) {
                    sort(children, true);
                }
            }
        }
    }

    private void sort(List list, final boolean dirOnTop) {
        Collections.sort(list, new Comparator<PViewNode>() {
            public int compare(PViewNode f1, PViewNode f2) {
                String name1 = f1.getName();
                String name2 = f2.getName();
                name1 = dirOnTop && f1.isDirectory() ? "A" + name1 : "B" + name1;
                name2 = dirOnTop && f2.isDirectory() ? "A" + name2 : "B" + name2;
                return name1.compareTo(name2);
            }
        });
    }

    private boolean accept(String filePath) {
        filePath = filePath.replace("\\", "/");
        for (String regex : skips) {
            if (filePath.matches(regex)) {
                return false;
            }
        }
        return true;
    }

    public String getFilePath() {
        return filePath;
    }

    public boolean isFile() {
        return "F".equals(type);
    }

    public boolean isDirectory() {
        return "D".equals(type);
    }

    public String getName() {
        return userObject.toString();
    }

    public PViewProvider.StatusCode getStatusCode() {
        return statusCode;
    }
}
