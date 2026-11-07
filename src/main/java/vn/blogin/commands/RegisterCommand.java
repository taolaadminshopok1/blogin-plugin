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

public class RegisterCommand implements CommandExecutor {
    private final Blogin plugin;

    public RegisterCommand(Blogin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd,
                             String label, String[] args) {
        if (!(sender instanceof Player player)) {
            ChatUtil.send(sender, plugin.getMessageManager().get(
                    sender, "commands.chi-choi", "&cLệnh này chỉ dành cho người chơi!"));
            return true;
        }
        if (plugin.getSessionManager().isAuthenticated(player.getUniqueId())) {
            ChatUtil.send(player, plugin.getMessageManager().get(
                    player, "tin-nhan.da-dang-nhap-roi", "&cBạn đã đăng nhập rồi!"));
            return true;
        }
        String password;
        String confirmPassword;
        int startIdx = 0;
        if (plugin.getCaptchaManager().isEnabled()
                && !player.hasPermission("blogin.bypass.captcha")) {
            String pendingCaptcha = plugin.getSessionManager().getPendingCaptcha(player.getUniqueId());
            if (pendingCaptcha != null) {
                if (args.length < 3) {
                    ChatUtil.send(player, plugin.getMessageManager().get(
                            player, "captcha.tin-nhan-captcha",
                            "&e[Captcha] &fVui lòng nhập mã: &a&l{captcha}", "captcha", pendingCaptcha));
                    ChatUtil.send(player, plugin.getMessageManager().get(
                            player, "commands.register.captcha-usage",
                            "&cSử dụng: /register <captcha> <mật khẩu> <xác nhận mật khẩu>"));
                    return true;
                }
                String captchaInput = args[0];
                if (!plugin.getSessionManager().verifyCaptcha(player.getUniqueId(), captchaInput)) {
                    ChatUtil.send(player, plugin.getMessageManager().get(
                            player, "captcha.tin-nhan-sai", "&c❌ Mã captcha sai!"));
                    String[] newCap = plugin.getCaptchaManager().generate();
                    plugin.getSessionManager().setPendingCaptcha(player.getUniqueId(), newCap[0], newCap[1]);
                    ChatUtil.send(player, plugin.getMessageManager().get(
                            player, "captcha.tin-nhan-captcha",
                            "&e[Captcha] &fVui lòng nhập mã: &a&l{captcha}", "captcha", newCap[0]));
                    return true;
                }
                startIdx = 1;
            }
        }
        if (args.length < startIdx + 2) {
            ChatUtil.send(player, plugin.getMessageManager().get(
                    player, "commands.register.usage",
                    "&cSử dụng: /register <mật khẩu> <xác nhận mật khẩu>"));
            return true;
        }
        password = args[startIdx];
        confirmPassword = args[startIdx + 1];
        ChatUtil.send(player, plugin.getMessageManager().get(
                        player, "misc.do-ma-ngau.label", "&8» &7Độ mạnh: ")
                + PasswordStrength.bar(plugin, password));
        if (!password.equals(confirmPassword)) {
            ChatUtil.send(player, plugin.getMessageManager().get(
                    player, "tin-nhan.mat-khau-khong-khop", "&c❌ Mật khẩu xác nhận không khớp!"));
            return true;
        }
        String ip = player.getAddress() != null
                ? player.getAddress().getAddress().getHostAddress() : "unknown";
        plugin.getAuthManager().registerAsync(player.getUniqueId(), player.getName(), password, ip,
                result -> handleResult(player, result, password));
        return true;
    }

    private void handleResult(Player player, AuthResult result, String password) {
        if (!player.isOnline()) return;
        switch (result) {
            case SUCCESS -> {
                plugin.getSessionManager().markAuthenticated(player.getUniqueId());
                ChatUtil.send(player, plugin.getMessageManager().get(
                        player, "tin-nhan.dang-ky-thanh-cong",
                        "&a✔ Đăng ký tài khoản thành công! Bạn đã được đăng nhập tự động."));
                SoundUtil.play(plugin, player, "dang-ky-thanh-cong",
                        Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                PlayerRestrictionUtil.release(plugin, player);
                if (plugin.getConfig().getBoolean("welcome.bat", true)) {
                    plugin.getWelcomeManager().playRegisterSuccessSequence(player);
                }
            }
            case ALREADY_REGISTERED -> ChatUtil.send(player, plugin.getMessageManager().get(
                    player, "tin-nhan.tai-khoan-da-ton-tai",
                    "&c❌ Tài khoản đã tồn tại! Hãy dùng /login."));
            case INVALID_PASSWORD_FORMAT -> {
                int min = plugin.getConfig().getInt("bao-mat.mat-khau-toi-thieu", 6);
                int max = plugin.getConfig().getInt("bao-mat.mat-khau-toi-da", 64);
                if (password.length() < min) {
                    ChatUtil.send(player, plugin.getMessageManager().get(
                            player, "tin-nhan.mat-khau-qua-ngan",
                            "&c❌ Mật khẩu phải có ít nhất {min} ký tự!", "min", String.valueOf(min)));
                } else if (password.length() > max) {
                    ChatUtil.send(player, plugin.getMessageManager().get(
                            player, "tin-nhan.mat-khau-qua-dai",
                            "&c❌ Mật khẩu không được vượt quá {max} ký tự!", "max", String.valueOf(max)));
                } else {
                    ChatUtil.send(player, plugin.getMessageManager().get(
                            player, "tin-nhan.mat-khau-yeu",
                            "&c❌ Mật khẩu phải có chữ hoa, chữ thường và số!"));
                }
            }
            case WEAK_PASSWORD -> {
                ChatUtil.send(player, plugin.getMessageManager().get(
                        player, "tin-nhan.mat-khau-yeu",
                        "&c❌ Mật khẩu quá yếu! Hãy dùng mật khẩu mạnh hơn."));
                ChatUtil.send(player, plugin.getMessageManager().get(
                                player, "misc.do-ma-ngau.label-hien-tai",
                                "&8» &7Độ mạnh hiện tại: ")
                        + PasswordStrength.bar(plugin, password));
                ChatUtil.send(player, plugin.getMessageManager().get(
                        player, "misc.goi-y.manh",
                        "&7Gợi ý: kết hợp chữ hoa, chữ thường, số & ký tự đặc biệt, tối thiểu 8 ký tự."));
            }
            default -> ChatUtil.send(player, plugin.getMessageManager().get(
                    player, "tin-nhan.loi-chung", "&c✖ Đã xảy ra lỗi khi đăng ký!"));
        }
    }
}
