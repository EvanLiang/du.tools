package du.tools.main.git.status;

import du.tools.main.commons.utils.CommonUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StatusReader {

    public static Map<String, Status> getStatus(File root, String statusText) {
        Pattern pattern = Pattern.compile("([ ADMR\\?])([ ADMR\\?]) ([0-9a-zA-Z_/\\.]+)|((.*) \\-> ([0-9a-zA-Z_/\\.]+))");
        Map<String, Status> statusMap = new HashMap<String, Status>();
        try {
            BufferedReader br = new BufferedReader(new StringReader(statusText));
            String line;
            while ((line = br.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    String file = matcher.group(5);
                    if (file == null) {
                        file = matcher.group(3);
                    }
                    String stype = matcher.group(1);
                    boolean cached = StringUtils.isNotBlank(stype) && !"?".equals(stype);
                    if (StringUtils.isBlank(stype)) {
                        stype = matcher.group(2);
                    }
                    if (file.endsWith("/")) {
                        loopDir(root, file, cached, stype, statusMap);
                    } else {
                        Status status = createStatus(file, cached, stype);
                        statusMap.put(file, status);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return statusMap;
    }

    private static void loopDir(File root, String folder, boolean cached, String stype, Map<String, Status> statusMap) {
        File dir = new File(root, folder);
        Collection<File> files = FileUtils.listFiles(dir, FileFileFilter.FILE, DirectoryFileFilter.DIRECTORY);
        for (File file : files) {
            String name = file.getAbsolutePath();
            name = CommonUtil.toSlash(name);
            name = name.replace(CommonUtil.toSlash(root.toString()), "");
            name = CommonUtil.trimLeft(name);
            Status status = createStatus(name, cached, stype);
            statusMap.put(name, status);
        }
    }

    private static Status createStatus(String file, boolean cached, String stype) {
        Status status = new Status();
        status.setFile(file);
        status.setStaged(cached);
        status.setAdded("A".equals(stype));
        status.setModified("M".equals(stype));
        status.setDeleted("D".equals(stype));
        status.setRenamed("R".equals(stype));
        status.setTracked(!"?".equals(stype));
        return status;
    }

}
