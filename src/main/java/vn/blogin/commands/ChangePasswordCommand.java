package vn.blogin.commands;

import vn.blogin.Blogin;
import vn.blogin.managers.AuthManager.AuthResult;
import vn.blogin.utils.ChatUtil;
import vn.blogin.utils.PasswordStrength;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChangePasswordCommand implements CommandExecutor {
    private final Blogin plugin;

    public ChangePasswordCommand(Blogin plugin) {
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
        if (!plugin.getSessionManager().isAuthenticated(player.getUniqueId())) {
            ChatUtil.send(player, plugin.getMessageManager().get(
                    player, "tin-nhan.chua-dang-nhap", "&c⚠ Bạn chưa đăng nhập!"));
            return true;
        }
        if (args.length < 2) {
            ChatUtil.send(player, plugin.getMessageManager().get(
                    player, "commands.changepassword.usage",
                    "&cSử dụng: /changepassword <mật khẩu cũ> <mật khẩu mới>"));
            return true;
        }
        String oldPwd = args[0];
        String newPwd = args[1];
        ChatUtil.send(player, plugin.getMessageManager().get(
                        player, "misc.do-ma-ngau.label-moi",
                        "&8» &7Độ mạnh mật khẩu mới: ")
                + PasswordStrength.bar(plugin, newPwd));
        plugin.getDatabaseManager().runAsync(
                () -> plugin.getAuthManager().changePassword(player.getName(), oldPwd, newPwd),
                result -> handleResult(player, result, newPwd)
        );
        return true;
    }

    private void handleResult(Player player, AuthResult result, String newPwd) {
        if (!player.isOnline()) return;
        switch (result) {
            case SUCCESS -> ChatUtil.send(player, plugin.getMessageManager().get(
                    player, "tin-nhan.mat-khau-da-thay-doi",
                    "&a✔ Mật khẩu đã được thay đổi thành công!"));
            case WRONG_PASSWORD -> ChatUtil.send(player, plugin.getMessageManager().get(
                    player, "tin-nhan.sai-mat-khau", "&c❌ Mật khẩu cũ không đúng!"));
            case WEAK_PASSWORD -> {
                ChatUtil.send(player, plugin.getMessageManager().get(
                        player, "tin-nhan.mat-khau-yeu", "&c❌ Mật khẩu mới quá yếu!"));
                ChatUtil.send(player, plugin.getMessageManager().get(
                        player, "misc.goi-y.chung",
                        "&7Gợi ý: kết hợp chữ hoa, chữ thường, số & ký tự đặc biệt."));
            }
            case INVALID_PASSWORD_FORMAT -> {
                int min = plugin.getConfig().getInt("bao-mat.mat-khau-toi-thieu", 6);
                int max = plugin.getConfig().getInt("bao-mat.mat-khau-toi-da", 64);
                if (newPwd.length() < min) {
                    ChatUtil.send(player, plugin.getMessageManager().get(
                            player, "tin-nhan.mat-khau-qua-ngan",
                            "&c❌ Mật khẩu phải có ít nhất {min} ký tự!", "min", String.valueOf(min)));
                } else if (newPwd.length() > max) {
                    ChatUtil.send(player, plugin.getMessageManager().get(
                            player, "tin-nhan.mat-khau-qua-dai",
                            "&c❌ Mật khẩu không được vượt quá {max} ký tự!", "max", String.valueOf(max)));
                } else {
                    ChatUtil.send(player, plugin.getMessageManager().get(
                            player, "tin-nhan.mat-khau-yeu",
                            "&c❌ Mật khẩu phải có chữ hoa, chữ thường và số!"));
                }
            }
            default -> ChatUtil.send(player, plugin.getMessageManager().get(
                    player, "tin-nhan.loi-chung", "&c✖ Đã xảy ra lỗi!"));
        }
    }
}
