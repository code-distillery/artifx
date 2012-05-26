package net.distilledcode.artifx.impl.util;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DigestUtil {

    private static final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();
    private static final int DIGEST_BUFFER_SIZE = 10240;


    public static String md5(final InputStream is) {
        return hexDigest("MD5", is);
    }

    public static String sha1(final InputStream is) {
        return hexDigest("SHA", is);
    }

    public static String hexDigest(final String algorithm, final InputStream is) {
        return toHex(digest(getDigest(algorithm), is));
    }

    private static MessageDigest getDigest(final String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] digest(final MessageDigest digest, final InputStream is) {
        try {
            final byte[] buf = new byte[DIGEST_BUFFER_SIZE];
            int read;
            while ((read = is.read(buf, 0, DIGEST_BUFFER_SIZE)) > -1) {
                digest.update(buf, 0, read);
            }
            return digest.digest();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String toHex(final byte[] data) {
        int len = data.length;
        char[] hex = new char[len << 1];
        // two characters form the hex value.
        for (int i = 0, j = 0; i < len; i++) {
            hex[j++] = HEX_DIGITS[(0xF0 & data[i]) >>> 4];
            hex[j++] = HEX_DIGITS[0x0F & data[i]];
        }
        return new String(hex);
    }
}
