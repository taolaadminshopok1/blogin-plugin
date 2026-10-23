package vn.blogin.database;

import vn.blogin.Blogin;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Khởi tạo và quản lý kết nối Database (MySQL hoặc SQLite).
 *
 * <p>Singleton pattern: chỉ có một instance duy nhất trong plugin.</p>
 *
 * @author BloginVN
 */
public class DatabaseManager {

    private final Blogin plugin;
    private final DatabaseType type;
    private final String host, database, username, password;
    private final int port, poolSize;
    private final boolean useSsl;

    private ConnectionPool connectionPool;

    /**
     * Loại database được cấu hình trong config.yml.
     */
    public enum DatabaseType {
        SQLITE, MYSQL
    }

    /**
     * Khởi tạo DatabaseManager từ config.
     *
     * @param plugin instance chính của Blogin
     */
    public DatabaseManager(Blogin plugin) {
        this.plugin = plugin;

        String typeStr = plugin.getConfig().getString("database.loai", "SQLITE").toUpperCase();
        this.type = DatabaseType.valueOf(typeStr);

        this.host = plugin.getConfig().getString("database.mysql.host", "localhost");
        this.port = plugin.getConfig().getInt("database.mysql.port", 3306);
        this.database = plugin.getConfig().getString("database.mysql.database", "blogin");
        this.username = plugin.getConfig().getString("database.mysql.username", "root");
        this.password = plugin.getConfig().getString("database.mysql.password", "");
        this.poolSize = plugin.getConfig().getInt("database.mysql.pool-size", 10);
        this.useSsl = plugin.getConfig().getBoolean("database.mysql.use-ssl", false);
    }

    /**
     * Khởi tạo connection pool và tạo bảng nếu chưa có.
     *
     * @return true nếu khởi tạo thành công
     */
    public boolean init() {
        try {
            if (type == DatabaseType.SQLITE) {
                File dataFolder = plugin.getDataFolder();
                if (!dataFolder.exists()) dataFolder.mkdirs();
                File dbFile = new File(dataFolder, "blogin.db");
                String jdbcUrl = "jdbc:sqlite:" + dbFile.getAbsolutePath();
                this.connectionPool = new ConnectionPool(jdbcUrl, null, null, 1, true);
            } else {
                String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/""
                        + database + "?useSSL=" + useSsl
                        + "&allowPublicKeyRetrieval=true&serverTimezone=UTC";
                this.connectionPool = new ConnectionPool(jdbcUrl, username, password, poolSize, false);
            }

            createTables();
            plugin.getLog().info("✔ Đã kết nối Database (" + type + ")");
            return true;
        } catch (Exception e) {
            plugin.getLog().severe("✖ Lỗi kết nối Database: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Tạo các bảng cần thiết.
     *
     * <p>Bảng {@code blogin_players}:
     * <ul>
     *   <li>id - khóa chính</li>
     *   <li>uuid - mã người chơi</li>
     *   <li>name - tên người chơi (lowercase)</li>
     *   <li>password_hash - BCrypt hash</li>
     *   <li>totp_secret - secret key 2FA (mã hóa base32)</li>
     *   <li>totp_enabled - bật/tắt 2FA</li>
     *   <li>last_ip - IP cuối cùng đăng nhập</li>
     *   <li>registration_date - thời điểm đăng ký (epoch ms)</li>
     *   <li>last_login - thời điểm đăng nhập cuối (epoch ms)</li>
     *   <li>login_count - số lần đã đăng nhập</li>
     * </ul>
     *
     * <p>Bảng {@code blogin_attempts}: ghi lại các lần đăng nhập thất bại (chống brute-force).</p>
     */
    private void createTables() throws SQLException {
        try (Connection conn = getConnection(); Statement st = conn.createStatement()) {

            // Bảng người chơi
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS blogin_players (
                    id INTEGER PRIMARY KEY %s,
                    uuid VARCHAR(36) UNIQUE NOT NULL,
                    name VARCHAR(32) UNIQUE NOT NULL,
                    password_hash VARCHAR(255) NOT NULL,
                    totp_secret VARCHAR(64),
                    totp_enabled BOOLEAN DEFAULT 0,
                    last_ip VARCHAR(45),
                    registration_date BIGINT NOT NULL,
                    last_login BIGINT DEFAULT 0,
                    login_count BIGINT DEFAULT 0
                )""".formatted(type == DatabaseType.MYSQL ? "AUTO_INCREMENT" : "AUTOINCREMENT"));

            // Bảng lịch sử đăng nhập thất bại
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS blogin_attempts (
                    id INTEGER PRIMARY KEY %s,
                    identifier VARCHAR(255) NOT NULL,
                    attempt_type VARCHAR(16) NOT NULL,
                    attempt_time BIGINT NOT NULL,
                    ip_address VARCHAR(45)
                )""".formatted(type == DatabaseType.MYSQL ? "AUTO_INCREMENT" : "AUTOINCREMENT"));

            // Index để tăng tốc truy vấn
            st.executeUpdate("CREATE INDEX IF NOT EXISTS idx_uuid ON blogin_players(uuid)");
            st.executeUpdate("CREATE INDEX IF NOT EXISTS idx_name ON blogin_players(name)");
            st.executeUpdate("CREATE INDEX IF NOT EXISTS idx_attempts ON blogin_attempts(identifier, attempt_time)");

        } catch (SQLException e) {
            plugin.getLog().warning("⚠ Lỗi tạo bảng (có thể bỏ qua nếu MySQL): " + e.getMessage());
            // MySQL không hỗ trợ IF NOT EXISTS cho index, chạy riêng
            try (Connection conn = getConnection(); Statement st = conn.createStatement()) {
                st.executeUpdate("CREATE INDEX idx_uuid ON blogin_players(uuid)");
                st.executeUpdate("CREATE INDEX idx_name ON blogin_players(name)");
                st.executeUpdate("CREATE INDEX idx_attempts ON blogin_attempts(identifier, attempt_time)");
            } catch (SQLException ignored) { }
        }
    }

    /**
     * Lấy một kết nối từ connection pool.
     *
     * @return Connection hợp lệ
     * @throws SQLException nếu không thể lấy kết nối
     */
    public Connection getConnection() throws SQLException {
        if (connectionPool == null) {
            throw new SQLException("Connection pool chưa được khởi tạo!");
        }
        return connectionPool.getConnection();
    }

    /**
     * Đóng connection pool.
     */
    public void close() {
        if (connectionPool != null) {
            connectionPool.close();
            plugin.getLog().info("✔ Đã đóng kết nối Database");
        }
    }

    public DatabaseType getType() { return type; }
}
