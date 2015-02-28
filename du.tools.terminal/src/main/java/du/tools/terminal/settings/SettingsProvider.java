package du.tools.terminal.settings;

import com.jediterm.terminal.ui.settings.DefaultTabbedSettingsProvider;

import java.awt.*;

public class SettingsProvider extends DefaultTabbedSettingsProvider {

    @Override
    public Font getTerminalFont() {
        return Font.decode("monospaced").deriveFont(getTerminalFontSize());
    }

    @Override
    public float getTerminalFontSize() {
        return 16;
    }

    @Override
    public boolean forceActionOnMouseReporting() {
        return true;
    }
}
