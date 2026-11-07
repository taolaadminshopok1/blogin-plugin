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
import org.bukkit.inventory.meta.SkullMeta;
import vn.blogin.Blogin;
import vn.blogin.utils.ChatUtil;
import vn.blogin.utils.Compat;
import vn.blogin.utils.PlayerRestrictionUtil;
import vn.blogin.utils.SoundUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Giao dien dang nhap (Chest GUI) co giao dien dep, ho tro nhap mat khau & 2FA,
 * hien thi captcha va thong tin trang thai theo thoi gian thuc.
 *
 * <p>Toan bo noi dung cua panel lay tu tep ngon ngu theo ngon ngu cua tung
 * nguoi choi ({@code lang/&lt;ma&gt;.yml}).</p>
 */
public class LoginGui implements Listener {

    private final Blogin plugin;
    private final Map<UUID, Inventory> panels = new HashMap<>();
    private final Map<UUID, String> tokens = new HashMap<>();
    private final Map<UUID, Boolean> registeredCache = new HashMap<>();

    /** Kich thuoc panel (3 hang x 9 cot). */
    private static final int SIZE = 27;

    /** Vi tri cac o trong panel. */
    private static final int SLOT_HEAD = 4;
    private static final int SLOT_INFO = 10;
    private static final int SLOT_CAPTCHA = 12;
    private static final int SLOT_LOGIN = 13;
    private static final int SLOT_HELP = 16;
    private static final int SLOT_PASSWORD = 20;
    private static final int SLOT_TOTP = 22;
    private static final int SLOT_CLOSE = 26;

    public LoginGui(Blogin plugin) {
        this.plugin = plugin;
    }

    /**
     * Mo panel dang nhap cho nguoi choi.
     */
    public void open(Player player) {
        if (!plugin.getConfig().getBoolean("gui.bat", true)) {
            return;
        }
        if (plugin.getSessionManager().isAuthenticated(player.getUniqueId())) {
            ChatUtil.send(player, plugin.getMessageManager().get(
                    player, "gui.hint.da-dang-nhap-roi", "&cBạn đã đăng nhập rồi!"));
            return;
        }

        String title = plugin.getMessageManager().getWithPlaceholders(
                player, "gui.tieu-de", "&8✦ &6&lBLOGIN &8✦ &7| &f{player}", "player", player.getName());

        // Dung Component overload de giu duoc hex color & formatting hien dai.
        Inventory inv = Bukkit.createInventory(null, SIZE, ChatUtil.parse(title));

        populate(inv, player);
        panels.put(player.getUniqueId(), inv);
        tokens.put(player.getUniqueId(), UUID.randomUUID().toString());
        refreshRegisteredCache(player);
        player.openInventory(inv);
    }

    /**
     * Cap nhat lai panel dang mo (goi khi trang thai thay doi).
     */
    public void refresh(Player player) {
        Inventory inv = panels.get(player.getUniqueId());
        if (inv == null) return;
        if (!player.isOnline()) return;

        populate(inv, player);
        player.updateInventory();
    }

    /**
     * Dien noi dung vao cac o cua panel.
     */
    private void populate(Inventory inv, Player player) {
        boolean showCaptcha = plugin.getConfig().getBoolean("gui.hien-o-captcha", true);
        boolean showHead = plugin.getConfig().getBoolean("gui.hien-dau-nguoi-choi", true);

        inv.setItem(SLOT_HEAD, showHead ? buildHead(player) : null);
        inv.setItem(SLOT_INFO, buildItem(player, Material.PAPER,
                "gui.item.info.name", "&e&lTHÔNG TIN ĐĂNG NHẬP",
                "gui.item.info.lore",
                "&7Tài khoản: &f{player}",
                "&7IP: &f{ip}",
                "&7Đã đăng ký: &f{status}",
                "&7Số lần thử còn lại: &f{remaining}",
                "",
                "&8Mật khẩu được lưu dạng BCrypt"));
        inv.setItem(SLOT_CAPTCHA, showCaptcha ? buildCaptchaItem(player) : null);
        inv.setItem(SLOT_LOGIN, buildItem(player, Material.LIME_CONCRETE,
                "gui.item.login.name", "&a&lĐĂNG NHẬP",
                "gui.item.login.lore",
                "&7Nhấn để xác nhận đăng nhập",
                "",
                "&7Mật khẩu đã nhập: &f{chuc}",
                "&72FA: &f{tfa}",
                "&7Captcha: &f{cap}",
                "",
                "&eNhấn Shift + Click để đóng"));
        inv.setItem(SLOT_HELP, buildItem(player, Material.BOOK,
                "gui.item.help.name", "&b&lHƯỚNG DẪN",
                "gui.item.help.lore",
                "&7Vào &f< ô mật khẩu > &7để nhập mật khẩu",
                "&7Vào &f< ô 2FA > &7để nhập mã 2FA (nếu có)",
                "&7Nhấn nút xanh lá để đăng nhập",
                "",
                "&7Hoặc dùng lệnh &a/login <mật khẩu>"));
        inv.setItem(SLOT_PASSWORD, buildItem(player, Material.WRITABLE_BOOK,
                "gui.item.password.name", "&e&lÔ NHẬP MẬT KHẨU",
                "gui.item.password.lore",
                "&7Nhấn để nhập mật khẩu",
                "",
                "&8Hiện tại: &7{chuc}"));
        inv.setItem(SLOT_TOTP, buildItem(player, Material.PAPER,
                "gui.item.totp.name", "&d&lÔ NHẬP MÃ 2FA",
                "gui.item.totp.lore",
                "&7Nhấn để nhập mã 6 số",
                "",
                "&8Hiện tại: &7{tfa}"));
        inv.setItem(SLOT_CLOSE, buildItem(player, Material.BARRIER,
                "gui.item.close.name", "&c&lĐÓNG",
                "gui.item.close.lore",
                "&7Đóng panel"));
    }

    private void refreshRegisteredCache(Player player) {
        UUID uuid = player.getUniqueId();
        plugin.getDatabaseManager().runAsync(
                () -> plugin.getAuthManager().getPlayerDAO().isRegistered(player.getName()),
                registered -> {
                    if (player.isOnline()) {
                        registeredCache.put(uuid, registered);
                        refresh(player);
                    }
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

        // Shift + Click: dong panel nhanh.
        if (event.isShiftClick()) {
            player.closeInventory();
            return;
        }

        if (slot == SLOT_CLOSE) {
            player.closeInventory();
            return;
        }
        if (slot == SLOT_LOGIN) {
            attemptLogin(player);
            return;
        }
        if (slot == SLOT_CAPTCHA) {
            if (!plugin.getConfig().getBoolean("gui.hien-o-captcha", true)) return;
            promptCaptcha(player);
            return;
        }
        if (slot == SLOT_PASSWORD) {
            closeAndPrompt(player, "gui.prompt.password",
                    "&eNhập mật khẩu trong chat (gõ &f/cancel &eđể huỷ):", "password");
            return;
        }
        if (slot == SLOT_TOTP) {
            closeAndPrompt(player, "gui.prompt.totp",
                    "&dNhập mã 2FA trong chat (gõ &f/cancel &dđể huỷ):", "totp");
        }
    }

    /**
     * Sinh lai ma captcha moi va hien thi len panel.
     */
    private void refreshCaptcha(Player player) {
        if (!plugin.getCaptchaManager().isEnabled()) return;
        String[] cap = plugin.getCaptchaManager().generate();
        plugin.getSessionManager().setPendingCaptcha(player.getUniqueId(), cap[0], cap[1]);
        ChatUtil.send(player, plugin.getMessageManager().get(
                player, "captcha.tin-nhan-captcha",
                "&e[Captcha] &fMã captcha của bạn: &a&l{captcha}", "captcha", cap[0]));
        refresh(player);
    }

    /**
     * Mo yeu cau nhap ma captcha trong chat.
     *
     * <p>Nguoi choi nhap ma captcha dung o day se duoc danh dau la da vuot qua
     * captcha, sau do bam nut dang nhap. Nho vay captcha <b>khong the bi bo qua</b>
     * bang cach dung GUI thay vi lenh {@code /login}.</p>
     */
    private void promptCaptcha(Player player) {
        if (!plugin.getCaptchaManager().isEnabled()) return;
        String pending = plugin.getSessionManager().getPendingCaptcha(player.getUniqueId());
        if (pending == null) {
            refreshCaptcha(player);
            pending = plugin.getSessionManager().getPendingCaptcha(player.getUniqueId());
            if (pending == null) return;
        }
        String prompt = plugin.getMessageManager().get(
                player, "gui.prompt.captcha",
                "&6Nhập mã captcha &7(&f" + pending + "&7) trong chat "
                        + "&8(gõ &f/cancel &8để huỷ):", "captcha", pending);
        closeAndPrompt(player, null, prompt, "captcha");
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
            ChatUtil.send(player, plugin.getMessageManager().get(
                    player, "gui.hint.chua-nhap-mat-khau",
                    "&c✖ Vui lòng nhập mật khẩu trước!"));
            playSound(player, "sai-mat-khau", Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.5f);
            return;
        }

        // Captcha bat buoc: GUI cung phai qua captcha nhu lenh /login.
        // Neu bo qua buoc nay, nguoi choi co the bo qua captcha bang cach
        // dung GUI thay vi lenh -> lohong hoan toan.
        if (plugin.getCaptchaManager().isEnabled()
                && !player.hasPermission("blogin.bypass.captcha")) {
            String pending = plugin.getSessionManager().getPendingCaptcha(player.getUniqueId());
            if (pending != null) {
                ChatUtil.send(player, plugin.getMessageManager().get(
                        player, "captcha.tin-nhan-captcha",
                        "&e[Captcha] &fVui lòng nhập mã: &a&l{captcha}", "captcha", pending));
                ChatUtil.send(player, plugin.getMessageManager().get(
                        player, "gui.hint.captcha-click",
                        "&7Nhấn ô captcha trên panel để lấy mã mới, hoặc dùng &a/captcha"));
                playSound(player, "sai-mat-khau", Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.5f);
                refresh(player);
                return;
            }
        }

        String ip = player.getAddress() != null
                ? player.getAddress().getAddress().getHostAddress() : "unknown";
        String totp = plugin.getSessionManager().getGuiTotp(player.getUniqueId());
        player.closeInventory();

        plugin.getAuthManager().loginAsync(player.getUniqueId(), player.getName(),
                password, totp, ip, result -> {
                    if (!player.isOnline()) return;
                    switch (result) {
                        case SUCCESS -> {
                            plugin.getSessionManager().markAuthenticated(player.getUniqueId());
                            ChatUtil.send(player, plugin.getMessageManager().get(
                                    player, "tin-nhan.da-dang-nhap",
                                    "&a✔ Đăng nhập thành công! Chào mừng {player} trở lại!",
                                    "player", player.getName()));
                            playSound(player, "dang-nhap-thanh-cong",
                                    Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
                            PlayerRestrictionUtil.release(plugin, player);
                            if (plugin.getConfig().getBoolean("welcome.bat", true)) {
                                plugin.getWelcomeManager().playLoginSuccessSequence(player);
                            }
                        }
                        case TOTP_REQUIRED -> {
                            // Tai khoan co 2FA nhung nguoi choi chua nhap ma.
                            // Giu lai mat khau, yeu cau nhap ma 2FA roi thu lai.
                            ChatUtil.send(player, plugin.getMessageManager().get(
                                    player, "tin-nhan.can-2fa",
                                    "&e🔐 Tài khoản này yêu cầu mã 2FA. "
                                            + "Nhập mã vào ô &d2FA &etrên panel."));
                            playSound(player, "sai-mat-khau", Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.5f);
                            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                if (player.isOnline()) open(player);
                            }, 5L);
                        }
                        default -> {
                            ChatUtil.send(player, plugin.getAuthManager().getErrorMessage(result, player));
                            playSound(player, "sai-mat-khau", Sound.ENTITY_VILLAGER_NO, 1.0f, 0.8f);
                            // Xoa mat khau da nhap de tranh giu lai mat khau trong RAM
                            // va buoc nguoi choi nhap lai sau khi sai.
                            plugin.getSessionManager().setGuiPassword(player.getUniqueId(), null);
                            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                if (player.isOnline()) open(player);
                            }, 5L);
                        }
                    }
                });
    }

    private void closeAndPrompt(Player player, String messageKey, String prompt, String type) {
        player.closeInventory();
        plugin.getSessionManager().setAwaitingChatInput(player.getUniqueId(), type);
        if (messageKey != null) {
            prompt = plugin.getMessageManager().get(player, messageKey, prompt);
        }
        ChatUtil.send(player, prompt);

        // Timeout: sau 30 giay neu nguoi choi van chua nhap gi, huy yeu cau
        // va mo lai panel. Truoc day ham nay xoa trang thai sau 10 giay ma KHONG
        // mo lai panel -> nguoi choi bi "mat" mat khau da nhap va khong bi
        // thong bao gi.
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            if (plugin.getSessionManager().isAuthenticated(player.getUniqueId())) return;
            if (!type.equals(plugin.getSessionManager().getAwaitingChatInput(player.getUniqueId()))) {
                return;
            }
            plugin.getSessionManager().clearAwaitingChatInput(player.getUniqueId());
            ChatUtil.send(player, plugin.getMessageManager().get(
                    player, "gui.hint.timeout-reopen",
                    "&7Đã hết thời gian nhập, panel đã mở lại."));
            open(player);
        }, 600L);
    }

    /**
     * Tao item hien thi dau cua nguoi choi.
     */
    private ItemStack buildHead(Player player) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        try {
            if (meta instanceof SkullMeta skull) {
                skull.setOwningPlayer(player);
            }
        } catch (RuntimeException | LinkageError ignored) {
            // API skull co the khac nhau giua cac phien ban - bo qua, panel van dung.
        }
        meta.displayName(legacy(plugin.getMessageManager().get(
                player, "gui.item.head.name", "&6&l✦ &e&lBLOGIN &6&l✦"))
                .decoration(TextDecoration.ITALIC, false));
        List<Component> lore = new ArrayList<>();
        for (String line : plugin.getMessageManager().getList(player, "gui.item.head.lore")) {
            lore.add(legacy(plugin.getMessageManager()
                            .applyPlaceholders(line, "player", player.getName()))
                    .decoration(TextDecoration.ITALIC, false));
        }
        if (lore.isEmpty()) {
            lore.add(legacy("&7Xin chào, &f" + player.getName() + "&7!")
                    .decoration(TextDecoration.ITALIC, false));
        }
        meta.lore(lore);
        item.setItemMeta(meta);
        Compat.applyGlint(item);
        return item;
    }

    /**
     * Tao item hien thi captcha.
     */
    private ItemStack buildCaptchaItem(Player player) {
        if (!plugin.getCaptchaManager().isEnabled()) {
            ItemStack item = new ItemStack(Material.LIME_DYE);
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return item;
            meta.displayName(legacy(plugin.getMessageManager().get(
                    player, "gui.item.captcha.off-name",
                    "&a&l✔ CAPTCHA ĐÃ ĐƯỢC BỎ QUA"))
                    .decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(legacy(plugin.getMessageManager().get(
                            player, "gui.item.captcha.off-lore",
                            "&7Captcha đang tắt trong cấu hình."))
                    .decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
            item.setItemMeta(meta);
            return item;
        }
        String pending = plugin.getSessionManager().getPendingCaptcha(player.getUniqueId());
        ItemStack item = new ItemStack(Material.NAME_TAG);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        meta.displayName(legacy(plugin.getMessageManager().get(
                player, "gui.item.captcha.name", "&6&lMÃ CAPTCHA"))
                .decoration(TextDecoration.ITALIC, false));
        List<Component> lore = new ArrayList<>();
        if (pending != null) {
            for (String line : plugin.getMessageManager().getList(player, "gui.item.captcha.lore")) {
                lore.add(legacy(plugin.getMessageManager()
                                .applyPlaceholders(line, "captcha", pending))
                        .decoration(TextDecoration.ITALIC, false));
            }
        } else {
            lore.add(legacy(plugin.getMessageManager().get(
                            player, "gui.item.captcha.lore-rong",
                            "&7Bạn chưa có mã captcha."))
                    .decoration(TextDecoration.ITALIC, false));
        }
        meta.lore(lore);
        item.setItemMeta(meta);
        Compat.applyGlint(item);
        return item;
    }

    /**
     * Tao item trong panel voi ten & lore lay tu tep ngon ngu.
     *
     * @param nameKey duong dan ten hien thi
     * @param loreKey duong dan danh sach lore
     * @param fallback ten mac dinh khi khong tim thay trong tep ngon ngu
     * @param fallbackLore lore mac dinh
     */
    private ItemStack buildItem(Player player, Material material, String nameKey, String fallback,
                                String loreKey, String... fallbackLore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        Component nameComp = legacy(plugin.getMessageManager().get(player, nameKey, fallback))
                .decoration(TextDecoration.ITALIC, false);
        meta.displayName(nameComp);

        String ip = player.getAddress() != null
                ? player.getAddress().getAddress().getHostAddress() : "unknown";
        String registered = isRegistered(player.getUniqueId())
                ? plugin.getMessageManager().get(player, "gui.value.co", "Có")
                : plugin.getMessageManager().get(player, "gui.value.khong", "Không");
        String password = plugin.getSessionManager().getGuiPassword(player.getUniqueId());
        String totp = plugin.getSessionManager().getGuiTotp(player.getUniqueId());
        String captcha = plugin.getSessionManager().getPendingCaptcha(player.getUniqueId());

        int remaining = plugin.getAuthManager().getRemainingAttempts(player.getName(), ip);

        List<String> loreLines = plugin.getMessageManager().getList(player, loreKey);
        if (loreLines.isEmpty()) {
            loreLines = new ArrayList<>(java.util.Arrays.asList(fallbackLore));
        }

        List<Component> loreList = new ArrayList<>();
        for (String line : loreLines) {
            String resolved = plugin.getMessageManager().applyPlaceholders(line,
                    "player", player.getName(),
                    "ip", ip,
                    "status", registered,
                    "remaining", String.valueOf(remaining),
                    "chuc", password == null || password.isEmpty()
                            ? plugin.getMessageManager().get(player, "gui.value.chua-nhap", "Chưa nhập")
                            : "••••••",
                    "tfa", totp == null || totp.isEmpty()
                            ? plugin.getMessageManager().get(player, "gui.value.khong-co", "Không có")
                            : "••••••",
                    "cap", captcha == null || captcha.isEmpty()
                            ? plugin.getMessageManager().get(player, "gui.value.chua-co", "Chưa có")
                            : captcha);
            loreList.add(legacy(resolved).decoration(TextDecoration.ITALIC, false));
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
