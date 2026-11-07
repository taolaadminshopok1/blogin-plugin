package vn.blogin.welcome;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitTask;
import vn.blogin.Blogin;
import vn.blogin.utils.ChatUtil;
import java.util.List;
public class WelcomeManager {
    private final Blogin plugin;
    private final WelcomeParticleEffect particleEffect;
    private final WelcomeTitle titleEffect;
    private final WelcomeSound soundEffect;
    private final WelcomeBossBar bossBarEffect;
    public enum WelcomeLevel {
        MINIMAL,
        NORMAL,
        EPIC,
        CUSTOM,
        DISABLED
    }
    public WelcomeManager(Blogin plugin) {
        this.plugin = plugin;
        this.particleEffect = new WelcomeParticleEffect(plugin);
        this.titleEffect = new WelcomeTitle(plugin);
        this.soundEffect = new WelcomeSound(plugin);
        this.bossBarEffect = new WelcomeBossBar(plugin);
    }
    public void playWelcomeSequence(Player player, boolean isNewPlayer) {
        WelcomeLevel level = getWelcomeLevel();
        if (level == WelcomeLevel.DISABLED) return;
        playWelcomeTitle(player, isNewPlayer);
        if (level != WelcomeLevel.MINIMAL) {
            playWelcomeParticles(player);
        }
        if (level == WelcomeLevel.NORMAL || level == WelcomeLevel.EPIC || level == WelcomeLevel.CUSTOM) {
            soundEffect.playStartupSound(player);
        } else if (level == WelcomeLevel.MINIMAL) {
            soundEffect.playSound(player,
                    org.bukkit.Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5f, 1.5f);
        }
        if (plugin.getConfig().getBoolean("welcome.actionbar.bat", true)) {
            String text;
            if (isNewPlayer) {
                text = plugin.getMessageManager().getWithPlaceholders(
                        player,
                        "welcome.actionbar.text-new",
                        "&a&l➤ &eChào mừng thành viên mới &b&l{player} &e&l➤",
                        "player", player.getName());
            } else {
                text = plugin.getMessageManager().getWithPlaceholders(
                        player,
                        "welcome.actionbar.text-back",
                        "&a&l➤ &eChào mừng trở lại &b&l{player} &e&l➤",
                        "player", player.getName());
            }
            int duration = plugin.getConfig().getInt("welcome.actionbar.duration", 100);
            titleEffect.startActionBarRainbow(player, text, duration);
        }
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
        if (level == WelcomeLevel.EPIC && plugin.getConfig().getBoolean("welcome.broadcast-join", true)) {
            broadcastJoinMessage(player, isNewPlayer);
        }
        if (plugin.getConfig().getBoolean("welcome.halo-neon.bat", false)) {
            startNeonHalo(player, plugin.getConfig().getInt("welcome.halo-neon.thoi-gian", 100));
        }
    }
    public void playLoginSuccessSequence(Player player) {
        WelcomeLevel level = getWelcomeLevel();
        if (level == WelcomeLevel.DISABLED) return;
        var titleCfg = plugin.getMessageManager().getWelcomeTitle(player, "welcome.title-login-thanh-cong");
        String line1 = plugin.getMessageManager().applyPlaceholders(titleCfg.line1(), "player", player.getName());
        String line2 = plugin.getMessageManager().applyPlaceholders(titleCfg.line2(), "player", player.getName());
        titleEffect.showTitle(player, line1, line2,
                titleCfg.fadeIn(), titleCfg.stay(), titleCfg.fadeOut());
        if (level != WelcomeLevel.MINIMAL) {
            String particleType = plugin.getConfig().getString("welcome.particle-login", "FIREWORK");
            try {
                WelcomeParticleEffect.EffectType type =
                        WelcomeParticleEffect.EffectType.valueOf(particleType.toUpperCase());
                burstParticles(player, type);
            } catch (IllegalArgumentException e) {
                burstParticles(player, WelcomeParticleEffect.EffectType.FIREWORK);
            }
        }
        soundEffect.playSuccessChime(player);
        if (level == WelcomeLevel.EPIC) {
            String text = plugin.getMessageManager().getWithPlaceholders(
                    player, "welcome.bossbar-login-thanh-cong", "&a&l✦ &eChào mừng &b{player} &a✦",
                    "player", player.getName());
            bossBarEffect.showGradientBossBar(player, text, 60);
        }
    }
    public void playRegisterSuccessSequence(Player player) {
        WelcomeLevel level = getWelcomeLevel();
        if (level == WelcomeLevel.DISABLED) return;
        var titleCfg = plugin.getMessageManager().getWelcomeTitle(player, "welcome.title-dang-ky-thanh-cong");
        String line1 = plugin.getMessageManager().applyPlaceholders(titleCfg.line1(), "player", player.getName());
        String line2 = plugin.getMessageManager().applyPlaceholders(titleCfg.line2(), "player", player.getName());
        titleEffect.showTitle(player, line1, line2,
                titleCfg.fadeIn(), titleCfg.stay(), titleCfg.fadeOut());
        if (level != WelcomeLevel.MINIMAL) {
            String particleType = plugin.getConfig().getString("welcome.particle-register", "HEARTS_RAIN");
            try {
                WelcomeParticleEffect.EffectType type =
                        WelcomeParticleEffect.EffectType.valueOf(particleType.toUpperCase());
                burstParticles(player, type);
            } catch (IllegalArgumentException e) {
                burstParticles(player, WelcomeParticleEffect.EffectType.HEARTS_RAIN);
            }
        }
        soundEffect.playFireworkSounds(player);
        if (level == WelcomeLevel.EPIC) {
            bossBarEffect.showPulseBossBar(player,
                    plugin.getMessageManager().getWithPlaceholders(
                            player,
                            "welcome.bossbar-dang-ky-thanh-cong",
                            "&6&l🎉 &eChào mừng thành viên mới &b{player} &6🎉",
                            "player", player.getName()),
                    100);
            if (plugin.getConfig().getBoolean("welcome.phao-hoa", true)) {
                int fireworks = plugin.getConfig().getInt("welcome.so-phao-hoa", 5);
                scheduleFireworks(player, fireworks);
            }
        }
        if (plugin.getConfig().getBoolean("welcome.broadcast-join", true)) {
            broadcastNewPlayer(player);
        }
    }
    public void startNeonHalo(Player player, int duration) {
        WelcomeParticleEffect.EffectType type = WelcomeParticleEffect.EffectType.PORTAL;
        // playEffect da tu dung sau duration ticks, khong can huy them.
        particleEffect.playEffect(player, player.getLocation(), type, duration / 20);
    }
    private void playWelcomeTitle(Player player, boolean isNewPlayer) {
        if (isNewPlayer) {
            titleEffect.showNewPlayerWelcome(player);
        } else {
            titleEffect.showWelcomeBack(player);
        }
    }
    private void playWelcomeParticles(Player player) {
        WelcomeParticleEffect.EffectType type = getRandomParticleType();
        // playEffect da tu dung sau 3 giay, khong can huy them.
        particleEffect.playEffect(player, player.getLocation(), type, 3);
    }
    private void burstParticles(Player player, WelcomeParticleEffect.EffectType type) {
        WelcomeParticleEffect.EffectType finalType = type;
        // playEffect da tu dung sau 2 giay, khong can huy them.
        particleEffect.playEffect(player, player.getLocation(), finalType, 2);
    }
    private void scheduleFireworks(Player player, int count) {
        for (int i = 0; i < count; i++) {
            final int delay = i * 20;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!player.isOnline()) return;
                spawnFireworkNear(player);
            }, delay);
        }
    }
    private void spawnFireworkNear(Player player) {
        try {
            Firework fw = player.getWorld().spawn(player.getLocation(), Firework.class);
            FireworkMeta meta = fw.getFireworkMeta();
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
            Bukkit.getScheduler().runTaskLater(plugin, fw::remove, 100L);
        } catch (Exception e) {
        }
    }
    private void broadcastJoinMessage(Player player, boolean isNewPlayer) {
        final String path;
        final String fallback;
        if (isNewPlayer) {
            path = "welcome.tin-chao-moi";
            fallback = "&6&l✦══════════════════════════════════✦\n"
                    + "&e&l   🌟 CHÀO MỪNG THÀNH VIÊN MỚI 🌟\n"
                    + "&6&l✦══════════════════════════════════✦\n"
                    + "&f   ▶ &b{player} &fđã gia nhập server!\n"
                    + "&7   Hãy chào hỏi và giúp đỡ họ nhé!\n"
                    + "&6&l✦══════════════════════════════════✦";
        } else {
            path = "welcome.tin-chao-lai";
            fallback = "&a&l➤ &e{player} &7đã quay lại server!";
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.getUniqueId().equals(player.getUniqueId())) {
                // Moi nguoi nhan dung ngon ngu cua ho.
                String msg = plugin.getMessageManager().getWithPlaceholders(
                        p, path, fallback, "player", player.getName());
                p.sendMessage(ChatUtil.parse(msg));
                if (isNewPlayer) {
                    soundEffect.playSound(p, org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.5f);
                }
            }
        }
    }
    private void broadcastNewPlayer(Player player) {
        final String fallback = "&6&l★═══════════════════════════════════★\n"
                + "&e   🎉 &b&l{player} &e&lvừa đăng ký tài khoản!\n"
                + "&7   Hãy chào mừng thành viên mới của chúng ta!\n"
                + "&6&l★═══════════════════════════════════★";
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.getUniqueId().equals(player.getUniqueId())) {
                String msg = plugin.getMessageManager().getWithPlaceholders(
                        p, "welcome.tin-dang-ky-thanh-cong", fallback, "player", player.getName());
                p.sendMessage(ChatUtil.parse(msg));
                soundEffect.playSound(p, org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.5f);
            }
        }
    }
    private WelcomeLevel getWelcomeLevel() {
        String level = plugin.getConfig().getString("welcome.muc-do", "EPIC").toUpperCase();
        try {
            return WelcomeLevel.valueOf(level);
        } catch (IllegalArgumentException e) {
            return WelcomeLevel.EPIC;
        }
    }
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
    public WelcomeParticleEffect getParticleEffect() { return particleEffect; }
    public WelcomeTitle getTitleEffect() { return titleEffect; }
    public WelcomeSound getSoundEffect() { return soundEffect; }
    public WelcomeBossBar getBossBarEffect() { return bossBarEffect; }
}
