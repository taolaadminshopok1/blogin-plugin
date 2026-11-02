package vn.blogin.gui;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import vn.blogin.Blogin;
import vn.blogin.utils.ChatUtil;
import vn.blogin.utils.PlayerRestrictionUtil;
import vn.blogin.utils.SoundUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
public class LoginGui implements Listener {
    private final Blogin plugin;
    private final Map<UUID, Inventory> panels = new HashMap<>();
    private final Map<UUID, String> tokens = new HashMap<>();
    private final Map<UUID, Boolean> registeredCache = new HashMap<>();
    private static final int SIZE = 27;
    public LoginGui(Blogin plugin) {
        this.plugin = plugin;
    }
    public void open(Player player) {
        if (!plugin.getConfig().getBoolean("gui.bat", true)) {
            return;
        }
        if (plugin.getSessionManager().isAuthenticated(player.getUniqueId())) {
            ChatUtil.send(player, "&cBạn đã đăng nhập rồi!");
            return;
        }
        String title = plugin.getMessageManager().getWithPlaceholders(
                "gui.tieu-de", "&8✦ &6&lBLOGIN &8✦ &7| &f{player}", "player", player.getName());
        Inventory inv = Bukkit.createInventory(null, SIZE, legacy(title));
        inv.setItem(13, buildItem(player, Material.LIME_CONCRETE, "&a&lĐĂNG NHẬP",
                "&7Nhấn để xác nhận đăng nhập",
                "",
                "&7Mật khẩu đã nhập: &f{chuc}",
                "&72FA: &f{tfa}",
                "",
                "&eNhấn Shift + Click để đóng"));
        inv.setItem(11, buildItem(player, Material.PAPER, "&e&lTHÔNG TIN ĐĂNG NHẬP",
                "&7Tài khoản: &f{player}",
                "&7IP: &f{ip}",
                "&7Đã đăng nhập: &f{status}",
                "&7Số lần thử còn lại: &f{remaining}",
                "",
                "&8Mật khẩu được lưu dạng BCrypt"));
        inv.setItem(15, buildItem(player, Material.BOOK, "&b&lHƯỚNG DẪN",
                "&7Vào &f< ô mật khẩu > &7để nhập mật khẩu",
                "&7Vào &f< ô 2FA > &7để nhập mã 2FA (nếu có)",
                "&7Nhấn nút xanh lá để đăng nhập",
                "",
                "&7Hoặc dùng lệnh &a/login <mật khẩu>"));
        inv.setItem(20, buildItem(player, Material.WRITABLE_BOOK, "&e&lÔ NHẬP MẬT KHẨU",
                "&7Nhấn để nhập mật khẩu",
                "",
                "&8Hiện tại: &7{chuc}"));
        inv.setItem(24, buildItem(player, Material.PAPER, "&d&lÔ NHẬP MÃ 2FA",
                "&7Nhấn để nhập mã 6 số",
                "",
                "&8Hiện tại: &7{tfa}"));
        inv.setItem(16, buildItem(player, Material.BARRIER, "&c&lĐÓNG", "&7Đóng panel"));
        panels.put(player.getUniqueId(), inv);
        tokens.put(player.getUniqueId(), UUID.randomUUID().toString());
        refreshRegisteredCache(player);
        player.openInventory(inv);
    }
    private void refreshRegisteredCache(Player player) {
        UUID uuid = player.getUniqueId();
        plugin.getDatabaseManager().runAsync(
                () -> plugin.getAuthManager().getPlayerDAO().isRegistered(player.getName()),
                registered -> {
                    if (player.isOnline()) registeredCache.put(uuid, registered);
                }
        );
    }
    private boolean isRegistered(UUID uuid) {
        return registeredCache.getOrDefault(uuid, Boolean.FALSE);
    }
    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        UUID uuid = player.getUniqueId();
        Inventory top = event.getView().getTopInventory();
        if (top == null || !panels.containsKey(uuid)) return;
        if (!top.equals(panels.get(uuid))) {
            event.setCancelled(true);
            return;
        }
        if (!top.equals(event.getClickedInventory())) {
            event.setCancelled(true);
            return;
        }
        event.setCancelled(true);
        int slot = event.getSlot();
        if (slot < 0 || slot >= SIZE) return;
        if (slot == 16) {
            player.closeInventory();
            return;
        }
        if (slot == 13) {
            attemptLogin(player);
            return;
        }
        if (slot == 20) {
            closeAndPrompt(player, "&eNhập mật khẩu trong chat (gõ &f/cancel &eđể huỷ):", "password");
            return;
        }
        if (slot == 24) {
            closeAndPrompt(player, "&dNhập mã 2FA trong chat (gõ &f/cancel &dđể huỷ):", "totp");
        }
    }
    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getWhoClicked() instanceof Player player
                && panels.containsKey(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (panels.containsKey(player.getUniqueId())) {
            panels.remove(player.getUniqueId());
            tokens.remove(player.getUniqueId());
        }
    }
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        panels.remove(event.getPlayer().getUniqueId());
        tokens.remove(event.getPlayer().getUniqueId());
        registeredCache.remove(event.getPlayer().getUniqueId());
    }
    private void attemptLogin(Player player) {
        String password = plugin.getSessionManager().getGuiPassword(player.getUniqueId());
        if (password == null || password.isEmpty()) {
            ChatUtil.send(player, "&c✖ Vui lòng nhập mật khẩu trước!");
            playSound(player, "sai-mat-khau", Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.5f);
            return;
        }
        String ip = player.getAddress() != null
                ? player.getAddress().getAddress().getHostAddress() : "unknown";
        String totp = plugin.getSessionManager().getGuiTotp(player.getUniqueId());
        player.closeInventory();
        plugin.getAuthManager().loginAsync(player.getUniqueId(), player.getName(),
                password, totp, ip, result -> {
                    if (!player.isOnline()) return;
                    if (result == vn.blogin.managers.AuthManager.AuthResult.SUCCESS) {
                        plugin.getSessionManager().markAuthenticated(player.getUniqueId());
                        ChatUtil.send(player, plugin.getMessageManager().get(
                                "tin-nhan.da-dang-nhap",
                                "&a✔ Đăng nhập thành công! Chào mừng {player} trở lại!",
                                "player", player.getName()));
                        playSound(player, "dang-nhap-thanh-cong", Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
                        PlayerRestrictionUtil.release(plugin, player);
                        if (plugin.getConfig().getBoolean("welcome.bat", true)) {
                            plugin.getWelcomeManager().playLoginSuccessSequence(player);
                        }
                    } else {
                        ChatUtil.send(player, plugin.getAuthManager().getErrorMessage(result));
                        playSound(player, "sai-mat-khau", Sound.ENTITY_VILLAGER_NO, 1.0f, 0.8f);
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            if (player.isOnline()) open(player);
                        }, 5L);
                    }
                });
    }
    private void closeAndPrompt(Player player, String prompt, String type) {
        player.closeInventory();
        plugin.getSessionManager().setAwaitingChatInput(player.getUniqueId(), type);
        ChatUtil.send(player, prompt);
        Bukkit.getScheduler().runTaskLater(plugin, () ->
                plugin.getSessionManager().setAwaitingChatInput(player.getUniqueId(), null), 200L);
    }
    private ItemStack buildItem(Player player, Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        Component nameComp = legacy(name).decoration(TextDecoration.ITALIC, false);
        meta.displayName(nameComp);
        String ip = player.getAddress() != null
                ? player.getAddress().getAddress().getHostAddress() : "unknown";
        String registered = isRegistered(player.getUniqueId()) ? "Có" : "Không";
        int remaining = plugin.getAuthManager().getRemainingAttempts(player.getName(), ip);
        String password = plugin.getSessionManager().getGuiPassword(player.getUniqueId());
        String totp = plugin.getSessionManager().getGuiTotp(player.getUniqueId());
        StringBuilder loreBuilder = new StringBuilder();
        for (String line : lore) {
            String resolved = line
                    .replace("{player}", player.getName())
                    .replace("{ip}", ip)
                    .replace("{status}", registered)
                    .replace("{remaining}", String.valueOf(remaining))
                    .replace("{chuc}", password == null || password.isEmpty() ? "Chưa nhập" : "••••••")
                    .replace("{tfa}", totp == null || totp.isEmpty() ? "Không có" : "••••••");
            loreBuilder.append(resolved).append('\n');
        }
        List<Component> loreList = new ArrayList<>();
        for (String line : loreBuilder.toString().split("\n")) {
            loreList.add(legacy(line).decoration(TextDecoration.ITALIC, false));
        }
        meta.lore(loreList);
        item.setItemMeta(meta);
        return item;
    }
    private Component legacy(String text) {
        return ChatUtil.parse(text);
    }
    private void playSound(Player player, String key, Sound fallback, float volume, float pitch) {
        SoundUtil.play(plugin, player, key, fallback, volume, pitch);
    }
    public boolean isLoginPanel(UUID uuid, Inventory inventory) {
        if (inventory == null) return false;
        Inventory panel = panels.get(uuid);
        return panel != null && panel.equals(inventory);
    }
    public void closeAll() {
        for (UUID uuid : panels.keySet()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) p.closeInventory();
        }
        panels.clear();
        tokens.clear();
    }
}
