package du.tools.explorer.widgets.unix;

import du.tools.explorer.unix.RemoteService;

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