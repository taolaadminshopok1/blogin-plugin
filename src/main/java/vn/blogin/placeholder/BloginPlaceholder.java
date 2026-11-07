package vn.blogin.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import vn.blogin.Blogin;
import vn.blogin.database.PlayerDAO.PlayerData;

/**
 * PlaceholderAPI expansion cho Blogin.
 *
 * <p><b>Luu y quan trong:</b> PlaceholderAPI co the goi {@link #onRequest} tu
 * <b>main thread</b>. Do do, phan lon cac placeholder yeu cau du lieu tu database
 * <b>khong duoc query truc tiep</b> o day - neu khong se block TPS. Thay vao do
 * du lieu duoc lay tu bo dem trong RAM cua {@code SessionManager} hoac tu
 * PlaceholderAPI voi co che cache cua no.</p>
 */
public class BloginPlaceholder extends PlaceholderExpansion {

    private final Blogin plugin;

    public BloginPlaceholder(Blogin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "blogin";
    }

    @Override
    public String getAuthor() {
        return "BloginVN";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        String key = params.toLowerCase();

        // Cac placeholder tong quan - doc tu bo dem RAM, khong cham database.
        switch (key) {
            case "total_players" -> {
                return String.valueOf(plugin.getAuthManager().getPlayerDAO().countPlayers());
            }
            case "totp_enabled_count" -> {
                return String.valueOf(plugin.getAuthManager().getPlayerDAO().countTotpEnabled());
            }
            case "version" -> {
                return plugin.getDescription().getVersion();
            }
            default -> { }
        }

        if (player == null) return "";

        if (player.isOnline() && player.getPlayer() != null) {
            var sm = plugin.getSessionManager();
            switch (key) {
                case "authenticated" -> {
                    return sm.isAuthenticated(player.getUniqueId()) ? "yes" : "no";
                }
                case "lock_remaining" -> {
                    String ip = getIp(player.getPlayer());
                    long sec = plugin.getAuthManager().getLockRemainingSeconds(player.getName(), ip);
                    return String.valueOf(sec);
                }
                case "remaining_attempts" -> {
                    String ip = getIp(player.getPlayer());
                    return String.valueOf(
                            plugin.getAuthManager().getRemainingAttempts(player.getName(), ip));
                }
                case "timeout" -> {
                    return String.valueOf(sm.getRemainingTime(player.getUniqueId()));
                }
                case "ip_current" -> {
                    return getIp(player.getPlayer());
                }
                default -> { }
            }
        }

        // Cac placeholder du lieu tai khoan can query database.
        // PlaceholderAPI goi ham nay tu main thread, nen dung ban ghi cache
        // (TTL 30s) de tranh block TPS.
        PlayerData data = plugin.getAuthManager().getPlayerDAO().getPlayerData(player.getName(), true);
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
            case "registered_at" -> {
                return data != null ? String.valueOf(data.registrationDate()) : "0";
            }
            default -> {
                return null;
            }
        }
    }

    private String getIp(org.bukkit.entity.Player player) {
        return player.getAddress() != null
                ? player.getAddress().getAddress().getHostAddress() : "0.0.0.0";
    }
}
