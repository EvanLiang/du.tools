package du.tools.commons;

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    private static final String PATTERN = "(.*):(.*)@([\\.0-9]*):?(.*)?";

    public static URL getClasspathURL(String path) {
        return ClassLoader.getSystemClassLoader().getResource(path);
    }

    /**
     * @param connectionStr (.*):(.*)@([\.0-9]*):?(.*)?
     * @return {host, user, password, path}
     */
    public static String[] parseUnixConnection(String connectionStr) {
        Matcher m = Pattern.compile(PATTERN).matcher(connectionStr);
        if (m.find()) {
            return new String[]{m.group(3), m.group(1), m.group(2), m.group(4)};
        } else {
            return null;
        }
    }
}
