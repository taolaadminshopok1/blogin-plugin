package vn.blogin.listeners;

import vn.blogin.Blogin;
import vn.blogin.utils.ChatUtil;
import vn.blogin.utils.PlayerRestrictionUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import java.util.List;

public class PlayerJoinListener implements Listener {
    private final Blogin plugin;

    public PlayerJoinListener(Blogin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String ip = player.getAddress() != null
                ? player.getAddress().getAddress().getHostAddress() : "unknown";
        plugin.getSessionManager().createSession(player.getUniqueId(), ip);
        PlayerRestrictionUtil.apply(plugin, player);
        PlayerRestrictionUtil.hideFromOthers(plugin, player);
        plugin.getDatabaseManager().runAsync(
                () -> plugin.getAuthManager().getPlayerDAO().getPlayerData(player.getName()),
                data -> handleJoin(player, data)
        );
    }

    private void handleJoin(Player player, vn.blogin.database.PlayerDAO.PlayerData data) {
        if (!player.isOnline()) return;
        boolean registered = data != null;
        if (registered && tryAutoLoginByIp(player, data)) return;
        sendWelcomeMessage(player, registered);
        if (plugin.getCaptchaManager().isEnabled()) {
            String[] cap = plugin.getCaptchaManager().generate();
            plugin.getSessionManager().setPendingCaptcha(player.getUniqueId(), cap[0], cap[1]);
            ChatUtil.send(player, plugin.getMessageManager().get(
                    player, "captcha.tin-nhan-captcha",
                    "&e[Captcha] &fVui lòng nhập mã: &a&l{captcha}", "captcha", cap[0]));
        }
        if (plugin.getConfig().getBoolean("gui.bat", true)
                && plugin.getConfig().getBoolean("gui.auto-mo", true)) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline() && !plugin.getSessionManager().isAuthenticated(player.getUniqueId())) {
                    plugin.getLoginGui().open(player);
                }
            }, 20L);
        }
        if (plugin.getConfig().getBoolean("welcome.bat", true)) {
            plugin.getWelcomeManager().playWelcomeSequence(player, !registered);
        }
    }

    private boolean tryAutoLoginByIp(Player player, vn.blogin.database.PlayerDAO.PlayerData data) {
        if (!plugin.getConfig().getBoolean("ip-lock.bat", false)) return false;
        if (!plugin.getConfig().getBoolean("ip-lock.tu-dong-dang-nhap", true)) return false;
        if (data.totpEnabled()) return false;
        String currentIp = player.getAddress() != null
                ? player.getAddress().getAddress().getHostAddress() : null;
        if (currentIp == null || data.lastIp() == null) return false;
        if (!currentIp.equals(data.lastIp())) return false;
        plugin.getSessionManager().markAuthenticated(player.getUniqueId());
        plugin.getDatabaseManager().runAsync(
                () -> plugin.getAuthManager().getPlayerDAO().updateLoginInfo(player.getUniqueId(), currentIp),
                ok -> { }
        );
        PlayerRestrictionUtil.release(plugin, player);
        ChatUtil.send(player, plugin.getMessageManager().get(
                player, "tin-nhan.da-dang-nhap",
                "&a✔ Đăng nhập thành công! Chào mừng {player} trở lại!",
                "player", player.getName()));
        ChatUtil.send(player, plugin.getMessageManager().get(
                player, "tin-nhan.tu-dong-dang-nhap-ip",
                "&7🔒 &fĐăng nhập tự động — IP đã được xác thực."));
        if (plugin.getConfig().getBoolean("welcome.bat", true)) {
            plugin.getWelcomeManager().playLoginSuccessSequence(player);
        }
        return true;
    }

    private void sendWelcomeMessage(Player player, boolean registered) {
        List<String> intro = plugin.getMessageManager().getList(player, "tin-nhap");
        if (!intro.isEmpty()) {
            sendStaggeredIntro(player, intro);
        } else {
            ChatUtil.send(player, "");
            ChatUtil.send(player, "&6&l╔═══════════════════════════════════════╗");
            ChatUtil.send(player, "&6&l║        &e&l✦ &fBLOGIN &e&l✦        &6&l║");
            ChatUtil.send(player, "&6&l╚═══════════════════════════════════════╝");
            ChatUtil.send(player, "");
            ChatUtil.send(player, "  &fChào mừng &b&l{player}&f đến với server!",
                    "player", player.getName());
            ChatUtil.send(player, "");
        }
        if (registered) {
            ChatUtil.send(player, "  &a➤ &fĐăng nhập: &e/login <mật khẩu>");
            ChatUtil.send(player, "  &a➤ &fHoặc dùng GUI: &e/blogin gui");
        } else {
            ChatUtil.send(player, "  &a➤ &fĐăng ký: &e/register <mật khẩu> <xác nhận>");
        }
        if (plugin.getCaptchaManager().isEnabled()) {
            ChatUtil.send(player, "  &a➤ &fLấy captcha mới: &e/captcha");
        }
        ChatUtil.send(player, "");
        ChatUtil.send(player, "&8Vui lòng đăng nhập để chơi game! &8(Thời gian có hạn)");
        ChatUtil.send(player, "");
    }

    private void sendStaggeredIntro(Player player, List<String> messages) {
        for (int i = 0; i < messages.size(); i++) {
            final String line = plugin.getMessageManager()
                    .applyPlaceholders(messages.get(i), "player", player.getName());
            final long delay = i * 20L;
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (!player.isOnline()) return;
                if (plugin.getSessionManager().isAuthenticated(player.getUniqueId())) return;
                ChatUtil.send(player, line);
            }, delay);
        }
    }
}
