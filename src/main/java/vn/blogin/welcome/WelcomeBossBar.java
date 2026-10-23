package vn.blogin.welcome;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import vn.blogin.Blogin;

/**
 * Hệ thống BossBar chào mừng với hiệu ứng gradient màu và thanh progress động.
 *
 * <p>Các loại hiệu ứng BossBar:
 * <ul>
 *   <li>{@code GRADIENT} - Đổi màu liên tục qua các màu cầu vồng</li>
 *   <li>{@code PROGRESS_FILL} - Thanh progress chạy từ 0 đến 100%</li>
 *   <li>{@code PROGRESS_DRAIN} - Thanh progress chạy ngược lại</li>
 *   <li>{@code PULSE} - Nhịp đập tăng giảm</li>
 *   <li>{@code SEGMENTED} - Hiển thị kiểu chia đoạn</li>
 * </ul>
 *
 * @author BloginVN
 */
public class WelcomeBossBar {

    private final Blogin plugin;

    /**
     * Loại hiệu ứng BossBar.
     */
    public enum BossEffect {
        GRADIENT, PROGRESS_FILL, PROGRESS_DRAIN, PULSE, NONE
    }

    public WelcomeBossBar(Blogin plugin) {
        this.plugin = plugin;
    }

    /**
     * Hiển thị BossBar chào mừng với hiệu ứng gradient đổi màu.
     *
     * @param player   người chơi
     * @param message  nội dung
     * @param duration thời gian hiển thị (ticks)
     * @return task đang chạy
     */
    public BukkitTask showGradientBossBar(Player player, String message, int duration) {
        BossBar.Color[] colors = {
                BossBar.Color.RED, BossBar.Color.PINK, BossBar.Color.PURPLE,
                BossBar.Color.BLUE, BossBar.Color.GREEN, BossBar.Color.YELLOW,
                BossBar.Color.RED
        };

        // Tạo BossBar với Adventure API
        Component comp = LegacyComponentSerializer.legacyAmpersand().deserialize(message);
        BossBar bossBar = BossBar.bossBar(comp, 1.0f,
                BossBar.Color.PURPLE, BossBar.Overlay.PROGRESS);
        bossBar.addViewer(player);

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline()) {
                bossBar.removeViewer(player);
                return;
            }
            long time = System.currentTimeMillis() / 200;
            int idx = (int) (time % colors.length);
            bossBar.color(colors[idx]);
        }, 0L, 10L);

        // Auto remove sau duration
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            bossBar.removeViewer(player);
            task.cancel();
        }, duration);
        return task;
    }

    /**
     * BossBar progress chạy từ 0 → 100% rồi biến mất.
     */
    public BukkitTask showProgressFillBossBar(Player player, String message, int duration) {
        Component comp = LegacyComponentSerializer.legacyAmpersand().deserialize(message);
        BossBar bossBar = BossBar.bossBar(comp, 0.0f,
                BossBar.Color.GREEN, BossBar.Overlay.PROGRESS);
        bossBar.addViewer(player);

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline()) {
                bossBar.removeViewer(player);
                return;
            }
            long elapsed = (System.currentTimeMillis() - startTime) / 1000.0f;
            float progress = Math.min(1.0f, elapsed / (duration / 20.0f));
            bossBar.progress(progress);
        }, 0L, 2L);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            bossBar.removeViewer(player);
            task.cancel();
        }, duration);
        return task;
    }

    private long startTime = System.currentTimeMillis();

    /**
     * BossBar pulse - thanh progress đập nhịp 0.3 → 1.0 liên tục.
     */
    public BukkitTask showPulseBossBar(Player player, String message, int duration) {
        Component comp = LegacyComponentSerializer.legacyAmpersand().deserialize(message);
        BossBar bossBar = BossBar.bossBar(comp, 1.0f,
                BossBar.Color.WHITE, BossBar.Overlay.PROGRESS);
        bossBar.addViewer(player);

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline()) {
                bossBar.removeViewer(player);
                return;
            }
            double pulse = (Math.sin(System.currentTimeMillis() / 200.0) + 1) / 2; // 0..1
            float progress = (float) (0.3 + pulse * 0.7);
            bossBar.progress(progress);

            // Đổi màu theo nhịp
            BossBar.Color[] pulseColors = {BossBar.Color.RED, BossBar.Color.YELLOW,
                                            BossBar.Color.GREEN, BossBar.Color.BLUE};
            int idx = (int) ((System.currentTimeMillis() / 500) % pulseColors.length);
            bossBar.color(pulseColors[idx]);
        }, 0L, 2L);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            bossBar.removeViewer(player);
            task.cancel();
        }, duration);
        return task;
    }

    /**
     * BossBar segmented style - chia 10 đoạn.
     */
    public BukkitTask showSegmentedBossBar(Player player, String message, int duration) {
        Component comp = LegacyComponentSerializer.legacyAmpersand().deserialize(message);
        BossBar bossBar = BossBar.bossBar(comp, 1.0f,
                BossBar.Color.PINK, BossBar.Overlay.NOTCHED_10);
        bossBar.addViewer(player);

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline()) {
                bossBar.removeViewer(player);
                return;
            }
            // Hiệu ứng đoạn chạy
            int segments = (int) ((System.currentTimeMillis() / 300) % 11);
            float progress = segments / 10.0f;
            bossBar.progress(progress);
        }, 0L, 5L);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            bossBar.removeViewer(player);
            task.cancel();
        }, duration);
        return task;
    }

    /**
     * BossBar cố định hiển thị thời gian còn lại để đăng nhập.
     */
    public BukkitTask showLoginTimeoutBar(Player player, int durationSeconds) {
        Component comp = LegacyComponentSerializer.legacyAmpersand().deserialize(
                "&e⏰ &fThời gian đăng nhập còn lại");
        BossBar bossBar = BossBar.bossBar(comp, 1.0f,
                BossBar.Color.YELLOW, BossBar.Overlay.PROGRESS);
        bossBar.addViewer(player);

        final long start = System.currentTimeMillis();
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline()) {
                bossBar.removeViewer(player);
                return;
            }
            long elapsed = (System.currentTimeMillis() - start) / 1000;
            float remaining = Math.max(0, durationSeconds - elapsed);
            float progress = remaining / (float) durationSeconds;

            // Đổi màu theo thời gian còn lại
            if (remaining < 5) {
                bossBar.color(BossBar.Color.RED);
                comp = LegacyComponentSerializer.legacyAmpersand().deserialize(
                        "&c⏰ &fCòn &c" + remaining + "s &fđể đăng nhập!");
            } else if (remaining < 15) {
                bossBar.color(BossBar.Color.YELLOW);
                comp = LegacyComponentSerializer.legacyAmpersand().deserialize(
                        "&e⏰ &fCòn &e" + remaining + "s &fđể đăng nhập!");
            } else {
                bossBar.color(BossBar.Color.GREEN);
                comp = LegacyComponentSerializer.legacyAmpersand().deserialize(
                        "&a⏰ &fCòn &a" + remaining + "s &fđể đăng nhập!");
            }
            bossBar.name(comp);
            bossBar.progress(progress);
        }, 0L, 5L);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            bossBar.removeViewer(player);
            task.cancel();
        }, durationSeconds * 20L);
        return task;
    }
}
