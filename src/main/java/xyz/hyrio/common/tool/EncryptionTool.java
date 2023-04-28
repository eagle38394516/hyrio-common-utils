package xyz.hyrio.common.tool;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;

public class EncryptionTool {
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private static final String CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";

    private static final Base64.Encoder base64Encoder = Base64.getEncoder();
    private static final Base64.Encoder base64UrlEncoder = Base64.getUrlEncoder();
    private static final Base64.Decoder base64Decoder = Base64.getDecoder();
    private static final Base64.Decoder base64UrlDecoder = Base64.getUrlDecoder();

    private final byte[] keyBytes;
    private final Object encryptionLock = new Object(), decryptionLock = new Object();
    private final Cipher encryptCipher, decryptCipher;

    public byte[] getKeyBytes() {
        return keyBytes.clone();
    }

    public EncryptionTool(String keyString) {
        this.keyBytes = base64Decoder.decode(keyString);
        try {
            // Required 128, 192, 256-bit key (16, 24, 32 bytes)
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
            encryptCipher = Cipher.getInstance(CIPHER_ALGORITHM);
            encryptCipher.init(Cipher.ENCRYPT_MODE, keySpec);
            decryptCipher = Cipher.getInstance(CIPHER_ALGORITHM);
            decryptCipher.init(Cipher.DECRYPT_MODE, keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException e) {
            throw new RuntimeException("encryption algorithm not supported", e);
        }
    }

    // Tools

    public static byte[] toBytes(String string) {
        return string.getBytes(DEFAULT_CHARSET);
    }

    private static String generateRandomEncryptionKey(int bytes) {
        if (!List.of(16, 24, 32).contains(bytes)) {
            throw new IllegalArgumentException("bytes must be 16, 24, or 32");
        }
        byte[] keyBytes = new byte[bytes];
        java.util.Random r = new java.util.Random();
        r.nextBytes(keyBytes);
        return Base64.getEncoder().encodeToString(keyBytes);
    }

    private static boolean isUrlEncoded(String str) {
        return str.contains("_") || str.contains("-");
    }

    // Encrypt

    private String encodeToString(Base64.Encoder encoder, byte[] encryptedBytes) {
        return encoder.encodeToString(encryptedBytes);
    }

    public byte[] encryptToBytes(byte[] bytes) {
        synchronized (encryptionLock) {
            try {
                return encryptCipher.doFinal(bytes);
            } catch (IllegalBlockSizeException | BadPaddingException e) {
                throw new IllegalArgumentException("failed to encrypt bytes", e);
            }
        }
    }

    public byte[] encryptToBytes(String str) {
        return encryptToBytes(toBytes(str));
    }

    public String encryptToString(byte[] bytes) {
        return encodeToString(base64Encoder, encryptToBytes(bytes));
    }

    public String encryptToString(String content) {
        return encodeToString(base64Encoder, encryptToBytes(content));
    }

    public String encryptToUrlString(byte[] bytes) {
        return encodeToString(base64UrlEncoder, encryptToBytes(bytes));
    }

    public String encryptToUrlString(String content) {
        return encodeToString(base64UrlEncoder, encryptToBytes(content));
    }

    // Decrypt

    public byte[] decryptToBytes(byte[] bytes) {
        synchronized (decryptionLock) {
            try {
                return decryptCipher.doFinal(bytes);
            } catch (IllegalBlockSizeException | BadPaddingException e) {
                throw new IllegalArgumentException("failed to decrypt bytes", e);
            }
        }
    }

    public byte[] decryptToBytes(String str) {
        return decryptToBytes((isUrlEncoded(str) ? base64UrlDecoder : base64Decoder).decode(str));
    }

    public String decryptToString(byte[] bytes) {
        return new String(decryptToBytes(bytes), DEFAULT_CHARSET);
    }

    public String decryptToString(String content) {
        return new String(decryptToBytes(content), DEFAULT_CHARSET);
    }

    // Test

    public void consoleTest(BufferedReader bufferedReader) throws IOException {
        System.out.print("Please select mode, encrypt(e) or decrypt(others): ");
        boolean encryption = "e".equalsIgnoreCase(bufferedReader.readLine());
        System.out.println((encryption ? "Encrypt" : "Decrypt") + " mode selected. (Enter empty line to exit.)");
        System.out.println();

        while (true) {
            System.out.print("Please enter the string to be " + (encryption ? "encrypted" : "decrypted") + ": ");
            String line = bufferedReader.readLine();
            if (line.isBlank()) {
                break;
            }
            try {
                System.out.printf("    ==> %s -> %s%n", line, encryption ? encryptToString(line) : decryptToString(line));
            } catch (Exception e) {
                System.out.println("    <[!]> Error: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) throws IOException {
        String key;
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in))) {
            System.out.print("Encryption key: ");
            key = bufferedReader.readLine();
            EncryptionTool tool = new EncryptionTool(key);
            System.out.println();
            tool.consoleTest(bufferedReader);
        }
    }
}
