package net.distilledcode.artifx.impl.generatedpom;

import net.distilledcode.artifx.impl.DependencyInspector;
import net.distilledcode.artifx.impl.util.DigestUtil;
import net.distilledcode.artifx.impl.util.PathUtil;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

/**
 * Generates a POM file containing dependency definitions for
 * all OSGi bundles that are installed in the OSGi system and
 * export any packages.
 */
public class GeneratedPOMResource extends StringResource {

    private static final String PATH_TEMPLATE = "/local/generated-pom/{version}/generated-pom-{version}.pom";

    private final DependencyInspector dependencyInspector;
    private final String dependencies;
    private final String version;

    GeneratedPOMResource(final DependencyInspector di) {
        super("", null); // neither path nor contents are pre-set
        dependencyInspector = di;
        dependencies = indent("        ", dependencyInspector.getDependencyString());
        version = calculateVersion(dependencies);
    }

    public String getPath() {
        return PathUtil.normalize(PATH_TEMPLATE.replace("{version}", version));
    }

    // TODO: escape output and set to text/xml
    public String getContentType() {
        return "text/plain";
    }

    public boolean isFolder() {
        return false;
    }

    protected String getContentString() {
        final String preamble =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" +
                "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd\">\n" +
                "    <modelVersion>4.0.0</modelVersion>\n\n" +
                "    <groupId>local</groupId>\n" +
                "    <artifactId>generated-pom</artifactId>\n" +
                "    <packaging>pom</packaging>\n\n" +
                "    <name>OSGi Dependencies</name>\n" +
                "    <description>\n" +
                "        POM generated from bundle information available\n" +
                "        in the OSGi framework.\n" +
                "    </description>\n" +
                "    <version>" + version + "</version>\n\n" +
                "    <dependencies>\n";
        final String postfix =
                "    </dependencies>\n" +
                "</project>\n";

        return preamble + dependencies + postfix;
    }

    private String calculateVersion(final String dependencies) {
        return DigestUtil.md5(new ByteArrayInputStream(getBytesFromString(dependencies)));
    }

    private String indent(final String indentation, final String source) {
        final String[] lines = source.split("\\n");
        final StringBuilder indented = new StringBuilder();
        for (final String line : lines) {
            indented.append(indentation).append(line).append("\n");
        }
        return indented.toString();
    }

    protected byte[] getBytesFromString(String string) {
        try {
            return string.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            return string.getBytes();
        }
    }
}
