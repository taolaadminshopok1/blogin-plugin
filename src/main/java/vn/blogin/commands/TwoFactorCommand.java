package vn.blogin.commands;

import vn.blogin.Blogin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Lệnh {@code /2fa} - Quản lý xác thực 2 yếu tố.
 *
 * <p>Sub-commands:
 * <ul>
 *   <li>{@code /2fa enable} - Bật 2FA cho tài khoản</li>
 *   <li>{@code /2fa confirm <code>} - Xác nhận bật 2FA bằng mã đầu tiên</li>
 *   <li>{@code /2fa disable <code>} - Tắt 2FA (yêu cầu mã hiện tại)</li>
 * </ul>
 *
 * @author BloginVN
 */
public class TwoFactorCommand implements CommandExecutor {

    private final Blogin plugin;

    public TwoFactorCommand(Blogin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Lệnh này chỉ dành cho người chơi!");
            return true;
        }

        if (!plugin.getSessionManager().isAuthenticated(player.getUniqueId())) {
            player.sendMessage(plugin.getMessageManager().get(
                    "tin-nhan.chua-dang-nhap", "&cBạn cần đăng nhập trước!"));
            return true;
        }

        if (!plugin.getConfig().getBoolean("two-factor.bat", true)) {
            player.sendMessage("&c2FA hiện đang tắt trong cấu hình!");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "enable", "on", "bat" -> {
                String secret = plugin.getAuthManager().enable2FA(player.getUniqueId());
                plugin.getSessionManager().setTotpPendingSecret(player.getUniqueId(), secret);

                String issuer = plugin.getConfig().getString("two-factor.ten-issuer", "BloginVN");
                String otpAuthUrl = plugin.getTotpGenerator().getOtpAuthUrl(secret, player.getName(), issuer);

                player.sendMessage("&6&l═══════ Thiết lập 2FA ═══════");
                player.sendMessage("&eQuét mã QR hoặc nhập thủ công secret key vào Google Authenticator:");
                player.sendMessage("&aSecret: &f" + secret);
                player.sendMessage("&eSau khi thêm, nhập mã 6 số hiện tại:");
                player.sendMessage("&a/2fa confirm <mã_6_số>");
                player.sendMessage("&7URL otpauth: &f" + otpAuthUrl);
                player.sendMessage("&6═══════════════════════════════");
            }
            case "confirm" -> {
                if (args.length < 2) {
                    player.sendMessage("&cSử dụng: /2fa confirm <mã_6_số>");
                    return true;
                }
                boolean ok = plugin.getAuthManager().confirmEnable2FA(player.getUniqueId(), args[1]);
                if (ok) {
                    player.sendMessage(plugin.getMessageManager().get(
                            "tin-nhan.bat-2fa-thanh-cong",
                            "&a✔ Đã bật 2FA thành công!"));
                } else {
                    player.sendMessage(plugin.getMessageManager().get(
                            "tin-nhan.sai-2fa", "&cMã không đúng!"));
                }
            }
            case "disable", "off", "tat" -> {
                if (args.length < 2) {
                    player.sendMessage("&cSử dụng: /2fa disable <mã_6_số_hiện_tại>");
                    return true;
                }
                boolean ok = plugin.getAuthManager().disable2FA(player.getUniqueId(), args[1]);
                if (ok) {
                    player.sendMessage(plugin.getMessageManager().get(
                            "tin-nhan.tat-2fa-thanh-cong", "&a✔ Đã tắt 2FA!"));
                } else {
                    player.sendMessage(plugin.getMessageManager().get(
                            "tin-nhan.sai-2fa", "&cMã không đúng!"));
                }
            }
            default -> sendHelp(player);
        }
        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage("&6&l═══════ 2FA Commands ═══════");
        player.sendMessage("&e/2fa enable &7- Bật 2FA");
        player.sendMessage("&e/2fa confirm <code> &7- Xác nhận bật");
        player.sendMessage("&e/2fa disable <code> &7- Tắt 2FA");
        player.sendMessage("&6═══════════════════════════════");
    }
}
