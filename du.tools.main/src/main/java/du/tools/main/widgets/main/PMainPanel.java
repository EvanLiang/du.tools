package du.tools.main.widgets.main;

import javax.swing.*;
import java.awt.*;

public class PMainPanel extends JPanel {

    private PTree pTrees;
    private PViews pViews;

    public PMainPanel() {
        initialize();
    }

    private void initialize() {
        setPreferredSize(new Dimension(1300, 800));
        setLayout(new BorderLayout());

        pTrees = new PTree(this);
        pViews = new PViews(this);
        JSplitPane pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pTrees, pViews);
        pane.setDividerSize(2);
        pane.setDividerLocation(300);
        add(pane, BorderLayout.CENTER);
    }

    private void createToolBar(){
        JToolBar toolBar = new JToolBar();
        add(toolBar, BorderLayout.NORTH);
        toolBar.setPreferredSize(new Dimension(0, 25));
        toolBar.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 2));
        JButton test = new JButton("Test");
        toolBar.add(test);
    }

    public PTree getpTrees() {
        return pTrees;
    }

    public PViews getpViews() {
        return pViews;
    }
}
