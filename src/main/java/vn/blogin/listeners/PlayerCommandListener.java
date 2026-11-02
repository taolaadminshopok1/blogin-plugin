package vn.blogin.listeners;
import vn.blogin.Blogin;
import vn.blogin.utils.ChatUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
public class PlayerCommandListener implements Listener {
    private final Blogin plugin;
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
        String raw = event.getMessage();
        if (raw == null || raw.isEmpty()) return;
        String cmd = raw.substring(1).split(" ")[0].toLowerCase();
        if (cmd.startsWith("blogin") || cmd.equals("bl")) return;
        if (ALLOWED_COMMANDS.contains(cmd)) return;
        for (String allowed : plugin.getConfig().getStringList("gioi-han.lenh-cho-phep")) {
            if (allowed != null && allowed.equalsIgnoreCase(cmd)) return;
        }
        event.setCancelled(true);
        ChatUtil.send(player, plugin.getMessageManager().get(
                "canh-bao.lenh", "&c⚠ Vui lòng đăng nhập trước!"));
    }
}
