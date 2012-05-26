package net.distilledcode.artifx.api;

import java.io.InputStream;

public interface Resource {

    public String getName();

    /**
     * @return The path of this resource.
     */
    public String getPath();

    /**
     * @return InputStream representing the contents of the resource or
     *         <code>null</code> if the resource is a folder {@see isFolder}.
     */
    public InputStream getContents();

    /**
     * @return The number of bytes returned by the {@see getContents} method
     *         or -1 if that information is not available.
     */
    public int getContentLength();

    /**
     * @return The content type of the resource or null. Needs to fulfill the criteria
     *         specified in <code>{@see javax.servlet.ServletResponse#setContentType()}</code>.
     */
    public String getContentType();

    /**
     * @return true if the resource represents a folder, i.e. it has no content
     *         but may have children.
     */
    public boolean isFolder();

}
