package du.tools.main.widgets.main;


import du.swingx.plaf.basic.DefaultTreeCellRenderer;

import javax.swing.*;
import java.awt.*;

public class PViewTreeRenderer extends DefaultTreeCellRenderer {

    public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                  boolean isSelected, boolean expanded, boolean leaf, int row,
                                                  boolean hasFocus) {

        super.getTreeCellRendererComponent(tree, value, isSelected, expanded, leaf, row, hasFocus);

        PViewNode node = (PViewNode) value;
        if (node.getStatusCode() != null) {
            setForeground(node.getStatusCode().color());
        }
        return this;
    }
}