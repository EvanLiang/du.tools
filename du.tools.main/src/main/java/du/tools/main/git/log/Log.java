package du.tools.main.git.log;

public class Log {
    private String commit;
    private String author;
    private String date;
    private String message;
    private String diffsText;

    public String getCommit() {
        return commit;
    }

    public void setCommit(String commit) {
        this.commit = commit;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDiffsText() {
        return diffsText;
    }

    public void setDiffsText(String diffsText) {
        this.diffsText = diffsText;
    }
}
