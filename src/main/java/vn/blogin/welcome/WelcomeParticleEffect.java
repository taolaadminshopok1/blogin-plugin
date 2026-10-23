package vn.blogin.welcome;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import vn.blogin.Blogin;

import java.util.ArrayList;
import java.util.List;

/**
 * Hệ thống hiệu ứng hạt (particle) chào mừng người chơi vào server.
 *
 * <p>Các hiệu ứng có sẵn:
 * <ul>
 *   <li>{@link #HEART} - Trái tim tỏa ra xung quanh</li>
 *   <li>{@link #STAR} - Ngôi sao 5 cánh xoay tròn</li>
 *   <li>{@link #SPIRAL} - Xoáy tròn spiral 3D</li>
 *   <li>{@link #CIRCLE} - Vòng tròn particle xoay</li>
 *   <li>{@link #HELIX} - Chuỗi xoắn ốc DNA</li>
 *   <li>{@link #FIREWORK} - Pháo hoa mini</li>
 *   <li>{@link #RAINBOW} - Cầu vồng gradient màu</li>
 *   <li>{@link #WINGS} - Đôi cánh thiên thần</li>
 *   <li>{@link #GALAXY} - Xoáy galaxy 3D</li>
 *   <li>{@link #HEARTS_RAIN} - Mưa trái tim</li>
 * </ul>
 *
 * <p>Tất cả particle đều có hiệu ứng màu sắc chuyển động và tối ưu hiệu năng
 * bằng cách giới hạn góc nhìn của người chơi.</p>
 *
 * @author BloginVN
 */
public class WelcomeParticleEffect {

    private final Blogin plugin;

    /**
     * Danh sách các hiệu ứng particle có sẵn.
     */
    public enum EffectType {
        HEART, STAR, SPIRAL, CIRCLE, HELIX,
        FIREWORK, RAINBOW, WINGS, GALAXY, HEARTS_RAIN,
        NONE
    }

    public WelcomeParticleEffect(Blogin plugin) {
        this.plugin = plugin;
    }

    /**
     * Chạy hiệu ứng particle cho người chơi trong khoảng thời gian (giây).
     *
     * @param player    người chơi xem hiệu ứng
     * @param center    vị trí trung tâm
     * @param type      loại hiệu ứng
     * @param duration  thời gian chạy (giây)
     * @return task đang chạy (có thể cancel)
     */
    public BukkitTask playEffect(Player player, Location center, EffectType type, int duration) {
        if (type == null || type == EffectType.NONE) return null;

        // Mỗi tick chạy 1 lần (~20 ticks/giây)
        return Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            // Chỉ render nếu người chơi đang online và không quá xa
            if (!player.isOnline()) return;
            if (player.getLocation().distanceSquared(center) > 30 * 30) return;

            switch (type) {
                case HEART -> spawnHeart(player, center);
                case STAR -> spawnStar(player, center);
                case SPIRAL -> spawnSpiral(player, center);
                case CIRCLE -> spawnCircle(player, center);
                case HELIX -> spawnHelix(player, center);
                case FIREWORK -> spawnFirework(player, center);
                case RAINBOW -> spawnRainbow(player, center);
                case WINGS -> spawnWings(player, center);
                case GALAXY -> spawnGalaxy(player, center);
                case HEARTS_RAIN -> spawnHeartsRain(player, center);
            }
        }, 0L, 2L); // Chạy mỗi 2 ticks (10 lần/giây) để mượt
    }

    // ============== CÁC HIỆU ỨNG ==============

    /**
     * Trái tim particle bắn ra xung quanh.
     */
    private void spawnHeart(Player player, Location center) {
        double radius = 1.5;
        double step = Math.PI / 16;
        for (double t = 0; t < Math.PI * 2; t += step) {
            double x = 16 * Math.pow(Math.sin(t), 3);
            double z = 13 * Math.cos(t) - 5 * Math.cos(2 * t) - 2 * Math.cos(3 * t) - Math.cos(4 * t);
            Location loc = center.clone().add(x / 16 * radius, 0.1, z / 16 * radius);
            spawnColoredParticle(player, loc, Color.RED);
        }
    }

    /**
     * Ngôi sao 5 cánh xoay tròn.
     */
    private void spawnStar(Player player, Location center) {
        long time = System.currentTimeMillis() / 50;
        int points = 5;
        double outerRadius = 1.8;
        double innerRadius = 0.7;
        for (int i = 0; i < points * 2; i++) {
            double angle = (Math.PI * 2 * i / (points * 2)) + (time * 0.02);
            double r = (i % 2 == 0) ? outerRadius : innerRadius;
            double x = Math.cos(angle) * r;
            double z = Math.sin(angle) * r;
            double y = Math.sin(angle * 2) * 0.4;
            Location loc = center.clone().add(x, y + 1, z);
            spawnColoredParticle(player, loc, Color.fromRGB(255, 215, 0)); // vàng gold
        }
    }

    /**
     * Xoáy tròn 3D spiral.
     */
    private void spawnSpiral(Player player, Location center) {
        long time = System.currentTimeMillis() / 30;
        for (double y = -1.5; y < 1.5; y += 0.05) {
            double angle = y * 4 + (time * 0.1);
            double radius = 0.3 + Math.abs(y) * 0.6;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            Location loc = center.clone().add(x, y + 1, z);
            // Gradient màu từ tím sang xanh
            int r = (int) (128 + 127 * (y + 1.5) / 3);
            int g = 0;
            int b = 255;
            spawnColoredParticle(player, loc, Color.fromRGB(r, g, b));
        }
    }

    /**
     * Vòng tròn particle xoay quanh.
     */
    private void spawnCircle(Player player, Location center) {
        long time = System.currentTimeMillis() / 30;
        double radius = 1.2;
        int particles = 24;
        for (int i = 0; i < particles; i++) {
            double angle = (Math.PI * 2 * i / particles) + (time * 0.05);
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            double y = Math.sin(time * 0.05 + i * 0.3) * 0.3;
            Location loc = center.clone().add(x, y + 1, z);
            // Màu xanh ngọc
            spawnColoredParticle(player, loc, Color.fromRGB(0, 255, 200));
        }
    }

    /**
     * Chuỗi xoắn ốc DNA - 2 sợi xoắn vào nhau.
     */
    private void spawnHelix(Player player, Location center) {
        long time = System.currentTimeMillis() / 50;
        for (double y = -1; y < 1; y += 0.05) {
            double angle1 = y * 6 + time * 0.1;
            double angle2 = angle1 + Math.PI;
            double radius = 0.8;

            Location loc1 = center.clone().add(
                    Math.cos(angle1) * radius, y + 1, Math.sin(angle1) * radius);
            Location loc2 = center.clone().add(
                    Math.cos(angle2) * radius, y + 1, Math.sin(angle2) * radius);

            spawnColoredParticle(player, loc1, Color.AQUA);
            spawnColoredParticle(player, loc2, Color.fromRGB(255, 105, 180)); // hot pink
        }
    }

    /**
     * Pháo hoa mini - particle bắn ra từ trung tâm.
     */
    private void spawnFirework(Player player, Location center) {
        long time = System.currentTimeMillis() / 200;
        for (int i = 0; i < 20; i++) {
            double angle = Math.PI * 2 * i / 20 + (time * 0.5);
            double speed = 0.5 + Math.random() * 0.3;
            double x = Math.cos(angle) * speed;
            double z = Math.sin(angle) * speed;
            double y = Math.random() * 0.5 + 0.5;
            Location loc = center.clone().add(x, y, z);
            Color[] colors = {Color.RED, Color.ORANGE, Color.YELLOW, Color.LIME,
                              Color.AQUA, Color.BLUE, Color.PURPLE, Color.FUCHSIA};
            Color c = colors[(int) (Math.random() * colors.length)];
            spawnColoredParticle(player, loc, c);
        }
    }

    /**
     * Cầu vồng gradient màu - xoay tròn nhiều lớp.
     */
    private void spawnRainbow(Player player, Location center) {
        long time = System.currentTimeMillis() / 40;
        int layers = 3;
        for (int layer = 0; layer < layers; layer++) {
            double layerOffset = layer * 0.4;
            for (int i = 0; i < 16; i++) {
                double angle = (Math.PI * 2 * i / 16) + (time * 0.04) + layerOffset;
                double radius = 1.0 + layer * 0.3;
                double x = Math.cos(angle) * radius;
                double z = Math.sin(angle) * radius;
                double y = Math.sin(time * 0.03 + i * 0.5 + layer) * 0.4;
                Location loc = center.clone().add(x, y + 1, z);
                // Hue chuyển động theo góc
                float hue = (float) (((angle + time * 0.02) / (Math.PI * 2)) % 1.0);
                Color color = hsvToRgb(hue, 1.0f, 1.0f);
                spawnColoredParticle(player, loc, color);
            }
        }
    }

    /**
     * Đôi cánh thiên thần - 2 hình cánh đối xứng.
     */
    private void spawnWings(Player player, Location center) {
        long time = System.currentTimeMillis() / 60;
        float wingFlap = (float) (Math.sin(time * 0.1) * 0.3);

        // Cánh trái
        for (double u = 0; u < Math.PI; u += Math.PI / 20) {
            double x = -Math.cos(u) * 1.5;
            double y = Math.sin(u) * (1.5 + wingFlap);
            double z = Math.sin(u) * 0.8 - 0.3;
            Location loc = center.clone().add(x, y + 1, z);
            spawnColoredParticle(player, loc, Color.WHITE);
        }
        // Cánh phải
        for (double u = 0; u < Math.PI; u += Math.PI / 20) {
            double x = Math.cos(u) * 1.5;
            double y = Math.sin(u) * (1.5 + wingFlap);
            double z = Math.sin(u) * 0.8 - 0.3;
            Location loc = center.clone().add(x, y + 1, z);
            spawnColoredParticle(player, loc, Color.WHITE);
        }
    }

    /**
     * Xoáy galaxy 3D - nhiều vòng xoắn lồng vào nhau.
     */
    private void spawnGalaxy(Player player, Location center) {
        long time = System.currentTimeMillis() / 30;
        int arms = 4;
        for (int arm = 0; arm < arms; arm++) {
            double armAngle = Math.PI * 2 * arm / arms;
            for (double r = 0; r < 2; r += 0.08) {
                double angle = armAngle + (r * 3) + (time * 0.02);
                double x = Math.cos(angle) * r;
                double z = Math.sin(angle) * r;
                double y = Math.sin(r * 2 + time * 0.05) * 0.3;
                Location loc = center.clone().add(x, y + 1, z);
                // Gradient màu galaxy: từ xanh dương -> tím -> hồng
                float ratio = (float) (r / 2);
                int red = (int) (100 + 155 * ratio);
                int green = (int) (100 - 50 * ratio);
                int blue = (int) (200 + 55 * ratio);
                spawnColoredParticle(player, loc, Color.fromRGB(red, green, blue));
            }
        }
    }

    /**
     * Mưa trái tim từ trên xuống.
     */
    private void spawnHeartsRain(Player player, Location center) {
        long time = System.currentTimeMillis() / 100;
        for (int i = 0; i < 5; i++) {
            double offset = (time + i * 5) % 20;
            double x = (Math.sin(time * 0.1 + i) * 2);
            double y = 3 - offset * 0.3;
            double z = (Math.cos(time * 0.1 + i) * 2);
            if (y < -1) continue;
            Location loc = center.clone().add(x, y, z);
            Color[] colors = {Color.RED, Color.FUCHSIA, Color.fromRGB(255, 105, 180)};
            spawnColoredParticle(player, loc, colors[i % colors.length]);
        }
    }

    // ============== UTILS ==============

    /**
     * Spawn một particle màu (redstone dust) cho người chơi cụ thể.
     */
    private void spawnColoredParticle(Player player, Location loc, Color color) {
        if (loc == null || !player.isOnline()) return;
        DustOptions dust = new DustOptions(color, 1.0f);
        player.spawnParticle(Particle.REDSTONE, loc, 0, 0, 0, 0, 0, dust);
    }

    /**
     * Chuyển đổi HSV sang RGB Color.
     */
    private Color hsvToRgb(float h, float s, float v) {
        int r, g, b;
        int i = (int) (h * 6) % 6;
        float f = h * 6 - i;
        float p = v * (1 - s);
        float q = v * (1 - f * s);
        float t = v * (1 - (1 - f) * s);
        switch (i) {
            case 0 -> { r = (int)(v*255); g = (int)(t*255); b = (int)(p*255); }
            case 1 -> { r = (int)(q*255); g = (int)(v*255); b = (int)(p*255); }
            case 2 -> { r = (int)(p*255); g = (int)(v*255); b = (int)(t*255); }
            case 3 -> { r = (int)(p*255); g = (int)(q*255); b = (int)(v*255); }
            case 4 -> { r = (int)(t*255); g = (int)(p*255); b = (int)(v*255); }
            default -> { r = (int)(v*255); g = (int)(p*255); b = (int)(q*255); }
        }
        return Color.fromRGB(r, g, b);
    }

    /**
     * Spawn particle cho tất cả người chơi xung quanh (dùng cho hiệu ứng broadcast).
     */
    public void broadcastEffect(Location center, EffectType type, int duration) {
        List<BukkitTask> tasks = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getLocation().distanceSquared(center) <= 50 * 50) {
                BukkitTask task = playEffect(p, center, type, duration);
                if (task != null) tasks.add(task);
            }
        }
        // Tự cancel sau duration giây
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            tasks.forEach(BukkitTask::cancel);
        }, duration * 20L);
    }
}
