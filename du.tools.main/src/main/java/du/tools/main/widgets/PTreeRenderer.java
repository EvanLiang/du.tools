package du.tools.main.widgets;


import du.swingx.plaf.basic.DefaultTreeCellRenderer;
import lib.common.utils.CommonUtil;

import javax.swing.*;
import java.awt.*;

public class PTreeRenderer extends DefaultTreeCellRenderer {
    Icon folder;

    public PTreeRenderer() {
        folder = new ImageIcon(CommonUtil.getURLInClasspath("/icon/folder.png"));
    }

    public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                  boolean isSelected, boolean expanded, boolean leaf, int row,
                                                  boolean hasFocus) {

        setLeafIcon(folder);
        setOpenIcon(folder);
        setClosedIcon(folder);

        return super.getTreeCellRendererComponent(tree, value, isSelected,
                expanded, leaf, row, hasFocus);
    }
}
