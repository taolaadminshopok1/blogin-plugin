package vn.blogin.listeners;

import vn.blogin.Blogin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Chặn các lệnh khi chưa đăng nhập (trừ các lệnh của Blogin).
 *
 * @author BloginVN
 */
public class PlayerCommandListener implements Listener {

    private final Blogin plugin;

    /**
     * Danh sách các lệnh được phép dùng khi chưa đăng nhập.
     */
    private static final Set<String> ALLOWED_COMMANDS = new HashSet<>(Arrays.asList(
            "login", "dangnhap",
            "register", "dangky", "reg",
            "captcha", "cap",
            "2fa", "totp", "otp",
            "blogin", "bl"
    ));

    public PlayerCommandListener(Blogin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (plugin.getSessionManager().isAuthenticated(player.getUniqueId())) return;

        if (!plugin.getConfig().getBoolean("gioi-han.lenh", true)) return;

        String cmd = event.getMessage().split(" ")[0].toLowerCase().replace("/", "");
        // Bỏ alias (vd: "/loginme" -> "loginme") - kiểm tra exact match
        if (ALLOWED_COMMANDS.contains(cmd)) return;

        event.setCancelled(true);
        player.sendMessage(plugin.getMessageManager().get(
                "canh-bao.lenh", "&cVui lòng đăng nhập trước!"));
    }
}
