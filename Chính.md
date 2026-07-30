# Thay đổi mã nguồn — so với bản gốc trong zip

So sánh với `nhom_6-main4-with-partC_1.zip`. `−` = xoá, `+` = thêm. Số dòng ghi theo **file gốc** để dễ dò tới đúng chỗ.

**Tổng cộng: +79 dòng, −22 dòng, trên 3 file.**

| File | Thêm | Xoá | Mục đích |
|---|---|---|---|
| `SearchActivity.java` | +10 | −1 | Mở file ngay khi bấm kết quả tìm kiếm |
| `ui/main/FileAdapter.java` | +25 | −8 | Đếm số file trong folder ở luồng nền (hết giật khi cuộn) |
| `MainActivity.java` | +44 | −13 | Load folder & Recent chạy nền (hết treo/ANR khi nhiều file) |

---

## 1. `SearchActivity.java` — +10 / −1

**Vị trí:** dòng 88, nhánh `else` trong `onItemClick`.

```diff
                 } else {
-                    Toast.makeText(SearchActivity.this, "File: " + item.getName(), Toast.LENGTH_SHORT).show();
+                    // Mo file ngay tren man hinh Search: dieu huong toi viewer phu hop
+                    // (anh/video/audio/text mo trong app, con lai mo bang app he thong).
+                    // Truyen ket qua tim kiem lam "siblings" de co the vuot qua file cung loai.
+                    try {
+                        com.example.myfile.feature.viewer.ViewerFactory.open(
+                                SearchActivity.this, item, adapter.getItems());
+                    } catch (Exception e) {
+                        Toast.makeText(SearchActivity.this,
+                                "Khong the mo file nay", Toast.LENGTH_SHORT).show();
+                    }
                 }
```

**Ý nghĩa:** trước đây bấm file chỉ hiện Toast; nay gọi `ViewerFactory.open(...)` để mở đúng viewer, bọc `try/catch` chống crash với file lạ.

---

## 2. `FileAdapter.java` — +25 / −8

### (a) Sau dòng 35 (`private boolean isRecentMode = false;`) — thêm 6 dòng field

```diff
     private boolean isRecentMode = false;
 
+    // Dem so file trong folder o luong nen -> khong doc dia tren UI thread khi cuon.
+    private static final java.util.concurrent.ExecutorService COUNT_EXECUTOR =
+            java.util.concurrent.Executors.newFixedThreadPool(2);
+    private static final android.os.Handler MAIN_HANDLER =
+            new android.os.Handler(android.os.Looper.getMainLooper());
+
     public FileAdapter(List<FileItem> items, OnItemClickListener listener) {
```

### (b) Dòng 138–145 (khối đếm file trong folder) — xoá 8, thêm 19

```diff
             String date = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(item.getLastModified());
-            int childCount = 0;
-            java.io.File f = new java.io.File(item.getPath());
-            if (f.exists() && f.isDirectory()) {
-                String[] children = f.list();
-                if (children != null) childCount = children.length;
-            }
-            String countText = childCount == 1 ? "1 item" : childCount + " items";
-            itemHolder.tvDetail.setText(date + "  |  " + countText);
+            // Hien ngay truoc, dem so file trong folder o luong nen (tag chong lech khi recycle)
+            final String folderPath = item.getPath();
+            itemHolder.tvDetail.setTag(folderPath);
+            itemHolder.tvDetail.setText(date + "  |  …");
+            final TextView detailView = itemHolder.tvDetail;
+            COUNT_EXECUTOR.execute(() -> {
+                int childCount = 0;
+                java.io.File f = new java.io.File(folderPath);
+                if (f.isDirectory()) {
+                    String[] children = f.list();
+                    if (children != null) childCount = children.length;
+                }
+                final String countText = childCount == 1 ? "1 item" : childCount + " items";
+                MAIN_HANDLER.post(() -> {
+                    if (folderPath.equals(detailView.getTag())) {
+                        detailView.setText(date + "  |  " + countText);
+                    }
+                });
+            });
```

**Ý nghĩa:** `f.list()` không còn chạy trên UI thread khi cuộn; đẩy sang `COUNT_EXECUTOR`, cập nhật qua `MAIN_HANDLER`, dùng `setTag` chống lệch view khi RecyclerView tái sử dụng.

---

## 3. `MainActivity.java` — +44 / −13 (4 chỗ)

### (a) Sau dòng 85 (`searchQuery = ""`) — thêm field executor

```diff
     private SortMode sortMode = SortMode.DATE_DESC;
     private String searchQuery = "";
+
+    // Luong nen de load/sort/group folder -> tranh treo UI (ANR) khi folder co nhieu file.
+    private final java.util.concurrent.ExecutorService listExecutor =
+            java.util.concurrent.Executors.newSingleThreadExecutor();
     private int viewMode = 0; // 0: List, 1: Grid (3 cols), 2: Large Grid (2 cols)
```

### (b) Đầu `loadRecentFiles()` (dòng ~286) — thêm 3 dòng, bọc thân hàm vào executor

```diff
     private void loadRecentFiles() {
+        // Load nen: query MediaStore + quet fallback + group deu nang -> tranh treo UI khi nhieu file
+        findViewById(R.id.btnBack).setVisibility(View.GONE);
+        listExecutor.execute(() -> {
         List<FileItem> recent = new ArrayList<>();
```

### (c) Cuối `loadRecentFiles()` (dòng 339–342) — xoá 4, thêm 8

```diff
-        List<FileItem> grouped = groupFilesByDate(recent);
-        adapter.updateData(grouped);
-        emptyState.setVisibility(grouped.isEmpty() ? View.VISIBLE : View.GONE);
-        findViewById(R.id.btnBack).setVisibility(View.GONE);
+        final List<FileItem> grouped = groupFilesByDate(recent);
+        runOnUiThread(() -> {
+            if (!isRecentTab) return; // da chuyen sang tab Browse -> bo ket qua cu
+            adapter.updateData(grouped);
+            emptyState.setVisibility(grouped.isEmpty() ? View.VISIBLE : View.GONE);
+            findViewById(R.id.btnBack).setVisibility(View.GONE);
+        });
+        });
     }
```

### (d) `loadFiles()` (dòng 438–450) — xoá 9, thêm khối chạy nền + `onDestroy()`

```diff
         currentPath = path;
         tvCurrentPath.setText(path);
-        List<FileItem> items = fileRepository.list(path);
-        items = FileListHelper.sort(items, sortMode);
-        items = FileListHelper.filter(items, searchQuery);
-        if (!path.equals(rootPath)) {
-            items = groupFilesByDate(items);
-        }
-        
-        updateBreadcrumb(path);
-        adapter.updateData(items);
-        emptyState.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
-        findViewById(R.id.btnBack).setVisibility(path.equals(rootPath) ? View.GONE : View.VISIBLE);
+        // Phan nhe (UI) chay ngay tren main thread
+        updateBreadcrumb(path);
+        findViewById(R.id.btnBack).setVisibility(path.equals(rootPath) ? View.GONE : View.VISIBLE);
+
+        // Snapshot trang thai de tranh doc field khi dang o thread khac
+        final String targetPath = path;
+        final SortMode modeSnapshot = sortMode;
+        final String querySnapshot = searchQuery;
+
+        // Phan nang (doc dia + sort + group) day xuong luong nen
+        listExecutor.execute(() -> {
+            List<FileItem> items = fileRepository.list(targetPath);
+            items = FileListHelper.sort(items, modeSnapshot);
+            items = FileListHelper.filter(items, querySnapshot);
+            if (!targetPath.equals(rootPath)) {
+                items = groupFilesByDate(items);
+            }
+            final List<FileItem> result = items;
+            runOnUiThread(() -> {
+                // Chi ve UI neu nguoi dung van dang o dung folder nay (tranh ket qua cu)
+                if (!targetPath.equals(currentPath)) return;
+                adapter.updateData(result);
+                emptyState.setVisibility(result.isEmpty() ? View.VISIBLE : View.GONE);
+            });
+        });
+    }
+
+    @Override
+    protected void onDestroy() {
+        super.onDestroy();
+        listExecutor.shutdownNow();
     }
```

---

## Tóm tắt cơ chế

Cả 3 thay đổi cùng một hướng: **dời I/O ra khỏi UI thread**.

- `SearchActivity`: nối click kết quả vào `ViewerFactory` (mở file thật thay vì Toast).
- `FileAdapter`: đếm file/folder bằng `COUNT_EXECUTOR` + `MAIN_HANDLER` (hết giật khi cuộn).
- `MainActivity`: `loadFiles()` và `loadRecentFiles()` chạy trong `listExecutor`, trả UI qua `runOnUiThread` (hết treo/ANR khi folder hoặc Recent nhiều file); thêm `onDestroy()` dọn executor.
- 
