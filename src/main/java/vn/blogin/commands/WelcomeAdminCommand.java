package vn.blogin.commands;
import vn.blogin.Blogin;
import vn.blogin.welcome.WelcomeManager;
import vn.blogin.welcome.WelcomeParticleEffect;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import vn.blogin.utils.ChatUtil;
import org.bukkit.entity.Player;
public class WelcomeAdminCommand implements CommandExecutor {
    private final Blogin plugin;
    public WelcomeAdminCommand(Blogin plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd,
                             String label, String[] args) {
        if (!(sender instanceof Player player)) {
            ChatUtil.send(sender, "&cLệnh này chỉ dành cho người chơi!");
            return true;
        }
        if (!player.hasPermission("blogin.admin")) {
            ChatUtil.send(player, "&cBạn không có quyền!");
            return true;
        }
        if (args.length == 0) {
            sendHelp(player);
            return true;
        }
        WelcomeManager wm = plugin.getWelcomeManager();
        switch (args[0].toLowerCase()) {
            case "test" -> {
                if (args.length < 2) {
                    ChatUtil.send(player, "&cSử dụng: /bloginwelcome test <new|back|login|register>");
                    return true;
                }
                switch (args[1].toLowerCase()) {
                    case "new" -> {
                        wm.playWelcomeSequence(player, true);
                        ChatUtil.send(player, "&a✔ Đã test welcome người chơi mới!");
                    }
                    case "back" -> {
                        wm.playWelcomeSequence(player, false);
                        ChatUtil.send(player, "&a✔ Đã test welcome người chơi cũ!");
                    }
                    case "login" -> {
                        wm.playLoginSuccessSequence(player);
                        ChatUtil.send(player, "&a✔ Đã test hiệu ứng đăng nhập thành công!");
                    }
                    case "register" -> {
                        wm.playRegisterSuccessSequence(player);
                        ChatUtil.send(player, "&a✔ Đã test hiệu ứng đăng ký!");
                    }
                    default -> ChatUtil.send(player, "&cLoại không hợp lệ!");
                }
            }
            case "particle", "p" -> {
                if (args.length < 2) {
                    ChatUtil.send(player, "&cSử dụng: /bloginwelcome particle <loại>");
                    ChatUtil.send(player, "&7Các loại: HEART, STAR, SPIRAL, CIRCLE, HELIX, FIREWORK, RAINBOW, WINGS, GALAXY, HEARTS_RAIN");
                    return true;
                }
                try {
                    WelcomeParticleEffect.EffectType type =
                            WelcomeParticleEffect.EffectType.valueOf(args[1].toUpperCase());
                    wm.getParticleEffect().playEffect(player, player.getLocation(), type, 5);
                    ChatUtil.send(player, "&a✔ Đang chạy particle: &e" + type);
                } catch (IllegalArgumentException e) {
                    ChatUtil.send(player, "&cLoại particle không hợp lệ!");
                }
            }
            case "title", "t" -> {
                wm.getTitleEffect().showTitle(player,
                        "&6&l✦ TEST TITLE ✦",
                        "&fSubtitle đẹp lung linh!",
                        10, 60, 20);
                wm.getTitleEffect().startActionBarTicker(player,
                        "&e&lActionBar &7» &fTest animation &e&l★", 60);
                ChatUtil.send(player, "&a✔ Đã test title + actionbar");
            }
            case "bossbar", "bb" -> {
                if (args.length < 2) {
                    ChatUtil.send(player, "&cSử dụng: /bloginwelcome bossbar <gradient|pulse|progress|segmented>");
                    return true;
                }
                switch (args[1].toLowerCase()) {
                    case "gradient", "g" -> wm.getBossBarEffect().showGradientBossBar(player,
                            "&a&l✦ GRADIENT BOSS BAR ✦", 100);
                    case "pulse", "p" -> wm.getBossBarEffect().showPulseBossBar(player,
                            "&e&l✦ PULSE BOSS BAR ✦", 100);
                    case "progress", "pr" -> wm.getBossBarEffect().showProgressFillBossBar(player,
                            "&b&l✦ PROGRESS FILL ✦", 100);
                    case "segmented", "s" -> wm.getBossBarEffect().showSegmentedBossBar(player,
                            "&d&l✦ SEGMENTED ✦", 100);
                    default -> {
                        ChatUtil.send(player, "&cLoại không hợp lệ!");
                        return true;
                    }
                }
                ChatUtil.send(player, "&a✔ Đã test bossbar: &e" + args[1]);
            }
            case "countdown", "cd" -> {
                int sec = 3;
                if (args.length >= 2) {
                    try {
                        sec = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        ChatUtil.send(player, "&cSố giây không hợp lệ!");
                        return true;
                    }
                }
                sec = Math.max(1, Math.min(10, sec));
                wm.getTitleEffect().showCountdown(player, sec);
                ChatUtil.send(player, "&a✔ Đã bắt đầu đếm ngược " + sec + " giây!");
            }
            case "sound", "s" -> {
                wm.getSoundEffect().playWelcomeMelody(player);
                ChatUtil.send(player, "&a✔ Đã test âm thanh!");
            }
            case "all" -> {
                wm.playWelcomeSequence(player, true);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    wm.playLoginSuccessSequence(player);
                }, 80L);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    wm.playRegisterSuccessSequence(player);
                }, 160L);
                ChatUtil.send(player, "&a✔ Đã test tất cả hiệu ứng!");
            }
            default -> sendHelp(player);
        }
        return true;
    }
    private void sendHelp(Player player) {
        ChatUtil.send(player, "&6&l═══════ Blogin Welcome Test ═══════");
        ChatUtil.send(player, "&e/bloginwelcome test <new|back|login|register> &7- Test hiệu ứng");
        ChatUtil.send(player, "&e/bloginwelcome particle <loại> &7- Test particle");
        ChatUtil.send(player, "&e/bloginwelcome title &7- Test title + actionbar");
        ChatUtil.send(player, "&e/bloginwelcome bossbar <gradient|pulse|progress|segmented> &7- Test bossbar");
        ChatUtil.send(player, "&e/bloginwelcome countdown <giây> &7- Đếm ngược 3-2-1-GO");
        ChatUtil.send(player, "&e/bloginwelcome sound &7- Test âm thanh");
        ChatUtil.send(player, "&e/bloginwelcome all &7- Test tất cả");
        ChatUtil.send(player, "&6════════════════════════════════════════");
    }
}
