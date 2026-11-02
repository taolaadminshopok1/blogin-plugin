package vn.blogin.commands;
import vn.blogin.Blogin;
import vn.blogin.utils.ChatUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
public class TwoFactorCommand implements CommandExecutor {
    private final Blogin plugin;
    public TwoFactorCommand(Blogin plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd,
                             String label, String[] args) {
        if (!(sender instanceof Player player)) {
            ChatUtil.send(sender, "&cLệnh này chỉ dành cho người chơi!");
            return true;
        }
        if (!plugin.getSessionManager().isAuthenticated(player.getUniqueId())) {
            ChatUtil.send(player, plugin.getMessageManager().get(
                    "tin-nhan.chua-dang-nhap", "&c⚠ Bạn cần đăng nhập trước!"));
            return true;
        }
        if (!plugin.getConfig().getBoolean("two-factor.bat", true)) {
            ChatUtil.send(player, "&c2FA hiện đang tắt trong cấu hình!");
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
                int digits = plugin.getConfig().getInt("two-factor.so-chu-so", 6);
                int period = plugin.getConfig().getInt("two-factor.thoi-gian-hieu-luc", 30);
                String otpAuthUrl = plugin.getTotpGenerator()
                        .getOtpAuthUrl(secret, player.getName(), issuer, digits, period);
                ChatUtil.send(player, "&6&l═══════ Thiết lập 2FA &6&l═══════");
                ChatUtil.send(player, "&eQuét mã QR hoặc nhập thủ công secret key vào Google Authenticator:");
                ChatUtil.send(player, "&aSecret: &f" + secret);
                ChatUtil.send(player, "&eSau khi thêm, nhập mã hiện tại:");
                ChatUtil.send(player, "&a/2fa confirm <mã_" + digits + "_số>");
                ChatUtil.send(player, "&7URL otpauth: &f" + otpAuthUrl);
                ChatUtil.send(player, "&8Lưu ý: secret được lưu mã hoá AES-GCM trong database.");
                ChatUtil.send(player, "&6&l═══════════════════════════════");
            }
            case "confirm" -> {
                if (args.length < 2) {
                    ChatUtil.send(player, "&cSử dụng: /2fa confirm <mã_6_số>");
                    return true;
                }
                boolean ok = plugin.getAuthManager().confirmEnable2FA(player.getUniqueId(), args[1]);
                if (ok) {
                    ChatUtil.send(player, plugin.getMessageManager().get(
                            "tin-nhan.bat-2fa-thanh-cong",
                            "&a✔ Đã bật 2FA thành công!"));
                } else {
                    ChatUtil.send(player, plugin.getMessageManager().get(
                            "tin-nhan.sai-2fa", "&c❌ Mã không đúng!"));
                }
            }
            case "disable", "off", "tat" -> {
                if (args.length < 2) {
                    ChatUtil.send(player, "&cSử dụng: /2fa disable <mã_6_số_hiện_tại>");
                    return true;
                }
                boolean ok = plugin.getAuthManager().disable2FA(player.getUniqueId(), args[1]);
                if (ok) {
                    ChatUtil.send(player, plugin.getMessageManager().get(
                            "tin-nhan.tat-2fa-thanh-cong", "&a✔ Đã tắt 2FA!"));
                } else {
                    ChatUtil.send(player, plugin.getMessageManager().get(
                            "tin-nhan.sai-2fa", "&c❌ Mã không đúng!"));
                }
            }
            case "status" -> {
                boolean enabled = plugin.getAuthManager().getPlayerDAO()
                        .getPlayerData(player.getName()) != null
                        && plugin.getAuthManager().getPlayerDAO()
                        .getPlayerData(player.getName()).totpEnabled();
                ChatUtil.send(player, "&6&l═══════ Trạng thái 2FA &6&l═══════");
                ChatUtil.send(player, "&eTrạng thái: " + (enabled ? "&a✔ Đang bật" : "&c✘ Đang tắt"));
                ChatUtil.send(player, "&6&l═══════════════════════════════");
            }
            default -> sendHelp(player);
        }
        return true;
    }
    private void sendHelp(Player player) {
        ChatUtil.send(player, "&6&l═══════ 2FA Commands &6&l═══════");
        ChatUtil.send(player, "&e/2fa enable &7- Bật 2FA");
        ChatUtil.send(player, "&e/2fa confirm <code> &7- Xác nhận bật");
        ChatUtil.send(player, "&e/2fa disable <code> &7- Tắt 2FA");
        ChatUtil.send(player, "&e/2fa status &7- Xem trạng thái");
        ChatUtil.send(player, "&6&l═══════════════════════════════");
    }
}
