package du.tools.main.widgets;


import du.swingx.plaf.basic.DefaultTreeCellRenderer;
import lib.common.utils.CommonUtil;
import du.tools.main.git.diff.Difference;

import javax.swing.*;
import java.awt.*;

public class PViewRenderer extends DefaultTreeCellRenderer {
    private Icon folder;
    private Icon file;

    public PViewRenderer() {
        folder = new ImageIcon(CommonUtil.getURLInClasspath("/icon/folder.png"));
        file = new ImageIcon(CommonUtil.getURLInClasspath("/icon/file.png"));
    }

    public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                  boolean isSelected, boolean expanded, boolean leaf, int row,
                                                  boolean hasFocus) {


        PViewNode node = (PViewNode) value;
        if (node.isDirectory()) {
            setLeafIcon(folder);
            setOpenIcon(folder);
            setClosedIcon(folder);
        } else {
            setLeafIcon(file);
            setOpenIcon(file);
            setClosedIcon(file);
        }

        super.getTreeCellRendererComponent(tree, value, isSelected, expanded, leaf, row, hasFocus);

        if (node.getStatus() != null) {
            if (node.getStatus().isAdded()) {
                this.setForeground(Difference.COLOR_ADD);
            }
            if (node.getStatus().isDeleted()) {
                this.setForeground(Difference.COLOR_DELETE);
            }
            if (node.getStatus().isRenamed()) {
                this.setForeground(Difference.COLOR_CHANGE);
            }
            if (node.getStatus().isModified()) {
                this.setForeground(Difference.COLOR_CHANGE);
            }
        }
        return this;
    }


}