package vn.blogin.database;
import org.bukkit.plugin.IllegalPluginAccessException;
import vn.blogin.Blogin;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
public class DatabaseManager {
    private final Blogin plugin;
    private final DatabaseType type;
    private final String host, database, username, password;
    private final int port, poolSize;
    private final boolean useSsl;
    private ConnectionPool connectionPool;
    private ExecutorService asyncExecutor;
    public enum DatabaseType {
        SQLITE, MYSQL
    }
    public DatabaseManager(Blogin plugin) {
        this.plugin = plugin;
        String typeStr = plugin.getConfig().getString("database.loai", "SQLITE").toUpperCase();
        DatabaseType parsed;
        try {
            parsed = DatabaseType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            plugin.getLog().warning("⚠ Loại database không hợp lệ (" + typeStr + "), dùng SQLITE.");
            parsed = DatabaseType.SQLITE;
        }
        this.type = parsed;
        this.host = plugin.getConfig().getString("database.mysql.host", "localhost");
        this.port = plugin.getConfig().getInt("database.mysql.port", 3306);
        this.database = plugin.getConfig().getString("database.mysql.database", "blogin");
        this.username = plugin.getConfig().getString("database.mysql.username", "root");
        this.password = plugin.getConfig().getString("database.mysql.password", "");
        this.poolSize = Math.max(1, plugin.getConfig().getInt("database.mysql.pool-size", 10));
        this.useSsl = plugin.getConfig().getBoolean("database.mysql.use-ssl", false);
    }
    public boolean init() {
        try {
            if (type == DatabaseType.SQLITE) {
                File dataFolder = plugin.getDataFolder();
                if (!dataFolder.exists()) dataFolder.mkdirs();
                File dbFile = new File(dataFolder, "blogin.db");
                String jdbcUrl = "jdbc:sqlite:" + dbFile.getAbsolutePath();
                this.connectionPool = new ConnectionPool(jdbcUrl, null, null, 1, true);
            } else {
                String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + database
                        + "?useSSL=" + useSsl
                        + "&allowPublicKeyRetrieval=true&serverTimezone=UTC"
                        + "&cachePrepStmts=true&prepStmtCacheSize=250&prepStmtCacheSqlLimit=2048"
                        + "&useServerPrepStmts=true&rewriteBatchedStatements=true";
                this.connectionPool = new ConnectionPool(jdbcUrl, username, password, poolSize, false);
            }
            createTables();
            initExecutor();
            plugin.getLog().info("✔ Đã kết nối Database (" + type + ")");
            return true;
        } catch (Exception e) {
            plugin.getLog().severe("✖ Lỗi kết nối Database: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    private void initExecutor() {
        int threads = Math.max(1, plugin.getConfig().getInt("database.async-threads", 2));
        AtomicInteger counter = new AtomicInteger();
        ThreadFactory factory = runnable -> {
            Thread t = new Thread(runnable, "Blogin-DB-" + counter.incrementAndGet());
            t.setDaemon(true);
            t.setPriority(Thread.NORM_PRIORITY - 1);
            return t;
        };
        this.asyncExecutor = Executors.newFixedThreadPool(threads, factory);
    }
    public <T> void runAsync(java.util.function.Supplier<T> supplier,
                           java.util.function.Consumer<T> callback) {
        runAsync(supplier, callback, null);
    }
    public <T> void runAsync(java.util.function.Supplier<T> supplier,
                           java.util.function.Consumer<T> callback,
                           java.util.function.Consumer<Throwable> onError) {
        if (asyncExecutor == null || asyncExecutor.isShutdown() || !plugin.isEnabled()) {
            runSync(supplier, callback, onError);
            return;
        }
        asyncExecutor.submit(() -> {
            T result;
            try {
                result = supplier.get();
            } catch (Throwable t) {
                plugin.getLog().warning("✖ Lỗi tác vụ database: " + t.getMessage());
                if (onError != null) {
                    scheduleOnMainThread(() -> onError.accept(t));
                }
                return;
            }
            scheduleOnMainThread(() -> callback.accept(result));
        });
    }
    private void scheduleOnMainThread(Runnable task) {
        try {
            if (!plugin.isEnabled()) {
                task.run();
                return;
            }
            plugin.getServer().getScheduler().runTask(plugin, task);
        } catch (IllegalPluginAccessException | IllegalStateException ex) {
            try {
                task.run();
            } catch (Exception ignored) { }
        }
    }
    private <T> void runSync(java.util.function.Supplier<T> supplier,
                            java.util.function.Consumer<T> callback,
                            java.util.function.Consumer<Throwable> onError) {
        try {
            T result = supplier.get();
            callback.accept(result);
        } catch (Throwable t) {
            plugin.getLog().warning("✖ Lỗi tác vụ database: " + t.getMessage());
            if (onError != null) onError.accept(t);
        }
    }
    private void createTables() throws SQLException {
        try (Connection conn = getConnection(); Statement st = conn.createStatement()) {
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS blogin_players (
                    id INTEGER PRIMARY KEY %s,
                    uuid VARCHAR(36) UNIQUE NOT NULL,
                    name VARCHAR(32) UNIQUE NOT NULL,
                    password_hash VARCHAR(255) NOT NULL,
                    totp_secret VARCHAR(255),
                    totp_enabled BOOLEAN DEFAULT 0,
                    last_ip VARCHAR(45),
                    registration_date BIGINT NOT NULL,
                    last_login BIGINT DEFAULT 0,
                    login_count BIGINT DEFAULT 0
                )""".formatted(type == DatabaseType.MYSQL ? "AUTO_INCREMENT" : "AUTOINCREMENT"));
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS blogin_attempts (
                    id INTEGER PRIMARY KEY %s,
                    identifier VARCHAR(255) NOT NULL,
                    attempt_type VARCHAR(16) NOT NULL,
                    attempt_time BIGINT NOT NULL,
                    ip_address VARCHAR(45)
                )""".formatted(type == DatabaseType.MYSQL ? "AUTO_INCREMENT" : "AUTOINCREMENT"));
            createIndexIfMissing(st, "idx_uuid", "blogin_players(uuid)");
            createIndexIfMissing(st, "idx_name", "blogin_players(name)");
            createIndexIfMissing(st, "idx_attempts", "blogin_attempts(identifier, attempt_time)");
        } catch (SQLException e) {
            plugin.getLog().warning("⚠ Lỗi tạo bảng (có thể bỏ qua nếu MySQL): " + e.getMessage());
            try (Connection conn = getConnection(); Statement st = conn.createStatement()) {
                st.executeUpdate("CREATE INDEX idx_uuid ON blogin_players(uuid)");
                st.executeUpdate("CREATE INDEX idx_name ON blogin_players(name)");
                st.executeUpdate("CREATE INDEX idx_attempts ON blogin_attempts(identifier, attempt_time)");
            } catch (SQLException ignored) { }
        }
    }
    private void createIndexIfMissing(Statement st, String indexName, String target) {
        try {
            st.executeUpdate("CREATE INDEX " + indexName + " ON " + target);
        } catch (SQLException e) {
        }
    }
    public Connection getConnection() throws SQLException {
        if (connectionPool == null) {
            throw new SQLException("Connection pool chưa được khởi tạo!");
        }
        return connectionPool.getConnection();
    }
    public void close() {
        if (asyncExecutor != null) {
            asyncExecutor.shutdown();
            try {
                if (!asyncExecutor.awaitTermination(3, TimeUnit.SECONDS)) {
                    asyncExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                asyncExecutor.shutdownNow();
            }
            asyncExecutor = null;
        }
        if (connectionPool != null) {
            connectionPool.close();
            plugin.getLog().info("✔ Đã đóng kết nối Database");
        }
    }
    public DatabaseType getType() { return type; }
    public int getPoolSize() { return connectionPool == null ? 0 : connectionPool.getMaxSize(); }
    public int getIdleConnections() { return connectionPool == null ? 0 : connectionPool.getIdleCount(); }
}
