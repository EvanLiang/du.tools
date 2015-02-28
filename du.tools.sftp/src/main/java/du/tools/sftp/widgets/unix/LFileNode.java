package du.tools.sftp.widgets.unix;

import java.io.File;

public class LFileNode extends DFileNode<File> {

    public LFileNode(File file) {
        super(file);
    }

    protected void cacheChildren() {
        File[] files = getFile().listFiles();
        if (files != null) {
            for (File f : files) {
                insert(new LFileNode(f), children.size());
            }
        }
    }
}
