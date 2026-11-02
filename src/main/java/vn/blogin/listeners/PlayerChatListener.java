package vn.blogin.listeners;
import vn.blogin.Blogin;
import vn.blogin.utils.ChatUtil;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import java.util.regex.Pattern;
public class PlayerChatListener implements Listener {
    private final Blogin plugin;
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
                                "canh-bao.chat", "&c⚠ Bạn cần đăng nhập trước khi chat!")));
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
                ChatUtil.send(player, "&7Đã huỷ nhập.");
                return;
            }
            if (!SAFE_TEXT.matcher(message).matches()) {
                ChatUtil.send(player, "&c✖ Dữ liệu không hợp lệ, vui lòng nhập lại.");
                return;
            }
            switch (type) {
                case "password" -> {
                    plugin.getSessionManager().setGuiPassword(player.getUniqueId(), message);
                    plugin.getSessionManager().setAwaitingChatInput(player.getUniqueId(), null);
                    ChatUtil.send(player, "&a✔ Đã lưu mật khẩu. Nhấn nút xanh lá để đăng nhập.");
                    reopenGui(player);
                }
                case "totp" -> {
                    plugin.getSessionManager().setGuiTotp(player.getUniqueId(), message);
                    plugin.getSessionManager().setAwaitingChatInput(player.getUniqueId(), null);
                    ChatUtil.send(player, "&a✔ Đã lưu mã 2FA. Nhấn nút xanh lá để đăng nhập.");
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
