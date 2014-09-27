package du.tools.main.git.log;


import du.tools.main.git.GitCommand;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogReader {

    public static List<Log> getLogs(File root) throws IOException {
        String regex = "commit (.*)\r\nAuthor: (.*)\r\nDate:   (.*)\r\n\r\n    (.*)\r\n";
        String result = new GitCommand().log(root);
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(result);
        List<Log> logs = new ArrayList<Log>();
        while (matcher.find()) {
            Log log = new Log();
            log.setCommit(matcher.group(1));
            log.setAuthor(matcher.group(2));
            log.setDate(matcher.group(3));
            log.setMessage(matcher.group(4));
            log.setDiffsText(null);
            logs.add(log);
        }
        return logs;
    }

    public static List<Log> getLogsWithDifference(File root) throws IOException {
        String regex = "commit (.*)\r\nAuthor: (.*)\r\nDate:   (.*)\r\n\r\n    (.*)\r\n([\\s\\S]+)";
        String result = new GitCommand().logWithDiff(root);
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(result);
        List<Log> logs = new ArrayList<Log>();
        int start = result.indexOf("commit ");
        while (start > -1 && start != result.length()) {
            int end = result.indexOf("commit ", start + 1);
            if (end == -1) {
                end = result.length();
            }
            Matcher region = matcher.region(start, end);
            if (region.find()) {
                Log log = new Log();
                log.setCommit(region.group(1));
                log.setAuthor(region.group(2));
                log.setDate(region.group(3));
                log.setMessage(region.group(4));
                log.setDiffsText(region.group(5));
                logs.add(log);
            }
            start = end;
        }
        return logs;
    }
}
