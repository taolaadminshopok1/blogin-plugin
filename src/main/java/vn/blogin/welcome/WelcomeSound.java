package vn.blogin.welcome;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import vn.blogin.Blogin;

import java.util.List;

/**
 * Hệ thống âm thanh chào mừng với hiệu ứng nhạc nâng cao.
 *
 * <p>Các loại âm thanh:
 * <ul>
 *   <li>{@code SINGLE} - Phát 1 âm thanh</li>
 *   <li>{@code MELODY} - Chuỗi nốt nhạc tạo giai điệu</li>
 *   <li>{@code HARMONY} - Nhiều nốt phát cùng lúc (hòa âm)</li>
 *   <li>{@code RISING} - Dãy nốt tăng dần</li>
 *   <li>{@code FALLING} - Dãy nốt giảm dần</li>
 *   <li>{@code FIREWORK} - Hiệu ứng pháo hoa</li>
 *   <li>{@code SUCCESS_CHIME} - Chuông thành công</li>
 * </ul>
 *
 * @author BloginVN
 */
public class WelcomeSound {

    private final Blogin plugin;

    public WelcomeSound(Blogin plugin) {
        this.plugin = plugin;
    }

    /**
     * Phát một âm thanh đơn lẻ.
     */
    public void playSound(Player player, Sound sound, float volume, float pitch) {
        if (sound == null) return;
        try {
            player.playSound(player.getLocation(), sound,
                    SoundCategory.MASTER, volume, pitch);
        } catch (Exception e) {
            // Fallback - dùng sound name string
            try {
                player.playSound(player.getLocation(), sound.name(),
                        SoundCategory.MASTER, volume, pitch);
            } catch (Exception ignored) { }
        }
    }

    /**
     * Phát chuỗi âm thanh liên tiếp (gõ nhẹ theo nhịp).
     */
    public BukkitTask playMelody(Player player, List<SoundStep> notes, int delayTicks) {
        return Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline()) return;
            long time = System.currentTimeMillis() / 50;
            int idx = (int) ((time / delayTicks) % notes.size());
            SoundStep step = notes.get(idx);
            playSound(player, step.sound(), step.volume(), step.pitch());
        }, 0L, delayTicks);
    }

    /**
     * Phát giai điệu chào mừng - chuỗi note C-E-G-C (Đô - Mi - Son - Đô).
     */
    public void playWelcomeMelody(Player player) {
        float[] pitches = {0.5f, 0.7f, 0.9f, 1.2f, 1.5f}; // Đô lên dần
        for (int i = 0; i < pitches.length; i++) {
            final int idx = i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!player.isOnline()) return;
                playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, pitches[idx]);
            }, i * 6L);
        }
    }

    /**
     * Phát hiệu ứng pháo hoa âm thanh.
     */
    public void playFireworkSounds(Player player) {
        // Tiếng phóng
        playSound(player, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 0.8f, 1.0f);
        // Tiếng nổ
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            playSound(player, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.5f, 1.2f);
            playSound(player, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 1.0f, 1.5f);
        }, 15L);
    }

    /**
     * Chuông thành công - khi đăng nhập thành công.
     */
    public void playSuccessChime(Player player) {
        // Hợp âm C-E-G
        float basePitch = 1.5f;
        playSound(player, Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, basePitch);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            playSound(player, Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, basePitch * 1.25f);
        }, 3L);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            playSound(player, Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, basePitch * 1.5f);
            playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.5f);
        }, 6L);
    }

    /**
     * Âm thanh thất bại - khi đăng nhập sai.
     */
    public void playFailSound(Player player) {
        playSound(player, Sound.ENTITY_VILLAGER_NO, 1.0f, 0.8f);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.5f);
        }, 5L);
    }

    /**
     * Âm thanh tích hợp lúc bắt đầu.
     */
    public void playStartupSound(Player player) {
        // C-E-G arpeggio
        playSound(player, Sound.BLOCK_NOTE_BLOCK_CHIME, 0.6f, 0.5f);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            playSound(player, Sound.BLOCK_NOTE_BLOCK_CHIME, 0.7f, 0.75f);
        }, 4L);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            playSound(player, Sound.BLOCK_NOTE_BLOCK_CHIME, 0.8f, 1.0f);
        }, 8L);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
        }, 12L);
    }

    /**
     * Phát cho toàn bộ server nghe (broadcast).
     */
    public void broadcastSound(Sound sound, float volume, float pitch) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            playSound(p, sound, volume, pitch);
        }
    }

    /**
     * Một bước trong giai điệu - sound + volume + pitch.
     */
    public record SoundStep(Sound sound, float volume, float pitch) {
        public static SoundStep of(Sound sound, float volume, float pitch) {
            return new SoundStep(sound, volume, pitch);
        }
    }
}
