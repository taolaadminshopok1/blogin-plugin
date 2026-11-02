package vn.blogin.commands;
import vn.blogin.Blogin;
import vn.blogin.utils.ChatUtil;
import vn.blogin.utils.PlayerRestrictionUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
public class LogoutCommand implements CommandExecutor {
    private final Blogin plugin;
    public LogoutCommand(Blogin plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd,
                             String label, String[] args) {
        if (!(sender instanceof Player player)) {
            ChatUtil.send(sender, "&cLệnh này chỉ dành cho người chơi!");
            return true;
        }
        if (!plugin.getSessionManager().isAuthenticated(player.getUniqueId())) {
            ChatUtil.send(player, plugin.getMessageManager().get(
                    "tin-nhan.chua-dang-nhap", "&c⚠ Bạn chưa đăng nhập!"));
            return true;
        }
        plugin.getSessionManager().logout(player.getUniqueId());
        String ip = player.getAddress() != null
                ? player.getAddress().getAddress().getHostAddress() : "unknown";
        plugin.getSessionManager().createSession(player.getUniqueId(), ip);
        ChatUtil.send(player, plugin.getMessageManager().get(
                "tin-nhan.da-dang-xuat", "&e👋 Đã đăng xuất khỏi tài khoản!"));
        PlayerRestrictionUtil.hideFromOthers(plugin, player);
        PlayerRestrictionUtil.apply(plugin, player);
        if (plugin.getCaptchaManager().isEnabled()) {
            String[] cap = plugin.getCaptchaManager().generate();
            plugin.getSessionManager().setPendingCaptcha(player.getUniqueId(), cap[0], cap[1]);
            ChatUtil.send(player, plugin.getMessageManager().get(
                    "captcha.tin-nhan-captcha", "&e[Captcha] &fVui lòng nhập mã: &a&l{captcha}",
                    "captcha", cap[0]));
        }
        return true;
    }
}
