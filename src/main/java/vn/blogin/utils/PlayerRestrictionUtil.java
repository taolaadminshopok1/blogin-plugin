package vn.blogin.utils;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import vn.blogin.Blogin;

/**
 * Tien ich ap dung / go bo cac hieu ung chan nguoi choi chua dang nhap.
 *
 * <p><b>Luu y quan trong:</b> hieu ung duoc tra ve <b>lazy</b> (giai lai thuoc tinh
 * moi lan goi) thay vi giu trong static final. Ly do: {@link PotionEffectType} tren
 * Paper 1.20.5+ can {@code RegistryAccess} de khoi tao static - neu goi qua
 * {@code Enum.valueOf} hay field {@code static final} o thoi diem class load ma
 * registry chua san sang, gia tri se la {@code null} <b>vinh vien</b> va hieu ung
 * "lam cham" / "nhay cao" se khong bao gio hoat dong.</p>
 */
public final class PlayerRestrictionUtil {

    private PlayerRestrictionUtil() { }

    /**
     * Tra ve hieu ung tang toc do nhay, hoac {@code null} neu phien ban hien tai
     * khong co hang so tuong ung.
     */
    public static PotionEffectType jumpEffect() {
        return Compat.effectType("JUMP_BOOST", "JUMP");
    }

    /**
     * Tra ve hieu ung lam cham, hoac {@code null} neu phien ban hien tai
     * khong co hang so tuong ung.
     */
    public static PotionEffectType slowEffect() {
        return Compat.effectType("SLOWNESS", "SLOW");
    }

    /**
     * Ap day hieu ung chan cho nguoi choi chua dang nhap (mù, chậm, nhảy cao).
     */
    public static void apply(Blogin plugin, Player player) {
        if (player == null) return;

        if (plugin.getConfig().getBoolean("hieu-ung.blindness", true)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS,
                    Integer.MAX_VALUE, 0, false, false));
        }

        if (plugin.getConfig().getBoolean("hieu-ung.slow", true)) {
            int level = plugin.getConfig().getInt("hieu-ung.slow-level", 5);
            PotionEffectType slow = slowEffect();
            if (slow != null) {
                player.addPotionEffect(new PotionEffect(slow,
                        Integer.MAX_VALUE, level, false, false));
            }
        }

        if (plugin.getConfig().getBoolean("hieu-ung.jump-boost", true)) {
            int level = plugin.getConfig().getInt("hieu-ung.jump-boost-level", 128);
            PotionEffectType jump = jumpEffect();
            if (jump != null) {
                player.addPotionEffect(new PotionEffect(jump,
                        Integer.MAX_VALUE, level, false, false));
            }
        }
    }

    /**
     * Go bo tat ca hieu ung do plugin ap dat.
     */
    public static void clearEffects(Player player) {
        if (player == null) return;
        player.removePotionEffect(PotionEffectType.BLINDNESS);

        PotionEffectType slow = slowEffect();
        if (slow != null) player.removePotionEffect(slow);

        PotionEffectType jump = jumpEffect();
        if (jump != null) player.removePotionEffect(jump);

        player.setWalkSpeed(0.2f);
        player.setFlySpeed(0.1f);
    }

    /**
     * An nguoi choi khoi cam xa cua nguoi choi khac.
     */
    public static void hideFromOthers(Blogin plugin, Player player) {
        if (player == null) return;
        for (Player other : player.getServer().getOnlinePlayers()) {
            if (other.getUniqueId().equals(player.getUniqueId())) continue;
            if (other.canSee(player)) {
                other.hidePlayer(plugin, player);
            }
        }
    }

    /**
     * Hien lai nguoi choi cho nguoi choi khac.
     */
    public static void showToOthers(Blogin plugin, Player player) {
        if (player == null) return;
        for (Player other : player.getServer().getOnlinePlayers()) {
            if (other.getUniqueId().equals(player.getUniqueId())) continue;
            if (!other.canSee(player)) {
                other.showPlayer(plugin, player);
            }
        }
    }

    /**
     * Go chan hoan toan cho nguoi choi vua dang nhap thanh cong.
     */
    public static void release(Blogin plugin, Player player) {
        clearEffects(player);
        showToOthers(plugin, player);
    }
}
