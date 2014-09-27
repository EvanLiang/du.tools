package du.tools.main.widgets.console.jediterm;

import du.tools.main.windows.WinMain;

import javax.swing.*;
import java.io.File;

public class ConsoleWindow extends JFrame {

    public ConsoleWindow(File file) {
        initialize();
    }

    private void initialize() {
        setTitle("Windows command line");
        setSize(1000, 800);
        setLocationRelativeTo(WinMain.frame);
    }
}
