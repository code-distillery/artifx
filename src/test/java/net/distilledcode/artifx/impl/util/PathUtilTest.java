package net.distilledcode.artifx.impl.util;

import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class PathUtilTest {
    final static Map<String, String> NORMALIZE = new LinkedHashMap<String, String>() {{
        put("/", "/");
        put("", "/");
        put(".", "/");
        put("./", "/");
        put("/.", "/");
        put("./.", "/");

        put("/a", "/a");
        put("a", "/a");
        put("a.", "/a.");
        put(".a", "/.a");
        put("a/", "/a");

        put("/one", "/one");
        put("one", "/one");
        put("one/", "/one");
        put("/one/..", "/");
        put("one/..", "/");

        put("/one/two/three", "/one/two/three");
        put("/one/two/three/", "/one/two/three");
        put("one/two/three", "/one/two/three");
        put("one/two/three/", "/one/two/three");
        put("one/./two/three/", "/one/two/three");
        put("./one/./two/three/", "/one/two/three");

        put("/one/../two/three", "/two/three");
        put("/one/../two/three/", "/two/three");
        put("one/../two/three", "/two/three");
        put("one/../two/three/", "/two/three");

        put("/one/two//three", "/one/two/three");
        put("/one/two///three", "/one/two/three");
        put("/one/two////three", "/one/two/three");
        put("///one/two////three", "/one/two/three");

        put("//./one/two//..//./../three", "/three");
    }};

    
    
    @Test
    public void isAncestor() {
        assertTrue(PathUtil.isAncestor("/one/two", "/one"));
        assertTrue(PathUtil.isAncestor("/one", "/"));
        assertTrue(PathUtil.isAncestor("/one/two/three", "/one"));
        assertTrue(PathUtil.isAncestor("/one/two/three//../four/./", "/one"));

        assertFalse(PathUtil.isAncestor("/one", "/one"));
        assertFalse(PathUtil.isAncestor("/", "/"));
        assertFalse(PathUtil.isAncestor("", "/"));
        assertFalse(PathUtil.isAncestor("/", ""));
        assertFalse(PathUtil.isAncestor("/", "/one"));

        assertTrue(PathUtil.isAncestor("/one/../two", "/one/.."));
        assertTrue(PathUtil.isAncestor("/one/../two", "/"));
    }

    @Test
    public void isParent() {
        assertTrue(PathUtil.isParent("/one/two", "/one"));
        assertTrue(PathUtil.isParent("/one", "/"));

        assertFalse(PathUtil.isParent("/one/two/three", "/one"));
        assertFalse(PathUtil.isParent("/one", "/one"));
        assertFalse(PathUtil.isParent("/", "/"));
        assertFalse(PathUtil.isParent("/", "/one"));

        assertTrue(PathUtil.isParent("/one/../two", "/one/.."));
        assertTrue(PathUtil.isParent("/one/../two", "/"));
    }

    @Test
    public void getParent() {
        assertNull(PathUtil.getParent("/"));
        assertNull(PathUtil.getParent(""));
        assertEquals("/one", PathUtil.getParent("/one/two"));
        assertEquals("/", PathUtil.getParent("/one"));
        assertEquals("/", PathUtil.getParent("/one/.././/two/"));
    }

    @Test
    public void getName() {
        assertEquals("", PathUtil.getName("/"));
        assertEquals("", PathUtil.getName(""));
        assertEquals("two", PathUtil.getName("/one/two"));
        assertEquals("two", PathUtil.getName("/one/.././/two/"));
    }

    @Test
    public void normalize() {
        for (final Map.Entry<String, String> pair : NORMALIZE.entrySet()) {
            assertEquals("Path", pair.getValue(), PathUtil.normalize(pair.getKey()));
        }
    }

    @Test
    public void normalizeOptimization() {
        final String[] paths = {"/", "/one", "/one/two/three"};
        for (final String path : paths) {
            assertTrue("Path [" + path + "] should be the same object after normalization", path == PathUtil.normalize(path));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void normalizeInvalidAbsolutePath() {
        PathUtil.normalize("/one/../../four");
    }

    @Test(expected = IllegalArgumentException.class)
    public void normalizeInvalidRelativePath() {
        PathUtil.normalize("../two/three");
    }
}
