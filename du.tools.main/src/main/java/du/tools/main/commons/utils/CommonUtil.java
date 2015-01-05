package du.tools.main.commons.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;

public class CommonUtil {

    private static Logger logger = LoggerFactory.getLogger(CommonUtil.class);

    public static File getFileInClasspath(String path) {
        URL url = getURLInClasspath(path);
        File file;
        if (url != null) {
            file = new File(url.getFile());
        } else {
            file = new File(path);
        }
        logger.debug("getFileInClasspath():{}", file.getAbsolutePath());
        return file;
    }

    public static URL getURLInClasspath(String path) {
        URL url = CommonUtil.class.getResource(path);
        if (url == null) {
            url = ClassLoader.getSystemClassLoader().getResource(path);
        }
        logger.debug("getURLInClasspath():{}", url != null ? url.toString() : "null");
        return url;
    }

    public static String addRight(String str) {
        if (str.startsWith("/") || str.startsWith("\\")) {
            return str;
        } else {
            if (str.contains("/")) {
                return str + "/";
            } else {
                return str + "\\";
            }
        }
    }

    public static String trimLeft(String str) {
        if (str.startsWith("/") || str.startsWith("\\")) {
            return str.substring(1);
        }
        return str;
    }

    public static String trimRight(String str) {
        if (str.startsWith("/") || str.startsWith("\\")) {
            return str.substring(0, str.length() - 1);
        }
        return str;
    }

    public static String toSlash(String str) {
        return str.replace("\\", "/");
    }

    public static String toBackSlash(String str) {
        return str.replace("/", "\\");
    }
}