package vn.blogin.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.UndeclaredThrowableException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * TOTP Generator triển khai theo RFC 6238 (Time-Based One-Time Password).
 *
 * <p>Tương thích với Google Authenticator, Microsoft Authenticator, Authy...</p>
 *
 * <p>Thuật toán HMAC-SHA1, mặc định 6 chữ số, thời gian hiệu lực 30 giây.</p>
 *
 * @author BloginVN
 */
public class TOTPGenerator {

    private static final String ALGORITHM = "HmacSHA1";
    private static final int SECRET_SIZE_BYTES = 20; // 160-bit
    private final SecureRandom random = new SecureRandom();

    /**
     * Tạo secret key ngẫu nhiên (Base32).
     *
     * @return chuỗi Base32 secret (khoảng 32 ký tự)
     */
    public String generateSecret() {
        byte[] bytes = new byte[SECRET_SIZE_BYTES];
        random.nextBytes(bytes);
        return base32Encode(bytes);
    }

    /**
     * Tạo mã OTP tại thời điểm hiện tại.
     *
     * @param secret     secret Base32 của người dùng
     * @param digits     số chữ số (6 hoặc 8)
     * @param stepSec    thời gian hiệu lực mỗi mã (giây, mặc định 30)
     * @return mã OTP hiện tại
     */
    public String generateCode(String secret, int digits, int stepSec) {
        return generateCode(secret, digits, stepSec, System.currentTimeMillis() / 1000L);
    }

    /**
     * Tạo mã OTP tại một thời điểm bất kỳ (dùng cho test).
     *
     * @param secret   secret Base32
     * @param digits   số chữ số
     * @param stepSec  thời gian hiệu lực
     * @param unixSec  thời điểm (epoch seconds)
     * @return mã OTP
     */
    public String generateCode(String secret, int digits, int stepSec, long unixSec) {
        try {
            byte[] keyBytes = base32Decode(secret);
            long counter = unixSec / stepSec;

            // Encode counter thành 8 bytes big-endian
            byte[] data = new byte[8];
            for (int i = 7; i >= 0; i--) {
                data[i] = (byte) (counter & 0xff);
                counter >>= 8;
            }

            // Tính HMAC-SHA1
            SecretKeySpec signKey = new SecretKeySpec(keyBytes, ALGORITHM);
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(signKey);
            byte[] hash = mac.doFinal(data);

            // Dynamic truncation theo RFC 4226
            int offset = hash[hash.length - 1] & 0x0f;
            long truncatedHash = 0;
            for (int i = 0; i < 4; ++i) {
                truncatedHash <<= 8;
                truncatedHash |= (hash[offset + i] & 0xff);
            }
            truncatedHash &= 0x7fffffff;
            truncatedHash %= (long) Math.pow(10, digits);

            // Format với số chữ số chính xác
            String format = "%0" + digits + "d";
            return String.format(format, truncatedHash);
        } catch (GeneralSecurityException e) {
            throw new UndeclaredThrowableException(e);
        }
    }

    /**
     * Xác thực mã OTP người dùng nhập có hợp lệ hay không.
     * Cho phép sai lệch ± window steps (mặc định 1 step = 30s trước/sau).
     *
     * @param secret  secret Base32 của người dùng
     * @param code    mã người dùng nhập
     * @param digits  số chữ số
     * @param stepSec thời gian hiệu lực
     * @param window  độ lệch cho phép (số step)
     * @return true nếu mã hợp lệ
     */
    public boolean verifyCode(String secret, String code, int digits, int stepSec, int window) {
        if (code == null || !code.matches("\\d{" + digits + "}")) {
            return false;
        }
        long currentSec = System.currentTimeMillis() / 1000L;
        for (int i = -window; i <= window; i++) {
            String validCode = generateCode(secret, digits, stepSec, currentSec + (long) i * stepSec);
            if (constantTimeEquals(code, validCode)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tạo URL otpauth:// để hiển thị QR code (Google Authenticator format).
     *
     * @param secret    secret Base32
     * @param account   tên tài khoản (thường là email hoặc tên người chơi)
     * @param issuer    tên ứng dụng
     * @return URL otpauth://
     */
    public String getOtpAuthUrl(String secret, String account, String issuer) {
        return String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s&digits=6&period=30&algorithm=SHA1",
                urlEncode(issuer), urlEncode(account), secret, urlEncode(issuer)
        );
    }

    /**
     * So sánh 2 chuỗi với thời gian cố định (chống timing attack).
     */
    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) return false;
        int diff = 0;
        for (int i = 0; i < a.length(); i++) {
            diff |= a.charAt(i) ^ b.charAt(i);
        }
        return diff == 0;
    }

    /**
     * URL-encode đơn giản cho otpauth URL.
     */
    private String urlEncode(String s) {
        return s.replace(":", "%3A").replace("@", "%40").replace(" ", "%20");
    }

    // ============== Base32 (RFC 4648) ==============

    private static final String BASE32_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final int[] BASE32_LOOKUP = new int[128];

    static {
        Arrays.fill(BASE32_LOOKUP, -1);
        for (int i = 0; i < BASE32_CHARS.length(); i++) {
            BASE32_LOOKUP[BASE32_CHARS.charAt(i)] = i;
            BASE32_LOOKUP[Character.toLowerCase(BASE32_CHARS.charAt(i))] = i;
        }
    }

    /**
     * Encode bytes thành Base32 (không padding).
     */
    public String base32Encode(byte[] data) {
        if (data.length == 0) return "";
        StringBuilder out = new StringBuilder();
        int buffer = 0, bitsLeft = 0;
        for (byte b : data) {
            buffer <<= 8;
            buffer |= b & 0xff;
            bitsLeft += 8;
            while (bitsLeft >= 5) {
                int idx = (buffer >> (bitsLeft - 5)) & 0x1f;
                out.append(BASE32_CHARS.charAt(idx));
                bitsLeft -= 5;
            }
        }
        if (bitsLeft > 0) {
            int idx = (buffer << (5 - bitsLeft)) & 0x1f;
            out.append(BASE32_CHARS.charAt(idx));
        }
        return out.toString();
    }

    /**
     * Decode Base32 thành bytes.
     */
    public byte[] base32Decode(String base32) {
        base32 = base32.toUpperCase().replace("=", "");
        byte[] out = new byte[base32.length() * 5 / 8];
        int buffer = 0, bitsLeft = 0, idx = 0;
        for (char c : base32.toCharArray()) {
            if (c >= BASE32_LOOKUP.length || BASE32_LOOKUP[c] == -1) {
                throw new IllegalArgumentException("Invalid Base32 character: " + c);
            }
            buffer <<= 5;
            buffer |= BASE32_LOOKUP[c];
            bitsLeft += 5;
            if (bitsLeft >= 8) {
                out[idx++] = (byte) ((buffer >> (bitsLeft - 8)) & 0xff);
                bitsLeft -= 8;
            }
        }
        return out;
    }

    /**
     * Helper: encode secret thành Base64 (cho QR code, dễ đọc).
     */
    public String toBase64(String base32Secret) {
        return Base64.getEncoder().encodeToString(base32Decode(base32Secret));
    }

    /**
     * Helper: chuyển bytes thành hex.
     */
    public String toHex(byte[] bytes) {
        return String.format("%040x", new BigInteger(1, bytes));
    }
}
