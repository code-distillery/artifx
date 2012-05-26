package net.distilledcode.artifx.api;

import java.util.Iterator;

/**
 * May be used to implement caching, eventually...
 */
public abstract class AbstractArtifactProvider implements ArtifactProvider {

    public boolean hasResource(String path) {
        return getResource(path) != null;
    }

    public Resource getResource(String path) {
        return doGetResource(path);
    }

    public Iterator<Resource> listChildren(String path) {
        return doListChildren(path);
    }

    protected abstract Resource doGetResource(String path);

    protected abstract Iterator<Resource> doListChildren(final String path);
}
