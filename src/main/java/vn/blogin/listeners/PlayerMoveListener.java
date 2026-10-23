package vn.blogin.listeners;

import vn.blogin.Blogin;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Chặn di chuyển khi chưa đăng nhập (chỉ cho phép xoay nhìn quanh - nhìn 1 góc nhỏ).
 *
 * @author BloginVN
 */
public class PlayerMoveListener implements Listener {

    private final Blogin plugin;

    public PlayerMoveListener(Blogin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (plugin.getSessionManager().isAuthenticated(player.getUniqueId())) return;

        if (!plugin.getConfig().getBoolean("gioi-han.di-chuyen", true)) return;

        // Cho phép xoay nhìn (yaw/pitch) nhưng không cho thay đổi vị trí
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) return;

        // Nếu có thay đổi block (x, z hoặc y) thì chặn
        if (from.getBlockX() != to.getBlockX()
                || from.getBlockZ() != to.getBlockZ()
                || from.getBlockY() != to.getBlockY()) {
            event.setTo(from);
            player.sendMessage(plugin.getMessageManager().get(
                    "canh-bao.di-chuyen", "&cBạn không thể di chuyển khi chưa đăng nhập!"));
        }
    }
}
