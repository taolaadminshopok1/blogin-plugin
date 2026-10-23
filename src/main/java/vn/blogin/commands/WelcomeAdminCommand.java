package vn.blogin.commands;

import vn.blogin.Blogin;
import vn.blogin.welcome.WelcomeBossBar;
import vn.blogin.welcome.WelcomeManager;
import vn.blogin.welcome.WelcomeParticleEffect;
import vn.blogin.welcome.WelcomeTitle;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Lệnh admin {@code /bloginwelcome} - Test và quản lý hiệu ứng welcome.
 *
 * <p>Cú pháp:
 * <ul>
 *   <li>{@code /bloginwelcome test <new|back|login|register>} - Test hiệu ứng</li>
 *   <li>{@code /bloginwelcome particle <loại>} - Test 1 particle</li>
 *   <li>{@code /bloginwelcome title} - Test title typewriter</li>
 *   <li>{@code /bloginwelcome bossbar <gradient|pulse|progress|segmented>} - Test bossbar</li>
 *   <li>{@code /bloginwelcome countdown <giây>} - Đếm ngược 3-2-1-GO</li>
 * </ul>
 *
 * @author BloginVN
 */
public class WelcomeAdminCommand implements CommandExecutor {

    private final Blogin plugin;

    public WelcomeAdminCommand(Blogin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Lệnh này chỉ dành cho người chơi!");
            return true;
        }
        if (!player.hasPermission("blogin.admin")) {
            player.sendMessage("&cBạn không có quyền!");
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
                    player.sendMessage("&cSử dụng: /bloginwelcome test <new|back|login|register>");
                    return true;
                }
                switch (args[1].toLowerCase()) {
                    case "new" -> {
                        wm.playWelcomeSequence(player, true);
                        player.sendMessage("&a✔ Đã test welcome người chơi mới!");
                    }
                    case "back" -> {
                        wm.playWelcomeSequence(player, false);
                        player.sendMessage("&a✔ Đã test welcome người chơi cũ!");
                    }
                    case "login" -> {
                        wm.playLoginSuccessSequence(player);
                        player.sendMessage("&a✔ Đã test hiệu ứng đăng nhập thành công!");
                    }
                    case "register" -> {
                        wm.playRegisterSuccessSequence(player);
                        player.sendMessage("&a✔ Đã test hiệu ứng đăng ký!");
                    }
                    default -> player.sendMessage("&cLoại không hợp lệ!");
                }
            }
            case "particle", "p" -> {
                if (args.length < 2) {
                    player.sendMessage("&cSử dụng: /bloginwelcome particle <loại>");
                    player.sendMessage("&7Các loại: HEART, STAR, SPIRAL, CIRCLE, HELIX, FIREWORK, RAINBOW, WINGS, GALAXY, HEARTS_RAIN");
                    return true;
                }
                try {
                    WelcomeParticleEffect.EffectType type =
                            WelcomeParticleEffect.EffectType.valueOf(args[1].toUpperCase());
                    wm.getParticleEffect().playEffect(player, player.getLocation(), type, 5);
                    player.sendMessage("&a✔ Đang chạy particle: &e" + type);
                } catch (IllegalArgumentException e) {
                    player.sendMessage("&cLoại particle không hợp lệ!");
                }
            }
            case "title", "t" -> {
                wm.getTitleEffect().showTitle(player,
                        "&6&l✦ TEST TITLE ✦",
                        "&fSubtitle đẹp lung linh!",
                        10, 60, 20);
                wm.getTitleEffect().startActionBarTicker(player,
                        "&e&lActionBar &7» &fTest animation &e&l★", 60);
                player.sendMessage("&a✔ Đã test title + actionbar");
            }
            case "bossbar", "bb" -> {
                if (args.length < 2) {
                    player.sendMessage("&cSử dụng: /bloginwelcome bossbar <gradient|pulse|progress|segmented>");
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
                        player.sendMessage("&cLoại không hợp lệ!");
                        return true;
                    }
                }
                player.sendMessage("&a✔ Đã test bossbar: &e" + args[1]);
            }
            case "countdown", "cd" -> {
                int sec = args.length >= 2 ? Integer.parseInt(args[1]) : 3;
                wm.getTitleEffect().showCountdown(player, sec);
                player.sendMessage("&a✔ Đã bắt đầu đếm ngược " + sec + " giây!");
            }
            case "sound", "s" -> {
                wm.getSoundEffect().playWelcomeMelody(player);
                player.sendMessage("&a✔ Đã test âm thanh!");
            }
            case "all" -> {
                // Test tất cả hiệu ứng
                wm.playWelcomeSequence(player, true);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    wm.playLoginSuccessSequence(player);
                }, 80L);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    wm.playRegisterSuccessSequence(player);
                }, 160L);
                player.sendMessage("&a✔ Đã test tất cả hiệu ứng!");
            }
            default -> sendHelp(player);
        }
        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage("&6&l═══════ Blogin Welcome Test ═══════");
        player.sendMessage("&e/bloginwelcome test <new|back|login|register> &7- Test hiệu ứng");
        player.sendMessage("&e/bloginwelcome particle <loại> &7- Test particle");
        player.sendMessage("&e/bloginwelcome title &7- Test title + actionbar");
        player.sendMessage("&e/bloginwelcome bossbar <gradient|pulse|progress|segmented> &7- Test bossbar");
        player.sendMessage("&e/bloginwelcome countdown <giây> &7- Đếm ngược 3-2-1-GO");
        player.sendMessage("&e/bloginwelcome sound &7- Test âm thanh");
        player.sendMessage("&e/bloginwelcome all &7- Test tất cả");
        player.sendMessage("&6════════════════════════════════════════");
    }
}
