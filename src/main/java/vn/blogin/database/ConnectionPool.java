package vn.blogin.database;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
public class ConnectionPool {
    private final BlockingQueue<Connection> idle;
    private final String jdbcUrl;
    private final String user;
    private final String pass;
    private final int maxSize;
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final AtomicInteger created = new AtomicInteger(0);
    private static final long WAIT_TIMEOUT_MS = 5000L;
    public ConnectionPool(String jdbcUrl, String user, String pass, int maxSize, boolean sqlite) {
        this.jdbcUrl = jdbcUrl;
        this.user = user;
        this.pass = pass;
        this.maxSize = sqlite ? 1 : Math.max(1, maxSize);
        this.idle = new ArrayBlockingQueue<>(this.maxSize);
        try {
            Connection initial = createConnection();
            created.incrementAndGet();
            idle.offer(initial);
        } catch (SQLException e) {
            throw new RuntimeException("Không thể tạo kết nối DB ban đầu: " + e.getMessage(), e);
        }
    }
    private Connection createConnection() throws SQLException {
        Connection conn;
        if (user == null) {
            conn = DriverManager.getConnection(jdbcUrl);
        } else {
            conn = DriverManager.getConnection(jdbcUrl, user, pass);
        }
        conn.setAutoCommit(true);
        return conn;
    }
    public Connection getConnection() throws SQLException {
        if (closed.get()) {
            throw new SQLException("Connection pool đã đóng!");
        }
        Connection conn = idle.poll();
        while (conn == null) {
            synchronized (this) {
                if (created.get() < maxSize) {
                    created.incrementAndGet();
                    try {
                        conn = createConnection();
                    } catch (SQLException e) {
                        created.decrementAndGet();
                        throw e;
                    }
                }
            }
            if (conn == null) {
                try {
                    conn = idle.poll(200, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new SQLException("Bị gián đoạn khi chờ kết nối DB", e);
                }
                if (conn == null && idle.isEmpty() && created.get() >= maxSize) {
                    throw new SQLException("Hết thời gian chờ kết nối DB (pool đã đầy)!");
                }
            }
        }
        boolean valid = false;
        try {
            valid = !conn.isClosed() && conn.isValid(3);
        } catch (SQLException e) {
            valid = false;
        }
        if (!valid) {
            try { conn.close(); } catch (SQLException ignored) { }
            created.decrementAndGet();
            return getConnection();
        }
        return wrap(conn);
    }
    private Connection wrap(Connection real) {
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                (proxy, method, args) -> {
                    try {
                        switch (method.getName()) {
                            case "close" -> {
                                returnConnection(real);
                                return null;
                            }
                            case "isClosed" -> {
                                return real.isClosed();
                            }
                            case "unwrap" -> {
                                if (args != null && args.length == 1 && args[0] == Connection.class) {
                                    return real;
                                }
                                return method.invoke(real, args);
                            }
                            case "isWrapperFor" -> {
                                if (args != null && args.length == 1 && args[0] == Connection.class) {
                                    return true;
                                }
                                return method.invoke(real, args);
                            }
                            default -> {
                                return method.invoke(real, args);
                            }
                        }
                    } catch (InvocationTargetException e) {
                        Throwable cause = e.getCause();
                        if (cause instanceof Exception ex) throw ex;
                        if (cause instanceof Error err) throw err;
                        throw new SQLException(cause);
                    }
                });
    }
    public void returnConnection(Connection conn) {
        if (conn == null) return;
        if (closed.get()) {
            try { conn.close(); } catch (SQLException ignored) { }
            return;
        }
        try {
            if (conn.isClosed()) {
                created.decrementAndGet();
                return;
            }
            if (!conn.getAutoCommit()) conn.setAutoCommit(true);
            if (!idle.offer(conn)) {
                conn.close();
                created.decrementAndGet();
            }
        } catch (SQLException e) {
            created.decrementAndGet();
        }
    }
    public void close() {
        if (closed.compareAndSet(false, true)) {
            Connection conn;
            while ((conn = idle.poll()) != null) {
                try { conn.close(); } catch (SQLException ignored) { }
            }
            created.set(0);
        }
    }
    public int getMaxSize() { return maxSize; }
    public int getCreatedCount() { return created.get(); }
    public int getIdleCount() { return idle.size(); }
    public boolean isClosed() { return closed.get(); }
}
