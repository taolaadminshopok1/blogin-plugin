package vn.blogin.listeners;
import vn.blogin.Blogin;
import vn.blogin.utils.ChatUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
public class PlayerMoveListener implements Listener {
    private final Blogin plugin;
    private final Map<UUID, Long> lastWarning = new HashMap<>();
    public PlayerMoveListener(Blogin plugin) {
        this.plugin = plugin;
    }
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (plugin.getSessionManager().isAuthenticated(player.getUniqueId())) return;
        if (!plugin.getConfig().getBoolean("gioi-han.di-chuyen", true)) return;
        var from = event.getFrom();
        var to = event.getTo();
        if (to == null) return;
        if (from.getBlockX() != to.getBlockX()
                || from.getBlockZ() != to.getBlockZ()
                || from.getBlockY() != to.getBlockY()) {
            event.setTo(from);
            warnThrottled(player);
        }
    }
    private void warnThrottled(Player player) {
        long now = System.currentTimeMillis();
        Long last = lastWarning.get(player.getUniqueId());
        if (last != null && now - last < 2000L) return;
        lastWarning.put(player.getUniqueId(), now);
        ChatUtil.send(player, plugin.getMessageManager().get(
                "canh-bao.di-chuyen", "&c⚠ Không thể di chuyển khi chưa đăng nhập!"));
    }
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        lastWarning.remove(event.getPlayer().getUniqueId());
    }
}
