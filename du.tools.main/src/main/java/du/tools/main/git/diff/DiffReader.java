package du.tools.main.git.diff;

import org.apache.commons.lang.StringUtils;
import du.tools.main.git.GitCommand;
import du.tools.main.git.status.Status;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiffReader {

    public static Map<String, Difference> getDiffs(File root) {
        String result = new GitCommand().diffHead(root);
        return getDiffs(result);
    }

    public static Map<String, Difference> getDiffs(String diffText) {
        String regex = "diff \\-\\-git a/(.*) b/(.*)\r\n((new|deleted) file mode ([0-9]*)\r\n)?index (.*)\\.\\.(.*) ?([0-9]*)\r\n\\-{3} (.*)\r\n\\+{3} (.*)\r\n(Binary files .* differ\r\n)?([\\s\\S]+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(diffText);
        Matcher diffMatcher = Pattern.compile("diff \\-\\-git a/(.*) b/(.*)").matcher(diffText);
        Map<String, Difference> diffs = new HashMap<String, Difference>();
        if (diffMatcher.find()) {
            int start = diffMatcher.start();
            while (start > -1 && start != diffText.length()) {
                int end = -1;
                if (diffMatcher.find()) {
                    end = diffMatcher.start();
                }
                if (end == -1) {
                    end = diffText.length();
                }
                Matcher region = matcher.region(start, end);
                if (region.find()) {
                    Difference diff = new Difference();
                    diff.setaName(region.group(1));
                    diff.setaName(region.group(2));

                    String t = matcher.group(4);
                    Status status = new Status();
                    status.setFile(diff.getbName());
                    status.setTracked(true);
                    status.setStaged(true);
                    if (StringUtils.equals(diff.getaName(), diff.getbName())) {
                        status.setRenamed(true);
                    } else if ("new".equals(t)) {
                        status.setAdded(true);
                    } else if ("deleted".equals(t)) {
                        status.setDeleted(true);
                    } else {
                        status.setModified(true);
                    }
                    diff.setStatus(status);

                    if (region.group(11) != null) {
                        diff.setTextFile(false);
                    }

                    String dText = region.group(12);
                    parseDetail(dText, diff);
                    diffs.put(diff.getaName(), diff);
                }
                start = end;
            }
        }

        return diffs;
    }

    private static void parseDetail(String diffText, Difference diff) {
        String regex = "@@ \\-([0-9]+),([0-9]+) \\+([0-9]+),([0-9]+) @@(\r\n)?([\\s\\S]+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(diffText);
        Matcher diffMatcher = Pattern.compile("@@ \\-([0-9]+),([0-9]+) \\+([0-9]+),([0-9]+) @@(.*)").matcher(diffText);
        if (diffMatcher.find()) {
            int start = diffMatcher.start();
            while (start > -1 && start != diffText.length()) {
                int end = -1;
                if (diffMatcher.find()) {
                    end = diffMatcher.start();
                }
                if (end == -1) {
                    end = diffText.length();
                }
                Matcher region = matcher.region(start, end);
                if (region.find()) {
                    int aNo = Integer.parseInt(region.group(1));
                    int bNo = Integer.parseInt(region.group(3));
                    String text = region.group(6);
                    String[] lines = text.split("\r\n");
                    for (String line : lines) {
                        if (line.startsWith("-")) {
                            diff.addaLine(aNo, line.substring(1));
                            aNo++;
                        } else if (line.startsWith("+")) {
                            diff.addbLine(bNo, line.substring(1));
                            bNo++;
                        } else {
                            aNo++;
                            bNo++;
                        }
                    }
                }
                start = end;
            }
        }
    }
}
