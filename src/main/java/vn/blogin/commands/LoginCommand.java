package vn.blogin.commands;
import vn.blogin.Blogin;
import vn.blogin.managers.AuthManager.AuthResult;
import vn.blogin.utils.ChatUtil;
import vn.blogin.utils.PasswordStrength;
import vn.blogin.utils.PlayerRestrictionUtil;
import vn.blogin.utils.SoundUtil;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
public class LoginCommand implements CommandExecutor {
    private final Blogin plugin;
    public LoginCommand(Blogin plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd,
                             String label, String[] args) {
        if (!(sender instanceof Player player)) {
            ChatUtil.send(sender, "&cLệnh này chỉ dành cho người chơi!");
            return true;
        }
        if (plugin.getSessionManager().isAuthenticated(player.getUniqueId())) {
            ChatUtil.send(player, plugin.getMessageManager().get(
                    "tin-nhan.da-dang-nhap-roi", "&cBạn đã đăng nhập rồi!"));
            return true;
        }
        String ip = getIp(player);
        String password;
        String totpCode = null;
        int startIdx = 0;
        if (plugin.getCaptchaManager().isEnabled()
                && !player.hasPermission("blogin.bypass.captcha")) {
            String pendingCaptcha = plugin.getSessionManager().getPendingCaptcha(player.getUniqueId());
            if (pendingCaptcha != null) {
                if (args.length < 2) {
                    ChatUtil.send(player, plugin.getMessageManager().get(
                            "captcha.tin-nhan-captcha", "&e[Captcha] &fVui lòng nhập mã: &a&l{captcha}",
                            "captcha", pendingCaptcha));
                    ChatUtil.send(player, "&7Cú pháp: &f/login <captcha> <mật khẩu> [mã 2FA]");
                    return true;
                }
                String captchaInput = args[0];
                if (!plugin.getSessionManager().verifyCaptcha(player.getUniqueId(), captchaInput)) {
                    ChatUtil.send(player, plugin.getMessageManager().get(
                            "captcha.tin-nhan-sai", "&c❌ Mã captcha sai! Dùng /captcha để lấy mã mới"));
                    playSound(player, "sai-mat-khau", Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    String[] newCap = plugin.getCaptchaManager().generate();
                    plugin.getSessionManager().setPendingCaptcha(player.getUniqueId(), newCap[0], newCap[1]);
                    ChatUtil.send(player, plugin.getMessageManager().get(
                            "captcha.tin-nhan-captcha", "&e[Captcha] &fVui lòng nhập mã: &a&l{captcha}",
                            "captcha", newCap[0]));
                    return true;
                }
                startIdx = 1;
            }
        }
        if (args.length < startIdx + 1) {
            ChatUtil.send(player, plugin.getMessageManager().get(
                    "tin-nhan.su-dung-sai", "&cSử dụng: /login <mật khẩu> [mã 2FA]"));
            return true;
        }
        password = args[startIdx];
        if (args.length >= startIdx + 2) {
            totpCode = args[startIdx + 1];
        }
        plugin.getAuthManager().loginAsync(player.getUniqueId(), player.getName(),
                password, totpCode, ip, result -> handleResult(player, result, password, ip));
        return true;
    }
    private void handleResult(Player player, AuthResult result, String password, String ip) {
        if (!player.isOnline()) return;
        switch (result) {
            case SUCCESS -> {
                plugin.getSessionManager().markAuthenticated(player.getUniqueId());
                ChatUtil.send(player, plugin.getMessageManager().get(
                        "tin-nhan.da-dang-nhap", "&a✔ Đăng nhập thành công! Chào mừng {player} trở lại!",
                        "player", player.getName()));
                playSound(player, "dang-nhap-thanh-cong", Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
                PlayerRestrictionUtil.release(plugin, player);
                if (plugin.getConfig().getBoolean("welcome.bat", true)) {
                    plugin.getWelcomeManager().playLoginSuccessSequence(player);
                }
                remindAdminToEnable2FA(player);
            }
            case WRONG_PASSWORD -> {
                plugin.getSessionManager().incrementFailedAttempts(player.getUniqueId());
                int remain = plugin.getAuthManager().getRemainingAttempts(player.getName(), ip);
                ChatUtil.send(player, plugin.getMessageManager().get(
                        "tin-nhan.sai-mat-khau", "&c❌ Mật khẩu không đúng! Còn {remaining} lần thử trước khi bị khóa.",
                        "remaining", String.valueOf(remain)));
                ChatUtil.send(player, "&8» &7Độ mạnh mật khẩu đã nhập: " + PasswordStrength.bar(password));
                playSound(player, "sai-mat-khau", Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            }
            case ACCOUNT_LOCKED -> {
                long remain = plugin.getAuthManager().getLockRemainingSeconds(player.getName(), ip);
                ChatUtil.send(player, plugin.getMessageManager().get(
                        "tin-nhan.bi-khoa", "&c⛔ Tài khoản của bạn đã bị khóa! Thử lại sau {time} giây.",
                        "time", String.valueOf(remain)));
                playSound(player, "sai-mat-khau", Sound.ENTITY_VILLAGER_NO, 1.0f, 0.5f);
            }
            case NOT_REGISTERED -> {
                ChatUtil.send(player, plugin.getMessageManager().get(
                        "tin-nhan.chua-dang-ky", "&c❌ Bạn chưa có tài khoản! Dùng /register <mật_khẩu> <mật_khẩu>."));
                ChatUtil.send(player, "&7Đăng ký ngay: &a/register <mật_khẩu> <xác_nhận_mật_khẩu>");
            }
            case TOTP_REQUIRED -> {
                plugin.getSessionManager().setAwaiting2FA(player.getUniqueId(), true);
                ChatUtil.send(player, plugin.getMessageManager().get(
                        "tin-nhan.can-2fa",
                        "&e🔐 Tài khoản này yêu cầu mã 2FA. Vui lòng nhập: /login <mật_khẩu> <mã_6_so>"));
            }
            case TOTP_INVALID -> {
                ChatUtil.send(player, plugin.getMessageManager().get(
                        "tin-nhan.sai-2fa", "&c❌ Mã 2FA không đúng!"));
                playSound(player, "sai-mat-khau", Sound.ENTITY_VILLAGER_NO, 1.0f, 0.8f);
            }
            case IP_BLOCKED -> {
                ChatUtil.send(player, plugin.getMessageManager().get(
                        "tin-nhan.ip-blocked", "&cIP của bạn đang bị tạm khóa, vui lòng thử lại sau!"));
            }
            default -> ChatUtil.send(player, plugin.getMessageManager().get(
                    "tin-nhan.loi-chung", "&c✖ Đã xảy ra lỗi khi đăng nhập!"));
        }
    }
    private void remindAdminToEnable2FA(Player player) {
        if (!plugin.getConfig().getBoolean("two-factor.bat-buoc-admin", true)) return;
        if (!plugin.getConfig().getBoolean("two-factor.bat", true)) return;
        if (!player.hasPermission("blogin.admin")) return;
        var session = plugin.getSessionManager().get(player.getUniqueId());
        if (session.reminded2FA) return;
        session.reminded2FA = true;
        plugin.getDatabaseManager().runAsync(
                () -> plugin.getAuthManager().getPlayerDAO().getPlayerData(player.getName()),
                data -> {
                    if (!player.isOnline() || data == null || data.totpEnabled()) return;
                    ChatUtil.send(player, "&e⏰ &7Bạn là admin — hãy bật 2FA để bảo vệ tài khoản: &a/2fa enable");
                }
        );
    }
    private String getIp(Player player) {
        return player.getAddress() != null
                ? player.getAddress().getAddress().getHostAddress() : "unknown";
    }
    private void playSound(Player player, String key, Sound fallback, float volume, float pitch) {
        SoundUtil.play(plugin, player, key, fallback, volume, pitch);
    }
}
