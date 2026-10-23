package vn.blogin.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Connection pool đơn giản nhưng hiệu quả, hỗ trợ cả MySQL và SQLite.
 *
 * <p>Không phụ thuộc thư viện ngoài, sử dụng {@link BlockingQueue} làm pool.</p>
 *
 * <p>Lưu ý: với SQLite, pool chỉ có 1 kết nối (vì SQLite không hỗ trợ concurrent write).</p>
 *
 * @author BloginVN
 */
public class ConnectionPool {

    private final BlockingQueue<Connection> pool;
    private final String jdbcUrl;
    private final String user;
    private final String pass;
    private final int maxSize;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    /**
     * Khởi tạo connection pool.
     *
     * @param jdbcUrl URL kết nối JDBC
     * @param user    username (có thể null với SQLite)
     * @param pass    password (có thể null với SQLite)
     * @param maxSize kích thước tối đa của pool
     * @param sqlite  true nếu là SQLite (sẽ ép pool size = 1)
     */
    public ConnectionPool(String jdbcUrl, String user, String pass, int maxSize, boolean sqlite) {
        this.jdbcUrl = jdbcUrl;
        this.user = user;
        this.pass = pass;
        this.maxSize = sqlite ? 1 : Math.max(1, maxSize);
        this.pool = new ArrayBlockingQueue<>(this.maxSize);

        // Tạo sẵn 1 kết nối ban đầu để kiểm tra kết nối ngay
        try {
            Connection initial = createConnection();
            pool.offer(initial);
        } catch (SQLException e) {
            throw new RuntimeException("Không thể tạo kết nối DB ban đầu: " + e.getMessage(), e);
        }
    }

    /**
     * Tạo một connection JDBC mới.
     */
    private Connection createConnection() throws SQLException {
        Connection conn;
        if (user == null) {
            conn = DriverManager.getConnection(jdbcUrl);
        } else {
            conn = DriverManager.getConnection(jdbcUrl, user, pass);
        }
        // Auto-commit mặc định = true
        conn.setAutoCommit(true);
        return conn;
    }

    /**
     * Lấy một kết nối từ pool. Nếu pool rỗng sẽ tạo mới (nếu chưa đạt maxSize).
     *
     * @return Connection hợp lệ
     * @throws SQLException nếu không thể lấy sau khi chờ
     */
    public Connection getConnection() throws SQLException {
        if (closed.get()) {
            throw new SQLException("Connection pool đã đóng!");
        }
        Connection conn = pool.poll();
        if (conn == null) {
            // Pool rỗng - tạo mới
            conn = createConnection();
        } else if (conn.isClosed() || !conn.isValid(3)) {
            // Connection cũ đã đóng - tạo mới
            try { conn.close(); } catch (SQLException ignored) { }
            conn = createConnection();
        }
        return conn;
    }

    /**
     * Trả connection về pool. Nếu pool đầy sẽ đóng connection đó.
     */
    public void returnConnection(Connection conn) {
        if (conn == null) return;
        if (closed.get()) {
            try { conn.close(); } catch (SQLException ignored) { }
            return;
        }
        try {
            if (conn.isClosed()) return;
            // Reset về trạng thái an toàn
            if (!conn.getAutoCommit()) conn.setAutoCommit(true);
            if (!pool.offer(conn)) {
                conn.close();
            }
        } catch (SQLException e) {
            // Bỏ qua - sẽ tạo mới khi cần
        }
    }

    /**
     * Đóng toàn bộ pool.
     */
    public void close() {
        if (closed.compareAndSet(false, true)) {
            Connection conn;
            while ((conn = pool.poll()) != null) {
                try { conn.close(); } catch (SQLException ignored) { }
            }
        }
    }

    public int getMaxSize() { return maxSize; }
    public boolean isClosed() { return closed.get(); }

    /**
     * Helper class để tự động trả connection về pool khi dùng try-with-resources.
     *
     * <p>Ví dụ:
     * <pre>{@code
     * try (var wrapper = new ConnectionWrapper(pool, pool.getConnection())) {
     *     // dùng wrapper.get()
     * }
     * }</pre>
     */
    public static class ConnectionWrapper implements AutoCloseable {
        private final ConnectionPool pool;
        private final Connection connection;

        public ConnectionWrapper(ConnectionPool pool, Connection connection) {
            this.pool = pool;
            this.connection = connection;
        }

        public Connection get() { return connection; }

        @Override
        public void close() {
            pool.returnConnection(connection);
        }
    }
}
