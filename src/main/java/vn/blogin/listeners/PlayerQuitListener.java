package vn.blogin.listeners;

import vn.blogin.Blogin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Lắng nghe sự kiện người chơi rời server - dọn dẹp session.
 *
 * @author BloginVN
 */
public class PlayerQuitListener implements Listener {

    private final Blogin plugin;

    public PlayerQuitListener(Blogin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getSessionManager().cleanup(player.getUniqueId());
    }
}
