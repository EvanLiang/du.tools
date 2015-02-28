package du.tools.mq;

public interface MHelper {
    QHelper getQHelper(String queueName) throws Exception;

    void close() throws Exception;
}
