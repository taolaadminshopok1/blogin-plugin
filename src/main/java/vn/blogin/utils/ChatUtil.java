package vn.blogin.utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.CommandSender;
import java.util.ArrayList;
import java.util.List;
public final class ChatUtil {
    private static final LegacyComponentSerializer LEGACY =
            LegacyComponentSerializer.builder()
                    .character('&')
                    .hexColors()
                    .useUnusualXRepeatedCharacterHexFormat()
                    .build();
    private static final String[] GRADIENTS = {
            "#FF6B6B", "#FFA502", "#FFE66D", "#4ECDC4", "#45B7D1",
            "#96CEB4", "#FFEAA7", "#DDA0DD", "#A29BFE", "#6C5CE7"
    };
    private static final String COLOR_CHARS = "0123456789abcdefklmnorABCDEFKLMNOR";
    private ChatUtil() { }
    public static Component parse(String text) {
        if (text == null) return Component.empty();
        return LEGACY.deserialize(normalizeHex(text));
    }
    private static String normalizeHex(String text) {
        if (text == null) return null;
        if (text.indexOf('#') < 0 && text.indexOf('§') < 0) return text;
        StringBuilder sb = new StringBuilder(text.length() + 20);
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '§') c = '&';
            if (c == '&' && i + 7 < text.length() && text.charAt(i + 1) == '#') {
                String hex = text.substring(i + 2, i + 8);
                if (hex.matches("(?i)[0-9a-f]{6}")) {
                    sb.append(c).append('x');
                    for (char h : hex.toCharArray()) {
                        sb.append(c).append(h);
                    }
                    i += 7;
                    continue;
                }
            }
            sb.append(c);
        }
        return sb.toString();
    }
    public static String colorize(String text) {
        if (text == null) return "";
        return LEGACY.serialize(parse(text));
    }
    public static String translate(String text) {
        if (text == null) return "";
        String normalized = normalizeHex(text);
        StringBuilder hexOut = new StringBuilder();
        int i = 0;
        while (i < normalized.length()) {
            char c = normalized.charAt(i);
            if (c == '&' && i + 13 < normalized.length()
                    && (normalized.charAt(i + 1) == 'x' || normalized.charAt(i + 1) == 'X')) {
                boolean valid = true;
                for (int k = 0; k < 6; k++) {
                    int p = i + 2 + k * 2;
                    if (p >= normalized.length() || normalized.charAt(p) != '&'
                            || !isHexDigit(normalized.charAt(p + 1))) {
                        valid = false;
                        break;
                    }
                }
                if (valid) {
                    hexOut.append('§').append('x');
                    for (int k = 0; k < 6; k++) {
                        int p = i + 2 + k * 2;
                        hexOut.append('§').append(Character.toUpperCase(normalized.charAt(p + 1)));
                    }
                    i += 14;
                    continue;
                }
            }
            hexOut.append(c);
            i++;
        }
        String normalized2 = hexOut.toString();
        StringBuilder sb = new StringBuilder(normalized2.length());
        for (int k = 0; k < normalized2.length(); k++) {
            char c = normalized2.charAt(k);
            if (c == '&' && k + 1 < normalized2.length()
                    && COLOR_CHARS.indexOf(normalized2.charAt(k + 1)) >= 0) {
                sb.append('§').append(Character.toLowerCase(normalized2.charAt(k + 1)));
                k++;
                continue;
            }
            sb.append(c);
        }
        return sb.toString();
    }
    private static boolean isHexDigit(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }
    public static void send(CommandSender target, String text) {
        if (target == null || text == null) return;
        target.sendMessage(parse(text));
    }
    public static void send(CommandSender target, String text, String... placeholders) {
        if (target == null || text == null) return;
        String resolved = text;
        if (placeholders != null) {
            for (int i = 0; i < placeholders.length - 1; i += 2) {
                resolved = resolved.replace("{" + placeholders[i] + "}",
                        placeholders[i + 1] == null ? "" : placeholders[i + 1]);
            }
        }
        target.sendMessage(parse(resolved));
    }
    public static void sendList(CommandSender target, List<String> lines) {
        if (target == null || lines == null) return;
        for (String line : lines) {
            send(target, line);
        }
    }
    public static void sendArray(CommandSender target, String... lines) {
        if (target == null || lines == null) return;
        for (String line : lines) {
            send(target, line);
        }
    }
    public static Component gradient(String text, TextColor from, TextColor to) {
        if (text == null || text.isEmpty()) return Component.empty();
        ComponentBuilder builder = Component.text();
        int len = text.length();
        for (int i = 0; i < len; i++) {
            float ratio = len == 1 ? 0f : (float) i / (len - 1);
            builder.append(Component.text(text.charAt(i))
                    .color(TextColor.lerp(ratio, from, to)));
        }
        return builder.build();
    }
    public static Component randomGradient(String text) {
        TextColor a = TextColor.fromHexString(GRADIENTS[randomIndex(GRADIENTS.length)]);
        TextColor b = TextColor.fromHexString(GRADIENTS[randomIndex(GRADIENTS.length)]);
        return gradient(text, a, b);
    }
    public static Component frame(String title, int width) {
        width = Math.max(20, Math.min(width, 60));
        String line = "&8&m" + " ".repeat(Math.max(0, width - 2));
        Component header = Component.text()
                .append(Component.text("  "))
                .append(parse(title).decoration(TextDecoration.BOLD, true))
                .append(Component.text("  "))
                .build();
        return Component.text()
                .append(parse(line))
                .appendNewline()
                .append(header)
                .appendNewline()
                .append(parse(line))
                .build();
    }
    public static String stripColors(String text) {
        if (text == null) return "";
        return text
                .replaceAll("(?i)[&§]x(?:[&§][0-9a-f]){6}", "")
                .replaceAll("(?i)[&§]#[0-9a-f]{6}", "")
                .replaceAll("(?i)[&§][0-9a-fk-or]", "");
    }
    public static String plain(Component component) {
        if (component == null) return "";
        return PlainTextComponentSerializer.plainText().serialize(component);
    }
    public static List<String> bullets(String... items) {
        List<String> list = new ArrayList<>();
        if (items == null) return list;
        for (String item : items) {
            list.add("&8» &7" + item);
        }
        return list;
    }
    private static int randomIndex(int max) {
        return (int) (Math.random() * max);
    }
}
