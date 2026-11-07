package vn.blogin.commands;
import vn.blogin.Blogin;
import vn.blogin.utils.ChatUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
public class LoginGuiCommand implements CommandExecutor {
    private final Blogin plugin;
    public LoginGuiCommand(Blogin plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd,
                             String label, String[] args) {
        if (!(sender instanceof Player player)) {
            ChatUtil.send(sender, plugin.getMessageManager().get(
                    sender, "commands.chi-choi", "&cLệnh này chỉ dành cho người chơi!"));
            return true;
        }
        if (plugin.getSessionManager().isAuthenticated(player.getUniqueId())) {
            ChatUtil.send(player, plugin.getMessageManager().get(
                    player, "gui.hint.da-dang-nhap-roi", "&cBạn đã đăng nhập rồi!"));
            return true;
        }
        if (!plugin.getConfig().getBoolean("gui.bat", true)) {
            ChatUtil.send(player, plugin.getMessageManager().get(
                    player, "commands.gui.off", "&cGUI đăng nhập đang tắt trong cấu hình!"));
            ChatUtil.send(player, plugin.getMessageManager().get(
                    player, "commands.gui.use-login", "&7Dùng lệnh: &f/login <mật khẩu>"));
            return true;
        }
        plugin.getLoginGui().open(player);
        return true;
    }
}
