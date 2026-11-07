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
            ChatUtil.send(sender, plugin.getMessageManager().get(
                    sender, "commands.chi-choi", "&cLệnh này chỉ dành cho người chơi!"));
            return true;
        }
        if (!plugin.getSessionManager().isAuthenticated(player.getUniqueId())) {
            ChatUtil.send(player, plugin.getMessageManager().get(
                    player, "commands.2fa.not-authenticated",
                    "&c⚠ Bạn cần đăng nhập trước!"));
            return true;
        }
        if (!plugin.getConfig().getBoolean("two-factor.bat", true)) {
            ChatUtil.send(player, plugin.getMessageManager().get(
                    player, "commands.2fa.off", "&c2FA hiện đang tắt trong cấu hình!"));
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

                ChatUtil.send(player, plugin.getMessageManager().get(
                        player, "commands.2fa.setup-header",
                        "&6&l═══════ Thiết lập 2FA &6&l═══════"));
                ChatUtil.send(player, plugin.getMessageManager().get(
                        player, "commands.2fa.setup-huong-dan",
                        "&eQuét mã QR hoặc nhập thủ công secret key vào Google Authenticator:"));
                ChatUtil.send(player, plugin.getMessageManager().get(
                        player, "commands.2fa.setup-secret", "&aSecret: &f{secret}", "secret", secret));
                ChatUtil.send(player, plugin.getMessageManager().get(
                        player, "commands.2fa.setup-buoc-2", "&eSau khi thêm, nhập mã hiện tại:"));
                ChatUtil.send(player, plugin.getMessageManager().get(
                        player, "commands.2fa.setup-lenh", "&a/2fa confirm <mã_{digits}_số>",
                        "digits", String.valueOf(digits)));
                ChatUtil.send(player, plugin.getMessageManager().get(
                        player, "commands.2fa.setup-otpauth", "&7URL otpauth: &f{url}", "url", otpAuthUrl));
                ChatUtil.send(player, plugin.getMessageManager().get(
                        player, "commands.2fa.setup-luu-y",
                        "&8Lưu ý: secret được lưu mã hoá AES-GCM trong database."));
                ChatUtil.send(player, plugin.getMessageManager().get(
                        player, "commands.2fa.setup-footer",
                        "&6&l═══════════════════════════════"));
            }
            case "confirm" -> {
                if (args.length < 2) {
                    ChatUtil.send(player, plugin.getMessageManager().get(
                            player, "commands.2fa.confirm-usage",
                            "&cSử dụng: /2fa confirm <mã_6_số>"));
                    return true;
                }
                boolean ok = plugin.getAuthManager().confirmEnable2FA(player.getUniqueId(), args[1]);
                if (ok) {
                    ChatUtil.send(player, plugin.getMessageManager().get(
                            player, "tin-nhan.bat-2fa-thanh-cong",
                            "&a✔ Đã bật 2FA thành công!"));
                } else {
                    ChatUtil.send(player, plugin.getMessageManager().get(
                            player, "tin-nhan.sai-2fa", "&c❌ Mã không đúng!"));
                }
            }
            case "disable", "off", "tat" -> {
                if (args.length < 2) {
                    ChatUtil.send(player, plugin.getMessageManager().get(
                            player, "commands.2fa.disable-usage",
                            "&cSử dụng: /2fa disable <mã_6_số_hiện_tại>"));
                    return true;
                }
                boolean ok = plugin.getAuthManager().disable2FA(player.getUniqueId(), args[1]);
                if (ok) {
                    ChatUtil.send(player, plugin.getMessageManager().get(
                            player, "tin-nhan.tat-2fa-thanh-cong", "&a✔ Đã tắt 2FA!"));
                } else {
                    ChatUtil.send(player, plugin.getMessageManager().get(
                            player, "tin-nhan.sai-2fa", "&c❌ Mã không đúng!"));
                }
            }
            case "status" -> {
                // Mot lan query duy nhat, co cache (tranh 2 lan query DB).
                var data = plugin.getAuthManager().getPlayerDAO().getPlayerData(player.getName(), true);
                boolean enabled = data != null && data.totpEnabled();
                ChatUtil.send(player, plugin.getMessageManager().get(
                        player, "commands.2fa.status-header",
                        "&6&l═══════ Trạng thái 2FA &6&l═══════"));
                ChatUtil.send(player, plugin.getMessageManager().get(
                                player, "commands.2fa.status-label", "&eTrạng thái: ")
                        + plugin.getMessageManager().get(
                                player,
                                enabled ? "commands.2fa.status-on" : "commands.2fa.status-off",
                                enabled ? "&a✔ Đang bật" : "&c✘ Đang tắt"));
                ChatUtil.send(player, plugin.getMessageManager().get(
                        player, "commands.2fa.status-footer",
                        "&6&l═══════════════════════════════"));
            }
            default -> sendHelp(player);
        }
        return true;
    }

    private void sendHelp(Player player) {
        ChatUtil.send(player, plugin.getMessageManager().get(
                player, "commands.2fa.help-header", "&6&l═══════ 2FA Commands &6&l═══════"));
        ChatUtil.send(player, plugin.getMessageManager().get(
                player, "commands.2fa.help-enable", "&e/2fa enable &7- Bật 2FA"));
        ChatUtil.send(player, plugin.getMessageManager().get(
                player, "commands.2fa.help-confirm", "&e/2fa confirm <code> &7- Xác nhận bật"));
        ChatUtil.send(player, plugin.getMessageManager().get(
                player, "commands.2fa.help-disable", "&e/2fa disable <code> &7- Tắt 2FA"));
        ChatUtil.send(player, plugin.getMessageManager().get(
                player, "commands.2fa.help-status", "&e/2fa status &7- Xem trạng thái"));
        ChatUtil.send(player, plugin.getMessageManager().get(
                player, "commands.2fa.help-footer", "&6&l═══════════════════════════════"));
    }
}
