package du.tools.mq;

import java.util.Hashtable;

public class TMessage {
    Hashtable<Object, Object> header = new Hashtable<Object, Object>();
    String text;


    public Hashtable<Object, Object> getHeader() {
        return header;
    }

    public void setHeader(Hashtable<Object, Object> header) {
        this.header = header;
    }

    public void addHeader(String name, String value){
        header.put(name, value);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
