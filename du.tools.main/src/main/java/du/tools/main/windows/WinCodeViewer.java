package du.tools.main.windows;

import org.apache.commons.io.FileUtils;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

public class WinCodeViewer extends JFrame {

    private File file;

    public WinCodeViewer(File file) {
        this.file = file;
        initialize();
    }

    private void initialize() {
        setTitle(file.getAbsolutePath());
        setSize(1200, 800);
        setLocationRelativeTo(WinMain.frame);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        try {
            String text = FileUtils.readFileToString(file);

            RSyntaxTextArea textArea = new RSyntaxTextArea();
            textArea.setEditable(false);
            textArea.setText(text);
            textArea.setSyntaxEditingStyle(getStyle());
            textArea.setCodeFoldingEnabled(true);
            textArea.select(0 ,0);

            add(new RTextScrollPane(textArea));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getStyle() {
        String name = file.getName().toLowerCase();
        if (name.endsWith(".java")) {
            return SyntaxConstants.SYNTAX_STYLE_JAVA;
        }
        if (name.endsWith(".xml")) {
            return SyntaxConstants.SYNTAX_STYLE_XML;
        }
        if (name.endsWith(".sql")) {
            return SyntaxConstants.SYNTAX_STYLE_SQL;
        }
        if (name.endsWith(".jsp")) {
            return SyntaxConstants.SYNTAX_STYLE_JSP;
        }
        return null;
    }
}
