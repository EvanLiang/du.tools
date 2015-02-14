package du.tools.explorer.windows;

import com.jgoodies.looks.plastic.PlasticLookAndFeel;

import du.swingx.JETabbedPane;
import du.tools.explorer.unix.RemoteService;
import du.tools.explorer.widgets.unix.LExplorerPanel;
import du.tools.explorer.widgets.unix.RExplorerPanel;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WinExplorer extends JFrame {

	private static final long serialVersionUID = 1L;
	private static Logger log = LoggerFactory.getLogger(WinExplorer.class);
	private static final String PATTERN = "(.*):(.*)@([\\.0-9]*):?(.*)?";
	
	private Properties prop;
	
	private JETabbedPane unixExplorers;
	private JTabbedPane windowsExplorer;
	private LExplorerPanel lExplorerPanel;

	public static void main(String[] ars) {
		URL url = ClassLoader.getSystemClassLoader().getResource("log4j.xml");
		DOMConfigurator.configure(url);
		try {
			UIManager.setLookAndFeel(new PlasticLookAndFeel());
			new WinExplorer()
					.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("App Error", e);
		}
	}

	public WinExplorer() throws IOException {
		loadConfig();
		initialise();
	}
	
	private void loadConfig() throws IOException{
		prop = new Properties();
		File pFile = new File("explorer.properties");
		if(!pFile.exists()){
			prop.setProperty("connection.xx", "user:pwd@host:/home");
			prop.store(new FileOutputStream(pFile), "Configuration of your remote host");
			return;
		}
		prop.load(new InputStreamReader(new FileInputStream(pFile)));
	}

	private void initialise() {
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Unix Explorer Tool");
		setSize(1200, 800);
		setLocationRelativeTo(null);

		windowsExplorer = new JTabbedPane();
		lExplorerPanel = new LExplorerPanel("D:/");
		windowsExplorer.addTab("Local Windows", lExplorerPanel);

		unixExplorers = new JETabbedPane();
		unixExplorers.setTabCloseListener(new JETabbedPane.TabCloseListener() {
			public boolean actionPerformed(int index) {
				return closeSession(index);
			}
		});

		addMenu();
		setVisible(true);

		final JSplitPane splitPane = new JSplitPane(
				JSplitPane.HORIZONTAL_SPLIT, windowsExplorer, unixExplorers);
		getContentPane().add(splitPane);

		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				super.componentResized(e);
				splitPane.setDividerLocation(getWidth() / 3);
			}
		});
	}

	private void addMenu() {
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		menuBar.setPreferredSize(new Dimension(0, 25));

		JMenu menu = new JMenu("Host name");
		menuBar.add(menu);
		
		for(Object key : prop.keySet()){
			final String connection = prop.getProperty(key.toString());
			JMenuItem item = new JMenuItem(connection);
			menu.add(item);
			item.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					openSession(connection);
				}
			});
		}
		
		JMenuItem item = new JMenuItem("Other host");
		menu.add(item);
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String connection = JOptionPane.showInputDialog(WinExplorer.this, "Give me your host name");
				openSession(connection);
			}
		});

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				for (int i = 0; i < unixExplorers.getTabCount(); i++) {
					unixExplorers.closeTabAt(i);
				}
				log.info("Window CLOSED");
			}
		});
	}

	private boolean closeSession(int index) {
		try {
			RExplorerPanel panel = (RExplorerPanel) unixExplorers
					.getComponentAt(index);
			panel.close();
			log.info("Closed Session {} - {}", new Object[] { unixExplorers.getTitleAt(index)});
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private boolean openSession(String connection) {
		Matcher m = Pattern.compile(PATTERN).matcher(connection);
		if(m.find()){
			return openSession(m.group(3), m.group(1), m.group(2), m.group(4));
		}else{
			JOptionPane.showMessageDialog(WinExplorer.this, PATTERN);
		}
		return false;
	}
	
	private boolean openSession(String host, String user, String pwd, String dir) {
		if (StringUtils.isNotBlank(host)) {
			try {
				RemoteService rs = new RemoteService();
				rs.connect(host, user, pwd);
				unixExplorers.addTab(user + "@" + host, new RExplorerPanel(rs, dir, lExplorerPanel));
				log.info("Opened Session {}:{}@{}", new Object[] { user, pwd, host});
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public JTabbedPane getWindowsExplorer() {
		return windowsExplorer;
	}
	
	
}
