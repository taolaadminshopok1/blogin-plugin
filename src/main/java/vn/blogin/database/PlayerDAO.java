package vn.blogin.database;
import vn.blogin.Blogin;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
public class PlayerDAO {
    /** Thoi gian song toi da cua ban ghi cache (ms). */
    private static final long CACHE_TTL_MS = 30_000L;

    private final Blogin plugin;

    /** Cache thong tin nguoi choi trong RAM (tranh query DB tren main thread). */
    private final Map<String, CachedPlayerData> playerDataCache = new ConcurrentHashMap<>();

    /** Cache cac so lieu thong ke (tranh query DB tren main thread). */
    private final Map<String, Integer> countCache = new ConcurrentHashMap<>();
    private final Map<String, Long> countCacheExpiry = new ConcurrentHashMap<>();

    public PlayerDAO(Blogin plugin) {
        this.plugin = plugin;
    }
    public boolean createPlayer(UUID uuid, String name, String passwordHash, String ip) {
        String sql = "INSERT INTO blogin_players (uuid, name, password_hash, last_ip, registration_date, last_login) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, name.toLowerCase());
            ps.setString(3, passwordHash);
            ps.setString(4, ip);
            ps.setLong(5, System.currentTimeMillis());
            ps.setLong(6, System.currentTimeMillis());
            boolean created = ps.executeUpdate() > 0;
            if (created) {
                invalidateCountCache();
                invalidateCache(name);
            }
            return created;
        } catch (SQLException e) {
            plugin.getLog().warning("✖ Lỗi tạo người chơi " + name + ": " + e.getMessage());
            return false;
        }
    }
    public String getPasswordHash(String name) {
        String sql = "SELECT password_hash FROM blogin_players WHERE name = ?";
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name.toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("password_hash");
            }
        } catch (SQLException e) {
            plugin.getLog().warning("✖ Lỗi truy vấn password hash: " + e.getMessage());
        }
        return null;
    }
    public boolean updatePassword(String name, String newHash) {
        String sql = "UPDATE blogin_players SET password_hash = ? WHERE name = ?";
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newHash);
            ps.setString(2, name.toLowerCase());
            boolean updated = ps.executeUpdate() > 0;
            if (updated) invalidateCache(name);
            return updated;
        } catch (SQLException e) {
            plugin.getLog().warning("✖ Lỗi cập nhật mật khẩu: " + e.getMessage());
            return false;
        }
    }
    public boolean updateTotpSecret(UUID uuid, String secret) {
        String sql = "UPDATE blogin_players SET totp_secret = ? WHERE uuid = ?";
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (secret == null) {
                ps.setNull(1, java.sql.Types.VARCHAR);
            } else {
                ps.setString(1, secret);
            }
            ps.setString(2, uuid.toString());
            boolean updated = ps.executeUpdate() > 0;
            if (updated) invalidateCacheByUuid(uuid);
            return updated;
        } catch (SQLException e) {
            plugin.getLog().warning("✖ Lỗi cập nhật TOTP secret: " + e.getMessage());
            return false;
        }
    }
    public boolean setTotpEnabled(UUID uuid, boolean enabled) {
        String sql = "UPDATE blogin_players SET totp_enabled = ? WHERE uuid = ?";
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, enabled);
            ps.setString(2, uuid.toString());
            boolean updated = ps.executeUpdate() > 0;
            if (updated) {
                invalidateCacheByUuid(uuid);
                invalidateCountCache();
            }
            return updated;
        } catch (SQLException e) {
            plugin.getLog().warning("✖ Lỗi cập nhật trạng thái 2FA: " + e.getMessage());
            return false;
        }
    }
    public String getTotpSecret(UUID uuid) {
        String sql = "SELECT totp_secret FROM blogin_players WHERE uuid = ?";
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("totp_secret");
            }
        } catch (SQLException e) {
            plugin.getLog().warning("✖ Lỗi truy vấn TOTP: " + e.getMessage());
        }
        return null;
    }
    public boolean updateLoginInfo(UUID uuid, String ip) {
        String sql = "UPDATE blogin_players SET last_login = ?, last_ip = ?, "
                   + "login_count = login_count + 1 WHERE uuid = ?";
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, System.currentTimeMillis());
            ps.setString(2, ip);
            ps.setString(3, uuid.toString());
            boolean updated = ps.executeUpdate() > 0;
            if (updated) invalidateCacheByUuid(uuid);
            return updated;
        } catch (SQLException e) {
            plugin.getLog().warning("✖ Lỗi cập nhật thông tin đăng nhập: " + e.getMessage());
            return false;
        }
    }
    public boolean isRegistered(String name) {
        String sql = "SELECT 1 FROM blogin_players WHERE name = ? LIMIT 1";
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name.toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            plugin.getLog().warning("✖ Lỗi kiểm tra đăng ký: " + e.getMessage());
        }
        return false;
    }
    public PlayerData getPlayerData(String name) {
        return getPlayerData(name, false);
    }

    /**
     * Lay thong tin nguoi choi, co the dung ban ghi trong cache.
     *
     * @param name ten nguoi choi
     * @param useCache {@code true} de dung ban ghi cache neu con hieu luc
     * @return du lieu nguoi choi hoac {@code null} neu khong ton tai
     */
    public PlayerData getPlayerData(String name, boolean useCache) {
        if (name == null) return null;
        String key = name.toLowerCase();
        if (useCache) {
            PlayerData cached = cachedPlayerData(key);
            if (cached != null) return cached;
        }
        String sql = "SELECT * FROM blogin_players WHERE name = ?";
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    PlayerData data = readPlayerData(rs);
                    cachePlayerData(key, data);
                    return data;
                }
            }
        } catch (SQLException e) {
            plugin.getLog().warning("✖ Lỗi lấy thông tin người chơi: " + e.getMessage());
        }
        return null;
    }

    /**
     * Bo dem thong tin nguoi choi trong RAM (dung cho placeholder & lenh admin
     * de khong phai query database tren main thread).
     */
    private void cachePlayerData(String key, PlayerData data) {
        if (data == null) {
            playerDataCache.remove(key);
            return;
        }
        playerDataCache.put(key, new CachedPlayerData(data, System.currentTimeMillis()));
    }

    private PlayerData cachedPlayerData(String key) {
        CachedPlayerData cached = playerDataCache.get(key);
        if (cached == null) return null;
        if (System.currentTimeMillis() - cached.cachedAt() > CACHE_TTL_MS) {
            playerDataCache.remove(key, cached);
            return null;
        }
        return cached.data();
    }

    /**
     * Xoa cache (dung khi du lieu vua thay doi, vi du doi mat khau / 2FA).
     */
    public void invalidateCache(String name) {
        if (name != null) playerDataCache.remove(name.toLowerCase());
    }

    /**
     * Xoa cache theo UUID (dung khi update qua UUID).
     */
    public void invalidateCacheByUuid(UUID uuid) {
        if (uuid == null) return;
        playerDataCache.entrySet().removeIf(entry ->
                entry.getValue().data().uuid().equals(uuid));
    }

    /**
     * Xoa cache so lieu thong ke (khi co tai khoan dang ky / bi xoa).
     */
    public void invalidateCountCache() {
        countCache.clear();
        countCacheExpiry.clear();
    }

    private record CachedPlayerData(PlayerData data, long cachedAt) { }
    public PlayerData getPlayerDataByUuid(UUID uuid) {
        String sql = "SELECT * FROM blogin_players WHERE uuid = ?";
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return readPlayerData(rs);
                }
            }
        } catch (SQLException e) {
            plugin.getLog().warning("✖ Lỗi lấy thông tin người chơi: " + e.getMessage());
        }
        return null;
    }
    private PlayerData readPlayerData(ResultSet rs) throws SQLException {
        return new PlayerData(
                UUID.fromString(rs.getString("uuid")),
                rs.getString("name"),
                rs.getString("password_hash"),
                rs.getString("totp_secret"),
                rs.getBoolean("totp_enabled"),
                rs.getString("last_ip"),
                rs.getLong("registration_date"),
                rs.getLong("last_login"),
                rs.getLong("login_count")
        );
    }
    public boolean deletePlayer(String name) {
        String sql = "DELETE FROM blogin_players WHERE name = ?";
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name.toLowerCase());
            boolean deleted = ps.executeUpdate() > 0;
            if (deleted) {
                invalidateCountCache();
                invalidateCache(name);
            }
            return deleted;
        } catch (SQLException e) {
            plugin.getLog().warning("✖ Lỗi xóa người chơi: " + e.getMessage());
        }
        return false;
    }
    public UUID getUUID(String name) {
        String sql = "SELECT uuid FROM blogin_players WHERE name = ?";
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name.toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return UUID.fromString(rs.getString("uuid"));
            }
        } catch (SQLException e) {
            plugin.getLog().warning("✖ Lỗi lấy UUID: " + e.getMessage());
        }
        return null;
    }
    public List<PlayerData> listPlayers(int limit, int offset) {
        String sql = "SELECT * FROM blogin_players ORDER BY last_login DESC LIMIT ? OFFSET ?";
        List<PlayerData> list = new ArrayList<>();
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ps.setInt(2, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(readPlayerData(rs));
                }
            }
        } catch (SQLException e) {
            plugin.getLog().warning("✖ Lỗi lấy danh sách người chơi: " + e.getMessage());
        }
        return list;
    }
    /**
     * Tong so tai khoan da dang ky.
     *
     * <p>Dung ban ghi cache trong RAM (TTL 30s) de khong query database tren
     * main thread - placeholder va lenh admin goi ham nay rat thuong xuyen.</p>
     */
    public int countPlayers() {
        Integer cached = countCache.get("total");
        if (cached != null && !isCountCacheExpired("total")) return cached;
        int count = countScalar("SELECT COUNT(*) FROM blogin_players", null);
        putCountCache("total", count);
        return count;
    }

    /**
     * So tai khoan dang bat 2FA (co cache, xem {@link #countPlayers()}).
     */
    public int countTotpEnabled() {
        Integer cached = countCache.get("totp");
        if (cached != null && !isCountCacheExpired("totp")) return cached;
        int count = countScalar("SELECT COUNT(*) FROM blogin_players WHERE totp_enabled = 1", null);
        putCountCache("totp", count);
        return count;
    }

    public int countRecentLogins(int hours) {
        long since = System.currentTimeMillis() - (hours * 3600_000L);
        return countScalar("SELECT COUNT(*) FROM blogin_players WHERE last_login >= ?", since);
    }
    public long totalLoginCount() {
        return countLongScalar("SELECT COALESCE(SUM(login_count), 0) FROM blogin_players");
    }

    private boolean isCountCacheExpired(String key) {
        Long expiry = countCacheExpiry.get(key);
        return expiry == null || System.currentTimeMillis() > expiry;
    }

    private void putCountCache(String key, int value) {
        countCache.put(key, value);
        countCacheExpiry.put(key, System.currentTimeMillis() + CACHE_TTL_MS);
    }
    private int countScalar(String sql, Long param) {
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (param != null) ps.setLong(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            plugin.getLog().warning("✖ Lỗi truy vấn thống kê: " + e.getMessage());
        }
        return 0;
    }
    private long countLongScalar(String sql) {
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }
        } catch (SQLException e) {
            plugin.getLog().warning("✖ Lỗi truy vấn thống kê: " + e.getMessage());
        }
        return 0L;
    }
    public record PlayerData(
            UUID uuid,
            String name,
            String passwordHash,
            String totpSecret,
            boolean totpEnabled,
            String lastIp,
            long registrationDate,
            long lastLogin,
            long loginCount
    ) {}
}
