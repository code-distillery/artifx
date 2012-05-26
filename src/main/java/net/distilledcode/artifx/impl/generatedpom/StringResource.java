package net.distilledcode.artifx.impl.generatedpom;

import net.distilledcode.artifx.api.Resource;
import net.distilledcode.artifx.impl.util.PathUtil;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

public class StringResource implements Resource {

    private String path;
    private String contents;

    StringResource(final String path, final String contents) {
        this.path = PathUtil.normalize(path);
        this.contents = contents;
    }

    public String getName() {
        return PathUtil.getName(getPath());
    }

    public String getPath() {
        return path;
    }

    public InputStream getContents() {
        return new ByteArrayInputStream(getBytesFromString(getContentString()));
    }

    public int getContentLength() {
        return getBytesFromString(getContentString()).length;
    }

    public String getContentType() {
        return "text/plain";
    }

    public boolean isFolder() {
        return false;
    }

    protected String getContentString() {
        return contents;
    }

    protected byte[] getBytesFromString(String string) {
        try {
            return string.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            return string.getBytes();
        }
    }
}
