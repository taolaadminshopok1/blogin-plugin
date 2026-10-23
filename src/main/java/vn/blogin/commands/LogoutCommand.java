package vn.blogin.commands;

import vn.blogin.Blogin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Lệnh {@code /logout} - Đăng xuất khỏi tài khoản.
 *
 * @author BloginVN
 */
public class LogoutCommand implements CommandExecutor {

    private final Blogin plugin;

    public LogoutCommand(Blogin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Lệnh này chỉ dành cho người chơi!");
            return true;
        }

        if (!plugin.getSessionManager().isAuthenticated(player.getUniqueId())) {
            player.sendMessage(plugin.getMessageManager().get(
                    "tin-nhan.chua-dang-nhap", "&cBạn chưa đăng nhập!"));
            return true;
        }

        plugin.getSessionManager().logout(player.getUniqueId());
        // Tạo session mới với trạng thái chưa auth
        String ip = player.getAddress() != null
                ? player.getAddress().getAddress().getHostAddress() : "unknown";
        plugin.getSessionManager().createSession(player.getUniqueId(), ip);

        player.sendMessage(plugin.getMessageManager().get(
                "tin-nhan.da-dang-xuat", "&e👋 Đã đăng xuất!"));

        // Cấp captcha mới cho lần đăng nhập kế tiếp (nếu bật)
        if (plugin.getCaptchaManager().isEnabled()) {
            String[] cap = plugin.getCaptchaManager().generate();
            plugin.getSessionManager().setPendingCaptcha(player.getUniqueId(), cap[0], cap[1]);
            player.sendMessage(plugin.getMessageManager().get(
                    "captcha.tin-nhan-captcha", "&e[Captcha] &fVui lòng nhập mã: &a&l{captcha}",
                    "captcha", cap[0]));
        }
        return true;
    }
}
