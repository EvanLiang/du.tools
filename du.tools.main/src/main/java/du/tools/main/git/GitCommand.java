package du.tools.main.git;


import du.tools.main.ConfigAccessor;
import du.tools.main.commons.cmd.Command;

import java.io.File;

public class GitCommand extends Command {
    private File exe = new File(ConfigAccessor.getInstance().getGitHome() + "\\bin\\git.exe");

    public static void main(String[] args) {
        //System.out.println(new Command().diff("-u", new File("a.txt"), new File("b.txt")));
//        File dir = new File(System.getProperty("user.dir"));
        //System.out.println(new GitCommand().gitStatus(dir));

//        File dir = new File("D:\\");
//        System.out.println(new GitCommand().clone(dir, "wg:git.helper.evnwhp.git"));
    }

    public String clone(File root, String url) {
        return callExe(root, exe, "clone", url);
    }

    public String status(File root) {
        return callExe(root, exe, "status", "-s");
    }

    public String branch(File root) {
        return callExe(root, exe, "branch");
    }

    public String branchAll(File root) {
        return callExe(root, exe, "branch", "-a");
    }

    public String checkout(File root, String branch) {
        return callExe(root, exe, "checkout", branch);
    }

    public String fetch(File root) {
        return callExe(root, exe, "fetch");
    }

    public String add(File root, String path) {
        return callExe(root, exe, "add", path);
    }

    public String addAll(File root) {
        return callExe(root, exe, "add", "*");
    }

    public String diff(File root) {
        return callExe(root, exe, "diff");
    }

    public String diffHead(File root) {
        return callExe(root, exe, "diff", "HEAD");
    }

    public String diff(File root, String branch) {
        return callExe(root, exe, "diff", branch);
    }

    public String diff(File root, String a, String b) {
        return callExe(root, exe, "diff", a, b);
    }

    public String log(File root) {
        return callExe(root, exe, "log");
    }

    public String logWithDiff(File root) {
        return callExe(root, exe, "log", "-p");
    }
}
