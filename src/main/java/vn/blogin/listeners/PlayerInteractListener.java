package vn.blogin.listeners;
import vn.blogin.Blogin;
import vn.blogin.utils.ChatUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
public class PlayerInteractListener implements Listener {
    private final Blogin plugin;
    private final java.util.Map<java.util.UUID, Long> lastWarning = new java.util.HashMap<>();
    public PlayerInteractListener(Blogin plugin) {
        this.plugin = plugin;
    }
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (plugin.getSessionManager().isAuthenticated(player.getUniqueId())) return;
        if (plugin.getConfig().getBoolean("gioi-han.tuong-tac-thuc-the", true)) {
            event.setCancelled(true);
            warnThrottled(player, "canh-bao.tuong-tac",
                    "&c⚠ Không thể tương tác khi chưa đăng nhập!");
        }
    }
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInteractEntity(org.bukkit.event.player.PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getSessionManager().isAuthenticated(player.getUniqueId())) {
            if (plugin.getConfig().getBoolean("gioi-han.tuong-tac-thuc-the", true)) {
                event.setCancelled(true);
            }
        }
    }
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInventoryClick(org.bukkit.event.inventory.InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (plugin.getSessionManager().isAuthenticated(player.getUniqueId())) return;
        var clicked = event.getClickedInventory();
        if (isBloginGui(player, clicked)) return;
        event.setCancelled(true);
    }
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInventoryOpen(org.bukkit.event.inventory.InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (!plugin.getSessionManager().isAuthenticated(player.getUniqueId())) {
            if (isBloginGui(player, event.getInventory())) return;
            event.setCancelled(true);
        }
    }
    private boolean isBloginGui(Player player, org.bukkit.inventory.Inventory inventory) {
        if (inventory == null) return false;
        return plugin.getLoginGui().isLoginPanel(player.getUniqueId(), inventory);
    }
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPickup(org.bukkit.event.entity.EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player
                && !plugin.getSessionManager().isAuthenticated(player.getUniqueId())) {
            if (plugin.getConfig().getBoolean("gioi-han.nhat-do", true)) {
                event.setCancelled(true);
            }
        }
    }
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onDrop(org.bukkit.event.player.PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getSessionManager().isAuthenticated(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }
    private void warnThrottled(Player player, String messageKey, String fallback) {
        long now = System.currentTimeMillis();
        Long last = lastWarning.get(player.getUniqueId());
        if (last != null && now - last < 2000L) return;
        lastWarning.put(player.getUniqueId(), now);
        ChatUtil.send(player, plugin.getMessageManager().get(messageKey, fallback));
    }
    @EventHandler
    public void onQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        lastWarning.remove(event.getPlayer().getUniqueId());
    }
}
