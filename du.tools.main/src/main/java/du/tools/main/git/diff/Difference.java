package du.tools.main.git.diff;


import du.tools.main.git.status.Status;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class Difference {

    public static final Color COLOR_ADD = new Color(8, 131, 19);
    public static final Color COLOR_CHANGE = new Color(57, 39, 164);
    public static final Color COLOR_DELETE = new Color(194, 25, 28);

    private Status status;
    private String aName;
    private String bName;
    private Map<Integer, String> aLine = new LinkedHashMap<Integer, String>();
    private Map<Integer, String> bLine = new LinkedHashMap<Integer, String>();
    private boolean textFile = true;

    public void addaLine(int line, String text) {
        aLine.put(line, text);
    }

    public void addbLine(int line, String text) {
        bLine.put(line, text);
    }

    public String getaName() {
        return aName;
    }

    public void setaName(String fileName) {
        this.aName = fileName;
    }

    public String getbName() {
        return bName;
    }

    public void setbName(String fileName) {
        this.bName = fileName;
    }

    public Map<Integer, String> getaLine() {
        return aLine;
    }

    public Map<Integer, String> getbLine() {
        return bLine;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        System.out.println(aLine);
        System.out.println(bLine);
        System.out.println("----");
        return super.toString();
    }

    public boolean isTextFile() {
        return textFile;
    }

    public void setTextFile(boolean textFile) {
        this.textFile = textFile;
    }
}
