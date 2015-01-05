package du.tools.main.mq.jms;

import com.ibm.mq.MQException;
import com.ibm.mq.jms.JMSC;
import com.ibm.mq.jms.MQConnectionFactory;
import com.ibm.mq.jms.MQQueue;
import com.ibm.mq.jms.MQQueueConnectionFactory;
import du.tools.main.mq.MHelper;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;

public class MHelperImpl implements MHelper {

    private Session session;
    private Connection conn;

    public MHelperImpl(String manager, String host, int port, String channel) throws MQException, JMSException {
        MQConnectionFactory qFactory = new MQConnectionFactory();
        qFactory.setTransportType(JMSC.MQJMS_TP_CLIENT_MQ_TCPIP);
        qFactory.setQueueManager(manager);
        qFactory.setHostName(host);
        qFactory.setPort(port);
        qFactory.setChannel(channel);

        conn = qFactory.createConnection();
        conn.start();
        session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
    }

    @Override
    public QHelperImpl getQHelper(String queueName) throws MQException, JMSException {
        Destination destination = new MQQueue(queueName);
        return new QHelperImpl(session, destination);
    }

    @Override
    public void close() throws JMSException {
        if (session != null) {
            session.close();
        }
        if (conn != null) {
            conn.close();
        }
    }
}