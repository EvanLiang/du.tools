package du.tools.main.widgets;

import du.swingx.JETabbedPane;

public class PViews extends JETabbedPane {

    private PMainPanel parent;

    public PViews(PMainPanel parent) {
        this.parent = parent;
    }

    @Override
    public void removeTabAt(int index) {
        PView pView = (PView) this.getComponentAt(index);
        pView.destroy();
        super.removeTabAt(index);
    }
}
