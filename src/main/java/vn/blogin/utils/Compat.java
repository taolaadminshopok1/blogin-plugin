package vn.blogin.utils;
import org.bukkit.Particle;
import org.bukkit.potion.PotionEffectType;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
public final class Compat {
    private static final Map<String, Object> CACHE = new HashMap<>();
    private Compat() { }
    public static PotionEffectType effectType(String... names) {
        if (names == null) return null;
        for (String name : names) {
            PotionEffectType type = lookup(PotionEffectType.class, name);
            if (type != null) return type;
        }
        return null;
    }
    public static Particle particle(String... names) {
        if (names == null) return null;
        for (String name : names) {
            Particle particle = lookup(Particle.class, name);
            if (particle != null) return particle;
        }
        return null;
    }
    @SuppressWarnings("unchecked")
    private static <T> T lookup(Class<T> type, String name) {        if (type == null || name == null || name.isBlank()) return null;
        String key = type.getName() + '#' + name;
        synchronized (CACHE) {
            if (CACHE.containsKey(key)) {
                Object cached = CACHE.get(key);
                return cached == null ? null : (T) cached;
            }
        }
        Object value = null;
        try {
            Field field = type.getField(name.trim().toUpperCase());
            value = field.get(null);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            // Hang so khong ton tai o phien ban hien tai -> thu ten khac
        } catch (LinkageError ignored) {
            // Lop enum chua khoi tao duoc (vi du PotionEffectType tren Paper 1.21+
            // can RegistryAccess chi co khi server that su chay).
            // Khong duong de exception lam plugin khong load duoc -> tra null.
        }
        synchronized (CACHE) {
            CACHE.put(key, value);
        }
        return type.cast(value);
    }
    public static void clearCache() {
        synchronized (CACHE) {
            CACHE.clear();
        }
    }
}
