package vn.blogin.welcome;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;
import vn.blogin.Blogin;
import vn.blogin.utils.ChatUtil;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
public class WelcomeBossBar implements Listener {
    private final Blogin plugin;
    private final Map<UUID, BossBar> activeBars = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> activeTasks = new ConcurrentHashMap<>();
    public enum BossEffect {
        GRADIENT, PROGRESS_FILL, PROGRESS_DRAIN, PULSE, NONE
    }
    public WelcomeBossBar(Blogin plugin) {
        this.plugin = plugin;
    }
    public BukkitTask showGradientBossBar(Player player, String message, int duration) {
        BossBar.Color[] colors = {
                BossBar.Color.RED, BossBar.Color.PINK, BossBar.Color.PURPLE,
                BossBar.Color.BLUE, BossBar.Color.GREEN, BossBar.Color.YELLOW,
                BossBar.Color.RED
        };
        Component comp = ChatUtil.parse(message);
        BossBar bossBar = BossBar.bossBar(comp, 1.0f,
                BossBar.Color.PURPLE, BossBar.Overlay.PROGRESS);
        bossBar.addViewer(player);
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline()) {
                stop(player);
                return;
            }
            long time = System.currentTimeMillis() / 200;
            int idx = (int) (time % colors.length);
            bossBar.color(colors[idx]);
        }, 0L, 10L);
        return register(player, bossBar, task, duration);
    }
    public BukkitTask showProgressFillBossBar(Player player, String message, int duration) {
        Component comp = ChatUtil.parse(message);
        BossBar bossBar = BossBar.bossBar(comp, 0.0f,
                BossBar.Color.GREEN, BossBar.Overlay.PROGRESS);
        bossBar.addViewer(player);
        final long startTime = System.currentTimeMillis();
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline()) {
                stop(player);
                return;
            }
            float elapsed = (System.currentTimeMillis() - startTime) / 1000.0f;
            float progress = Math.min(1.0f, elapsed / (duration / 20.0f));
            bossBar.progress(progress);
        }, 0L, 2L);
        return register(player, bossBar, task, duration);
    }
    public BukkitTask showPulseBossBar(Player player, String message, int duration) {
        Component comp = ChatUtil.parse(message);
        BossBar bossBar = BossBar.bossBar(comp, 1.0f,
                BossBar.Color.WHITE, BossBar.Overlay.PROGRESS);
        bossBar.addViewer(player);
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline()) {
                stop(player);
                return;
            }
            double pulse = (Math.sin(System.currentTimeMillis() / 200.0) + 1) / 2;
            float progress = (float) (0.3 + pulse * 0.7);
            bossBar.progress(progress);
            BossBar.Color[] pulseColors = {BossBar.Color.RED, BossBar.Color.YELLOW,
                                            BossBar.Color.GREEN, BossBar.Color.BLUE};
            int idx = (int) ((System.currentTimeMillis() / 500) % pulseColors.length);
            bossBar.color(pulseColors[idx]);
        }, 0L, 2L);
        return register(player, bossBar, task, duration);
    }
    public BukkitTask showSegmentedBossBar(Player player, String message, int duration) {
        Component comp = ChatUtil.parse(message);
        BossBar bossBar = BossBar.bossBar(comp, 1.0f,
                BossBar.Color.PINK, BossBar.Overlay.NOTCHED_10);
        bossBar.addViewer(player);
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline()) {
                stop(player);
                return;
            }
            int segments = (int) ((System.currentTimeMillis() / 300) % 11);
            float progress = segments / 10.0f;
            bossBar.progress(progress);
        }, 0L, 5L);
        return register(player, bossBar, task, duration);
    }
    public BukkitTask showLoginTimeoutBar(Player player, int durationSeconds) {
        Component comp = ChatUtil.parse("&e⏰ &fThời gian đăng nhập còn lại");
        BossBar bossBar = BossBar.bossBar(comp, 1.0f,
                BossBar.Color.YELLOW, BossBar.Overlay.PROGRESS);
        bossBar.addViewer(player);
        final long start = System.currentTimeMillis();
        final String green = plugin.getMessageManager().get("welcome.bossbar-text-green",
                "&a⏰ &fCòn &a{time}s &fđể đăng nhập!");
        final String yellow = plugin.getMessageManager().get("welcome.bossbar-text-yellow",
                "&e⏰ &fCòn &e{time}s &fđể đăng nhập!");
        final String red = plugin.getMessageManager().get("welcome.bossbar-text-red",
                "&c⏰ &fCòn &c{time}s &fđể đăng nhập!");
        final BossBar.Color colorGreen = parseBossBarColor(
                plugin.getConfig().getString("welcome.bossbar-mau-xanh", "GREEN"));
        final BossBar.Color colorYellow = parseBossBarColor(
                plugin.getConfig().getString("welcome.bossbar-mau-vang", "YELLOW"));
        final BossBar.Color colorRed = parseBossBarColor(
                plugin.getConfig().getString("welcome.bossbar-mau-do", "RED"));
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline()) {
                stop(player);
                return;
            }
            long elapsed = (System.currentTimeMillis() - start) / 1000;
            long remaining = Math.max(0, durationSeconds - elapsed);
            float progress = durationSeconds <= 0 ? 0f : remaining / (float) durationSeconds;
            if (remaining < 5) {
                bossBar.color(colorRed);
                bossBar.name(ChatUtil.parse(plugin.getMessageManager()
                        .applyPlaceholders(red, "time", String.valueOf(remaining))));
            } else if (remaining < 15) {
                bossBar.color(colorYellow);
                bossBar.name(ChatUtil.parse(plugin.getMessageManager()
                        .applyPlaceholders(yellow, "time", String.valueOf(remaining))));
            } else {
                bossBar.color(colorGreen);
                bossBar.name(ChatUtil.parse(plugin.getMessageManager()
                        .applyPlaceholders(green, "time", String.valueOf(remaining))));
            }
            bossBar.progress(progress);
        }, 0L, 5L);
        return register(player, bossBar, task, (int) (durationSeconds * 20L));
    }
    private BossBar.Color parseBossBarColor(String name) {
        if (name == null || name.isBlank()) return BossBar.Color.YELLOW;
        try {
            return BossBar.Color.valueOf(name.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLog().warning("⚠ Màu BossBar không hợp lệ: \"" + name + "\"");
            return BossBar.Color.YELLOW;
        }
    }
    private BukkitTask register(Player player, BossBar bossBar, BukkitTask task, int duration) {
        UUID uuid = player.getUniqueId();
        stop(player);
        activeBars.put(uuid, bossBar);
        activeTasks.put(uuid, task);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            bossBar.removeViewer(player);
            BukkitTask current = activeTasks.remove(uuid);
            if (current != null) current.cancel();
            activeBars.remove(uuid);
        }, Math.max(1L, duration));
        return task;
    }
    public void stop(Player player) {
        if (player == null) return;
        UUID uuid = player.getUniqueId();
        BossBar bar = activeBars.remove(uuid);
        if (bar != null) bar.removeViewer(player);
        BukkitTask task = activeTasks.remove(uuid);
        if (task != null) task.cancel();
    }
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        stop(event.getPlayer());
    }
    public void shutdown() {
        for (UUID uuid : activeTasks.keySet()) {
            BukkitTask task = activeTasks.remove(uuid);
            if (task != null) task.cancel();
        }
        for (Map.Entry<UUID, BossBar> entry : activeBars.entrySet()) {
            Player viewer = Bukkit.getPlayer(entry.getKey());
            if (viewer != null) {
                entry.getValue().removeViewer(viewer);
            }
        }
        activeBars.clear();
    }
}
