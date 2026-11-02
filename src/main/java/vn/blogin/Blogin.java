package vn.blogin;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import vn.blogin.commands.*;
import vn.blogin.database.DatabaseManager;
import vn.blogin.gui.LoginGui;
import vn.blogin.listeners.*;
import vn.blogin.managers.AuthManager;
import vn.blogin.managers.CaptchaManager;
import vn.blogin.managers.MessageManager;
import vn.blogin.managers.SessionManager;
import vn.blogin.placeholder.BloginPlaceholder;
import vn.blogin.utils.ChatUtil;
import vn.blogin.utils.EncryptionUtil;
import vn.blogin.utils.HashingUtil;
import vn.blogin.utils.PlayerRestrictionUtil;
import vn.blogin.utils.TOTPGenerator;
import vn.blogin.welcome.WelcomeManager;
import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
public class Blogin extends JavaPlugin {
    private static Blogin instance;
    private final Logger log = getLogger();
    private DatabaseManager databaseManager;
    private AuthManager authManager;
    private SessionManager sessionManager;
    private CaptchaManager captchaManager;
    private MessageManager messageManager;
    private HashingUtil hashingUtil;
    private TOTPGenerator totpGenerator;
    private WelcomeManager welcomeManager;
    private EncryptionUtil encryptionUtil;
    private LoginGui loginGui;
    private BukkitTask timeoutTask;
    private BukkitTask cleanupTask;
    @Override
    public void onEnable() {
        instance = this;
        long startTime = System.currentTimeMillis();
        log.info("╔══════════════════════════════════════════╗");
        log.info("║   Blogin v" + getDescription().getVersion() + " - Khởi động                  ║");
        log.info("║   Plugin Login xịn xò - Việt hóa 100%   ║");
        log.info("╚══════════════════════════════════════════╝");
        saveDefaultConfig();
        this.totpGenerator = new TOTPGenerator();
        if (!initEncryption()) {
            log.severe("✖ Không thể khởi tạo khoá mã hoá! Plugin sẽ tắt.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        int cost = getConfig().getInt("bao-mat.bcrypt-cost", HashingUtil.DEFAULT_COST);
        this.hashingUtil = new HashingUtil(cost);
        this.messageManager = new MessageManager(this);
        this.databaseManager = new DatabaseManager(this);
        if (!databaseManager.init()) {
            log.severe("✖ Không thể kết nối Database! Plugin sẽ tắt.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        this.captchaManager = new CaptchaManager(this);
        this.sessionManager = new SessionManager(this);
        this.authManager = new AuthManager(this);
        this.welcomeManager = new WelcomeManager(this);
        this.loginGui = new LoginGui(this);
        registerCommands();
        registerListeners();
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            try {
                new BloginPlaceholder(this).register();
                log.info("✔ Đã hook vào PlaceholderAPI");
            } catch (Exception e) {
                log.warning("✖ Không thể đăng ký PlaceholderAPI: " + e.getMessage());
            }
        }
        startTimeoutTask();
        startCleanupTask();
        long elapsed = System.currentTimeMillis() - startTime;
        log.info("✔ Blogin đã sẵn sàng! (" + elapsed + "ms)");
    }
    private boolean initEncryption() {
        Path keyFile = getDataFolder().toPath().resolve("secret.key");
        try {
            this.encryptionUtil = new EncryptionUtil(keyFile);
            return true;
        } catch (IOException e) {
            log.log(Level.SEVERE, "✖ Lỗi đọc/tạo file khoá mã hoá: " + e.getMessage(), e);
            return false;
        }
    }
    @Override
    public void onDisable() {
        log.info("Đang tắt Blogin...");
        if (timeoutTask != null) timeoutTask.cancel();
        if (cleanupTask != null) cleanupTask.cancel();
        for (Player p : Bukkit.getOnlinePlayers()) {
            PlayerRestrictionUtil.clearEffects(p);
            PlayerRestrictionUtil.showToOthers(this, p);
        }
        if (welcomeManager != null) {
            welcomeManager.getBossBarEffect().shutdown();
        }
        if (sessionManager != null) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                sessionManager.logout(p.getUniqueId());
            }
        }
        if (databaseManager != null) databaseManager.close();
        log.info("✔ Blogin đã tắt an toàn");
    }
    private void registerCommands() {
        register("login", new LoginCommand(this));
        register("register", new RegisterCommand(this));
        register("logout", new LogoutCommand(this));
        register("changepassword", new ChangePasswordCommand(this));
        register("blogin", new BloginAdminCommand(this));
        register("captcha", new CaptchaCommand(this));
        register("2fa", new TwoFactorCommand(this));
        register("bloginwelcome", new WelcomeAdminCommand(this));
        register("blogingui", new LoginGuiCommand(this));
        log.info("✔ Đã đăng ký 9 commands");
    }
    private void register(String name, org.bukkit.command.CommandExecutor executor) {
        var cmd = getCommand(name);
        if (cmd == null) {
            log.warning("✖ Không tìm thấy command: " + name);
            return;
        }
        cmd.setExecutor(executor);
    }
    private void registerListeners() {
        var pm = Bukkit.getPluginManager();
        pm.registerEvents(new PlayerJoinListener(this), this);
        pm.registerEvents(new PlayerQuitListener(this), this);
        pm.registerEvents(new PlayerChatListener(this), this);
        pm.registerEvents(new PlayerMoveListener(this), this);
        pm.registerEvents(new PlayerInteractListener(this), this);
        pm.registerEvents(new PlayerCommandListener(this), this);
        pm.registerEvents(new PlayerDamageListener(this), this);
        pm.registerEvents(new PlayerBlockBreakListener(this), this);
        pm.registerEvents(new PlayerBlockPlaceListener(this), this);
        pm.registerEvents(new AuthenticatedListener(this), this);
        pm.registerEvents(new PlayerLoginListener(this), this);
        pm.registerEvents(new PlayerJoinMessageListener(this), this);
        pm.registerEvents(loginGui, this);
        pm.registerEvents(welcomeManager.getBossBarEffect(), this);
        log.info("✔ Đã đăng ký 14 event listeners");
    }
    private void startTimeoutTask() {
        timeoutTask = Bukkit.getScheduler().runTaskTimer(this, () -> {
            int timeout = getConfig().getInt("thoi-gian-dang-nhap", 60);
            if (timeout <= 0) return;
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (sessionManager.isAuthenticated(p.getUniqueId())) continue;
                if (p.hasPermission("blogin.bypass.timeout")) continue;
                sessionManager.tickTimeout(p.getUniqueId());
                int remain = sessionManager.getRemainingTime(p.getUniqueId());
                if (remain == 10 || remain == 5 || remain == 3 || remain == 2 || remain == 1) {
                    ChatUtil.send(p, messageManager.get("tin-nhan.canh-bao-thoi-gian",
                            "&e⏰ Còn {time} giây để đăng nhập!", "time", String.valueOf(remain)));
                }
                if (remain <= 0) {
                    Component kickMsg = ChatUtil.parse(messageManager.get("tin-nhan.het-thoi-gian",
                            "&c⏰ Hết thời gian đăng nhập!"));
                    p.kick(kickMsg);
                }
            }
        }, 20L, 20L);
    }
    private void startCleanupTask() {
        long intervalTicks = Math.max(60L, getConfig().getLong("bao-mat.chu-ky-don-dep", 300L) * 20L);
        cleanupTask = Bukkit.getScheduler().runTaskTimer(this, () -> {
            try {
                authManager.cleanupExpiredLocks();
                int removed = sessionManager.cleanupOrphanSessions(
                        uuid -> Bukkit.getPlayer(uuid) != null);
                if (removed > 0 && getConfig().getBoolean("debug", false)) {
                    log.info("🧹 Đã dọn " + removed + " session mồ côi");
                }
                long olderThan = System.currentTimeMillis() - 86_400_000L;
                getDatabaseManager().runAsync(
                        () -> authManager.getAttemptDAO().cleanupOldAttempts(olderThan),
                        deleted -> {
                            if (deleted != null && deleted > 0 && getConfig().getBoolean("debug", false)) {
                                log.info("🧹 Đã xoá " + deleted + " bản ghi đăng nhập sai cũ");
                            }
                        });
            } catch (Exception e) {
                log.warning("⚠ Lỗi trong task dọn dẹp: " + e.getMessage());
            }
        }, intervalTicks, intervalTicks);
    }
    public void reloadPluginConfig() {
        reloadConfig();
        if (messageManager != null) messageManager.reload();
        if (hashingUtil != null) {
            int cost = getConfig().getInt("bao-mat.bcrypt-cost", HashingUtil.DEFAULT_COST);
            hashingUtil.setCostFactor(cost);
        }
        if (captchaManager != null) captchaManager.reload();
    }
    public static Blogin getInstance() { return instance; }
    public DatabaseManager getDatabaseManager() { return databaseManager; }
    public AuthManager getAuthManager() { return authManager; }
    public SessionManager getSessionManager() { return sessionManager; }
    public CaptchaManager getCaptchaManager() { return captchaManager; }
    public MessageManager getMessageManager() { return messageManager; }
    public HashingUtil getHashingUtil() { return hashingUtil; }
    public TOTPGenerator getTotpGenerator() { return totpGenerator; }
    public WelcomeManager getWelcomeManager() { return welcomeManager; }
    public EncryptionUtil getEncryptionUtil() { return encryptionUtil; }
    public LoginGui getLoginGui() { return loginGui; }
    public Logger getLog() { return log; }
}
