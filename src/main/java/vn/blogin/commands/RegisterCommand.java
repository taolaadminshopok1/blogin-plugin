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
 * Lệnh {@code /register} - Đăng ký tài khoản mới.
 *
 * <p>Cú pháp:
 * <ul>
 *   <li>{@code /register <mật_khẩu> <xác_nhận_mật_khẩu>}</li>
 *   <li>Nếu captcha bật: {@code /register <captcha> <mật_khẩu> <xác_nhận_mật_khẩu>}</li>
 * </ul>
 *
 * @author BloginVN
 */
public class RegisterCommand implements CommandExecutor {

    private final Blogin plugin;

    public RegisterCommand(Blogin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Lệnh này chỉ dành cho người chơi!");
            return true;
        }

        if (plugin.getSessionManager().isAuthenticated(player.getUniqueId())) {
            player.sendMessage(plugin.getMessageManager().get(
                    "tin-nhan.da-dang-nhap-roi", "&cBạn đã đăng nhập rồi!"));
            return true;
        }

        String password;
        String confirmPassword;
        int startIdx = 0;

        // Nếu có captcha
        if (plugin.getCaptchaManager().isEnabled()
                && !player.hasPermission("blogin.bypass.captcha")) {
            String pendingCaptcha = plugin.getSessionManager().getPendingCaptcha(player.getUniqueId());
            if (pendingCaptcha != null) {
                if (args.length < 3) {
                    player.sendMessage(plugin.getMessageManager().get(
                            "captcha.tin-nhan-captcha", "&e[Captcha] &fVui lòng nhập mã: &a&l{captcha}",
                            "captcha", pendingCaptcha));
                    player.sendMessage("&cSử dụng: /register <captcha> <mật_khẩu> <xác_nhận_mật_khẩu>");
                    return true;
                }
                String captchaInput = args[0];
                if (!plugin.getSessionManager().verifyCaptcha(player.getUniqueId(), captchaInput)) {
                    player.sendMessage(plugin.getMessageManager().get(
                            "captcha.tin-nhan-sai", "&cMã captcha sai!"));
                    // Cấp captcha mới
                    String[] newCap = plugin.getCaptchaManager().generate();
                    plugin.getSessionManager().setPendingCaptcha(player.getUniqueId(), newCap[0], newCap[1]);
                    player.sendMessage(plugin.getMessageManager().get(
                            "captcha.tin-nhan-captcha", "&e[Captcha] &fVui lòng nhập mã: &a&l{captcha}",
                            "captcha", newCap[0]));
                    return true;
                }
                startIdx = 1;
            }
        }

        if (args.length < startIdx + 2) {
            player.sendMessage(plugin.getMessageManager().get(
                    "tin-nhan.su-dung-sai", "&cSử dụng: /register <mật_khẩu> <xác_nhận_mật_khẩu>"));
            return true;
        }

        password = args[startIdx];
        confirmPassword = args[startIdx + 1];

        // Xác nhận mật khẩu
        if (!password.equals(confirmPassword)) {
            player.sendMessage(plugin.getMessageManager().get(
                    "tin-nhan.mat-khau-khong-khop", "&cMật khẩu xác nhận không khớp!"));
            return true;
        }

        String ip = player.getAddress() != null
                ? player.getAddress().getAddress().getHostAddress() : "unknown";

        AuthResult result = plugin.getAuthManager().register(
                player.getUniqueId(), player.getName(), password, ip);

        switch (result) {
            case SUCCESS -> {
                plugin.getSessionManager().markAuthenticated(player.getUniqueId());
                player.sendMessage(plugin.getMessageManager().get(
                        "tin-nhan.dang-ky-thanh-cong",
                        "&a✔ Đăng ký thành công! Bạn đã được đăng nhập tự động."));
                try {
                    player.playSound(player.getLocation(),
                            Sound.valueOf("UI_TOAST_CHALLENGE_COMPLETE"), 1.0f, 1.0f);
                } catch (Exception ignored) { }

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

                // Chạy hiệu ứng welcome rực rỡ cho thành viên mới
                if (plugin.getConfig().getBoolean("welcome.bat", true)) {
                    plugin.getWelcomeManager().playRegisterSuccessSequence(player);
                }
            }
            case ALREADY_REGISTERED -> player.sendMessage(plugin.getMessageManager().get(
                    "tin-nhan.tai-khoan-da-ton-tai",
                    "&cTài khoản đã tồn tại! Hãy dùng /login."));
            case INVALID_PASSWORD_FORMAT -> {
                int min = plugin.getConfig().getInt("bao-mat.mat-khau-toi-thieu", 6);
                int max = plugin.getConfig().getInt("bao-mat.mat-khau-toi-da", 64);
                if (password.length() < min) {
                    player.sendMessage(plugin.getMessageManager().get(
                            "tin-nhan.mat-khau-qua-ngan",
                            "&cMật khẩu quá ngắn!", "min", String.valueOf(min)));
                } else if (password.length() > max) {
                    player.sendMessage(plugin.getMessageManager().get(
                            "tin-nhan.mat-khau-qua-dai",
                            "&cMật khẩu quá dài!", "max", String.valueOf(max)));
                } else {
                    player.sendMessage(plugin.getMessageManager().get(
                            "tin-nhan.mat-khau-yeu",
                            "&cMật khẩu phải có chữ hoa, chữ thường và số!"));
                }
            }
            default -> player.sendMessage(plugin.getMessageManager().get(
                    "tin-nhan.loi-chung", "&cĐã xảy ra lỗi khi đăng ký!"));
        }
        return true;
    }
}
