package du.tools.mq.ibm;

import com.ibm.mq.*;
import com.ibm.mq.constants.MQConstants;
import du.tools.main.mq.QHelper;
import du.tools.main.mq.TMessage;

import java.io.IOException;
import java.util.Enumeration;

public class QHelperImpl implements QHelper {
    private MQQueueManager qManager;
    private String queueName;
    private MQQueue queue = null;

    public QHelperImpl(MQQueueManager qManager, String queueName) throws MQException {
        this.qManager = qManager;
        this.queueName = queueName;
        this.queue = accessQueue(queueName, false);
    }

    private MQQueue accessQueue(String queueName, boolean readOnly) throws MQException {
        int openOptions;
        if (readOnly) {
            openOptions = MQConstants.MQOO_BROWSE;
        } else {
            openOptions = MQConstants.MQOO_INPUT_AS_Q_DEF | MQConstants.MQOO_INQUIRE | MQConstants.MQOO_OUTPUT;
        }
        return qManager.accessQueue(queueName, openOptions);
    }

    public void send(TMessage msg) throws MQException, IOException {
        MQPutMessageOptions mqPutMessageOptions = new MQPutMessageOptions();
        MQMessage mqMessage = new MQMessage();
        mqMessage.encoding = 273;
        mqMessage.format = "MQSTR";
        mqMessage.writeString(msg.getText());
        for (Object key : msg.getHeader().keySet()) {
            mqMessage.setObjectProperty(key.toString(), msg.getHeader().get(key));
        }
        queue.put(mqMessage, mqPutMessageOptions);
    }

    @Override
    public int getQueueDepth() throws MQException {
        return queue.getCurrentDepth();
    }

    public TMessage receive() throws MQException, IOException {
        if (queue.getCurrentDepth() == 0) {
            return null;
        }
        MQMessage mqMsg = new MQMessage();
        MQGetMessageOptions mqGetMessageOptions = new MQGetMessageOptions();
        queue.get(mqMsg, mqGetMessageOptions);
        return transform(mqMsg);
    }

    public TMessage browse(int index) throws Exception {
        if (queue.getCurrentDepth() == 0) {
            return null;
        }
        MQQueue queue = accessQueue(queueName, true);
        MQMessage mqMsg = new MQMessage();
        MQGetMessageOptions mqGetMessageOptions = new MQGetMessageOptions();
        mqGetMessageOptions.options += MQConstants.MQGMO_BROWSE_NEXT;
        while (index-- > 0) {
            queue.get(mqMsg, mqGetMessageOptions);
        }
        queue.close();
        return transform(mqMsg);
    }

    public int drain() throws MQException, IOException {
        int depth = queue.getCurrentDepth();
        while (depth-- > 0) {
            MQMessage mqMsg = new MQMessage();
            MQGetMessageOptions mqGetMessageOptions = new MQGetMessageOptions();
            queue.get(mqMsg, mqGetMessageOptions);
            int len = mqMsg.getDataLength();
            byte[] message = new byte[len];
            mqMsg.readFully(message, 0, len);
            System.out.println(new String(message));
        }
        return depth;
    }

    private TMessage transform(MQMessage mqMsg) throws MQException, IOException {
        TMessage t = null;
        if (mqMsg != null) {
            t = new TMessage();
            Enumeration e = mqMsg.getPropertyNames("ALL");
            while (e.hasMoreElements()) {
                String name = e.nextElement().toString();
                if (!name.trim().startsWith("JMS")) {
                    t.addHeader(name, mqMsg.getStringProperty(name));
                }
            }
            int len = mqMsg.getDataLength();
            byte[] message = new byte[len];
            mqMsg.readFully(message, 0, len);
            t.setText(new String(message));
        }
        return t;
    }

    public MQQueueManager getqManager() {
        return qManager;
    }

    public String getQueueName() {
        return queueName;
    }
}
