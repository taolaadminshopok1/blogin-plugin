package vn.blogin.managers;

import vn.blogin.Blogin;
import vn.blogin.utils.ChatUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Lay noi dung thong diep da duoc phien dich & dinh dang mau.
 *
 * <p>Neu nguoi choi da chon ngon ngu ({@code /blogin language <ma>}) hoac client
 * ho tro ngon ngu tuong ung, noi dung se duoc lay tu tep trong {@code lang/}.
 * Neu khong tim thay, ham se quay ve {@code config.yml} (de tuong thich nguoi
 * dung config cu) roi cuoi cung la dinh nghia mac dinh trong ma nguon.</p>
 */
public class MessageManager {
    private final Blogin plugin;
    private final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([^}]+)\\}");

    public MessageManager(Blogin plugin) {
        this.plugin = plugin;
    }

    // ------------------------------------------------------------------
    //  Lay noi dung theo nguoi choi
    // ------------------------------------------------------------------

    /**
     * Lay noi dung theo ngon ngu cua nguoi choi.
     */
    public String get(CommandSender target, String path, String defaultMsg, String... placeholders) {
        return resolve(target, path, defaultMsg, placeholders);
    }

    /**
     * Lay noi dung theo ngon ngu cua nguoi choi (khong co placeholder).
     */
    public String get(CommandSender target, String path, String defaultMsg) {
        return resolve(target, path, defaultMsg, new String[0]);
    }

    /**
     * Lay noi dung theo ngon ngu mac dinh cua server.
     */
    public String get(String path, String defaultMsg, String... placeholders) {
        return resolve(null, path, defaultMsg, placeholders);
    }

    /**
     * Lay noi dung theo ngon ngu mac dinh cua server (khong co placeholder).
     */
    public String get(String path, String defaultMsg) {
        return resolve(null, path, defaultMsg, new String[0]);
    }

    /**
     * Lay danh sach noi dung theo ngon ngu cua nguoi choi.
     */
    public List<String> getList(CommandSender target, String path) {
        String code = languageOf(target);
        List<String> raw = plugin.getLanguageManager().getRawList(code, path);
        return applyColors(raw);
    }

    /**
     * Lay danh sach noi dung theo ngon ngu mac dinh cua server.
     */
    public List<String> getList(String path) {
        return getList(null, path);
    }

    /**
     * Lay noi dung co placeholder theo ngon ngu cua nguoi choi.
     */
    public String getWithPlaceholders(CommandSender target, String path, String defaultMsg,
                                      String... placeholders) {
        return resolve(target, path, defaultMsg, placeholders);
    }

    /**
     * Lay noi dung co placeholder theo ngon ngu mac dinh cua server.
     */
    public String getWithPlaceholders(String path, String defaultMsg, String... placeholders) {
        return resolve(null, path, defaultMsg, placeholders);
    }

    /**
     * Lay noi dung da tach thanh nhieu dong (theo ngon ngu mac dinh).
     */
    public String[] getMultiline(String path, String defaultMsg) {
        String msg = resolve(null, path, defaultMsg, new String[0]);
        return msg.split("\\n");
    }

    // ------------------------------------------------------------------
    //  Noi dung khong phu thuoc nguon ngu (chi doc config.yml)
    // ------------------------------------------------------------------

    public int getInt(String path, int defaultValue) {
        return plugin.getConfig().getInt(path, defaultValue);
    }

    public boolean getBoolean(String path, boolean defaultValue) {
        return plugin.getConfig().getBoolean(path, defaultValue);
    }

    /**
     * Lay cau hinh title (line1/line2) cho hieu ung welcome.
     */
    public WelcomeTitleConfig getWelcomeTitle(String path) {
        return getWelcomeTitle(null, path);
    }

    /**
     * Lay cau hinh title theo ngon ngu cua nguoi nhan.
     */
    public WelcomeTitleConfig getWelcomeTitle(CommandSender target, String path) {
        String code = languageOf(target);
        String line1 = plugin.getLanguageManager().getRaw(code, path + ".line1");
        String line2 = plugin.getLanguageManager().getRaw(code, path + ".line2");
        if (line1 == null) line1 = plugin.getConfig().getString(path + ".line1", "");
        if (line2 == null) line2 = plugin.getConfig().getString(path + ".line2", "");
        int fadeIn = plugin.getConfig().getInt(path + ".fade-in", 10);
        int stay = plugin.getConfig().getInt(path + ".stay", 60);
        int fadeOut = plugin.getConfig().getInt(path + ".fade-out", 20);
        return new WelcomeTitleConfig(applyColors(line1), applyColors(line2), fadeIn, stay, fadeOut);
    }

    // ------------------------------------------------------------------
    //  Tien ich
    // ------------------------------------------------------------------

    /**
     * Giai quyet noi dung cua mot nguoi choi (hoac ngon ngu mac dinh).
     */
    private String resolve(CommandSender target, String path, String defaultMsg, String... placeholders) {
        String code = languageOf(target);
        String msg = plugin.getLanguageManager().getRaw(code, path);
        if (msg == null) msg = defaultMsg;
        if (msg == null) return "";
        return applyPlaceholders(applyColors(msg), placeholders);
    }

    /**
     * Xac dinh ma ngon ngu phu hop voi nguoi nhan tin nhan.
     */
    private String languageOf(CommandSender target) {
        if (target instanceof Player player) {
            return plugin.getLanguageManager().getLanguage(player);
        }
        return plugin.getLanguageManager().getDefaultLanguage();
    }

    public String applyPlaceholders(String text, String... placeholders) {
        if (text == null || placeholders == null || placeholders.length == 0) return text;
        for (int i = 0; i < placeholders.length - 1; i += 2) {
            String key = placeholders[i];
            String val = placeholders[i + 1];
            if (text.contains("{" + key + "}")) {
                text = text.replace("{" + key + "}", val == null ? "" : val);
            }
        }
        return text;
    }

    public String applyColors(String text) {
        if (text == null) return "";
        return ChatUtil.translate(text);
    }

    public List<String> applyColors(List<String> list) {
        if (list == null) return Collections.emptyList();
        return list.stream().map(this::applyColors).collect(java.util.stream.Collectors.toList());
    }

    public String resolveNested(Map<String, Object> map, String path) {
        String[] parts = path.split("\\.");
        Object current = map;
        for (String part : parts) {
            if (current instanceof Map) {
                current = ((Map<?, ?>) current).get(part);
            } else {
                return null;
            }
        }
        return current == null ? null : current.toString();
    }

    public void reload() {
        plugin.getLog().info("✔ MessageManager đã được reload");
    }

    /** Ngon ngu mac dinh cua server. */
    public String getLanguage() {
        return plugin.getLanguageManager().getDefaultLanguage();
    }

    public boolean isVietnamese() {
        return "vi".equals(getLanguage());
    }

    public record WelcomeTitleConfig(String line1, String line2, int fadeIn, int stay, int fadeOut) { }
}
