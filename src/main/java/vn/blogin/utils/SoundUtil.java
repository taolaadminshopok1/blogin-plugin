package vn.blogin.utils;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
public final class SoundUtil {
    private SoundUtil() { }
    public static boolean play(vn.blogin.Blogin plugin, Player player, String key,
                               Sound fallbackSound, float fallbackVolume, float fallbackPitch) {
        if (plugin == null || player == null || key == null) return false;
        if (!plugin.getConfig().getBoolean("am-thanh." + key + ".bat", true)) {
            return false;
        }
        String soundName = plugin.getConfig().getString("am-thanh." + key + ".am-thanh", "");
        float volume = (float) plugin.getConfig().getDouble("am-thanh." + key + ".am-luong", fallbackVolume);
        float pitch = (float) plugin.getConfig().getDouble("am-thanh." + key + ".pitch", fallbackPitch);
        Sound sound = fallbackSound;
        if (soundName != null && !soundName.isBlank()) {
            Sound parsed = parseSound(soundName);
            if (parsed != null) {
                sound = parsed;
            } else {
                plugin.getLog().warning("⚠ Âm thanh không hợp lệ trong config: am-thanh."
                        + key + ".am-thanh = \"" + soundName + "\"");
            }
        }
        if (sound == null) return false;
        try {
            player.playSound(player.getLocation(), sound, volume, pitch);
            return true;
        } catch (Exception e) {
            plugin.getLog().warning("⚠ Không phát được âm thanh " + sound + ": " + e.getMessage());
            return false;
        }
    }
    public static boolean play(vn.blogin.Blogin plugin, Player player, String key,
                               Sound fallbackSound) {
        return play(plugin, player, key, fallbackSound, 1.0f, 1.0f);
    }
    public static Sound parseSound(String name) {
        if (name == null || name.isBlank()) return null;
        String n = name.trim();
        try {
            return Sound.valueOf(n.toUpperCase());
        } catch (IllegalArgumentException ignored) {
        }
        StringBuilder sb = new StringBuilder();
        for (String part : n.toLowerCase().split("[.:\\s]+")) {
            if (part.isEmpty()) continue;
            sb.append(Character.toUpperCase(part.charAt(0)))
              .append(part.substring(1).toUpperCase())
              .append('_');
        }
        String candidate = sb.toString();
        while (candidate.endsWith("_")) {
            candidate = candidate.substring(0, candidate.length() - 1);
        }
        try {
            return Sound.valueOf(candidate);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
