package du.tools.main.widgets.main;


import du.swingx.plaf.basic.DefaultTreeCellRenderer;
import du.tools.main.commons.utils.CommonUtil;
import du.tools.main.commons.utils.IconUtil;

import javax.swing.*;
import java.awt.*;

public class PTreeRenderer extends DefaultTreeCellRenderer {
    Icon openIcon;
    Icon closedIcon;

    public PTreeRenderer() {
        openIcon = new ImageIcon(CommonUtil.getURLInClasspath("/icon/OpenIcon.png"));
        closedIcon = new ImageIcon(CommonUtil.getURLInClasspath("/icon/ClosedIcon.png"));
    }

    public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                  boolean isSelected, boolean expanded, boolean leaf, int row,
                                                  boolean hasFocus) {
        setLeafIcon(closedIcon);
        setOpenIcon(openIcon);
        setClosedIcon(closedIcon);

        return super.getTreeCellRendererComponent(tree, value, isSelected,
                expanded, leaf, row, hasFocus);
    }
}
