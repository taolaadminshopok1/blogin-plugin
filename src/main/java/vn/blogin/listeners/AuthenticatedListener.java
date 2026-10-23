package vn.blogin.listeners;

import vn.blogin.Blogin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Lắng nghe để xử lý:
 * <ul>
 *   <li>Hiển thị lại người chơi cho nhau sau khi đăng nhập thành công</li>
 *   <li>Cho phép di chuyển lại sau khi đăng nhập</li>
 *   <li>Bỏ hiệu ứng hạn chế khi đã auth</li>
 * </ul>
 *
 * <p>Quản lý vòng đời "auth state" của người chơi.</p>
 *
 * @author BloginVN
 */
public class AuthenticatedListener implements Listener {

    private final Blogin plugin;
    /**
     * Cache người chơi đã từng đăng nhập trong session này (để biết khi nào vừa auth).
     */
    private final Set<UUID> wasAuthenticated = new HashSet<>();

    public AuthenticatedListener(Blogin plugin) {
        this.plugin = plugin;
    }

    /**
     * Cho phép di chuyển bình thường sau khi đăng nhập thành công.
     * Logic này thực ra đã có ở PlayerMoveListener, file này để mở rộng sau này.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onMoveAfterAuth(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (plugin.getSessionManager().isAuthenticated(player.getUniqueId())) {
            wasAuthenticated.add(player.getUniqueId());
        }
    }

    /**
     * Khi người chơi rời server - cleanup.
     */
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        // Hiển thị lại player này cho những người khác (nếu bị ẩn trước đó)
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (!other.canSee(player)) {
                other.showPlayer(plugin, player);
            }
        }
        wasAuthenticated.remove(player.getUniqueId());
    }
}
