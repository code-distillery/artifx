package net.distilledcode.artifx.impl;

import net.distilledcode.artifx.api.Resource;
import net.distilledcode.artifx.impl.generatedpom.GeneratedPOMArtifactProvider;
import net.distilledcode.artifx.impl.util.PathUtil;
import org.apache.felix.webconsole.SimpleWebConsolePlugin;
import org.apache.felix.webconsole.WebConsoleConstants;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Iterator;

public class ArtifXServlet extends SimpleWebConsolePlugin {

    private static final Logger log = LoggerFactory.getLogger(ArtifXServlet.class);

    private static final String LABEL = "artifx";
    private static final String TITLE = "ArtifX";
    private static final String[] CSS_REFERENCES = new String[0];
    private GeneratedPOMArtifactProvider generatedPOMArtifactProvider;
    private DependencyInspector dependencyInspector;

    ArtifXServlet() {
        super(LABEL, TITLE, CSS_REFERENCES);
    }

    @Override
    protected void renderContent(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        final Resource resource = (Resource)req.getAttribute(getClass().getName() + "/resource");
        log.debug("Starting renderContent for resource {}", resource.getPath());

        final PrintWriter out = res.getWriter();
        out.print("<h1>Index of ");
        out.print(resource.getPath() + ((!"/".equals(resource.getPath()) && resource.isFolder()) ? "/" : ""));
        out.println("</h1><hr/>");

        // TODO: ask ArtifactProviders
        final Iterator<Resource> children = generatedPOMArtifactProvider.listChildren(resource.getPath());
        final String parent = PathUtil.getParent(resource.getPath());

        out.print("<ul>");
        if (parent != null || "/".equals(parent)) {
            out.print("<li><a href=\"../\">../</a></li>");
        }
        while (children.hasNext()) {
            final Resource child = children.next();
            final String externalPath = getExternalPath(req, child);
            final String suffix =  child.isFolder() ? "/" : ""; // only for aesthetic reasons
            out.print("<li><a href=\"" + externalPath + suffix +"\">");
            out.print(PathUtil.getName(externalPath) + suffix);
            out.println("</a></li>");
        }
        out.println("</ul>");
    }

    private String getExternalPath(HttpServletRequest req, Resource child) {
        final String context = (String)req.getAttribute(WebConsoleConstants.ATTR_APP_ROOT);
        return context + "/" + getLabel() + child.getPath();

    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse res) throws IOException, ServletException {
        try {
            final String pathInfo = req.getPathInfo();
            if (pathInfo.length() >= getLabel().length() + 1) {
                final String path = getPathWithoutLabel(pathInfo);
                log.debug("Path: [{}]", path);

                final Resource resource = generatedPOMArtifactProvider.getResource(path);

                if (resource == null) {
                    log.debug("Sending 404, resource {} is null", path);
                    res.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }


                final InputStream contents = resource.getContents();
                if (contents != null) {
                    log.debug("Sending contents of resource {}", resource.getPath());
                    res.setContentType(resource.getContentType());
                    res.setCharacterEncoding("UTF-8");
                    res.setContentLength(resource.getContentLength());
                    final ServletOutputStream os = res.getOutputStream();
                    final byte[] buf = new byte[10240];
                    int read;
                    while ((read = contents.read(buf)) > -1) {
                        os.write(buf, 0, read);
                    }
                    contents.close();
                    os.close();
                } else {
                    log.debug("Delegating to super for resource {}", resource.getPath());
                    req.setAttribute(getClass().getName() + "/resource", resource);
                    super.doGet(req, res);
                    req.removeAttribute(getClass().getName() + "/resource");
                    log.debug("Returned from super for resource {}", resource.getPath());
                }

            }
        } catch (Throwable t) {
            log.error("Unexpected Throwable in doGet:", t);
        }
    }

    protected URL getResource(String path) {
        final String pathWithoutLabel = getPathWithoutLabel(path);
        if (generatedPOMArtifactProvider.getResource(pathWithoutLabel) == null) {
            log.debug("Delegating getResource to super for {}", pathWithoutLabel);
            return super.getResource(path);
        }
        return null;
    }

    private String getPathWithoutLabel(String path) {
        return PathUtil.normalize(path.replaceFirst("/" + getLabel(), ""));
    }

    @Override
    public void activate(BundleContext bundleContext) {
        super.activate(bundleContext);
        dependencyInspector = new DependencyInspector(getBundleContext());
        generatedPOMArtifactProvider = new GeneratedPOMArtifactProvider(dependencyInspector);
    }
}
