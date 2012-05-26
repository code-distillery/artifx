package net.distilledcode.artifx.api;

import java.util.Iterator;

/**
 *
 */
public interface ArtifactProvider {

    public boolean hasResource(final String path);

    public Resource getResource(final String path);

    public Iterator<Resource> listChildren(final String path);
}
