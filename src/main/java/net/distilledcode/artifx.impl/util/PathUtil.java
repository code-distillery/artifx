package net.distilledcode.artifx.impl.util;

/**
 * Utility class to perform frequent path manipulations.
 */
public class PathUtil {

    /**
     *
     * @param path
     * @param parent
     * @return true if the parameter parent is a direct parent of path.
     */
    public static boolean isParent(final String path, final String parent) {
        final String np = normalize(path);
        final String pp = normalize(parent);
        return pp != null && pp.equals(getParent(np));
    }

    /**
     *
     * @param path
     * @param ancestor
     * @return true if the parameter ancestor is an ancestor of path.
     */
    public static boolean isAncestor(final String path, final String ancestor) {
        final String np = normalize(path);
        final String ap = normalize(ancestor);
        return np.length() > ap.length() && ("/".equals(ap) || np.startsWith(ap) && np.charAt(ap.length()) == '/');
    }
    /**
     *
     * @param path
     * @return The name or empty string for root.
     */
    public static String getName(final String path) {
        final String np = normalize(path);
        final int i = np.lastIndexOf('/');
        if (i > -1) return np.substring(i + 1);
        return "";
    }

    /**
     *
     * @param path
     * @return The parent path or null if there is no parent.
     */
    public static String getParent(final String path) {
        final String np = normalize(path);
        final int i = np.lastIndexOf('/');
        if (i == 0 && np.length() > 1) return np.substring(0, 1);
        if (i > 0) return np.substring(0, i);
        return null;
    }

    public static boolean isNormalized(final String path) {
        final int len = path.length();
        return !((len == 0 || path.charAt(0) != '/')        // no leading slash
                || (len > 1 && path.charAt(len - 1) == '/') // trailing slash
                || path.contains("//")                      // duplicate slashes
                || path.charAt(0) == '.'                    // relative paths
                || path.contains("/."));                    // path navigation

    }

    public static String normalize(final String path) {
        if (!isNormalized(path)) {
            final String[] segments = path.split("/");
            final String[] normalized = new String[segments.length]; // this has no leading slash (empty first element

            int i = "".equals(segments[0]) ? 1 : 0; // ignore leading slash if present
            int j = 0;
            for (; i < segments.length; i++, j++) {
                if ("..".equals(segments[i])) {
                    if ((j -= 2) < -1) { // may be -1 because 1 is added in the next iteration
                        throw new IllegalArgumentException("path may not be relative beyond its root: " + path);
                    }
                } else if ("".equals(segments[i]) || ".".equals(segments[i])) {
                    j--; // skip empty segments
                } else {
                    normalized[j] = segments[i];
                }
            }

            final StringBuilder normalizedPath = new StringBuilder("/"); // add leading slash
            for (int k = 0; k < j; k++) {
                normalizedPath.append(normalized[k]);
                if (k + 1 < j) normalizedPath.append("/");
            }
            return normalizedPath.toString();
        }
        return path;
    }
}
