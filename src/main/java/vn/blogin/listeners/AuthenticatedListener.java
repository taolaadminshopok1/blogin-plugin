package vn.blogin.listeners;
import vn.blogin.Blogin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
public class AuthenticatedListener implements Listener {
    private final Blogin plugin;
    private final Set<UUID> wasAuthenticated = new HashSet<>();
    public AuthenticatedListener(Blogin plugin) {
        this.plugin = plugin;
    }
    @EventHandler(priority = EventPriority.MONITOR)
    public void onMoveAfterAuth(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (plugin.getSessionManager().isAuthenticated(player.getUniqueId())) {
            wasAuthenticated.add(player.getUniqueId());
        }
    }
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (!other.canSee(player)) {
                other.showPlayer(plugin, player);
            }
        }
        wasAuthenticated.remove(player.getUniqueId());
    }
}
