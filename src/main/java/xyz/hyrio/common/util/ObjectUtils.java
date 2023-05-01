package xyz.hyrio.common.util;

import xyz.hyrio.common.exception.internal.DatabaseException;
import xyz.hyrio.common.exception.request.InvalidParameterException;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import static org.springframework.util.ObjectUtils.isEmpty;
import static org.springframework.util.StringUtils.*;

public final class ObjectUtils {
    private ObjectUtils() {
    }

    public static final String WHITESPACE_REGEX = "\\s+";
    public static final String UUID_REGEX = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";

    public static String requireHasText(String s, String message) {
        if (!hasText(s)) {
            throw new InvalidParameterException(message);
        }
        return s;
    }

    public static void checkDmlResult(int result, int expected, String message) {
        if (result != expected) {
            throw new DatabaseException(message);
        }
    }

    public static void checkDmlResult(int result, String message) {
        checkDmlResult(result, 1, message);
    }

    public static boolean isTrue(Boolean b) {
        return b != null && b;
    }

    public static String blank2NullOtherwiseStrip(String str) {
        return hasText(str) ? str.strip() : null;
    }

    public static String requireHasTextElse(String str, String other) {
        return hasText(str) ? str : other;
    }

    public static <K, V> Map<K, V> mapOf(Object... kvs) {
        if (isEmpty(kvs)) {
            return new HashMap<>();
        }
        if (kvs.length % 2 != 0) {
            throw new IllegalArgumentException("kvs.length must be even");
        }
        Map<K, V> map = new HashMap<>();
        for (int i = 0; i < kvs.length; i += 2) {
            @SuppressWarnings("unchecked")
            K k = (K) kvs[i];
            @SuppressWarnings("unchecked")
            V v = (V) kvs[i + 1];
            map.put(k, v);
        }
        return map;
    }

    public static String integerToString(Integer i) {
        return i == null ? null : i.toString();
    }

    private static boolean isNeedPadding(String str, int length) {
        return str != null && str.length() < length;
    }

    public static String padStart(String str, int minLength, char padChar) {
        return isNeedPadding(str, minLength) ? String.valueOf(padChar).repeat(minLength - str.length()) + str : str;
    }

    public static String padEnd(String str, int minLength, char padChar) {
        return isNeedPadding(str, minLength) ? str + String.valueOf(padChar).repeat(minLength - str.length()) : str;
    }

    private static final String PASSWORD_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final Random r = new Random();

    public static String randomPassword(int length) {
        StringBuilder ret = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            ret.append(PASSWORD_CHARS.charAt(r.nextInt(PASSWORD_CHARS.length())));
        }
        return ret.toString();
    }

    public static String randomUuid() {
        return UUID.randomUUID().toString();
    }

    public static String getFilenameInFolder(String currentFilename, Collection<String> filenames) {
        if (!filenames.contains(currentFilename)) {
            return currentFilename;
        }
        String prefix = stripFilenameExtension(currentFilename);
        String extension = getFilenameExtension(currentFilename);
        String suffix = hasText(extension) ? "." + extension : "";
        for (int i = 1; i < Integer.MAX_VALUE; i++) {
            String newFilename = prefix + " (" + i + ")" + suffix;
            if (!filenames.contains(newFilename)) {
                return newFilename;
            }
        }
        throw new IllegalStateException("cannot find a new filename for " + currentFilename);
    }

    public static LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,###");
    private static final long FILE_SIZE_THRESHOLD = 1024;

    public static String stringifyFileSize(long fileSize) {
        if (fileSize < FILE_SIZE_THRESHOLD) {
            return DECIMAL_FORMAT.format(fileSize) + " Bytes";
        }
        int exp = Math.min((int) (Math.log(fileSize) / Math.log(FILE_SIZE_THRESHOLD)), 5);
        char pre = "kMGTPE".charAt(exp - 1);
        return String.format("%.2f %sB (%s Bytes)", fileSize / Math.pow(FILE_SIZE_THRESHOLD, exp), pre, DECIMAL_FORMAT.format(fileSize));
    }
}
