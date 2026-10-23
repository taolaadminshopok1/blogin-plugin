package vn.blogin.managers;

import vn.blogin.Blogin;
import vn.blogin.database.AttemptDAO;
import vn.blogin.database.PlayerDAO;
import vn.blogin.database.PlayerDAO.PlayerData;
import vn.blogin.utils.TOTPGenerator;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Quản lý logic xác thực: đăng ký, đăng nhập, đăng xuất, đổi mật khẩu.
 *
 * <p>Sử dụng {@link PlayerDAO} và {@link AttemptDAO} để tương tác với database.</p>
 *
 * <p>Đây là tầng Business Logic - chứa tất cả quy tắc nghiệp vụ (validation, rate limiting...).</p>
 *
 * @author BloginVN
 */
public class AuthManager {

    private final Blogin plugin;
    private final PlayerDAO playerDAO;
    private final AttemptDAO attemptDAO;

    /**
     * Kết quả trả về cho các thao tác xác thực.
     */
    public enum AuthResult {
        SUCCESS,
        ALREADY_REGISTERED,
        NOT_REGISTERED,
        WRONG_PASSWORD,
        ACCOUNT_LOCKED,
        INVALID_PASSWORD_FORMAT,
        PASSWORD_MISMATCH,
        TOTP_REQUIRED,
        TOTP_INVALID,
        TOO_MANY_ATTEMPTS,
        PLAYER_NOT_FOUND,
        ALREADY_AUTHENTICATED
    }

    /**
     * Lưu trữ thời điểm khóa của từng identifier (account/ip).
     * Tránh truy vấn DB liên tục để check lock.
     */
    private final Map<String, Long> lockUntilMap = new HashMap<>();

    public AuthManager(Blogin plugin) {
        this.plugin = plugin;
        this.playerDAO = new PlayerDAO(plugin);
        this.attemptDAO = new AttemptDAO(plugin);
    }

    // ==================== ĐĂNG KÝ ====================

    /**
     * Đăng ký tài khoản mới.
     *
     * @param uuid     UUID người chơi
     * @param name     tên người chơi
     * @param password mật khẩu (đã verify định dạng)
     * @param ip       IP người chơi
     * @return kết quả đăng ký
     */
    public AuthResult register(UUID uuid, String name, String password, String ip) {
        // 1. Kiểm tra đã đăng ký chưa
        if (playerDAO.isRegistered(name)) {
            return AuthResult.ALREADY_REGISTERED;
        }

        // 2. Validate định dạng mật khẩu
        AuthResult validation = validatePasswordFormat(password);
        if (validation != AuthResult.SUCCESS) {
            return validation;
        }

        // 3. Hash và lưu
        String hash = plugin.getHashingUtil().hash(password);
        boolean success = playerDAO.createPlayer(uuid, name, hash, ip);
        if (!success) {
            return AuthResult.ALREADY_REGISTERED; // Race condition
        }

        if (plugin.getConfig().getBoolean("log-su-kien", true)) {
            plugin.getLog().info(String.format("[Đăng ký] %s (%s) từ IP %s", name, uuid, ip));
        }
        return AuthResult.SUCCESS;
    }

    // ==================== ĐĂNG NHẬP ====================

    /**
     * Xác thực đăng nhập.
     *
     * @param uuid     UUID người chơi
     * @param name     tên người chơi
     * @param password mật khẩu
     * @param totpCode mã 2FA (có thể null)
     * @param ip       IP người chơi
     * @return kết quả xác thực
     */
    public AuthResult login(UUID uuid, String name, String password, String totpCode, String ip) {
        // 1. Kiểm tra tài khoản có tồn tại không
        PlayerData data = playerDAO.getPlayerData(name);
        if (data == null) {
            return AuthResult.NOT_REGISTERED;
        }

        // 2. Kiểm tra khóa
        AuthResult lockCheck = checkLock(name, ip);
        if (lockCheck != AuthResult.SUCCESS) {
            return lockCheck;
        }

        // 3. Verify mật khẩu
        boolean passwordOk = plugin.getHashingUtil().verify(password, data.passwordHash());
        if (!passwordOk) {
            recordFailure(name, ip);
            return AuthResult.WRONG_PASSWORD;
        }

        // 4. Kiểm tra 2FA nếu được bật
        if (data.totpEnabled()) {
            // Cần mã 2FA
            if (totpCode == null || totpCode.isBlank()) {
                return AuthResult.TOTP_REQUIRED;
            }
            int digits = plugin.getConfig().getInt("two-factor.so-chu-so", 6);
            int period = plugin.getConfig().getInt("two-factor.thoi-gian-hieu-luc", 30);
            int window = plugin.getConfig().getInt("two-factor.do-lech-cho-phep", 1);

            boolean totpOk = plugin.getTotpGenerator().verifyCode(
                    data.totpSecret(), totpCode, digits, period, window);
            if (!totpOk) {
                recordFailure(name, ip);
                return AuthResult.TOTP_INVALID;
            }
        }

        // 5. Thành công - cập nhật thông tin
        playerDAO.updateLoginInfo(uuid, ip);
        clearFailure(name, ip);

        if (plugin.getConfig().getBoolean("log-su-kien", true)) {
            plugin.getLog().info(String.format("[Đăng nhập] %s (%s) từ IP %s", name, uuid, ip));
        }
        return AuthResult.SUCCESS;
    }

    // ==================== ĐỔI MẬT KHẨU ====================

    /**
     * Đổi mật khẩu cho người chơi đã đăng nhập.
     *
     * @param name        tên tài khoản
     * @param oldPassword mật khẩu cũ
     * @param newPassword mật khẩu mới
     * @return kết quả
     */
    public AuthResult changePassword(String name, String oldPassword, String newPassword) {
        PlayerData data = playerDAO.getPlayerData(name);
        if (data == null) return AuthResult.PLAYER_NOT_FOUND;

        // Xác thực mật khẩu cũ
        if (!plugin.getHashingUtil().verify(oldPassword, data.passwordHash())) {
            return AuthResult.WRONG_PASSWORD;
        }

        // Validate mật khẩu mới
        AuthResult validation = validatePasswordFormat(newPassword);
        if (validation != AuthResult.SUCCESS) return validation;

        // Mật khẩu mới phải khác mật khẩu cũ
        if (oldPassword.equals(newPassword)) {
            return AuthResult.INVALID_PASSWORD_FORMAT;
        }

        String newHash = plugin.getHashingUtil().hash(newPassword);
        boolean success = playerDAO.updatePassword(name, newHash);
        return success ? AuthResult.SUCCESS : AuthResult.PLAYER_NOT_FOUND;
    }

    /**
     * Reset mật khẩu bởi admin (force đăng nhập).
     */
    public boolean adminResetPassword(String name, String newPassword) {
        AuthResult validation = validatePasswordFormat(newPassword);
        if (validation != AuthResult.SUCCESS) return false;
        String newHash = plugin.getHashingUtil().hash(newPassword);
        return playerDAO.updatePassword(name, newHash);
    }

    // ==================== 2FA ====================

    /**
     * Bật 2FA cho người chơi - lưu secret tạm vào session.
     * Người chơi cần verify mã 1 lần để xác nhận bật.
     */
    public String enable2FA(UUID uuid) {
        String secret = plugin.getTotpGenerator().generateSecret();
        plugin.getSessionManager().setTotpPendingSecret(uuid, secret);
        return secret;
    }

    /**
     * Xác nhận bật 2FA (sau khi người chơi nhập đúng mã TOTP đầu tiên).
     */
    public boolean confirmEnable2FA(UUID uuid, String code) {
        String secret = plugin.getSessionManager().getTotpPendingSecret(uuid);
        if (secret == null) return false;

        int digits = plugin.getConfig().getInt("two-factor.so-chu-so", 6);
        int period = plugin.getConfig().getInt("two-factor.thoi-gian-hieu-luc", 30);
        int window = plugin.getConfig().getInt("two-factor.do-lech-cho-phep", 1);

        if (!plugin.getTotpGenerator().verifyCode(secret, code, digits, period, window)) {
            return false;
        }

        // Lưu secret vào DB và bật cờ enabled
        PlayerDAO dao = new PlayerDAO(plugin);
        boolean updSecret = dao.updateTotpSecret(uuid, secret);
        boolean updEnabled = dao.setTotpEnabled(uuid, true);

        // Dọn secret tạm trong session
        plugin.getSessionManager().setTotpPendingSecret(uuid, null);
        return updSecret && updEnabled;
    }

    /**
     * Tắt 2FA cho người chơi (yêu cầu xác thực mã TOTP).
     */
    public boolean disable2FA(UUID uuid, String code) {
        PlayerDAO dao = new PlayerDAO(plugin);
        String secret = dao.getTotpSecret(uuid);
        if (secret == null) return true; // đã tắt rồi

        int digits = plugin.getConfig().getInt("two-factor.so-chu-so", 6);
        int period = plugin.getConfig().getInt("two-factor.thoi-gian-hieu-luc", 30);
        int window = plugin.getConfig().getInt("two-factor.do-lech-cho-phep", 1);

        if (!plugin.getTotpGenerator().verifyCode(secret, code, digits, period, window)) {
            return false;
        }
        return dao.setTotpEnabled(uuid, false);
    }

    // ==================== BRUTE FORCE PROTECTION ====================

    /**
     * Kiểm tra tài khoản/IP có đang bị khóa không.
     */
    private AuthResult checkLock(String name, String ip) {
        String mode = plugin.getConfig().getString("bao-mat.che-do-khoa", "BOTH");
        long now = System.currentTimeMillis();

        // Kiểm tra trong cache trước
        for (String id : getIdentifiers(name, ip, mode)) {
            Long until = lockUntilMap.get(id);
            if (until != null && now < until) {
                return AuthResult.ACCOUNT_LOCKED;
            }
            if (until != null && now >= until) {
                lockUntilMap.remove(id); // hết hạn khóa
            }
        }

        // Đếm số lần sai gần đây trong DB
        long lockDurationMs = plugin.getConfig().getInt("bao-mat.thoi-gian-khoa", 300) * 1000L;
        long sinceMs = now - lockDurationMs;

        for (String id : getIdentifiers(name, ip, mode)) {
            int failures = attemptDAO.countRecentFailures(id, sinceMs);
            int maxAttempts = plugin.getConfig().getInt("bao-mat.so-lan-sai-toi-da", 5);
            if (failures >= maxAttempts) {
                lockUntilMap.put(id, now + lockDurationMs);
                return AuthResult.ACCOUNT_LOCKED;
            }
        }
        return AuthResult.SUCCESS;
    }

    /**
     * Ghi nhận một lần đăng nhập thất bại.
     */
    private void recordFailure(String name, String ip) {
        String mode = plugin.getConfig().getString("bao-mat.che-do-khoa", "BOTH");
        for (String id : getIdentifiers(name, ip, mode)) {
            attemptDAO.recordFailure(id, mode, ip);
        }
    }

    /**
     * Xóa các bản ghi thất bại (sau khi đăng nhập thành công).
     */
    private void clearFailure(String name, String ip) {
        String mode = plugin.getConfig().getString("bao-mat.che-do-khoa", "BOTH");
        for (String id : getIdentifiers(name, ip, mode)) {
            attemptDAO.clearAttempts(id);
            lockUntilMap.remove(id);
        }
    }

    /**
     * Lấy danh sách identifier cần ghi/kiểm tra dựa trên chế độ khóa.
     */
    private String[] getIdentifiers(String name, String ip, String mode) {
        return switch (mode.toUpperCase()) {
            case "ACCOUNT" -> new String[]{"acc:" + name.toLowerCase()};
            case "IP" -> new String[]{"ip:" + ip};
            default -> new String[]{"acc:" + name.toLowerCase(), "ip:" + ip};
        };
    }

    /**
     * Lấy số lần sai còn lại trước khi bị khóa.
     */
    public int getRemainingAttempts(String name, String ip) {
        String mode = plugin.getConfig().getString("bao-mat.che-do-khoa", "BOTH");
        long lockDurationMs = plugin.getConfig().getInt("bao-mat.thoi-gian-khoa", 300) * 1000L;
        long sinceMs = System.currentTimeMillis() - lockDurationMs;

        int max = plugin.getConfig().getInt("bao-mat.so-lan-sai-toi-da", 5);
        int worst = 0;
        for (String id : getIdentifiers(name, ip, mode)) {
            int failures = attemptDAO.countRecentFailures(id, sinceMs);
            worst = Math.max(worst, failures);
        }
        return Math.max(0, max - worst);
    }

    /**
     * Lấy thời gian còn lại của khóa (giây).
     */
    public long getLockRemainingSeconds(String name, String ip) {
        String mode = plugin.getConfig().getString("bao-mat.che-do-khoa", "BOTH");
        long now = System.currentTimeMillis();
        long maxRemain = 0;
        for (String id : getIdentifiers(name, ip, mode)) {
            Long until = lockUntilMap.get(id);
            if (until != null) {
                long rem = (until - now) / 1000L;
                if (rem > maxRemain) maxRemain = rem;
            }
        }
        return maxRemain;
    }

    /**
     * Reset khóa cho một tài khoản (dành cho admin).
     */
    public void adminUnlock(String name, String ip) {
        String mode = plugin.getConfig().getString("bao-mat.che-do-khoa", "BOTH");
        for (String id : getIdentifiers(name, ip, mode)) {
            lockUntilMap.remove(id);
            attemptDAO.clearAttempts(id);
        }
    }

    // ==================== VALIDATION ====================

    /**
     * Kiểm tra định dạng mật khẩu theo config.
     */
    public AuthResult validatePasswordFormat(String password) {
        if (password == null) return AuthResult.INVALID_PASSWORD_FORMAT;

        int min = plugin.getConfig().getInt("bao-mat.mat-khau-toi-thieu", 6);
        int max = plugin.getConfig().getInt("bao-mat.mat-khau-toi-da", 64);
        if (password.length() < min) return AuthResult.INVALID_PASSWORD_FORMAT;
        if (password.length() > max) return AuthResult.INVALID_PASSWORD_FORMAT;

        boolean requireUpper = plugin.getConfig().getBoolean("bao-mat.yeu-cau-mat-khau.chu-hoa", true);
        boolean requireLower = plugin.getConfig().getBoolean("bao-mat.yeu-cau-mat-khau.chu-thuong", true);
        boolean requireDigit = plugin.getConfig().getBoolean("bao-mat.yeu-cau-mat-khau.so", true);
        boolean requireSpecial = plugin.getConfig().getBoolean("bao-mat.yeu-cau-mat-khau.ky-tu-dac-biet", false);

        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = password.chars().anyMatch(c -> !Character.isLetterOrDigit(c));

        if (requireUpper && !hasUpper) return AuthResult.INVALID_PASSWORD_FORMAT;
        if (requireLower && !hasLower) return AuthResult.INVALID_PASSWORD_FORMAT;
        if (requireDigit && !hasDigit) return AuthResult.INVALID_PASSWORD_FORMAT;
        if (requireSpecial && !hasSpecial) return AuthResult.INVALID_PASSWORD_FORMAT;

        return AuthResult.SUCCESS;
    }

    /**
     * Lấy thông báo lỗi thân thiện cho từng kết quả AuthResult.
     */
    public String getErrorMessage(AuthResult result) {
        MessageManager msg = plugin.getMessageManager();
        return switch (result) {
            case ALREADY_REGISTERED -> msg.get("tin-nhan.tai-khoan-da-ton-tai", "&cTài khoản đã tồn tại!");
            case NOT_REGISTERED -> msg.get("tin-nhan.chua-dang-ky", "&cBạn chưa đăng ký!");
            case WRONG_PASSWORD -> msg.get("tin-nhan.sai-mat-khau", "&cMật khẩu sai!");
            case ACCOUNT_LOCKED -> msg.get("tin-nhan.bi-khoa", "&cTài khoản bị khóa!");
            case INVALID_PASSWORD_FORMAT -> msg.get("tin-nhan.mat-khau-yeu", "&cMật khẩu không đủ mạnh!");
            case PASSWORD_MISMATCH -> msg.get("tin-nhan.mat-khau-khong-khop", "&cMật khẩu không khớp!");
            case TOTP_REQUIRED -> msg.get("tin-nhan.can-2fa", "&cCần mã 2FA!");
            case TOTP_INVALID -> msg.get("tin-nhan.sai-2fa", "&cMã 2FA sai!");
            case PLAYER_NOT_FOUND -> "&cKhông tìm thấy người chơi!";
            case ALREADY_AUTHENTICATED -> msg.get("tin-nhan.da-dang-nhap-roi", "&cBạn đã đăng nhập rồi!");
            default -> "&cĐã xảy ra lỗi!";
        };
    }

    public PlayerDAO getPlayerDAO() { return playerDAO; }
}
