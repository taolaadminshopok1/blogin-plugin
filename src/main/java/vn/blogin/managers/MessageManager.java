package vn.blogin.managers;

import vn.blogin.Blogin;
import net.md_5.bungee.api.ChatColor;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Quản lý và lấy các thông điệp đã được Việt hóa từ config.yml.
 *
 * <p>Hỗ trợ placeholder thay thế bằng cú pháp {@code {key}} và mã màu Minecraft ký tự {@code &}.</p>
 *
 * @author BloginVN
 */
public class MessageManager {

    private final Blogin plugin;
    private final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([^}]+)\\}");

    public MessageManager(Blogin plugin) {
        this.plugin = plugin;
    }

    /**
     * Lấy thông điệp theo đường dẫn trong config, có áp dụng các placeholder.
     *
     * @param path        đường dẫn trong config.yml (vd: "tin-nhan.da-dang-nhap")
     * @param defaultMsg  giá trị mặc định nếu không tìm thấy
     * @param placeholders các placeholder (key, value, key, value, ...)
     * @return thông điệp đã được định dạng
     */
    public String get(String path, String defaultMsg, String... placeholders) {
        String msg = plugin.getConfig().getString(path, defaultMsg);
        return applyPlaceholders(applyColors(msg), placeholders);
    }

    /**
     * Lấy thông điệp (không placeholder).
     */
    public String get(String path, String defaultMsg) {
        return get(path, defaultMsg, new String[0]);
    }

    /**
     * Lấy danh sách thông điệp (dạng List&lt;String&gt;).
     */
    public java.util.List<String> getList(String path) {
        return applyColors(plugin.getConfig().getStringList(path));
    }

    /**
     * Áp dụng các placeholder cho chuỗi.
     *
     * <p>Ví dụ: {@code "Xin chào {player}"} + ("player", "Blogin") = {@code "Xin chào Blogin"}</p>
     */
    public String applyPlaceholders(String text, String... placeholders) {
        if (text == null || placeholders == null || placeholders.length == 0) return text;
        // placeholders là mảng key1, val1, key2, val2, ...
        for (int i = 0; i < placeholders.length - 1; i += 2) {
            String key = placeholders[i];
            String val = placeholders[i + 1];
            text = text.replace("{" + key + "}", val == null ? "" : val);
        }
        return text;
    }

    /**
     * Chuyển mã màu {@code &} sang {@code §} (ký tự section) của Minecraft.
     */
    public String applyColors(String text) {
        if (text == null) return "";
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    /**
     * Áp dụng màu cho cả danh sách.
     */
    public java.util.List<String> applyColors(java.util.List<String> list) {
        if (list == null) return java.util.Collections.emptyList();
        return list.stream().map(this::applyColors).collect(java.util.stream.Collectors.toList());
    }

    /**
     * Lấy nội dung 1 placeholder theo đường dẫn (dùng cho placeholder API).
     */
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

    /**
     * Lấy thông tin title block từ config (vd: welcome.title-chao).
     * Bao gồm line1, line2, fade-in, stay, fade-out.
     */
    public WelcomeTitleConfig getWelcomeTitle(String path) {
        String line1 = plugin.getConfig().getString(path + ".line1", "");
        String line2 = plugin.getConfig().getString(path + ".line2", "");
        int fadeIn = plugin.getConfig().getInt(path + ".fade-in", 10);
        int stay = plugin.getConfig().getInt(path + ".stay", 60);
        int fadeOut = plugin.getConfig().getInt(path + ".fade-out", 20);
        return new WelcomeTitleConfig(applyColors(line1), applyColors(line2), fadeIn, stay, fadeOut);
    }

    /**
     * DTO chứa thông tin title block.
     */
    public record WelcomeTitleConfig(String line1, String line2, int fadeIn, int stay, int fadeOut) {}

    /**
     * Lấy chuỗi có hỗ trợ placeholder.
     */
    public String getWithPlaceholders(String path, String defaultMsg, String... placeholders) {
        String msg = plugin.getConfig().getString(path, defaultMsg);
        return applyPlaceholders(applyColors(msg), placeholders);
    }

    /**
     * Lấy chuỗi multi-line (dùng cho YAML literal block).
     */
    public String[] getMultiline(String path, String defaultMsg) {
        String msg = plugin.getConfig().getString(path, defaultMsg);
        return applyColors(msg).split("\\n");
    }

    /**
     * Tải lại thông điệp (sau khi reload config).
     */
    public void reload() {
        // Không cần cache, mỗi lần get() sẽ đọc trực tiếp từ config
        plugin.getLog().info("✔ MessageManager đã được reload");
    }
}
