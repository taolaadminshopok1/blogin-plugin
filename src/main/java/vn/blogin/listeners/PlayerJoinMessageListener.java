package vn.blogin.listeners;
import vn.blogin.Blogin;
import vn.blogin.utils.ChatUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
public class PlayerJoinMessageListener implements Listener {
    private final Blogin plugin;
    public PlayerJoinMessageListener(Blogin plugin) {
        this.plugin = plugin;
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        event.joinMessage(null);
        if (!plugin.getConfig().getBoolean("welcome.an-thong-bao-join", false)) return;
        String template = plugin.getMessageManager().getWithPlaceholders(
                player, "welcome.thong-bao-join",
                "&8» &b{player} &7vừa tham gia server!",
                "player", player.getName());
        ChatUtil.send(player, template);
    }
}
