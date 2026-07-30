# Thay đổi: Thoát chế độ Recent khi vào Storage/Quick folder

So với `MainActivity.java` bản gốc trong zip. `−` = xoá, `+` = thêm.

## Vấn đề

Đang ở tab **Recent**, mở sidebar rồi bấm vào một **Storage** (hoặc **Thư mục nhanh**):
- Tiêu đề đầu trang vẫn là **"Recent"** thay vì **"My File"**.
- Bấm **Sort** thì sắp xếp danh sách **Recent**, không phải folder vừa mở.

**Nguyên nhân:** 2 handler trong sidebar gọi thẳng `loadFiles(...)` nhưng **không reset trạng thái Recent**. Cờ `isRecentTab` vẫn `true`, tiêu đề không đổi, `adapter` vẫn ở `recentMode`. Nhiều nhánh (`updateSortMode`, `onSelectionChanged`, `bulkVault`...) rẽ theo `isRecentTab` nên vẫn thao tác trên Recent.

## Cách sửa

Tách phần reset trạng thái Recent thành 1 hàm dùng chung `exitRecentMode()`, rồi gọi nó trước khi mở Storage/Quick folder.

---

## 1. Tách helper `exitRecentMode()` + refactor `switchToBrowse()`

```diff
 private void switchToBrowse() {
-    isRecentTab = false;
-    TextView tvTitle = findViewById(R.id.tvToolbarTitle);
-    if (tvTitle != null) tvTitle.setText("My File");
-
-    findViewById(R.id.breadcrumbScroll).setVisibility(View.VISIBLE);
-    findViewById(R.id.quickActionsContainer).setVisibility(View.VISIBLE);
-
-    adapter.setRecentMode(false);
+    exitRecentMode();
     if (currentPath == null) {
         currentPath = rootPath;
     }
     loadFiles(currentPath);
 }
+
+/** Chỉ reset trạng thái Recent (không load): dùng khi rời Recent để vào Browse/Storage/Quick folder. */
+private void exitRecentMode() {
+    isRecentTab = false;
+    TextView tvTitle = findViewById(R.id.tvToolbarTitle);
+    if (tvTitle != null) tvTitle.setText("My File");
+    findViewById(R.id.breadcrumbScroll).setVisibility(View.VISIBLE);
+    findViewById(R.id.quickActionsContainer).setVisibility(View.VISIBLE);
+    adapter.setRecentMode(false);
+}
```

> `switchToBrowse()` giữ nguyên hành vi cũ, chỉ đổi sang gọi `exitRecentMode()`.

## 2. Handler Storage (dòng gốc ~1036) — thêm 1 dòng

```diff
 rvStorage.setAdapter(new StorageAdapter(StorageHelper.getStorages(this), storage -> {
+    exitRecentMode(); // Thoat che do Recent khi vao storage: doi tieu de + cho sort dung folder
     rootPath = storage.getPath();
     searchQuery = "";
     loadFiles(rootPath);
     drawerLayout.closeDrawer(GravityCompat.START);
 }));
```

## 3. Handler Quick folder (dòng gốc ~1045) — thêm 1 dòng

```diff
 rvQuickFolders.setAdapter(new QuickFolderAdapter(StorageHelper.getQuickFolders(), folder -> {
+    exitRecentMode(); // Thoat che do Recent khi vao thu muc nhanh
     rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
     searchQuery = "";
     loadFiles(folder.getPath());
     drawerLayout.closeDrawer(GravityCompat.START);
 }));
```

---

## Tóm tắt

| Hạng mục | Chi tiết |
|---|---|
| Thêm | Hàm `exitRecentMode()` (reset cờ + tiêu đề + breadcrumb/quick actions + recentMode) |
| Sửa | `switchToBrowse()` gọi lại `exitRecentMode()` thay vì lặp code |
| Thêm | 2 lời gọi `exitRecentMode()` trong handler Storage & Quick folder |
| Không đụng | `FileRepositoryImpl`, `MainViewModel`, các Operation |

### Kết quả
Vào Storage/Thư mục nhanh từ Recent → tiêu đề đổi về **"My File"**, `isRecentTab = false` nên **Sort chạy đúng trên folder đang trỏ tới**, danh sách hiển thị kiểu Browse (không còn định dạng Recent).

### Nên rà thêm (tùy chọn)
Bảo đảm mọi lối rời Recent đều gọi `exitRecentMode()` — hiện đã phủ tab Browse, Storage, Quick folder. Nếu sau này thêm lối vào folder khác (vd mở từ notification/intent) thì nhớ gọi kèm.

