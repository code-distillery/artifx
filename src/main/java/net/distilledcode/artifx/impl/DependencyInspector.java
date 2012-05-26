package net.distilledcode.artifx.impl;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

public class DependencyInspector {

    private final static Logger log = LoggerFactory.getLogger(DependencyInspector.class);

    private final BundleContext bundleContext;

    public DependencyInspector(final BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public String getDependencyString() {
        final StringBuilder sb = new StringBuilder();
        for (final Dependency dep : getDependencies()) {
            sb.append(dep.toString());
        }
        return sb.toString();
    }

    public List<Dependency> getDependencies() {
        final Bundle[] bundles = bundleContext.getBundles();

        final List<Dependency> dependencies = new ArrayList<Dependency>();
        for (final Bundle bundle : bundles) {
            final Dependency dep = getDependency(bundle);
            if (dep != null) {
                dependencies.add(dep);
            }
        }

        Collections.sort(dependencies, new Comparator<Dependency>() {
            public int compare(final Dependency a, final Dependency b) {
                final int groups = a.groupId().compareTo(b.groupId());
                final int artifacts = groups == 0 ? a.artifactId().compareTo(b.artifactId()) : 0;
                final int version = groups == 0 && artifacts == 0 ? a.version().compareTo(b.version()) : 0;
                return groups + artifacts + version;
            }
        });
        return dependencies;
    }

    private Dependency getDependency(final Bundle bundle) {
        @SuppressWarnings("unchecked")
        final Enumeration<URL> urls = (Enumeration<URL>) bundle.findEntries("META-INF/maven", "pom.properties", true);
        final URL url = urls != null && urls.hasMoreElements() ? urls.nextElement() : null;
        final String[] exports = parseExports((String) bundle.getHeaders().get(Constants.EXPORT_PACKAGE));

        final File bundleFile = getBundleFile(bundle);

        // TODO: if pom.properties is missing, try guessing the information
        //       - groupId = Bundle-SymbolicName
        //       - artifactId = from jar filename
        //       - version = Bundle-Version or from jar filename
        if (bundleFile == null || exports == null || url == null) {
            log.info("Skipping bundle {} (id:{}), it lacks {} {}", new String[]{bundle.getSymbolicName(), Long.toString(bundle.getBundleId()), (exports == null ? "exports" : ""), (url == null ? "pom.properties" : "")});
            if (bundleFile == null) {
                log.warn("Skipping bundle {} (id:{}). No JAR file found.", bundle.getSymbolicName(), bundle.getBundleId());
            }
            return null;
        }

        final Properties mavenProperties = new Properties();
        Dependency dependency;
        try {
            mavenProperties.load(url.openStream());
            dependency = new Dependency(bundle, bundleFile, mavenProperties, exports);
        } catch (IOException e) {
            log.error("Error reading pom.properties from bundle [{}, {}]", bundle.getBundleId(), bundle.getSymbolicName());
            dependency = null;
        }
        return dependency;
    }

    // TODO: this is Apache Felix specific
    private File getBundleFile(final Bundle bundle) {
        final BundleContext bc = bundle.getBundleContext();
        if (bc == null) {
            log.debug("Bundle {} has no bundle context", bundle.getBundleId());
            return null;
        }

        final File dataFolder = bc.getDataFile("");
        final File bundleFolder = dataFolder.getParentFile();
        final File[] versions = bundleFolder.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return file.getName().startsWith("version");
            }
        });

        if (versions.length > 0) {
            // TODO: respect version number when sorting, i.e. make version2.0 < version10.0
            Arrays.sort(versions);

            final File[] bundleFiles = versions[versions.length - 1].listFiles(new FileFilter() {
                public boolean accept(File file) {
                    return file.getName().equals("bundle.jar");
                }
            });

            if (bundleFiles.length != 1) {
                log.warn("Found {} bundle.jar files, instead of just a single one. Returning the last one.",
                        bundleFiles.length);
            }

            return bundleFiles[bundleFiles.length - 1];

        } else {
            if (bundle.getBundleId() != 0) {
                log.error("Did not find versions for bundle {} in folder at {}", bundle.getBundleId(), bundleFolder.getAbsolutePath());
            }
            return null;
        }

    }

    // TODO: refactor or replace with Felix utils
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

    // TODO: refactor or replace with Felix utils
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

    public static class Dependency {
        private static final String DEPENDENCY_TEMPLATE =
                "<dependency>\n" +
                "    <groupId>%s</groupId>\n" +
                "    <artifactId>%s</artifactId>\n" +
                "    <version>%s</version>\n" +
                "    <scope>%s</scope>\n" +
                "    <!--\n" +
                "        Export-Package:\n" +
                "%s" +
                "    -->\n" +
                "</dependency>\n";

        private static final String SCOPE = "provided";

        private final Properties props;
        private final String[] exports;
        private File bundleFile;
        private Bundle bundle;

        // TODO: add reference to bundle? or bundle ID?
        private Dependency(final Bundle bundle, final File bundleFile, final Properties props, final String[] exports) {
            this.bundle = bundle;
            this.bundleFile = bundleFile;
            this.props = props;
            this.exports = exports;
        }

        public String groupId() {
            return props.getProperty("groupId");
        }

        public String artifactId() {
            return props.getProperty("artifactId");
        }

        public String version() {
            return props.getProperty("version");
        }

        public String scope() {
            return SCOPE;
        }

        public Bundle getBundle() {
            return bundle;
        }

        public File getFile() {
            return bundleFile;
        }

        public String getJarPath() {
            return groupId().replace(".", "/") + "/" + artifactId() + "/" + version() + "/" + artifactId() + "-" + version() + ".jar";
        }

        public String toString() {
            final StringBuilder comment = new StringBuilder();
            for (final String export : exports) {
                comment.append("        - ").append(export).append("\n");
            }
            return String.format(DEPENDENCY_TEMPLATE, groupId(), artifactId(), version(), scope(), comment.toString());
        }
    }
}
