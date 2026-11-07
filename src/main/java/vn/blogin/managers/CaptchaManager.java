package vn.blogin.managers;
import vn.blogin.Blogin;
import java.security.SecureRandom;
public class CaptchaManager {
    private final Blogin plugin;
    private final SecureRandom random = new SecureRandom();
    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final String CHARS_MIXED = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    public CaptchaManager(Blogin plugin) {
        this.plugin = plugin;
    }
    public String[] generate() {
        boolean enabled = plugin.getConfig().getBoolean("captcha.bat", true);
        if (!enabled) {
            return new String[]{"", ""};
        }
        int length = plugin.getConfig().getInt("captcha.do-dai", 5);
        boolean onlyAlnum = plugin.getConfig().getBoolean("captcha.chi-chu-va-so", true);
        boolean caseSensitive = plugin.getConfig().getBoolean("captcha.phan-biet-hoa-thuong", false);
        String pool = onlyAlnum ? CHARS : CHARS_MIXED;
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(pool.charAt(random.nextInt(pool.length())));
        }
        String display = sb.toString();
        String answer = caseSensitive ? display : display.toLowerCase();
        return new String[]{display, answer};
    }
    public boolean isEnabled() {
        return plugin.getConfig().getBoolean("captcha.bat", true);
    }
    public void reload() {
    }
}
