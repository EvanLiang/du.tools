package du.tools.main.mq;

public interface QHelper {
    void send(TMessage t) throws Exception;

    TMessage receive() throws Exception;

    TMessage browse(int index) throws Exception;

    int drain() throws Exception;

    public int getQueueDepth() throws Exception;
}
