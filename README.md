# 🦴 BoneMeal AutoFarm Mod v4.0.0
**Fabric 1.21.1 | Client-Side Only**

Mod tự động nạp lại Bone Meal cho server: tự /shop qua 7 menu, craft bone → bonemeal, rồi /kho lấy lúa mì.

---

## 📦 Cài đặt

1. Cài **Fabric Loader** 0.15.11 → https://fabricmc.net/use/installer/
2. Cài **Fabric API** 0.100.8+1.21.1 → bỏ `.jar` vào `mods/`
3. Bỏ `bonemealauto-4.0.0.jar` vào thư mục `mods/`
4. Khởi động game → config tự tạo tại:
   - Windows: `%AppData%\.minecraft\config\bonemealauto.json`
   - Linux: `~/.minecraft/config/bonemealauto.json`

---

## ⌨️ Phím tắt

| Phím | Chức năng |
|------|-----------|
| **J** | Bật/tắt auto-farm |

Đổi phím: vào **Options → Controls → Keybinds → BoneMeal AutoFarm**

---

## ⚙️ Cấu hình Slot (QUAN TRỌNG)

Mở file `.minecraft/config/bonemealauto.json` và điền số slot đúng cho server của bạn.

### Cách tìm số slot

1. Cài mod helper **"Inventory Slots"** từ https://modrinth.com/mod/inventory-slots
2. Vào game, gõ `/shop` → hover chuột vào từng ô → xem số slot hiển thị
3. Ghi lại đúng slot cho từng menu

### Sơ đồ slot mặc định (cần chỉnh lại!)

```
SHOP FLOW (7 menu):
  Menu 1 (player inventory): SHOP_MENU1_SLOT  → dòng 2, ô 8 từ trái
  Menu 2 (double chest):     SHOP_MENU2_SLOT  → dòng 6, ô 7
  Menu 3 (double chest):     SHOP_MENU3_SLOT  → dòng 6, ô 7
  Menu 4 (double chest):     SHOP_MENU4_SLOT  → dòng 6, ô 7
  Menu 5 (double chest):     SHOP_MENU5_SLOT  → dòng 6, ô 7
  Menu 6 (double chest):     SHOP_MENU6_SLOT  → dòng 4, ô 6
  Menu 7 (double chest):     SHOP_MENU7_SLOT  → dòng 3, ô 3

KHO FLOW (2 menu):
  Menu 1 (double chest):  KHO_MENU1_SLOT  → dòng 2, ô 8 (hoặc tìm item Wheat)
  Menu 2 (player inv):    KHO_MENU2_SLOT  → dòng 3, ô 4
```

### Cách tính số slot từ dòng/cột

```
Double chest (54 slot = 6 hàng × 9 cột):
  slot = (hàng - 1) × 9 + (cột - 1)
  Ví dụ: dòng 6, ô 7 → (6-1)×9 + (7-1) = 45+6 = 51

Player inventory (36 slot = 4 hàng × 9 cột):
  slot = (hàng - 1) × 9 + (cột - 1)
  Ví dụ: dòng 2, ô 8 → (2-1)×9 + (8-1) = 9+7 = 16
```

---

## 🔄 Cách hoạt động

```
[J] Bật mod
  ↓
Mỗi 6 phút: quét bone_meal trong inventory
  ↓ (nếu hết)
1. DROP_SEEDS   : nhìn xuống, vứt toàn bộ seeds (không đụng offhand)
2. POSITIONING  : chạy thẳng + nhảy 2.5 giây (~10 block)
3. SHOP ×4 lần : gõ /shop → click qua 7 menu → craft bone→bonemeal
4. WAIT         : đợi 20 giây (anti-detection)
5. KHO          : nếu có lúa mì → gõ /kho → click lấy lúa
6. IDLE         : về đầu, chờ vòng lặp tiếp theo
```

### 3-Layer Fail-Safe
- **Layer 1**: Mỗi state có timeout riêng (3–30 giây), tự retry 2 lần rồi reset
- **Layer 2**: Click GUI lag → retry tối đa 2 lần với delay ngẫu nhiên
- **Layer 3**: Watchdog 5 phút → nếu không về IDLE thì force reset

---

## 🐛 Debug

- Chat in-game: xem thông báo `[BoneMealAuto]` màu xám
- Log: `.minecraft/logs/latest.log`
- Bị kẹt: bấm **J** tắt rồi bật lại; watchdog cũng tự reset sau 5 phút

---

## 🏗️ Build từ source

```bash
# Yêu cầu: Java 17+, Git
git clone <repo_url> bonemealauto
cd bonemealauto

# Windows
gradlew.bat build

# Linux/macOS
./gradlew build

# Output: build/libs/bonemealauto-4.0.0.jar
```

---

## ⚠️ Lưu ý

- Mod này **client-side only** — không cần cài trên server
- Chỉ hoạt động với server có lệnh `/shop` và `/kho` theo đúng flow đã mô tả
- Slot số có thể thay đổi khi server update → chỉnh lại config
- Không can thiệp offhand slot
