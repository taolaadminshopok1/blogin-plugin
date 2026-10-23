package vn.blogin.listeners;

import vn.blogin.Blogin;
import vn.blogin.welcome.WelcomeManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

/**
 * Lắng nghe sự kiện người chơi vào server - tạo hiệu ứng chào mừng rực rỡ.
 *
 * <p>Khi người chơi vào:
 * <ul>
 *   <li>Tạo session mới</li>
 *   <li>Hiển thị tin nhắn chào mừng đẹp mắt</li>
 *   <li>Phát hiệu ứng welcome: Title + Particle + Sound + BossBar + Fireworks</li>
 *   <li>Áp hiệu ứng hạn chế (Blindness, Slow, Jump Boost)</li>
 *   <li>Ẩn người chơi khỏi tab & thế giới khi chưa đăng nhập</li>
 * </ul>
 *
 * @author BloginVN
 */
public class PlayerJoinListener implements Listener {

    private final Blogin plugin;

    public PlayerJoinListener(Blogin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String ip = player.getAddress() != null
                ? player.getAddress().getAddress().getHostAddress() : "unknown";

        // 1. Tạo session mới
        plugin.getSessionManager().createSession(player.getUniqueId(), ip);

        // 2. Kiểm tra đã đăng ký chưa
        boolean registered = plugin.getAuthManager().getPlayerDAO().isRegistered(player.getName());

        // 3. Hiển thị tin nhắn chào mừng đẹp mắt trong chat
        sendWelcomeMessage(player, registered);

        // 4. Cấp captcha nếu bật
        if (plugin.getCaptchaManager().isEnabled()) {
            String[] cap = plugin.getCaptchaManager().generate();
            plugin.getSessionManager().setPendingCaptcha(player.getUniqueId(), cap[0], cap[1]);
            player.sendMessage(plugin.getMessageManager().get(
                    "captcha.tin-nhan-captcha", "&e[Captcha] &fVui lòng nhập mã: &a&l{captcha}",
                    "captcha", cap[0]));
        }

        // 5. Áp hiệu ứng hạn chế tạm thời
        applyRestrictionEffects(player);

        // 6. Ẩn người chơi khỏi người khác khi chưa đăng nhập
        hideFromOthers(player);

        // 7. Chạy hiệu ứng welcome rực rỡ (nếu được bật trong config)
        if (plugin.getConfig().getBoolean("welcome.bat", true)) {
            plugin.getWelcomeManager().playWelcomeSequence(player, !registered);
        }
    }

    /**
     * Gửi tin nhắn chào mừng đẹp mắt.
     */
    private void sendWelcomeMessage(Player player, boolean registered) {
        // Header đẹp
        player.sendMessage(plugin.getMessageManager().applyColors(""));
        player.sendMessage(plugin.getMessageManager().applyColors(
                "&6&l✦═════════════════════════════════════════✦"));
        player.sendMessage(plugin.getMessageManager().applyColors(""));
        player.sendMessage(plugin.getMessageManager().applyPlaceholders(
                plugin.getMessageManager().applyColors(
                        "  &e&l✦ &6&lCHÀO MỪNG &e&l✦"),
                "player", player.getName()));
        player.sendMessage(plugin.getMessageManager().applyColors(""));
        player.sendMessage(plugin.getMessageManager().applyPlaceholders(
                plugin.getMessageManager().applyColors(
                        "  &fChào bạn &b&l{player}&f!"),
                "player", player.getName()));
        player.sendMessage(plugin.getMessageManager().applyColors(""));
        player.sendMessage(plugin.getMessageManager().applyColors(
                "  &7Vui lòng đăng nhập để chơi game!"));
        player.sendMessage(plugin.getMessageManager().applyColors(""));

        if (!registered) {
            player.sendMessage(plugin.getMessageManager().applyColors(
                    "  &a➤ &fĐăng ký: &e/register <mật_khẩu> <xác_nhận>"));
        } else {
            player.sendMessage(plugin.getMessageManager().applyColors(
                    "  &a➤ &fĐăng nhập: &e/login <mật_khẩu>"));
        }

        // Captcha
        if (plugin.getCaptchaManager().isEnabled()) {
            player.sendMessage(plugin.getMessageManager().applyColors(
                    "  &a➤ &fLấy captcha mới: &e/captcha"));
        }

        player.sendMessage(plugin.getMessageManager().applyColors(""));
        player.sendMessage(plugin.getMessageManager().applyColors(
                "&6&l✦═════════════════════════════════════════✦"));
        player.sendMessage(plugin.getMessageManager().applyColors(""));
    }

    /**
     * Áp hiệu ứng hạn chế khi chưa đăng nhập.
     */
    private void applyRestrictionEffects(Player player) {
        boolean blindness = plugin.getConfig().getBoolean("hieu-ung.blindness", true);
        boolean slow = plugin.getConfig().getBoolean("hieu-ung.slow", true);
        boolean jump = plugin.getConfig().getBoolean("hieu-ung.jump-boost", true);

        if (blindness) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS,
                    Integer.MAX_VALUE, 0, false, false));
        }
        if (slow) {
            int level = plugin.getConfig().getInt("hieu-ung.slow-level", 5);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW,
                    Integer.MAX_VALUE, level, false, false));
        }
        if (jump) {
            int level = plugin.getConfig().getInt("hieu-ung.jump-boost-level", 128);
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST,
                    Integer.MAX_VALUE, level, false, false));
        }
    }

    /**
     * Ẩn người chơi chưa đăng nhập khỏi những người khác.
     */
    private void hideFromOthers(Player player) {
        for (Player other : player.getServer().getOnlinePlayers()) {
            if (!other.getUniqueId().equals(player.getUniqueId())) {
                if (other.canSee(player)) {
                    other.hidePlayer(plugin, player);
                }
            }
        }
    }
}
