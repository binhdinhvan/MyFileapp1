# My File — Hướng dẫn build APK để test trên điện thoại

Đây là **project Android Studio đầy đủ** (đã có sẵn `build.gradle`, manifest, icon,
theme, gradle wrapper). Có 3 cách lấy file `app-debug.apk`. Chọn 1 cách.

---

## Cách 1 — Android Studio (dễ nhất nếu đã cài)

1. Giải nén thư mục `MyFile`.
2. Mở Android Studio → **Open** → chọn thư mục `MyFile`.
3. Đợi **Gradle Sync** xong (lần đầu sẽ tải thư viện, cần mạng).
4. Menu **Build → Build App Bundle(s) / APK(s) → Build APK(s)**.
5. Bấm **locate** trong thông báo, hoặc lấy file tại:
   `app/build/outputs/apk/debug/app-debug.apk`
6. Copy file `.apk` vào điện thoại → mở → cho phép **Cài từ nguồn không xác định** → cài.

---

## Cách 2 — Dòng lệnh (không cần mở Android Studio)

Yêu cầu: đã cài **JDK 17** và **Android SDK** (đặt biến `ANDROID_HOME` hoặc file
`local.properties` có dòng `sdk.dir=/duong/dan/den/Android/Sdk`).

```bash
cd MyFile
./gradlew assembleDebug        # Windows: gradlew.bat assembleDebug
```
APK nằm ở: `app/build/outputs/apk/debug/app-debug.apk`

---

## Cách 3 — Build trên cloud bằng GitHub Actions (KHÔNG cần cài gì)

Phù hợp nếu máy chưa cài Android Studio/SDK.

1. Tạo 1 repo mới trên GitHub (Private cũng được).
2. Đẩy toàn bộ thư mục `MyFile` lên nhánh `main`:
   ```bash
   cd MyFile
   git init
   git add .
   git commit -m "My File app"
   git branch -M main
   git remote add origin https://github.com/<ten-ban>/<ten-repo>.git
   git push -u origin main
   ```
3. Vào tab **Actions** của repo → workflow **"Build debug APK"** tự chạy
   (hoặc bấm **Run workflow**).
4. Chạy xong (~3–5 phút), mở lần chạy đó → mục **Artifacts** → tải
   **app-debug** → giải nén ra `app-debug.apk` → cài lên điện thoại.

> File workflow đã có sẵn tại `.github/workflows/build-apk.yml`, không cần chỉnh.

---

## Sau khi cài — lưu ý quyền

App cần quyền **"Quyền truy cập tất cả tệp" (All files access)**:
- Lần đầu mở, app sẽ chuyển sang màn hình Cài đặt → bật quyền cho **My File** → quay lại app.
- Đây là app quản lý file nên cần quyền này để đọc/ghi bộ nhớ.

## Thông số project
- `applicationId` / namespace: `com.example.myfile`
- `minSdk` 24 · `targetSdk` 34 · `compileSdk` 34
- AGP 8.2.2 · Gradle 8.5 · JDK 17
- Thư viện: appcompat, material, recyclerview, activity
