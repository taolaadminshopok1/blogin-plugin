package vn.blogin.utils;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Base64;
public class EncryptionUtil {
    private static final String PREFIX = "ENC:";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH = 128;
    private final SecretKeySpec key;
    private final SecureRandom random = new SecureRandom();
    public EncryptionUtil(Path keyFile) throws IOException {
        byte[] keyBytes;
        if (Files.exists(keyFile)) {
            String raw = Files.readString(keyFile, StandardCharsets.UTF_8).trim();
            if (raw.isEmpty()) {
                keyBytes = generateAndPersistKey(keyFile);
            } else {
                try {
                    keyBytes = Base64.getDecoder().decode(raw);
                } catch (IllegalArgumentException e) {
                    throw new IOException("File khoá mã hoá hỏng: " + keyFile);
                }
                if (keyBytes.length != 32) {
                    throw new IOException("File khoá mã hoá không hợp lệ (cần 32 byte): " + keyFile);
                }
            }
        } else {
            keyBytes = generateAndPersistKey(keyFile);
        }
        this.key = new SecretKeySpec(keyBytes, "AES");
    }
    private byte[] generateAndPersistKey(Path keyFile) throws IOException {
        byte[] keyBytes = new byte[32];
        random.nextBytes(keyBytes);
        Path parent = keyFile.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(keyFile, Base64.getEncoder().encodeToString(keyBytes), StandardCharsets.UTF_8);
        try {
            Files.setPosixFilePermissions(keyFile,
                    java.nio.file.attribute.PosixFilePermissions.fromString("rw-------"));
        } catch (Exception ignored) {
        }
        return keyBytes;
    }
    public String encrypt(String plain) {
        if (plain == null) return null;
        try {
            byte[] iv = new byte[IV_LENGTH];
            random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH, iv));
            byte[] encrypted = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
            return PREFIX + Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            return null;
        }
    }
    public String decrypt(String encrypted) {
        if (encrypted == null) return null;
        if (!encrypted.startsWith(PREFIX)) return encrypted;
        try {
            byte[] combined = Base64.getDecoder().decode(encrypted.substring(PREFIX.length()));
            byte[] iv = new byte[IV_LENGTH];
            byte[] data = new byte[combined.length - IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH);
            System.arraycopy(combined, IV_LENGTH, data, 0, data.length);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH, iv));
            return new String(cipher.doFinal(data), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }
    public boolean isEncrypted(String value) {
        return value != null && value.startsWith(PREFIX);
    }
}
