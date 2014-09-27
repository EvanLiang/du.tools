package du.tools.main.widgets.console.pty;

public interface Terminal {
    public void append(String text);

    void deleteLine();

    void carriageReturn();

}
