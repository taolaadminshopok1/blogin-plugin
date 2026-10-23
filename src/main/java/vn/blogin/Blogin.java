package vn.blogin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import vn.blogin.commands.*;
import vn.blogin.database.DatabaseManager;
import vn.blogin.listeners.*;
import vn.blogin.managers.AuthManager;
import vn.blogin.managers.CaptchaManager;
import vn.blogin.managers.MessageManager;
import vn.blogin.managers.SessionManager;
import vn.blogin.placeholder.BloginPlaceholder;
import vn.blogin.utils.HashingUtil;
import vn.blogin.utils.TOTPGenerator;
import vn.blogin.welcome.WelcomeManager;

import java.util.logging.Logger;

/**
 * Blogin - Plugin Login cao cấp cho Minecraft 1.20.1 - 1.21.4
 *
 * <p>Tính năng:
 * <ul>
 *   <li>Đăng ký / đăng nhập bảo mật với BCrypt hash + salt ngẫu nhiên</li>
 *   <li>Chống brute-force theo IP / tài khoản / cả hai</li>
 *   <li>Captcha chống bot</li>
 *   <li>2FA (TOTP - Google Authenticator)</li>
 *   <li>Việt hóa 100% trong config</li>
 *   <li>MySQL / SQLite linh hoạt</li>
 *   <li>PlaceholderAPI support</li>
 * </ul>
 *
 * @author BloginVN
 */
public class Blogin extends JavaPlugin {

    private static Blogin instance;

    // Logger chính của plugin
    private final Logger log = getLogger();

    // Các Manager chính - Singleton pattern
    private DatabaseManager databaseManager;
    private AuthManager authManager;
    private SessionManager sessionManager;
    private CaptchaManager captchaManager;
    private MessageManager messageManager;
    private HashingUtil hashingUtil;
    private TOTPGenerator totpGenerator;
    private WelcomeManager welcomeManager;

    // Task đếm ngược timeout đăng nhập
    private BukkitTask timeoutTask;

    /**
     * Hàm khởi tạo khi plugin được bật.
     */
    @Override
    public void onEnable() {
        instance = this;
        long startTime = System.currentTimeMillis();

        log.info("╔══════════════════════════════════════════╗");
        log.info("║   Blogin v" + getDescription().getVersion() + " - Khởi động                  ║");
        log.info("║   Plugin Login xịn xò - Việt hóa 100%   ║");
        log.info("╚══════════════════════════════════════════╝");

        // 1. Lưu config mặc định nếu chưa có
        saveDefaultConfig();

        // 2. Khởi tạo các utility
        this.hashingUtil = new HashingUtil();
        this.totpGenerator = new TOTPGenerator();

        // 3. Khởi tạo MessageManager - đọc các chuỗi Việt hóa
        this.messageManager = new MessageManager(this);

        // 4. Khởi tạo Database
        this.databaseManager = new DatabaseManager(this);
        if (!databaseManager.init()) {
            log.severe("✖ Không thể kết nối Database! Plugin sẽ tắt.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // 5. Khởi tạo các Manager nghiệp vụ
        this.captchaManager = new CaptchaManager(this);
        this.sessionManager = new SessionManager(this);
        this.authManager = new AuthManager(this);
        this.welcomeManager = new WelcomeManager(this);

        // 6. Đăng ký Commands
        registerCommands();

        // 7. Đăng ký Event Listeners
        registerListeners();

        // 8. Đăng ký PlaceholderAPI (nếu có)
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            try {
                new BloginPlaceholder(this).register();
                log.info("✔ Đã hook vào PlaceholderAPI");
            } catch (Exception e) {
                log.warning("✖ Không thể đăng ký PlaceholderAPI: " + e.getMessage());
            }
        }

        // 9. Bắt đầu task kiểm tra timeout đăng nhập mỗi giây
        startTimeoutTask();

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("✔ Blogin đã sẵn sàng! (" + elapsed + "ms)");
    }

    /**
     * Hàm dọn dẹp khi plugin tắt.
     */
    @Override
    public void onDisable() {
        log.info("Đang tắt Blogin...");
        if (timeoutTask != null) timeoutTask.cancel();

        // Đăng xuất tất cả người chơi đang online (chỉ xóa session)
        if (sessionManager != null) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                sessionManager.logout(p.getUniqueId());
            }
        }

        if (databaseManager != null) databaseManager.close();
        log.info("✔ Blogin đã tắt an toàn");
    }

    /**
     * Đăng ký tất cả commands của plugin.
     */
    private void registerCommands() {
        getCommand("login").setExecutor(new LoginCommand(this));
        getCommand("register").setExecutor(new RegisterCommand(this));
        getCommand("logout").setExecutor(new LogoutCommand(this));
        getCommand("changepassword").setExecutor(new ChangePasswordCommand(this));
        getCommand("blogin").setExecutor(new BloginAdminCommand(this));
        getCommand("captcha").setExecutor(new CaptchaCommand(this));
        getCommand("2fa").setExecutor(new TwoFactorCommand(this));
        getCommand("bloginwelcome").setExecutor(new WelcomeAdminCommand(this));
        log.info("✔ Đã đăng ký " + 8 + " commands");
    }

    /**
     * Đăng ký tất cả event listeners.
     */
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
        log.info("✔ Đã đăng ký 10 event listeners");
    }

    /**
     * Task chạy định kỳ kiểm tra timeout đăng nhập của người chơi.
     */
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
                    p.sendMessage(messageManager.get("tin-nhan.canh-bao-thoi-gian",
                            "&e⏰ Còn {time} giây để đăng nhập!", "time", String.valueOf(remain)));
                }
                if (remain <= 0) {
                    Component kickMsg = LegacyComponentSerializer.legacyAmpersand()
                            .deserialize(messageManager.get("tin-nhan.het-thoi-gian",
                                    "&c⏰ Hết thời gian đăng nhập!"));
                    p.kick(kickMsg);
                }
            }
        }, 20L, 20L);
    }

    /**
     * Tải lại cấu hình từ file.
     */
    public void reloadPluginConfig() {
        reloadConfig();
        messageManager.reload();
    }

    // ========== GETTERS ==========
    public static Blogin getInstance() { return instance; }
    public DatabaseManager getDatabaseManager() { return databaseManager; }
    public AuthManager getAuthManager() { return authManager; }
    public SessionManager getSessionManager() { return sessionManager; }
    public CaptchaManager getCaptchaManager() { return captchaManager; }
    public MessageManager getMessageManager() { return messageManager; }
    public HashingUtil getHashingUtil() { return hashingUtil; }
    public TOTPGenerator getTotpGenerator() { return totpGenerator; }
    public WelcomeManager getWelcomeManager() { return welcomeManager; }
    public Logger getLog() { return log; }
}
