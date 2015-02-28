package du.tools.mq.ibm;

import com.ibm.mq.MQEnvironment;
import com.ibm.mq.MQException;
import com.ibm.mq.MQQueueManager;
import du.tools.main.mq.MHelper;

public class MHelperImpl implements MHelper {

    private MQQueueManager qManager = null;

    public MHelperImpl(String manager, String host, int port, String channel) throws MQException {
        MQEnvironment.hostname = host;
        MQEnvironment.port = port;
        MQEnvironment.channel = channel;
        qManager = new MQQueueManager(manager);
    }

    public QHelperImpl getQHelper(String queueName) throws MQException {
        return new QHelperImpl(qManager, queueName);
    }

    public void close() throws MQException {
        if (qManager != null && qManager.isOpen()) {
            qManager.disconnect();
            qManager.close();
        }
    }
}