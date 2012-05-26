package net.distilledcode.artifx.impl.generatedpom;

import net.distilledcode.artifx.api.AbstractArtifactProvider;
import net.distilledcode.artifx.api.Resource;
import net.distilledcode.artifx.impl.DependencyInspector;
import net.distilledcode.artifx.impl.util.DigestUtil;
import net.distilledcode.artifx.impl.util.PathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class GeneratedPOMArtifactProvider extends AbstractArtifactProvider {

    private static final Logger log = LoggerFactory.getLogger(GeneratedPOMArtifactProvider.class);

    private GeneratedPOMResource pom;
    private StringResource sha1;
    private StringResource md5;
    private Map<String,Resource> paths;

    public GeneratedPOMArtifactProvider(final DependencyInspector di) {
        pom = new GeneratedPOMResource(di);

        // It's ok not to close these input streams, because they're backed by strings
        sha1 = new StringResource(pom.getPath() + ".sha1", DigestUtil.sha1(pom.getContents()));
        md5 = new StringResource(pom.getPath() + ".md5", DigestUtil.md5(pom.getContents()));

        paths = new HashMap<String, Resource>();
        paths.put(pom.getPath(), pom);
        paths.put(sha1.getPath(), sha1);
        paths.put(md5.getPath(), md5);
        mkdirp(PathUtil.getParent(pom.getPath()));

        addDependencies(di.getDependencies());

    }

    private void addDependencies(List<DependencyInspector.Dependency> dependencies) {
        for (final DependencyInspector.Dependency dep : dependencies) {
            // TODO: add actual JAR resource + pom + sha1 + md5 ...
            final String jarPath = PathUtil.normalize(dep.getJarPath());
            mkdirp(PathUtil.getParent(jarPath));
            final BundleJarResource jarResource = new BundleJarResource(dep.getBundle(), dep.getFile(), dep.getJarPath());
            paths.put(jarPath, jarResource);
            paths.put(jarResource.getMd5Resource().getPath(), jarResource.getMd5Resource());
            paths.put(jarResource.getSha1Resource().getPath(), jarResource.getSha1Resource());
            if (jarResource.getPomResource() != null) {
                paths.put(jarResource.getPomResource().getPath(), jarResource.getPomResource());
                paths.put(jarResource.getPomMd5Resource().getPath(), jarResource.getPomMd5Resource());
                paths.put(jarResource.getPomSha1Resource().getPath(), jarResource.getPomSha1Resource());
            }
        }
    }


    private void mkdirp(String path) {
        while (path != null && paths.get(path) == null) {
            final FolderResource folder = new FolderResource(path);
            paths.put(path, folder);
            log.debug("Added folder on path: [{}]", path);
            path = PathUtil.getParent(path);
        }
    }


    @Override
    protected Resource doGetResource(final String path) {
        return paths.get(path);
    }

    @Override
    protected Iterator<Resource> doListChildren(final String path) {
        final List<Resource> children = new ArrayList<Resource>();
        for(final String p : paths.keySet()) {
            if (path.equals(PathUtil.getParent(p))) {
                children.add(paths.get(p));
            }
        }
        return children.iterator();
    }
}
