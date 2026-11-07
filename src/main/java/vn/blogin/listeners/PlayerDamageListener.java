package vn.blogin.listeners;
import vn.blogin.Blogin;
import vn.blogin.utils.ChatUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
public class PlayerDamageListener implements Listener {
    private final Blogin plugin;
    public PlayerDamageListener(Blogin plugin) {
        this.plugin = plugin;
    }
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (plugin.getSessionManager().isAuthenticated(player.getUniqueId())) return;
        event.setCancelled(true);
    }
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player damager)) return;
        if (plugin.getSessionManager().isAuthenticated(damager.getUniqueId())) return;
        if (plugin.getConfig().getBoolean("gioi-han.tan-cong", true)) {
            event.setCancelled(true);
        }
    }
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onAttackUnauthenticated(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player damager)) return;
        if (plugin.getSessionManager().isAuthenticated(damager.getUniqueId())) return;
        if (!plugin.getConfig().getBoolean("gioi-han.tan-cong", true)) return;
        ChatUtil.send(damager, plugin.getMessageManager().get(
                damager, "canh-bao.tan-cong", "&c⚠ Không thể tấn công khi chưa đăng nhập!"));
    }
}
