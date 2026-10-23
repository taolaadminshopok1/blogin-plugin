package vn.blogin.welcome;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitTask;
import vn.blogin.Blogin;

import java.util.ArrayList;
import java.util.List;

/**
 * WelcomeManager - điều phối tất cả hiệu ứng welcome một cách đồng bộ.
 *
 * <p>Kết hợp:
 * <ul>
 *   <li>{@link WelcomeParticleEffect} - Particle</li>
 *   <li>{@link WelcomeTitle} - Title & ActionBar</li>
 *   <li>{@link WelcomeSound} - Âm thanh</li>
 *   <li>{@link WelcomeBossBar} - BossBar</li>
 * </ul>
 *
 * <p>Có 3 gói hiệu ứng chính:
 * <ul>
 *   <li>{@code MINIMAL} - Chỉ title + sound ngắn gọn</li>
 *   <li>{@code NORMAL} - Title + particle + sound</li>
 *   <li>{@code EPIC} - Tất cả: title + particle + sound + bossbar + fireworks</li>
 * </ul>
 *
 * @author BloginVN
 */
public class WelcomeManager {

    private final Blogin plugin;
    private final WelcomeParticleEffect particleEffect;
    private final WelcomeTitle titleEffect;
    private final WelcomeSound soundEffect;
    private final WelcomeBossBar bossBarEffect;

    /**
     * Mức độ hiệu ứng welcome.
     */
    public enum WelcomeLevel {
        MINIMAL,   // Tối giản: title + âm thanh
        NORMAL,    // Bình thường: title + particle + âm thanh
        EPIC,      // Xịn xò: tất cả + fireworks + bossbar
        DISABLED   // Tắt
    }

    public WelcomeManager(Blogin plugin) {
        this.plugin = plugin;
        this.particleEffect = new WelcomeParticleEffect(plugin);
        this.titleEffect = new WelcomeTitle(plugin);
        this.soundEffect = new WelcomeSound(plugin);
        this.bossBarEffect = new WelcomeBossBar(plugin);
    }

    /**
     * Chạy toàn bộ hiệu ứng welcome cho người chơi vừa vào server.
     */
    public void playWelcomeSequence(Player player, boolean isNewPlayer) {
        WelcomeLevel level = getWelcomeLevel();
        if (level == WelcomeLevel.DISABLED) return;

        // 1. Hiệu ứng title (lấy từ config)
        playWelcomeTitle(player, isNewPlayer);

        // 2. Particle (chỉ NORMAL trở lên)
        if (level == WelcomeLevel.NORMAL || level == WelcomeLevel.EPIC) {
            playWelcomeParticles(player);
        }

        // 3. Âm thanh
        if (level == WelcomeLevel.NORMAL || level == WelcomeLevel.EPIC) {
            soundEffect.playStartupSound(player);
        } else if (level == WelcomeLevel.MINIMAL) {
            soundEffect.playSound(player,
                    org.bukkit.Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5f, 1.5f);
        }

        // 4. ActionBar (nếu được bật)
        if (plugin.getConfig().getBoolean("welcome.actionbar.bat", true)) {
            String text;
            if (isNewPlayer) {
                text = plugin.getMessageManager().getWithPlaceholders(
                        "welcome.actionbar.text-new",
                        "&a&l➤ &eChào mừng thành viên mới &b&l{player} &e&l➤",
                        "player", player.getName());
            } else {
                text = plugin.getMessageManager().getWithPlaceholders(
                        "welcome.actionbar.text-back",
                        "&a&l➤ &eChào mừng trở lại &b&l{player} &e&l➤",
                        "player", player.getName());
            }
            int duration = plugin.getConfig().getInt("welcome.actionbar.duration", 100);
            titleEffect.startActionBarRainbow(player, text, duration);
        }

        // 5. BossBar + pháo hoa (chỉ EPIC)
        if (level == WelcomeLevel.EPIC) {
            int timeout = plugin.getConfig().getInt("thoi-gian-dang-nhap", 60);
            if (plugin.getConfig().getBoolean("welcome.bossbar-timeout", true)) {
                bossBarEffect.showLoginTimeoutBar(player, timeout);
            }
            if (plugin.getConfig().getBoolean("welcome.phao-hoa", true)) {
                int fireworks = plugin.getConfig().getInt("welcome.so-phao-hoa", 5);
                scheduleFireworks(player, fireworks);
            }
        }

        // 6. Broadcast cho người chơi khác (chỉ EPIC)
        if (level == WelcomeLevel.EPIC && plugin.getConfig().getBoolean("welcome.broadcast-join", true)) {
            broadcastJoinMessage(player, isNewPlayer);
        }
    }

    /**
     * Chạy hiệu ứng khi đăng nhập thành công.
     */
    public void playLoginSuccessSequence(Player player) {
        WelcomeLevel level = getWelcomeLevel();
        if (level == WelcomeLevel.DISABLED) return;

        // Title "Đăng nhập thành công" - lấy từ config
        var titleCfg = plugin.getMessageManager().getWelcomeTitle("welcome.title-login-thanh-cong");
        String line1 = plugin.getMessageManager().applyPlaceholders(titleCfg.line1(), "player", player.getName());
        String line2 = plugin.getMessageManager().applyPlaceholders(titleCfg.line2(), "player", player.getName());
        titleEffect.showTitle(player, line1, line2,
                titleCfg.fadeIn(), titleCfg.stay(), titleCfg.fadeOut());

        // Particle burst - lấy từ config
        if (level == WelcomeLevel.NORMAL || level == WelcomeLevel.EPIC) {
            String particleType = plugin.getConfig().getString("welcome.particle-login", "FIREWORK");
            try {
                WelcomeParticleEffect.EffectType type =
                        WelcomeParticleEffect.EffectType.valueOf(particleType.toUpperCase());
                burstParticles(player, type);
            } catch (IllegalArgumentException e) {
                burstParticles(player, WelcomeParticleEffect.EffectType.FIREWORK);
            }
        }

        // Chuông thành công
        soundEffect.playSuccessChime(player);

        // BossBar ngắn (chỉ EPIC)
        if (level == WelcomeLevel.EPIC) {
            String text = "&a&l✦ &eChào mừng &b" + player.getName() + " &a✦";
            bossBarEffect.showGradientBossBar(player, text, 60);
        }
    }

    /**
     * Chạy hiệu ứng khi đăng ký thành công.
     */
    public void playRegisterSuccessSequence(Player player) {
        WelcomeLevel level = getWelcomeLevel();
        if (level == WelcomeLevel.DISABLED) return;

        // Title "Chào mừng thành viên mới" - lấy từ config
        var titleCfg = plugin.getMessageManager().getWelcomeTitle("welcome.title-dang-ky-thanh-cong");
        String line1 = plugin.getMessageManager().applyPlaceholders(titleCfg.line1(), "player", player.getName());
        String line2 = plugin.getMessageManager().applyPlaceholders(titleCfg.line2(), "player", player.getName());
        titleEffect.showTitle(player, line1, line2,
                titleCfg.fadeIn(), titleCfg.stay(), titleCfg.fadeOut());

        // Particle - lấy từ config
        if (level == WelcomeLevel.NORMAL || level == WelcomeLevel.EPIC) {
            String particleType = plugin.getConfig().getString("welcome.particle-register", "HEARTS_RAIN");
            try {
                WelcomeParticleEffect.EffectType type =
                        WelcomeParticleEffect.EffectType.valueOf(particleType.toUpperCase());
                burstParticles(player, type);
            } catch (IllegalArgumentException e) {
                burstParticles(player, WelcomeParticleEffect.EffectType.HEARTS_RAIN);
            }
        }

        // Âm thanh pháo hoa
        soundEffect.playFireworkSounds(player);

        // BossBar chúc mừng (chỉ EPIC)
        if (level == WelcomeLevel.EPIC) {
            bossBarEffect.showPulseBossBar(player,
                    "&6&l🎉 &eChào mừng thành viên mới &b" + player.getName() + " &6🎉",
                    100);
            if (plugin.getConfig().getBoolean("welcome.phao-hoa", true)) {
                int fireworks = plugin.getConfig().getInt("welcome.so-phao-hoa", 5);
                scheduleFireworks(player, fireworks);
            }
        }

        // Broadcast cho mọi người
        if (plugin.getConfig().getBoolean("welcome.broadcast-join", true)) {
            broadcastNewPlayer(player);
        }
    }

    /**
     * Hiển thị title welcome.
     */
    private void playWelcomeTitle(Player player, boolean isNewPlayer) {
        if (isNewPlayer) {
            titleEffect.showNewPlayerWelcome(player);
        } else {
            titleEffect.showWelcomeBack(player);
        }
    }

    /**
     * Particle welcome - chạy liên tục 3 giây.
     */
    private void playWelcomeParticles(Player player) {
        WelcomeParticleEffect.EffectType type = getRandomParticleType();
        BukkitTask task = particleEffect.playEffect(player, player.getLocation(), type, 3);

        if (task != null) {
            Bukkit.getScheduler().runTaskLater(plugin, task::cancel, 60L);
        }
    }

    /**
     * Burst particles - 1 lần phát nổ.
     */
    private void burstParticles(Player player, WelcomeParticleEffect.EffectType type) {
        WelcomeParticleEffect.EffectType finalType = type;
        BukkitTask task = particleEffect.playEffect(player, player.getLocation(), finalType, 2);
        if (task != null) {
            Bukkit.getScheduler().runTaskLater(plugin, task::cancel, 40L);
        }
    }

    /**
     * Lên lịch bắn pháo hoa thật (entity Firework).
     */
    private void scheduleFireworks(Player player, int count) {
        // Bắn pháo hoa quanh người chơi
        for (int i = 0; i < count; i++) {
            final int delay = i * 20;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!player.isOnline()) return;
                spawnFireworkNear(player);
            }, delay);
        }
    }

    /**
     * Spawn một pháo hoa thật ngay cạnh người chơi.
     */
    private void spawnFireworkNear(Player player) {
        try {
            Firework fw = player.getWorld().spawn(player.getLocation(), Firework.class);
            FireworkMeta meta = fw.getFireworkMeta();
            // Random hiệu ứng
            FireworkEffect.Type[] types = FireworkEffect.Type.values();
            FireworkEffect.Type type = types[(int) (Math.random() * types.length)];

            Color[] colors = {Color.RED, Color.AQUA, Color.YELLOW, Color.LIME,
                              Color.FUCHSIA, Color.ORANGE, Color.PURPLE};
            Color c1 = colors[(int) (Math.random() * colors.length)];
            Color c2 = colors[(int) (Math.random() * colors.length)];

            FireworkEffect effect = FireworkEffect.builder()
                    .with(type)
                    .withColor(c1)
                    .withFade(c2)
                    .withFlicker()
                    .withTrail()
                    .build();
            meta.addEffect(effect);
            meta.setPower(1 + (int) (Math.random() * 2));
            fw.setFireworkMeta(meta);

            // Tự hủy sau 5s để tránh rác
            Bukkit.getScheduler().runTaskLater(plugin, fw::remove, 100L);
        } catch (Exception e) {
            // Bỏ qua nếu lỗi
        }
    }

    /**
     * Broadcast thông báo có người chơi mới vào.
     */
    private void broadcastJoinMessage(Player player, boolean isNewPlayer) {
        String msg;
        if (isNewPlayer) {
            msg = plugin.getMessageManager().getWithPlaceholders(
                    "welcome.tin-chao-moi",
                    "&6&l✦══════════════════════════════════✦\n"
                            + "&e&l   🌟 CHÀO MỪNG THÀNH VIÊN MỚI 🌟\n"
                            + "&6&l✦══════════════════════════════════✦\n"
                            + "&f   ▶ &b{player} &fđã gia nhập server!\n"
                            + "&7   Hãy chào hỏi và giúp đỡ họ nhé!\n"
                            + "&6&l✦══════════════════════════════════✦",
                    "player", player.getName());
        } else {
            msg = plugin.getMessageManager().getWithPlaceholders(
                    "welcome.tin-chao-lai",
                    "&a&l➤ &e{player} &7đã quay lại server!",
                    "player", player.getName());
        }

        Component comp = LegacyComponentSerializer.legacyAmpersand().deserialize(msg);
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.getUniqueId().equals(player.getUniqueId())) {
                p.sendMessage(comp);
                // Phát âm thanh thông báo cho mọi người
                if (isNewPlayer) {
                    soundEffect.playSound(p, org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.5f);
                }
            }
        }
    }

    /**
     * Broadcast thông báo người chơi mới đăng ký.
     */
    private void broadcastNewPlayer(Player player) {
        String msg = plugin.getMessageManager().getWithPlaceholders(
                "welcome.tin-dang-ky-thanh-cong",
                "&6&l★═══════════════════════════════════★\n"
                        + "&e   🎉 &b&l{player} &e&lvừa đăng ký tài khoản!\n"
                        + "&7   Hãy chào mừng thành viên mới của chúng ta!\n"
                        + "&6&l★═══════════════════════════════════★",
                "player", player.getName());

        Component comp = LegacyComponentSerializer.legacyAmpersand().deserialize(msg);
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.getUniqueId().equals(player.getUniqueId())) {
                p.sendMessage(comp);
                soundEffect.playSound(p, org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.5f);
            }
        }
    }

    // ============== UTILS ==============

    /**
     * Lấy WelcomeLevel từ config.
     */
    private WelcomeLevel getWelcomeLevel() {
        String level = plugin.getConfig().getString("welcome.muc-do", "EPIC").toUpperCase();
        try {
            return WelcomeLevel.valueOf(level);
        } catch (IllegalArgumentException e) {
            return WelcomeLevel.EPIC;
        }
    }

    /**
     * Random chọn 1 loại particle từ danh sách trong config.
     */
    private WelcomeParticleEffect.EffectType getRandomParticleType() {
        List<String> types = plugin.getConfig().getStringList("welcome.particle-types");
        if (types.isEmpty()) {
            return WelcomeParticleEffect.EffectType.RAINBOW;
        }
        String chosen = types.get((int) (Math.random() * types.size()));
        try {
            return WelcomeParticleEffect.EffectType.valueOf(chosen.toUpperCase());
        } catch (IllegalArgumentException e) {
            return WelcomeParticleEffect.EffectType.RAINBOW;
        }
    }

    // ============== GETTERS ==============

    public WelcomeParticleEffect getParticleEffect() { return particleEffect; }
    public WelcomeTitle getTitleEffect() { return titleEffect; }
    public WelcomeSound getSoundEffect() { return soundEffect; }
    public WelcomeBossBar getBossBarEffect() { return bossBarEffect; }
}
