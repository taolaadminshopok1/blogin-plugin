package vn.blogin.commands;

import vn.blogin.Blogin;
import vn.blogin.managers.LanguageManager;
import vn.blogin.utils.ChatUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * Lenh {@code /blogin language <ma>} - doi ngon ngu cho nguoi choi.
 *
 * <p>Neu khong chi dinh ma, hien thi danh sach cac ngon ngu co san.</p>
 */
public class LanguageCommand implements CommandExecutor {

    private final Blogin plugin;

    public LanguageCommand(Blogin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            ChatUtil.send(sender, plugin.getMessageManager().get(
                    sender, "commands.chi-choi", "&cLệnh này chỉ dành cho người chơi!"));
            return true;
        }

        LanguageManager lang = plugin.getLanguageManager();

        if (args.length == 0) {
            showList(player, lang);
            return true;
        }

        String input = args[0];
        String code = lang.extractLanguageCode(input);

        if (code == null || !lang.hasLanguage(code)) {
            ChatUtil.send(player, plugin.getMessageManager().get(
                    player, "language.invalid",
                    "&cMã ngôn ngữ không hợp lệ! Dùng: &f/blogin language <mã>"));
            showList(player, lang);
            return true;
        }

        lang.setPlayerLanguage(player.getUniqueId(), code);
        ChatUtil.send(player, plugin.getMessageManager().get(
                player, "language.changed", "&a✔ Đã đổi ngôn ngữ sang &f{lang}",
                "lang", lang.getDisplayName(code)));
        return true;
    }

    private void showList(Player player, LanguageManager lang) {
        ChatUtil.send(player, plugin.getMessageManager().get(
                player, "language.header", "&6&l═══════ Ngôn ngữ Blogin &6&l═══════"));
        ChatUtil.send(player, plugin.getMessageManager().get(
                player, "language.current", "&eNgôn ngữ hiện tại của bạn: &f{lang}",
                "lang", lang.getDisplayName(lang.getLanguage(player))));
        ChatUtil.send(player, plugin.getMessageManager().get(
                player, "language.server-default", "&eNgôn ngữ mặc định của server: &f{lang}",
                "lang", lang.getDisplayName(lang.getDefaultLanguage())));

        StringBuilder list = new StringBuilder();
        for (Map.Entry<String, String> entry : lang.getDisplayNames().entrySet()) {
            if (list.length() > 0) list.append("&8, ");
            list.append("&f").append(entry.getKey())
                    .append(" &7(").append(entry.getValue()).append(")");
        }
        ChatUtil.send(player, plugin.getMessageManager().get(
                player, "language.available", "&eCác ngôn ngữ có sẵn: &f{list}",
                "list", list.toString()));
        ChatUtil.send(player, plugin.getMessageManager().get(
                player, "language.select", "&7Chọn một ngôn ngữ bằng cách nhập mã tương ứng:"));
        ChatUtil.send(player, plugin.getMessageManager().get(
                player, "language.footer", "&6&l═══════════════════════════════"));
    }
}
