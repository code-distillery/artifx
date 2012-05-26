package net.distilledcode.artifx.impl.bundle;

import net.distilledcode.artifx.api.ArtifactProvider;
import net.distilledcode.artifx.api.Resource;
import org.osgi.framework.BundleContext;

import java.util.Iterator;

public class BundleArtifactProvider implements ArtifactProvider {

    private BundleContext bundleContext;

    public BundleArtifactProvider(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public boolean hasResource(String path) {
        return getResource(path) != null;
    }

    public Resource getResource(String path) {
        return null;
    }

    public Iterator<Resource> listChildren(String path) {
        return null;
    }
}
