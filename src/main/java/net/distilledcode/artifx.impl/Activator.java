package net.distilledcode.artifx.impl;

import org.apache.felix.webconsole.SimpleWebConsolePlugin;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import java.util.ArrayList;

public class Activator implements BundleActivator {

    private final ArrayList<SimpleWebConsolePlugin> plugins = new ArrayList<SimpleWebConsolePlugin>();

    public void start(BundleContext bundleContext) {
        plugins.add(new ArtifXServlet());

        for (final SimpleWebConsolePlugin plugin : plugins) {
            plugin.register(bundleContext);
        }
    }

    public void stop(BundleContext bundleContext) {
        for (final SimpleWebConsolePlugin plugin : plugins) {
            plugin.unregister();
        }
        plugins.clear();
    }
}
