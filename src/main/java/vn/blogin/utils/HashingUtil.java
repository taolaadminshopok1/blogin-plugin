package vn.blogin.utils;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility để hash và xác thực mật khẩu sử dụng BCrypt.
 *
 * <p>BCrypt tự động tạo salt ngẫu nhiên và nhúng vào trong hash, không cần quản lý salt riêng.</p>
 *
 * <p>Cost factor mặc định: 12 (cân bằng giữa bảo mật và tốc độ trên server Minecraft).</p>
 *
 * @author BloginVN
 */
public class HashingUtil {

    /**
     * Cost factor - càng cao càng an toàn nhưng càng chậm.
     * 12 = ~250ms/hash trên CPU phổ thông, đủ bảo mật cho game server.
     */
    private static final int COST_FACTOR = 12;

    /**
     * Hash một mật khẩu sử dụng BCrypt với salt ngẫu nhiên.
     *
     * @param plainPassword mật khẩu gốc (plaintext)
     * @return chuỗi hash có dạng {@code $2a$12$...}
     */
    public String hash(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Mật khẩu không được rỗng!");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(COST_FACTOR));
    }

    /**
     * Xác thực mật khẩu có khớp với hash đã lưu hay không.
     *
     * @param plainPassword mật khẩu người dùng nhập
     * @param hashedPassword hash đã lưu trong database
     * @return true nếu mật khẩu khớp
     */
    public boolean verify(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) return false;
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            // Hash không hợp lệ
            return false;
        }
    }
}
