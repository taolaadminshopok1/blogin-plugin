package vn.blogin.managers;
import vn.blogin.Blogin;
import vn.blogin.utils.ChatUtil;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
public class MessageManager {
    private final Blogin plugin;
    private final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([^}]+)\\}");
    public MessageManager(Blogin plugin) {
        this.plugin = plugin;
    }
    public String get(String path, String defaultMsg, String... placeholders) {
        String msg = plugin.getConfig().getString(path, defaultMsg);
        return applyPlaceholders(applyColors(msg), placeholders);
    }
    public String get(String path, String defaultMsg) {
        return get(path, defaultMsg, new String[0]);
    }
    public List<String> getList(String path) {
        return applyColors(plugin.getConfig().getStringList(path));
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
    public WelcomeTitleConfig getWelcomeTitle(String path) {
        String line1 = plugin.getConfig().getString(path + ".line1", "");
        String line2 = plugin.getConfig().getString(path + ".line2", "");
        int fadeIn = plugin.getConfig().getInt(path + ".fade-in", 10);
        int stay = plugin.getConfig().getInt(path + ".stay", 60);
        int fadeOut = plugin.getConfig().getInt(path + ".fade-out", 20);
        return new WelcomeTitleConfig(applyColors(line1), applyColors(line2), fadeIn, stay, fadeOut);
    }
    public record WelcomeTitleConfig(String line1, String line2, int fadeIn, int stay, int fadeOut) { }
    public String getWithPlaceholders(String path, String defaultMsg, String... placeholders) {
        String msg = plugin.getConfig().getString(path, defaultMsg);
        return applyPlaceholders(applyColors(msg), placeholders);
    }
    public String[] getMultiline(String path, String defaultMsg) {
        String msg = plugin.getConfig().getString(path, defaultMsg);
        return applyColors(msg).split("\\n");
    }
    public int getInt(String path, int defaultValue) {
        return plugin.getConfig().getInt(path, defaultValue);
    }
    public boolean getBoolean(String path, boolean defaultValue) {
        return plugin.getConfig().getBoolean(path, defaultValue);
    }
    public void reload() {
        plugin.getLog().info("✔ MessageManager đã được reload");
    }
    public String getLanguage() {
        return plugin.getConfig().getString("ngon-ngu", "vi").toLowerCase();
    }
    public boolean isVietnamese() {
        return "vi".equals(getLanguage());
    }
}
