package vn.blogin.welcome;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import vn.blogin.Blogin;

import java.time.Duration;

/**
 * Hệ thống Title và ActionBar chào mừng rực rỡ.
 *
 * <p>Các loại hiệu ứng title:
 * <ul>
 *   <li>{@code TITLE_FADE} - Title fade in/out đơn giản</li>
 *   <li>{@code TITLE_TYPEWRITER} - Hiệu ứng đánh máy từng ký tự</li>
 *   <li>{@code TITLE_SLIDE_UP} - Trượt từ dưới lên</li>
 *   <li>{@code TITLE_RAINBOW} - Đổi màu cầu vồng liên tục</li>
 *   <li>{@code TITLE_BOUNCE} - Nảy lên xuống</li>
 *   <li>{@code TITLE_FIREWORK} - Title pháo hoa đa lớp</li>
 *   <li>{@code TITLE_TYPING} - Hiệu ứng gõ từng chữ</li>
 * </ul>
 *
 * <p>ActionBar:
 * <ul>
 *   <li>{@code ACTION_TICKER} - Chạy chữ marquee liên tục</li>
 *   <li>{@code ACTION_PULSE} - Nhịp đập fade in/out</li>
 *   <li>{@code ACTION_RAINBOW} - Đổi màu gradient</li>
 * </ul>
 *
 * @author BloginVN
 */
public class WelcomeTitle {

    private final Blogin plugin;

    public WelcomeTitle(Blogin plugin) {
        this.plugin = plugin;
    }

    // ============== TITLE EFFECTS ==============

    /**
     * Hiển thị title fade in/out đẹp mắt.
     */
    public void showTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        Component tComp = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
                .legacyAmpersand().deserialize(title);
        Component sComp = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
                .legacyAmpersand().deserialize(subtitle);

        Title.Times times = Title.Times.times(
                Duration.ofMillis(fadeIn * 50L),
                Duration.ofMillis(stay * 50L),
                Duration.ofMillis(fadeOut * 50L)
        );
        player.showTitle(Title.title(tComp, sComp, times));
    }

    /**
     * Hiệu ứng typewriter - đánh từng ký tự một.
     *
     * @param text      nội dung
     * @param charDelay delay giữa mỗi ký tự (ticks)
     */
    public void typewriterEffect(Player player, String text, int charDelay, TitlePosition position) {
        typewriterEffect(player, text, "", charDelay, position);
    }

    /**
     * Hiệu ứng typewriter cho cả title và subtitle.
     */
    public void typewriterEffect(Player player, String title, String subtitle,
                                  int charDelay, TitlePosition position) {
        // Fade in title
        Component tComp = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
                .legacyAmpersand().deserialize(title);
        Component sComp = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
                .legacyAmpersand().deserialize(subtitle);

        Title.Times times = Title.Times.times(
                Duration.ofMillis(500), Duration.ofMillis(title.length() * charDelay * 50L + 2000),
                Duration.ofMillis(1000)
        );
        player.showTitle(Title.title(tComp, sComp, times));

        // Đánh từng ký tự trên action bar
        String[] colors = {"&c", "&6", "&e", "&a", "&b", "&d", "&f"};
        for (int i = 0; i < title.length(); i++) {
            final int idx = i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!player.isOnline()) return;
                String partial = title.substring(0, idx + 1);
                String color = colors[idx % colors.length];
                String animated = color + partial + "&7" +
                        (idx < title.length() - 1 ? "_" : "");
                Component actionBar = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
                        .legacyAmpersand().deserialize(animated);
                player.sendActionBar(actionBar);
            }, i * charDelay);
        }
    }

    /**
     * Hiệu ứng rainbow title - đổi màu liên tục.
     */
    public void rainbowTitle(Player player, String title, String subtitle, int durationTicks) {
        String[] colors = {"&c", "&6", "&e", "&a", "&b", "&3", "&9", "&d", "&5", "&f"};
        for (int i = 0; i < durationTicks; i += 2) {
            final int idx = i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!player.isOnline()) return;
                String color = colors[(idx / 2) % colors.length];
                String rainbowTitle = color + title;
                showTitle(player, rainbowTitle, subtitle, 1, 3, 1);
            }, i);
        }
    }

    /**
     * Hiển thị welcome message typewriter cho người chơi mới (lần đầu vào).
     */
    public void showNewPlayerWelcome(Player player) {
        // Lấy title config từ config.yml
        String line2Template = plugin.getConfig().getString("welcome.title-chao.line2-new",
                "&b&l{player} &fđã gia nhập server!");
        String line2 = plugin.getMessageManager().applyPlaceholders(line2Template, "player", player.getName());

        // Animation typewriter - đổi màu line1 qua các màu khác nhau
        String[] colorPrefixes = {"&6&l", "&e&l", "&a&l", "&b&l", "&d&l"};
        for (int i = 0; i < colorPrefixes.length; i++) {
            final int idx = i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!player.isOnline()) return;
                String animated = colorPrefixes[idx] + "✦ &fCHÀO MỪNG &" + colorPrefixes[idx].charAt(1) + "✦";
                Component tComp = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
                        .legacyAmpersand().deserialize(animated);
                Component sComp = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
                        .legacyAmpersand().deserialize(line2);
                Title.Times times = Title.Times.times(
                        Duration.ofMillis(200),
                        Duration.ofMillis(500),
                        Duration.ofMillis(200)
                );
                player.showTitle(Title.title(tComp, sComp, times));
            }, i * 8L);
        }
    }

    /**
     * Hiển thị welcome message typewriter cho người chơi đã đăng ký (trở lại).
     */
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

        // ActionBar chạy chữ
        String abText = "&e&lBlogin &7» &fChào mừng &b" + player.getName() + " &fđã trở lại!";
        startActionBarTicker(player, abText, stay);
    }

    /**
     * ActionBar chạy chữ marquee liên tục.
     *
     * @param player   người chơi
     * @param text     nội dung
     * @param duration thời gian (ticks)
     * @return task đang chạy
     */
    public BukkitTask startActionBarTicker(Player player, String text, int duration) {
        return Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline()) return;
            // Tạo hiệu ứng dấu | chạy qua lại
            long time = System.currentTimeMillis() / 100;
            String[] anim = {"&7[&a█&7] &f", "&7[&a██&7] &f", "&7[&a███&7] &f", "&7[&a████&7] &f"};
            String prefix = anim[(int) (time % anim.length)];

            Component comp = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
                    .legacyAmpersand().deserialize(prefix + text);
            player.sendActionBar(comp);
        }, 0L, 5L);
    }

    /**
     * ActionBar pulse - nhịp đập fade in/out.
     */
    public BukkitTask startActionBarPulse(Player player, String text, int duration) {
        return Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline()) return;
            long time = System.currentTimeMillis() / 50;
            // Hiệu ứng ░▒▓█ tăng dần rồi giảm
            float pulse = (float) (Math.sin(time * 0.1) * 0.5 + 0.5); // 0 -> 1
            String anim;
            if (pulse < 0.25) anim = "&7▓░░░";
            else if (pulse < 0.5) anim = "&e▓▓░░";
            else if (pulse < 0.75) anim = "&6▓▓▓░";
            else anim = "&c▓▓▓▓";

            Component comp = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
                    .legacyAmpersand().deserialize(anim + " &r" + text);
            player.sendActionBar(comp);
        }, 0L, 3L);
    }

    /**
     * ActionBar rainbow - đổi màu gradient liên tục.
     */
    public BukkitTask startActionBarRainbow(Player player, String text, int duration) {
        String[] colors = {"&c", "&6", "&e", "&a", "&b", "&d"};
        return Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline()) return;
            long time = System.currentTimeMillis() / 200;
            String color = colors[(int) (time % colors.length)];
            Component comp = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
                    .legacyAmpersand().deserialize(color + text);
            player.sendActionBar(comp);
        }, 0L, 4L);
    }

    /**
     * Hiển thị đếm ngược 3-2-1-GO khi bắt đầu game.
     */
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

    /**
     * Phát âm thanh note piano.
     */
    private void playNoteSound(Player player, float pitch) {
        try {
            player.playSound(player.getLocation(),
                    org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING,
                    org.bukkit.SoundCategory.MASTER, 1.0f, pitch);
        } catch (Exception ignored) { }
    }

    /**
     * Vị trí hiển thị title (chỉ để tương thích API).
     */
    public enum TitlePosition {
        TITLE, SUBTITLE, ACTIONBAR
    }
}
