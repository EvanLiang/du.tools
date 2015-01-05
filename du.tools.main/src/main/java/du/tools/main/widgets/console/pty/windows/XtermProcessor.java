package du.tools.main.widgets.console.pty.windows;

import du.tools.main.widgets.console.pty.windows.PtyConnector;
import du.tools.main.widgets.console.pty.windows.PtyTextPane;

import java.io.IOException;

public class XtermProcessor {

    private PtyConnector pty;
    private PtyTextPane terminal;

    private char[] buffer = new char[1024];
    private int length;
    private int offset;

    public XtermProcessor(PtyConnector pty, PtyTextPane terminal) {
        this.pty = pty;
        this.terminal = terminal;
    }

    private char getChar() throws IOException {
        if (offset == length) {
            fillBuffer();
        }
        return buffer[offset++];
    }

    private void fillBuffer() throws IOException {
        length = pty.read(buffer);
        offset = 0;
    }

    public void process() throws IOException {
        char ch = getChar();
        switch (ch) {
            case '\000':
                break;
            case '\007':
//                System.out.println("terminal.beep();");
                break;
            case '\b':
//                System.out.println("terminal.backspace();");
                break;
            case '\r':
//                System.out.println("terminal.carriageReturn();");
                terminal.carriageReturn();
                break;
            case '\005':
//                System.out.println("Terminal status:" + ch);
                break;
            case '\n': //\n 10
            case '\013': //\n 10
            case '\f': //\n 10
//                System.out.println("terminal.newLine();");
                terminal.append("\r\n");
//                System.out.println();
                break;
            case '\017':
//                System.out.println("terminal.mapCharsetToGL(0);");
                break;
            case '\016':
//                System.out.println("terminal.mapCharsetToGL(1);");
                break;
            case '\t':
//                System.out.println("terminal.horizontalTab();");
                break;
            case '\033'://ESC 27
//                System.out.println("processEscapeSequence()" + ch);
                processEscapeSequence();
                break;
            case '\001':
            case '\002':
            case '\003':
            case '\004':
            case '\006':
            case '\020':
            case '\021':
            case '\022':
            case '\023':
            case '\024':
            case '\025':
            case '\026':
            case '\027':
            case '\030':
            case '\031':
            case '\032':
            default:
                if (ch <= '\037') {
                    System.out.println("Unhandled control character:");
                } else {
                    String nonControlCharacters = getNonControlCharacters();
                    terminal.append(nonControlCharacters);
                    //                    System.out.print(nonControlCharacters);
                }
                break;
        }
    }

    private void processEscapeSequence() throws IOException {
        offset--;
        StringBuilder escapeSequence = new StringBuilder();
        escapeSequence.append(getChar());
        char c = getChar();
        escapeSequence.append(c);
        switch (c) {
            case '[':
                while (offset < length) {
                    char b = getChar();
                    escapeSequence.append(b);
                    if (('@' <= b) && (b <= '~')) {
                        //Stop reading the control char
                        break;
                    }
                }
                break;
            default:
                System.out.println("");
        }
//        System.out.println(escapeSequence.toString().replace("\u001B", "\\u001B"));

        switch (escapeSequence.toString()) {
            case "\u001B[?25l":
                //System.out.println();
                break;
            case "\u001B[2K":
//                System.out.println("delete whole line");
                terminal.deleteLine();
                break;
        }
    }

    private String getNonControlCharacters() throws IOException {
        offset--;
        int index = offset;
        int len = 0;
        while (offset < length) {
            char tmp = getChar();
            if (tmp >= ' ') {
                len++;
            } else {
                if (tmp == '\033') {
                    offset--;
                }
                break;
            }
        }
        return new String(buffer, index, len);
    }
}
