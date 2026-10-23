package vn.blogin.commands;

import vn.blogin.Blogin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Lệnh {@code /captcha} - Lấy captcha mới khi bị sai hoặc mất.
 *
 * @author BloginVN
 */
public class CaptchaCommand implements CommandExecutor {

    private final Blogin plugin;

    public CaptchaCommand(Blogin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Lệnh này chỉ dành cho người chơi!");
            return true;
        }

        if (!plugin.getCaptchaManager().isEnabled()) {
            player.sendMessage("&cCaptcha hiện đang tắt!");
            return true;
        }

        // Cấp captcha mới
        String[] cap = plugin.getCaptchaManager().generate();
        plugin.getSessionManager().setPendingCaptcha(player.getUniqueId(), cap[0], cap[1]);
        player.sendMessage(plugin.getMessageManager().get(
                "captcha.tin-nhan-captcha", "&e[Captcha] &fMã captcha mới của bạn: &a&l{captcha}",
                "captcha", cap[0]));
        return true;
    }
}
