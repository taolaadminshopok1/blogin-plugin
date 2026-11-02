package vn.blogin.welcome;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import vn.blogin.Blogin;
import vn.blogin.utils.ChatUtil;
import java.time.Duration;
public class WelcomeTitle {
    private final Blogin plugin;
    public WelcomeTitle(Blogin plugin) {
        this.plugin = plugin;
    }
    public void showTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        Component tComp = ChatUtil.parse(title);
        Component sComp = ChatUtil.parse(subtitle);
        Title.Times times = Title.Times.times(
                Duration.ofMillis(fadeIn * 50L),
                Duration.ofMillis(stay * 50L),
                Duration.ofMillis(fadeOut * 50L)
        );
        player.showTitle(Title.title(tComp, sComp, times));
    }
    public void typewriterEffect(Player player, String text, int charDelay, TitlePosition position) {
        typewriterEffect(player, text, "", charDelay, position);
    }
    public void typewriterEffect(Player player, String title, String subtitle,
                                  int charDelay, TitlePosition position) {
        Component tComp = ChatUtil.parse(title);
        Component sComp = ChatUtil.parse(subtitle);
        int typeDuration = title.length() * Math.max(1, charDelay);
        Title.Times times = Title.Times.times(
                Duration.ofMillis(500),
                Duration.ofMillis(typeDuration * 50L + 2000),
                Duration.ofMillis(1000)
        );
        player.showTitle(Title.title(tComp, sComp, times));
        String[] colors = {"&c", "&6", "&e", "&a", "&b", "&d", "&f"};
        for (int i = 0; i < title.length(); i++) {
            final int idx = i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!player.isOnline()) return;
                String partial = title.substring(0, idx + 1);
                String color = colors[idx % colors.length];
                String animated = color + partial + "&7"
                        + (idx < title.length() - 1 ? "_" : "");
                player.sendActionBar(ChatUtil.parse(animated));
            }, i * (long) Math.max(1, charDelay));
        }
    }
    public void rainbowTitle(Player player, String title, String subtitle, int durationTicks) {
        String[] colors = {"&c", "&6", "&e", "&a", "&b", "&3", "&9", "&d", "&5", "&f"};
        for (int i = 0; i < durationTicks; i += 2) {
            final int idx = i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!player.isOnline()) return;
                String color = colors[(idx / 2) % colors.length];
                showTitle(player, color + title, subtitle, 1, 3, 1);
            }, i);
        }
    }
    public void showNewPlayerWelcome(Player player) {
        String line2Template = plugin.getConfig().getString("welcome.title-chao.line2-new",
                "&b&l{player} &fđã gia nhập server!");
        String line2 = plugin.getMessageManager().applyPlaceholders(line2Template, "player", player.getName());
        String[] colorPrefixes = {"&6&l", "&e&l", "&a&l", "&b&l", "&d&l"};
        for (int i = 0; i < colorPrefixes.length; i++) {
            final int idx = i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!player.isOnline()) return;
                String animated = colorPrefixes[idx] + "✦ &fCHÀO MỪNG &" + colorPrefixes[idx].charAt(1) + "✦";
                Component tComp = ChatUtil.parse(animated);
                Component sComp = ChatUtil.parse(line2);
                Title.Times times = Title.Times.times(
                        Duration.ofMillis(200),
                        Duration.ofMillis(500),
                        Duration.ofMillis(200)
                );
                player.showTitle(Title.title(tComp, sComp, times));
            }, i * 8L);
        }
    }
    public void showWelcomeBack(Player player) {
        String line1 = plugin.getConfig().getString("welcome.title-chao.line1",
                "&6&l✦ &e&lCHÀO MỪNG &6&l✦");
        String line2Template = plugin.getConfig().getString("welcome.title-chao.line2-back",
                "&b&l{player} &7đã quay lại!");
        String line2 = plugin.getMessageManager().applyPlaceholders(line2Template, "player", player.getName());
        int fadeIn = plugin.getConfig().getInt("welcome.title-chao.fade-in", 10);
        int stay = plugin.getConfig().getInt("welcome.title-chao.stay", 60);
        int fadeOut = plugin.getConfig().getInt("welcome.title-chao.fade-out", 20);
        showTitle(player, line1, line2, fadeIn, stay, fadeOut);
        String abText = plugin.getMessageManager().getWithPlaceholders(
                "welcome.actionbar.text-back-short",
                "&e&lBlogin &7» &fChào mừng &b{player} &fđã trở lại!",
                "player", player.getName());
        startActionBarTicker(player, abText, stay);
    }
    public BukkitTask startActionBarTicker(Player player, String text, int duration) {
        final long endTime = System.currentTimeMillis() + duration * 50L;
        return Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline() || System.currentTimeMillis() > endTime) return;
            long time = System.currentTimeMillis() / 100;
            String[] anim = {"&7[&a█&7] &f", "&7[&a██&7] &f", "&7[&a███&7] &f", "&7[&a████&7] &f"};
            String prefix = anim[(int) (time % anim.length)];
            player.sendActionBar(ChatUtil.parse(prefix + text));
        }, 0L, 5L);
    }
    public BukkitTask startActionBarPulse(Player player, String text, int duration) {
        final long endTime = System.currentTimeMillis() + duration * 50L;
        return Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline() || System.currentTimeMillis() > endTime) return;
            long time = System.currentTimeMillis() / 50;
            float pulse = (float) (Math.sin(time * 0.1) * 0.5 + 0.5);
            String anim;
            if (pulse < 0.25) anim = "&7▓░░░";
            else if (pulse < 0.5) anim = "&e▓▓░░";
            else if (pulse < 0.75) anim = "&6▓▓▓░";
            else anim = "&c▓▓▓▓";
            player.sendActionBar(ChatUtil.parse(anim + " &r" + text));
        }, 0L, 3L);
    }
    public BukkitTask startActionBarRainbow(Player player, String text, int duration) {
        final long endTime = System.currentTimeMillis() + duration * 50L;
        String[] colors = {"&c", "&6", "&e", "&a", "&b", "&d"};
        return Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline() || System.currentTimeMillis() > endTime) return;
            long time = System.currentTimeMillis() / 200;
            String color = colors[(int) (time % colors.length)];
            player.sendActionBar(ChatUtil.parse(color + text));
        }, 0L, 4L);
    }
    public void showCountdown(Player player, int seconds) {
        for (int i = seconds; i > 0; i--) {
            final int count = i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!player.isOnline()) return;
                String color = switch (count) {
                    case 1 -> "&c&l";
                    case 2 -> "&6&l";
                    case 3 -> "&e&l";
                    default -> "&a&l";
                };
                showTitle(player,
                        color + count,
                        count == 1 ? "&a&lGO!" : "&7Chuẩn bị...",
                        0, 25, 0);
                playNoteSound(player, 1.0f + (seconds - count) * 0.2f);
            }, (seconds - i) * 25L);
        }
    }
    private void playNoteSound(Player player, float pitch) {
        try {
            player.playSound(player.getLocation(),
                    org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING,
                    org.bukkit.SoundCategory.MASTER, 1.0f, pitch);
        } catch (Exception ignored) { }
    }
    public enum TitlePosition {
        TITLE, SUBTITLE, ACTIONBAR
    }
}
