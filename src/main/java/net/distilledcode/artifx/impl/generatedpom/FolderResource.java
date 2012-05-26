package net.distilledcode.artifx.impl.generatedpom;

import net.distilledcode.artifx.api.Resource;
import net.distilledcode.artifx.impl.util.PathUtil;

import java.io.InputStream;

public class FolderResource implements Resource {

    protected String path;

    public FolderResource(String path) {
        this.path = PathUtil.normalize(path);
    }

    public String getName() {
        return PathUtil.getName(getPath());
    }

    public String getPath() {
        return path;
    }

    public InputStream getContents() {
        return null;
    }

    public int getContentLength() {
        return -1;
    }

    public String getContentType() {
        return null;
    }

    public boolean isFolder() {
        return true;
    }

}
