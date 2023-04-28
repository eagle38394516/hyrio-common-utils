package xyz.hyrio.common.tool;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.CRC32;

/**
 * 散列值计算器，可用于计算字节数组（或字符串）的散列值（MD5、SHA-1、SHA-256、CRC32）。
 *
 * @author Hyrio 2013-01-03
 */
public class Digester {
    /**
     * 十六进制数值的字符数组。
     */
    private static final String HEX_CHARS_UPPER_CASE_STR = "0123456789ABCDEF";

    private static final char[] HEX_CHARS_UPPER_CASE = HEX_CHARS_UPPER_CASE_STR.toCharArray();
    private static final char[] HEX_CHARS_LOWER_CASE = HEX_CHARS_UPPER_CASE_STR.toLowerCase().toCharArray();

    /**
     * 将字节数组转换为十六进制字符串。
     *
     * @param bytes 要转换的字节数组。
     * @return 转换后的十六进制字符串。
     */
    private static String encodeHex(byte[] bytes, boolean lowerCase) {
        char[] HEX_CHARS = lowerCase ? HEX_CHARS_LOWER_CASE : HEX_CHARS_UPPER_CASE;
        StringBuilder ret = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            ret.append(HEX_CHARS[(b >>> 4) & 0x0F]).append(HEX_CHARS[b & 0x0F]);
        }
        return ret.toString();
    }

    public enum Algorithm {
        MD5, SHA1, SHA256, CRC32
    }

    private final MessageDigest md5;
    private final MessageDigest sha1;
    private final MessageDigest sha256;
    private final CRC32 crc32;
    private final Algorithm[] algorithms;
    private boolean closed = false;

    private boolean isEnabled(Algorithm algorithm) {
        for (Algorithm a : algorithms) {
            if (a == algorithm) {
                return true;
            }
        }
        return false;
    }

    public Digester(Algorithm... algorithms) {
        this.algorithms = algorithms;
        try {
            md5 = isEnabled(Algorithm.MD5) ? MessageDigest.getInstance("MD5") : null;
            sha1 = isEnabled(Algorithm.SHA1) ? MessageDigest.getInstance("SHA-1") : null;
            sha256 = isEnabled(Algorithm.SHA256) ? MessageDigest.getInstance("SHA-256") : null;
            crc32 = isEnabled(Algorithm.CRC32) ? new CRC32() : null;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("error initializing digester", e);
        }
    }

    public Digester() {
        this(Algorithm.MD5, Algorithm.SHA1, Algorithm.SHA256, Algorithm.CRC32);
    }

    private void close() {
        closed = true;
    }

    public Digester update(byte[] bytes, int offset, int len) {
        if (closed) {
            throw new IllegalStateException("digester is closed");
        }
        if (md5 != null) {
            md5.update(bytes, offset, len);
        }
        if (sha1 != null) {
            sha1.update(bytes, offset, len);
        }
        if (sha256 != null) {
            sha256.update(bytes, offset, len);
        }
        if (crc32 != null) {
            crc32.update(bytes, offset, len);
        }
        return this;
    }

    public Digester update(byte[] bytes) {
        return update(bytes, 0, bytes.length);
    }

    public Digester update(String str) {
        return update(str.getBytes());
    }

    public String getMD5() {
        close();
        return encodeHex(md5.digest(), true);
    }

    public String getSHA1() {
        close();
        return encodeHex(sha1.digest(), true);
    }

    public String getSHA256() {
        close();
        return encodeHex(sha256.digest(), true);
    }

    public String getCRC32() {
        close();
        long digest = crc32.getValue();
        byte[] bytes = new byte[Integer.BYTES];
        for (int i = 0; i < Integer.BYTES; i++) {
            bytes[i] = (byte) (digest >>> ((Integer.BYTES - 1 - i) * Byte.SIZE));
        }
        return encodeHex(bytes, false);
    }

    public void reset() {
        if (md5 != null) {
            md5.reset();
        }
        if (sha1 != null) {
            sha1.reset();
        }
        if (sha256 != null) {
            sha256.reset();
        }
        if (crc32 != null) {
            crc32.reset();
        }
        closed = false;
    }

    public static String getMD5(String str) {
        return new Digester(Algorithm.MD5).update(str).getMD5();
    }

    public static String getSHA1(String str) {
        return new Digester(Algorithm.SHA1).update(str).getSHA1();
    }

    public static String getSHA256(String str) {
        return new Digester(Algorithm.SHA256).update(str).getSHA256();
    }

    public static String getCRC32(String str) {
        return new Digester(Algorithm.CRC32).update(str).getCRC32();
    }

    public static String getMD5(byte[] bytes) {
        return new Digester(Algorithm.MD5).update(bytes).getMD5();
    }

    public static String getSHA1(byte[] bytes) {
        return new Digester(Algorithm.SHA1).update(bytes).getSHA1();
    }

    public static String getSHA256(byte[] bytes) {
        return new Digester(Algorithm.SHA256).update(bytes).getSHA256();
    }

    public static String getCRC32(byte[] bytes) {
        return new Digester(Algorithm.CRC32).update(bytes).getCRC32();
    }
}
