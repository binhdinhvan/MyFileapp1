# Fix: Thoát chế độ Recent khi vào Storage/Quick folder từ sidebar

**File:** `app/src/main/java/com/example/myfile/MainActivity.java`

## Lỗi

Khi đang ở tab **Recent**, mở sidebar và vào một **Storage** hoặc **Quick folder**:

- Tiêu đề đầu trang vẫn là "Recent" thay vì "My File".
- Bấm Sort thì sắp xếp danh sách Recent, không phải folder vừa mở.
- Danh sách vẫn render kiểu Recent (dòng chi tiết `size | tên folder cha`).
- Bottom-nav vẫn sáng ở "Recent".

Nguyên nhân: 2 handler trong sidebar gọi thẳng `loadFiles(...)` nhưng không reset trạng thái Recent. Cờ `isRecentTab` vẫn `true`, tiêu đề không đổi, `adapter` vẫn ở `recentMode`. Nhiều nhánh (`updateSortMode`, `onSelectionChanged`...) rẽ theo `isRecentTab` nên vẫn thao tác trên Recent.

## Cách sửa

Tách phần reset trạng thái Recent thành 1 hàm dùng chung `applyBrowseUi()` (đồng bộ luôn highlight bottom-nav), rồi gọi nó trước khi mở Storage/Quick folder.

## 1. Thêm field `bottomNav` + tách listener dùng chung

```diff
 private DrawerLayout drawerLayout;
+private com.google.android.material.bottomnavigation.BottomNavigationView bottomNav;
 private HorizontalScrollView breadcrumbScroll;
```

```diff
+private final com.google.android.material.navigation.NavigationBarView.OnItemSelectedListener bottomNavListener = item -> {
+    int id = item.getItemId();
+    if (id == R.id.nav_recent) { switchToRecent(); return true; }
+    else if (id == R.id.nav_browse) { switchToBrowse(); return true; }
+    return false;
+};
+
 private void setupBottomNavigation() {
-    com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
-    bottomNav.setOnItemSelectedListener(item -> {
-        int id = item.getItemId();
-        if (id == R.id.nav_recent) { switchToRecent(); return true; }
-        else if (id == R.id.nav_browse) { switchToBrowse(); return true; }
-        return false;
-    });
+    bottomNav = findViewById(R.id.bottomNavigation);
+    bottomNav.setOnItemSelectedListener(bottomNavListener);
     bottomNav.setSelectedItemId(R.id.nav_browse);
 }
```

## 2. Tách helper `applyBrowseUi()` + refactor `switchToBrowse()`

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
+    applyBrowseUi();
     if (currentPath == null) {
         currentPath = rootPath;
     }
     loadFiles(currentPath);
 }
+
+/** Reset trạng thái Recent về Browse (KHÔNG load): caller tự quyết định path cần load */
+private void applyBrowseUi() {
+    isRecentTab = false;
+    TextView tvTitle = findViewById(R.id.tvToolbarTitle);
+    if (tvTitle != null) tvTitle.setText("My File");
+    findViewById(R.id.breadcrumbScroll).setVisibility(View.VISIBLE);
+    findViewById(R.id.quickActionsContainer).setVisibility(View.VISIBLE);
+    adapter.setRecentMode(false);
+    // Đồng bộ highlight bottom-nav sang Browse mà không kích hoạt reload lần nữa
+    if (bottomNav != null && bottomNav.getSelectedItemId() != R.id.nav_browse) {
+        bottomNav.setOnItemSelectedListener(null);
+        bottomNav.setSelectedItemId(R.id.nav_browse);
+        bottomNav.setOnItemSelectedListener(bottomNavListener);
+    }
+}
```

## 3. Handler Storage (dòng gốc ~1036) — thêm 1 dòng

```diff
 rvStorage.setAdapter(new StorageAdapter(StorageHelper.getStorages(this), storage -> {
     rootPath = storage.getPath();
     searchQuery = "";
+    applyBrowseUi(); // Thoát chế độ Recent khi vào storage: đổi tiêu đề + cho sort đúng folder
     loadFiles(rootPath);
     drawerLayout.closeDrawer(GravityCompat.START);
 }));
```

## 4. Handler Quick folder (dòng gốc ~1045) — thêm 1 dòng

```diff
 rvQuickFolders.setAdapter(new QuickFolderAdapter(StorageHelper.getQuickFolders(), folder -> {
     rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
     searchQuery = "";
+    applyBrowseUi(); // Thoát chế độ Recent khi vào thư mục nhanh
     loadFiles(folder.getPath());
     drawerLayout.closeDrawer(GravityCompat.START);
 }));
```

## 5. Sort phân biệt Recent / Browse

```diff
 sortMode = SortMode.valueOf(field + suffix);
-if (currentPath != null) {
+if (isRecentTab) {
+    loadRecentFiles();
+} else if (currentPath != null) {
     loadFiles(currentPath);
 }
```

## Tóm tắt

| Hạng mục | Chi tiết |
|----------|----------|
| Thêm | Field `bottomNav` + listener dùng chung `bottomNavListener` |
| Thêm | Hàm `applyBrowseUi()` (reset cờ + tiêu đề + breadcrumb/quick actions + recentMode + đồng bộ bottom-nav) |
| Sửa | `switchToBrowse()` gọi lại `applyBrowseUi()` thay vì lặp code |
| Thêm | 2 lời gọi `applyBrowseUi()` trong handler Storage & Quick folder |
| Sửa | `updateSortMode()` thêm nhánh `isRecentTab` để không phá vỡ view Recent |
| Không đụng | `FileRepositoryImpl`, các `Operation`, các `Viewer` |

## Kết quả

Vào Storage/Thư mục nhanh từ Recent → tiêu đề đổi về "My File", `isRecentTab = false`, bottom-nav chuyển sang Browse. Sort chạy đúng trên folder đang trỏ tới, danh sách hiển thị kiểu Browse (không còn định dạng Recent).

