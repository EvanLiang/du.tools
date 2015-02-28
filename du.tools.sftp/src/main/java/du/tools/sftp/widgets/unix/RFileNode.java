package du.tools.sftp.widgets.unix;

import du.tools.sftp.unix.RemoteService;

public class RFileNode extends DFileNode<RemoteService.RFile> {
    public RFileNode(RemoteService.RFile file) {
        super(file);
    }

    protected void cacheChildren() {
        RemoteService.RFile[] files = getFile().listFiles();
        if (files != null) {
            for (RemoteService.RFile f : files) {
                insert(new RFileNode(f), children.size());
            }
        }
    }
}
