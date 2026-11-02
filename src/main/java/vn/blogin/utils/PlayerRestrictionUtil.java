package vn.blogin.utils;
import vn.blogin.Blogin;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
public final class PlayerRestrictionUtil {
    private PlayerRestrictionUtil() { }
    private static final PotionEffectType JUMP_EFFECT = resolveEffect(
            "JUMP_BOOST", "JUMP");
    private static final PotionEffectType SLOW_EFFECT = resolveEffect(
            "SLOWNESS", "SLOW");
    private static PotionEffectType resolveEffect(String... names) {
        return Compat.effectType(names);
    }
    public static void apply(Blogin plugin, Player player) {
        if (player == null) return;
        if (plugin.getConfig().getBoolean("hieu-ung.blindness", true)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS,
                    Integer.MAX_VALUE, 0, false, false));
        }
        if (plugin.getConfig().getBoolean("hieu-ung.slow", true)) {
            int level = plugin.getConfig().getInt("hieu-ung.slow-level", 5);
            if (SLOW_EFFECT != null) {
                player.addPotionEffect(new PotionEffect(SLOW_EFFECT,
                        Integer.MAX_VALUE, level, false, false));
            }
        }
        if (plugin.getConfig().getBoolean("hieu-ung.jump-boost", true)) {
            int level = plugin.getConfig().getInt("hieu-ung.jump-boost-level", 128);
            if (JUMP_EFFECT != null) {
                player.addPotionEffect(new PotionEffect(JUMP_EFFECT,
                        Integer.MAX_VALUE, level, false, false));
            }
        }
    }
    public static void clearEffects(Player player) {
        if (player == null) return;
        player.removePotionEffect(PotionEffectType.BLINDNESS);
        if (SLOW_EFFECT != null) player.removePotionEffect(SLOW_EFFECT);
        if (JUMP_EFFECT != null) player.removePotionEffect(JUMP_EFFECT);
        player.setWalkSpeed(0.2f);
        player.setFlySpeed(0.1f);
    }
    public static void hideFromOthers(Blogin plugin, Player player) {
        if (player == null) return;
        for (Player other : player.getServer().getOnlinePlayers()) {
            if (other.getUniqueId().equals(player.getUniqueId())) continue;
            if (other.canSee(player)) {
                other.hidePlayer(plugin, player);
            }
        }
    }
    public static void showToOthers(Blogin plugin, Player player) {
        if (player == null) return;
        for (Player other : player.getServer().getOnlinePlayers()) {
            if (other.getUniqueId().equals(player.getUniqueId())) continue;
            if (!other.canSee(player)) {
                other.showPlayer(plugin, player);
            }
        }
    }
    public static void release(Blogin plugin, Player player) {
        clearEffects(player);
        showToOthers(plugin, player);
    }
}
