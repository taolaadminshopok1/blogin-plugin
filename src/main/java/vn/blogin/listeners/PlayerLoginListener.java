package vn.blogin.listeners;
import vn.blogin.Blogin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
public class PlayerLoginListener implements Listener {
    private final Blogin plugin;
    public PlayerLoginListener(Blogin plugin) {
        this.plugin = plugin;
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLogin(PlayerLoginEvent event) {
        if (event.getResult() != PlayerLoginEvent.Result.ALLOWED) return;
        Player player = event.getPlayer();
        String ip = event.getAddress() != null
                ? event.getAddress().getHostAddress() : "unknown";
        int maxPerIp = plugin.getConfig().getInt("gioi-han-ip", 3);
        if (maxPerIp > 0) {
            int current = plugin.getSessionManager().countActiveByIp(ip);
            if (current >= maxPerIp) {
                String message = plugin.getMessageManager().get(
                        "tin-nhan.qua-nhieu-lan-dang-nhap",
                        "&cIP của bạn đã đạt giới hạn {max} phiên đăng nhập đồng thời!",
                        "max", String.valueOf(maxPerIp));
                event.disallow(PlayerLoginEvent.Result.KICK_BANNED, message);
                plugin.getLog().warning("[Chặn IP] " + ip + " vượt giới hạn " + maxPerIp + " phiên");
                return;
            }
        }
        if (plugin.getConfig().getBoolean("ip-lock.bat", false)) {
            long lockMs = plugin.getConfig().getInt("bao-mat.thoi-gian-khoa", 300) * 1000L;
            long since = System.currentTimeMillis() - lockMs;
            int maxAttempts = plugin.getConfig().getInt("bao-mat.so-lan-sai-toi-da", 5);
            int failures = countIpFailures(ip, since);
            if (failures >= maxAttempts) {
                String message = plugin.getMessageManager().get(
                        "tin-nhan.ip-blocked", "&cIP của bạn đang bị tạm khóa, vui lòng thử lại sau!");
                event.disallow(PlayerLoginEvent.Result.KICK_BANNED, message);
                plugin.getLog().warning("[IP-Lock] " + ip + " bị chặn do đăng nhập sai " + failures + " lần");
                return;
            }
        }
        if (plugin.getConfig().getBoolean("log-su-kien", true)) {
            plugin.getLog().info(String.format("[Kết nối] %s (%s) từ IP %s - Client %s",
                    player.getName(), player.getUniqueId(), ip, getClientBrand(player)));
        }
    }
    private String getClientBrand(Player player) {
        try {
            String brand = player.getClientBrandName();
            return brand == null ? "Unknown" : brand;
        } catch (Throwable ignored) {
            return "Unknown";
        }
    }
    private int countIpFailures(String ip, long sinceMs) {
        try {
            return plugin.getAuthManager().getCachedFailureCount("ip:" + ip, sinceMs);
        } catch (Exception e) {
            return 0;
        }
    }
}
