package du.tools.main;

import lib.common.utils.XPathUtil;
import org.w3c.dom.Document;

import java.util.List;

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
    private static final String XPATH_HOST_NAME = "/Config/Projects/HostName";
    private static final String XPATH_REMOTE_HOST = "/Config/Projects/RemoteHost";
    private static final String XPATH_LOCAL_REPO = "/Config/Projects/LocalRepo";
    private static ConfigAccessor context;
    private Document document;


    private ConfigAccessor() {
        loadConfig();
    }

    public void loadConfig() {
        document = XPathUtil.getDocument("config.xml");
    }

    public static ConfigAccessor getInstance() {
        if (context == null) {
            context = new ConfigAccessor();
        }
        return context;
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
        return XPathUtil.getString(document, XPATH_CONSOLE_BUTTON_COMMAND.replace("${name}", buttonName));
    }

    public String getConsoleButtonGroup(String buttonName) {
        return XPathUtil.getString(document, XPATH_CONSOLE_BUTTON_GROUP.replace("${name}", buttonName));
    }

    public List<String> getConsoleButtonCommands(String buttonName) {
        return XPathUtil.getStringList(document, XPATH_CONSOLE_BUTTON_COMMAND.replace("${name}", buttonName));
    }

    public String getConsoleButtonWorkDir(String buttonName) {
        return XPathUtil.getString(document, XPATH_CONSOLE_BUTTON_WORKDIR.replace("${name}", buttonName));
    }

    public String getGitHome() {
        return XPathUtil.getString(document, XPATH_GIT_HOME);
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
}
