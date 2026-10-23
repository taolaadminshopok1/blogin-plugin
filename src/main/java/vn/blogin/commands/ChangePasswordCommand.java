package vn.blogin.commands;

import vn.blogin.Blogin;
import vn.blogin.managers.AuthManager.AuthResult;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Lệnh {@code /changepassword} - Đổi mật khẩu.
 *
 * <p>Yêu cầu người chơi đã đăng nhập.</p>
 *
 * <p>Cú pháp: {@code /changepassword <mật_khẩu_cũ> <mật_khẩu_mới>}</p>
 *
 * @author BloginVN
 */
public class ChangePasswordCommand implements CommandExecutor {

    private final Blogin plugin;

    public ChangePasswordCommand(Blogin plugin) {
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

        if (args.length < 2) {
            player.sendMessage("&cSử dụng: /changepassword <mật_khẩu_cũ> <mật_khẩu_mới>");
            return true;
        }

        String oldPwd = args[0];
        String newPwd = args[1];

        AuthResult result = plugin.getAuthManager().changePassword(player.getName(), oldPwd, newPwd);

        switch (result) {
            case SUCCESS -> player.sendMessage(plugin.getMessageManager().get(
                    "tin-nhan.mat-khau-da-thay-doi", "&a✔ Mật khẩu đã thay đổi!"));
            case WRONG_PASSWORD -> player.sendMessage(plugin.getMessageManager().get(
                    "tin-nhan.sai-mat-khau", "&cMật khẩu cũ không đúng!"));
            case INVALID_PASSWORD_FORMAT -> {
                if (newPwd.length() < plugin.getConfig().getInt("bao-mat.mat-khau-toi-thieu", 6)) {
                    player.sendMessage(plugin.getMessageManager().get(
                            "tin-nhan.mat-khau-qua-ngan", "&cMật khẩu quá ngắn!",
                            "min", String.valueOf(plugin.getConfig().getInt("bao-mat.mat-khau-toi-thieu", 6))));
                } else {
                    player.sendMessage(plugin.getMessageManager().get(
                            "tin-nhan.mat-khau-yeu", "&cMật khẩu không đủ mạnh!"));
                }
            }
            default -> player.sendMessage(plugin.getMessageManager().get(
                    "tin-nhan.loi-chung", "&cĐã xảy ra lỗi!"));
        }
        return true;
    }
}
