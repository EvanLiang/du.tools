package du.tools.main.widgets.console.jediterm;

import com.jediterm.terminal.TerminalColor;
import com.jediterm.terminal.TextStyle;
import com.jediterm.terminal.ui.settings.DefaultSettingsProvider;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class SettingsProvider extends DefaultSettingsProvider {
    @Override
    public boolean allowSelectionOnMouseReporting() {
        return true;
    }

    @Override
    public Font getTerminalFont() {
        return Font.decode("Monospaced").deriveFont(getTerminalFontSize());
    }

    @Override
    public float getTerminalFontSize() {
        return 16;
    }

    @Override
    public TextStyle getDefaultStyle() {
        return new TextStyle(TerminalColor.rgb(169, 183, 198), TerminalColor.rgb(43, 43, 43));
    }

    @Override
    public TextStyle getSelectionColor() {
        return new TextStyle(TerminalColor.rgb(169, 183, 198), TerminalColor.rgb(33, 66, 131));
    }

    @Override
    public KeyStroke[] getCopyKeyStrokes() {
        return new KeyStroke[]{KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK)};
    }

    @Override
    public KeyStroke[] getPasteKeyStrokes() {
        return new KeyStroke[]{KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK)};
    }

}
