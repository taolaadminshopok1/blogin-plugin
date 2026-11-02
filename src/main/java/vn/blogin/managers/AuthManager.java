package vn.blogin.managers;
import vn.blogin.Blogin;
import vn.blogin.database.AttemptDAO;
import vn.blogin.database.PlayerDAO;
import vn.blogin.database.PlayerDAO.PlayerData;
import vn.blogin.utils.EncryptionUtil;
import vn.blogin.utils.PasswordStrength;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
public class AuthManager {
    private final Blogin plugin;
    private final PlayerDAO playerDAO;
    private final AttemptDAO attemptDAO;
    private final EncryptionUtil encryptionUtil;
    public enum AuthResult {
        SUCCESS,
        ALREADY_REGISTERED,
        NOT_REGISTERED,
        WRONG_PASSWORD,
        ACCOUNT_LOCKED,
        INVALID_PASSWORD_FORMAT,
        PASSWORD_MISMATCH,
        WEAK_PASSWORD,
        TOTP_REQUIRED,
        TOTP_INVALID,
        TOO_MANY_ATTEMPTS,
        PLAYER_NOT_FOUND,
        ALREADY_AUTHENTICATED,
        IP_BLOCKED
    }
    private final Map<String, Long> lockUntilMap = new ConcurrentHashMap<>();
    private final Map<String, String> lastLoginIpMap = new ConcurrentHashMap<>();
    private final Map<String, Integer> ipFailureCache = new ConcurrentHashMap<>();
    private final Map<String, Long> ipFailureCacheExpiry = new ConcurrentHashMap<>();
    private static final long IP_CACHE_TTL_MS = 10_000L;
    public AuthManager(Blogin plugin) {
        this.plugin = plugin;
        this.playerDAO = new PlayerDAO(plugin);
        this.attemptDAO = new AttemptDAO(plugin);
        this.encryptionUtil = plugin.getEncryptionUtil();
    }
    public int getCachedFailureCount(String identifier, long sinceMs) {
        long now = System.currentTimeMillis();
        Long expiry = ipFailureCacheExpiry.get(identifier);
        if (expiry != null && now < expiry) {
            Integer cached = ipFailureCache.get(identifier);
            if (cached != null) return cached;
        }
        int count = attemptDAO.countRecentFailures(identifier, sinceMs);
        ipFailureCache.put(identifier, count);
        ipFailureCacheExpiry.put(identifier, now + IP_CACHE_TTL_MS);
        return count;
    }
    private void invalidateFailureCache(String identifier) {
        ipFailureCache.remove(identifier);
        ipFailureCacheExpiry.remove(identifier);
    }
    public AuthResult register(UUID uuid, String name, String password, String ip) {
        if (playerDAO.isRegistered(name)) {
            return AuthResult.ALREADY_REGISTERED;
        }
        AuthResult validation = validatePasswordFormat(password);
        if (validation != AuthResult.SUCCESS) {
            return validation;
        }
        if (plugin.getConfig().getBoolean("bao-mat.chan-mat-khau-yeu", true)
                && PasswordStrength.score(password) < plugin.getConfig().getInt("bao-mat.diem-mat-khau-toi-thieu", 2)) {
            return AuthResult.WEAK_PASSWORD;
        }
        String hash = plugin.getHashingUtil().hash(password);
        boolean success = playerDAO.createPlayer(uuid, name, hash, ip);
        if (!success) {
            return AuthResult.ALREADY_REGISTERED;
        }
        if (plugin.getConfig().getBoolean("log-su-kien", true)) {
            plugin.getLog().info(String.format("[Đăng ký] %s (%s) từ IP %s", name, uuid, ip));
        }
        return AuthResult.SUCCESS;
    }
    public AuthResult login(UUID uuid, String name, String password, String totpCode, String ip) {
        PlayerData data = playerDAO.getPlayerData(name);
        if (data == null) {
            return AuthResult.NOT_REGISTERED;
        }
        AuthResult lockCheck = checkLock(name, ip);
        if (lockCheck != AuthResult.SUCCESS) {
            return lockCheck;
        }
        boolean passwordOk = plugin.getHashingUtil().verify(password, data.passwordHash());
        if (!passwordOk) {
            recordFailure(name, ip);
            return AuthResult.WRONG_PASSWORD;
        }
        if (data.totpEnabled()) {
            String secret = encryptionUtil.decrypt(data.totpSecret());
            if (secret == null || secret.isBlank()) {
                recordFailure(name, ip);
                return AuthResult.TOTP_INVALID;
            }
            if (totpCode == null || totpCode.isBlank()) {
                return AuthResult.TOTP_REQUIRED;
            }
            int digits = plugin.getConfig().getInt("two-factor.so-chu-so", 6);
            int period = plugin.getConfig().getInt("two-factor.thoi-gian-hieu-luc", 30);
            int window = plugin.getConfig().getInt("two-factor.do-lech-cho-phep", 1);
            boolean totpOk = plugin.getTotpGenerator().verifyCode(
                    secret, totpCode, digits, period, window);
            if (!totpOk) {
                recordFailure(name, ip);
                return AuthResult.TOTP_INVALID;
            }
        }
        playerDAO.updateLoginInfo(uuid, ip);
        clearFailure(name, ip);
        if (plugin.getHashingUtil().needsRehash(data.passwordHash())) {
            String newHash = plugin.getHashingUtil().hash(password);
            playerDAO.updatePassword(name, newHash);
        }
        lastLoginIpMap.put(name.toLowerCase(), ip);
        if (plugin.getConfig().getBoolean("log-su-kien", true)) {
            plugin.getLog().info(String.format("[Đăng nhập] %s (%s) từ IP %s", name, uuid, ip));
        }
        return AuthResult.SUCCESS;
    }
    public void loginAsync(UUID uuid, String name, String password, String totpCode,
                           String ip, java.util.function.Consumer<AuthResult> callback) {
        plugin.getDatabaseManager().runAsync(
                () -> login(uuid, name, password, totpCode, ip),
                callback
        );
    }
    public void registerAsync(UUID uuid, String name, String password, String ip,
                              java.util.function.Consumer<AuthResult> callback) {
        plugin.getDatabaseManager().runAsync(
                () -> register(uuid, name, password, ip),
                callback
        );
    }
    public AuthResult changePassword(String name, String oldPassword, String newPassword) {
        PlayerData data = playerDAO.getPlayerData(name);
        if (data == null) return AuthResult.PLAYER_NOT_FOUND;
        if (!plugin.getHashingUtil().verify(oldPassword, data.passwordHash())) {
            return AuthResult.WRONG_PASSWORD;
        }
        AuthResult validation = validatePasswordFormat(newPassword);
        if (validation != AuthResult.SUCCESS) return validation;
        if (plugin.getConfig().getBoolean("bao-mat.chan-mat-khau-yeu", true)
                && PasswordStrength.score(newPassword) < plugin.getConfig().getInt("bao-mat.diem-mat-khau-toi-thieu", 2)) {
            return AuthResult.WEAK_PASSWORD;
        }
        if (oldPassword.equals(newPassword)) {
            return AuthResult.INVALID_PASSWORD_FORMAT;
        }
        String newHash = plugin.getHashingUtil().hash(newPassword);
        boolean success = playerDAO.updatePassword(name, newHash);
        return success ? AuthResult.SUCCESS : AuthResult.PLAYER_NOT_FOUND;
    }
    public boolean adminResetPassword(String name, String newPassword) {
        AuthResult validation = validatePasswordFormat(newPassword);
        if (validation != AuthResult.SUCCESS) return false;
        String newHash = plugin.getHashingUtil().hash(newPassword);
        return playerDAO.updatePassword(name, newHash);
    }
    public String enable2FA(UUID uuid) {
        String secret = plugin.getTotpGenerator().generateSecret();
        plugin.getSessionManager().setTotpPendingSecret(uuid, secret);
        return secret;
    }
    public boolean confirmEnable2FA(UUID uuid, String code) {
        String secret = plugin.getSessionManager().getTotpPendingSecret(uuid);
        if (secret == null) return false;
        int digits = plugin.getConfig().getInt("two-factor.so-chu-so", 6);
        int period = plugin.getConfig().getInt("two-factor.thoi-gian-hieu-luc", 30);
        int window = plugin.getConfig().getInt("two-factor.do-lech-cho-phep", 1);
        if (!plugin.getTotpGenerator().verifyCode(secret, code, digits, period, window)) {
            return false;
        }
        String encrypted = encryptionUtil.encrypt(secret);
        if (encrypted == null) {
            plugin.getSessionManager().setTotpPendingSecret(uuid, null);
            plugin.getLog().severe("✖ Không thể mã hoá secret 2FA cho " + uuid);
            return false;
        }
        boolean updSecret = playerDAO.updateTotpSecret(uuid, encrypted);
        boolean updEnabled = playerDAO.setTotpEnabled(uuid, true);
        plugin.getSessionManager().setTotpPendingSecret(uuid, null);
        return updSecret && updEnabled;
    }
    public boolean disable2FA(UUID uuid, String code) {
        String encrypted = playerDAO.getTotpSecret(uuid);
        if (encrypted == null) return true;
        String secret = encryptionUtil.decrypt(encrypted);
        if (secret == null) return false;
        int digits = plugin.getConfig().getInt("two-factor.so-chu-so", 6);
        int period = plugin.getConfig().getInt("two-factor.thoi-gian-hieu-luc", 30);
        int window = plugin.getConfig().getInt("two-factor.do-lech-cho-phep", 1);
        if (!plugin.getTotpGenerator().verifyCode(secret, code, digits, period, window)) {
            return false;
        }
        boolean disabled = playerDAO.setTotpEnabled(uuid, false);
        if (disabled) {
            playerDAO.updateTotpSecret(uuid, null);
        }
        return disabled;
    }
    public String getDecryptedTotpSecret(UUID uuid) {
        String encrypted = playerDAO.getTotpSecret(uuid);
        return encrypted == null ? null : encryptionUtil.decrypt(encrypted);
    }
    private AuthResult checkLock(String name, String ip) {
        String mode = plugin.getConfig().getString("bao-mat.che-do-khoa", "BOTH");
        long now = System.currentTimeMillis();
        for (String id : getIdentifiers(name, ip, mode)) {
            Long until = lockUntilMap.get(id);
            if (until != null) {
                if (now < until) {
                    return AuthResult.ACCOUNT_LOCKED;
                }
                lockUntilMap.remove(id, until);
            }
        }
        long lockDurationMs = plugin.getConfig().getInt("bao-mat.thoi-gian-khoa", 300) * 1000L;
        long sinceMs = now - lockDurationMs;
        int maxAttempts = plugin.getConfig().getInt("bao-mat.so-lan-sai-toi-da", 5);
        for (String id : getIdentifiers(name, ip, mode)) {
            int failures = attemptDAO.countRecentFailures(id, sinceMs);
            if (failures >= maxAttempts) {
                lockUntilMap.put(id, now + lockDurationMs);
                return AuthResult.ACCOUNT_LOCKED;
            }
        }
        return AuthResult.SUCCESS;
    }
    private void recordFailure(String name, String ip) {
        String mode = plugin.getConfig().getString("bao-mat.che-do-khoa", "BOTH");
        for (String id : getIdentifiers(name, ip, mode)) {
            attemptDAO.recordFailure(id, mode, ip);
            invalidateFailureCache(id);
        }
    }
    private void clearFailure(String name, String ip) {
        String mode = plugin.getConfig().getString("bao-mat.che-do-khoa", "BOTH");
        for (String id : getIdentifiers(name, ip, mode)) {
            attemptDAO.clearAttempts(id);
            lockUntilMap.remove(id);
            invalidateFailureCache(id);
        }
    }
    private String[] getIdentifiers(String name, String ip, String mode) {
        return switch (mode.toUpperCase()) {
            case "ACCOUNT" -> new String[]{"acc:" + name.toLowerCase()};
            case "IP" -> new String[]{"ip:" + ip};
            default -> new String[]{"acc:" + name.toLowerCase(), "ip:" + ip};
        };
    }
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
    public void adminUnlock(String name, String ip) {
        String mode = plugin.getConfig().getString("bao-mat.che-do-khoa", "BOTH");
        for (String id : getIdentifiers(name, ip, mode)) {
            lockUntilMap.remove(id);
            attemptDAO.clearAttempts(id);
            invalidateFailureCache(id);
        }
    }
    public void cleanupExpiredLocks() {
        long now = System.currentTimeMillis();
        lockUntilMap.entrySet().removeIf(entry -> entry.getValue() <= now);
        ipFailureCacheExpiry.entrySet().removeIf(entry -> entry.getValue() <= now);
        ipFailureCache.keySet().removeIf(key -> !ipFailureCacheExpiry.containsKey(key));
    }
    public String getLastLoginIp(String name) {
        return lastLoginIpMap.get(name.toLowerCase());
    }
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
    public String getErrorMessage(AuthResult result) {
        MessageManager msg = plugin.getMessageManager();
        return switch (result) {
            case ALREADY_REGISTERED -> msg.get("tin-nhan.tai-khoan-da-ton-tai", "&cTài khoản đã tồn tại!");
            case NOT_REGISTERED -> msg.get("tin-nhan.chua-dang-ky", "&cBạn chưa đăng ký!");
            case WRONG_PASSWORD -> msg.get("tin-nhan.sai-mat-khau", "&cMật khẩu sai!");
            case ACCOUNT_LOCKED -> msg.get("tin-nhan.bi-khoa", "&cTài khoản bị khóa!");
            case INVALID_PASSWORD_FORMAT -> msg.get("tin-nhan.mat-khau-yeu", "&cMật khẩu không đủ mạnh!");
            case PASSWORD_MISMATCH -> msg.get("tin-nhan.mat-khau-khong-khop", "&cMật khẩu không khớp!");
            case WEAK_PASSWORD -> msg.get("tin-nhan.mat-khau-yeu", "&cMật khẩu quá yếu, hãy dùng mật khẩu mạnh hơn!");
            case TOTP_REQUIRED -> msg.get("tin-nhan.can-2fa", "&cCần mã 2FA!");
            case TOTP_INVALID -> msg.get("tin-nhan.sai-2fa", "&cMã 2FA sai!");
            case PLAYER_NOT_FOUND -> "&cKhông tìm thấy người chơi!";
            case ALREADY_AUTHENTICATED -> msg.get("tin-nhan.da-dang-nhap-roi", "&cBạn đã đăng nhập rồi!");
            case IP_BLOCKED -> msg.get("tin-nhan.ip-blocked", "&cIP của bạn đang bị tạm khóa!");
            default -> "&cĐã xảy ra lỗi!";
        };
    }
    public PlayerDAO getPlayerDAO() { return playerDAO; }
    public AttemptDAO getAttemptDAO() { return attemptDAO; }
}
