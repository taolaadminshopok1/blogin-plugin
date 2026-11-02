package vn.blogin.commands;
import vn.blogin.Blogin;
import vn.blogin.database.PlayerDAO;
import vn.blogin.database.PlayerDAO.PlayerData;
import vn.blogin.utils.ChatUtil;
import vn.blogin.utils.PlayerRestrictionUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
public class BloginAdminCommand implements CommandExecutor {
    private final Blogin plugin;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    public BloginAdminCommand(Blogin plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd,
                             String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        if (!sender.hasPermission("blogin.admin")
                && !args[0].equalsIgnoreCase("gui")) {
            ChatUtil.send(sender, "&c❌ Bạn không có quyền sử dụng lệnh này!");
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "reload" -> {
                plugin.reloadPluginConfig();
                ChatUtil.send(sender, plugin.getMessageManager().get(
                        "admin.reload-thanh-cong", "&a✔ Đã tải lại cấu hình Blogin!"));
            }
            case "unregister", "delete" -> {
                if (args.length < 2) {
                    ChatUtil.send(sender, "&cSử dụng: /blogin unregister <player>");
                    return true;
                }
                String target = args[1];
                PlayerDAO dao = plugin.getAuthManager().getPlayerDAO();
                boolean ok = dao.deletePlayer(target);
                if (ok) {
                    ChatUtil.send(sender, plugin.getMessageManager().get(
                            "admin.unregister-thanh-cong",
                            "&a✔ Đã xóa tài khoản của {player}!", "player", target));
                    Player online = Bukkit.getPlayerExact(target);
                    if (online != null) {
                        plugin.getSessionManager().logout(online.getUniqueId());
                        Component kickMsg = ChatUtil.parse("&cTài khoản của bạn đã bị admin xóa!");
                        online.kick(kickMsg);
                    }
                } else {
                    ChatUtil.send(sender, "&cKhông tìm thấy người chơi: " + target);
                }
            }
            case "info" -> {
                if (args.length < 2) {
                    ChatUtil.send(sender, "&cSử dụng: /blogin info <player>");
                    return true;
                }
                PlayerData data = plugin.getAuthManager().getPlayerDAO().getPlayerData(args[1]);
                if (data == null) {
                    ChatUtil.send(sender, "&cKhông tìm thấy người chơi: " + args[1]);
                    return true;
                }
                showPlayerInfo(sender, data);
            }
            case "force", "forcelogin" -> {
                if (args.length < 2) {
                    ChatUtil.send(sender, "&cSử dụng: /blogin force <player>");
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    ChatUtil.send(sender, "&cNgười chơi không online: " + args[1]);
                    return true;
                }
                plugin.getSessionManager().markAuthenticated(target.getUniqueId());
                PlayerRestrictionUtil.release(plugin, target);
                ChatUtil.send(sender, plugin.getMessageManager().get(
                        "admin.force-login", "&a✔ Đã ép buộc đăng nhập cho {player}!",
                        "player", target.getName()));
                ChatUtil.send(target, plugin.getMessageManager().get(
                        "tin-nhan.da-dang-nhap", "&a✔ Bạn đã được admin đăng nhập!"));
            }
            case "unlock" -> {
                if (args.length < 2) {
                    ChatUtil.send(sender, "&cSử dụng: /blogin unlock <player>");
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                String ip = target != null && target.getAddress() != null
                        ? target.getAddress().getAddress().getHostAddress() : "0.0.0.0";
                plugin.getAuthManager().adminUnlock(args[1], ip);
                ChatUtil.send(sender, "&a✔ Đã mở khóa tài khoản: " + args[1]);
            }
            case "reset", "resetpassword" -> {
                if (args.length < 3) {
                    ChatUtil.send(sender, "&cSử dụng: /blogin reset <player> <mật_khẩu_mới>");
                    return true;
                }
                boolean ok = plugin.getAuthManager().adminResetPassword(args[1], args[2]);
                if (ok) {
                    ChatUtil.send(sender, "&a✔ Đã reset mật khẩu cho &e" + args[1]);
                    Player online = Bukkit.getPlayerExact(args[1]);
                    if (online != null) {
                        plugin.getSessionManager().logout(online.getUniqueId());
                        ChatUtil.send(online, "&eMật khẩu của bạn đã được admin reset, hãy đăng nhập lại.");
                    }
                } else {
                    ChatUtil.send(sender, "&cReset thất bại (mật khẩu không đạt yêu cầu hoặc không tìm thấy tài khoản).");
                }
            }
            case "2fa" -> {
                if (args.length < 3) {
                    ChatUtil.send(sender, "&cSử dụng: /blogin 2fa <player> <on|off|secret>");
                    return true;
                }
                handle2FA(sender, args[1], args[2]);
            }
            case "gui" -> {
                if (!(sender instanceof Player player)) {
                    ChatUtil.send(sender, "&cLệnh này chỉ dành cho người chơi!");
                    return true;
                }
                if (plugin.getSessionManager().isAuthenticated(player.getUniqueId())) {
                    ChatUtil.send(sender, "&cBạn đã đăng nhập rồi!");
                    return true;
                }
                if (plugin.getConfig().getBoolean("gui.bat", true)) {
                    plugin.getLoginGui().open(player);
                } else {
                    ChatUtil.send(sender, "&cGUI đăng nhập đang tắt trong cấu hình!");
                }
            }
            case "list", "ds" -> {
                int page = args.length >= 2 ? safeParse(args[1], 1) : 1;
                showPlayerList(sender, page);
            }
            case "status" -> showStatus(sender);
            case "help" -> sendHelp(sender);
            default -> sendHelp(sender);
        }
        return true;
    }
    private void showPlayerInfo(CommandSender sender, PlayerData data) {
        ChatUtil.send(sender, plugin.getMessageManager().get(
                "admin.info-header", "&6&l═══════ Thông tin {player} ═══════",
                "player", data.name()));
        ChatUtil.send(sender, plugin.getMessageManager().get(
                "admin.info-uuid", "&eUUID: &f{uuid}", "uuid", data.uuid().toString()));
        ChatUtil.send(sender, plugin.getMessageManager().get(
                "admin.info-status", "&eTrạng thái: {status}",
                "status", getStatusText(data.name())));
        ChatUtil.send(sender, plugin.getMessageManager().get(
                "admin.info-2fa", "&e2FA: {status}",
                "status", data.totpEnabled()
                        ? plugin.getMessageManager().get("admin.bat-2fa", "&aBật")
                        : plugin.getMessageManager().get("admin.tat-2fa", "&cTắt")));
        ChatUtil.send(sender, plugin.getMessageManager().get(
                "admin.info-ip", "&eIP đã đăng ký: &f{ip}", "ip",
                data.lastIp() == null ? "Chưa có" : data.lastIp()));
        ChatUtil.send(sender, plugin.getMessageManager().get(
                "admin.info-last-login", "&eLần đăng nhập cuối: &f{time}", "time",
                data.lastLogin() == 0 ? "Chưa đăng nhập"
                        : dateFormat.format(new Date(data.lastLogin()))));
        ChatUtil.send(sender, plugin.getMessageManager().get(
                "admin.info-login-count", "&eTổng lần đăng nhập: &f{count}",
                "count", String.valueOf(data.loginCount())));
        ChatUtil.send(sender, plugin.getMessageManager().get(
                "admin.info-registered", "&eNgày đăng ký: &f{time}", "time",
                dateFormat.format(new Date(data.registrationDate()))));
        ChatUtil.send(sender, plugin.getMessageManager().get(
                "admin.info-footer", "&6&l═══════════════════════════════"));
    }
    private String getStatusText(String name) {
        Player online = Bukkit.getPlayerExact(name);
        boolean authenticated = online != null
                && plugin.getSessionManager().isAuthenticated(online.getUniqueId());
        return plugin.getMessageManager().get(
                authenticated ? "admin.status-on" : "admin.status-off",
                authenticated ? "&a✔ Đã đăng nhập" : "&c✘ Chưa đăng nhập");
    }
    private void handle2FA(CommandSender sender, String name, String action) {
        PlayerDAO dao = plugin.getAuthManager().getPlayerDAO();
        PlayerData data = dao.getPlayerData(name);
        if (data == null) {
            ChatUtil.send(sender, "&cKhông tìm thấy người chơi: " + name);
            return;
        }
        switch (action.toLowerCase()) {
            case "on", "enable" -> {
                dao.setTotpEnabled(data.uuid(), true);
                ChatUtil.send(sender, "&a✔ Đã bật 2FA cho &e" + data.name());
            }
            case "off", "disable" -> {
                dao.setTotpEnabled(data.uuid(), false);
                ChatUtil.send(sender, "&a✔ Đã tắt 2FA cho &e" + data.name());
            }
            case "secret" -> {
                String secret = plugin.getAuthManager().getDecryptedTotpSecret(data.uuid());
                if (secret == null) {
                    ChatUtil.send(sender, "&cNgười chơi chưa thiết lập 2FA.");
                } else {
                    ChatUtil.send(sender, "&eSecret 2FA của &b" + data.name() + "&e: &f" + secret);
                }
            }
            default -> ChatUtil.send(sender, "&cSử dụng: /blogin 2fa <player> <on|off|secret>");
        }
    }
    private void showPlayerList(CommandSender sender, int page) {
        int perPage = 10;
        int total = plugin.getAuthManager().getPlayerDAO().countPlayers();
        int maxPage = Math.max(1, (total + perPage - 1) / perPage);
        int current = Math.max(1, Math.min(page, maxPage));
        List<PlayerData> list = plugin.getAuthManager().getPlayerDAO()
                .listPlayers(perPage, (current - 1) * perPage);
        ChatUtil.send(sender, "&6&l═══════ Danh sách tài khoản &7(" + current + "/" + maxPage + ") &6&l═══════");
        if (list.isEmpty()) {
            ChatUtil.send(sender, "&8Không có tài khoản nào.");
            return;
        }
        for (PlayerData d : list) {
            String last = d.lastLogin() == 0 ? "Chưa đăng nhập" : dateFormat.format(new Date(d.lastLogin()));
            ChatUtil.send(sender, "&8• &f" + d.name() + " &8| &7Đăng nhập: &f" + d.loginCount()
                    + " &8| &7" + last + (d.totpEnabled() ? " &a[2FA]" : ""));
        }
        ChatUtil.send(sender, "&7Trang tiếp: &f/blogin list " + (current + 1));
    }
    private void showStatus(CommandSender sender) {
        PlayerDAO dao = plugin.getAuthManager().getPlayerDAO();
        ChatUtil.send(sender, "&6&l═══════ Blogin Status &6&l═══════");
        ChatUtil.send(sender, "&ePhiên bản: &f" + plugin.getDescription().getVersion());
        ChatUtil.send(sender, "&eDatabase: &f" + plugin.getDatabaseManager().getType()
                + " &8(pool &f" + plugin.getDatabaseManager().getPoolSize() + "&8)");
        ChatUtil.send(sender, "&eTài khoản đã đăng ký: &f" + dao.countPlayers());
        ChatUtil.send(sender, "&eTài khoản bật 2FA: &f" + dao.countTotpEnabled());
        ChatUtil.send(sender, "&eĐăng nhập 24h gần nhất: &f" + dao.countRecentLogins(24));
        ChatUtil.send(sender, "&eTổng lần đăng nhập: &f" + dao.totalLoginCount());
        ChatUtil.send(sender, "&eNgười chơi online: &f" + Bukkit.getOnlinePlayers().size());
        ChatUtil.send(sender, "&eĐã đăng nhập: &f" + countAuthenticated());
        ChatUtil.send(sender, "&eCaptcha: &f" +
                (plugin.getCaptchaManager().isEnabled() ? "&aBật" : "&cTắt"));
        ChatUtil.send(sender, "&e2FA: &f" +
                (plugin.getConfig().getBoolean("two-factor.bat", true) ? "&aBật" : "&cTắt"));
        ChatUtil.send(sender, "&eBCrypt cost: &f" + plugin.getHashingUtil().getCostFactor());
        ChatUtil.send(sender, "&6&l════════════════════════════════");
    }
    private int countAuthenticated() {
        int count = 0;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (plugin.getSessionManager().isAuthenticated(p.getUniqueId())) count++;
        }
        return count;
    }
    private int safeParse(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    private void sendHelp(CommandSender sender) {
        ChatUtil.send(sender, "&6&l═══════ Blogin Admin Commands &6&l═══════");
        ChatUtil.send(sender, "&e/blogin reload &7- Tải lại cấu hình");
        ChatUtil.send(sender, "&e/blogin unregister <player> &7- Xóa tài khoản");
        ChatUtil.send(sender, "&e/blogin info <player> &7- Xem thông tin");
        ChatUtil.send(sender, "&e/blogin force <player> &7- Ép buộc đăng nhập");
        ChatUtil.send(sender, "&e/blogin unlock <player> &7- Mở khóa tài khoản");
        ChatUtil.send(sender, "&e/blogin reset <player> <mk> &7- Reset mật khẩu");
        ChatUtil.send(sender, "&e/blogin 2fa <player> <on|off|secret> &7- Quản lý 2FA");
        ChatUtil.send(sender, "&e/blogin list [trang] &7- Danh sách tài khoản");
        ChatUtil.send(sender, "&e/blogin status &7- Trạng thái server");
        ChatUtil.send(sender, "&e/blogin gui &7- Mở GUI đăng nhập");
        ChatUtil.send(sender, "&6&l════════════════════════════════");
    }
}
