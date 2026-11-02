package vn.blogin.commands;
import vn.blogin.Blogin;
import vn.blogin.utils.ChatUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
public class CaptchaCommand implements CommandExecutor {
    private final Blogin plugin;
    public CaptchaCommand(Blogin plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd,
                             String label, String[] args) {
        if (!(sender instanceof Player player)) {
            ChatUtil.send(sender, "&cLệnh này chỉ dành cho người chơi!");
            return true;
        }
        if (!plugin.getCaptchaManager().isEnabled()) {
            ChatUtil.send(player, "&cCaptcha hiện đang tắt!");
            return true;
        }
        String[] cap = plugin.getCaptchaManager().generate();
        plugin.getSessionManager().setPendingCaptcha(player.getUniqueId(), cap[0], cap[1]);
        ChatUtil.send(player, plugin.getMessageManager().get(
                "captcha.tin-nhan-captcha", "&e[Captcha] &fMã captcha mới của bạn: &a&l{captcha}",
                "captcha", cap[0]));
        return true;
    }
}
