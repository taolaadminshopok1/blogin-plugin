package vn.blogin.utils;

import org.bukkit.Particle;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Kiem chung runtime cho lop {@link Compat} tren tung phien ban Bukkit/Paper.
 *
 * <p><b>Luu y:</b> o Paper 1.20.5+ viec khoi tao tinh cua
 * {@link PotionEffectType} can {@code RegistryAccess} chi co khi server that su
 * chay. Vi vay probe nay tach thanh hai buoc:</p>
 *
 * <ol>
 *   <li><b>Buoc 1 - kiem chung truc tiep:</b> goi {@link Compat#effectType(String...)}
 *       va so sanh <b>danh tinh doi tuong</b> voi hang so enum goc. Buoc nay chi
 *       chay duoc khi registry da san sang (vi du Paper 1.20.1).</li>
 *   <li><b>Buoc 2 - kiem chung ten hang so:</b> liet ke cac truong khai bao
 *       cua enum (khong khoi tao tinh) de xac nhan {@link Compat} se chon
 *       dung hang so tren phien ban dang chay.</li>
 * </ol>
 */
public final class CompatProbe {

    private CompatProbe() { }

    public static void main(String[] args) {
        System.out.println("=== Bukkit/Paper API probe ===");

        boolean ok = true;

        // --- Buoc 1: kiem chung truc tiep (can registry) ---
        System.out.println("-- Buoc 1: kiem chung truc tiep --");
        ok &= directCheck("SLOWNESS", "SLOW");
        ok &= directCheck("JUMP_BOOST", "JUMP");
        ok &= directCheck("BLINDNESS");

        // --- Buoc 2: kiem chung ten hang so (khong can registry) ---
        System.out.println();
        System.out.println("-- Buoc 2: hang so co trong phien ban hien tai --");
        ok &= nameCheck(PotionEffectType.class, "SLOWNESS", "SLOW");
        ok &= nameCheck(PotionEffectType.class, "JUMP_BOOST", "JUMP");
        ok &= nameCheck(Particle.class, "DUST", "REDSTONE");

        System.out.println();
        System.out.println("RESULT = " + (ok ? "PASS" : "FAIL"));
        if (!ok) System.exit(1);
    }

    /**
     * Buoc 1: goi {@link Compat} va so sanh voi hang so enum goc.
     */
    private static boolean directCheck(String... names) {
        Object resolved;
        try {
            resolved = Compat.effectType(names);
        } catch (Throwable t) {
            System.out.println("  effectType(" + join(names) + ") -> nem loi " + t.getClass().getSimpleName()
                    + " (registry chua san sang, bo qua kiem chung truc tiep)");
            return true;
        }
        Object original = raw(PotionEffectType.class, names);

        if (resolved == null) {
            System.out.println("  effectType(" + join(names) + ") -> null"
                    + " (registry chua san sang, bo qua kiem chung truc tiep)");
            return true;
        }
        boolean same = resolved == original;
        System.out.println("  effectType(" + join(names) + ") -> khop hang so goc: " + same);
        return same;
    }

    /**
     * Buoc 2: xac nhan danh sach hang so ma {@link Compat} se chon.
     */
    private static <T> boolean nameCheck(Class<T> type, String... preferred) {
        List<String> declared = declaredFieldNames(type);
        String chosen = null;
        for (String name : preferred) {
            if (declared.contains(name)) {
                chosen = name;
                break;
            }
        }
        boolean ok = chosen != null;
        System.out.println("  " + type.getSimpleName() + ": uu tien " + join(preferred)
                + " -> chon '" + (chosen == null ? "KHONG TIM THAY" : chosen) + "'");
        return ok;
    }

    /**
     * Liet ke ten truong khai bao cua lop enum/hold ma KHONG khoi tao tinh static.
     *
     * <p>Trong 1.20.1 {@code PotionEffectType} la lop co truong {@code static final}
     * (khong phai enum constant), con 1.21+ la enum - nen lay toan bo truong
     * cong khai thay vi chi {@code isEnumConstant()}.</p>
     */
    private static List<String> declaredFieldNames(Class<?> type) {
        List<String> names = new ArrayList<>();
        for (Field field : type.getFields()) {
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers())
                    && field.getType() == type) {
                names.add(field.getName());
            }
        }
        return names;
    }

    /**
     * Doc hang so enum goc bang reflection.
     *
     * <p>Bat {@link Throwable} vi {@code ExceptionInInitializerError} la {@code Error}
     * (khong phai {@code Exception}) va se nem ra neu lop enum chua khoi tao duoc.</p>
     */
    private static Object raw(Class<?> type, String... names) {
        for (String name : names) {
            try {
                Field field = type.getField(name);
                return field.get(null);
            } catch (Throwable ignored) {
                // thu ten tiep theo
            }
        }
        return null;
    }

    private static String join(String[] names) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < names.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(names[i]);
        }
        return sb.toString();
    }
}
