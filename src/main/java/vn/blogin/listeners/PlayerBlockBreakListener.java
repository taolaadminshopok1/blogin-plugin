package vn.blogin.listeners;
import vn.blogin.Blogin;
import vn.blogin.utils.ChatUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
public class PlayerBlockBreakListener implements Listener {
    private final Blogin plugin;
    public PlayerBlockBreakListener(Blogin plugin) {
        this.plugin = plugin;
    }
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getSessionManager().isAuthenticated(player.getUniqueId())) {
            if (plugin.getConfig().getBoolean("gioi-han.pha-khoi", true)) {
                event.setCancelled(true);
                ChatUtil.send(player, plugin.getMessageManager().get(
                        player, "canh-bao.pha-khoi", "&c⚠ Không thể phá khối khi chưa đăng nhập!"));
            }
        }
    }
}
