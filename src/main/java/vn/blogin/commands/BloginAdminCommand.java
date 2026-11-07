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
    private final LanguageCommand languageCommand;

    public BloginAdminCommand(Blogin plugin) {
        this.plugin = plugin;
        this.languageCommand = new LanguageCommand(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd,
                             String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        // /blogin language <ma> - dung cho moi nguoi choi (khong can quyen admin).
        if (args[0].equalsIgnoreCase("language") || args[0].equalsIgnoreCase("lang")) {
            return languageCommand.onCommand(sender, cmd, label, args);
        }
        if (!sender.hasPermission("blogin.admin")
                && !args[0].equalsIgnoreCase("gui")) {
            ChatUtil.send(sender, plugin.getMessageManager().get(
                    sender, "admin.quyen", "&c❌ Bạn không có quyền sử dụng lệnh này!"));
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "reload" -> {
                plugin.reloadPluginConfig();
                ChatUtil.send(sender, plugin.getMessageManager().get(
                        sender, "admin.reload-thanh-cong", "&a✔ Đã tải lại cấu hình Blogin!"));
            }
            case "unregister", "delete" -> {
                if (args.length < 2) {
                    ChatUtil.send(sender, plugin.getMessageManager().get(
                            sender, "admin.su-dung-unregister", "&cSử dụng: /blogin unregister <player>"));
                    return true;
                }
                String target = args[1];
                PlayerDAO dao = plugin.getAuthManager().getPlayerDAO();
                boolean ok = dao.deletePlayer(target);
                if (ok) {
                    ChatUtil.send(sender, plugin.getMessageManager().get(
                            sender, "admin.unregister-thanh-cong",
                            "&a✔ Đã xóa tài khoản của {player}!", "player", target));
                    Player online = Bukkit.getPlayerExact(target);
                    if (online != null) {
                        plugin.getSessionManager().logout(online.getUniqueId());
                        Component kickMsg = ChatUtil.parse(plugin.getMessageManager().get(
                                online, "tin-nhan.admin-xoa-tai-khoan",
                                "&cTài khoản của bạn đã bị admin xóa!"));
                        online.kick(kickMsg);
                    }
                } else {
                    ChatUtil.send(sender, plugin.getMessageManager().get(
                            sender, "admin.khong-tim-thay", "&cKhông tìm thấy người chơi: {player}",
                            "player", target));
                }
            }
            case "info" -> {
                if (args.length < 2) {
                    ChatUtil.send(sender, plugin.getMessageManager().get(
                            sender, "admin.su-dung-info", "&cSử dụng: /blogin info <player>"));
                    return true;
                }
                PlayerData data = plugin.getAuthManager().getPlayerDAO().getPlayerData(args[1], true);
                if (data == null) {
                    ChatUtil.send(sender, plugin.getMessageManager().get(
                            sender, "admin.khong-tim-thay", "&cKhông tìm thấy người chơi: {player}",
                            "player", args[1]));
                    return true;
                }
                showPlayerInfo(sender, data);
            }
            case "force", "forcelogin" -> {
                if (args.length < 2) {
                    ChatUtil.send(sender, plugin.getMessageManager().get(
                            sender, "admin.su-dung-force", "&cSử dụng: /blogin force <player>"));
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    ChatUtil.send(sender, plugin.getMessageManager().get(
                            sender, "admin.khong-online", "&cNgười chơi không online: {player}",
                            "player", args[1]));
                    return true;
                }
                plugin.getSessionManager().markAuthenticated(target.getUniqueId());
                PlayerRestrictionUtil.release(plugin, target);
                ChatUtil.send(sender, plugin.getMessageManager().get(
                        sender, "admin.force-login", "&a✔ Đã ép buộc đăng nhập cho {player}!",
                        "player", target.getName()));
                ChatUtil.send(target, plugin.getMessageManager().get(
                        target, "tin-nhan.da-dang-nhap", "&a✔ Bạn đã được admin đăng nhập!"));
            }
            case "unlock" -> {
                if (args.length < 2) {
                    ChatUtil.send(sender, plugin.getMessageManager().get(
                            sender, "admin.su-dung-unlock", "&cSử dụng: /blogin unlock <player>"));
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                String ip = target != null && target.getAddress() != null
                        ? target.getAddress().getAddress().getHostAddress() : "0.0.0.0";
                plugin.getAuthManager().adminUnlock(args[1], ip);
                ChatUtil.send(sender, plugin.getMessageManager().get(
                        sender, "admin.mo-khoa-thanh-cong", "&a✔ Đã mở khóa tài khoản: {player}",
                        "player", args[1]));
            }
            case "reset", "resetpassword" -> {
                if (args.length < 3) {
                    ChatUtil.send(sender, plugin.getMessageManager().get(
                            sender, "admin.su-dung-reset",
                            "&cSử dụng: /blogin reset <player> <mật khẩu_mới>"));
                    return true;
                }
                boolean ok = plugin.getAuthManager().adminResetPassword(args[1], args[2]);
                if (ok) {
                    ChatUtil.send(sender, plugin.getMessageManager().get(
                            sender, "admin.reset-thanh-cong", "&a✔ Đã reset mật khẩu cho &e{player}",
                            "player", args[1]));
                    Player online = Bukkit.getPlayerExact(args[1]);
                    if (online != null) {
                        plugin.getSessionManager().logout(online.getUniqueId());
                        ChatUtil.send(online, plugin.getMessageManager().get(
                                online, "tin-nhan.admin-reset-mat-khau",
                                "&eMật khẩu của bạn đã được admin reset, hãy đăng nhập lại."));
                    }
                } else {
                    ChatUtil.send(sender, plugin.getMessageManager().get(
                            sender, "admin.reset-that-bai",
                            "&cReset thất bại (mật khẩu không đạt yêu cầu hoặc không tìm thấy tài khoản)."));
                }
            }
            case "2fa" -> {
                if (args.length < 3) {
                    ChatUtil.send(sender, plugin.getMessageManager().get(
                            sender, "admin.su-dung-2fa",
                            "&cSử dụng: /blogin 2fa <player> <on|off|secret>"));
                    return true;
                }
                handle2FA(sender, args[1], args[2]);
            }
            case "gui" -> {
                if (!(sender instanceof Player player)) {
                    ChatUtil.send(sender, plugin.getMessageManager().get(
                            sender, "commands.chi-choi", "&cLệnh này chỉ dành cho người chơi!"));
                    return true;
                }
                if (plugin.getSessionManager().isAuthenticated(player.getUniqueId())) {
                    ChatUtil.send(sender, plugin.getMessageManager().get(
                            sender, "gui.hint.da-dang-nhap-roi", "&cBạn đã đăng nhập rồi!"));
                    return true;
                }
                if (plugin.getConfig().getBoolean("gui.bat", true)) {
                    plugin.getLoginGui().open(player);
                } else {
                    ChatUtil.send(sender, plugin.getMessageManager().get(
                            sender, "commands.gui.off",
                            "&cGUI đăng nhập đang tắt trong cấu hình!"));
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
                sender, "admin.info-header", "&6&l═══════ Thông tin {player} ═══════",
                "player", data.name()));
        ChatUtil.send(sender, plugin.getMessageManager().get(
                sender, "admin.info-uuid", "&eUUID: &f{uuid}", "uuid", data.uuid().toString()));
        ChatUtil.send(sender, plugin.getMessageManager().get(
                sender, "admin.info-status", "&eTrạng thái: {status}",
                "status", getStatusText(sender, data.name())));
        ChatUtil.send(sender, plugin.getMessageManager().get(
                sender, "admin.info-2fa", "&e2FA: {status}",
                "status", data.totpEnabled()
                        ? plugin.getMessageManager().get(sender, "admin.bat-2fa", "&aBật")
                        : plugin.getMessageManager().get(sender, "admin.tat-2fa", "&cTắt")));
        ChatUtil.send(sender, plugin.getMessageManager().get(
                sender, "admin.info-ip", "&eIP đã đăng ký: &f{ip}", "ip",
                data.lastIp() == null
                        ? plugin.getMessageManager().get(sender, "admin.chua-co-ip", "Chưa có")
                        : data.lastIp()));
        ChatUtil.send(sender, plugin.getMessageManager().get(
                sender, "admin.info-last-login", "&eLần đăng nhập cuối: &f{time}", "time",
                data.lastLogin() == 0
                        ? plugin.getMessageManager().get(sender, "admin.chua-dang-nhap", "Chưa đăng nhập")
                        : dateFormat.format(new Date(data.lastLogin()))));
        ChatUtil.send(sender, plugin.getMessageManager().get(
                sender, "admin.info-login-count", "&eTổng lần đăng nhập: &f{count}",
                "count", String.valueOf(data.loginCount())));
        ChatUtil.send(sender, plugin.getMessageManager().get(
                sender, "admin.info-registered", "&eNgày đăng ký: &f{time}", "time",
                dateFormat.format(new Date(data.registrationDate()))));
        ChatUtil.send(sender, plugin.getMessageManager().get(
                sender, "admin.info-footer", "&6&l═══════════════════════════════"));
    }

    private String getStatusText(CommandSender sender, String name) {
        Player online = Bukkit.getPlayerExact(name);
        boolean authenticated = online != null
                && plugin.getSessionManager().isAuthenticated(online.getUniqueId());
        return plugin.getMessageManager().get(
                sender,
                authenticated ? "admin.status-on" : "admin.status-off",
                authenticated ? "&a✔ Đã đăng nhập" : "&c✘ Chưa đăng nhập");
    }

    private void handle2FA(CommandSender sender, String name, String action) {
        PlayerDAO dao = plugin.getAuthManager().getPlayerDAO();
        PlayerData data = dao.getPlayerData(name, true);
        if (data == null) {
            ChatUtil.send(sender, plugin.getMessageManager().get(
                    sender, "admin.khong-tim-thay", "&cKhông tìm thấy người chơi: {player}",
                    "player", name));
            return;
        }
        switch (action.toLowerCase()) {
            case "on", "enable" -> {
                dao.setTotpEnabled(data.uuid(), true);
                ChatUtil.send(sender, plugin.getMessageManager().get(
                        sender, "admin.2fa-bat", "&a✔ Đã bật 2FA cho &e{player}",
                        "player", data.name()));
            }
            case "off", "disable" -> {
                dao.setTotpEnabled(data.uuid(), false);
                ChatUtil.send(sender, plugin.getMessageManager().get(
                        sender, "admin.2fa-tat", "&a✔ Đã tắt 2FA cho &e{player}",
                        "player", data.name()));
            }
            case "secret" -> {
                String secret = plugin.getAuthManager().getDecryptedTotpSecret(data.uuid());
                if (secret == null) {
                    ChatUtil.send(sender, plugin.getMessageManager().get(
                            sender, "admin.2fa-chua-thiet-lap",
                            "&cNgười chơi chưa thiết lập 2FA."));
                } else {
                    ChatUtil.send(sender, plugin.getMessageManager().get(
                            sender, "admin.2fa-secret", "&eSecret 2FA của &b{player}&e: &f{secret}",
                            "player", data.name(), "secret", secret));
                }
            }
            default -> ChatUtil.send(sender, plugin.getMessageManager().get(
                    sender, "admin.su-dung-2fa",
                    "&cSử dụng: /blogin 2fa <player> <on|off|secret>"));
        }
    }

    private void showPlayerList(CommandSender sender, int page) {
        int perPage = 10;
        int total = plugin.getAuthManager().getPlayerDAO().countPlayers();
        int maxPage = Math.max(1, (total + perPage - 1) / perPage);
        int current = Math.max(1, Math.min(page, maxPage));
        List<PlayerData> list = plugin.getAuthManager().getPlayerDAO()
                .listPlayers(perPage, (current - 1) * perPage);
        ChatUtil.send(sender, plugin.getMessageManager().get(
                sender, "admin.list-header",
                "&6&l═══════ Danh sách tài khoản &7({current}/{max}) &6&l═══════",
                "current", String.valueOf(current), "max", String.valueOf(maxPage)));
        if (list.isEmpty()) {
            ChatUtil.send(sender, plugin.getMessageManager().get(
                    sender, "admin.list-empty", "&8Không có tài khoản nào."));
            return;
        }
        for (PlayerData d : list) {
            String last = d.lastLogin() == 0
                    ? plugin.getMessageManager().get(sender, "admin.chua-dang-nhap", "Chưa đăng nhập")
                    : dateFormat.format(new Date(d.lastLogin()));
            ChatUtil.send(sender, plugin.getMessageManager().get(
                    sender, "admin.list-item",
                    "&8• &f{player} &8| &7Đăng nhập: &f{count} &8| &7{time}",
                    "player", d.name(),
                    "count", String.valueOf(d.loginCount()),
                    "time", last));
        }
        ChatUtil.send(sender, plugin.getMessageManager().get(
                sender, "admin.list-next", "&7Trang tiếp: &f/blogin list {page}",
                "page", String.valueOf(current + 1)));
    }

    private void showStatus(CommandSender sender) {
        PlayerDAO dao = plugin.getAuthManager().getPlayerDAO();
        ChatUtil.send(sender, plugin.getMessageManager().get(
                sender, "admin.status-header", "&6&l═══════ Blogin Status &6&l═══════"));
        ChatUtil.send(sender, plugin.getMessageManager().get(
                sender, "admin.status-version", "&ePhiên bản: &f{version}",
                "version", plugin.getDescription().getVersion()));
        ChatUtil.send(sender, plugin.getMessageManager().get(
                sender, "admin.status-database", "&eDatabase: &f{type} &8(pool &f{pool}&8)",
                "type", String.valueOf(plugin.getDatabaseManager().getType()),
                "pool", String.valueOf(plugin.getDatabaseManager().getPoolSize())));
        ChatUtil.send(sender, plugin.getMessageManager().get(
                sender, "admin.status-tai-khoan", "&eTài khoản đã đăng ký: &f{count}",
                "count", String.valueOf(dao.countPlayers())));
        ChatUtil.send(sender, plugin.getMessageManager().get(
                sender, "admin.status-2fa", "&eTài khoản bật 2FA: &f{count}",
                "count", String.valueOf(dao.countTotpEnabled())));
        ChatUtil.send(sender, plugin.getMessageManager().get(
                sender, "admin.status-dang-nhap-24h", "&eĐăng nhập 24h gần nhất: &f{count}",
                "count", String.valueOf(dao.countRecentLogins(24))));
        ChatUtil.send(sender, plugin.getMessageManager().get(
                sender, "admin.status-tong-dang-nhap", "&eTổng lần đăng nhập: &f{count}",
                "count", String.valueOf(dao.totalLoginCount())));
        ChatUtil.send(sender, plugin.getMessageManager().get(
                sender, "admin.status-online", "&eNgười chơi online: &f{count}",
                "count", String.valueOf(Bukkit.getOnlinePlayers().size())));
        ChatUtil.send(sender, plugin.getMessageManager().get(
                sender, "admin.status-da-dang-nhap", "&eĐã đăng nhập: &f{count}",
                "count", String.valueOf(countAuthenticated())));
        ChatUtil.send(sender, plugin.getMessageManager().get(
                sender, "admin.status-captcha", "&eCaptcha: &f{status}",
                "status", plugin.getMessageManager().get(
                        sender,
                        plugin.getCaptchaManager().isEnabled() ? "admin.bat" : "admin.tat",
                        plugin.getCaptchaManager().isEnabled() ? "&aBật" : "&cTắt")));
        ChatUtil.send(sender, plugin.getMessageManager().get(
                sender, "admin.status-2fa-bat", "&e2FA: &f{status}",
                "status", plugin.getMessageManager().get(
                        sender,
                        plugin.getConfig().getBoolean("two-factor.bat", true) ? "admin.bat" : "admin.tat",
                        plugin.getConfig().getBoolean("two-factor.bat", true) ? "&aBật" : "&cTắt")));
        ChatUtil.send(sender, plugin.getMessageManager().get(
                sender, "admin.status-bcrypt", "&eBCrypt cost: &f{count}",
                "count", String.valueOf(plugin.getHashingUtil().getCostFactor())));
        ChatUtil.send(sender, plugin.getMessageManager().get(
                sender, "admin.status-footer", "&6&l════════════════════════════════"));
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
        ChatUtil.send(sender, plugin.getMessageManager().get(
                sender, "admin.help-header", "&6&l═══════ Blogin Admin Commands &6&l═══════"));
        ChatUtil.send(sender, plugin.getMessageManager().get(
                sender, "admin.help-reload", "&e/blogin reload &7- Tải lại cấu hình"));
        ChatUtil.send(sender, plugin.getMessageManager().get(
                sender, "admin.help-unregister", "&e/blogin unregister <player> &7- Xóa tài khoản"));
        ChatUtil.send(sender, plugin.getMessageManager().get(
                sender, "admin.help-info", "&e/blogin info <player> &7- Xem thông tin"));
        ChatUtil.send(sender, plugin.getMessageManager().get(
                sender, "admin.help-force", "&e/blogin force <player> &7- Ép buộc đăng nhập"));
        ChatUtil.send(sender, plugin.getMessageManager().get(
                sender, "admin.help-unlock", "&e/blogin unlock <player> &7- Mở khóa tài khoản"));
        ChatUtil.send(sender, plugin.getMessageManager().get(
                sender, "admin.help-reset", "&e/blogin reset <player> <mk> &7- Reset mật khẩu"));
        ChatUtil.send(sender, plugin.getMessageManager().get(
                sender, "admin.help-2fa", "&e/blogin 2fa <player> <on|off|secret> &7- Quản lý 2FA"));
        ChatUtil.send(sender, plugin.getMessageManager().get(
                sender, "admin.help-list", "&e/blogin list [trang] &7- Danh sách tài khoản"));
        ChatUtil.send(sender, plugin.getMessageManager().get(
                sender, "admin.help-status", "&e/blogin status &7- Trạng thái server"));
        ChatUtil.send(sender, plugin.getMessageManager().get(
                sender, "admin.help-gui", "&e/blogin gui &7- Mở GUI đăng nhập"));
        ChatUtil.send(sender, plugin.getMessageManager().get(
                sender, "admin.help-language", "&e/blogin language <mã> &7- Đổi ngôn ngữ"));
        ChatUtil.send(sender, plugin.getMessageManager().get(
                sender, "admin.help-footer", "&6&l════════════════════════════════"));
    }
}
