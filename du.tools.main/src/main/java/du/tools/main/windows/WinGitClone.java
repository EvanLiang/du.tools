package du.tools.main.windows;


import lib.common.utils.CommonUtil;
import du.tools.main.ConfigAccessor;
import du.tools.main.git.GitCommand;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class WinGitClone extends JDialog {

    private String path;
    private String result;

    public WinGitClone(String path) {
        super(WinMain.frame, true);
        this.path = CommonUtil.trimLeft(path);
        initialize();
    }

    private void initialize() {
        setTitle("Clone project: " + path);
        setSize(460, 160);
        setLocationRelativeTo(WinMain.frame);
        setLayout(null);

        JLabel label = new JLabel("Clone from: ");
        label.setBounds(10, 10, 70, 22);
        add(label);
        final String url = ConfigAccessor.getInstance().getHostName() + ":" + path + ".git";
        label = new JLabel(url);
        label.setBounds(80, 10, 350, 22);
        add(label);

        label = new JLabel("Clone into: ");
        label.setBounds(10, 40, 70, 22);
        add(label);
        String localRepo = ConfigAccessor.getInstance().getLocalRepo();
        localRepo = CommonUtil.toBackSlash(localRepo);
        localRepo = CommonUtil.addRight(localRepo);
        final JTextField location = new JTextField(localRepo + CommonUtil.toBackSlash(path));
        location.setBounds(80, 40, 350, 25);
        location.setEditable(false);
        add(location);

        JButton btnClone = new JButton("CLONE");
        btnClone.setBounds(10, 80, 80, 25);
        btnClone.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                File project = new File(location.getText());
                GitCommand git = new GitCommand();

                if (project.exists()) {
                    int answer = JOptionPane.showConfirmDialog(WinGitClone.this, "Exists in local, do you want to delete the existing repo?");
                    if (answer == JOptionPane.OK_OPTION) {
                        if (!project.delete()) {
                            JOptionPane.showMessageDialog(WinGitClone.this, "Delete directory failed, please delete manually.");
                            return;
                        }
                    }
                }

                if (!project.getParentFile().exists()) {
                    if (!project.getParentFile().mkdirs()) {
                        setVisible(false);
                        result = "FAIL";
                        JOptionPane.showMessageDialog(WinGitClone.this, "Creates the directory failed:" + project.getParentFile().getAbsoluteFile());
                    }
                }
                String msg = git.clone(project.getParentFile(), url);
                JOptionPane.showMessageDialog(WinGitClone.this, msg);
                if (msg.contains("fatal:")) {
                    result = msg;
                } else {
                    setVisible(false);
                    result = "OK";
                }
            }
        });
        add(btnClone);

        JButton btnCancel = new JButton("CANCEL");
        btnCancel.setBounds(100, 80, 80, 25);
        btnCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                result = "CANCEL";
            }
        });
        add(btnCancel);
    }

    public String open() {
        setVisible(true);
        return result;
    }
}