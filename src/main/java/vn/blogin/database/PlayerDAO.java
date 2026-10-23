package vn.blogin.database;

import vn.blogin.Blogin;

import java.sql.*;
import java.util.UUID;

/**
 * DAO (Data Access Object) - chứa tất cả các truy vấn liên quan đến bảng {@code blogin_players}.
 *
 * <p>Sử dụng pattern DAO để tách biệt logic nghiệp vụ với logic truy cập database.</p>
 *
 * @author BloginVN
 */
public class PlayerDAO {

    private final Blogin plugin;

    public PlayerDAO(Blogin plugin) {
        this.plugin = plugin;
    }

    /**
     * Tạo một record người chơi mới trong database.
     *
     * @param uuid          UUID của người chơi
     * @param name          tên người chơi (lowercase)
     * @param passwordHash  mật khẩu đã hash BCrypt
     * @param ip            IP đăng ký
     * @return true nếu tạo thành công
     */
    public boolean createPlayer(UUID uuid, String name, String passwordHash, String ip) {
        String sql = "INSERT INTO blogin_players (uuid, name, password_hash, last_ip, registration_date, last_login) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";

        // Sử dụng connection trực tiếp
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, uuid.toString());
            ps.setString(2, name.toLowerCase());
            ps.setString(3, passwordHash);
            ps.setString(4, ip);
            ps.setLong(5, System.currentTimeMillis());
            ps.setLong(6, System.currentTimeMillis());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            plugin.getLog().warning("✖ Lỗi tạo người chơi " + name + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Lấy thông tin hash mật khẩu của người chơi theo tên.
     *
     * @param name tên người chơi (lowercase)
     * @return hash mật khẩu, hoặc null nếu không tìm thấy
     */
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

    /**
     * Cập nhật mật khẩu mới cho người chơi.
     *
     * @param name     tên người chơi
     * @param newHash  hash mật khẩu mới
     * @return true nếu cập nhật thành công
     */
    public boolean updatePassword(String name, String newHash) {
        String sql = "UPDATE blogin_players SET password_hash = ? WHERE name = ?";
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newHash);
            ps.setString(2, name.toLowerCase());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            plugin.getLog().warning("✖ Lỗi cập nhật mật khẩu: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cập nhật secret key 2FA cho người chơi.
     */
    public boolean updateTotpSecret(UUID uuid, String secret) {
        String sql = "UPDATE blogin_players SET totp_secret = ? WHERE uuid = ?";
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, secret);
            ps.setString(2, uuid.toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            plugin.getLog().warning("✖ Lỗi cập nhật TOTP secret: " + e.getMessage());
            return false;
        }
    }

    /**
     * Bật/tắt 2FA cho người chơi.
     */
    public boolean setTotpEnabled(UUID uuid, boolean enabled) {
        String sql = "UPDATE blogin_players SET totp_enabled = ? WHERE uuid = ?";
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, enabled);
            ps.setString(2, uuid.toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            plugin.getLog().warning("✖ Lỗi cập nhật trạng thái 2FA: " + e.getMessage());
            return false;
        }
    }

    /**
     * Lấy secret key 2FA của người chơi.
     */
    public String getTotpSecret(UUID uuid) {
        String sql = "SELECT totp_secret, totp_enabled FROM blogin_players WHERE uuid = ?";
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    if (rs.getBoolean("totp_enabled")) {
                        return rs.getString("totp_secret");
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLog().warning("✖ Lỗi truy vấn TOTP: " + e.getMessage());
        }
        return null;
    }

    /**
     * Cập nhật thời gian đăng nhập cuối, IP và tăng login_count.
     */
    public boolean updateLoginInfo(UUID uuid, String ip) {
        String sql = "UPDATE blogin_players SET last_login = ?, last_ip = ?, login_count = login_count + 1 WHERE uuid = ?";
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, System.currentTimeMillis());
            ps.setString(2, ip);
            ps.setString(3, uuid.toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            plugin.getLog().warning("✖ Lỗi cập nhật thông tin đăng nhập: " + e.getMessage());
            return false;
        }
    }

    /**
     * Kiểm tra người chơi đã đăng ký hay chưa.
     */
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

    /**
     * Lấy thông tin chi tiết của người chơi.
     */
    public PlayerData getPlayerData(String name) {
        String sql = "SELECT * FROM blogin_players WHERE name = ?";
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name.toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
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
            }
        } catch (SQLException e) {
            plugin.getLog().warning("✖ Lỗi lấy thông tin người chơi: " + e.getMessage());
        }
        return null;
    }

    /**
     * Xóa tài khoản người chơi (dùng cho admin).
     */
    public boolean deletePlayer(String name) {
        String sql = "DELETE FROM blogin_players WHERE name = ?";
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name.toLowerCase());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            plugin.getLog().warning("✖ Lỗi xóa người chơi: " + e.getMessage());
        }
        return false;
    }

    /**
     * Lấy UUID của người chơi theo tên.
     */
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

    /**
     * Data Transfer Object chứa thông tin đầy đủ của người chơi.
     */
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
