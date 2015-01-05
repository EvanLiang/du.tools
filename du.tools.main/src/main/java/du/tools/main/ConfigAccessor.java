package du.tools.main;

import du.tools.main.commons.utils.CommonUtil;
import du.tools.main.commons.utils.XPathUtil;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigAccessor {

    private static final String XPATH_ROOT_NAME = "/Config/Projects/RootName";
    private static final String XPATH_PROJECT_DIRECTORY = "/Config/Projects/Project/Directory";
    private static final String XPATH_PROJECT_SKIPS = "/Config/Projects/Skips/Skip";

    private static final String XPATH_CONSOLE_INIT_COMMAND = "/Config/Console/Init/Command";
    private static final String XPATH_CONSOLE_BEFORE_COUNT = "/Config/Console/WorkDirMatch";
    private static final String XPATH_CONSOLE_BEFORE_DIRMATCH = "/Config/Console/WorkDirMatch[${index}]/DirMatch";
    private static final String XPATH_CONSOLE_BEFORE_COMMAND = "/Config/Console/WorkDirMatch[${index}]/Command";


    private static final String XPATH_CONSOLE_ENV_BUTTON_NAME = "/Config/Console/EnvButton/Name";
    private static final String XPATH_CONSOLE_ENV_BUTTON_DESCRIPTION = "/Config/Console/EnvButton[Name='${name}']/Description";
    private static final String XPATH_CONSOLE_ENV_BUTTON_COMMAND = "/Config/Console/EnvButton[Name='${name}']/Command";

    private static final String XPATH_CONSOLE_BUTTON_NAME = "/Config/Console/ActionButton/Name";
    private static final String XPATH_CONSOLE_BUTTON_DESCRIPTION = "/Config/Console/ActionButton[Name='${name}']/Description";
    private static final String XPATH_CONSOLE_BUTTON_GROUP = "/Config/Console/ActionButton[Name='${name}']/Group";
    private static final String XPATH_CONSOLE_BUTTON_WORKDIR = "/Config/Console/ActionButton[Name='${name}']/WorkDir";
    private static final String XPATH_CONSOLE_BUTTON_COMMAND = "/Config/Console/ActionButton[Name='${name}']/Command";

    private static final String XPATH_GIT_HOME = "/Config/Git/Home";
    private static final String XPATH_MAVEN_HOME = "/Config/Maven/Home";
    private static final String XPATH_IDE_EXE = "/Config/Ide/Exe";
    private static final String XPATH_HOST_NAME = "/Config/Projects/HostName";
    private static final String XPATH_REMOTE_HOST = "/Config/Projects/RemoteHost";
    private static final String XPATH_LOCAL_REPO = "/Config/Projects/LocalRepo";

    private static final String XPATH_STASH_USER = "/Config/Stash/User";
    private static final String XPATH_STASH_PASSWORD = "/Config/Stash/Password";

    private static final String XPATH_UNIX_USER = "/Config/Unix/User";
    private static final String XPATH_UNIX_PASSWORD = "/Config/Unix/Password";
    private static final String XPATH_UNIX_HOSTS = "/Config/Unix/Hosts/Host";

    private static final String XPATH_TERMINAL_PATH = "/Config/Terminal/Path";

    private static ConfigAccessor context;
    private Document document;

    public static void main(String[] args) {
        ConfigAccessor.getInstance().getConsoleButtonCommand("INSTALL");
    }

    private ConfigAccessor() {
        loadConfig();
    }

    public void loadConfig() {
        try {
            File bkFile = CommonUtil.getFileInClasspath("cfg/config.xml");
            File config = new File(System.getProperty("user.home"), "du.tools/config.xml");
            if (!config.exists()) {
                if (!config.getParentFile().exists()) {
                    config.getParentFile().mkdir();
                }
                FileUtils.copyFile(bkFile, config);
            }
            document = XPathUtil.getDocument(config);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    private List<String> placeHolder(List<String> list) {
        for (int i = 0; i < list.size(); i++) {
            list.set(i, placeHolder(list.get(i)));
        }
        return list;
    }

    private String placeHolder(String text) {
        Pattern p = Pattern.compile("\\$\\{([A-Za-z/]+)\\}");
        Matcher m = p.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String xp = m.group(1);
            String value = XPathUtil.getString(document, xp);
            m.appendReplacement(sb, value);
        }
        m.appendTail(sb);
        return sb.toString();
    }

    public static ConfigAccessor getInstance() {
        if (context == null) {
            context = new ConfigAccessor();
        }
        return context;
    }

    public String getStashUser() {
        return XPathUtil.getString(document, XPATH_STASH_USER);
    }

    public String getStashPassword() {
        return XPathUtil.getString(document, XPATH_STASH_PASSWORD);
    }

    public String getUnixUser() {
        return XPathUtil.getString(document, XPATH_UNIX_USER);
    }

    public String getUnixPassword() {
        return XPathUtil.getString(document, XPATH_UNIX_PASSWORD);
    }

    public List<String> getUnixHosts() {
        return XPathUtil.getStringList(document, XPATH_UNIX_HOSTS);
    }

    public String getRootName() {
        return XPathUtil.getString(document, XPATH_ROOT_NAME);
    }

    public List<String> getProjectDirectories() {
        return XPathUtil.getStringList(document, XPATH_PROJECT_DIRECTORY);
    }

    public List<String> getProjectSkips() {
        return XPathUtil.getStringList(document, XPATH_PROJECT_SKIPS);
    }

    public List<String> getConsoleInitCommands() {
        return XPathUtil.getStringList(document, XPATH_CONSOLE_INIT_COMMAND);
    }

    public int countConsoleBefore() {
        return XPathUtil.countElement(document, XPATH_CONSOLE_BEFORE_COUNT);
    }

    public List<String> getConsoleBeforeDirMatches(int index) {
        return XPathUtil.getStringList(document, XPATH_CONSOLE_BEFORE_DIRMATCH.replace("${index}", String.valueOf(index)));
    }

    public String getConsoleBeforeCommand(int index) {
        return XPathUtil.getString(document, XPATH_CONSOLE_BEFORE_COMMAND.replace("${index}", String.valueOf(index)));
    }

    public List<String> getConsoleButtonNames() {
        return XPathUtil.getStringList(document, XPATH_CONSOLE_BUTTON_NAME);
    }

    public String getConsoleButtonDescription(String buttonName) {
        return XPathUtil.getString(document, XPATH_CONSOLE_BUTTON_DESCRIPTION.replace("${name}", buttonName));
    }

    public String getConsoleButtonCommand(String buttonName) {
        return placeHolder(XPathUtil.getString(document, XPATH_CONSOLE_BUTTON_COMMAND.replace("${name}", buttonName)));
    }

    public String getConsoleButtonGroup(String buttonName) {
        return XPathUtil.getString(document, XPATH_CONSOLE_BUTTON_GROUP.replace("${name}", buttonName));
    }

    public List<String> getConsoleButtonCommands(String buttonName) {
        return placeHolder(XPathUtil.getStringList(document, XPATH_CONSOLE_BUTTON_COMMAND.replace("${name}", buttonName)));
    }

    public String getConsoleButtonWorkDir(String buttonName) {
        return XPathUtil.getString(document, XPATH_CONSOLE_BUTTON_WORKDIR.replace("${name}", buttonName));
    }

    public String getGitHome() {
        return XPathUtil.getString(document, XPATH_GIT_HOME);
    }

    public String getIdeEex() {
        return XPathUtil.getString(document, XPATH_IDE_EXE);
    }

    public String getMavenHome() {
        return XPathUtil.getString(document, XPATH_MAVEN_HOME);
    }

    public String getHostName() {
        return XPathUtil.getString(document, XPATH_HOST_NAME);
    }

    public String getRemoteHost() {
        return XPathUtil.getString(document, XPATH_REMOTE_HOST);
    }

    public String getLocalRepo() {
        return XPathUtil.getString(document, XPATH_LOCAL_REPO);
    }

    public List<String> getConsoleEnvButtons() {
        return XPathUtil.getStringList(document, XPATH_CONSOLE_ENV_BUTTON_NAME);
    }

    public List<String> getConsoleEnvButtonCommands(String name) {
        return XPathUtil.getStringList(document, XPATH_CONSOLE_ENV_BUTTON_COMMAND.replace("${name}", name));
    }

    public String getConsoleEnvButtonDescription(String name) {
        return XPathUtil.getString(document, XPATH_CONSOLE_ENV_BUTTON_DESCRIPTION.replace("${name}", name));
    }

    public String getTerminalPath() {
        return XPathUtil.getString(document, XPATH_TERMINAL_PATH);
    }
}
