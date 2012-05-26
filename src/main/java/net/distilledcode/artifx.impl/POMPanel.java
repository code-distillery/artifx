package net.distilledcode.artifx.impl;

import org.apache.felix.webconsole.ConfigurationPrinter;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

public class POMPanel implements ConfigurationPrinter, BundleActivator {

    private static final String TITLE = "POM";

    private final static String SERVICE_NAME = ConfigurationPrinter.class.getName();

    final Dictionary<String, Object> properties = new Hashtable<String, Object>() {{
        put(Constants.SERVICE_DESCRIPTION, "POM Panel (" + SERVICE_NAME + ")");
        put(Constants.SERVICE_VENDOR, "Code Distillery");
    }};

    private BundleContext bundleContext;
    private ServiceRegistration serviceRegistration;

    /**
     * @see org.apache.felix.webconsole.ConfigurationPrinter#getTitle()
     */
    public String getTitle() {
        return TITLE;
    }

    // ---------- ConfigurationPrinter

    /**
     * @see org.apache.felix.webconsole.ConfigurationPrinter#printConfiguration(java.io.PrintWriter)
     */
    public void printConfiguration(PrintWriter pw) {
        final Bundle[] bundles = bundleContext.getBundles();
        for (final Bundle bundle : bundles) {

            @SuppressWarnings("unchecked")
            final Enumeration<URL> urls = (Enumeration<URL>) bundle.findEntries("META-INF/maven", "pom.properties", true);
            final URL url = urls != null && urls.hasMoreElements() ? urls.nextElement() : null;

            final String[] exports = parseExports((String) bundle.getHeaders().get(Constants.EXPORT_PACKAGE));

            if (exports == null || url == null) {
                String reason = "";
                {
                    final String[] reasons = {"missing exports", "missing POM data"};
                    final Object[] conditions = {exports, url};

                    for (int i = 0; i < conditions.length; i++) {
                        if (conditions[i] == null) {
                            reason += (reason.equals("") ? "" : " and ") + reasons[i];
                        }
                    }
                }

                pw.println("<!-- skipped bundle " + bundle.getSymbolicName() + " due to " + reason + " -->");
                pw.println("-----------------------------------------------------------------------------");
                continue;
            }


            final Properties mavenProperties = new Properties();
            try {
                mavenProperties.load(url.openStream());
            } catch (IOException e) {
                // ignore
                continue;
            }

            // TODO: HTML escape external strings
            pw.println("<dependency>");
            for (final String name : new String[]{"groupId", "artifactId", "version"}) {
                pw.println(String.format("    <%s>%s</%s>", name, mavenProperties.getProperty(name), name));
            }
            pw.println("    <scope>provided</scope>");
            pw.println("    <!--");
            pw.println("        Export-Package:");
            for (final String export : exports) {
                pw.println("        - " + export);
            }
            pw.println("    -->");
            pw.println("</dependency>");
        }
    }

    private String[] parseExports(String rawExports) {
        if (rawExports == null) return null;

        final char[] chars = rawExports.toCharArray();
        final List<String> exports = new ArrayList<String>();
        boolean insideQuotes = false;
        int nextStart = 0;

        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == ',' && !insideQuotes) {
                exports.add(normalizeExportString(new String(chars, nextStart, i - nextStart)));
                nextStart = i + 1;
            }

            if (chars[i] == '\"') insideQuotes = !insideQuotes;
        }
        exports.add(normalizeExportString(new String(chars, nextStart, chars.length - nextStart)));
        return exports.toArray(new String[exports.size()]);
    }

    private String normalizeExportString(String rawExport) {
        final StringBuilder normalized = new StringBuilder();
        final String[] parts = rawExport.trim().split(";");
        for (int i = 0; i < parts.length; i++) {
            if (!parts[i].startsWith("uses:=")) {
                normalized.append(i == 0 ? "" : ";").append(parts[i]);
            }
        }
        return normalized.toString();
    }


    // ---------- BundleActivator
    public void start(BundleContext bundleContext) {
        this.bundleContext = bundleContext;

        try {
            serviceRegistration = bundleContext.registerService(SERVICE_NAME, this, properties);
        } catch (Throwable t) {
            // web console might not be available, don't care
        }
    }

    public void stop(BundleContext bundleContext) {
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
            serviceRegistration = null;
        }
        this.bundleContext = null;
    }
}