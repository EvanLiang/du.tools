package du.tools.mq.windows;

import com.ibm.mq.MQException;
import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;
import du.tools.main.mq.MHelper;
import du.tools.main.mq.QHelper;
import du.tools.main.mq.TMessage;
import du.tools.main.mq.jms.MHelperImpl;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import javax.jms.JMSException;
import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class WinMQueue extends JFrame {

    private Map<String, MHelper> mHelpers = new HashMap<String, MHelper>();
    private Properties pro;
    private JComboBox<String> envList;
    private JComboBox<String> domainList;
    private JComboBox<QueueName> queueList;

    File sendFile = null;
    JButton btnSend;
    JButton btnReceive;
    JButton btnBrowse;
    JButton btnClean;
    JTextField tfBrowse;
    JTextArea proArea;
    JTextArea textArea;

    public WinMQueue() throws IOException {
        loadConfig();
        init();
    }

    private void loadConfig() throws IOException {
        String fileName = "cfg/config.properties";
        InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream(fileName);
        if (is == null) {
            //if moved the resources to parent dir
            URL url = ClassLoader.getSystemClassLoader().getResource(".");
            if (url != null) {
                File file = new File(url.getFile());
                is = new FileInputStream(new File(file.getParent(), fileName));
            }
        }
        System.out.println();
        if (is == null) {
            is = getClass().getClassLoader().getResourceAsStream(fileName);
        }
        if (is == null) {
            is = new FileInputStream(fileName);
        }
        pro = new Properties();
        pro.load(is);
    }

    private void init() {
        setTitle("MQueue Explorer");
        setSize(1200, 800);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JPanel jp = new JPanel();
        jp.setLayout(null);
        getContentPane().add(jp);

        JLabel l1 = new JLabel("ENV:");
        envList = new JComboBox<>();
        JLabel l2 = new JLabel("DOMAIN:");
        domainList = new JComboBox<>();
        JLabel l3 = new JLabel("QUEUE:");
        queueList = new JComboBox<>();
        JLabel l4 = new JLabel("FILE:");
        final JTextField fileField = new JTextField();
        JButton btnSelect = new JButton("...");
        proArea = new JTextArea();
        final JScrollPane ta1 = new JScrollPane(proArea);
        textArea = new JTextArea();
        textArea.setLineWrap(true);
        final JScrollPane ta = new JScrollPane(textArea);
        btnSend = new JButton("Send");
        btnReceive = new JButton("Receive");
        btnBrowse = new JButton("Browse");
        tfBrowse = new JTextField("1");
        btnClean = new JButton("Clear");

        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                ta.setSize(WinMQueue.this.getWidth() - 35, WinMQueue.this.getHeight() - 175);
                textArea.updateUI();
            }
        });

        jp.add(l1);
        jp.add(envList);
        jp.add(l2);
        jp.add(domainList);
        jp.add(l3);
        jp.add(queueList);
        jp.add(l4);
        jp.add(fileField);
        jp.add(btnSelect);
        jp.add(ta1);
        jp.add(ta);
        jp.add(btnSend);
        jp.add(btnReceive);
        jp.add(btnBrowse);
        jp.add(tfBrowse);
        jp.add(btnClean);

        l1.setBounds(10, 10, 30, 20);
        envList.setBounds(40, 10, 100, 20);
        for (String env : pro.getProperty("env").split(",")) {
            envList.addItem(env.trim());
        }
        envList.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                getQueueList();
            }
        });

        l2.setBounds(150, 10, 50, 20);
        domainList.setBounds(210, 10, 100, 20);
        for (String domain : pro.getProperty("domain").split(",")) {
            domainList.addItem(domain.trim());
        }
        domainList.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                getQueueList();
            }
        });

        l3.setBounds(10, 40, 50, 20);
        queueList.setBounds(60, 40, 350, 20);
        getQueueList();

        l4.setBounds(10, 70, 50, 20);
        fileField.setBounds(40, 70, 350, 22);
        fileField.setEditable(false);
        btnSelect.setBounds(390, 70, 20, 22);
        final JFileChooser jfc = new JFileChooser();

        btnSelect.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jfc.showOpenDialog(WinMQueue.this);
                sendFile = jfc.getSelectedFile();
                fileField.setText(sendFile.getAbsolutePath());

                try {
                    String s = FileUtils.readFileToString(sendFile);
                    textArea.setText(s);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

        ta1.setBounds(420, 10, 305, 85);
        proArea.setFont(FontUIResource.getFont("Calibri"));

        ta.setLocation(10, 130);
        textArea.setFont(FontUIResource.getFont("Calibri"));

        sendBtn();
        receiveBtn();
        browseBtn();
        cleanBtn();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void sendBtn() {
        btnSend.setBounds(10, 100, 80, 25);
        btnSend.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    String text = textArea.getText();
                    if (StringUtils.isBlank(text)) {
                        JOptionPane.showMessageDialog(WinMQueue.this, "Empty File");
                        return;
                    }

                    Properties p = new Properties();
                    p.load(new StringReader(proArea.getText()));

                    MHelper mh = getMHelper();

                    QueueName qn = (QueueName) queueList.getSelectedItem();
                    QHelper qh = mh.getQHelper(qn.queue);

                    TMessage t = new TMessage();
                    t.setHeader(p);
                    t.setText(text);
                    qh.send(t);

                    JOptionPane.showMessageDialog(WinMQueue.this, "Sent successfully");
                } catch (Exception e1) {
                    e1.printStackTrace();
                    JOptionPane.showMessageDialog(WinMQueue.this, e1.getMessage());
                }
            }
        });
    }

    private void receiveBtn() {
        btnReceive.setBounds(100, 100, 80, 25);
        btnReceive.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    MHelper mh = getMHelper();

                    QueueName qn = (QueueName) queueList.getSelectedItem();
                    QHelper qh = mh.getQHelper(qn.queue);
                    TMessage t = qh.receive();
                    showPro(t);
                    textArea.setText(t.getText());

                } catch (Exception e1) {
                    e1.printStackTrace();
                    JOptionPane.showMessageDialog(WinMQueue.this, e1.getMessage());
                }
            }
        });
    }

    private void showPro(TMessage t) {
        proArea.setText("");
        String s = t.getHeader().toString();
        s = s.substring(1).substring(0, s.length() - 2);
        for (String p : s.split(",")) {
            proArea.append(p.trim() + "\r\n");
        }
    }

    private void browseBtn() {
        btnBrowse.setBounds(190, 100, 80, 25);
        tfBrowse.setBounds(280, 100, 80, 25);
        btnBrowse.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    MHelper mh = getMHelper();

                    QueueName qn = (QueueName) queueList.getSelectedItem();
                    QHelper qh = mh.getQHelper(qn.queue);

                    TMessage t = qh.browse(Integer.parseInt(tfBrowse.getText()));
                    showPro(t);
                    textArea.setText(t.getText());

                } catch (Exception e1) {
                    e1.printStackTrace();
                    JOptionPane.showMessageDialog(WinMQueue.this, e1.getMessage());
                }
            }
        });
    }

    private void cleanBtn() {
        btnClean.setBounds(380, 100, 80, 25);
        btnClean.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                proArea.setText("");
                textArea.setText("");
            }
        });
    }

    private MHelper getMHelper() throws MQException, JMSException {
        String env = envList.getSelectedItem().toString();
        if (!mHelpers.containsKey(env)) {
            String manager = pro.getProperty(env + ".manager");
            String host = pro.getProperty(env + ".host");
            String port = pro.getProperty(env + ".port");
            String channel = pro.getProperty(env + ".channel");
            MHelper mh = new MHelperImpl(manager, host, Integer.parseInt(port), channel);
//            MHelper mh = new mq.ibm.MHelperImpl(manager, host, Integer.parseInt(port), channel);
            mHelpers.put(env, mh);
            return mh;
        } else {
            return mHelpers.get(env);
        }
    }

    private void getQueueList() {
        queueList.removeAllItems();
        String prefix = envList.getSelectedItem().toString() + "." + domainList.getSelectedItem().toString() + ".";
        for (Object key : pro.keySet()) {
            if (key.toString().startsWith(prefix)) {
                queueList.addItem(new QueueName(key.toString().replace(prefix, ""), pro.getProperty(key.toString())));
            }
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new WindowsLookAndFeel());
            new WinMQueue();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    class QueueName {
        private String name;
        private String queue;

        public QueueName(String name, String queue) {
            this.name = name;
            this.queue = queue;
        }

        public String toString() {
            return name + " | " + queue;
        }
    }
}
