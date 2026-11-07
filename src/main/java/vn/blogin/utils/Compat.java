package vn.blogin.utils;

import org.bukkit.Particle;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Field;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lop compat phien ban cho cac hang so Bukkit/Paper da doi kieu giua cac phien ban.
 *
 * <p><b>Ly do ton tai:</b> tu 1.20.5 tro len, Paper da chuyen {@code PotionEffectType}
 * va {@code Enchantment} tu enum sang abstract class, va cac hang so bi doi ten
 * (vi du {@code REDSTONE} -&gt; {@code DUST}, {@code LUCK} -&gt; {@code UNBREAKING}).
 * Bien dich <b>luon</b> chay tren API 1.20.1 de tao ra JAR chay duoc tren ca hai
 * phien ban cua nhat, nen khong the tham chieu truc tiep hang so co ten khac nhau.</p>
 *
 * <p><b>Quy tac quan trong:</b> khong bao gio <b>cache gia tri null</b>. Rieng
 * {@code PotionEffectType} can {@code RegistryAccess} san sang de khoi tao static
 * (chi co khi server that su chay). Neu cache null o lan goi dau tien thi moi
 * phien ban sau do se <b>khong bao gio</b> lay duoc hieu ung nua, vi du hieu ung
 * lam cham hay tang toc do nhay.</p>
 */
public final class Compat {

    private static final Map<String, Object> CACHE = new ConcurrentHashMap<>();

    private Compat() { }

    /**
     * Tra ve {@link PotionEffectType} dau tien co trong danh sach ten.
     *
     * @param names cac ten hang so theo thu tu uu tien (VD: {@code "JUMP_BOOST", "JUMP"})
     * @return hang so hoac {@code null} neu khong ton tai tren phien ban hien tai
     */
    public static PotionEffectType effectType(String... names) {
        return lookup(PotionEffectType.class, names);
    }

    /**
     * Tra ve {@link Particle} dau tien co trong danh sach ten.
     *
     * @param names cac ten hang so theo thu tu uu tien (VD: {@code "DUST", "REDSTONE"})
     * @return hang so hoac {@code null} neu khong ton tai tren phien ban hien tai
     */
    public static Particle particle(String... names) {
        return lookup(Particle.class, names);
    }

    /**
     * Tra ve {@link Enchantment} dau tien co trong danh sach ten.
     *
     * <p>Vi du {@code "UNBREAKING"} (1.21+) hoac {@code "DURABILITY"} (1.20.1).</p>
     *
     * @param names cac ten hang so theo thu tu uu tien
     * @return hang so hoac {@code null} neu khong ton tai tren phien ban hien tai
     */
    public static Enchantment enchantment(String... names) {
        return lookup(Enchantment.class, names);
    }

    /**
     * Bat hieu ung "lanh" (enchantment glow) cho vat pham ma <b>tuong thich moi phien ban</b>.
     *
     * <p>Khong dung {@code ItemMeta#setEnchantmentGlintOverride} vi API do chi ton tai
     * tu 1.20.5 tro len. Thay vao do ta gan enchantment qua {@link #enchantment(String...)}
     * roi an no bang {@link ItemFlag#HIDE_ENCHANTS} - cach nay hoat dong tren ca
     * 1.20.1 va 1.21.11+.</p>
     *
     * @param item vat pham can bat hieu ung (co the null)
     * @return {@code true} neu bat thanh cong
     */
    public static boolean applyGlint(ItemStack item) {
        if (item == null) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        Enchantment enchantment = enchantment("UNBREAKING", "DURABILITY");
        if (enchantment == null) return false;
        try {
            meta.addEnchant(enchantment, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
            return true;
        } catch (RuntimeException | LinkageError ignored) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T lookup(Class<T> type, String... names) {
        if (type == null || names == null) return null;
        for (String name : names) {
            if (name == null || name.isBlank()) continue;
            String normalized = name.trim().toUpperCase(Locale.ROOT);
            String key = type.getName() + '#' + normalized;

            Object cached = CACHE.get(key);
            if (cached != null) return (T) cached;

            Object value = resolve(type, normalized);
            if (value != null) {
                // Chi cache gia tri hop le - KHONG cache null (xem javadoc cua lop).
                CACHE.put(key, value);
                return (T) value;
            }
        }
        return null;
    }

    /**
     * Doc hang so goc bang reflection.
     *
     * <p>Bat {@link LinkageError} vi {@code ExceptionInInitializerError} va
     * {@code NoClassDefFoundError} la {@code Error} (khong phai {@code Exception}).
     * Neu de lan thu khong bat loi nay thi <b>plugin se khong load duoc</b> tren
     * phien ban moi.</p>
     */
    private static Object resolve(Class<?> type, String name) {
        try {
            Field field = type.getField(name);
            return field.get(null);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            // Hang so khong ton tai o phien ban hien tai -> thu ten khac
        } catch (LinkageError ignored) {
            // Lop enum/chua khoi tao duoc (vi du PotionEffectType can RegistryAccess
            // chi co khi server that su chay). Khong duong de exception lam plugin
            // khong load duoc -> tra null va thu lai o lan goi sau.
        }
        return null;
    }

    /**
     * Xoa cache (dung khi server vua khoi dong lai registry hoac khi reload).
     */
    public static void clearCache() {
        CACHE.clear();
    }
}
