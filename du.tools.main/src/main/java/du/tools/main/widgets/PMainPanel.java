package du.tools.main.widgets;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;

public class PMainPanel extends JPanel {

    private PTree pTrees;
    private PViews pViews;

    public PMainPanel() {
        initialize();
    }

    private void initialize() {
        setPreferredSize(new Dimension(1200, 600));
        setBorder(new BevelBorder(BevelBorder.LOWERED));
        setLayout(new BorderLayout());

        pTrees = new PTree(this);
        pViews = new PViews(this);
        JSplitPane pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pTrees, pViews);
        pane.setDividerSize(2);
        add(pane);
    }

    public PTree getpTrees() {
        return pTrees;
    }

    public PViews getpViews() {
        return pViews;
    }
}
