package vn.blogin.listeners;

import vn.blogin.Blogin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

/**
 * Chặn tương tác vật phẩm, thực thể, inventory khi chưa đăng nhập.
 *
 * @author BloginVN
 */
public class PlayerInteractListener implements Listener {

    private final Blogin plugin;

    public PlayerInteractListener(Blogin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getSessionManager().isAuthenticated(player.getUniqueId())) {
            if (plugin.getConfig().getBoolean("gioi-han.tuong-tac-thuc-the", true)) {
                event.setCancelled(true);
                player.sendMessage(plugin.getMessageManager().get(
                        "canh-bao.tuong-tac", "&cKhông thể tương tác khi chưa đăng nhập!"));
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getSessionManager().isAuthenticated(player.getUniqueId())) {
            if (plugin.getConfig().getBoolean("gioi-han.tuong-tac-thuc-the", true)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!plugin.getSessionManager().isAuthenticated(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (!plugin.getSessionManager().isAuthenticated(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        // 1.20+ dùng EntityPickupItemEvent thay cho PlayerPickupItemEvent
        if (event.getEntity() instanceof Player player
                && !plugin.getSessionManager().isAuthenticated(player.getUniqueId())) {
            if (plugin.getConfig().getBoolean("gioi-han.nhat-do", true)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getSessionManager().isAuthenticated(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }
}
