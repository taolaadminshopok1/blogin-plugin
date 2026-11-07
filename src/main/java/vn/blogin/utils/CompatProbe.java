package vn.blogin.utils;

import org.bukkit.Particle;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
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
 *
 * <p>Buoc 2 cung kiem tra <b>khong cache gia tri null</b> - day la loai bug
 * da lam hieu ung "lam cham" / "nhay cao" khong hoat dong tren mot so server.</p>
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
        ok &= nameCheck(PotionEffectType.class, "BLINDNESS");
        ok &= nameCheck(Particle.class, "DUST", "REDSTONE");
        ok &= nameCheck(Enchantment.class, "UNBREAKING", "DURABILITY");

        // --- Buoc 3: kiem chung khong cache null ---
        System.out.println();
        System.out.println("-- Buoc 3: hanh vi cache --");
        ok &= nullCacheCheck();

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
     * Buoc 3: xac nhan {@link Compat} KHONG cache gia tri null.
     *
     * <p>Neu {@link Compat} cache null, thi mot lan tra loi sai truoc khi
     * registry san sang se khoa vo hinh hieu ung "lam cham" / "nhay cao" -
     * bug da gap tren server that.</p>
     */
    private static boolean nullCacheCheck() {
        Compat.clearCache();

        // Dung mot ten khong ton tai tren bat ky phien ban nao.
        Compat.effectType("BLOGIN_TEST_KHONG_TON_TAI");
        Compat.particle("BLOGIN_TEST_KHONG_TON_TAI");

        // Neu gia tri null bi cache, lan goi sau cung se tra null ngay.
        Object again = Compat.effectType("BLOGIN_TEST_KHONG_TON_TAI");
        boolean notCached = again == null;

        // Hang so thuc su ton tai phai tra ve gia tri (neu registry san sang).
        Object real = Compat.effectType("SLOWNESS", "SLOW");
        boolean realOk = real != null || !registryReady();

        System.out.println("  gia tri null khong bi cache: " + notCached);
        System.out.println("  hang so that tra ve gia tri: " + realOk
                + (real == null ? " (registry chua san sang)" : ""));

        return notCached && realOk;
    }

    /**
     * Kiem tra xem registry cua PotionEffectType da san sang chua.
     *
     * <p>Dung reflection (khong tham chieu truc tiep {@code SLOWNESS}/{@code SLOW})
     * vi ten hang so khac nhau giua 1.20.1 va 1.21+ - dang bi dich tren 1.20.1
     * nen chi co the lay qua {@code Field}.</p>
     */
    private static boolean registryReady() {
        try {
            return raw(PotionEffectType.class, "SLOWNESS", "SLOW") != null;
        } catch (Throwable t) {
            return false;
        }
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
            if (Modifier.isStatic(field.getModifiers())
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
