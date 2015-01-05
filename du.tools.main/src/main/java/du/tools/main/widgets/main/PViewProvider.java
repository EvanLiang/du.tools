package du.tools.main.widgets.main;

import org.eclipse.jgit.api.Status;

import java.awt.*;

public class PViewProvider {

    public static StatusCode statusCode(Status status, PViewNode node) {
        if (status != null && node.isFile()) {
            String key = node.getFilePath();
            if (key.startsWith("/")) {
                key = key.substring(1);
            }
            if (status.getModified().contains(key)) {
                return StatusCode.M;
            }
            if (status.getAdded().contains(key)) {
                return StatusCode.A;
            }
            if (status.getMissing().contains(key)) {
                return StatusCode.R;
            }
            if (status.getRemoved().contains(key)) {
                return StatusCode.D;
            }
            if (status.getUntracked().contains(key)) {
                return StatusCode.U;
            }
        }
        return null;
    }

    public enum StatusCode {
        M("M", new Color(86, 132, 150)),
        A("A", new Color(0, 163, 0)),
        R("R", new Color(83, 113, 131)),
        D("D", new Color(88, 88, 88)),
        U("U", new Color(169, 13, 15));

        private String code;
        private Color color;

        private StatusCode(String code, Color color) {
            this.code = code;
            this.color = color;
        }

        public Color color() {
            return color;
        }

        public String toString() {
            return code;
        }
    }
}
