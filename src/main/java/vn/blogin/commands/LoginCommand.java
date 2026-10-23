package vn.blogin.commands;

import vn.blogin.Blogin;
import vn.blogin.managers.AuthManager.AuthResult;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Lệnh {@code /login} - Đăng nhập vào tài khoản.
 *
 * <p>Cú pháp: {@code /login <mật_khẩu> [mã_2fa]}</p>
 *
 * @author BloginVN
 */
public class LoginCommand implements CommandExecutor {

    private final Blogin plugin;

    public LoginCommand(Blogin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Lệnh này chỉ dành cho người chơi!");
            return true;
        }

        // Kiểm tra đã đăng nhập chưa
        if (plugin.getSessionManager().isAuthenticated(player.getUniqueId())) {
            player.sendMessage(plugin.getMessageManager().get(
                    "tin-nhan.da-dang-nhap-roi", "&cBạn đã đăng nhập rồi!"));
            return true;
        }

        // Cần nhập mật khẩu
        if (args.length < 1) {
            player.sendMessage(plugin.getMessageManager().get(
                    "tin-nhan.su-dung-sai", "&cSử dụng: /login <mật_khẩu> [mã_2fa]"));
            return true;
        }

        String password = args[0];
        String totpCode = args.length >= 2 ? args[1] : null;
        String ip = player.getAddress() != null ? player.getAddress().getAddress().getHostAddress() : "unknown";

        // Kiểm tra captcha (nếu có)
        if (plugin.getCaptchaManager().isEnabled()
                && !player.hasPermission("blogin.bypass.captcha")) {
            String pendingCaptcha = plugin.getSessionManager().getPendingCaptcha(player.getUniqueId());
            if (pendingCaptcha != null) {
                // Cần nhập captcha: định dạng /login <captcha> <mật_khẩu>
                if (args.length < 2) {
                    player.sendMessage(plugin.getMessageManager().get(
                            "captcha.tin-nhan-captcha", "&e[Captcha] &fVui lòng nhập mã: &a&l{captcha}",
                            "captcha", pendingCaptcha));
                    return true;
                }
                // Đối số 0 là captcha, đối số 1 là password
                String captchaInput = args[0];
                password = args[1];
                totpCode = args.length >= 3 ? args[2] : null;

                if (!plugin.getSessionManager().verifyCaptcha(player.getUniqueId(), captchaInput)) {
                    player.sendMessage(plugin.getMessageManager().get(
                            "captcha.tin-nhan-sai", "&cMã captcha sai!"));
                    playSound(player, "ENTITY_VILLAGER_NO", 1.0f, 1.0f);
                    // Cấp captcha mới
                    String[] newCap = plugin.getCaptchaManager().generate();
                    plugin.getSessionManager().setPendingCaptcha(player.getUniqueId(), newCap[0], newCap[1]);
                    player.sendMessage(plugin.getMessageManager().get(
                            "captcha.tin-nhan-captcha", "&e[Captcha] &fVui lòng nhập mã: &a&l{captcha}",
                            "captcha", newCap[0]));
                    return true;
                }
            }
        }

        // Thực hiện đăng nhập
        AuthResult result = plugin.getAuthManager().login(
                player.getUniqueId(), player.getName(), password, totpCode, ip);

        switch (result) {
            case SUCCESS -> {
                plugin.getSessionManager().markAuthenticated(player.getUniqueId());
                player.sendMessage(plugin.getMessageManager().get(
                        "tin-nhan.da-dang-nhap", "&aĐăng nhập thành công!", "player", player.getName()));
                playSound(player, "ENTITY_PLAYER_LEVELUP", 1.0f, 1.5f);

                // Bỏ hiệu ứng hạn chế
                player.removePotionEffect(org.bukkit.potion.PotionEffectType.BLINDNESS);
                player.removePotionEffect(org.bukkit.potion.PotionEffectType.SLOW);
                player.removePotionEffect(org.bukkit.potion.PotionEffectType.JUMP_BOOST);
                player.setWalkSpeed(0.2f);
                player.setFlySpeed(0.1f);

                // Hiển thị lại người chơi này cho mọi người khác
                for (org.bukkit.entity.Player other : player.getServer().getOnlinePlayers()) {
                    if (!other.canSee(player)) {
                        other.showPlayer(plugin, player);
                    }
                }

                // Chạy hiệu ứng welcome khi đăng nhập thành công (rực rỡ)
                if (plugin.getConfig().getBoolean("welcome.bat", true)) {
                    plugin.getWelcomeManager().playLoginSuccessSequence(player);
                }
            }
            case WRONG_PASSWORD -> {
                int remain = plugin.getAuthManager().getRemainingAttempts(player.getName(), ip);
                player.sendMessage(plugin.getMessageManager().get(
                        "tin-nhan.sai-mat-khau", "&cMật khẩu sai!",
                        "remaining", String.valueOf(remain)));
                playSound(player, "ENTITY_VILLAGER_NO", 1.0f, 1.0f);
            }
            case ACCOUNT_LOCKED -> {
                long remain = plugin.getAuthManager().getLockRemainingSeconds(player.getName(), ip);
                player.sendMessage(plugin.getMessageManager().get(
                        "tin-nhan.bi-khoa", "&cTài khoản bị khóa!",
                        "time", String.valueOf(remain)));
                playSound(player, "ENTITY_VILLAGER_NO", 1.0f, 0.5f);
            }
            case NOT_REGISTERED -> {
                player.sendMessage(plugin.getMessageManager().get(
                        "tin-nhan.chua-dang-ky", "&cBạn chưa đăng ký! Dùng /register"));
            }
            case TOTP_REQUIRED -> {
                player.sendMessage(plugin.getMessageManager().get(
                        "tin-nhan.can-2fa",
                        "&e🔐 Tài khoản này yêu cầu mã 2FA. Vui lòng nhập: /login <mật_khẩu> <mã_6_số>"));
            }
            case TOTP_INVALID -> {
                player.sendMessage(plugin.getMessageManager().get(
                        "tin-nhan.sai-2fa", "&cMã 2FA không đúng!"));
            }
            default -> player.sendMessage(plugin.getMessageManager().get(
                    "tin-nhan.loi-chung", "&cĐã xảy ra lỗi khi đăng nhập!"));
        }
        return true;
    }

    /**
     * Phát âm thanh nếu được bật trong config.
     */
    private void playSound(Player player, String sound, float volume, float pitch) {
        try {
            Sound s = Sound.valueOf(sound);
            player.playSound(player.getLocation(), s, volume, pitch);
        } catch (Exception ignored) {
            // Âm thanh không hợp lệ - bỏ qua
        }
    }
}
