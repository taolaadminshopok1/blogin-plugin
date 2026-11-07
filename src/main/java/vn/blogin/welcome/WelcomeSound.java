package vn.blogin.welcome;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import vn.blogin.Blogin;
import java.util.List;
public class WelcomeSound {
    private final Blogin plugin;
    public WelcomeSound(Blogin plugin) {
        this.plugin = plugin;
    }
    public void playSound(Player player, Sound sound, float volume, float pitch) {
        if (sound == null) return;
        try {
            player.playSound(player.getLocation(), sound,
                    SoundCategory.MASTER, volume, pitch);
        } catch (Exception e) {
            try {
                player.playSound(player.getLocation(), sound.name(),
                        SoundCategory.MASTER, volume, pitch);
            } catch (Exception ignored) { }
        }
    }
    public BukkitTask playMelody(Player player, List<SoundStep> notes, int delayTicks) {
        return Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline()) return;
            long time = System.currentTimeMillis() / 50;
            int idx = (int) ((time / delayTicks) % notes.size());
            SoundStep step = notes.get(idx);
            playSound(player, step.sound(), step.volume(), step.pitch());
        }, 0L, delayTicks);
    }
    public void playWelcomeMelody(Player player) {
        float[] pitches = {0.5f, 0.7f, 0.9f, 1.2f, 1.5f};
        for (int i = 0; i < pitches.length; i++) {
            final int idx = i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!player.isOnline()) return;
                playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, pitches[idx]);
            }, i * 6L);
        }
    }
    public void playFireworkSounds(Player player) {
        playSound(player, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 0.8f, 1.0f);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            playSound(player, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.5f, 1.2f);
            playSound(player, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 1.0f, 1.5f);
        }, 15L);
    }
    public void playSuccessChime(Player player) {
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
    public void playFailSound(Player player) {
        playSound(player, Sound.ENTITY_VILLAGER_NO, 1.0f, 0.8f);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.5f);
        }, 5L);
    }
    public void playStartupSound(Player player) {
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
    public void broadcastSound(Sound sound, float volume, float pitch) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            playSound(p, sound, volume, pitch);
        }
    }
    public record SoundStep(Sound sound, float volume, float pitch) {
        public static SoundStep of(Sound sound, float volume, float pitch) {
            return new SoundStep(sound, volume, pitch);
        }
    }
}
