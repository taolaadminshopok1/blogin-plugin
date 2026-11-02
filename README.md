Source URL: https://github.com/taolaadminshopok1/blogin-plugin
Title: GitHub - taolaadminshopok1/blogin-plugin: A secure and lightweight Minecraft authentication plugin providing fast /login and /register commands, password hashing, and session protection for offline-mode servers. · GitHub

Skip to content 

## Navigation Menu

Sign in

* Products  
   * Platform  
         * GitHubRepository hosting, code review, and collaboration  
         * CopilotAI coding agent for tasks, projects and automated work  
   * Build & Ship  
         * ActionsCI/CD and automation  
         * Code QualityQuality analysis, fixes, and enforcement  
         * CodespacesCloud development environments  
   * Secure  
         * Code SecurityVulnerability detection and remediation  
         * Secret ProtectionSecret scanning and leak prevention  
All Features
* Developers  
   * Learn  
         * Documentation  
         * Training  
         * Certifications  
   * Build  
         * Build with Copilot  
         * Customize Copilot  
         * Integrations  
   * Explore  
         * Trending repos  
         * GitHub Sponsors  
         * Events  
   * Changelog  
         * ChangelogYour source for the latest features, improvements, and fixes across GitHub products, all in one place.
* Businesses  
   * By Stage  
         * Enterprises  
         * Team  
         * Startups  
         * Nonprofits  
   * By Industry  
         * Healthcare  
         * Financial services  
         * Manufacturing  
         * Government  
         * See all industries  
   * Evaluate GitHub  
         * Why GitHub  
         * Customer Stories  
         * Business insights  
         * Trust Center  
   * Add-ons  
         * GitHub Advanced SecurityEnterprise-grade code security  
         * Copilot for businessEnterprise-grade AI features  
         * Premium SupportEnterprise-grade 24/7 support
* Resources  
   * Learn & Connect  
         * Documentation  
         * Resource center  
         * Blog  
         * Events & webinars  
         * Community Forum  
         * Customer Stories  
         * Integrations  
   * Support  
         * Customer support  
         * Training  
         * GitHub Status  
   * Company  
         * About  
         * Partners  
         * Changelog  
   * GitHub Universe  
         * GitHub UniverseThe event for developers, by developers — talks, workshops, and what’s next for GitHub.
* Pricing

Search/

Sign in

Sign up

Appearance settings

You signed in with another tab or window. Reload to refresh your session. You signed out in another tab or window. Reload to refresh your session. You switched accounts on another tab or window. Reload to refresh your session. Dismiss alert 

{{ message }}

1. taolaadminshopok1
2. blogin-plugin

## Repository navigation

* Code
* Issues
* Pull requests
* Actions
* Projects
* Security and quality
* Insights

More items

taolaadminshopok1

**blogin-plugin**

Public

* NotificationsYou must be signed in to change notification settings
* Fork0 (0)
* Star1 (1)You must be signed in to star a repository

## About

A secure and lightweight Minecraft authentication plugin providing fast /login and /register commands, password hashing, and session protection for offline-mode servers.

### Resources

Readme

MIT license

Activity

### Stars

**1** star

### Watchers

**0** watching

### Forks

**0** forks

Report repository

main

BranchesTags

Go to file

Code

Open more actions menu

## Latest commit

## History

3 Commits

3 Commits

## Folders and files

| Name                                                                                                        | Name                                                                                                        | Last commit message | Last commit date |
| ----------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------- | ------------------- | ---------------- |
| [src/main](/taolaadminshopok1/blogin-plugin/tree/main/src/main "This path skips through empty directories") | [src/main](/taolaadminshopok1/blogin-plugin/tree/main/src/main "This path skips through empty directories") |                     |                  |
| [.gitignore](/taolaadminshopok1/blogin-plugin/blob/main/.gitignore ".gitignore")                            | [.gitignore](/taolaadminshopok1/blogin-plugin/blob/main/.gitignore ".gitignore")                            |                     |                  |
| [LICENSE](/taolaadminshopok1/blogin-plugin/blob/main/LICENSE "LICENSE")                                     | [LICENSE](/taolaadminshopok1/blogin-plugin/blob/main/LICENSE "LICENSE")                                     |                     |                  |
| [README.md](/taolaadminshopok1/blogin-plugin/blob/main/README.md "README.md")                               | [README.md](/taolaadminshopok1/blogin-plugin/blob/main/README.md "README.md")                               |                     |                  |
| [pom.xml](/taolaadminshopok1/blogin-plugin/blob/main/pom.xml "pom.xml")                                     | [pom.xml](/taolaadminshopok1/blogin-plugin/blob/main/pom.xml "pom.xml")                                     |                     |                  |
| View all files                                                                                              |                                                                                                             |                     |                  |

## Repository files navigation

* README
* MIT license

More items

# 🛡️ Blogin - Plugin Login Xịn Xò Cho Minecraft

> **Phiên bản:** 2.0.0**Hỗ trợ:** Minecraft 1.20.1 - 1.21.4 (Paper / Spigot)**Java:** 17+**Việt hóa:** 100%**Bản quyền:** BloginVN

Plugin đăng nhập/đăng ký **xịn xò nhất** cho server Minecraft Việt Nam, tích hợp **bảo mật cao cấp**, **2FA**, **Captcha chống bot**, chống **brute-force**, hỗ trợ **MySQL** và **SQLite**.

---

## 🎯 Tính năng nổi bật

### 🔐 Bảo mật đỉnh cao

* ✅ **BCrypt** hashing với cost factor 12 + salt ngẫu nhiên (chuẩn công nghiệp)
* ✅ **2FA (TOTP)** chuẩn RFC 6238 - tương thích Google Authenticator, Authy, Microsoft Authenticator
* ✅ **Chống brute-force**: khóa tài khoản/IP theo cấu hình (ACCOUNT / IP / BOTH)
* ✅ **Captcha** chống bot đăng ký hàng loạt
* ✅ **Chống timing attack** trong xác thực TOTP
* ✅ Session in-memory không lưu password plaintext

### 🌐 Database linh hoạt

* ✅ **SQLite** \- cài đặt 0 config, file `blogin.db` tự tạo
* ✅ **MySQL / MariaDB** \- hỗ trợ connection pool, production-ready
* ✅ Tự động tạo bảng + index khi khởi động

### 🇻🇳 Việt hóa 100%

* ✅ Toàn bộ thông báo trong `config.yml` đã Việt hóa
* ✅ Dễ tùy chỉnh - chỉ cần sửa YAML
* ✅ Hỗ trợ mã màu Minecraft `&` \+ placeholder `{player}`, `{time}`, `{remaining}`
* ✅ Tùy chọn chuyển sang tiếng Anh

### 🎨 Trải nghiệm người chơi

* ✅ Hiệu ứng hạn chế (Blindness, Slow, Jump Boost) khi chưa đăng nhập
* ✅ Ẩn người chơi chưa login khỏi tab & thế giới
* ✅ Đếm ngược timeout đăng nhập (mặc định 60 giây)
* ✅ Âm thanh thành công/thất bại
* ✅ Captcha hiển thị ngay khi vào server
* ✅ Lệnh `/captcha` để lấy captcha mới nếu quên

### 🛡️ Khóa hành động

Người chưa đăng nhập **không thể**:

* ❌ Chat trong server
* ❌ Di chuyển (chỉ xoay góc nhìn)
* ❌ Phá / đặt khối
* ❌ Tấn công / bị tấn công
* ❌ Mở inventory, tương tác thực thể
* ❌ Nhặt / thả vật phẩm
* ❌ Dùng lệnh khác (trừ `/login`, `/register`, `/captcha`, `/2fa`, `/blogin`)

### 🔌 Tích hợp

* ✅ **PlaceholderAPI** \- 7 placeholder có sẵn
* ✅ Paper API hiện đại (1.20.1 - 1.21.4)
* ✅ Maven build - shade dependencies

### 🎆 Hệ thống Welcome & Hiệu ứng rực rỡ (MỚI!)

* ✅ **10 loại particle** đẹp lung linh: Trái tim, Ngôi sao, Galaxy, Cầu vồng, Đôi cánh thiên thần, Mưa trái tim...
* ✅ **Title animation typewriter** đổi màu liên tục
* ✅ **ActionBar rainbow/pulse/ticker** chạy chữ động
* ✅ **BossBar gradient** đổi màu cầu vồng, pulse, progress fill
* ✅ **Pháo hoa thật (entity Firework)** bắn quanh người chơi
* ✅ **Hệ thống âm thanh** giai điệu (C-E-G arpeggio, pháo hoa, chuông thành công)
* ✅ **3 mức độ**: MINIMAL / NORMAL / EPIC (tắt cũng được)
* ✅ **Tin nhắn broadcast** cho mọi người khi có thành viên mới
* ✅ **Lệnh admin `/bloginwelcome`** để test từng hiệu ứng
* ✅ **Countdown 3-2-1-GO** cho sự kiện
* ✅ **Particle galaxy xoắn ốc 3D** tuyệt đẹp
* ✅ **Cánh thiên thần** với animation flap theo nhịp

---

## 📦 Cài đặt

### Yêu cầu

* **Java 17** trở lên
* **Paper** hoặc **Spigot 1.20.1 - 1.21.4**
* (Tùy chọn) **PlaceholderAPI** nếu muốn dùng placeholder
* (Tùy chọn) **MySQL/MariaDB** nếu muốn dùng database server

### Bước 1: Build plugin (từ mã nguồn)

cd Blogin
mvn clean package

File JAR sẽ được tạo tại: `target/Blogin-2.0.0.jar`

### Bước 2: Cài lên server

1. Copy `Blogin-2.0.0.jar` vào thư mục `plugins/` của server
2. Khởi động server (hoặc dùng `/reload confirm`)
3. Sửa file `plugins/Blogin/config.yml` theo ý bạn
4. Dùng lệnh `/blogin reload` để tải lại config

### Bước 3 (Tùy chọn): Cài MySQL

CREATE DATABASE blogin CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'blogin_user'@'%' IDENTIFIED BY 'mat_khau_cua_ban';
GRANT ALL PRIVILEGES ON blogin.* TO 'blogin_user'@'%';
FLUSH PRIVILEGES;

Sau đó sửa trong `config.yml`:

database:
  loai: "MYSQL"
  mysql:
    host: "localhost"
    port: 3306
    database: "blogin"
    username: "blogin_user"
    password: "mat_khau_cua_ban"
    pool-size: 10
    use-ssl: false

---

## 🎮 Hướng dẫn sử dụng

### 👤 Cho người chơi

| Lệnh                               | Alias                          | Mô tả                 |
| ---------------------------------- | ------------------------------ | --------------------- |
| /login <mk> \[2fa\]                | /dangnhap                      | Đăng nhập             |
| /register <mk> <xác\_nhận>         | /dangky, /reg                  | Đăng ký tài khoản     |
| /logout                            | /dangxuat                      | Đăng xuất             |
| /changepassword <cũ> <mới>         | /doimatkhau, /cpass            | Đổi mật khẩu          |
| /2fa enable                        | /totp, /otp                    | Bật xác thực 2 yếu tố |
| /2fa confirm <code>                | Xác nhận bật 2FA               |                       |
| /2fa disable <code>                | Tắt 2FA                        |                       |
| /captcha                           | /cap                           | Lấy captcha mới       |
| \`/bloginwelcome test <new         | back                           | login                 |
| /bloginwelcome particle <loại>     | Test 1 loại particle (admin)   |                       |
| /bloginwelcome title               | Test title + actionbar (admin) |                       |
| \`/bloginwelcome bossbar <gradient | pulse                          | progress              |
| /bloginwelcome countdown <giây>    | Đếm ngược 3-2-1-GO (admin)     |                       |
| /bloginwelcome all                 | Test tất cả hiệu ứng (admin)   |                       |

#### Ví dụ cụ thể:

**Đăng ký** (lần đầu vào server):

```
/register Matkhau123 Matkhau123

```

**Đăng nhập** (những lần sau):

```
/login Matkhau123

```

**Đăng nhập có 2FA**:

```
/login Matkhau123 482915

```

**Khi có captcha**: Cú pháp: `/login <captcha> <mật_khẩu>`

```
/login aB3x9 Matkhau123

```

**Bật 2FA**:

1. `/2fa enable` → nhận secret key
2. Mở Google Authenticator → nhập secret
3. `/2fa confirm 123456` (mã hiện tại)

---

### 👑 Cho Admin

| Lệnh                        | Mô tả                                            |
| --------------------------- | ------------------------------------------------ |
| /blogin reload              | Tải lại config mà không cần restart              |
| /blogin unregister <player> | Xóa tài khoản người chơi                         |
| /blogin info <player>       | Xem thông tin chi tiết                           |
| /blogin force <player>      | Ép buộc đăng nhập (dành cho người quên mật khẩu) |
| /blogin unlock <player>     | Mở khóa tài khoản bị khóa do brute-force         |
| /blogin status              | Xem trạng thái plugin                            |
| /blogin help                | Hiển thị trợ giúp                                |

**Quyền admin:** `blogin.admin` (mặc định chỉ OP)

---

## ⚙️ Cấu hình (config.yml)

### File cấu hình có cấu trúc:

# Ngôn ngữ (vi/en)
ngon-ngu: "vi"

# Timeout đăng nhập (giây)
thoi-gian-dang-nhap: 60

# Database
database:
  loai: "SQLITE"  # hoặc MYSQL
  mysql:
    host: "localhost"
    port: 3306
    database: "blogin"
    username: "root"
    password: ""
    pool-size: 10

# Bảo mật
bao-mat:
  so-lan-sai-toi-da: 5
  thoi-gian-khoa: 300
  che-do-khoa: "BOTH"  # ACCOUNT | IP | BOTH
  mat-khau-toi-thieu: 6
  mat-khau-toi-da: 64
  yeu-cau-mat-khau:
    chu-hoa: true
    chu-thuong: true
    so: true
    ky-tu-dac-biet: false

# Captcha
captcha:
  bat: true
  do-dai: 5
  chi-chu-va-so: true
  phan-biet-hoa-thuong: false

# 2FA
two-factor:
  bat: true
  ten-issuer: "BloginVN"
  bat-buoc-admin: true
  so-chu-so: 6
  thoi-gian-hieu-luc: 30
  do-lech-cho-phep: 1

### Tùy chỉnh tin nhắn

Tất cả tin nhắn đều nằm trong `config.yml` dưới mục `tin-nhan:`. Bạn có thể sửa trực tiếp và dùng:

* Mã màu Minecraft: `&a` (xanh lá), `&c` (đỏ), `&e` (vàng), `&b` (xanh dương), `&6` (cam), `&l` (in đậm), `&o` (in nghiêng), `&n` (gạch chân)
* Hex color: `&#FF5733`
* Placeholder: `{player}`, `{time}`, `{remaining}`, `{count}`, `{min}`, `{max}`, `{captcha}`, `{ip}`, `{status}`, `{uuid}`

---

## 🔌 PlaceholderAPI

Nếu cài **PlaceholderAPI**, các placeholder sau có sẵn:

| Placeholder               | Mô tả                         | Ví dụ         |
| ------------------------- | ----------------------------- | ------------- |
| %blogin\_authenticated%   | Đã đăng nhập chưa             | yes / no      |
| %blogin\_registered%      | Đã đăng ký chưa               | yes / no      |
| %blogin\_2fa%             | Có bật 2FA không              | yes / no      |
| %blogin\_ip%              | IP đã đăng ký                 | 192.168.1.100 |
| %blogin\_logins%          | Tổng số lần đăng nhập         | 42            |
| %blogin\_last\_login%     | Thời gian đăng nhập cuối (ms) | 1700000000000 |
| %blogin\_lock\_remaining% | Thời gian khóa còn lại (giây) | 120           |

Dùng trong các plugin như TAB, Scoreboard, DeluxeMenus...

---

## 🎆 Hệ thống Welcome & Hiệu ứng rực rỡ

### Cấu hình Welcome trong config.yml

welcome:
  bat: true
  muc-do: "EPIC"   # MINIMAL | NORMAL | EPIC | DISABLED
  particle-types: [RAINBOW, STAR, GALAXY, SPIRAL]
  particle-login: "FIREWORK"
  particle-register: "HEARTS_RAIN"
  phao-hoa: true
  so-phao-hoa: 5
  bossbar-timeout: true
  broadcast-join: true

### 10 loại particle có sẵn

| Hiệu ứng     | Mô tả                         |
| ------------ | ----------------------------- |
| HEART        | Trái tim particle bắn ra      |
| STAR         | Ngôi sao 5 cánh xoay          |
| SPIRAL       | Xoáy tròn 3D spiral           |
| CIRCLE       | Vòng tròn xoay quanh          |
| HELIX        | Chuỗi xoắn ốc DNA (2 sợi)     |
| FIREWORK     | Pháo hoa mini                 |
| RAINBOW      | Cầu vồng gradient màu (3 lớp) |
| WINGS        | Đôi cánh thiên thần flap      |
| GALAXY       | Xoáy galaxy 3D (4 arms)       |
| HEARTS\_RAIN | Mưa trái tim từ trên xuống    |

### 3 mức độ Welcome

| Mức độ      | Bao gồm                                  |
| ----------- | ---------------------------------------- |
| **MINIMAL** | Title + âm thanh ngắn                    |
| **NORMAL**  | Title + Particle (random) + Sound        |
| **EPIC**    | Tất cả + BossBar + Fireworks + Broadcast |

### Test hiệu ứng (admin)

```
/bloginwelcome test new       # Test welcome người chơi mới
/bloginwelcome test back      # Test welcome người chơi cũ
/bloginwelcome test login     # Test hiệu ứng đăng nhập thành công
/bloginwelcome test register  # Test hiệu ứng đăng ký
/bloginwelcome particle GALAXY  # Test riêng 1 particle
/bloginwelcome bossbar gradient # Test bossbar gradient
/bloginwelcome countdown 5    # Đếm ngược 5 giây
/bloginwelcome all            # Test tất cả

```

---

## 🏗️ Cấu trúc dự án

```
Blogin/
├── pom.xml                          # Maven build config
├── src/main/
│   ├── java/vn/blogin/
│   │   ├── Blogin.java              # Main class
│   │   ├── commands/                # 8 command executors
│   │   │   ├── LoginCommand.java
│   │   │   ├── RegisterCommand.java
│   │   │   ├── LogoutCommand.java
│   │   │   ├── ChangePasswordCommand.java
│   │   │   ├── BloginAdminCommand.java
│   │   │   ├── CaptchaCommand.java
│   │   │   ├── TwoFactorCommand.java
│   │   │   └── WelcomeAdminCommand.java  # Test hiệu ứng welcome
│   │   ├── database/                # Tầng database
│   │   │   ├── DatabaseManager.java
│   │   │   ├── ConnectionPool.java
│   │   │   ├── PlayerDAO.java
│   │   │   └── AttemptDAO.java
│   │   ├── managers/                # Business logic
│   │   │   ├── AuthManager.java
│   │   │   ├── SessionManager.java
│   │   │   ├── CaptchaManager.java
│   │   │   └── MessageManager.java
│   │   ├── welcome/                 # Hệ thống Welcome rực rỡ
│   │   │   ├── WelcomeManager.java
│   │   │   ├── WelcomeParticleEffect.java  # 10 loại particle
│   │   │   ├── WelcomeTitle.java           # Title + ActionBar animated
│   │   │   ├── WelcomeSound.java           # Chuỗi âm thanh
│   │   │   └── WelcomeBossBar.java         # BossBar gradient
│   │   ├── listeners/               # 10 event listeners
│   │   │   ├── PlayerJoinListener.java
│   │   │   ├── PlayerQuitListener.java
│   │   │   ├── PlayerChatListener.java
│   │   │   ├── PlayerMoveListener.java
│   │   │   ├── PlayerBlockBreakListener.java
│   │   │   ├── PlayerBlockPlaceListener.java
│   │   │   ├── PlayerDamageListener.java
│   │   │   ├── PlayerCommandListener.java
│   │   │   ├── PlayerInteractListener.java
│   │   │   └── AuthenticatedListener.java
│   │   ├── placeholder/
│   │   │   └── BloginPlaceholder.java
│   │   └── utils/
│   │       ├── HashingUtil.java     # BCrypt
│   │       └── TOTPGenerator.java   # RFC 6238 2FA
│   └── resources/
│       ├── plugin.yml
│       └── config.yml               # Cấu hình Việt hóa + Welcome

```

---

## 🧪 Kiến trúc & Design Patterns

Plugin được thiết kế theo các nguyên tắc **SOLID** và sử dụng các **Design Patterns** phổ biến:

| Pattern                      | Áp dụng tại                                 |
| ---------------------------- | ------------------------------------------- |
| **Singleton**                | Blogin.getInstance()                        |
| **DAO (Data Access Object)** | PlayerDAO, AttemptDAO                       |
| **Manager / Service**        | AuthManager, SessionManager, CaptchaManager |
| **Strategy**                 | DatabaseManager (SQLite/MySQL)              |
| **Static Factory**           | HashingUtil, TOTPGenerator                  |
| **DTO (Record)**             | PlayerData                                  |
| **Modular Config**           | MessageManager (đọc từ config)              |

### Luồng xử lý đăng nhập

```
Player gửi /login <mk>
        ↓
LoginCommand → validate input
        ↓
AuthManager.login() → kiểm tra khóa, verify password, check 2FA
        ↓              ↓                    ↓
    AttemptDAO    PlayerDAO        TOTPGenerator
        ↓
Trả về AuthResult → SessionManager → Player nhận phản hồi

```

---

## 🐛 Troubleshooting

### Plugin không khởi động được

**Lỗi:** `✖ Không thể kết nối Database!`

* **MySQL:** Kiểm tra thông tin host/port/user/pass
* **SQLite:** Đảm bảo plugin có quyền ghi file vào thư mục `plugins/Blogin/`

### Captcha không hiện

* Kiểm tra `captcha.bat: true` trong config
* Dùng `/captcha` để lấy captcha mới

### 2FA không hoạt động

* Đảm bảo thời gian hệ thống chính xác (TOTP dựa vào thời gian)
* Tăng `do-lech-cho-phep` lên 2-3 nếu thường xuyên lỗi
* Ứng dụng Google Authenticator cần thêm tài khoản với secret key

### Quên mật khẩu admin

Dùng lệnh sau trong console (không phải trong game):

```
blogin adminunregister <player>

```

Hoặc vào MySQL:

DELETE FROM blogin_players WHERE name = 'tên_player';

---

## 📝 Logs & Debug

Bật chế độ debug trong config:

debug: true
log-su-kien: true

Log sẽ ghi ra console:

```
[Đăng ký] NewPlayer (uuid-here) từ IP 192.168.1.10
[Đăng nhập] NewPlayer (uuid-here) từ IP 192.168.1.10

```

---

## 🤝 Đóng góp

Plugin được tạo bởi **BloginVN** với ❤️ cho cộng đồng Minecraft Việt Nam.

Mọi đóng góp/báo lỗi xin gửi về:

* GitHub: github.com/blogin-vn/blogin

---

## 📜 License

MIT License - Sử dụng tự do cho mục đích cá nhân và thương mại.

---

## 🎉 Credits

* **BCrypt** \- jBCrypt
* **TOTP** \- Triển khai theo RFC 6238
* **Paper API** \- PaperMC
* **PlaceholderAPI** \- PlaceholderAPI

## About

A secure and lightweight Minecraft authentication plugin providing fast /login and /register commands, password hashing, and session protection for offline-mode servers.

### Resources

Readme

MIT license

Activity

### Stars

**1** star

### Watchers

**0** watching

### Forks

**0** forks

Report repository

## Releases

## Packages

## Contributors

## Languages

## Footer

 © 2026 GitHub, Inc. 

### Footer navigation

* Terms
* Privacy
* Security
* Status
* Community
* Docs
* Contact
* Manage cookies
* Do not share my personal information

 You can’t perform that action at this time.
