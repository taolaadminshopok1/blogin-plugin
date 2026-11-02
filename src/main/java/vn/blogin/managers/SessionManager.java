package vn.blogin.managers;
import vn.blogin.Blogin;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
public class SessionManager {
    private final Blogin plugin;
    private final Map<UUID, SessionData> sessions = new ConcurrentHashMap<>();
    public static class SessionData {
        public boolean authenticated = false;
        public boolean registering = false;
        public String pendingCaptcha = null;
        public String captchaAnswer = null;
        public String ip = null;
        public String passwordConfirm = null;
        public long loginStartTime = 0;
        public int remainingTime = 0;
        public boolean awaiting2FA = false;
        public String totpPendingSecret = null;
        public int failedAttempts = 0;
        public boolean captchaPassed = false;
        public long lastActivity = 0;
        public String guiPassword = null;
        public String guiTotp = null;
        public String awaitingChatInput = null;
        public boolean reminded2FA = false;
    }
    public SessionManager(Blogin plugin) {
        this.plugin = plugin;
    }
    public SessionData createSession(UUID uuid, String ip) {
        SessionData data = new SessionData();
        data.ip = ip;
        data.loginStartTime = System.currentTimeMillis();
        data.lastActivity = data.loginStartTime;
        data.remainingTime = plugin.getConfig().getInt("thoi-gian-dang-nhap", 60);
        sessions.put(uuid, data);
        return data;
    }
    public SessionData get(UUID uuid) {
        return sessions.computeIfAbsent(uuid, k -> {
            SessionData d = new SessionData();
            d.remainingTime = plugin.getConfig().getInt("thoi-gian-dang-nhap", 60);
            d.loginStartTime = System.currentTimeMillis();
            d.lastActivity = d.loginStartTime;
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
    public void markAuthenticated(UUID uuid) {
        SessionData d = get(uuid);
        d.authenticated = true;
        d.registering = false;
        d.pendingCaptcha = null;
        d.captchaAnswer = null;
        d.passwordConfirm = null;
        d.awaiting2FA = false;
        d.failedAttempts = 0;
        d.captchaPassed = true;
    }
    public void logout(UUID uuid) {
        sessions.remove(uuid);
    }
    public void setPendingCaptcha(UUID uuid, String captcha, String answer) {
        SessionData d = get(uuid);
        d.pendingCaptcha = captcha;
        d.captchaAnswer = answer;
        d.captchaPassed = false;
    }
    public String getPendingCaptcha(UUID uuid) {
        SessionData d = sessions.get(uuid);
        return d == null ? null : d.pendingCaptcha;
    }
    public boolean verifyCaptcha(UUID uuid, String userInput) {
        SessionData d = sessions.get(uuid);
        if (d == null || d.captchaAnswer == null) return true;
        boolean ok = d.captchaAnswer.equalsIgnoreCase(userInput);
        if (ok) {
            d.pendingCaptcha = null;
            d.captchaAnswer = null;
            d.captchaPassed = true;
        }
        return ok;
    }
    public boolean isCaptchaPassed(UUID uuid) {
        SessionData d = sessions.get(uuid);
        return d != null && d.captchaPassed;
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
    public void incrementFailedAttempts(UUID uuid) {
        SessionData d = get(uuid);
        d.failedAttempts++;
    }
    public int getFailedAttempts(UUID uuid) {
        SessionData d = sessions.get(uuid);
        return d == null ? 0 : d.failedAttempts;
    }
    public void tickTimeout(UUID uuid) {
        SessionData d = sessions.get(uuid);
        if (d == null) return;
        if (d.remainingTime > 0) d.remainingTime--;
        d.lastActivity = System.currentTimeMillis();
    }
    public int getRemainingTime(UUID uuid) {
        SessionData d = sessions.get(uuid);
        return d == null ? 0 : d.remainingTime;
    }
    public String getIp(UUID uuid) {
        SessionData d = sessions.get(uuid);
        return d == null ? null : d.ip;
    }
    public void updateIp(UUID uuid, String ip) {
        SessionData d = sessions.get(uuid);
        if (d != null) d.ip = ip;
    }
    public void resetTimeout(UUID uuid) {
        SessionData d = sessions.get(uuid);
        if (d != null) {
            d.remainingTime = plugin.getConfig().getInt("thoi-gian-dang-nhap", 60);
            d.loginStartTime = System.currentTimeMillis();
            d.lastActivity = d.loginStartTime;
        }
    }
    public void cleanup(UUID uuid) {
        sessions.remove(uuid);
    }
    public void setGuiPassword(UUID uuid, String password) {
        get(uuid).guiPassword = password;
    }
    public String getGuiPassword(UUID uuid) {
        SessionData d = sessions.get(uuid);
        return d == null ? null : d.guiPassword;
    }
    public void setGuiTotp(UUID uuid, String totp) {
        get(uuid).guiTotp = totp;
    }
    public String getGuiTotp(UUID uuid) {
        SessionData d = sessions.get(uuid);
        return d == null ? null : d.guiTotp;
    }
    public void setAwaitingChatInput(UUID uuid, String type) {
        get(uuid).awaitingChatInput = type;
    }
    public String getAwaitingChatInput(UUID uuid) {
        SessionData d = sessions.get(uuid);
        return d == null ? null : d.awaitingChatInput;
    }
    public int countActiveByIp(String ip) {
        int count = 0;
        for (SessionData d : sessions.values()) {
            if (ip.equals(d.ip)) count++;
        }
        return count;
    }
    public Map<UUID, SessionData> getAll() {
        return sessions;
    }
    public int cleanupOrphanSessions(java.util.function.Predicate<UUID> isOnline) {
        int removed = 0;
        for (UUID uuid : new java.util.ArrayList<>(sessions.keySet())) {
            if (!isOnline.test(uuid)) {
                sessions.remove(uuid);
                removed++;
            }
        }
        return removed;
    }
}
