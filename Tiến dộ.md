# Thay đổi: Thanh tiến trình Move/Copy theo từng file

So với `MainActivity.java` bản gốc trong zip. `−` = xoá, `+` = thêm.

## Vấn đề

Bản cũ đặt `total = paths.size()` — tức **số item được chọn (folder)**. Move 3 folder, mỗi folder chứa hàng nghìn file → bar chỉ nhích khi xong **nguyên 1 folder**, nên đứng yên rất lâu ở `Moving 1/3`, người dùng tưởng treo.

## Cách sửa

Đếm **tổng số file thật** (đệ quy) rồi cập nhật bar theo **từng file** đã copy/move.

```
TRƯỚC:  Moving 1/3        (3 = số folder, đứng yên rất lâu)
SAU:    Moving 3450/12000 (12000 = số file thật, nhích liên tục)
```

---

## 1. `runBulkMove()` — viết lại

```diff
 private void runBulkMove(List<String> paths, String destPath) {
-    int total = paths.size();
-    showDeterminateProgress("Moving", total);
-    new Thread(() -> {
-        int successCount = 0;
-        for (int i = 0; i < total; i++) {
-            FileOperation operation = new MoveOperation(fileRepository, paths.get(i), destPath);
-            if (operation.execute()) {
-                successCount++;
-            }
-            int current = i + 1;
-            runOnUiThread(() -> updateDeterminateProgress("Moving", current, total));
-        }
-        int finalSuccessCount = successCount;
-        runOnUiThread(() -> {
-            dismissProgress();
-            Toast.makeText(this, "Moved " + finalSuccessCount + "/" + total + " items", Toast.LENGTH_SHORT).show();
-            adapter.exitSelectionMode();
-            loadFiles(currentPath);
-        });
-    }).start();
+    final int totalItems = paths.size();
+    showIndeterminateProgress("Đang chuẩn bị…"); // đếm file có thể mất chút thời gian
+    new Thread(() -> {
+        // Đếm tổng số FILE thật (đệ quy) -> bar phản ánh khối lượng, không phải số folder
+        int counted = 0;
+        for (String p : paths) counted += countFilesRecursive(new java.io.File(p));
+        final int totalFiles = Math.max(1, counted);
+        runOnUiThread(() -> { dismissProgress(); showDeterminateProgress("Moving", totalFiles); });
+
+        java.io.File destFolder = new java.io.File(destPath);
+        final int[] done = {0};
+        int successCount = 0;
+        for (String p : paths) {
+            java.io.File src = new java.io.File(p);
+            int filesHere = countFilesRecursive(src);
+            if (moveOneWithProgress(src, destFolder, done, totalFiles, filesHere, "Moving")) {
+                successCount++;
+            }
+        }
+        runOnUiThread(() -> updateDeterminateProgress("Moving", totalFiles, totalFiles));
+        final int finalSuccessCount = successCount;
+        runOnUiThread(() -> {
+            dismissProgress();
+            Toast.makeText(this, "Moved " + finalSuccessCount + "/" + totalItems + " items", Toast.LENGTH_SHORT).show();
+            adapter.exitSelectionMode();
+            loadFiles(currentPath);
+        });
+    }).start();
 }
```

## 2. `runBulkCopy()` — viết lại (tương tự, không có nhánh renameTo)

```diff
 private void runBulkCopy(List<String> paths, String destPath) {
-    int total = paths.size();
-    showDeterminateProgress("Copying", total);
-    new Thread(() -> {
-        int successCount = 0;
-        for (int i = 0; i < total; i++) {
-            FileOperation operation = new CopyOperation(fileRepository, paths.get(i), destPath);
-            if (operation.execute()) {
-                successCount++;
-            }
-            int current = i + 1;
-            runOnUiThread(() -> updateDeterminateProgress("Copying", current, total));
-        }
-        int finalSuccessCount = successCount;
-        runOnUiThread(() -> {
-            dismissProgress();
-            Toast.makeText(this, "Copied " + finalSuccessCount + "/" + total + " items", Toast.LENGTH_SHORT).show();
-            adapter.exitSelectionMode();
-            loadFiles(currentPath);
-        });
-    }).start();
+    final int totalItems = paths.size();
+    showIndeterminateProgress("Đang chuẩn bị…");
+    new Thread(() -> {
+        int counted = 0;
+        for (String p : paths) counted += countFilesRecursive(new java.io.File(p));
+        final int totalFiles = Math.max(1, counted);
+        runOnUiThread(() -> { dismissProgress(); showDeterminateProgress("Copying", totalFiles); });
+
+        java.io.File destFolder = new java.io.File(destPath);
+        final int[] done = {0};
+        int successCount = 0;
+        for (String p : paths) {
+            java.io.File src = new java.io.File(p);
+            if (copyOneWithProgress(src, destFolder, done, totalFiles, "Copying")) {
+                successCount++;
+            }
+        }
+        runOnUiThread(() -> updateDeterminateProgress("Copying", totalFiles, totalFiles));
+        final int finalSuccessCount = successCount;
+        runOnUiThread(() -> {
+            dismissProgress();
+            Toast.makeText(this, "Copied " + finalSuccessCount + "/" + totalItems + " items", Toast.LENGTH_SHORT).show();
+            adapter.exitSelectionMode();
+            loadFiles(currentPath);
+        });
+    }).start();
 }
```

## 3. Thêm mới — 7 hàm helper (ngay sau `runBulkCopy`)

```diff
+    // ============ Tiến trình theo TỪNG FILE cho move/copy hàng loạt ============
+
+    /** Đếm số file (leaf) trong 1 cây thư mục — làm tổng cho thanh tiến trình. */
+    private int countFilesRecursive(java.io.File f) {
+        if (f == null || !f.exists()) return 0;
+        if (f.isFile()) return 1;
+        java.io.File[] children = f.listFiles();
+        if (children == null) return 0;
+        int c = 0;
+        for (java.io.File ch : children) c += countFilesRecursive(ch);
+        return c;
+    }
+
+    private long lastProgressUpdate = 0;
+    /** Giới hạn ~80ms/lần để không nghẽn UI thread khi có hàng nghìn file. */
+    private void throttledProgress(int current, int total, String label) {
+        long now = System.currentTimeMillis();
+        if (now - lastProgressUpdate >= 80 || current >= total) {
+            lastProgressUpdate = now;
+            runOnUiThread(() -> updateDeterminateProgress(label, current, total));
+        }
+    }
+
+    /** Move 1 item: cùng ổ -> renameTo tức thì (cộng luôn số file); khác ổ -> copy từng file rồi xóa nguồn. */
+    private boolean moveOneWithProgress(java.io.File src, java.io.File destFolder,
+                                        int[] done, int total, int filesHere, String label) {
+        if (!src.exists() || !destFolder.isDirectory()) return false;
+        if (src.isDirectory() && isSubPathLocal(src, destFolder)) return false;
+        java.io.File dest = new java.io.File(destFolder, src.getName());
+        if (dest.exists()) return false;
+        if (src.renameTo(dest)) {
+            done[0] += filesHere;                 // di chuyển tức thì cả cây (cùng volume)
+            throttledProgress(done[0], total, label);
+            return true;
+        }
+        try {
+            copyTreeWithProgress(src, dest, done, total, label);
+        } catch (java.io.IOException e) {
+            return false;
+        }
+        return deleteRecursiveLocal(src);
+    }
+
+    /** Copy 1 item vào destFolder, đếm từng file đã copy. */
+    private boolean copyOneWithProgress(java.io.File src, java.io.File destFolder,
+                                        int[] done, int total, String label) {
+        if (!src.exists() || !destFolder.isDirectory()) return false;
+        if (src.isDirectory() && isSubPathLocal(src, destFolder)) return false;
+        java.io.File dest = new java.io.File(destFolder, src.getName());
+        if (dest.exists()) return false;
+        try {
+            copyTreeWithProgress(src, dest, done, total, label);
+            return true;
+        } catch (java.io.IOException e) {
+            return false;
+        }
+    }
+
+    /** Copy đệ quy; mỗi khi copy xong 1 file thì tăng done[0] và cập nhật tiến trình. */
+    private void copyTreeWithProgress(java.io.File source, java.io.File dest,
+                                      int[] done, int total, String label) throws java.io.IOException {
+        if (source.isDirectory()) {
+            if (!dest.exists() && !dest.mkdirs()) throw new java.io.IOException("Cannot create " + dest);
+            java.io.File[] children = source.listFiles();
+            if (children != null) {
+                for (java.io.File child : children) {
+                    copyTreeWithProgress(child, new java.io.File(dest, child.getName()), done, total, label);
+                }
+            }
+        } else {
+            try (java.io.FileInputStream in = new java.io.FileInputStream(source);
+                 java.io.FileOutputStream out = new java.io.FileOutputStream(dest)) {
+                byte[] buffer = new byte[8192];
+                int len;
+                while ((len = in.read(buffer)) > 0) out.write(buffer, 0, len);
+            }
+            done[0]++;                            // đã copy xong 1 file
+            throttledProgress(done[0], total, label);
+        }
+    }
+
+    private boolean isSubPathLocal(java.io.File parent, java.io.File possibleChild) {
+        java.io.File current = possibleChild;
+        while (current != null) {
+            if (current.equals(parent)) return true;
+            current = current.getParentFile();
+        }
+        return false;
+    }
+
+    private boolean deleteRecursiveLocal(java.io.File file) {
+        if (file.isDirectory()) {
+            java.io.File[] children = file.listFiles();
+            if (children != null) {
+                for (java.io.File child : children) {
+                    if (!deleteRecursiveLocal(child)) return false;
+                }
+            }
+        }
+        return file.delete();
+    }
```

---

## Tóm tắt

| Hạng mục | Chi tiết |
|---|---|
| Sửa | `runBulkMove()`, `runBulkCopy()` — đổi tổng từ *số folder* sang *số file* |
| Thêm | 7 helper: `countFilesRecursive`, `throttledProgress`, `moveOneWithProgress`, `copyOneWithProgress`, `copyTreeWithProgress`, `isSubPathLocal`, `deleteRecursiveLocal` |
| Không đụng | `FileRepositoryImpl`, `MoveOperation`/`CopyOperation`, `MainViewModel` |

### Điểm cơ chế cần nhớ
- **Move cùng ổ vẫn nhanh:** `renameTo` thành công thì cộng luôn số file của cây đó vào bar (không copy lại vô ích). Chỉ move khác ổ / copy mới đi từng file.
- **Throttle 80ms:** tránh post quá nhiều lên UI thread gây lag ngược.
- **Đếm file chạy nền:** với cây lớn, việc đếm cũng tốn thời gian nên đặt trong `new Thread`, che bằng spinner "Đang chuẩn bị…".
- **Giữ nguyên rào an toàn:** chặn move/copy vào thư mục con của chính nó (`isSubPathLocal`), bỏ qua khi đích đã tồn tại.
- 
