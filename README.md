<div align="center">

# 🛡️ Blogin — Giải Pháp Xác Thực & Bảo Mật Cao Cấp Cho Minecraft

![Version](https://img.shields.io/badge/Phiên_bản-3.0.1-brightgreen?style=for-the-badge)
![Minecraft](https://img.shields.io/badge/Paper_%2F_Spigot-1.20.1_--_1.21.4-blue?style=for-the-badge)
![Java](https://img.shields.io/badge/Java-17%2B-orange?style=for-the-badge)
![License](https://img.shields.io/badge/License-MIT-purple?style=for-the-badge)

**Plugin đăng nhập/đăng ký thế hệ mới dành riêng cho cộng đồng Minecraft Việt Nam.**  
Tích hợp **Chest GUI đồ hoạ**, **2FA mã hoá AES-GCM**, **BCrypt bất đồng bộ**, **Captcha chống Bot**, **Brute-force Protection** và **Hệ thống hiệu ứng Welcome rực rỡ** — Tối ưu hoá 100% không gây tụt TPS cho server lớn.

[Tính năng](#-tính-năng-nổi-bật) • [Điểm mới v3.0.1](#-cập-nhật-phiên-bản-301) • [Cài đặt](#-hướng-dẫn-cài-đặt) • [Lệnh & Quyền](#-hướng-dẫn-sử-dụng) • [Cấu hình](#️-cấu-hình-configyml)

</div>

---

## 🎯 Tính Năng Nổi Bật

### 🔐 Bảo Mật Đỉnh Cao (Enterprise-Grade Security)
- **Băm mật khẩu BCrypt:** Hỗ trợ cấu hình `cost factor` (4–16), tự động sinh salt ngẫu nhiên và **tự động re-hash** khi admin nâng cấp độ khó trong config.
- **Xác thực 2 lớp (2FA / TOTP):** Chuẩn quốc tế **RFC 6238** (tương thích *Google Authenticator, Authy, Microsoft Authenticator*), tích hợp cơ chế chống tấn công đo thời gian (*Timing Attack*).
- **Mã hoá khoá 2FA bằng AES-GCM:** Toàn bộ mã Secret 2FA của người chơi được mã hoá bằng khoá riêng tại `plugins/Blogin/secret.key` — ngay cả khi lộ Database, kẻ tấn công cũng không thể lấy được mã 2FA.
- **Chống Brute-Force & IP-Lock:** Khoá tự động theo Tài khoản, IP hoặc Cả hai (`ACCOUNT` / `IP` / `BOTH`). Chặn kết nối xấu ngay từ `PlayerLoginEvent` trước khi người chơi kịp vào thế giới. Hỗ trợ tự động đăng nhập theo IP tin cậy (tự động tắt với tài khoản bật 2FA).
- **Kiểm soát chất lượng mật khẩu:** Chấm điểm độ mạnh (thang 0–5), chặn các mật khẩu phổ biến dễ đoán (`123456`, `password`...) và nhắc nhở Admin bật 2FA định kỳ.
- **Captcha thông minh:** Ngăn chặn triệt để các đợt tấn công dùng Bot đăng ký/đăng nhập hàng loạt.

### ⚡ Hiệu Năng Vượt Trội — Không Bao Giờ Block Main Thread
- **100% Bất đồng bộ (Fully Async):** Toàn bộ truy vấn Database và quá trình tính toán BCrypt (~250ms) đều chạy trên Thread Pool riêng biệt — giữ **20.0 TPS** ổn định ngay cả khi hàng trăm người chơi đăng nhập cùng lúc.
- **Hồ chứa kết nối (Connection Pool) thông minh:** Tự động tái sử dụng kết nối cho cả **MySQL/MariaDB** và **SQLite**, có hàng đợi chịu tải (timeout 5s) và bộ nhớ đệm `PreparedStatement`.
- **Cơ chế tự điều tiết (Auto-Throttling):** Tự động giảm mật độ hạt (Particle) khi server đông người (>60 người chơi) để bảo vệ FPS cho client yếu.
- **Quản lý bộ nhớ chặt chẽ:** Sử dụng `ConcurrentHashMap` kết hợp tiến trình tự động dọn dẹp cache hết hạn, bản ghi lỗi cũ và huỷ các task BossBar mồ côi khi người chơi thoát.

### 🖥️ Giao Diện (Chest GUI) & Hiệu Ứng Rực Rỡ
- **GUI Đăng nhập 27 ô:** Giao diện trực quan hiển thị số lần thử còn lại, địa chỉ IP, trạng thái 2FA. Hỗ trợ nhấn vào ô để nhập mật khẩu/2FA kín đáo qua khung chat.
- **Hệ thống Welcome 4 cấp độ (`MINIMAL` / `NORMAL` / `EPIC` / `CUSTOM`):**
  - **12 hiệu ứng Particle 3D:** `HEART`, `STAR`, `SPIRAL`, `CIRCLE`, `HELIX`, `FIREWORK`, `RAINBOW`, `WINGS`, `GALAXY`, `HEARTS_RAIN`, `PORTAL` (mới), `AURA` (mới).
  - **Hoạt ảnh chữ động:** Title chạy chữ kiểu máy đánh chữ (*Typewriter*), ActionBar đổi màu cầu vồng và BossBar đếm ngược chuyển sắc (Xanh $\rightarrow$ Vàng $\rightarrow$ Đỏ).
  - **Âm thanh giai điệu:** Hợp âm C-E-G arpeggio, pháo hoa thực thể (*Firework Entity*) và âm báo tuỳ chỉnh hoàn toàn trong `config.yml`.
- **100% Việt hoá & Adventure API:** Hỗ trợ đầy đủ mã màu truyền thống (`&a`, `&l`) lẫn mã màu **Hex 24-bit (`&#RRGGBB`)** và dải màu Gradient.

### 🛡️ Hệ Thống Phong Toả Trước Khi Đăng Nhập
Người chơi chưa xác thực sẽ bị cô lập hoàn toàn và áp dụng trạng thái ẩn/hạn chế đồng bộ:
- ❌ Không thể di chuyển (chỉ cho phép xoay góc nhìn), cảnh báo chống spam (2 giây/lần).
- ❌ Không thể chat, mở túi đồ (Inventory), nhặt/vứt vật phẩm, phá/đặt khối hay tấn công/nhận sát thương.
- ❌ Chặn mọi câu lệnh ngoại trừ: `/login`, `/register`, `/captcha`, `/2fa`, `/blogin`, `/blogingui`.

---

## 🔧 Cập Nhật Phiên Bản 3.0.1

> Bản vá **3.0.1** giải quyết triệt để **13 lỗi biên dịch** từ bản 3.0.0 và khắc phục hoàn toàn các lỗi sập ngầm (Runtime Crash) khi chạy trên nền tảng Minecraft 1.20.5+ / 1.21.4.

### 🎯 Tương thích đa phiên bản (1.20.1 – 1.21.4)
Sử dụng lớp cầu nối mới **`vn.blogin.utils.Compat`** tự động dò tìm và lưu cache hằng số theo phiên bản server lúc chạy (Runtime), đồng thời bắt `LinkageError` để không bao giờ làm sập plugin:

| Hiệu ứng / Hằng số | Server 1.20.1 | Server 1.20.5+ / 1.21+ | Cơ chế xử lý |
| :--- | :---: | :---: | :--- |
| Làm chậm (Slowness) | `SLOW` | `SLOWNESS` | Tra cứu động qua `Compat` |
| Nhảy cao (Jump Boost) | `JUMP` | `JUMP_BOOST` | Tra cứu động qua `Compat` |
| Hạt bụi màu (Redstone) | `REDSTONE` | `DUST` | Tra cứu động qua `Compat` |
| Bytecode chuẩn | Java 17 (Major 61) | Java 21 (Tương thích ngược) | Biên dịch trên baseline Paper 1.20.1 |

<details>
<summary><b>📋 Xem chi tiết danh sách lỗi biên dịch & lỗi rò rỉ bộ nhớ đã sửa trong 3.0.1 (Nhấn để mở rộng)</b></summary>

#### 1. Sửa lỗi biên dịch (Compile Fixes)
| Lỗi gốc ở 3.0.0 | Nguyên nhân | Giải pháp ở 3.0.1 |
| :--- | :--- | :--- |
| `Component.Builder` không tồn tại | Adventure 4.14+ đổi tên API | Chuyển sang `ComponentBuilder` |
| `TextColor.lerp(from, to, ratio)` sai | Adventure đổi thứ tự tham số | Đổi thành `lerp(ratio, a, b)` |
| `PlayerInteractEvent.getClickedInventory()` | Không tồn tại trong API 1.20.1–1.21.4 | Chuyển kiểm soát sang `InventoryClickEvent` |
| `Player.getVersion()` bị gỡ bỏ | API cũ không còn hỗ trợ | Thay bằng `getClientBrandName()` |
| Thiếu `captchaManager.reload()` | Gây lỗi khi gõ `/blogin reload` | Bổ sung phương thức `reload()` |
| Lỗi suy luận kiểu `HashSet<>` | `BossBar#viewers()` trả về `Iterable` | Duyệt thủ công và sao chép sang `List` |

#### 2. Sửa lỗi Logic, Bảo mật & Rò rỉ bộ nhớ (Bug & Security Fixes)
- **Sửa lỗi `/blogin force`:** Gom toàn bộ logic gỡ hiệu ứng mù/chậm và hiển thị lại người chơi vào một điểm duy nhất là `PlayerRestrictionUtil`, khắc phục lỗi admin ép đăng nhập xong người chơi vẫn bị tàng hình và mù.
- **Chống rò rỉ bộ nhớ (Memory Leak):** Tự động xoá dữ liệu cảnh báo trong `PlayerMoveListener` / `PlayerInteractListener` và huỷ ngay lập tức tiến trình chạy BossBar khi người chơi thoát game hoặc tắt server.
- **An toàn khoá 2FA:** Không bao giờ lưu Secret 2FA dạng plaintext nếu quá trình mã hoá lỗi; tự động sinh khoá mới nếu file `secret.key` bị rỗng (0 byte); xoá sạch secret bằng `setNull()` chuẩn JDBC khi người chơi tắt 2FA.
- **Kiểm thử tự động:** Vượt qua **35/35 bài test** trong `SelfTest` (Base32 RFC 4648, TOTP RFC 6238, độ mạnh mật khẩu, AES-GCM) và kiểm chứng bằng `CompatProbe`.

</details>

---

## 📦 Hướng Dẫn Cài Đặt

### 1. Yêu cầu hệ thống
- **Java:** JDK/JRE 17 trở lên (Khuyên dùng Java 21 cho server 1.21.x).
- **Nền tảng:** Paper hoặc Spigot `1.20.1` – `1.21.4` (Khuyên dùng **Paper** để tối ưu toàn bộ tính năng Adventure API).
- **Phụ thuộc (Tuỳ chọn):** `PlaceholderAPI` (để dùng biến hiển thị), `MySQL / MariaDB` (cho mạng lưới server lớn).

### 2. Các bước cài đặt
1. Tải file **`Blogin-3.0.1.jar`** (hoặc tự build từ mã nguồn bằng lệnh `mvn clean package`).
2. Bỏ file `Blogin-3.0.1.jar` vào thư mục `plugins/` của server.
3. **Khởi động lại (Restart) server** để plugin tạo cấu trúc thư mục và khoá bảo mật. *(Lưu ý: Hạn chế dùng `/reload confirm` của Bukkit để tránh xung đột luồng dữ liệu).*
4. Tuỳ chỉnh file `plugins/Blogin/config.yml` theo nhu cầu, sau đó dùng lệnh **`/blogin reload`** trong game để áp dụng ngay lập tức.

> ⚠️ **CẢNH BÁO BẢO MẬT QUAN TRỌNG:**  
> File `plugins/Blogin/secret.key` chứa khoá gốc dùng để mã hoá và giải mã toàn bộ mã 2FA của người chơi. **Tuyệt đối không chia sẻ hoặc xoá file này!** Nếu làm mất file, toàn bộ người chơi đã bật 2FA sẽ phải được Admin reset 2FA mới có thể đăng nhập lại.

<details>
<summary><b>🗄️ Hướng dẫn cấu hình MySQL / MariaDB (Tuỳ chọn cho Server lớn)</b></summary>

Chạy câu lệnh SQL sau để tạo cơ sở dữ liệu với bảng mã chuẩn hỗ trợ tiếng Việt:
```sql
CREATE DATABASE blogin CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'blogin_user'@'%' IDENTIFIED BY 'mat_khau_cua_ban';
GRANT ALL PRIVILEGES ON blogin.* TO 'blogin_user'@'%';
FLUSH PRIVILEGES;
```

Sau đó cập nhật mục `database` trong `plugins/Blogin/config.yml`:
```yaml
database:
  loai: "MYSQL"
  async-threads: 2
  mysql:
    host: "localhost"
    port: 3306
    database: "blogin"
    username: "blogin_user"
    password: "mat_khau_cua_ban"
    pool-size: 10
    use-ssl: false
```
</details>

---

## 🎮 Hướng Dẫn Sử Dụng

### 👤 Lệnh dành cho Người chơi

| Câu lệnh | Lệnh viết tắt (Alias) | Mô tả chức năng |
| :--- | :--- | :--- |
| `/register <mật_khẩu> <nhập_lại>` | `/dangky`, `/reg` | Đăng ký tài khoản mới khi vào server lần đầu |
| `/login <mật_khẩu> [mã_2fa]` | `/dangnhap` | Đăng nhập vào tài khoản *(nếu bật Captcha: `/login <captcha> <mk>`)* |
| `/blogingui` | `/bgui`, `/logingui` | Mở bảng điều khiển đăng nhập dạng Chest GUI |
| `/changepassword <mk_cũ> <mk_mới>` | `/doimatkhau`, `/cpass` | Thay đổi mật khẩu hiện tại |
| `/logout` | `/dangxuat` | Đăng xuất khỏi phiên làm việc hiện tại |
| `/2fa enable` | `/totp`, `/otp` | Nhận mã Secret Key để thiết lập bảo mật 2 lớp |
| `/2fa confirm <mã_6_số>` | — | Xác nhận và kích hoạt 2FA từ ứng dụng Authenticator |
| `/2fa disable <mã_6_số>` | — | Huỷ kích hoạt bảo mật 2 lớp |
| `/2fa status` | — | Kiểm tra trạng thái bật/tắt 2FA của bản thân |
| `/captcha` | `/cap` | Yêu cầu cấp lại mã Captcha mới |

### 👑 Lệnh dành cho Quản trị viên (Quyền: `blogin.admin` — Mặc định: OP)

| Câu lệnh | Mô tả chức năng |
| :--- | :--- |
| `/blogin reload` | Tải lại toàn bộ cấu hình, âm thanh và Captcha không cần tắt server |
| `/blogin info <player>` | Xem hồ sơ chi tiết của người chơi (IP, trạng thái khoá, 2FA, lần đăng nhập cuối) |
| `/blogin force <player>` | Ép đăng nhập trực tiếp cho người chơi đang ở trong server |
| `/blogin reset <player> <mk_mới>` | Đặt lại mật khẩu mới cho người chơi bị quên mật khẩu |
| `/blogin 2fa <player> <on\|off\|secret>` | Bật, tắt hoặc xem mã Secret 2FA của người chơi từ xa |
| `/blogin unlock <player>` | Gỡ khoá ngay lập tức cho tài khoản/IP bị khoá do nhập sai quá số lần |
| `/blogin unregister <player>` | Xoá hoàn toàn tài khoản của người chơi khỏi Database |
| `/blogin list [trang]` | Xem danh sách toàn bộ tài khoản đã đăng ký (có phân trang) |
| `/blogin status` | Xem thống kê hoạt động của plugin, bộ nhớ đệm và kết nối Database |
| `/bloginwelcome <test\|particle\|...>` | Công cụ kiểm thử trực tiếp các hiệu ứng hạt, BossBar và đếm ngược |

---

## ⚙️ Cấu Hình (`config.yml`)

```yaml
# Thời gian chờ đăng nhập tối đa trước khi bị kick (giây)
thoi-gian-dang-nhap: 60

# Cấu hình Cơ sở dữ liệu
database:
  loai: "SQLITE"          # SQLITE (tự tạo file blogin.db) hoặc MYSQL
  async-threads: 2        # Số luồng xử lý bất đồng bộ (tăng lên 4 nếu CPU nhiều nhân)
  mysql:
    host: "localhost"
    port: 3306
    database: "blogin"
    username: "root"
    password: ""
    pool-size: 10

# Thiết lập Bảo mật & Chống Brute-Force
bao-mat:
  so-lan-sai-toi-da: 5
  thoi-gian-khoa: 300          # Thời gian khoá tạm thời (giây)
  che-do-khoa: "BOTH"          # ACCOUNT | IP | BOTH
  bcrypt-cost: 12              # Độ khó BCrypt từ 4-16 (Server yếu: 10, Server mạnh: 12-13)
  chan-mat-khau-yeu: true
  diem-mat-khau-toi-thieu: 2   # Thang điểm 0-5
  chu-ky-don-dep: 300          # Chu kỳ dọn rác bộ nhớ đệm (giây)
  mat-khau-toi-thieu: 6
  mat-khau-toi-da: 64
  yeu-cau-mat-khau:
    chu-hoa: true
    chu-thuong: true
    so: true
    ky-tu-dac-biet: false

# Giao diện Chest GUI
gui:
  bat: true
  auto-mo: true
  tieu-de: "&8✦ &6&lBLOGIN &8✦ &7| &f{player}"

# Xác thực Captcha
captcha:
  bat: true
  do-dai: 5
  chi-chu-va-so: true
  phan-biet-hoa-thuong: false

# Bảo mật 2 lớp (TOTP)
two-factor:
  bat: true
  ten-issuer: "BloginVN"
  bat-buoc-admin: true         # Nhắc nhở Admin bật 2FA sau mỗi lần đăng nhập
  so-chu-so: 6
  thoi-gian-hieu-luc: 30
  do-lech-cho-phep: 1          # Độ lệch múi giờ cho phép (tăng lên 2 nếu hay bị báo sai mã)

# Khoá IP & Tự động đăng nhập
ip-lock:
  bat: false
  tu-dong-dang-nhap: true      # Tự đăng nhập nếu trùng IP cũ (Không áp dụng cho nick bật 2FA)

# Hệ thống Hiệu ứng Chào mừng (Welcome)
welcome:
  bat: true
  muc-do: "EPIC"               # MINIMAL | NORMAL | EPIC | CUSTOM | DISABLED
  nguong-chat-server: 60       # Tự giảm hạt particle khi số người online vượt ngưỡng này
  particle-types: [RAINBOW, STAR, GALAXY, SPIRAL, PORTAL]
  particle-login: "FIREWORK"
  particle-register: "HEARTS_RAIN"
  phao-hoa: true
  so-phao-hoa: 5
  bossbar-timeout: true
  broadcast-join: true
  halo-neon:
    bat: false
    thoi-gian: 100

# Âm thanh sự kiện
am-thanh:
  dang-nhap-thanh-cong:
    bat: true
    am-thanh: "ENTITY_PLAYER_LEVELUP"
    am-luong: 1.0
    pitch: 1.5
  dang-ky-thanh-cong:
    bat: true
    am-thanh: "UI_TOAST_CHALLENGE_COMPLETE"
    am-luong: 1.0
    pitch: 1.0
  sai-mat-khau:
    bat: true
    am-thanh: "ENTITY_VILLAGER_NO"
    am-luong: 1.0
    pitch: 1.0
```

---

## 🔌 Danh Sách Biến PlaceholderAPI

Tích hợp sẵn **13 placeholders** để sử dụng trong các plugin bảng điểm (Scoreboard), Tablist (TAB) hoặc Menu (DeluxeMenus):

| Placeholder | Ý nghĩa | Giá trị trả về mẫu |
| :--- | :--- | :--- |
| `%blogin_authenticated%` | Trạng thái đã đăng nhập hay chưa | `yes` / `no` |
| `%blogin_registered%` | Tài khoản đã đăng ký hay chưa | `yes` / `no` |
| `%blogin_2fa%` | Trạng thái kích hoạt bảo mật 2 lớp | `yes` / `no` |
| `%blogin_ip%` | Địa chỉ IP lúc đăng ký tài khoản | `192.168.1.100` |
| `%blogin_ip_current%` | Địa chỉ IP của phiên kết nối hiện tại | `192.168.1.100` |
| `%blogin_logins%` | Tổng số lần đăng nhập thành công | `42` |
| `%blogin_last_login%` | Mốc thời gian đăng nhập gần nhất (Epoch ms) | `1700000000000` |
| `%blogin_lock_remaining%` | Thời gian bị khoá tài khoản còn lại (giây) | `120` |
| `%blogin_remaining_attempts%` | Số lần nhập sai mật khẩu còn lại trước khi bị khoá | `3` |
| `%blogin_timeout%` | Thời gian đếm ngược chờ đăng nhập còn lại (giây) | `45` |
| `%blogin_total_players%` | Tổng số tài khoản đã đăng ký trên toàn server | `152` |
| `%blogin_totp_enabled_count%` | Tổng số tài khoản đang bật bảo mật 2FA | `38` |
| `%blogin_version%` | Phiên bản hiện tại của plugin Blogin | `3.0.1` |

---

## 🏗️ Kiến Trúc Hệ Thống & Cấu Trúc Mã Nguồn

### 1. Luồng xử lý đăng nhập bất đồng bộ (Async Pipeline)
```text
Người chơi nhập /login <mk> (hoặc qua Chest GUI)
        │
        ▼
[Main Thread] LoginCommand ──► Kiểm tra cú pháp & Captcha (In-memory)
        │
        ▼ (Chuyển giao sang Async Thread Pool)
[Async Worker] AuthManager.loginAsync()
        ├──► 1. Kiểm tra trạng thái khoá (Cache ConcurrentHashMap + AttemptDAO)
        ├──► 2. Xác thực mật khẩu với BCrypt (~250ms, không chạm vào Main Thread)
        ├──► 3. Giải mã AES-GCM & đối chiếu mã TOTP 2FA (nếu tài khoản có bật)
        └──► 4. Cập nhật thông tin phiên đăng nhập vào PlayerDAO (MySQL / SQLite)
        │
        ▼ (Callback trả kết quả về lại Main Thread)
[Main Thread] SessionManager.markAuthenticated()
        └──► PlayerRestrictionUtil gỡ bỏ hiệu ứng hạn chế + kích hoạt Welcome Effect
```

### 2. Cây thư mục dự án (45 Class Files)
```text
Blogin/
├── pom.xml                                  # Cấu hình Maven (Baseline: Paper 1.20.1 / Java 17)
└── src/main/
    ├── java/vn/blogin/
    │   ├── Blogin.java                      # Lớp khởi tạo chính (Singleton)
    │   ├── commands/                        # 9 bộ xử lý câu lệnh
    │   │   ├── LoginCommand.java
    │   │   ├── RegisterCommand.java
    │   │   ├── LogoutCommand.java
    │   │   ├── ChangePasswordCommand.java
    │   │   ├── BloginAdminCommand.java
    │   │   ├── CaptchaCommand.java
    │   │   ├── TwoFactorCommand.java
    │   │   ├── LoginGuiCommand.java
    │   │   └── WelcomeAdminCommand.java
    │   ├── database/                        # Tầng truy xuất dữ liệu (DAO & Pool)
    │   │   ├── DatabaseManager.java
    │   │   ├── ConnectionPool.java          # Quản lý kết nối bằng JDBC Dynamic Proxy
    │   │   ├── PlayerDAO.java
    │   │   └── AttemptDAO.java
    │   ├── managers/                        # Tầng xử lý nghiệp vụ cốt lõi
    │   │   ├── AuthManager.java
    │   │   ├── SessionManager.java
    │   │   ├── CaptchaManager.java
    │   │   └── MessageManager.java
    │   ├── gui/
    │   │   └── LoginGui.java                # Giao diện đăng nhập Chest GUI 27 ô
    │   ├── welcome/                         # Hệ thống hiệu ứng nghe nhìn
    │   │   ├── WelcomeManager.java
    │   │   ├── WelcomeParticleEffect.java   # Toán học toạ độ cho 12 hiệu ứng hạt 3D
    │   │   ├── WelcomeTitle.java
    │   │   ├── WelcomeSound.java
    │   │   └── WelcomeBossBar.java          # Quản lý BossBar theo UUID chống rò rỉ task
    │   ├── listeners/                       # 12 bộ lắng nghe sự kiện bảo vệ
    │   │   ├── PlayerJoinListener.java
    │   │   ├── PlayerJoinMessageListener.java
    │   │   ├── PlayerLoginListener.java
    │   │   ├── PlayerQuitListener.java
    │   │   ├── PlayerChatListener.java
    │   │   ├── PlayerMoveListener.java
    │   │   ├── PlayerBlockBreakListener.java
    │   │   ├── PlayerBlockPlaceListener.java
    │   │   ├── PlayerDamageListener.java
    │   │   ├── PlayerCommandListener.java
    │   │   ├── PlayerInteractListener.java
    │   │   └── AuthenticatedListener.java
    │   ├── placeholder/
    │   │   └── BloginPlaceholder.java       # Tích hợp PlaceholderAPI
    │   └── utils/                           # Các tiện ích cốt lõi & Bảo mật
    │       ├── Compat.java                  # [Mới 3.0.1] Tương thích động đa phiên bản
    │       ├── PlayerRestrictionUtil.java   # [Mới 3.0.1] Quản lý tập trung trạng thái hạn chế
    │       ├── ChatUtil.java                # Xử lý Adventure API, mã Hex & Gradient
    │       ├── EncryptionUtil.java          # Mã hoá AES-GCM cho khoá 2FA
    │       ├── HashingUtil.java             # Băm và kiểm tra re-hash BCrypt
    │       ├── PasswordStrength.java        # Thuật toán chấm điểm độ mạnh mật khẩu
    │       ├── TOTPGenerator.java           # Thuật toán sinh mã 2FA chuẩn RFC 6238
    │       └── SelfTest.java                # Bộ 35 bài kiểm thử tự động độc lập
    └── resources/
        ├── plugin.yml
        └── config.yml
```

---

## 🛠️ Xử Lý Sự Cố Thường Gặp (Troubleshooting)

* **Lỗi `✖ Không thể khởi tạo khoá mã hoá!` khi bật server:**  
  Do file `plugins/Blogin/secret.key` bị hỏng định dạng. Hãy xoá file này để plugin tự tạo khoá mới *(Lưu ý: Các tài khoản đã bật 2FA trước đó sẽ cần Admin gõ `/blogin 2fa <player> off` để đặt lại)*.
* **Nhập mã 2FA trên điện thoại liên tục báo sai:**  
  Thuật toán TOTP phụ thuộc vào đồng hồ hệ thống. Hãy đồng bộ lại giờ chuẩn (NTP) trên máy chủ vật lý/VPS của bạn, hoặc tăng mục `two-factor.do-lech-cho-phep` trong `config.yml` lên `2` hoặc `3`.
* **Server có hiện tượng trễ khi hàng loạt người chơi đăng nhập cùng lúc:**  
  Hãy giảm `bao-mat.bcrypt-cost` từ `12` xuống `10` (giảm thời gian tính toán mỗi mật khẩu từ ~250ms xuống ~65ms) và tăng `database.async-threads` lên `4` nếu VPS có từ 4 nhân CPU trở lên.

---

## 🤝 Đóng Góp & Bản Quyền

Dự án được phát triển bởi **BloginVN** với ❤️ dành cho cộng đồng Minecraft Việt Nam.
* **Báo lỗi & Đóng góp mã nguồn:** [github.com/blogin-vn/blogin](https://github.com/blogin-vn/blogin)
* **Giấy phép:** Phân phối dưới giấy phép **MIT License** — Tự do sử dụng, chỉnh sửa cho cả mục đích cá nhân và thương mại.
* **Thư viện & Tiêu chuẩn sử dụng:** [jBCrypt](https://github.com/jeremyh/jBCrypt) • [RFC 6238 (TOTP)](https://tools.ietf.org/html/rfc6238) • Java Cryptography Architecture (AES-GCM) • [PaperMC API](https://papermc.io/) • [PlaceholderAPI](https://github.com/PlaceholderAPI/PlaceholderAPI)
