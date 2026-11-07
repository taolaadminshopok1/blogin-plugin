package vn.blogin.managers;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import vn.blogin.Blogin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Quan ly viec tai & tra cuoi cac tep ngon ngu (multi-language).
 *
 * <p>Moi tep ngon ngu nam trong {@code plugins/Blogin/lang/&lt;ma&gt;.yml}. Khi
 * plugin khoi dong, cac tep mac dinh di kem trong JAR se duoc giai nen ra
 * thu muc {@code lang/} neu chua ton tai - nho do nguoi chu se duoc tu sua
 * them ma khong bi ghi de khi cap nhat plugin.</p>
 *
 * <p>Thu tu tuong doi cho nguoi choi duoc xac dinh theo thu tu sau:</p>
 * <ol>
 *   <li>Luu chon rieng cua nguoi choi ({@code /blogin language <ma>})</li>
 *   <li>Ngon ngu client cua nguoi choi ({@link Player#getLocale()})</li>
 *   <li>Ngon ngu mac dinh trong {@code config.yml} ({@code ngon-ngu})</li>
 * </ol>
 */
public class LanguageManager {

    private final Blogin plugin;

    /** Ma ngon ngu -> tep cau hinh da load. */
    private final Map<String, YamlConfiguration> languages = new ConcurrentHashMap<>();

    /** Ma ngon ngu -> ten hien thi (tieng Anh) cho danh sach lenh. */
    private final Map<String, String> displayNames = Collections.synchronizedMap(new LinkedHashMap<>());

    /** UUID nguoi choi -> ma ngon ngu da chon. */
    private final Map<UUID, String> playerLanguages = new ConcurrentHashMap<>();

    /** Ngon ngu mac dinh dung chung cho server. */
    private String defaultLanguage = "vi";

    /** Ngon ngu cua ngon ngu mac dinh - dung lam fallback cuoi cung. */
    private static final String FALLBACK_LANGUAGE = "en";

    /**
     * Anh xa ma ngon ngu client Minecraft -> ma tep ngon ngu cua plugin.
     *
     * <p>Chi giu lai nhung ngon ngu plugin thuc su co tep. Client tra ve
     * {@code vi_vn}, {@code en_us}... nen ta chi lay phan truoc dau {@code _}.</p>
     */
    private static final Map<String, String> LOCALE_MAP = Map.ofEntries(
            Map.entry("vi", "vi"),
            Map.entry("en", "en"),
            Map.entry("zh", "zh"),
            Map.entry("es", "es"),
            Map.entry("fr", "fr"),
            Map.entry("de", "de"),
            Map.entry("ja", "ja"),
            Map.entry("ko", "ko"),
            Map.entry("ru", "ru"),
            Map.entry("pt", "pt")
    );

    public LanguageManager(Blogin plugin) {
        this.plugin = plugin;
    }

    // ------------------------------------------------------------------
    //  Tai / luu tep ngon ngu
    // ------------------------------------------------------------------

    /**
     * Giai nen cac tep ngon ngu mac dinh va tai tat ca tep trong thu muc lang.
     */
    public void load() {
        this.defaultLanguage = normalizeCode(
                plugin.getConfig().getString("ngon-ngu", "vi"));

        File dir = new File(plugin.getDataFolder(), "lang");
        if (!dir.exists() && !dir.mkdirs()) {
            plugin.getLog().warning("⚠ Không tạo được thư mục ngôn ngữ: " + dir.getPath());
        }

        // Giai nen tep ngon ngu mac dinh (khong ghi de ban user da sua).
        for (String code : LOCALE_MAP.keySet()) {
            extractBundled("lang/" + code + ".yml", new File(dir, code + ".yml"));
        }

        languages.clear();
        displayNames.clear();

        File[] files = dir.listFiles((d, name) -> name.toLowerCase(Locale.ROOT).endsWith(".yml"));
        if (files == null || files.length == 0) {
            plugin.getLog().warning("⚠ Không tìm thấy tệp ngôn ngữ nào trong " + dir.getPath());
            return;
        }
        for (File file : files) {
            String code = file.getName().substring(0, file.getName().length() - 4).toLowerCase(Locale.ROOT);
            try {
                YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
                languages.put(code, yaml);
                String name = yaml.getString("language.display-name", code.toUpperCase(Locale.ROOT));
                displayNames.put(code, name);
            } catch (Exception e) {
                plugin.getLog().warning("⚠ Không đọc được tệp ngôn ngữ " + file.getName()
                        + ": " + e.getMessage());
            }
        }

        // Dam bao ngon ngu mac dinh luon ton tai.
        if (!languages.containsKey(defaultLanguage)) {
            String fallback = languages.containsKey(FALLBACK_LANGUAGE) ? FALLBACK_LANGUAGE : null;
            if (fallback == null && !languages.isEmpty()) {
                fallback = languages.keySet().iterator().next();
            }
            if (fallback != null) {
                plugin.getLog().warning("⚠ Ngôn ngữ mặc định '" + defaultLanguage
                        + "' không tồn tại, dùng '" + fallback + "' thay thế.");
                this.defaultLanguage = fallback;
            }
        }

        plugin.getLog().info("✔ Đã tải " + languages.size() + " ngôn ngữ: "
                + String.join(", ", languages.keySet()));
    }

    /**
     * Copy tep ngon ngu mac dinh tu trong JAR ra thu muc (neu chua ton tai).
     */
    private void extractBundled(String resourcePath, File target) {
        if (target.exists()) return;
        try (InputStream in = plugin.getResource(resourcePath)) {
            if (in == null) return;
            Path parent = target.toPath().getParent();
            if (parent != null) Files.createDirectories(parent);
            try (OutputStream out = Files.newOutputStream(target.toPath())) {
                in.transferTo(out);
            }
        } catch (IOException e) {
            plugin.getLog().warning("⚠ Không giai nén được " + resourcePath + ": " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    //  Truy van noi dung
    // ------------------------------------------------------------------

    /**
     * Lay noi dung tho (chua xu ly mau & placeholder) theo ngon ngu mac dinh.
     */
    public String getRaw(String path) {
        return getRaw(defaultLanguage, path);
    }

    /**
     * Lay noi dung tho theo mot ma ngon ngu cu the.
     *
     * <p>Thu tu uu tien: ngon ngu yeu cau -> ngon ngu mac dinh -> gia tri trong
     * {@code config.yml} (de tuong thich nguoi dung config cu) -> {@code null}.</p>
     */
    public String getRaw(String languageCode, String path) {
        if (path == null) return null;

        YamlConfiguration lang = languages.get(normalizeCode(languageCode));
        if (lang != null) {
            String value = lang.getString(path);
            if (value != null) return value;
        }

        YamlConfiguration fallback = languages.get(defaultLanguage);
        if (fallback != null && !fallback.equals(lang)) {
            String value = fallback.getString(path);
            if (value != null) return value;
        }

        // Lop tuong thich nguoi dung config cu.
        return plugin.getConfig().getString(path);
    }

    /**
     * Lay danh sach noi dung (theo ngon ngu mac dinh).
     */
    public List<String> getRawList(String path) {
        return getRawList(defaultLanguage, path);
    }

    public List<String> getRawList(String languageCode, String path) {
        if (path == null) return Collections.emptyList();

        YamlConfiguration lang = languages.get(normalizeCode(languageCode));
        if (lang != null) {
            List<String> value = lang.getStringList(path);
            if (!value.isEmpty()) return value;
        }
        YamlConfiguration fallback = languages.get(defaultLanguage);
        if (fallback != null && !fallback.equals(lang)) {
            List<String> value = fallback.getStringList(path);
            if (!value.isEmpty()) return value;
        }
        return plugin.getConfig().getStringList(path);
    }

    // ------------------------------------------------------------------
    //  Ngon ngu theo nguoi choi
    // ------------------------------------------------------------------

    /**
     * Xac dinh ngon ngu hien tai cua mot nguoi choi.
     */
    public String getLanguage(Player player) {
        if (player == null) return defaultLanguage;
        String chosen = playerLanguages.get(player.getUniqueId());
        if (chosen != null && languages.containsKey(chosen)) return chosen;

        String fromLocale = fromClientLocale(player);
        if (fromLocale != null) return fromLocale;
        return defaultLanguage;
    }

    /**
     * Doan ngon ngu tu client cua nguoi choi (vi du {@code vi_vn} -> {@code vi}).
     */
    private String fromClientLocale(Player player) {
        String locale;
        try {
            locale = player.getLocale();
        } catch (Throwable ignored) {
            return null;
        }
        if (locale == null || locale.isBlank()) return null;
        String prefix = locale.toLowerCase(Locale.ROOT).split("_")[0];
        String mapped = LOCALE_MAP.get(prefix);
        if (mapped != null && languages.containsKey(mapped)) return mapped;
        return null;
    }

    /**
     * Luu lua chon ngon ngu cua nguoi choi (goi tu {@code /blogin language}).
     */
    public void setPlayerLanguage(UUID uuid, String code) {
        if (uuid == null) return;
        String normalized = normalizeCode(code);
        if (normalized == null || !languages.containsKey(normalized)) return;
        playerLanguages.put(uuid, normalized);
    }

    public String getPlayerLanguage(UUID uuid) {
        return uuid == null ? defaultLanguage : playerLanguages.getOrDefault(uuid, defaultLanguage);
    }

    public void clearPlayerLanguage(UUID uuid) {
        if (uuid != null) playerLanguages.remove(uuid);
    }

    // ------------------------------------------------------------------
    //  Tien ich
    // ------------------------------------------------------------------

    /** Chuan hoa ma ngon ngu (viet thuong, bo khoang trang). */
    public static String normalizeCode(String code) {
        if (code == null) return null;
        String trimmed = code.trim().toLowerCase(Locale.ROOT);
        return trimmed.isEmpty() ? null : trimmed;
    }

    /** Ngon ngu mac dinh cua server. */
    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    /** Danh sach ma ngon ngu dang co tep. */
    public List<String> getAvailableLanguages() {
        return new ArrayList<>(languages.keySet());
    }

    /** Danh sach {@code ma -> ten hien thi}. */
    public Map<String, String> getDisplayNames() {
        return displayNames;
    }

    public String getDisplayName(String code) {
        return displayNames.getOrDefault(normalizeCode(code), code);
    }

    public boolean hasLanguage(String code) {
        return languages.containsKey(normalizeCode(code));
    }

    /** Chi lay phan dau cua ma ngon ngu (vd {@code vi_vn} -> {@code vi}). */
    public String extractLanguageCode(String value) {
        if (value == null) return null;
        String trimmed = value.trim().toLowerCase(Locale.ROOT);
        if (trimmed.isEmpty()) return null;
        int idx = trimmed.indexOf('_');
        String prefix = idx > 0 ? trimmed.substring(0, idx) : trimmed;
        String mapped = LOCALE_MAP.get(prefix);
        return mapped != null ? mapped : prefix;
    }
}
