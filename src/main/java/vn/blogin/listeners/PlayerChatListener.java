package vn.blogin.listeners;

import vn.blogin.Blogin;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Chặn chat khi người chơi chưa đăng nhập.
 *
 * <p>Dùng {@link AsyncChatEvent} của Paper API (thay thế {@code AsyncPlayerChatEvent} deprecated từ 1.20+).</p>
 *
 * @author BloginVN
 */
public class PlayerChatListener implements Listener {

    private final Blogin plugin;

    public PlayerChatListener(Blogin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getSessionManager().isAuthenticated(player.getUniqueId())) {
            if (plugin.getConfig().getBoolean("gioi-han.chat", true)) {
                event.setCancelled(true);
                plugin.getServer().getScheduler().runTask(plugin, () ->
                    player.sendMessage(plugin.getMessageManager().get(
                        "canh-bao.chat", "&cBạn cần đăng nhập trước khi chat!")));
            }
        }
    }
}
