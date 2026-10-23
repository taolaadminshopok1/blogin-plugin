package vn.blogin.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import vn.blogin.Blogin;
import vn.blogin.database.PlayerDAO.PlayerData;

/**
 * PlaceholderAPI Expansion cho Blogin.
 *
 * <p>Các placeholder có sẵn:
 * <ul>
 *   <li>{@code %blogin_authenticated%} - "yes" / "no"</li>
 *   <li>{@code %blogin_registered%} - "yes" / "no"</li>
 *   <li>{@code %blogin_2fa%} - "yes" / "no"</li>
 *   <li>{@code %blogin_ip%} - IP đã đăng ký</li>
 *   <li>{@code %blogin_logins%} - Tổng số lần đăng nhập</li>
 *   <li>{@code %blogin_last_login%} - Lần đăng nhập cuối (timestamp)</li>
 *   <li>{@code %blogin_lock_remaining%} - Thời gian khóa còn lại (giây)</li>
 * </ul>
 *
 * @author BloginVN
 */
public class BloginPlaceholder extends PlaceholderExpansion {

    private final Blogin plugin;

    public BloginPlaceholder(Blogin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "blogin";
    }

    @Override
    public @NotNull String getAuthor() {
        return "BloginVN";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return "";
        String key = params.toLowerCase();

        // Các placeholder cần người chơi online
        if (player.isOnline() && player.getPlayer() != null) {
            var sm = plugin.getSessionManager();
            switch (key) {
                case "authenticated" -> {
                    return sm.isAuthenticated(player.getUniqueId()) ? "yes" : "no";
                }
                case "lock_remaining" -> {
                    String ip = player.getPlayer().getAddress() != null
                            ? player.getPlayer().getAddress().getAddress().getHostAddress() : "0.0.0.0";
                    long sec = plugin.getAuthManager().getLockRemainingSeconds(player.getName(), ip);
                    return String.valueOf(sec);
                }
            }
        }

        // Các placeholder chỉ cần truy vấn DB
        PlayerData data = plugin.getAuthManager().getPlayerDAO().getPlayerData(player.getName());
        switch (key) {
            case "registered" -> {
                return data != null ? "yes" : "no";
            }
            case "2fa" -> {
                return data != null && data.totpEnabled() ? "yes" : "no";
            }
            case "ip" -> {
                return data != null && data.lastIp() != null ? data.lastIp() : "";
            }
            case "logins" -> {
                return data != null ? String.valueOf(data.loginCount()) : "0";
            }
            case "last_login" -> {
                return data != null ? String.valueOf(data.lastLogin()) : "0";
            }
        }
        return null;
    }
}
