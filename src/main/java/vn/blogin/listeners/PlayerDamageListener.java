package vn.blogin.listeners;

import vn.blogin.Blogin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * Chặn gây/nhận sát thương khi chưa đăng nhập.
 *
 * @author BloginVN
 */
public class PlayerDamageListener implements Listener {

    private final Blogin plugin;

    public PlayerDamageListener(Blogin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (plugin.getSessionManager().isAuthenticated(player.getUniqueId())) return;

        // Người chơi chưa đăng nhập - bất khả xâm phạm
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player damager
                && !plugin.getSessionManager().isAuthenticated(damager.getUniqueId())) {
            event.setCancelled(true);
        }
    }
}
