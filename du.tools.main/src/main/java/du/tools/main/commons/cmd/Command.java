package du.tools.main.commons.cmd;


import du.tools.main.commons.utils.CommonUtil;

import java.io.*;

public class Command {

    public String diff(String param, File a, File b) {
        File dir = new File(System.getProperty("user.dir"));
        File exe = CommonUtil.getFileInClasspath("/diff/diff.exe");
        return callExe(dir, exe, param, a.getAbsolutePath(), b.getAbsolutePath());
    }

    protected String callExe(File dir, File exe, String... params) {
        try {
            ProcessBuilder pb = new ProcessBuilder();
            pb.redirectErrorStream(true);
            pb.directory(dir);
            String[] command = new String[params.length + 1];
            command[0] = exe.getAbsolutePath();
            System.arraycopy(params, 0, command, 1, params.length);
            pb.command(command);
            Process pro = pb.start();
            InputStream in = pro.getInputStream();
            String result = readResult(in);
            pro.destroy();
            return result;
        } catch (IOException e) {
            throw new RuntimeException("Process failed", e);
        }
    }

    private String readResult(final InputStream inputStream) {
        try {
            StringBuilder output = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            String line;
            while ((line = br.readLine()) != null) {
                output.append(line).append("\r\n");
            }
            return output.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

}