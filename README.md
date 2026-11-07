# 🛡️ Blogin - Plugin Login Xịn Xò Cho Minecraft

> **Phiên bản:** 3.2.0
> **Hỗ trợ:** Minecraft 1.20.1 - 1.21.11 và các bản Paper 26.x (Paper / Spigot)
> **Java:** 17+ (bản build cho 26.x cần JDK 25)
> **Đa ngôn ngữ:** 10 ngôn ngữ — 🇻🇳 Tiếng Việt · 🇬🇧 English · 🇨🇳 简体中文 · 🇪🇸 Español · 🇫🇷 Français · 🇩🇪 Deutsch · 🇯🇵 日本語 · 🇰🇷 한국어 · 🇷🇺 Русский · 🇵🇹 Português
> **Bản quyền:** BloginVN

Plugin đăng nhập/đăng ký **xịn xò nhất** cho server Minecraft, tích hợp **GUI đăng nhập đồ hoạ**, **bảo mật cao cấp**, **2FA mã hoá**, **Captcha chống bot**, chống **brute-force**, hỗ trợ **MySQL** và **SQLite**, và **đa ngôn ngữ tự động** — tối ưu hiệu năng cho server lớn.

---

## 🌍 Bản vá 3.2.0 — Hỗ trợ đa ngôn ngữ (i18n)

### 🎯 Đa ngôn ngữ — 10 ngôn ngữ đầy đủ

| Mã | Ngôn ngữ | Mã | Ngôn ngữ |
|----|----------|----|----------|
| `vi` | 🇻🇳 Tiếng Việt | `ja` | 🇯🇵 日本語 |
| `en` | 🇬🇧 English | `ko` | 🇰🇷 한국어 |
| `zh` | 🇨🇳 简体中文 | `ru` | 🇷🇺 Русский |
| `es` | 🇪🇸 Español | `pt` | 🇵🇹 Português |
| `fr` | 🇫🇷 Français | `de` | 🇩🇪 Deutsch |

> Mỗi ngôn ngữ có **217 khoá thông điệp** — bao gồm chat, GUI (tên + lore item), title, actionbar, bossbar, thông báo welcome, thông báo admin, thang độ mạnh mật khẩu.

### 🔄 Cách xác định ngôn ngữ (thứ tự ưu tiên)

```
1. Người chơi tự chọn   →  /blogin language en   (ghi nhớ theo UUID)
2. Ngôn ngữ client      →  tự động đọc Player#getLocale()  (vi_vn → vi, en_us → en)
3. Ngôn ngữ server      →  config.yml: ngon-ngu: "vi"
4. Tệp ngôn ngữ khác    →  fallback về ngôn ngữ mặc định
5. Chuỗi mặc định       →  giá trị hard-code trong mã nguồn
```

✅ Người chơi nước ngoài **vào server là thấy đúng ngôn ngữ của họ** — không cần cấu hình gì.
✅ Đổi ngôn ngữ bằng 1 lệnh → **GUI và chat cập nhật tức thì**.

### 📁 Cấu trúc tệp ngôn ngữ

```
plugins/Blogin/lang/
├── vi.yml    ← Tiếng Việt (mặc định)
├── en.yml    ← English
├── zh.yml    ← 简体中文
├── es.yml    ← Español
├── fr.yml    ← Français
├── de.yml    ← Deutsch
├── ja.yml    ← 日本語
├── ko.yml    ← 한국어
├── ru.yml    ← Русский
└── pt.yml    ← Português
```

- Tệp được **giải nén tự động** từ JAR ở lần chạy đầu tiên.
- Tệp của bạn **không bị ghi đè** khi cập nhật plugin → có thể tuỳ chỉnh thoải mái.
- Thêm ngôn ngữ mới: chỉ cần tạo `<mã>.yml` trong thư mục `lang/` → plugin tự load.

### 🛠️ Lệnh ngôn ngữ

| Lệnh | Alias | Mô tả |
|------|-------|-------|
| `/blogin language <mã>` | `/lang`, `/ngonngu` | Đổi ngôn ngữ của bạn |
| `/blogin language` | | Xem danh sách ngôn ngữ + ngôn ngữ hiện tại |
| `/blogin reload` | | Tải lại toàn bộ tệp ngôn ngữ |

### 🎨 Hỗ trợ trong GUI

Toàn bộ GUI đăng nhập cũng được dịch: tiêu đề panel, tên item, lore, thanh độ mạnh mật khẩu, prompt nhập chat, nhắc nhở hết thời gian — tất cả theo ngôn ngữ của người chơi đang xem.

### 🧩 Kiến trúc

| Lớp | Vai trò |
|-----|---------|
| `LanguageManager` | Nạp/ghi tệp ngôn ngữ, dò locale client, lưu lựa chọn của người chơi |
| `MessageManager` | Tra cứu thông điệp theo người nhận, thay placeholder, dịch màu `&`/hex |

Cả hai lớp đều hỗ trợ **fallback nhiều lớp** → không bao giờ hiện thông điệp rỗng.

---

## 🚀 Bản vá 3.1.0 — Nâng cấp giao diện & hỗ trợ 1.21.11 / 26.x

### 🎯 Yêu cầu chính đã đáp ứng

| Yêu cầu | Kết quả |
|---------|---------|
| Hỗ trợ 1.21.11 hoặc cao hơn | ✅ Đã build & kiểm chứng trên API `1.21.11-R0.1` **và** `26.1.2` (bản Paper mới nhất) |
| Nâng cấp giao diện đẹp hơn | ✅ GUI đăng nhập được thiết kế lại hoàn toàn (xem bên dưới) |
| Fix bug | ✅ Sửa **9 lỗi thật** — trong đó có 2 lỗi bảo mật nghiêm trọng (xem bên dưới) |

### 🐛 Bug đã sửa

#### 🔴 Lỗi bảo mật nghiêm trọng

| # | Lỗi | Mức độ | Nguyên nhân | Sửa |
|---|-----|---------|-------------|-----|
| 1 | **Captcha bị bỏ qua hoàn toàn khi dùng GUI** | 🔴 Cao | `LoginGui.attemptLogin()` gọi thẳng `loginAsync()` mà **không kiểm tra captcha** — trong khi lệnh `/login` thì có. Người chơi chỉ cần dùng GUI là vượt qua được captcha. | GUI giờ bắt buộc nhập captcha, có ô captcha riêng + luồng nhập trong chat |
| 2 | **Mật khẩu nằm trong RAM sau khi đăng nhập thành công** | 🟡 Trung bình | `markAuthenticated()` không xoá `guiPassword`/`guiTotp` | Xoá plaintext sau khi đăng nhập thành công, và sau khi đăng nhập sai |

#### 🔴 Lỗi hiển thị / hiệu năng

| # | Lỗi | Nguyên nhân | Sửa |
|---|-----|-------------|-----|
| 3 | **Hiệu ứng particle "chạy" nhưng không thấy gì** | `player.spawnParticle(particle, loc, **0**, 0,0,0, 0, dust)` — với `count = 0`, Bukkit **không spawn particle nào cả**. Toàn bộ hiệu ứng welcome (tim, sao, cầu vồng, hào quang…) vô hình! | Đổi `count = 0` → `count = 1` |
| 4 | **Query database trên main thread mỗi lần mở GUI** | `getRemainingAttempts()` gọi `COUNT(*)` trực tiếp → block TPS khi nhiều người mở GUI | Bổ sung bộ đếm trong RAM, không còn query DB trên main thread |
| 5 | **`/blogingui` bị chặn khi chưa đăng nhập** | `PlayerCommandListener` không khai báo `blogingui`/`bgui`/`logingui` trong danh sách lệnh được phép | Bổ sung vào allow-list |
| 6 | **Mất mật khẩu đã nhập khi hết thời gian chờ** | Timeout 10s xoá trạng thái nhưng **không mở lại panel** → người chơi bị "mất" mật khẩu, không có thông báo | Timeout 30s + mở lại panel + thông báo rõ ràng |
| 7 | **Hàng trăm task scheduler không tự dừng** | Particle, actionbar, bossbar đều là `runTaskTimer` không bao giờ cancel | Tất cả tự dừng theo thời hạn, tự huỷ khi người chơi offline |
| 8 | **`PotionEffectType` bị cache `null` vĩnh viễn** | `Compat` cache cả giá trị `null`. Nếu lần gọi đầu tiên xảy ra trước khi registry sẵn sàng, hiệu ứng "chậm"/"nhảy cao" **không bao giờ hoạt động** nữa | Chỉ cache giá trị hợp lệ; thử lại ở lần gọi sau |
| 9 | **`/2fa status` query database 2 lần** | Gọi `getPlayerData()` hai lần liên tiếp | Một lần + cache 30s |

### 🎨 Giao diện GUI đăng nhập mới

GUI cũ chỉ có 6 vật phẩm rời rạc, không có khung, không hiển thị đầu người chơi, không có captcha, và mọi thông tin đều là chữ tĩnh.

**Bố cục mới (3 hàng × 9 cột):**

```
         [Đầu người chơi + glint]
[Thông tin]  [Captcha]  [ĐĂNG NHẬP]  [Hướng dẫn]
[Ô nhập mật khẩu]        [Ô nhập 2FA]        [Đóng]
```

| Hạng mục | Mô tả |
|----------|-------|
| 🎭 **Đầu người chơi** | Hiển thị skin của chính người chơi, có hiệu ứng lấp lánh (glint) |
| 📋 **Thông tin** | Tài khoản, IP, trạng thái đã đăng ký, số lần thử còn lại — cập nhật tức thì |
| 🔐 **Ô Captcha** | Hiện mã captcha hiện tại, nhấn để lấy mã mới hoặc nhập mã trong chat |
| ✅ **Nút ĐĂNG NHẬP** | Hiển thị trạng thái mật khẩu / 2FA / captcha đã nhập hay chưa |
| ⌨️ **Ô nhập mật khẩu & 2FA** | Nhập trực tiếp qua chat, có phản hồi trạng thái ngay trên panel |
| ⌨️ **Shift + Click** | Đóng panel nhanh bất kỳ ô nào |

Mọi thông tin trên panel được **làm mới tự động** khi trạng thái thay đổi (đăng ký mới, đổi mật khẩu, hết lượt thử…).

### 🏗️ Build đa phiên bản

Bản build mặc định dùng **paper-api 1.20.1** (bytecode Java 17) → **một JAR chạy được trên toàn bộ** server 1.20.1 → 1.21.11 → 26.x.

```bash
# JAR dùng chung cho mọi phiên bản (khuyến nghị)
mvn clean package

# Biên dịch riêng cho 1.21.11
mvn clean package -P paper-1.21.11

# Biên dịch riêng cho Paper 26.x (cần JDK 25)
mvn clean package -P paper-26
```

| API build | Bytecode | Chạy được trên |
|-----------|----------|----------------|
| `1.20.1-R0.1` (mặc định) | Java 17 (major 61) | 1.20.1 → 1.21.11 → 26.x |
| `1.21.11-R0.1` | Java 21 (major 65) | 1.21.11 → 26.x |
| `26.1.2` | Java 25 (major 69) | 26.x |

> **Lưu ý:** Paper đã đổi từ đánh phiên bản `1.21.x` sang `26.x` sau 1.21.11. Profile `paper-26` **bắt buộc JDK 25** vì `paper-api 26.x` dùng bytecode Java 25.

### 🔧 Bảo mật

---

## 🔧 Bản vá 3.0.1 — Sửa lỗi biên dịch & tương thích đa phiên bản

> ℹ️ Phần này ghi lại lịch sử bản vá 3.0.1. Xem [bản 3.1.0](#-bản-vá-310--nâng-cấp-giao-diện--hỗ-trợ-121111--26x) ở trên cho thay đổi mới nhất.

Phiên bản 3.0.0 **không biên dịch được** (13 lỗi compile) và nhiều lỗi runtime chỉ xuất hiện trên server mới. Bản 3.0.1 đã sửa toàn bộ.

### 🐛 Lỗi biên dịch (plugin không load được — nướng server)

| Lỗi | Nguyên nhân | Sửa |
|-----|-----------|-----|
| `Component.Builder` không tồn tại | Adventure 4.14+ đổi tên sang `ComponentBuilder` | Đổi sang `ComponentBuilder` |
| `TextColor.lerp(from, to, ratio)` sai thứ tự | Adventure đổi signature thành `lerp(ratio, a, b)` | Sửa thứ tự tham số |
| `PotionEffectType.SLOW` không tồn tại | 1.21+ đổi tên thành `SLOWNESS` | Dùng `Compat` tra cứu động |
| `Particle.REDSTONE` không tồn tại | 1.20.5+ đổi tên thành `DUST` | Dùng `Compat` tra cứu động |
| `PlayerInteractEvent.getClickedInventory()` không tồn tại | API này không có trong 1.20.1/1.21.4 | Bỏ, chặn inventory ở `InventoryClickEvent` |
| `Player.getVersion()` không tồn tại | Đã bị gỡ bỏ | Dùng `getClientBrandName()` |
| `captchaManager.reload()` không tồn tại | Lớp thiếu method → `/blogin reload` ném lỗi | Bổ sung `CaptchaManager.reload()` |
| `HashSet<>` không suy luận được | `BossBar#viewers()` trả `Iterable`, không phải `Collection` | Duyệt thủ công, copy sang list |
| `long` → `float` / `int` mismatch | Chia sai kiểu số | Sửa kiểu |

### 🎯 Lỗi runtime chỉ xảy ra trên server MỚI (1.21.x)

Đây là loại lỗi nguy hiểm nhất: **server 1.20.1 chạy bình thường, server 1.21.4 crash ngay khi plugin load**.

- 🔴 `PotionEffectType.SLOW` → `NoSuchFieldError` trên 1.21.x
  → Hiệu ứng "chậm" của người chơi chưa đăng nhập **gây crash plugin**
- 🔴 `Particle.REDSTONE` → `NoSuchFieldError` trên 1.20.5+
  → Hiệu ứng welcome **gây crash plugin**
- 🔴 `ExceptionInInitializerError` khi tra cứu enum chưa khởi tạo
  → Đã bắt `LinkageError` trong `Compat` để không bao giờ làm hỏng plugin

> ✅ **Giải pháp:** lớp mới `vn.blogin.utils.Compat` tự dò đúng hằng số theo phiên bản server đang chạy, có cache để không tốn hiệu năng.

| Ý nghĩa | 1.20.1 | 1.20.5+ / 1.21+ |
|---------|--------|------------------|
| Chậm (Slowness) | `SLOW` | `SLOWNESS` |
| Nhảy cao (Jump) | `JUMP` | `JUMP_BOOST` |
| Bụi đỏ (Redstone) | `REDSTONE` | `DUST` |

### 🏗️ Build đa phiên bản

- ✅ Chuyển build sang **paper-api 1.20.1** (bytecode Java 17) làm chuẩn
  → JAR nạp được trên server 1.20.1 (Java 17) **và** 1.21.4 (Java 21)
  → Trước đây build bằng 1.21.4 tạo bytecode Java 21 → **không chạy được trên 1.20.1**
- ✅ Xác minh cùng một mã nguồn **biên dịch thành công trên cả hai** API baseline
- ✅ Xác minh bytecode: toàn bộ class của plugin đều là **major 61 (Java 17)**

### 🔐 Bảo mật

- ✅ `EncryptionUtil` **không còn crash** khi file khoá 2FA bị rỗng
  (server crash giữa lúc ghi) → tự sinh khoá mới thay vì làm plugin không load
- ✅ `ConnectionPool` xử lý đúng `Error` (không chỉ `Exception`) khi unwrap JDBC proxy
- ✅ `PlayerRestrictionUtil` không gọi `removePotionEffect(null)` khi hiệu ứng không resolve được

### 🧪 Kiểm thử tự động

- ✅ `SelfTest`: **35/35 test pass** (Base32 RFC 4648, TOTP RFC 6238, độ mạnh mật khẩu, AES-GCM)
- ✅ `CompatProbe`: kiểm chứng runtime trên **cả 1.20.1 và 1.21.4** — xác nhận chọn đúng hằng số
- ✅ Kiểm tra bytecode tự động: không còn tham chiếu cứng tới hằng số đã đổi tên

### 🛠️ Cách build

```bash
# Yêu cầu: JDK 17 + Maven 3.9+
mvn clean package
# JAR: target/Blogin-3.0.1.jar
```

---

## 🚀 Điểm mới ở phiên bản 3.0

### 🖥️ GUI Đăng nhập (Chest GUI) — TÍNH NĂNG MỚI
- ✅ Panel đăng nhập 27 ô với item mô tả đẹp mắt (nút đăng nhập, ô nhập mật khẩu, ô nhập 2FA)
- ✅ **Thiết kế lại 3.1.0**: đầu người chơi + glint, ô captcha riêng, thông tin cập nhật tức thì
- ✅ Tự động mở GUI khi người chơi vào server (tuỳ chỉnh được)
- ✅ Nhập mật khẩu / mã 2FA / captcha trực tiếp trong chat (tin nhắn **được ẩn** khỏi người khác)
- ✅ Chặn kéo/thả item, không thể lấy item ra khỏi GUI
- ✅ Hiển thị số lần thử còn lại, IP, trạng thái 2FA ngay trên panel
- ✅ Lệnh `/blogingui` (alias `/bgui`) để mở thủ công
- ✅ **Captcha cũng bắt buộc khi dùng GUI** (không thể bỏ qua như bản cũ)

### ⚡ Hiệu năng — KHÔNG CÒN BLOCK TPS
- ✅ **Toàn bộ truy vấn database chạy bất đồng bộ** trên thread riêng
- ✅ BCrypt (~250ms) không còn chạy trên main thread → **TPS ổn định khi nhiều người login đồng thời**
- ✅ **Connection pool thông minh**: connection được trả về pool thay vì đóng vĩnh viễn, có hàng đợi chờ (timeout 5s)
- ✅ **Tự giảm tần suất particle** khi server đông (>60 người) → không giật FPS
- ✅ Task dọn dẹp định kỳ: cache khóa hết hạn, bản ghi thất bại cũ, session mồ côi

### 🔒 Bảo mật nâng cấp
- ✅ **Secret 2FA được mã hoá AES-GCM** bằng khoá sinh ngẫu nhiên lưu ở `plugins/Blogin/secret.key`
  → Ngay cả khi lộ database thì mã 2FA vẫn không dùng được
- ✅ **Đánh giá độ mạnh mật khẩu** (thang 0-5), chặn mật khẩu yếu (`123456`, `password`...)
- ✅ **Tự re-hash** khi cost factor BCrypt được nâng lên
- ✅ Cost factor BCrypt **cấu hình được** (4-16) — server yếu dùng 10, server mạnh dùng 13
- ✅ Chặn IP ngay từ `PlayerLoginEvent` (trước khi người chơi vào thế giới)
- ✅ **IP-lock**: khoá IP đăng nhập sai quá nhiều lần + tuỳ chọn tự động đăng nhập theo IP đã xác thực
  (tài khoản có bật 2FA sẽ **không** được auto-login)
- ✅ **Nhắc admin bật 2FA** sau khi đăng nhập (1 lần mỗi phiên, bất đồng bộ)
- ✅ Ghi log kết nối (IP + phiên bản client) để quản trị
- ✅ Cache khóa dùng `ConcurrentHashMap` + dọn dẹp định kỳ — không rò rỉ bộ nhớ

### 🎨 Giao diện tối ưu cho server
- ✅ Hỗ trợ **mã màu hex** `&#RRGGBB` trên toàn bộ tin nhắn, title, lore, bossbar
- ✅ **12 loại particle** (thêm `PORTAL` cổng dịch chuyển & `AURA` hào quang neon)
- ✅ Khung giao diện mở khung + gradient màu theo ký tự
- ✅ BossBar đếm ngược đổi màu xanh → vàng → đỏ theo thời gian
- ✅ Ẩn thông báo "joined the game" mặc định của Minecraft (tuỳ chỉnh được)
- ✅ `/logout` giờ ẩn người chơi + áp lại hiệu ứng hạn chế (đồng nhất lúc mới vào)
- ✅ Cảnh báo di chuyển **giảm spam** (1 lần / 2 giây) thay vì spam mỗi tick

### 🐛 Bug đã sửa (v3.0.1)
- ✅ **`/blogin force` gây lỗi hiển thị**: trước đây admin ép đăng nhập xong người chơi vẫn **bị mù, bị chậm và ẩn khỏi mọi người khác** vì logic gỡ hiệu ứng bị lặp ở nhiều nơi và thiếu ở luồng force-login
  → Đã gom toàn bộ vào `PlayerRestrictionUtil` (điểm vào duy nhất cho mọi luồng đăng nhập thành công)
- ✅ **Rò rỉ bộ nhớ**: `PlayerMoveListener` và `PlayerInteractListener` giữ map cảnh báo mãi không xoá khi người chơi thoát
- ✅ **Rò rỉ task**: BossBar cứ tiếp tục chạy (2-10 tick/lần) sau khi người chơi offline
  → Đã thêm sổ theo dõi theo UUID + tự huỷ khi thoát + `shutdown()` khi tắt plugin
- ✅ **Mất Javadoc**: quy trình xử lý file trước đó vô tình cắt mất toàn bộ comment trong source
  → Đã phục hồi đầy đủ từ bản backup và khôi phục Javadoc cho toàn bộ 45 file
- ✅ **2FA**: khi mã hoá secret thất bại thì **không còn lưu secret dạng plaintext**; khi tắt 2FA secret cũng được xoá khỏi DB
- ✅ **`updateTotpSecret(uuid, null)`** dùng `setNull()` đúng chuẩn JDBC thay vì `setString(null)`

### ⚙️ Tuỳ chỉnh được từ config (trước đây bị hard-code)
- ✅ **Âm thanh**: toàn bộ âm thanh đăng nhập / đăng ký / sai mật khẩu giờ đọc từ `am-thanh.*` (tên âm, âm lượng, cao độ, bật/tắt)
- ✅ **Màu BossBar đếm ngược** đọc từ `welcome.bossbar-mau-xanh/vang/do`
- ✅ **`gioi-han.tan-cong`** thực sự có hiệu lực + hiện cảnh báo cho người chơi thay vì chặn âm thầm
- ✅ **`tin-nhap`**: hiển thị từng dòng cách nhau 1 giây (hiệu ứng typewriter)
- ✅ **`admin.status-on/off`** và **`admin.info-status`** dùng cho `/blogin info`
- ✅ **`ngon-ngu`** được ghi nhận chính thức qua `MessageManager#getLanguage()`

### 🛠️ Lệnh admin mới
| Lệnh | Mô tả |
|------|-------|
| `/blogin list [trang]` | Danh sách tài khoản có phân trang |
| `/blogin reset <player> <mk>` | Reset mật khẩu cho người chơi |
| `/blogin 2fa <player> <on\|off\|secret>` | Quản lý 2FA từ xa |
| `/blogin gui` | Mở GUI đăng nhập (không cần quyền admin) |
| `/2fa status` | Xem trạng thái 2FA của bản thân |

---

## 🎯 Tính năng nổi bật

### 🔐 Bảo mật đỉnh cao
- ✅ **BCrypt** hashing với cost factor cấu hình được + salt ngẫu nhiên
- ✅ **2FA (TOTP)** chuẩn RFC 6238 — Google Authenticator, Authy, Microsoft Authenticator
- ✅ **Chống brute-force**: khóa tài khoản/IP theo cấu hình (ACCOUNT / IP / BOTH)
- ✅ **Captcha** chống bot đăng ký hàng loạt
- ✅ **Chống timing attack** trong xác thực TOTP
- ✅ Session in-memory, không lưu password plaintext

### 🌐 Database linh hoạt
- ✅ **SQLite** — cài đặt 0 config, file `blogin.db` tự tạo
- ✅ **MySQL / MariaDB** — connection pool + prepared statement cache
- ✅ Tự động tạo bảng + index khi khởi động
- ✅ Thread pool riêng cho truy vấn bất đồng bộ (`database.async-threads`)

### 🌍 Đa ngôn ngữ (10 ngôn ngữ)
- ✅ Toàn bộ thông báo **đã dịch đầy đủ** sang 10 ngôn ngữ: vi, en, zh, es, fr, de, ja, ko, ru, pt
- ✅ **Tự động nhận diện** ngôn ngữ client (`Player#getLocale`) — người chơi nước ngoài thấy đúng ngôn ngữ của họ
- ✅ Người chơi tự đổi ngôn ngữ: `/blogin language <mã>` (ghi nhớ theo UUID)
- ✅ Mọi thông báo, GUI (tên + lore), title, actionbar, bossbar đều theo ngôn ngữ từng người
- ✅ Ngôn ngữ mặc định cấu hình ở `config.yml` → `ngon-ngu: "vi"`
- ✅ Hỗ trợ mã màu `&` + hex `&#RRGGBB` + placeholder `{player}`, `{time}`, `{remaining}`
- ✅ Mọi thao tác gửi tin dùng Adventure API → hiển thị đồng nhất mọi client

### 🛡️ Khóa hành động
Người chưa đăng nhập **không thể**:
- ❌ Chat trong server
- ❌ Di chuyển (chỉ xoay góc nhìn)
- ❌ Phá / đặt khối
- ❌ Tấn công / bị tấn công
- ❌ Mở inventory, tương tác thực thể
- ❌ Nhặt / thả vật phẩm
- ❌ Dùng lệnh khác (trừ `/login`, `/register`, `/captcha`, `/2fa`, `/blogin`, `/blogingui`)

### 🔌 Tích hợp
- ✅ **PlaceholderAPI** — 13 placeholder có sẵn
- ✅ Paper API hiện đại (1.20.1 - 1.21.4)
- ✅ Maven build — shade dependencies

### 🎆 Hệ thống Welcome & Hiệu ứng rực rỡ
- ✅ **12 loại particle**: Trái tim, Ngôi sao, Galaxy, Cầu vồng, Đôi cánh thiên thần, Mưa trái tim, Cổng dịch chuyển, Hào quang...
- ✅ **Title animation typewriter** đổi màu liên tục
- ✅ **ActionBar rainbow/pulse/ticker** chạy chữ động
- ✅ **BossBar gradient** đổi màu cầu vồng, pulse, progress fill
- ✅ **Pháo hoa thật (entity Firework)** bắn quanh người chơi
- ✅ **Hệ thống âm thanh** giai điệu (C-E-G arpeggio, pháo hoa, chuông thành công)
- ✅ **4 mức độ**: MINIMAL / NORMAL / EPIC / CUSTOM
- ✅ **Tin nhắn broadcast** cho mọi người khi có thành viên mới
- ✅ **Lệnh admin `/bloginwelcome`** để test từng hiệu ứng
- ✅ **Countdown 3-2-1-GO** cho sự kiện

---

## 📦 Cài đặt

### Yêu cầu
- **Java 17** trở lên
- **Paper** hoặc **Spigot 1.20.1 - 1.21.4**
- (Tùy chọn) **PlaceholderAPI** nếu muốn dùng placeholder
- (Tùy chọn) **MySQL/MariaDB** nếu muốn dùng database server

### Bước 1: Build plugin (từ mã nguồn)
```bash
cd Blogin
mvn clean package
```
File JAR sẽ được tạo tại: `target/Blogin-3.0.0.jar`

### Bước 2: Cài lên server
1. Copy `Blogin-3.0.0.jar` vào thư mục `plugins/` của server
2. Khởi động server (hoặc dùng `/reload confirm`)
3. Sửa file `plugins/Blogin/config.yml` theo ý bạn
4. Dùng lệnh `/blogin reload` để tải lại config

> ⚠️ **Quan trọng:** File `plugins/Blogin/secret.key` là khoá mã hoá 2FA — **KHÔNG được chia sẻ hoặc xoá**.
> Nếu mất file này, toàn bộ secret 2FA đã lưu sẽ không giải mã được.

### Bước 3 (Tùy chọn): Cài MySQL
```sql
CREATE DATABASE blogin CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'blogin_user'@'%' IDENTIFIED BY 'mat_khau_cua_ban';
GRANT ALL PRIVILEGES ON blogin.* TO 'blogin_user'@'%';
FLUSH PRIVILEGES;
```
Sau đó sửa trong `config.yml`:
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

---

## 🎮 Hướng dẫn sử dụng

### 👤 Cho người chơi

| Lệnh | Alias | Mô tả |
|------|-------|-------|
| `/login <mk> [2fa]` | `/dangnhap` | Đăng nhập |
| `/register <mk> <xác nhận>` | `/dangky`, `/reg` | Đăng ký tài khoản |
| `/blogingui` | `/bgui`, `/logingui` | Mở GUI đăng nhập |
| `/logout` | `/dangxuat` | Đăng xuất |
| `/changepassword <cũ> <mới>` | `/doimatkhau`, `/cpass` | Đổi mật khẩu |
| `/2fa enable` | `/totp`, `/otp` | Bật xác thực 2 yếu tố |
| `/2fa confirm <code>` | | Xác nhận bật 2FA |
| `/2fa disable <code>` | | Tắt 2FA |
| `/2fa status` | | Xem trạng thái 2FA |
| `/captcha` | `/cap` | Lấy captcha mới |
| `/blogin language <mã>` | `/lang`, `/ngonngu` | Đổi ngôn ngữ hiển thị |

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

**Khi có captcha** — Cú pháp: `/login <captcha> <mật khẩu>`:
```
/login aB3x9 Matkhau123
```

**Bật 2FA**:
1. `/2fa enable` → nhận secret key
2. Mở Google Authenticator → nhập secret
3. `/2fa confirm 123456` (mã hiện tại)

**Dùng GUI**: gõ `/blogingui` → nhấn ô mật khẩu → gõ mật khẩu trong chat → nhấn nút xanh lá.

---

### 👑 Cho Admin

| Lệnh | Mô tả |
|------|-------|
| `/blogin reload` | Tải lại config mà không cần restart |
| `/blogin unregister <player>` | Xóa tài khoản người chơi |
| `/blogin info <player>` | Xem thông tin chi tiết |
| `/blogin force <player>` | Ép buộc đăng nhập (dành cho người quên mật khẩu) |
| `/blogin unlock <player>` | Mở khóa tài khoản bị khóa do brute-force |
| `/blogin reset <player> <mk>` | Reset mật khẩu cho người chơi |
| `/blogin 2fa <player> <on\|off\|secret>` | Quản lý 2FA từ xa |
| `/blogin list [trang]` | Danh sách tài khoản có phân trang |
| `/blogin status` | Xem trạng thái plugin + thống kê |
| `/blogin help` | Hiển thị trợ giúp |

**Quyền admin:** `blogin.admin` (mặc định chỉ OP)

---

## ⚙️ Cấu hình (config.yml)

### File cấu hình có cấu trúc:
```yaml
# Timeout đăng nhập (giây)
thoi-gian-dang-nhap: 60

# Database
database:
  loai: "SQLITE"        # hoặc MYSQL
  async-threads: 2       # thread xử lý DB bất đồng bộ
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
  che-do-khoa: "BOTH"      # ACCOUNT | IP | BOTH
  bcrypt-cost: 12          # 4-16 (server yếu: 10, server mạnh: 13)
  chan-mat-khau-yeu: true
  diem-mat-khau-toi-thieu: 2   # 0-5
  chu-ky-don-dep: 300
  mat-khau-toi-thieu: 6
  mat-khau-toi-da: 64
  yeu-cau-mat-khau:
    chu-hoa: true
    chu-thuong: true
    so: true
    ky-tu-dac-biet: false

# GUI đăng nhập
gui:
  bat: true
  auto-mo: true
  tieu-de: "&8✦ &6&lBLOGIN &8✦ &7| &f{player}"
  hien-dau-nguoi-choi: true   # hiện đầu người chơi ở ô giữa hàng trên
  hien-o-captcha: true        # hiện ô captcha trên panel

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
  bat-buoc-admin: true     # nhắc admin bật 2FA sau khi đăng nhập
  so-chu-so: 6
  thoi-gian-hieu-luc: 30
  do-lech-cho-phep: 1

# IP-lock - khoá IP & tự động đăng nhập theo IP
ip-lock:
  bat: false
  tu-dong-dang-nhap: true   # auto-login nếu IP trùng IP đã đăng ký (không áp dụng 2FA)

# Âm thanh - tuỳ chỉnh được cho từng sự kiện
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

### Bảng mã màu

| Mã | Màu | Mã | Màu |
|----|------|----|------|
| `&0` | Đen | `&a` | Xanh lá |
| `&1` | Xanh dương đậm | `&b` | Xanh dương |
| `&2` | Xanh lá đậm | `&c` | Đỏ |
| `&3` | Xanh dương nhạt | `&d` | Tím hồng |
| `&4` | Vàng đậm | `&e` | Vàng |
| `&5` | Tím | `&f` | Trắng |
| `&6` | Cam | `&l` | In đậm |
| `&7` | Xám | `&o` | In nghiêng |
| `&8` | Xám đậm | `&n` | Gạch chân |
| `&9` | Xanh dương | `&k` | Ẩn (obfuscated) |
| `&r` | Xoá màu | `&#RRGGBB` | Hex 24-bit |

### Tùy chỉnh tin nhắn
Tất cả tin nhắn đều nằm trong `config.yml` dưới mục `tin-nhan:`. Bạn có thể sửa trực tiếp và dùng:
- Mã màu Minecraft: `&a` (xanh lá), `&c` (đỏ), `&e` (vàng), `&b` (xanh dương), `&6` (cam), `&l` (in đậm), `&o` (in nghiêng), `&n` (gạch chân)
- Hex color: `&#FF5733`
- Placeholder: `{player}`, `{time}`, `{remaining}`, `{count}`, `{min}`, `{max}`, `{captcha}`, `{ip}`, `{status}`, `{uuid}`

---

## 🔌 PlaceholderAPI

| Placeholder | Mô tả | Ví dụ |
|-------------|-------|-------|
| `%blogin_authenticated%` | Đã đăng nhập chưa | `yes` / `no` |
| `%blogin_registered%` | Đã đăng ký chưa | `yes` / `no` |
| `%blogin_2fa%` | Có bật 2FA không | `yes` / `no` |
| `%blogin_ip%` | IP đã đăng ký | `192.168.1.100` |
| `%blogin_ip_current%` | IP hiện tại | `192.168.1.100` |
| `%blogin_logins%` | Tổng số lần đăng nhập | `42` |
| `%blogin_last_login%` | Thời gian đăng nhập cuối (ms) | `1700000000000` |
| `%blogin_lock_remaining%` | Thời gian khóa còn lại (giây) | `120` |
| `%blogin_remaining_attempts%` | Số lần thử còn lại | `3` |
| `%blogin_timeout%` | Thời gian đăng nhập còn lại (giây) | `45` |
| `%blogin_total_players%` | Tổng số tài khoản đã đăng ký | `152` |
| `%blogin_totp_enabled_count%` | Số tài khoản bật 2FA | `38` |
| `%blogin_version%` | Phiên bản plugin | `3.0.0` |

Dùng trong các plugin như TAB, Scoreboard, DeluxeMenus...

---

## 🎆 Hệ thống Welcome & Hiệu ứng rực rỡ

### Cấu hình Welcome trong config.yml

```yaml
welcome:
  bat: true
  muc-do: "EPIC"   # MINIMAL | NORMAL | EPIC | CUSTOM | DISABLED
  nguong-chat-server: 60   # tự giảm particle khi server đông
  particle-types: [RAINBOW, STAR, GALAXY, SPIRAL, PORTAL]
  particle-login: "FIREWORK"
  particle-register: "HEARTS_RAIN"
  phao-hoa: true
  so-phao-hoa: 5
  bossbar-timeout: true
  broadcast-join: true
  halo-neon:
    bat: false     # hào quang neon quanh người chơi
    thoi-gian: 100
```

### 12 loại particle có sẵn

| Hiệu ứng | Mô tả |
|----------|-------|
| `HEART` | Trái tim particle bắn ra |
| `STAR` | Ngôi sao 5 cánh xoay |
| `SPIRAL` | Xoáy tròn 3D spiral |
| `CIRCLE` | Vòng tròn xoay quanh |
| `HELIX` | Chuỗi xoắn ốc DNA (2 sợi) |
| `FIREWORK` | Pháo hoa mini |
| `RAINBOW` | Cầu vồng gradient màu (3 lớp) |
| `WINGS` | Đôi cánh thiên thần flap |
| `GALAXY` | Xoáy galaxy 3D (4 arms) |
| `HEARTS_RAIN` | Mưa trái tim từ trên xuống |
| `PORTAL` | Cổng dịch chuyển phát sáng (**mới**) |
| `AURA` | Hào quang neon bao quanh (**mới**) |

### 4 mức độ Welcome

| Mức độ | Bao gồm |
|--------|---------|
| **MINIMAL** | Title + âm thanh ngắn |
| **NORMAL** | Title + Particle (random) + Sound |
| **EPIC** | Tất cả + BossBar + Fireworks + Broadcast |
| **CUSTOM** | Tự bật/tắt từng thành phần bên dưới |

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
│   │   ├── commands/                # 10 command executors
│   │   │   ├── LoginCommand.java
│   │   │   ├── RegisterCommand.java
│   │   │   ├── LogoutCommand.java
│   │   │   ├── ChangePasswordCommand.java
│   │   │   ├── BloginAdminCommand.java
│   │   │   ├── CaptchaCommand.java
│   │   │   ├── TwoFactorCommand.java
│   │   │   ├── LoginGuiCommand.java       # Mở GUI đăng nhập
│   │   │   ├── LanguageCommand.java        # Đổi ngôn ngữ (mới)
│   │   │   └── WelcomeAdminCommand.java   # Test hiệu ứng welcome
│   │   ├── database/                # Tầng database
│   │   │   ├── DatabaseManager.java
│   │   │   ├── ConnectionPool.java  # Pool thông minh + async executor
│   │   │   ├── PlayerDAO.java
│   │   │   └── AttemptDAO.java
│   │   ├── managers/                # Business logic
│   │   │   ├── AuthManager.java
│   │   │   ├── SessionManager.java
│   │   │   ├── CaptchaManager.java
│   │   │   ├── LanguageManager.java       # Quản lý ngôn ngữ (mới)
│   │   │   └── MessageManager.java
│   │   ├── gui/                     # GUI đăng nhập (mới)
│   │   │   └── LoginGui.java
│   │   ├── welcome/                 # Hệ thống Welcome rực rỡ
│   │   │   ├── WelcomeManager.java
│   │   │   ├── WelcomeParticleEffect.java  # 12 loại particle
│   │   │   ├── WelcomeTitle.java           # Title + ActionBar animated
│   │   │   ├── WelcomeSound.java           # Chuỗi âm thanh
│   │   │   └── WelcomeBossBar.java         # BossBar gradient
│   │   ├── listeners/               # 12 event listeners
│   │   │   ├── PlayerJoinListener.java
│   │   │   ├── PlayerJoinMessageListener.java
│   │   │   ├── PlayerLoginListener.java
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
│   │       ├── ChatUtil.java        # Adventure + hex + gradient (mới)
│   │       ├── EncryptionUtil.java  # AES-GCM cho 2FA (mới)
│   │       ├── HashingUtil.java     # BCrypt + rehash
│   │       ├── PasswordStrength.java# Đánh giá độ mạnh (mới)
│   │       ├── TOTPGenerator.java   # RFC 6238 2FA
│   │       └── SelfTest.java        # Test độc lập cho logic thuần Java
│   └── resources/
│       ├── plugin.yml
│       ├── config.yml               # Cấu hình Việt hóa + Welcome + GUI
│       └── lang/                    # 10 tệp ngôn ngữ (mới)
│           ├── vi.yml  en.yml  zh.yml  es.yml  fr.yml
│           └── de.yml  ja.yml  ko.yml  ru.yml  pt.yml
```

---

## 🧪 Kiến trúc & Design Patterns

| Pattern | Áp dụng tại |
|---------|-------------|
| **Singleton** | `Blogin.getInstance()` |
| **DAO (Data Access Object)** | `PlayerDAO`, `AttemptDAO` |
| **Manager / Service** | `AuthManager`, `SessionManager`, `CaptchaManager` |
| **Strategy** | `DatabaseManager` (SQLite/MySQL) |
| **Static Factory** | `HashingUtil`, `TOTPGenerator`, `EncryptionUtil` |
| **DTO (Record)** | `PlayerData` |
| **Proxy (Connection Pool)** | `ConnectionPool.wrap()` |
| **Modular Config** | `MessageManager` (đọc từ config) |

### Luồng xử lý đăng nhập (bất đồng bộ)
```
Player gửi /login <mk>
        ↓
LoginCommand → validate input (captcha, số args)
        ↓
AuthManager.loginAsync()  → chạy trên thread DB
        ↓
   ┌────┴─────────────────────────────┐
   ↓                                  ↓
verify BCrypt                   check lock (cache + DB)
   ↓                                  ↓
TOTP verify (nếu có)           AttemptDAO.countRecentFailures
   ↓
PlayerDAO.updateLoginInfo
        ↓
AuthResult → callback trên main thread → SessionManager.markAuthenticated()
        ↓
Hiệu ứng welcome + bỏ hạn chế + hiện lại player
```

---

## 🐛 Troubleshooting

### Plugin không khởi động được
**Lỗi:** `✖ Không thể kết nối Database!`
- **MySQL:** Kiểm tra thông tin host/port/user/pass
- **SQLite:** Đảm bảo plugin có quyền ghi file vào thư mục `plugins/Blogin/`

**Lỗi:** `✖ Không thể khởi tạo khoá mã hoá!`
- Xoá file `plugins/Blogin/secret.key` để plugin tạo lại
- ⚠️ Mất file này thì các secret 2FA đã lưu sẽ không giải mã được → cần người chơi bật lại 2FA

### GUI không mở
- Kiểm tra `gui.bat: true` và `gui.auto-mo: true`
- Dùng `/blogingui` để mở thủ công

### Captcha không hiện
- Kiểm tra `captcha.bat: true` trong config
- Dùng `/captcha` để lấy captcha mới

### 2FA không hoạt động
- Đảm bảo thời gian hệ thống chính xác (TOTP dựa vào thời gian)
- Tăng `do-lech-cho-phep` lên 2-3 nếu thường xuyên lỗi
- Ứng dụng Google Authenticator cần thêm tài khoản với secret key

### Server giật khi nhiều người login
- Giảm `bao-mat.bcrypt-cost` xuống 10
- Giảm `database.async-threads` nếu CPU yếu (tối thiểu 1)
- Tắt `welcome.halo-neon.bat`

### Quên mật khẩu admin
Dùng lệnh trong game (có quyền admin):
```
/blogin reset <player> <mật_khẩu_mới>
```
Hoặc vào MySQL:
```sql
DELETE FROM blogin_players WHERE name = 'tên_player';
```

---

## 📝 Logs & Debug

Bật chế độ debug trong config:
```yaml
debug: true
log-su-kien: true
```

Log sẽ ghi ra console:
```
[Kết nối] NewPlayer (uuid-here) từ IP 192.168.1.10 - Client 1.21.4
[Đăng ký] NewPlayer (uuid-here) từ IP 192.168.1.10
[Đăng nhập] NewPlayer (uuid-here) từ IP 192.168.1.10
```

---

## 🤝 Đóng góp

Plugin được tạo bởi **BloginVN** với ❤️ cho cộng đồng Minecraft.

Mọi đóng góp/báo lỗi xin gửi về:
- GitHub: [github.com/taolaadminshopok1/blogin-plugin](https://github.com/taolaadminshopok1/blogin-plugin)
- Website: [blogin.vn](https://blogin.vn)

---

## 📜 License

MIT License - Sử dụng tự do cho mục đích cá nhân và thương mại.

---

## 🎉 Credits

- **BCrypt** - [jBCrypt](https://github.com/jeremyh/jBCrypt)
- **TOTP** - Triển khai theo [RFC 6238](https://tools.ietf.org/html/rfc6238)
- **AES-GCM** - Java Cryptography Architecture (JCA)
- **Paper API** - [PaperMC](https://papermc.io/)
- **PlaceholderAPI** - [PlaceholderAPI](https://github.com/PlaceholderAPI/PlaceholderAPI)
