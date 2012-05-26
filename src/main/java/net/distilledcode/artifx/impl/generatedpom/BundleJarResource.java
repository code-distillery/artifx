package net.distilledcode.artifx.impl.generatedpom;

import net.distilledcode.artifx.api.Resource;
import net.distilledcode.artifx.impl.util.DigestUtil;
import org.osgi.framework.Bundle;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

public class BundleJarResource extends FolderResource {

    private File bundleJar;
    private Bundle bundle;

    BundleJarResource(final Bundle bundle, final File bundleJar, final String path) {
        super(path);
        this.bundle = bundle;
        this.bundleJar = bundleJar;
    }

    public InputStream getContents() {
        try {
            return new BufferedInputStream(new FileInputStream(bundleJar));
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    public int getContentLength() {
        return (int)bundleJar.length();
    }

    public String getContentType() {
        return "application/java-archive";
    }

    public boolean isFolder() {
        return false;
    }

    Resource getPomResource() {
        @SuppressWarnings("unchecked")
        final Enumeration<URL> urls = (Enumeration<URL>) bundle.findEntries("META-INF/maven", "pom.xml", true);
        final URL url = urls != null && urls.hasMoreElements() ? urls.nextElement() : null;
        try {
            // TODO: error handling: what to do if inputStreamtoString returns null?
            return new StringResource(getPath().replaceAll(".jar$", ".pom"), inputStreamtoString(url.openStream()));
        } catch (IOException e) {
            return null;
        }
    }

    private String inputStreamtoString(InputStream is) {
        final StringBuilder sb = new StringBuilder();
        final byte[] bytes = new byte[2048];
        int c;
        try {
            while (0 < (c = is.read(bytes))) {
                sb.append(new String(bytes, 0, c, "UTF-8"));
            }
        } catch (IOException e) {
            return null;
        }
        return sb.toString();
    }

    Resource getPomMd5Resource() {
        final Resource pomResource = getPomResource();
        return new StringResource(pomResource.getPath() + ".md5", DigestUtil.md5(pomResource.getContents()));
    }

    Resource getPomSha1Resource() {
        final Resource pomResource = getPomResource();
        return new StringResource(pomResource.getPath() + ".sha1", DigestUtil.sha1(pomResource.getContents()));
    }

    Resource getMd5Resource() {
        return new StringResource(getPath() + ".md5", DigestUtil.md5(getContents()));
    }

    Resource getSha1Resource() {
        return new StringResource(getPath() + ".sha1", DigestUtil.sha1(getContents()));
    }
}
