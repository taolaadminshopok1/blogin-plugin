package vn.blogin.utils;

import vn.blogin.Blogin;

/**
 * Cham do manh mat khau (0-5) + hien thi thanh mau.
 *
 * <p>Ten muc do duoc lay tu tep ngon ngu ({@code misc.do-ma-ngau}) de ho tro
 * da ngon ngu.</p>
 */
public final class PasswordStrength {
    private static final String[] COMMON = {
            "123456", "password", "12345678", "qwerty", "123456789",
            "111111", "1234567", "abc123", "123123", "admin",
            "letmein", "welcome", "monkey", "dragon", "iloveyou",
            "sunshine", "princess", "football", "charlie", "shadow",
            "login", "master", "hello", "freedom", "whatever",
            "qazwsx", "minecraft", "server", "gamertag"
    };

    private PasswordStrength() { }

    public static int score(String password) {
        if (password == null || password.isEmpty()) return 0;
        int score = 0;
        int length = password.length();
        if (length >= 6) score++;
        if (length >= 10) score++;
        if (length >= 14) score++;
        boolean hasUpper = false, hasLower = false, hasDigit = false, hasSpecial = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else hasSpecial = true;
        }
        int classes = (hasUpper ? 1 : 0) + (hasLower ? 1 : 0) + (hasDigit ? 1 : 0) + (hasSpecial ? 1 : 0);
        if (classes >= 2) score++;
        if (classes >= 3) score++;
        if (classes == 4) score++;
        if (length < 8) score--;
        if (isCommon(password)) score -= 2;
        if (hasRepeatingRun(password)) score--;
        return Math.max(0, Math.min(5, score));
    }

    /**
     * Ten muc do manh theo ngon ngu cua nguoi choi.
     */
    public static String describe(Blogin plugin, String password) {
        int score = score(password);
        return plugin.getMessageManager().get(
                "misc.do-ma-ngau." + score,
                switch (score) {
                    case 0 -> "&c&lRẤT YẾU";
                    case 1 -> "&c&lYẾU";
                    case 2 -> "&e&lTRUNG BÌNH";
                    case 3 -> "&a&lKHÁ";
                    case 4 -> "&b&lMẠNH";
                    default -> "&d&lRẤT MẠNH";
                });
    }

    /**
     * Thanh mau do manh theo ngon ngu cua nguoi choi.
     */
    public static String bar(Blogin plugin, String password) {
        int score = score(password);
        StringBuilder sb = new StringBuilder("&8[");
        String[] colors = {"&c", "&c", "&e", "&a", "&b", "&d"};
        for (int i = 0; i < 5; i++) {
            sb.append(i < score ? colors[score] : "&8");
            sb.append("█");
        }
        sb.append("&8] ");
        sb.append(describe(plugin, password));
        return sb.toString();
    }

    public static boolean isCommon(String password) {
        if (password == null) return false;
        String lower = password.toLowerCase();
        for (String c : COMMON) {
            if (lower.contains(c)) return true;
        }
        return false;
    }

    private static boolean hasRepeatingRun(String password) {
        if (password.length() < 4) return false;
        for (int i = 0; i + 3 < password.length(); i++) {
            if (password.charAt(i) == password.charAt(i + 1)
                    && password.charAt(i) == password.charAt(i + 2)
                    && password.charAt(i) == password.charAt(i + 3)) {
                return true;
            }
        }
        return false;
    }
}
