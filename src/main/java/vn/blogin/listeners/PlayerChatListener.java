package vn.blogin.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import vn.blogin.Blogin;
import vn.blogin.utils.ChatUtil;

import java.util.regex.Pattern;

/**
 * Chan chat khi nguoi choi chua dang nhap + xu ly viec nhap mat khau / 2FA / captcha
 * qua chat khi dang mo GUI dang nhap.
 */
public class PlayerChatListener implements Listener {

    private final Blogin plugin;

    /**
     * Cho phep ky tu ASCII in duoc (khoang trang + chu in hoa).
     *
     * <p>Chuoi <b>phai</b> khong chua dau cach, nen ta cat khoang trang o dau/cuoi
     * truoc khi kiem tra (xem {@code trim()} ben duoi).</p>
     */
    private static final Pattern SAFE_TEXT = Pattern.compile("^[\\x20-\\x7E]{1,64}$");

    public PlayerChatListener(Blogin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        String awaiting = plugin.getSessionManager().getAwaitingChatInput(player.getUniqueId());
        if (awaiting != null && !plugin.getSessionManager().isAuthenticated(player.getUniqueId())) {
            event.setCancelled(true);
            handleGuiInput(player, awaiting, event);
            return;
        }
        if (!plugin.getSessionManager().isAuthenticated(player.getUniqueId())) {
            if (plugin.getConfig().getBoolean("gioi-han.chat", true)) {
                event.setCancelled(true);
                plugin.getServer().getScheduler().runTask(plugin, () ->
                        ChatUtil.send(player, plugin.getMessageManager().get(
                                player, "canh-bao.chat", "&c⚠ Bạn cần đăng nhập trước khi chat!")));
            }
        }
    }

    private void handleGuiInput(Player player, String type, AsyncChatEvent event) {
        String message = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
                .plainText().serialize(event.message()).trim();
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (!player.isOnline()) return;
            if (message.equalsIgnoreCase("cancel") || message.equalsIgnoreCase("hủy")
                    || message.equalsIgnoreCase("huyd")) {
                plugin.getSessionManager().setAwaitingChatInput(player.getUniqueId(), null);
                ChatUtil.send(player, plugin.getMessageManager().get(
                        player, "misc.chat.huy", "&7Đã huỷ nhập."));
                reopenGui(player);
                return;
            }
            if (!SAFE_TEXT.matcher(message).matches()) {
                ChatUtil.send(player, plugin.getMessageManager().get(
                        player, "misc.chat.invalid",
                        "&c✖ Dữ liệu không hợp lệ, vui lòng nhập lại."));
                return;
            }
            switch (type) {
                case "password" -> {
                    plugin.getSessionManager().setGuiPassword(player.getUniqueId(), message);
                    plugin.getSessionManager().setAwaitingChatInput(player.getUniqueId(), null);
                    ChatUtil.send(player, plugin.getMessageManager().get(
                            player, "misc.chat.saved-password",
                            "&a✔ Đã lưu mật khẩu. Nhấn nút xanh lá để đăng nhập."));
                    reopenGui(player);
                }
                case "totp" -> {
                    plugin.getSessionManager().setGuiTotp(player.getUniqueId(), message);
                    plugin.getSessionManager().setAwaitingChatInput(player.getUniqueId(), null);
                    ChatUtil.send(player, plugin.getMessageManager().get(
                            player, "misc.chat.saved-totp",
                            "&a✔ Đã lưu mã 2FA. Nhấn nút xanh lá để đăng nhập."));
                    reopenGui(player);
                }
                case "captcha" -> {
                    if (!plugin.getSessionManager().verifyCaptcha(player.getUniqueId(), message)) {
                        ChatUtil.send(player, plugin.getMessageManager().get(
                                player, "captcha.tin-nhan-sai",
                                "&c❌ Mã captcha sai! Dùng /captcha để lấy mã mới"));
                        // Sinh ma moi ngay de nguoi choi khong bi ket.
                        String[] newCap = plugin.getCaptchaManager().generate();
                        plugin.getSessionManager().setPendingCaptcha(
                                player.getUniqueId(), newCap[0], newCap[1]);
                        ChatUtil.send(player, plugin.getMessageManager().get(
                                player, "captcha.tin-nhan-captcha",
                                "&e[Captcha] &fMã captcha mới: &a&l{captcha}", "captcha", newCap[0]));
                    } else {
                        plugin.getSessionManager().setAwaitingChatInput(player.getUniqueId(), null);
                        ChatUtil.send(player, plugin.getMessageManager().get(
                                player, "misc.chat.captcha-ok",
                                "&a✔ Đã xác nhận captcha. Nhấn nút xanh lá để đăng nhập."));
                    }
                    reopenGui(player);
                }
                default -> plugin.getSessionManager().setAwaitingChatInput(player.getUniqueId(), null);
            }
        });
    }

    private void reopenGui(Player player) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline() && !plugin.getSessionManager().isAuthenticated(player.getUniqueId())) {
                plugin.getLoginGui().open(player);
            }
        }, 2L);
    }
}
