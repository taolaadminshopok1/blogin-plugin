package vn.blogin.utils;
import org.mindrot.jbcrypt.BCrypt;
public class HashingUtil {
    public static final int DEFAULT_COST = 12;
    public static final int MIN_COST = 4;
    public static final int MAX_COST = 16;
    private volatile int costFactor;
    public HashingUtil() {
        this(DEFAULT_COST);
    }
    public HashingUtil(int costFactor) {
        this.costFactor = Math.max(MIN_COST, Math.min(MAX_COST, costFactor));
    }
    public void setCostFactor(int costFactor) {
        this.costFactor = Math.max(MIN_COST, Math.min(MAX_COST, costFactor));
    }
    public String hash(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Mật khẩu không được rỗng!");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(costFactor));
    }
    public boolean verify(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) return false;
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    public boolean needsRehash(String hashedPassword) {
        if (hashedPassword == null || hashedPassword.isEmpty()) return true;
        try {
            int currentCost = Integer.parseInt(hashedPassword.substring(4, 6));
            return currentCost < costFactor;
        } catch (Exception e) {
            return false;
        }
    }
    public int getCostFactor() {
        return costFactor;
    }
}
