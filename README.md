
# 🛡️ Blogin - Next-Gen Minecraft Authentication Plugin

**Giải pháp bảo mật, xác thực người chơi & giao diện GUI toàn diện nhất cho Minecraft Server.**

[![Release](https://img.shields.io/badge/Release-v3.2.0-blue.svg?style=for-the-badge&logo=git)](https://github.com/taolaadminshopok1/blogin-plugin/releases)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1%20--%201.21.11%20%7C%2026.x-brightgreen.svg?style=for-the-badge&logo=minecraft)](https://papermc.io)
[![Java](https://img.shields.io/badge/Java-17%2B%20%7C%2025%20(Paper%2026.x)-orange.svg?style=for-the-badge&logo=openjdk)](https://adoptium.net)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)](LICENSE)
[![Build Status](https://img.shields.io/badge/CI-Passing-success.svg?style=for-the-badge&logo=githubactions)](https://github.com/taolaadminshopok1/blogin-plugin/actions)

<p align="center">
  <a href="#-tính-năng-nổi-bật">Tính năng</a> •
  <a href="#-kiến-trúc--bản-vá-320">Bản vá 3.2.0</a> •
  <a href="#-cài-đặt--triển-khai">Cài đặt</a> •
  <a href="#-lệnh--quyền-hạn">Lệnh & Quyền</a> •
  <a href="#-placeholderapi">PAPI</a> •
  <a href="#-tùy-biến-config">Cấu hình</a> •
  <a href="#-xử-lý-sự-cố">Troubleshooting</a>
</p>

</div>

---

## 🌟 Tổng quan

**Blogin** là plugin xác thực danh tính thế hệ mới dành cho Paper/Spigot server. Được thiết kế theo chuẩn doanh nghiệp nhằm tối ưu hóa hiệu năng, xử lý triệt để tình trạng nghẽn Main Thread (Zero TPS Drop) khi có hàng loạt người chơi đăng nhập cùng lúc, tích hợp GUI tương tác trực quan, chống brute-force đa tầng và hệ thống đa ngôn ngữ tự động (i18n).

---

## 🎯 Tính năng nổi bật

### 🔒 Bảo mật tối đa (Hardened Security)
* **BCrypt Hashing:** Tự động điều chỉnh `cost factor` (4–16) kèm cơ chế tự động re-hash khi nâng độ phức tạp.
* **Mã hóa 2FA (RFC 6238 TOTP):** Tương thích hoàn toàn với Google Authenticator, Authy, Microsoft Authenticator. Toàn bộ `secret key` lưu trữ trong database được mã hóa bằng thuật toán **AES-GCM (256-bit)**.
* **Chống Brute-Force & Bot:** Tự động khóa IP hoặc Account khi vượt ngưỡng đăng nhập sai. Tích hợp Captcha ngẫu nhiên chống script spam register.
* **Session & Anti-Timing Attack:** Session quản lý in-memory an toàn, zero plaintext password lưu trên RAM sau khi xác thực.

### ⚡ Hiệu năng vượt trội (High Concurrency)
* **100% Async Database Processing:** Toàn bộ tác vụ nặng (BCrypt verify, SQLite/MySQL read/write) được đẩy sang Thread Pool riêng biệt.
* **Smart Connection Pooling:** Tái sử dụng kết nối cơ sở dữ liệu, loại bỏ overhead tạo/đóng kết nối liên tục.
* **Dynamic Server Load Throttling:** Tự động giảm hoặc vô hiệu hóa hiệu ứng particle khi TPS dao động hoặc lượng online vượt mốc chỉ định.

### 🌍 Đa ngôn ngữ thông minh (i18n)
* Tích hợp sẵn **10 ngôn ngữ** hoàn chỉnh (217 translation keys mỗi tệp).
* Tự động nhận diện ngôn ngữ client (`Player#getLocale`) ngay khi kết nối.

### 🎨 Trải nghiệm người dùng (UX / UI)
* **Interactive Chest GUI (27 ô):** Hiển thị Skin Head có hiệu ứng glint, nhập pass/2FA trực tiếp trên chat được ẩn nội dung, cập nhật thời gian thực.
* **Hệ thống Welcome sống động:** Tích hợp 12 loại particle, pháo hoa thật, âm thanh hợp âm, BossBar gradient và hiệu ứng Typewriter.

---

## 🔄 Luồng xác thực bất đồng bộ (Lifecycle Flow)

```mermaid
sequenceDiagram
    autonumber
    actor Player as 🎮 Người chơi
    participant Server as ⚙️ Paper Main Thread
    participant Auth as 🧠 AuthManager (Async Pool)
    participant DB as 🗄️ Database (MySQL/SQLite)
    participant Cache as ⚡ Memory Session

    Player->>Server: /login <mật khẩu> [2FA]
    Server->>Server: Kiểm tra Captcha & Regex đầu vào
    Server->>Auth: Đẩy tác vụ kiểm tra (Async Dispatch)
    Auth->>Cache: Kiểm tra trạng thái khóa IP/Tài khoản
    Auth->>DB: Truy vấn hash mật khẩu & secret 2FA
    Auth->>Auth: Xác minh BCrypt (~200ms) & giải mã AES-GCM TOTP
    Auth->>DB: Ghi log đăng nhập & cập nhật last_login
    Auth-->>Server: Trả AuthResult (Thành công)
    Server->>Cache: markAuthenticated(UUID)
    Server->>Player: Hủy hiệu ứng hạn chế, mở Welcome FX & Unlock di chuyển

🌍 Kiến trúc & Bản vá 3.2.0
🎯 Ma trận ngôn ngữ hỗ trợ
| Mã | Ngôn ngữ | Mã | Ngôn ngữ |
|---|---|---|---|
| vi | 🇻🇳 Tiếng Việt (Default) | ja | 🇯🇵 日本語 |
| en | 🇬🇧 English | ko | 🇰🇷 한국어 |
| zh | 🇨🇳 简体中文 | ru | 🇷🇺 Русский |
| es | 🇪🇸 Español | pt | 🇵🇹 Português |
| fr | 🇫🇷 Français | de | 🇩🇪 Deutsch |
Thứ tự ưu tiên nạp ngôn ngữ:
 * Lựa chọn cá nhân của người chơi qua /blogin language <mã> (Lưu theo UUID).
 * Tự động đọc từ Client Locale (Player#getLocale).
 * Cấu hình mặc định trong config.yml (ngon-ngu: "vi").
 * Fallback về tệp ngôn ngữ gốc hoặc hard-coded fallback strings.
🐛 Báo cáo sửa lỗi (Changelog v3.1.0 - v3.2.0)
🔴 Lỗ hổng bảo mật đã triệt tiêu
 * Lỗ hổng Bypass Captcha qua GUI: LoginGui trước đây gọi trực tiếp loginAsync() bỏ qua bước kiểm tra captcha. Đã tái cấu trúc GUI: người chơi bắt buộc phải nhập và giải captcha hợp lệ trên menu/chat trước khi kích hoạt nút đăng nhập.
 * Rò rỉ thông tin trong RAM: Plaintext password và mã 2FA tạm thời trong biến guiPassword/guiTotp hiện được dọn sạch hoàn toàn khỏi memory ngay khi quá trình xác thực kết thúc hoặc thất bại.
⚡ Khắc phục hiệu năng & Tương thích API
 * Triệt tiêu Main Thread DB Query: Loại bỏ hàm đếm số lần thử thất bại COUNT(*) chạy trên luồng chính; thay thế hoàn toàn bằng Concurrent In-Memory Cache với TTL tự hủy.
 * Tương thích Paper 26.x & 1.21.x:
   * Thay thế triệt để các trường Enum bị đổi tên (PotionEffectType.SLOW \to SLOWNESS, Particle.REDSTONE \to DUST) thông qua lớp đệm vn.blogin.utils.Compat.
   * Sửa lỗi Particle.spawnParticle với count = 0 khiến toàn bộ hiệu ứng biến mất trên các bản Paper hiện đại.
 * Chống rò rỉ Scheduler & Memory: Mọi tác vụ chạy nền (BossBar, Actionbar, Particle) hiện đã được gán UUID tracking và tự hủy (cancel()) ngay khi người chơi ngắt kết nối.
📦 Cài đặt & Triển khai
Yêu cầu môi trường
 * Runtime: Java 17 hoặc Java 21 (Với Paper 26.x yêu cầu JDK 25).
 * Nền tảng: Paper, Purpur, Spigot từ 1.20.1 đến 1.21.11 & 26.x.
 * Bộ nhớ: Tối thiểu 512MB RAM khả dụng cho plugin trên các cụm máy chủ lớn.
📥 Biên dịch từ mã nguồn
git clone [https://github.com/taolaadminshopok1/blogin-plugin.git](https://github.com/taolaadminshopok1/blogin-plugin.git)
cd blogin-plugin

# Trên Windows (Tự động định vị JDK & Maven)
.\mvnw.ps1 clean package

# Trên Linux / macOS
mvn clean package

> Hồ sơ build đa phiên bản:
>  * Mặc định: Java 17 bytecode (Tương thích toàn diện từ 1.20.1 đến 26.x).
>  * mvn clean package -P paper-1.21.11: Tối ưu riêng cho bản build 1.21.11.
>  * mvn clean package -P paper-26: Build dành riêng cho Paper 26.x (Yêu cầu JDK 25).
> 
🚀 Cài đặt nhanh
 * Copy file JAR vừa build tại target/Blogin-3.2.0.jar vào thư mục plugins/ của máy chủ.
 * Khởi động server để plugin tự động giải nén cấu hình và cơ sở dữ liệu mẫu.
 * Tùy chỉnh plugins/Blogin/config.yml theo nhu cầu và chạy lệnh /blogin reload.
> ⚠️ CẢNH BÁO BẢO MẬT: File plugins/Blogin/secret.key chứa khóa Master AES dùng để mã hóa toàn bộ dữ liệu 2FA của người chơi. Tuyệt đối không làm mất hoặc để lộ file này.
> 
🎮 Lệnh & Quyền hạn
👤 Dành cho người chơi
| Lệnh | Alias | Quyền hạn | Mô tả |
|---|---|---|---|
| /login <mk> [2fa] | /dangnhap | Mặc định | Đăng nhập vào hệ thống |
| /register <mk> <nhập_lại> | /dangky, /reg | Mặc định | Đăng ký tài khoản mới |
| /blogingui | /bgui, /logingui | Mặc định | Mở menu tương tác đăng nhập |
| /logout | /dangxuat | Mặc định | Đăng xuất và khóa quyền tạm thời |
| /changepassword <cũ> <mới> | /cpass, /doimk | Mặc định | Đổi mật khẩu tài khoản |
| /2fa <enable|confirm|disable|status> | /totp, /otp | Mặc định | Quản lý xác thực hai yếu tố |
| /captcha | /cap | Mặc định | Yêu cầu đổi mã Captcha mới |
| /blogin language [mã] | /lang, /ngonngu | Mặc định | Xem hoặc đổi ngôn ngữ hiển thị |
👑 Dành cho Quản trị viên (Admin)
> Yêu cầu quyền: blogin.admin (Mặc định: OP)
> 
| Lệnh | Mô tả |
|---|---|
| /blogin reload | Tải lại toàn bộ configuration, message & lang file |
| /blogin info <player> | Tra cứu chi tiết tài khoản, IP đăng ký, UUID, trạng thái 2FA |
| /blogin reset <player> <mk_mới> | Đặt lại mật khẩu cho người chơi chỉ định |
| /blogin force <player> | Ép buộc đăng nhập bỏ qua mật khẩu (Xử lý sự cố) |
| /blogin unlock <player> | Gỡ khóa tài khoản/IP bị hạn chế do brute-force |
| /blogin unregister <player> | Xóa toàn bộ dữ liệu tài khoản khỏi cơ sở dữ liệu |
| /blogin 2fa <player> <on|off|secret> | Quản lý hoặc tắt khẩn cấp 2FA của thành viên |
| /blogin list [trang] | Liệt kê danh sách tài khoản đã đăng ký (Phân trang) |
| /bloginwelcome test <new|back|login> | Kiểm tra hiệu ứng Welcome trực quan |
🔌 PlaceholderAPI
Tích hợp trực tiếp vào Scoreboard, TAB, Chat Formatter hoặc DeluxeMenus:
| Placeholder | Mô tả | Giá trị trả về |
|---|---|---|
| %blogin_authenticated% | Kiểm tra trạng thái đã đăng nhập | yes / no |
| %blogin_registered% | Kiểm tra tài khoản đã đăng ký hay chưa | yes / no |
| %blogin_2fa% | Trạng thái kích hoạt bảo mật 2FA | yes / no |
| %blogin_ip_current% | Địa chỉ IP hiện tại của người chơi | String |
| %blogin_remaining_attempts% | Số lượt thử mật khẩu còn lại trước khi khóa | Integer |
| %blogin_timeout% | Thời gian đếm ngược còn lại để login (giây) | Integer |
| %blogin_total_players% | Tổng số lượng người chơi trong cơ sở dữ liệu | Integer |
| %blogin_totp_enabled_count% | Tổng số tài khoản đang sử dụng 2FA | Integer |
⚙️ Tùy biến Config (config.yml)
# ========================================================
#              BLOGIN CORE CONFIGURATION
# ========================================================

# Thời gian tối đa cho phép thực hiện đăng nhập (giây)
thoi-gian-dang-nhap: 60

# Cấu hình Cơ sở dữ liệu
database:
  loai: "SQLITE"         # Tuỳ chọn: SQLITE hoặc MYSQL
  async-threads: 2        # Số thread xử lý truy vấn song song
  mysql:
    host: "localhost"
    port: 3306
    database: "blogin_db"
    username: "blogin_user"
    password: "StrongPasswordHere"
    pool-size: 10
    use-ssl: false

# Thiết lập An ninh & Phòng thủ
bao-mat:
  so-lan-sai-toi-da: 5
  thoi-gian-khoa: 300     # Khóa 5 phút nếu vượt ngưỡng
  che-do-khoa: "BOTH"     # Khóa theo: ACCOUNT, IP hoặc BOTH
  bcrypt-cost: 12         # Khuyến nghị: 10-12
  chan-mat-khau-yeu: true
  diem-mat-khau-toi-thieu: 2 # Thang điểm 0 - 5
  mat-khau-toi-thieu: 6
  mat-khau-toi-da: 64

# Menu đồ hoạ
gui:
  bat: true
  auto-mo: true
  tieu-de: "&#00E5FF&lBLOGIN &#888888| &f{player}"
  hien-dau-nguoi-choi: true
  hien-o-captcha: true

# Hệ thống 2FA (RFC 6238 TOTP)
two-factor:
  bat: true
  ten-issuer: "BloginMC"
  bat-buoc-admin: true    # Tự động gửi cảnh báo nếu Admin chưa kích hoạt 2FA
  so-chu-so: 6
  thoi-gian-hieu-luc: 30

# Hiệu ứng Chào mừng & Âm thanh
welcome:
  bat: true
  muc-do: "EPIC"          # MINIMAL | NORMAL | EPIC | CUSTOM
  nguong-chat-server: 60  # Tự động ngắt bớt particle nếu online > 60 để giữ TPS

🛠️ Cấu trúc mã nguồn
src/main/java/vn/blogin/
├── Blogin.java                 # Main Class & Lifecycle Coordinator
├── commands/                   # Command Handlers (10 lệnh độc lập)
├── database/                   # Persistence Layer (ConnectionPool, DAOs)
├── gui/                        # Inventory GUI Engine & Canvas
├── listeners/                  # Event Handlers (Traffic Interceptors)
├── managers/                   # Business Services (Auth, Session, i18n)
├── placeholder/                # Hook PlaceholderAPI
├── utils/                      # Mật mã học (AES-GCM, BCrypt, TOTP) & Compat API
└── welcome/                    # Visual & Audio Effects Orchestrator

❓ Xử lý sự cố (Troubleshooting)
<details>
<summary><b>1. Plugin báo lỗi không thể tạo khoá mã hoá (Encryption Key Error)?</b></summary>
Kiểm tra quyền ghi thư mục (Write Permissions) của user chạy server trên Linux. Nếu file <code>secret.key</code> bị hỏng hoặc rỗng, hãy xóa file đó để plugin tạo lại (Lưu ý: Thao tác này sẽ khiến các mã secret 2FA cũ phải cài đặt lại).
</details>
<details>
<summary><b>2. Bị lag giật mỗi khi có đợt người chơi đăng nhập ồ ạt?</b></summary>
Hạ giá trị <code>bcrypt-cost</code> trong <code>config.yml</code> xuống <code>10</code>. Đảm bảo bạn đang sử dụng Paper hoặc các nhánh tối ưu thay vì CraftBukkit/Spigot thuần.
</details>
<details>
<summary><b>3. Người chơi không thấy mã Captcha hoặc GUI không phản hồi?</b></summary>
Kiểm tra xem các plugin quản lý chat hoặc quản lý túi đồ khác (như ProtocolLib, ChatColor plugins) có đang chặn packet gửi tin nhắn ẩn hoặc sự kiện <code>InventoryClickEvent</code> hay không.
</details>
📄 Bản quyền & Đóng góp
Phát triển bởi đội ngũ BloginVN. Phát hành theo giấy phép MIT License. Mọi đóng góp Pull Request cải tiến mã nguồn hoặc bản dịch ngôn ngữ luôn được hoan nghênh.

