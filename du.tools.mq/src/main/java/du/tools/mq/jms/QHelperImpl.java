package du.tools.mq.jms;


import du.tools.main.mq.QHelper;
import du.tools.main.mq.TMessage;

import javax.jms.*;
import java.util.Enumeration;

public class QHelperImpl implements QHelper {
    Session session;
    Destination destination;

    public QHelperImpl(Session session, Destination destination) throws JMSException {
        this.session = session;
        this.destination = destination;
    }

    @Override
    public void send(TMessage t) throws JMSException {
        MessageProducer producer = session.createProducer(destination);
        TextMessage msg = session.createTextMessage();
        msg.setText(t.getText());
        for (Object key : t.getHeader().keySet()) {
            msg.setStringProperty(key.toString(), t.getHeader().get(key).toString());
        }
        producer.send(msg);
        producer.close();
    }

    @Override
    public TMessage receive() throws JMSException {
        MessageConsumer consumer = session.createConsumer(destination);
        Message msg = consumer.receive(2000);
        TMessage t = transform(msg);
        consumer.close();
        return t;
    }

    private TMessage transform(Message msg) throws JMSException {
        TMessage t = null;
        if (msg != null) {
            t = new TMessage();
            Enumeration e = msg.getPropertyNames();
            while (e.hasMoreElements()) {
                String name = e.nextElement().toString();
                if (!name.trim().startsWith("JMS")) {
                    t.addHeader(name, msg.getStringProperty(name));
                } else {
                    System.out.println(name.trim() + "=" + msg.getObjectProperty((name)));
                }
            }
            if (msg instanceof TextMessage) {
                TextMessage tm = (TextMessage) msg;
                t.setText(tm.getText());
            }
            System.out.println(msg.toString());
        }
        return t;
    }

    @Override
    public int getQueueDepth() throws Exception {
        QueueBrowser browser = session.createBrowser((Queue) destination);
        Enumeration enumeration = browser.getEnumeration();

        int count = 1;
        while (enumeration.hasMoreElements()) {
            count++;
        }
        return count;
    }

    @Override
    public TMessage browse(int index) throws JMSException {
        QueueBrowser browser = session.createBrowser((Queue) destination);
        Enumeration enumeration = browser.getEnumeration();

        int count = 1;
        while (enumeration.hasMoreElements()) {
            Message msg = (Message) enumeration.nextElement();
            if (count == index) {
                return transform(msg);
            }
            count++;
        }

        browser.close();
        return null;
    }

    @Override
    public int drain() throws JMSException {
        MessageConsumer consumer = session.createConsumer(destination);
        Message msg;
        int count = 0;
        while ((msg = consumer.receive(1000)) != null) {
            System.out.println("  removed message: " + msg.getJMSMessageID());
            count++;
        }
        consumer.close();
        return count;
    }

    public Session getSession() {
        return session;
    }

    public Destination getDestination() {
        return destination;
    }
}
