package du.tools.main.widgets;


import lib.common.utils.XPathUtil;
import org.w3c.dom.Document;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Vector;

public class PTreeNode extends DefaultMutableTreeNode {
    private Document document;
    private String xpath;
    private String repoPath;

    public PTreeNode(Document document, String xpath) {
        this.document = document;
        this.xpath = xpath;
        userObject = XPathUtil.getString(document, xpath + "/@name");
    }

    public PTreeNode(Document document, String xpath, String repoPath) {
        this.document = document;
        this.xpath = xpath;
        this.repoPath = repoPath;
        userObject = XPathUtil.getString(document, xpath + "/@name");
    }

    public int getChildCount() {
        return getChildren() == null ? 0 : children.size();
    }

    public Vector getChildren() {
        cachedChildren();
        return children;
    }

    private void cachedChildren() {
        if (children == null) {
            String cxpath = xpath + "/Project";
            int count = XPathUtil.countElement(document, cxpath);
            if (count > 0) {
                children = new Vector();
                for (int i = 0; i < count; i++) {
                    String p = cxpath + "[" + (i + 1) + "]";
                    PTreeNode node = new PTreeNode(document, p, "/" + XPathUtil.getString(document, p + "/@key"));
                    add(node);
                }
            }

            cxpath = xpath + "/Repository";
            count = XPathUtil.countElement(document, cxpath);
            if (count > 0) {
                children = new Vector();
                for (int i = 0; i < count; i++) {
                    String p = cxpath + "[" + (i + 1) + "]";
                    PTreeNode node = new PTreeNode(document, p, repoPath + "/" + XPathUtil.getString(document, p + "/@name"));
                    add(node);
                }
            }
        }
    }

    public String getRepoPath() {
        return repoPath;
    }
}