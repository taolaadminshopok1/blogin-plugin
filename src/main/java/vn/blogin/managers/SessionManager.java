package vn.blogin.managers;

import org.bukkit.entity.Player;
import vn.blogin.Blogin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Quản lý phiên đăng nhập của người chơi trong bộ nhớ.
 *
 * <p>Các thông tin được lưu:
 * <ul>
 *   <li>Đã xác thực hay chưa</li>
 *   <li>Đang đăng ký hay đang đăng nhập</li>
 *   <li>Captcha đang chờ</li>
 *   <li>Mã captcha đã nhập</li>
 *   <li>Thời gian còn lại để đăng nhập</li>
 *   <li>IP đã bind với phiên</li>
 * </ul>
 *
 * @author BloginVN
 */
public class SessionManager {

    private final Blogin plugin;
    private final Map<UUID, SessionData> sessions = new HashMap<>();

    /**
     * Dữ liệu phiên của một người chơi.
     */
    public static class SessionData {
        public boolean authenticated = false;
        public boolean registering = false;
        public String pendingCaptcha = null;       // captcha user cần nhập
        public String captchaAnswer = null;         // captcha đúng
        public String ip = null;
        public String passwordConfirm = null;      // cho quá trình register (lưu mk lần 1)
        public long loginStartTime = 0;
        public int remainingTime = 0;
        public boolean awaiting2FA = false;
        public String totpPendingSecret = null;    // secret tạm khi đang setup 2FA
    }

    public SessionManager(Blogin plugin) {
        this.plugin = plugin;
    }

    /**
     * Tạo phiên mới cho người chơi vừa vào server.
     */
    public SessionData createSession(UUID uuid, String ip) {
        SessionData data = new SessionData();
        data.ip = ip;
        data.loginStartTime = System.currentTimeMillis();
        data.remainingTime = plugin.getConfig().getInt("thoi-gian-dang-nhap", 60);
        sessions.put(uuid, data);
        return data;
    }

    /**
     * Lấy dữ liệu phiên (tạo mới nếu chưa có).
     */
    public SessionData get(UUID uuid) {
        return sessions.computeIfAbsent(uuid, k -> {
            SessionData d = new SessionData();
            d.remainingTime = plugin.getConfig().getInt("thoi-gian-dang-nhap", 60);
            d.loginStartTime = System.currentTimeMillis();
            return d;
        });
    }

    public boolean isAuthenticated(UUID uuid) {
        SessionData d = sessions.get(uuid);
        return d != null && d.authenticated;
    }

    public boolean isRegistering(UUID uuid) {
        SessionData d = sessions.get(uuid);
        return d != null && d.registering;
    }

    /**
     * Đánh dấu người chơi đã đăng nhập thành công.
     */
    public void markAuthenticated(UUID uuid) {
        SessionData d = get(uuid);
        d.authenticated = true;
        d.registering = false;
        d.pendingCaptcha = null;
        d.captchaAnswer = null;
        d.passwordConfirm = null;
        d.awaiting2FA = false;
    }

    /**
     * Đăng xuất người chơi (xóa phiên).
     */
    public void logout(UUID uuid) {
        sessions.remove(uuid);
    }

    public void setPendingCaptcha(UUID uuid, String captcha, String answer) {
        SessionData d = get(uuid);
        d.pendingCaptcha = captcha;
        d.captchaAnswer = answer;
    }

    public String getPendingCaptcha(UUID uuid) {
        SessionData d = sessions.get(uuid);
        return d == null ? null : d.pendingCaptcha;
    }

    public boolean verifyCaptcha(UUID uuid, String userInput) {
        SessionData d = sessions.get(uuid);
        if (d == null || d.captchaAnswer == null) return true; // không bắt buộc
        boolean ok = d.captchaAnswer.equalsIgnoreCase(userInput);
        if (ok) {
            d.pendingCaptcha = null;
            d.captchaAnswer = null;
        }
        return ok;
    }

    public void setPasswordConfirm(UUID uuid, String pwd) {
        get(uuid).passwordConfirm = pwd;
    }

    public String getPasswordConfirm(UUID uuid) {
        SessionData d = sessions.get(uuid);
        return d == null ? null : d.passwordConfirm;
    }

    public void clearPasswordConfirm(UUID uuid) {
        SessionData d = sessions.get(uuid);
        if (d != null) d.passwordConfirm = null;
    }

    public void setRegistering(UUID uuid, boolean reg) {
        get(uuid).registering = reg;
    }

    public void setAwaiting2FA(UUID uuid, boolean awaiting) {
        get(uuid).awaiting2FA = awaiting;
    }

    public boolean isAwaiting2FA(UUID uuid) {
        SessionData d = sessions.get(uuid);
        return d != null && d.awaiting2FA;
    }

    public void setTotpPendingSecret(UUID uuid, String secret) {
        get(uuid).totpPendingSecret = secret;
    }

    public String getTotpPendingSecret(UUID uuid) {
        SessionData d = sessions.get(uuid);
        return d == null ? null : d.totpPendingSecret;
    }

    /**
     * Tick thời gian đếm ngược mỗi giây.
     */
    public void tickTimeout(UUID uuid) {
        SessionData d = sessions.get(uuid);
        if (d == null) return;
        if (d.remainingTime > 0) d.remainingTime--;
    }

    public int getRemainingTime(UUID uuid) {
        SessionData d = sessions.get(uuid);
        return d == null ? 0 : d.remainingTime;
    }

    public String getIp(UUID uuid) {
        SessionData d = sessions.get(uuid);
        return d == null ? null : d.ip;
    }

    /**
     * Reset thời gian còn lại (dùng khi đăng nhập thất bại và vẫn còn lượt thử).
     */
    public void resetTimeout(UUID uuid) {
        SessionData d = sessions.get(uuid);
        if (d != null) {
            d.remainingTime = plugin.getConfig().getInt("thoi-gian-dang-nhap", 60);
            d.loginStartTime = System.currentTimeMillis();
        }
    }

    /**
     * Xóa phiên khi người chơi rời server.
     */
    public void cleanup(UUID uuid) {
        sessions.remove(uuid);
    }

    /**
     * Đếm số phiên đang hoạt động (để giới hạn IP).
     */
    public int countActiveByIp(String ip) {
        int count = 0;
        for (SessionData d : sessions.values()) {
            if (ip.equals(d.ip)) count++;
        }
        return count;
    }

    /**
     * Lấy tất cả session đang hoạt động (cho admin xem).
     */
    public Map<UUID, SessionData> getAll() {
        return sessions;
    }
}
