package vn.blogin.database;
import vn.blogin.Blogin;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
public class AttemptDAO {
    private final Blogin plugin;
    public AttemptDAO(Blogin plugin) {
        this.plugin = plugin;
    }
    public void recordFailure(String identifier, String type, String ip) {
        String sql = "INSERT INTO blogin_attempts (identifier, attempt_type, attempt_time, ip_address) VALUES (?, ?, ?, ?)";
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, identifier);
            ps.setString(2, type);
            ps.setLong(3, System.currentTimeMillis());
            ps.setString(4, ip);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLog().warning("✖ Lỗi ghi attempt: " + e.getMessage());
        }
    }
    public int countRecentFailures(String identifier, long sinceMs) {
        String sql = "SELECT COUNT(*) AS cnt FROM blogin_attempts WHERE identifier = ? AND attempt_time >= ?";
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, identifier);
            ps.setLong(2, sinceMs);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("cnt");
            }
        } catch (SQLException e) {
            plugin.getLog().warning("✖ Lỗi đếm attempt: " + e.getMessage());
        }
        return 0;
    }
    public int cleanupOldAttempts(long olderThanMs) {
        String sql = "DELETE FROM blogin_attempts WHERE attempt_time < ?";
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, olderThanMs);
            return ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLog().warning("✖ Lỗi cleanup attempts: " + e.getMessage());
        }
        return 0;
    }
    public void clearAttempts(String identifier) {
        String sql = "DELETE FROM blogin_attempts WHERE identifier = ?";
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, identifier);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLog().warning("✖ Lỗi xóa attempts: " + e.getMessage());
        }
    }
}
