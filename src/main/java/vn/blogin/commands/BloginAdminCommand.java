package vn.blogin.commands;

import vn.blogin.Blogin;
import vn.blogin.database.PlayerDAO;
import vn.blogin.database.PlayerDAO.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Lệnh admin {@code /blogin} - Quản trị plugin.
 *
 * <p>Sub-commands:
 * <ul>
 *   <li>{@code reload} - Tải lại config</li>
 *   <li>{@code unregister <player>} - Xóa tài khoản người chơi</li>
 *   <li>{@code info <player>} - Xem thông tin người chơi</li>
 *   <li>{@code force <player>} - Ép buộc đăng nhập</li>
 *   <li>{@code status} - Trạng thái server</li>
 *   <li>{@code unlock <player>} - Mở khóa tài khoản</li>
 * </ul>
 *
 * @author BloginVN
 */
public class BloginAdminCommand implements CommandExecutor {

    private final Blogin plugin;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    public BloginAdminCommand(Blogin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd,
                             @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("blogin.admin")) {
            sender.sendMessage("&cBạn không có quyền sử dụng lệnh này!");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                plugin.reloadPluginConfig();
                sender.sendMessage(plugin.getMessageManager().get(
                        "admin.reload-thanh-cong", "&a✔ Đã tải lại cấu hình!"));
            }
            case "unregister", "delete" -> {
                if (args.length < 2) {
                    sender.sendMessage("&cSử dụng: /blogin unregister <player>");
                    return true;
                }
                String target = args[1];
                PlayerDAO dao = plugin.getAuthManager().getPlayerDAO();
                boolean ok = dao.deletePlayer(target);
                if (ok) {
                    sender.sendMessage(plugin.getMessageManager().get(
                            "admin.unregister-thanh-cong",
                            "&a✔ Đã xóa tài khoản!",
                            "player", target));
                    Player online = Bukkit.getPlayerExact(target);
                    if (online != null) {
                        plugin.getSessionManager().logout(online.getUniqueId());
                        Component kickMsg = LegacyComponentSerializer.legacyAmpersand()
                                .deserialize("&cTài khoản của bạn đã bị admin xóa!");
                        online.kick(kickMsg);
                    }
                } else {
                    sender.sendMessage("&cKhông tìm thấy người chơi: " + target);
                }
            }
            case "info" -> {
                if (args.length < 2) {
                    sender.sendMessage("&cSử dụng: /blogin info <player>");
                    return true;
                }
                PlayerData data = plugin.getAuthManager().getPlayerDAO().getPlayerData(args[1]);
                if (data == null) {
                    sender.sendMessage("&cKhông tìm thấy người chơi: " + args[1]);
                    return true;
                }
                sender.sendMessage(plugin.getMessageManager().get(
                        "admin.info-header", "&6═══════ Thông tin {player} ═══════",
                        "player", data.name()));
                sender.sendMessage(plugin.getMessageManager().get(
                        "admin.info-uuid", "&eUUID: {uuid}", "uuid", data.uuid().toString()));
                sender.sendMessage(plugin.getMessageManager().get(
                        "admin.info-2fa", "&e2FA: {status}",
                        "status", data.totpEnabled()
                                ? plugin.getMessageManager().get("admin.bat-2fa", "&aBật")
                                : plugin.getMessageManager().get("admin.tat-2fa", "&cTắt")));
                sender.sendMessage(plugin.getMessageManager().get(
                        "admin.info-ip", "&eIP: {ip}", "ip",
                        data.lastIp() == null ? "Chưa có" : data.lastIp()));
                sender.sendMessage(plugin.getMessageManager().get(
                        "admin.info-last-login", "&eLần cuối: {time}", "time",
                        data.lastLogin() == 0 ? "Chưa đăng nhập"
                                : dateFormat.format(new Date(data.lastLogin()))));
                sender.sendMessage(plugin.getMessageManager().get(
                        "admin.info-login-count", "&eTổng lần đăng nhập: {count}",
                        "count", String.valueOf(data.loginCount())));
                sender.sendMessage(plugin.getMessageManager().get(
                        "admin.info-registered", "&eNgày đăng ký: {time}", "time",
                        dateFormat.format(new Date(data.registrationDate()))));
                sender.sendMessage(plugin.getMessageManager().get(
                        "admin.info-footer", "&6═══════════════════════════════"));
            }
            case "force", "forcelogin" -> {
                if (args.length < 2) {
                    sender.sendMessage("&cSử dụng: /blogin force <player>");
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    sender.sendMessage("&cNgười chơi không online: " + args[1]);
                    return true;
                }
                plugin.getSessionManager().markAuthenticated(target.getUniqueId());
                sender.sendMessage(plugin.getMessageManager().get(
                        "admin.force-login", "&a✔ Đã ép buộc đăng nhập!",
                        "player", target.getName()));
                target.sendMessage(plugin.getMessageManager().get(
                        "tin-nhan.da-dang-nhap", "&a✔ Bạn đã được admin đăng nhập!"));
            }
            case "unlock" -> {
                if (args.length < 2) {
                    sender.sendMessage("&cSử dụng: /blogin unlock <player>");
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                String ip = target != null && target.getAddress() != null
                        ? target.getAddress().getAddress().getHostAddress() : "0.0.0.0";
                plugin.getAuthManager().adminUnlock(args[1], ip);
                sender.sendMessage("&a✔ Đã mở khóa tài khoản: " + args[1]);
            }
            case "status" -> {
                sender.sendMessage("&6&l═══════ Blogin Status ═══════");
                sender.sendMessage("&ePhiên bản: &f" + plugin.getDescription().getVersion());
                sender.sendMessage("&eDatabase: &f" + plugin.getDatabaseManager().getType());
                sender.sendMessage("&eNgười chơi online: &f" + Bukkit.getOnlinePlayers().size());
                sender.sendMessage("&eĐã đăng nhập: &f" + countAuthenticated());
                sender.sendMessage("&eCaptcha: &f" +
                        (plugin.getCaptchaManager().isEnabled() ? "&aBật" : "&cTắt"));
                sender.sendMessage("&e2FA: &f" +
                        (plugin.getConfig().getBoolean("two-factor.bat", true) ? "&aBật" : "&cTắt"));
                sender.sendMessage("&6═══════════════════════════════");
            }
            case "help" -> sendHelp(sender);
            default -> sendHelp(sender);
        }
        return true;
    }

    private int countAuthenticated() {
        int count = 0;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (plugin.getSessionManager().isAuthenticated(p.getUniqueId())) count++;
        }
        return count;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("&6&l═══════ Blogin Admin Commands ═══════");
        sender.sendMessage("&e/blogin reload &7- Tải lại cấu hình");
        sender.sendMessage("&e/blogin unregister <player> &7- Xóa tài khoản");
        sender.sendMessage("&e/blogin info <player> &7- Xem thông tin");
        sender.sendMessage("&e/blogin force <player> &7- Ép buộc đăng nhập");
        sender.sendMessage("&e/blogin unlock <player> &7- Mở khóa tài khoản");
        sender.sendMessage("&e/blogin status &7- Trạng thái server");
        sender.sendMessage("&6═══════════════════════════════════════");
    }
}
