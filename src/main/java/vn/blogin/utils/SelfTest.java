package vn.blogin.utils;
import java.nio.charset.StandardCharsets;
public final class SelfTest {
    private static int passed = 0;
    private static int failed = 0;
    private SelfTest() { }
    public static void main(String[] args) throws Exception {
        testBase32();
        testTotp();
        testPasswordStrength();
        testEncryption();
        System.out.println();
        System.out.println("========================================");
        System.out.printf("Kết quả: %d thành công, %d thất bại%n", passed, failed);
        System.out.println("========================================");
        if (failed > 0) System.exit(1);
    }
    private static void testBase32() {
        TOTPGenerator gen = new TOTPGenerator();
        check("Base32 empty", "", gen.base32Encode(new byte[0]));
        check("Base32 f", "MY", gen.base32Encode("f".getBytes(StandardCharsets.UTF_8)));
        check("Base32 fo", "MZXQ", gen.base32Encode("fo".getBytes(StandardCharsets.UTF_8)));
        check("Base32 foo", "MZXW6", gen.base32Encode("foo".getBytes(StandardCharsets.UTF_8)));
        check("Base32 foob", "MZXW6YQ", gen.base32Encode("foob".getBytes(StandardCharsets.UTF_8)));
        check("Base32 fooba", "MZXW6YTB", gen.base32Encode("fooba".getBytes(StandardCharsets.UTF_8)));
        check("Base32 foobar", "MZXW6YTBOI", gen.base32Encode("foobar".getBytes(StandardCharsets.UTF_8)));
        byte[] original = new byte[20];
        for (int i = 0; i < original.length; i++) original[i] = (byte) (i * 7);
        String encoded = gen.base32Encode(original);
        byte[] decoded = gen.base32Decode(encoded);
        check("Base32 round-trip length", original.length, decoded.length);
        boolean same = true;
        for (int i = 0; i < original.length; i++) {
            if (original[i] != decoded[i]) same = false;
        }
        check("Base32 round-trip content", true, same);
    }
    private static void testTotp() {
        TOTPGenerator gen = new TOTPGenerator();
        String secret = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ";
        check("TOTP 59s (8 digits)", "94287082", gen.generateCode(secret, 8, 30, 59L));
        check("TOTP 1111111109s", "07081804", gen.generateCode(secret, 8, 30, 1111111109L));
        check("TOTP 1111111111s", "14050471", gen.generateCode(secret, 8, 30, 1111111111L));
        check("TOTP 1234567890s", "89005924", gen.generateCode(secret, 8, 30, 1234567890L));
        check("TOTP 2000000000s", "69279037", gen.generateCode(secret, 8, 30, 2000000000L));
        String current = gen.generateCode(secret, 6, 30);
        check("TOTP verify current", true, gen.verifyCode(secret, current, 6, 30, 1));
        check("TOTP verify sai", false, gen.verifyCode(secret, "000000", 6, 30, 0));
        check("TOTP verify sai format", false, gen.verifyCode(secret, "abc", 6, 30, 1));
    }
    private static void testPasswordStrength() {
        check("Strength null", 0, PasswordStrength.score(null));
        check("Strength rỗng", 0, PasswordStrength.score(""));
        check("Strength ngắn", true, PasswordStrength.score("a") < 3);
        check("Strength mạnh", true, PasswordStrength.score("Xk9#mQ2!vLp") >= 4);
        check("Strength phổ biến bị trừ", true, PasswordStrength.score("password123") < PasswordStrength.score("Xk9#mQ2!vLp"));
        check("Strength lặp bị trừ", true, PasswordStrength.score("aaaaaaaaaa") < PasswordStrength.score("Xk9#mQ2!vLp"));
        check("Common detect", true, PasswordStrength.isCommon("admin123"));
        check("Common detect false", false, PasswordStrength.isCommon("Xk9#mQ2!vLp"));
    }
    private static void testEncryption() throws Exception {
        java.nio.file.Path keyFile = java.nio.file.Files.createTempFile("blogin-test", ".key");
        try {
            EncryptionUtil enc = new EncryptionUtil(keyFile);
            String plain = "JBSWY3DPEHPK3PXPJBSWY3DPEHPK3PXP";
            String cipher = enc.encrypt(plain);
            check("Encrypt có tiền tố ENC:", true, cipher != null && cipher.startsWith("ENC:"));
            check("Encrypt khác plaintext", true, !plain.equals(cipher));
            check("Decrypt khớp", plain, enc.decrypt(cipher));
            check("Decrypt plaintext không mã hoá", plain, enc.decrypt(plain));
            check("IsEncrypted true", true, enc.isEncrypted(cipher));
            check("IsEncrypted false", false, enc.isEncrypted(plain));
            String cipher2 = enc.encrypt(plain);
            check("Encrypt ngẫu nhiên IV", true, !cipher.equals(cipher2));
            check("Decrypt vẫn khớp", plain, enc.decrypt(cipher2));
            EncryptionUtil enc2 = new EncryptionUtil(keyFile);
            check("Khôi phục từ file khoá", plain, enc2.decrypt(cipher));
            java.nio.file.Path emptyKey = java.nio.file.Files.createTempFile("blogin-test-empty", ".key");
            try {
                java.nio.file.Files.writeString(emptyKey, "");
                EncryptionUtil enc3 = new EncryptionUtil(emptyKey);
                check("Key file rỗng -> tự sinh khoá", true, enc3.encrypt(plain) != null);
            } catch (Exception e) {
                check("Key file rỗng -> tự sinh khoá", false, e);
            } finally {
                java.nio.file.Files.deleteIfExists(emptyKey);
            }
        } finally {
            java.nio.file.Files.deleteIfExists(keyFile);
        }
    }
    private static void check(String name, Object expected, Object actual) {
        boolean ok = expected == null ? actual == null : expected.equals(actual);
        if (ok) {
            passed++;
            System.out.println("  ✔ " + name);
        } else {
            failed++;
            System.out.println("  ✘ " + name + " | expected=" + expected + " actual=" + actual);
        }
    }
}
