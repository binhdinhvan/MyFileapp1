# So sánh thay đổi: bản gốc đầu tiên → bản hiện tại (MyFileapp1)
**Bản gốc (A):** `myfile-source-code__5_.zip` — chỉ có source, chưa build được.
**Bản hiện tại (B):** `MyFileapp1.zip` — đã là project Android Studio hoàn chỉnh + thêm nhiều tính năng.
> So sánh theo đường dẫn tương đối trong cây `app/src/main`. Đã loại các thư mục sinh tự động: `.git/ .gradle/ .idea/ .qodo/ build/`.
## Tổng quan
| Nhóm | Số lượng |
|---|---|
| File **thêm mới** | 49 |
| File **bị xoá / đổi tên** | 2 |
| File **bị sửa** | 5 |
| File **giữ nguyên** | 18 |

---

## A. FILE THÊM MỚI
### A1. Khung project & build (mình thêm để build được APK)
| File | Mục đích |
|---|---|
| `.gitattributes` | Cấu hình Git (chuẩn hoá line-ending) |
| `.gitignore` | Bỏ qua file build/ide khi commit |
| `.github/workflows/build-apk.yml` | GitHub Actions: build APK tự động trên cloud |
| `HUONG_DAN_BUILD_APK.md` | Hướng dẫn build APK (3 cách) |
| `MEMBER_A_NOTES.txt` | Ghi chú module duyệt/điều hướng |
| `README_source.md` | Bản README gốc (đổi tên từ README.md) |
| `build.gradle.kts` | Script Gradle gốc (khai báo AGP) |
| `settings.gradle.kts` | Khai báo module :app + repo google()/mavenCentral() |
| `gradle.properties` | Cấu hình Gradle (AndroidX...) |
| `gradlew / gradlew.bat` | Gradle wrapper (chạy build không cần cài Gradle) |
| `gradle/wrapper/gradle-wrapper.jar` | Nhân của wrapper |
| `gradle/wrapper/gradle-wrapper.properties` | Phiên bản Gradle (8.5) |
| `local.properties` | Trỏ tới Android SDK trên máy bạn |
| `app/build.gradle.kts` | Cấu hình module app (sdk, deps, minSdk 24...) |
| `app/proguard-rules.pro` | Rule ProGuard (rỗng, placeholder) |
| `app/src/main/AndroidManifest.xml` | Manifest đầy đủ (gộp từ MANIFEST_ADDITIONS.txt) |

### A2. Java — module duyệt/điều hướng (bước trước)
| File | Mục đích |
|---|---|
| `app/.../ui/main/SortMode.java` | enum kiểu sắp xếp |
| `app/.../ui/main/FileListHelper.java` | Hàm sort + filter (tìm kiếm) |
| `app/.../data/storage/StorageHelper.java` | Liệt kê storage + quick folders |
| `app/.../data/storage/StorageVolumeItem.java` | Model 1 storage |
| `app/.../ui/storage/StorageAdapter.java` | Adapter danh sách storage trong drawer |

### A3. Java — tính năng bổ sung thêm sau (search, quick folder)
| File | Mục đích |
|---|---|
| `app/.../SearchActivity.java` | Màn hình tìm kiếm riêng |
| `app/.../data/storage/QuickFolderItem.java` | Model thư mục nhanh (Download/DCIM...) |
| `app/.../ui/storage/QuickFolderAdapter.java` | Adapter thư mục nhanh trong drawer |

### A4. Layout mới
| File | Mục đích |
|---|---|
| `app/.../layout/item_file_grid.xml` | Ô hiển thị dạng lưới (Grid) |
| `app/.../layout/item_storage.xml` | Dòng storage trong drawer |
| `app/.../layout/activity_search.xml` | Giao diện màn hình tìm kiếm |
| `app/.../layout/item_date_header.xml` | Tiêu đề nhóm theo ngày |
| `app/.../layout/item_quick_folder.xml` | Dòng thư mục nhanh |
| `app/.../layout/item_recent_search.xml` | Dòng lịch sử tìm kiếm |
| `app/.../layout/layout_popup_sort.xml` | Popup chọn sắp xếp + kiểu xem |

### A5. Drawable mới
| File | Mục đích |
|---|---|
| `ic_launcher.xml` | Icon app (vector) |
| `ic_history.xml` | Icon lịch sử |
| `bg_badge_folder/img/video/audio/file.xml` | Nền huy hiệu loại file |
| `bg_card_item.xml / bg_card_selected.xml` | Nền thẻ item (thường/đang chọn) |
| `bg_action_pill.xml / bg_icon_circle.xml` | Nền nút bo tròn |
| `bg_search_box.xml` | Nền ô tìm kiếm |
| `bg_storage_icon.xml` | Nền icon storage |
| `bg_toolbar_gradient.xml` | Nền gradient thanh công cụ |

### A6. values/
| File | Mục đích |
|---|---|
| `app/.../values/colors.xml` | Bảng màu |
| `app/.../values/strings.xml` | Chuỗi (app_name...) |
| `app/.../values/themes.xml` | Theme app |

---

## B. FILE BỊ XOÁ / ĐỔI TÊN
| File | Mục đích |
|---|---|
| `MANIFEST_ADDITIONS.txt` | Đã gộp nội dung vào app/src/main/AndroidManifest.xml |
| `README.md` | Đổi tên thành README_source.md |

---

## C. FILE BỊ SỬA (chi tiết theo dòng)
Mỗi file gồm: tóm tắt thay đổi + diff đầy đủ (bấm mở phần *Xem diff*). Ký hiệu diff: dòng bắt đầu bằng `+` là **thêm**, `-` là **xoá**; dòng `@@ -a,b +c,d @@` cho biết vị trí dòng ở bản cũ/mới.

### `app/src/main/java/com/example/myfile/MainActivity.java`  (+263 / -1)
- Thêm import: DrawerLayout, GravityCompat, GridLayoutManager, PopupWindow, TextWatcher, HorizontalScrollView, MaterialAlertDialogBuilder, và các helper (FileListHelper, SortMode, StorageHelper, StorageAdapter, QuickFolderAdapter).
- Thêm field: drawerLayout, breadcrumbScroll, breadcrumbContainer, sortMode (mặc định DATE_DESC), searchQuery, viewMode (0=List,1=Grid,2=Large Grid).
- onCreate: gắn nút btnMenu (mở drawer), btnSearchIcon (mở SearchActivity), btnSortMenu (mở popup sắp xếp); gọi setupBrowseFeatures().
- loadFiles(): thêm sort + filter theo searchQuery, nhóm file theo ngày (groupFilesByDate), cập nhật breadcrumb.
- Thêm hàng loạt method mới: setupBrowseFeatures, showSortMenu (PopupWindow), updateSortMode, updateViewMode (List/Grid/Large Grid qua GridLayoutManager + SpanSizeLookup), groupFilesByDate, updateBreadcrumb, addCrumb, dpToPx.

<details>
<summary><b>Xem diff đầy đủ — MainActivity.java</b></summary>

```diff
--- cmp/orig/myfile-project/app/src/main/java/com/example/myfile/MainActivity.java	2026-07-24 23:46:27.000000000 +0000
+++ cmp/new/MyFileapp1/app/src/main/java/com/example/myfile/MainActivity.java	2026-07-26 19:59:42.000000000 +0000
@@ -10,16 +10,24 @@
 import android.os.Environment;
 import android.provider.Settings;
 import android.view.View;
+import android.text.Editable;
+import android.text.TextWatcher;
 import android.widget.EditText;
+import android.widget.HorizontalScrollView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
+import android.widget.PopupWindow;
 import androidx.activity.OnBackPressedCallback;
 import androidx.appcompat.app.AppCompatActivity;
 import androidx.core.app.ActivityCompat;
 import androidx.core.content.ContextCompat;
 import androidx.core.content.FileProvider;
+import androidx.core.view.GravityCompat;
+import androidx.drawerlayout.widget.DrawerLayout;
+import androidx.recyclerview.widget.GridLayoutManager;
 import androidx.recyclerview.widget.LinearLayoutManager;
+import androidx.recyclerview.widget.GridLayoutManager;
 import androidx.recyclerview.widget.RecyclerView;
 import com.example.myfile.data.model.FileItem;
 import com.example.myfile.data.repository.FileRepository;
@@ -33,7 +41,13 @@
 import com.example.myfile.domain.operation.RenameOperation;
 import com.example.myfile.domain.operation.SoftDeleteOperation;
 import com.example.myfile.ui.main.FileAdapter;
+import com.example.myfile.ui.main.FileListHelper;
+import com.example.myfile.ui.main.SortMode;
+import com.example.myfile.data.storage.StorageHelper;
+import com.example.myfile.ui.storage.QuickFolderAdapter;
+import com.example.myfile.ui.storage.StorageAdapter;
 import com.google.android.material.snackbar.Snackbar;
+import com.google.android.material.dialog.MaterialAlertDialogBuilder;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
@@ -61,6 +75,13 @@
     private static final int BULK_MOVE_REQUEST_CODE = 202;
     private static final int BULK_COPY_REQUEST_CODE = 203;
 
+    private DrawerLayout drawerLayout;
+    private HorizontalScrollView breadcrumbScroll;
+    private LinearLayout breadcrumbContainer;
+    private SortMode sortMode = SortMode.DATE_DESC;
+    private String searchQuery = "";
+    private int viewMode = 0; // 0: List, 1: Grid (3 cols), 2: Large Grid (2 cols)
+
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
@@ -92,7 +113,9 @@
         findViewById(R.id.btnSelCopy).setOnClickListener(v -> bulkCopy());
         findViewById(R.id.btnTrash).setOnClickListener(v -> startActivity(new Intent(this, TrashActivity.class)));
         findViewById(R.id.btnBack).setOnClickListener(v -> navigateUp());
-
+        findViewById(R.id.btnSearchIcon).setOnClickListener(v -> startActivity(new Intent(this, SearchActivity.class)));
+        findViewById(R.id.btnSortMenu).setOnClickListener(v -> showSortMenu(v));
+        
         getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
             @Override
             public void handleOnBackPressed() {
@@ -108,6 +131,8 @@
                 }
             }
         });
+
+        setupBrowseFeatures();
     }
 
     private void checkPermissionAndLoad() {
@@ -191,6 +216,13 @@
         currentPath = path;
         tvCurrentPath.setText(path);
         List<FileItem> items = fileRepository.list(path);
+        items = FileListHelper.sort(items, sortMode);
+        items = FileListHelper.filter(items, searchQuery);
+        if (!path.equals(rootPath)) {
+            items = groupFilesByDate(items);
+        }
+        
+        updateBreadcrumb(path);
         adapter.updateData(items);
         emptyState.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
         findViewById(R.id.btnBack).setVisibility(path.equals(rootPath) ? View.GONE : View.VISIBLE);
@@ -207,6 +239,68 @@
         }
     }
 
+    private List<FileItem> groupFilesByDate(List<FileItem> original) {
+        if (original.isEmpty()) return original;
+        
+        List<FileItem> folders = new ArrayList<>();
+        List<FileItem> filesToGroup = new ArrayList<>();
+        for (FileItem item : original) {
+            if (item.isDirectory()) {
+                folders.add(item);
+            } else {
+                filesToGroup.add(item);
+            }
+        }
+        
+        // Sort files by date descending before grouping
+        java.util.Collections.sort(filesToGroup, (f1, f2) -> Long.compare(f2.getLastModified(), f1.getLastModified()));
+        
+        List<FileItem> finalItems = new ArrayList<>(folders);
+        
+        if (filesToGroup.isEmpty()) {
+            return finalItems;
+        }
+
+        java.util.Map<String, List<FileItem>> groups = new java.util.LinkedHashMap<>();
+        
+        java.util.Calendar cal = java.util.Calendar.getInstance();
+        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
+        cal.set(java.util.Calendar.MINUTE, 0);
+        cal.set(java.util.Calendar.SECOND, 0);
+        cal.set(java.util.Calendar.MILLISECOND, 0);
+        long todayStart = cal.getTimeInMillis();
+        long yesterdayStart = todayStart - 86400000L;
+        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault());
+        
+        for (FileItem item : filesToGroup) {
+            long time = item.getLastModified();
+            String groupName;
+            if (time >= todayStart) {
+                groupName = "Today";
+            } else if (time >= yesterdayStart) {
+                groupName = "Yesterday";
+            } else if (time >= todayStart - 7L * 86400000L) {
+                int days = (int) ((todayStart - time) / 86400000L) + 1;
+                groupName = days + " days ago";
+            } else {
+                groupName = sdf.format(new java.util.Date(time));
+            }
+            
+            if (!groups.containsKey(groupName)) {
+                groups.put(groupName, new ArrayList<>());
+            }
+            groups.get(groupName).add(item);
+        }
+        
+        for (java.util.Map.Entry<String, List<FileItem>> entry : groups.entrySet()) {
+            String title = entry.getKey() + "  |  " + entry.getValue().size() + " items";
+            finalItems.add(new FileItem(title));
+            finalItems.addAll(entry.getValue());
+        }
+        
+        return finalItems;
+    }
+
     @Override
     public void onItemClick(FileItem item) {
         if (item.isDirectory()) {
@@ -601,4 +695,172 @@
         intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
         startActivity(Intent.createChooser(intent, "Share " + item.getName()));
     }
+
+
+    private void setupBrowseFeatures() {
+
+        drawerLayout = findViewById(R.id.drawerLayout);
+        findViewById(R.id.btnMenu).setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
+
+        RecyclerView rvStorage = findViewById(R.id.recyclerViewStorage);
+        rvStorage.setLayoutManager(new LinearLayoutManager(this));
+        rvStorage.setAdapter(new StorageAdapter(StorageHelper.getStorages(this), storage -> {
+            rootPath = storage.getPath();
+            searchQuery = "";
+            loadFiles(rootPath);
+            drawerLayout.closeDrawer(GravityCompat.START);
+        }));
+
+        RecyclerView rvQuickFolders = findViewById(R.id.recyclerViewQuickFolders);
+        rvQuickFolders.setLayoutManager(new LinearLayoutManager(this));
+        rvQuickFolders.setAdapter(new QuickFolderAdapter(StorageHelper.getQuickFolders(), folder -> {
+            rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
+            searchQuery = "";
+            loadFiles(folder.getPath());
+            drawerLayout.closeDrawer(GravityCompat.START);
+        }));
+
+        breadcrumbScroll = findViewById(R.id.breadcrumbScroll);
+        breadcrumbContainer = findViewById(R.id.breadcrumbContainer);
+
+
+        if (currentPath != null) {
+            updateBreadcrumb(currentPath);
+        }
+    }
+
+    private void showSortMenu(View anchor) {
+        View popupView = getLayoutInflater().inflate(R.layout.layout_popup_sort, null);
+        PopupWindow popupWindow = new PopupWindow(popupView, 
+                LinearLayout.LayoutParams.WRAP_CONTENT, 
+                LinearLayout.LayoutParams.WRAP_CONTENT, true);
+        
+        popupView.findViewById(R.id.check_grid).setVisibility(viewMode == 1 ? View.VISIBLE : View.INVISIBLE);
+        popupView.findViewById(R.id.check_large_grid).setVisibility(viewMode == 2 ? View.VISIBLE : View.INVISIBLE);
+        popupView.findViewById(R.id.check_list).setVisibility(viewMode == 0 ? View.VISIBLE : View.INVISIBLE);
+
+        boolean isDesc = sortMode.name().endsWith("_DESC");
+        String field = sortMode.name().split("_")[0]; 
+
+        popupView.findViewById(R.id.check_name).setVisibility("NAME".equals(field) ? View.VISIBLE : View.INVISIBLE);
+        popupView.findViewById(R.id.check_size).setVisibility("SIZE".equals(field) ? View.VISIBLE : View.INVISIBLE);
+        popupView.findViewById(R.id.check_time).setVisibility("DATE".equals(field) ? View.VISIBLE : View.INVISIBLE);
+        popupView.findViewById(R.id.check_type).setVisibility("TYPE".equals(field) ? View.VISIBLE : View.INVISIBLE);
+
+        popupView.findViewById(R.id.check_forward).setVisibility(!isDesc ? View.VISIBLE : View.INVISIBLE);
+        popupView.findViewById(R.id.check_reverse).setVisibility(isDesc ? View.VISIBLE : View.INVISIBLE);
+
+        popupView.findViewById(R.id.menu_grid).setOnClickListener(v -> { viewMode = 1; updateViewMode(); popupWindow.dismiss(); });
+        popupView.findViewById(R.id.menu_list).setOnClickListener(v -> { viewMode = 0; updateViewMode(); popupWindow.dismiss(); });
+        popupView.findViewById(R.id.menu_large_grid).setOnClickListener(v -> { viewMode = 2; updateViewMode(); popupWindow.dismiss(); });
+
+        popupView.findViewById(R.id.menu_name).setOnClickListener(v -> { updateSortMode("NAME", isDesc); popupWindow.dismiss(); });
+        popupView.findViewById(R.id.menu_size).setOnClickListener(v -> { updateSortMode("SIZE", isDesc); popupWindow.dismiss(); });
+        popupView.findViewById(R.id.menu_time).setOnClickListener(v -> { updateSortMode("DATE", isDesc); popupWindow.dismiss(); });
+        popupView.findViewById(R.id.menu_type).setOnClickListener(v -> { updateSortMode("TYPE", isDesc); popupWindow.dismiss(); });
+
+        popupView.findViewById(R.id.menu_forward).setOnClickListener(v -> { updateSortMode(field, false); popupWindow.dismiss(); });
+        popupView.findViewById(R.id.menu_reverse).setOnClickListener(v -> { updateSortMode(field, true); popupWindow.dismiss(); });
+
+        popupWindow.setElevation(16f);
+        popupWindow.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
+        
+        int xOffset = -popupView.getMeasuredWidth() + anchor.getWidth();
+        popupWindow.showAsDropDown(anchor, xOffset, 0);
+    }
+    
+    private void updateSortMode(String field, boolean isDesc) {
+        if ("TYPE".equals(field)) field = "NAME"; 
+        String suffix = isDesc ? "_DESC" : "_ASC";
+        sortMode = SortMode.valueOf(field + suffix);
+        if (currentPath != null) {
+            loadFiles(currentPath);
+        }
+    }
+
+    private void updateViewMode() {
+        RecyclerView rv = findViewById(R.id.recyclerView);
+        
+        if (viewMode == 0) {
+            rv.setLayoutManager(new LinearLayoutManager(this));
+        } else if (viewMode == 1) {
+            GridLayoutManager glm = new GridLayoutManager(this, 3);
+            glm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
+                @Override
+                public int getSpanSize(int position) {
+                    if (adapter != null && adapter.getItemViewType(position) == 3) return 3;
+                    return 1;
+                }
+            });
+            rv.setLayoutManager(glm);
+        } else if (viewMode == 2) {
+            GridLayoutManager glm = new GridLayoutManager(this, 2);
+            glm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
+                @Override
+                public int getSpanSize(int position) {
+                    if (adapter != null && adapter.getItemViewType(position) == 3) return 2;
+                    return 1;
+                }
+            });
+            rv.setLayoutManager(glm);
+        }
+        
+        adapter.setViewMode(viewMode);
+    }
+
+    private void updateBreadcrumb(String path) {
+        if (breadcrumbContainer == null || rootPath == null) {
+            return;
+        }
+        breadcrumbContainer.removeAllViews();
+        addCrumb("Root", rootPath, true);
+        if (path != null && path.startsWith(rootPath) && path.length() > rootPath.length()) {
+            String rest = path.substring(rootPath.length());
+            if (rest.startsWith("/")) {
+                rest = rest.substring(1);
+            }
+            String[] parts = rest.split("/");
+            StringBuilder acc = new StringBuilder(rootPath);
+            for (String part : parts) {
+                if (part.isEmpty()) {
+                    continue;
+                }
+                acc.append("/").append(part);
+                addCrumb(part, acc.toString(), false);
+            }
+        }
+        if (breadcrumbScroll != null) {
+            breadcrumbScroll.post(() -> breadcrumbScroll.fullScroll(View.FOCUS_RIGHT));
+        }
+    }
+
+    private void addCrumb(String label, String targetPath, boolean isRoot) {
+        if (!isRoot) {
+            TextView sep = new TextView(this);
+            sep.setText("  \u203A  ");
+            sep.setTextColor(0xFF9E9E9E);
+            breadcrumbContainer.addView(sep);
+        }
+        TextView crumb = new TextView(this);
+        crumb.setText(label);
+        crumb.setTextColor(0xFF1976D2);
+        crumb.setTextSize(13);
+        crumb.setTypeface(null, android.graphics.Typeface.BOLD);
+        crumb.setMaxLines(1);
+        int pad = dpToPx(6);
+        crumb.setPadding(pad, pad, pad, pad);
+        android.util.TypedValue tv = new android.util.TypedValue();
+        getTheme().resolveAttribute(android.R.attr.selectableItemBackground, tv, true);
+        crumb.setBackgroundResource(tv.resourceId);
+        crumb.setOnClickListener(v -> {
+            if (!targetPath.equals(currentPath)) {
+                loadFiles(targetPath);
+            }
+        });
+        breadcrumbContainer.addView(crumb);
+    }
+
+    private int dpToPx(int dp) {
+        return (int) (dp * getResources().getDisplayMetrics().density);
+    }
 }
```

</details>

### `app/src/main/java/com/example/myfile/ui/main/FileAdapter.java`  (+117 / -49)
- Đổi sang đa kiểu view: RecyclerView.Adapter<RecyclerView.ViewHolder> với getItemViewType (3 = header ngày, list, grid).
- Thêm field viewMode + method setViewMode(int) thay cho cách cũ.
- onCreateViewHolder/onBindViewHolder xử lý header, và badge bằng drawable (bg_badge_*) + emoji thay cho màu nền phẳng.
- Thêm getBadgeLabel/getBadgeEmoji/getBadgeDrawable; badge dùng ContextCompat.getDrawable.
- Bỏ cách render cũ (setBackgroundColor phẳng, getBadgeText/getBadgeColor kiểu cũ).

<details>
<summary><b>Xem diff đầy đủ — FileAdapter.java</b></summary>

```diff
--- cmp/orig/myfile-project/app/src/main/java/com/example/myfile/ui/main/FileAdapter.java	2026-07-24 23:44:59.000000000 +0000
+++ cmp/new/MyFileapp1/app/src/main/java/com/example/myfile/ui/main/FileAdapter.java	2026-07-26 19:43:58.000000000 +0000
@@ -6,6 +6,7 @@
 import android.widget.CheckBox;
 import android.widget.TextView;
 import androidx.annotation.NonNull;
+import androidx.core.content.ContextCompat;
 import androidx.recyclerview.widget.RecyclerView;
 import com.example.myfile.R;
 import com.example.myfile.data.model.FileItem;
@@ -16,7 +17,7 @@
 import java.util.Locale;
 import java.util.Set;
 
-public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> {
+public class FileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
 
     public interface OnItemClickListener {
         void onItemClick(FileItem item);
@@ -28,6 +29,7 @@
     private final OnItemClickListener listener;
     private boolean selectionMode = false;
     private final Set<String> selectedPaths = new HashSet<>();
+    private int viewMode = 0; 
 
     public FileAdapter(List<FileItem> items, OnItemClickListener listener) {
         this.items = items;
@@ -39,6 +41,11 @@
         notifyDataSetChanged();
     }
 
+    public void setViewMode(int mode) {
+        this.viewMode = mode;
+        notifyDataSetChanged();
+    }
+
     public boolean isSelectionMode() {
         return selectionMode;
     }
@@ -71,45 +78,82 @@
         listener.onSelectionChanged(true, selectedPaths.size());
     }
 
+    @Override
+    public int getItemViewType(int position) {
+        if (items.get(position).isHeader()) return 3;
+        return viewMode;
+    }
+
     @NonNull
     @Override
-    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
-        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file, parent, false);
-        return new ViewHolder(view);
+    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
+        if (viewType == 3) {
+            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_date_header, parent, false);
+            return new HeaderViewHolder(view);
+        }
+        int res = (viewType == 0) ? R.layout.item_file : R.layout.item_file_grid;
+        View view = LayoutInflater.from(parent.getContext()).inflate(res, parent, false);
+        return new ItemViewHolder(view);
     }
 
     @Override
-    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
+    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
         FileItem item = items.get(position);
-        holder.tvName.setText(item.getName());
+        
+        if (getItemViewType(position) == 3) {
+            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
+            headerHolder.tvHeaderTitle.setText(item.getHeaderTitle());
+            return;
+        }
+        
+        ItemViewHolder itemHolder = (ItemViewHolder) holder;
+        itemHolder.tvName.setText(item.getName());
 
         if (item.isDirectory()) {
-            holder.tvName.setTextColor(0xFF1976D2);
-            holder.tvDetail.setText("Thu muc");
-            holder.tvBadge.setText("DIR");
-            holder.tvBadge.setBackgroundColor(0xFF1976D2);
+            itemHolder.tvName.setTextColor(0xFF1976D2);
+            String date = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(item.getLastModified());
+            int childCount = 0;
+            java.io.File f = new java.io.File(item.getPath());
+            if (f.exists() && f.isDirectory()) {
+                String[] children = f.list();
+                if (children != null) childCount = children.length;
+            }
+            String countText = childCount == 1 ? "1 item" : childCount + " items";
+            itemHolder.tvDetail.setText(date + "  |  " + countText);
+            itemHolder.tvBadge.setText("\uD83D\uDCC1");
+            itemHolder.tvBadge.setTextSize(22);
+            itemHolder.tvBadge.setBackground(
+                    ContextCompat.getDrawable(itemHolder.itemView.getContext(), R.drawable.bg_badge_folder));
         } else {
-            holder.tvName.setTextColor(0xFF212121);
-            String date = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(item.getLastModified());
+            itemHolder.tvName.setTextColor(0xFF212121);
+            String date = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(item.getLastModified());
             String sizeText = formatSize(item.getSize());
-            holder.tvDetail.setText(sizeText + " - " + date);
-            holder.tvBadge.setText(getBadgeText(item.getName()));
-            holder.tvBadge.setBackgroundColor(getBadgeColor(item.getName()));
+            itemHolder.tvDetail.setText(date + "  |  " + sizeText);
+            itemHolder.tvBadge.setText(getBadgeLabel(item.getName()));
+            itemHolder.tvBadge.setTextSize(22);
+            itemHolder.tvBadge.setBackground(
+                    ContextCompat.getDrawable(itemHolder.itemView.getContext(), getBadgeDrawable(item.getName())));
         }
 
         boolean isSelected = selectedPaths.contains(item.getPath());
-        holder.itemView.setBackgroundColor(isSelected ? 0xFFE3F2FD : 0xFFFFFFFF);
-        holder.tvCheck.setVisibility(selectionMode ? View.VISIBLE : View.GONE);
-        holder.tvCheck.setChecked(isSelected);
+        itemHolder.itemView.setAlpha(isSelected ? 0.85f : 1.0f);
+
+        if (isSelected) {
+            itemHolder.itemView.setBackgroundResource(R.drawable.bg_card_selected);
+        } else {
+            itemHolder.itemView.setBackgroundResource(R.drawable.bg_card_item);
+        }
+        itemHolder.tvCheck.setVisibility(selectionMode ? View.VISIBLE : View.GONE);
+        itemHolder.tvCheck.setChecked(isSelected);
 
-        holder.itemView.setOnClickListener(v -> {
+        itemHolder.itemView.setOnClickListener(v -> {
             if (selectionMode) {
                 toggleSelection(item);
             } else {
                 listener.onItemClick(item);
             }
         });
-        holder.itemView.setOnLongClickListener(v -> {
+        itemHolder.itemView.setOnLongClickListener(v -> {
             if (selectionMode) {
                 toggleSelection(item);
             } else {
@@ -124,40 +168,55 @@
         return items.size();
     }
 
-    private String getBadgeText(String name) {
+    private String getBadgeLabel(String name) {
         String lower = name.toLowerCase(Locale.getDefault());
-        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png")) {
-            return "IMG";
-        } else if (lower.endsWith(".mp4") || lower.endsWith(".avi") || lower.endsWith(".mkv")) {
-            return "VID";
-        } else if (lower.endsWith(".zip") || lower.endsWith(".rar")) {
-            return "ZIP";
+        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".gif") || lower.endsWith(".webp")) {
+            return "\uD83D\uDDBC";
+        } else if (lower.endsWith(".mp4") || lower.endsWith(".avi") || lower.endsWith(".mkv") || lower.endsWith(".mov")) {
+            return "\uD83C\uDFAC";
+        } else if (lower.endsWith(".zip") || lower.endsWith(".rar") || lower.endsWith(".7z")) {
+            return "\uD83D\uDCE6";
         } else if (lower.endsWith(".pdf")) {
-            return "PDF";
-        } else if (lower.endsWith(".txt")) {
-            return "TXT";
-        } else if (lower.endsWith(".mp3") || lower.endsWith(".wav")) {
-            return "MP3";
-        }
-        return "FILE";
+            return "\uD83D\uDCCB";
+        } else if (lower.endsWith(".txt") || lower.endsWith(".md")) {
+            return "\uD83D\uDCDD";
+        } else if (lower.endsWith(".mp3") || lower.endsWith(".wav") || lower.endsWith(".flac") || lower.endsWith(".ogg")) {
+            return "\uD83C\uDFB5";
+        } else if (lower.endsWith(".apk")) {
+            return "\uD83D\uDCF1";
+        } else if (lower.endsWith(".doc") || lower.endsWith(".docx")) {
+            return "\uD83D\uDCC3";
+        } else if (lower.endsWith(".xls") || lower.endsWith(".xlsx") || lower.endsWith(".csv")) {
+            return "\uD83D\uDCCA";
+        }
+        return "\uD83D\uDCC4";
+    }
+
+    
+    private String getBadgeEmoji(String name) {
+        return getBadgeLabel(name);
     }
 
-    private int getBadgeColor(String name) {
+    private int getBadgeDrawable(String name) {
         String lower = name.toLowerCase(Locale.getDefault());
-        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png")) {
-            return 0xFF43A047;
-        } else if (lower.endsWith(".mp4") || lower.endsWith(".avi") || lower.endsWith(".mkv")) {
-            return 0xFF8E24AA;
-        } else if (lower.endsWith(".zip") || lower.endsWith(".rar")) {
-            return 0xFFFB8C00;
-        } else if (lower.endsWith(".pdf")) {
-            return 0xFFE53935;
-        } else if (lower.endsWith(".txt")) {
-            return 0xFF607D8B;
-        } else if (lower.endsWith(".mp3") || lower.endsWith(".wav")) {
-            return 0xFFD81B60;
+        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".gif") || lower.endsWith(".webp")) {
+            return R.drawable.bg_badge_img;
+        } else if (lower.endsWith(".mp4") || lower.endsWith(".avi") || lower.endsWith(".mkv") || lower.endsWith(".mov")) {
+            return R.drawable.bg_badge_video;
+        } else if (lower.endsWith(".mp3") || lower.endsWith(".wav") || lower.endsWith(".flac") || lower.endsWith(".ogg")) {
+            return R.drawable.bg_badge_audio;
         }
-        return 0xFF90A4AE;
+        return R.drawable.bg_badge_file;
+    }
+
+    
+    private String getBadgeText(String name) {
+        return getBadgeEmoji(name);
+    }
+
+    
+    private int getBadgeColor(String name) {
+        return 0xFFFFF3E0;
     }
 
     private String formatSize(long bytes) {
@@ -169,13 +228,13 @@
         return String.format(Locale.getDefault(), "%.1f %s", bytes / Math.pow(1024, exp), unit);
     }
 
-    static class ViewHolder extends RecyclerView.ViewHolder {
+    static class ItemViewHolder extends RecyclerView.ViewHolder {
         TextView tvBadge;
         TextView tvName;
         TextView tvDetail;
         CheckBox tvCheck;
 
-        ViewHolder(View itemView) {
+        ItemViewHolder(View itemView) {
             super(itemView);
             tvBadge = itemView.findViewById(R.id.tvBadge);
             tvName = itemView.findViewById(R.id.tvName);
@@ -183,4 +242,13 @@
             tvCheck = itemView.findViewById(R.id.tvCheck);
         }
     }
+    
+    static class HeaderViewHolder extends RecyclerView.ViewHolder {
+        TextView tvHeaderTitle;
+        
+        HeaderViewHolder(View itemView) {
+            super(itemView);
+            tvHeaderTitle = itemView.findViewById(R.id.tvHeaderTitle);
+        }
+    }
 }
```

</details>

### `app/src/main/java/com/example/myfile/data/model/FileItem.java`  (+22 / -0)
- Thêm 2 field: isHeader, headerTitle (phục vụ nhóm theo ngày).
- Thêm constructor mới FileItem(String headerTitle) để tạo dòng tiêu đề.
- Thêm getter isHeader() và getHeaderTitle(). Không xoá gì.

<details>
<summary><b>Xem diff đầy đủ — FileItem.java</b></summary>

```diff
--- cmp/orig/myfile-project/app/src/main/java/com/example/myfile/data/model/FileItem.java	2026-07-24 13:13:14.000000000 +0000
+++ cmp/new/MyFileapp1/app/src/main/java/com/example/myfile/data/model/FileItem.java	2026-07-26 19:38:14.000000000 +0000
@@ -8,6 +8,8 @@
     private final boolean isDirectory;
     private final long size;
     private final long lastModified;
+    private final boolean isHeader;
+    private final String headerTitle;
 
     public FileItem(String name, String path, boolean isDirectory, long size, long lastModified) {
         this.name = name;
@@ -15,6 +17,18 @@
         this.isDirectory = isDirectory;
         this.size = size;
         this.lastModified = lastModified;
+        this.isHeader = false;
+        this.headerTitle = null;
+    }
+    
+    public FileItem(String headerTitle) {
+        this.name = "";
+        this.path = "";
+        this.isDirectory = false;
+        this.size = 0;
+        this.lastModified = 0;
+        this.isHeader = true;
+        this.headerTitle = headerTitle;
     }
 
     public static FileItem fromFile(File file) {
@@ -45,4 +59,12 @@
     public long getLastModified() {
         return lastModified;
     }
+    
+    public boolean isHeader() {
+        return isHeader;
+    }
+    
+    public String getHeaderTitle() {
+        return headerTitle;
+    }
 }
```

</details>

### `app/src/main/res/layout/activity_main.xml`  (+437 / -165 (viết lại gần như toàn bộ))
- Bọc toàn bộ trong <DrawerLayout id=drawerLayout>; thêm panel drawer chứa recyclerViewStorage + recyclerViewQuickFolders.
- Header đổi sang nền gradient; thêm btnMenu (☰), btnSearchIcon, btnSortMenu.
- Thêm breadcrumb (breadcrumbScroll + breadcrumbContainer).
- Giữ nguyên các id chức năng cũ: recyclerView, emptyState, normalToolbar, selectionToolbar, btnBack, btnPaste, btnSel*, btnNewFile/Folder, btnSelectMode, btnTrash, tvCurrentPath, tvSelectionCount.

<details>
<summary><b>Xem diff đầy đủ — activity_main.xml</b></summary>

```diff
--- cmp/orig/myfile-project/app/src/main/res/layout/activity_main.xml	2026-07-25 00:15:34.000000000 +0000
+++ cmp/new/MyFileapp1/app/src/main/res/layout/activity_main.xml	2026-07-26 19:55:40.000000000 +0000
@@ -1,217 +1,489 @@
 <?xml version="1.0" encoding="utf-8"?>
-<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
-    android:orientation="vertical"
+<androidx.drawerlayout.widget.DrawerLayout xmlns:android="http://schemas.android.com/apk/res/android"
+    android:id="@+id/drawerLayout"
     android:layout_width="match_parent"
     android:layout_height="match_parent">
 
+    
     <LinearLayout
+        android:orientation="vertical"
         android:layout_width="match_parent"
-        android:layout_height="wrap_content"
-        android:orientation="horizontal"
-        android:gravity="center_vertical"
-        android:background="#1976D2"
-        android:padding="16dp"
-        android:elevation="4dp">
-
-        <TextView
-            android:id="@+id/btnBack"
-            android:layout_width="40dp"
-            android:layout_height="40dp"
-            android:layout_marginEnd="8dp"
-            android:gravity="center"
-            android:text="&lt;"
-            android:textColor="#FFFFFF"
-            android:textSize="24sp"
-            android:textStyle="bold"
-            android:background="?attr/selectableItemBackgroundBorderless"
-            android:visibility="gone" />
+        android:layout_height="match_parent"
+        android:background="@color/bg_surface">
 
+        
         <LinearLayout
-            android:layout_width="wrap_content"
+            android:layout_width="match_parent"
             android:layout_height="wrap_content"
-            android:orientation="vertical">
+            android:orientation="horizontal"
+            android:gravity="center_vertical"
+            android:background="@color/blue_primary"
+            android:paddingStart="8dp"
+            android:paddingEnd="12dp"
+            android:paddingTop="12dp"
+            android:paddingBottom="12dp"
+            android:elevation="6dp">
 
             <TextView
-                android:layout_width="wrap_content"
-                android:layout_height="wrap_content"
-                android:text="My File"
+                android:id="@+id/btnMenu"
+                android:layout_width="40dp"
+                android:layout_height="40dp"
+                android:gravity="center"
+                android:text="&#9776;"
                 android:textColor="#FFFFFF"
                 android:textSize="20sp"
-                android:textStyle="bold" />
+                android:background="?attr/selectableItemBackgroundBorderless" />
 
             <TextView
-                android:layout_width="wrap_content"
+                android:id="@+id/btnBack"
+                android:layout_width="40dp"
+                android:layout_height="40dp"
+                android:gravity="center"
+                android:text="&#8592;"
+                android:textColor="#FFFFFF"
+                android:textSize="22sp"
+                android:textStyle="bold"
+                android:background="?attr/selectableItemBackgroundBorderless"
+                android:visibility="gone" />
+
+            <LinearLayout
+                android:layout_width="0dp"
                 android:layout_height="wrap_content"
-                android:text="Quan ly file thiet bi"
-                android:textColor="#BBDEFB"
-                android:textSize="12sp"
-                android:layout_marginTop="2dp" />
+                android:layout_weight="1"
+                android:orientation="vertical"
+                android:layout_marginStart="4dp">
+
+                <TextView
+                    android:layout_width="wrap_content"
+                    android:layout_height="wrap_content"
+                    android:text="My File"
+                    android:textColor="#FFFFFF"
+                    android:textSize="19sp"
+                    android:textStyle="bold" />
+
+                <TextView
+                    android:layout_width="wrap_content"
+                    android:layout_height="wrap_content"
+                    android:text="File Manager"
+                    android:textColor="#BBDEFB"
+                    android:textSize="11sp"
+                    android:layout_marginTop="1dp" />
+            </LinearLayout>
+
+            <TextView
+                android:id="@+id/btnSearchIcon"
+                android:layout_width="40dp"
+                android:layout_height="40dp"
+                android:gravity="center"
+                android:text="&#128269;"
+                android:textSize="18sp"
+                android:textColor="#FFFFFF"
+                android:background="?attr/selectableItemBackgroundBorderless"
+                android:contentDescription="Search" />
+                
+            <TextView
+                android:id="@+id/btnSortMenu"
+                android:layout_width="40dp"
+                android:layout_height="40dp"
+                android:gravity="center"
+                android:text="&#8645;"
+                android:textSize="20sp"
+                android:textColor="#FFFFFF"
+                android:background="?attr/selectableItemBackgroundBorderless"
+                android:contentDescription="Sort and View" />
         </LinearLayout>
-    </LinearLayout>
 
-    <TextView
-        android:id="@+id/tvCurrentPath"
-        android:layout_width="match_parent"
-        android:layout_height="wrap_content"
-        android:padding="10dp"
-        android:background="#F5F5F5"
-        android:textColor="#616161"
-        android:textSize="12sp"
-        android:singleLine="true"
-        android:ellipsize="start" />
+        
+        <HorizontalScrollView
+            android:id="@+id/breadcrumbScroll"
+            android:layout_width="match_parent"
+            android:layout_height="40dp"
+            android:background="#FFFFFF"
+            android:scrollbars="none"
+            android:fillViewport="true"
+            android:elevation="2dp">
 
-    <LinearLayout
-        android:id="@+id/selectionToolbar"
-        android:layout_width="match_parent"
-        android:layout_height="wrap_content"
-        android:orientation="horizontal"
-        android:gravity="center_vertical"
-        android:background="#1565C0"
-        android:padding="12dp"
-        android:visibility="gone">
+            <LinearLayout
+                android:id="@+id/breadcrumbContainer"
+                android:layout_width="wrap_content"
+                android:layout_height="match_parent"
+                android:orientation="horizontal"
+                android:gravity="center_vertical"
+                android:paddingStart="12dp"
+                android:paddingEnd="12dp" />
+        </HorizontalScrollView>
 
+        
         <TextView
-            android:id="@+id/tvSelectionCount"
-            android:layout_width="0dp"
+            android:id="@+id/tvCurrentPath"
+            android:layout_width="match_parent"
             android:layout_height="wrap_content"
-            android:layout_weight="1"
-            android:text="0 selected"
-            android:textColor="#FFFFFF"
-            android:textStyle="bold" />
+            android:textSize="12sp"
+            android:visibility="gone" />
 
-        <TextView
-            android:id="@+id/btnSelDelete"
-            android:layout_width="wrap_content"
-            android:layout_height="wrap_content"
-            android:padding="8dp"
-            android:text="Delete"
-            android:textColor="#FFFFFF"
-            android:background="?attr/selectableItemBackground" />
+        
 
-        <TextView
-            android:id="@+id/btnSelMove"
-            android:layout_width="wrap_content"
+        
+        <LinearLayout
+            android:id="@+id/selectionToolbar"
+            android:layout_width="match_parent"
             android:layout_height="wrap_content"
-            android:padding="8dp"
-            android:layout_marginStart="4dp"
-            android:text="Move"
-            android:textColor="#FFFFFF"
-            android:background="?attr/selectableItemBackground" />
+            android:orientation="horizontal"
+            android:gravity="center_vertical"
+            android:background="#1565C0"
+            android:paddingStart="16dp"
+            android:paddingEnd="8dp"
+            android:paddingTop="10dp"
+            android:paddingBottom="10dp"
+            android:visibility="gone">
 
-        <TextView
-            android:id="@+id/btnSelCopy"
-            android:layout_width="wrap_content"
-            android:layout_height="wrap_content"
-            android:padding="8dp"
-            android:layout_marginStart="4dp"
-            android:text="Copy"
-            android:textColor="#FFFFFF"
-            android:background="?attr/selectableItemBackground" />
+            <TextView
+                android:id="@+id/tvSelectionCount"
+                android:layout_width="0dp"
+                android:layout_height="wrap_content"
+                android:layout_weight="1"
+                android:text="0 selected"
+                android:textColor="#FFFFFF"
+                android:textStyle="bold"
+                android:textSize="14sp" />
 
-        <TextView
-            android:id="@+id/btnSelCancel"
-            android:layout_width="wrap_content"
-            android:layout_height="wrap_content"
-            android:padding="8dp"
-            android:layout_marginStart="4dp"
-            android:text="Cancel"
-            android:textColor="#FFFFFF"
-            android:background="?attr/selectableItemBackground" />
+            <TextView
+                android:id="@+id/btnSelDelete"
+                android:layout_width="wrap_content"
+                android:layout_height="36dp"
+                android:gravity="center"
+                android:paddingStart="12dp"
+                android:paddingEnd="12dp"
+                android:text="Delete"
+                android:textColor="#FF8A80"
+                android:textSize="12sp"
+                android:textStyle="bold"
+                android:background="?attr/selectableItemBackground" />
 
-    </LinearLayout>
+            <TextView
+                android:id="@+id/btnSelMove"
+                android:layout_width="wrap_content"
+                android:layout_height="36dp"
+                android:gravity="center"
+                android:paddingStart="10dp"
+                android:paddingEnd="10dp"
+                android:text="Move"
+                android:textColor="#FFD180"
+                android:textSize="12sp"
+                android:textStyle="bold"
+                android:background="?attr/selectableItemBackground" />
 
-    <LinearLayout
-        android:id="@+id/normalToolbar"
-        android:layout_width="match_parent"
-        android:layout_height="wrap_content"
-        android:orientation="horizontal"
-        android:gravity="end">
-
-        <ImageView
-            android:id="@+id/btnTrash"
-            android:layout_width="24dp"
-            android:layout_height="24dp"
-            android:layout_margin="12dp"
-            android:src="@drawable/ic_delete"
-            android:background="?attr/selectableItemBackground"
-            android:contentDescription="Trash" />
+            <TextView
+                android:id="@+id/btnSelCopy"
+                android:layout_width="wrap_content"
+                android:layout_height="36dp"
+                android:gravity="center"
+                android:paddingStart="10dp"
+                android:paddingEnd="10dp"
+                android:text="Copy"
+                android:textColor="#80D8FF"
+                android:textSize="12sp"
+                android:textStyle="bold"
+                android:background="?attr/selectableItemBackground" />
 
-        <TextView
-            android:id="@+id/btnSelectMode"
-            android:layout_width="wrap_content"
-            android:layout_height="wrap_content"
-            android:padding="12dp"
-            android:text="Select"
-            android:textColor="#7B1FA2"
-            android:textStyle="bold"
-            android:background="?attr/selectableItemBackground" />
+            <TextView
+                android:id="@+id/btnSelCancel"
+                android:layout_width="36dp"
+                android:layout_height="36dp"
+                android:gravity="center"
+                android:text="&#10005;"
+                android:textColor="#FFFFFF"
+                android:textSize="16sp"
+                android:background="?attr/selectableItemBackgroundBorderless" />
+        </LinearLayout>
 
-        <TextView
-            android:id="@+id/btnPaste"
-            android:layout_width="wrap_content"
+        
+        <LinearLayout
+            android:id="@+id/normalToolbar"
+            android:layout_width="match_parent"
             android:layout_height="wrap_content"
-            android:padding="12dp"
-            android:text="Paste"
-            android:textColor="#43A047"
-            android:textStyle="bold"
-            android:background="?attr/selectableItemBackground" />
+            android:orientation="horizontal"
+            android:gravity="end|center_vertical"
+            android:background="#FFFFFF"
+            android:paddingEnd="8dp"
+            android:paddingTop="4dp"
+            android:paddingBottom="4dp">
 
-        <TextView
-            android:id="@+id/btnNewFile"
-            android:layout_width="wrap_content"
-            android:layout_height="wrap_content"
-            android:padding="12dp"
-            android:text="+ New File"
-            android:textColor="#FB8C00"
-            android:textStyle="bold"
-            android:background="?attr/selectableItemBackground" />
+            <TextView
+                android:id="@+id/btnSelectMode"
+                android:layout_width="wrap_content"
+                android:layout_height="32dp"
+                android:gravity="center"
+                android:paddingStart="10dp"
+                android:paddingEnd="10dp"
+                android:text="Select"
+                android:textColor="#7B1FA2"
+                android:textSize="12sp"
+                android:background="@drawable/bg_action_pill" />
 
-        <TextView
-            android:id="@+id/btnNewFolder"
-            android:layout_width="wrap_content"
-            android:layout_height="wrap_content"
-            android:padding="12dp"
-            android:text="+ New Folder"
-            android:textColor="#1976D2"
-            android:textStyle="bold"
-            android:background="?attr/selectableItemBackground" />
-    </LinearLayout>
+            <TextView
+                android:id="@+id/btnPaste"
+                android:layout_width="wrap_content"
+                android:layout_height="32dp"
+                android:gravity="center"
+                android:paddingStart="10dp"
+                android:paddingEnd="10dp"
+                android:layout_marginStart="6dp"
+                android:text="Paste"
+                android:textColor="#2E7D32"
+                android:textSize="12sp"
+                android:background="@drawable/bg_action_pill" />
 
-    <FrameLayout
-        android:layout_width="match_parent"
-        android:layout_height="0dp"
-        android:layout_weight="1">
+            <TextView
+                android:id="@+id/btnNewFile"
+                android:layout_width="wrap_content"
+                android:layout_height="32dp"
+                android:gravity="center"
+                android:paddingStart="10dp"
+                android:paddingEnd="10dp"
+                android:layout_marginStart="6dp"
+                android:text="+ File"
+                android:textColor="#E65100"
+                android:textSize="12sp"
+                android:background="@drawable/bg_action_pill" />
+
+            <TextView
+                android:id="@+id/btnNewFolder"
+                android:layout_width="wrap_content"
+                android:layout_height="32dp"
+                android:gravity="center"
+                android:paddingStart="10dp"
+                android:paddingEnd="10dp"
+                android:layout_marginStart="6dp"
+                android:text="+ Folder"
+                android:textColor="@color/blue_primary"
+                android:textSize="12sp"
+                android:background="@drawable/bg_action_pill" />
+        </LinearLayout>
 
-        <androidx.recyclerview.widget.RecyclerView
-            android:id="@+id/recyclerView"
+        
+        <FrameLayout
             android:layout_width="match_parent"
-            android:layout_height="match_parent" />
+            android:layout_height="0dp"
+            android:layout_weight="1">
+
+            <androidx.recyclerview.widget.RecyclerView
+                android:id="@+id/recyclerView"
+                android:layout_width="match_parent"
+                android:layout_height="match_parent"
+                android:paddingStart="12dp"
+                android:paddingEnd="12dp"
+                android:paddingTop="8dp"
+                android:paddingBottom="8dp"
+                android:clipToPadding="false" />
+
+            
+            <LinearLayout
+                android:id="@+id/emptyState"
+                android:layout_width="wrap_content"
+                android:layout_height="wrap_content"
+                android:layout_gravity="center"
+                android:orientation="vertical"
+                android:gravity="center"
+                android:visibility="gone">
+
+                <TextView
+                    android:layout_width="wrap_content"
+                    android:layout_height="wrap_content"
+                    android:text="&#128194;"
+                    android:textSize="56sp" />
+
+                <TextView
+                    android:layout_width="wrap_content"
+                    android:layout_height="wrap_content"
+                    android:layout_marginTop="12dp"
+                    android:text="Empty folder"
+                    android:textColor="@color/text_secondary"
+                    android:textSize="17sp"
+                    android:textStyle="bold" />
+
+                <TextView
+                    android:layout_width="wrap_content"
+                    android:layout_height="wrap_content"
+                    android:layout_marginTop="4dp"
+                    android:text="No files or folders here"
+                    android:textColor="@color/text_hint"
+                    android:textSize="13sp" />
+            </LinearLayout>
+        </FrameLayout>
+
+    </LinearLayout>
 
+    
+    <LinearLayout
+        android:layout_width="300dp"
+        android:layout_height="match_parent"
+        android:layout_gravity="start"
+        android:orientation="vertical"
+        android:background="@color/bg_surface">
+
+        
         <LinearLayout
-            android:id="@+id/emptyState"
-            android:layout_width="wrap_content"
+            android:layout_width="match_parent"
             android:layout_height="wrap_content"
-            android:layout_gravity="center"
             android:orientation="vertical"
-            android:gravity="center"
-            android:visibility="gone">
+            android:background="@color/blue_primary"
+            android:paddingStart="20dp"
+            android:paddingEnd="20dp"
+            android:paddingTop="36dp"
+            android:paddingBottom="20dp">
+
+            <TextView
+                android:layout_width="54dp"
+                android:layout_height="54dp"
+                android:gravity="center"
+                android:text="&#128193;"
+                android:textSize="28sp"
+                android:background="@drawable/bg_icon_circle" />
 
             <TextView
                 android:layout_width="wrap_content"
                 android:layout_height="wrap_content"
-                android:text="Thu muc trong"
-                android:textColor="#9E9E9E"
-                android:textSize="16sp"
+                android:layout_marginTop="12dp"
+                android:text="My File"
+                android:textColor="#FFFFFF"
+                android:textSize="20sp"
                 android:textStyle="bold" />
 
             <TextView
                 android:layout_width="wrap_content"
                 android:layout_height="wrap_content"
-                android:text="Khong co file hoac thu muc nao"
-                android:textColor="#BDBDBD"
-                android:textSize="13sp"
-                android:layout_marginTop="4dp" />
+                android:layout_marginTop="2dp"
+                android:text="File Manager"
+                android:textColor="#BBDEFB"
+                android:textSize="12sp" />
         </LinearLayout>
-    </FrameLayout>
 
-</LinearLayout>
+        
+        <ScrollView
+            android:layout_width="match_parent"
+            android:layout_height="0dp"
+            android:layout_weight="1"
+            android:scrollbars="none"
+            android:background="@color/bg_surface">
+
+            <LinearLayout
+                android:layout_width="match_parent"
+                android:layout_height="wrap_content"
+                android:orientation="vertical"
+                android:paddingBottom="16dp">
+
+                
+                <TextView
+                    android:layout_width="match_parent"
+                    android:layout_height="wrap_content"
+                    android:text="STORAGE"
+                    android:textColor="@color/text_hint"
+                    android:textSize="10sp"
+                    android:textStyle="bold"
+                    android:paddingStart="20dp"
+                    android:paddingEnd="20dp"
+                    android:paddingTop="16dp"
+                    android:paddingBottom="4dp"
+                    android:letterSpacing="0.12" />
+
+                <androidx.recyclerview.widget.RecyclerView
+                    android:id="@+id/recyclerViewStorage"
+                    android:layout_width="match_parent"
+                    android:layout_height="wrap_content"
+                    android:nestedScrollingEnabled="false"
+                    android:paddingStart="10dp"
+                    android:paddingEnd="10dp"
+                    android:clipToPadding="false" />
+
+                
+                <View
+                    android:layout_width="match_parent"
+                    android:layout_height="1dp"
+                    android:layout_marginStart="20dp"
+                    android:layout_marginEnd="20dp"
+                    android:layout_marginTop="10dp"
+                    android:layout_marginBottom="4dp"
+                    android:background="@color/divider" />
+
+                
+                <TextView
+                    android:layout_width="match_parent"
+                    android:layout_height="wrap_content"
+                    android:text="QUICK ACCESS"
+                    android:textColor="@color/text_hint"
+                    android:textSize="10sp"
+                    android:textStyle="bold"
+                    android:paddingStart="20dp"
+                    android:paddingEnd="20dp"
+                    android:paddingTop="8dp"
+                    android:paddingBottom="4dp"
+                    android:letterSpacing="0.12" />
+
+                <androidx.recyclerview.widget.RecyclerView
+                    android:id="@+id/recyclerViewQuickFolders"
+                    android:layout_width="match_parent"
+                    android:layout_height="wrap_content"
+                    android:nestedScrollingEnabled="false"
+                    android:paddingStart="10dp"
+                    android:paddingEnd="10dp"
+                    android:clipToPadding="false" />
+
+                <View
+                    android:layout_width="match_parent"
+                    android:layout_height="1dp"
+                    android:layout_marginStart="20dp"
+                    android:layout_marginEnd="20dp"
+                    android:layout_marginTop="10dp"
+                    android:layout_marginBottom="4dp"
+                    android:background="@color/divider" />
+
+                <TextView
+                    android:layout_width="match_parent"
+                    android:layout_height="wrap_content"
+                    android:text="TOOLS"
+                    android:textColor="@color/text_hint"
+                    android:textSize="10sp"
+                    android:textStyle="bold"
+                    android:paddingStart="20dp"
+                    android:paddingEnd="20dp"
+                    android:paddingTop="8dp"
+                    android:paddingBottom="4dp"
+                    android:letterSpacing="0.12" />
+
+                <LinearLayout
+                    android:id="@+id/btnTrash"
+                    android:layout_width="match_parent"
+                    android:layout_height="48dp"
+                    android:layout_marginStart="10dp"
+                    android:layout_marginEnd="10dp"
+                    android:orientation="horizontal"
+                    android:gravity="center_vertical"
+                    android:background="?attr/selectableItemBackground"
+                    android:paddingStart="12dp"
+                    android:paddingEnd="12dp">
+                    
+                    <TextView
+                        android:layout_width="36dp"
+                        android:layout_height="36dp"
+                        android:gravity="center"
+                        android:text="&#128465;"
+                        android:textSize="20sp"
+                        android:background="@drawable/bg_icon_circle" />
+                        
+                    <TextView
+                        android:layout_width="wrap_content"
+                        android:layout_height="wrap_content"
+                        android:layout_marginStart="12dp"
+                        android:text="Trash"
+                        android:textColor="@color/black"
+                        android:textSize="14sp"
+                        android:textStyle="bold" />
+                </LinearLayout>
+
+            </LinearLayout>
+        </ScrollView>
+
+    </LinearLayout>
+
+</androidx.drawerlayout.widget.DrawerLayout>
```

</details>

### `app/src/main/res/layout/item_file.xml`  (+34 / -20)
- Item chuyển sang dạng thẻ (card): nền @drawable/bg_card_item + elevation, margin dưới 6dp.
- Badge to hơn (48dp, emoji 22sp) dùng @drawable/bg_badge_folder thay nền phẳng #90A4AE.
- Màu chữ dùng @color/... ; tvDetail thêm maxLines/ellipsize.
- Bỏ đường kẻ ngăn cách (View 1dp) cũ; thêm mũi tê n › ở cuối dòng.

<details>
<summary><b>Xem diff đầy đủ — item_file.xml</b></summary>

```diff
--- cmp/orig/myfile-project/app/src/main/res/layout/item_file.xml	2026-07-24 23:47:03.000000000 +0000
+++ cmp/new/MyFileapp1/app/src/main/res/layout/item_file.xml	2026-07-26 18:54:48.000000000 +0000
@@ -3,34 +3,42 @@
     android:orientation="vertical"
     android:layout_width="match_parent"
     android:layout_height="wrap_content"
-    android:background="?attr/selectableItemBackground">
+    android:layout_marginBottom="6dp"
+    android:background="@drawable/bg_card_item"
+    android:elevation="2dp">
 
     <LinearLayout
         android:orientation="horizontal"
         android:layout_width="match_parent"
         android:layout_height="wrap_content"
-        android:padding="16dp"
-        android:gravity="center_vertical">
+        android:paddingStart="14dp"
+        android:paddingEnd="14dp"
+        android:paddingTop="12dp"
+        android:paddingBottom="12dp"
+        android:gravity="center_vertical"
+        android:background="?attr/selectableItemBackground">
 
         <CheckBox
             android:id="@+id/tvCheck"
             android:layout_width="wrap_content"
             android:layout_height="wrap_content"
+            android:layout_marginEnd="4dp"
             android:clickable="false"
             android:focusable="false"
             android:visibility="gone" />
 
+        
         <TextView
             android:id="@+id/tvBadge"
-            android:layout_width="44dp"
-            android:layout_height="44dp"
-            android:layout_marginStart="8dp"
+            android:layout_width="48dp"
+            android:layout_height="48dp"
             android:gravity="center"
-            android:textSize="10sp"
+            android:textSize="22sp"
             android:textStyle="bold"
-            android:textColor="#FFFFFF"
-            android:background="#90A4AE" />
+            android:textColor="@color/badge_folder_text"
+            android:background="@drawable/bg_badge_folder" />
 
+        
         <LinearLayout
             android:orientation="vertical"
             android:layout_width="0dp"
@@ -42,9 +50,9 @@
                 android:id="@+id/tvName"
                 android:layout_width="match_parent"
                 android:layout_height="wrap_content"
-                android:textSize="16sp"
+                android:textSize="15sp"
                 android:textStyle="bold"
-                android:textColor="#212121"
+                android:textColor="@color/black"
                 android:maxLines="1"
                 android:ellipsize="end" />
 
@@ -52,16 +60,22 @@
                 android:id="@+id/tvDetail"
                 android:layout_width="match_parent"
                 android:layout_height="wrap_content"
-                android:textSize="13sp"
-                android:textColor="#757575"
-                android:layout_marginTop="2dp" />
+                android:textSize="12sp"
+                android:textColor="@color/text_secondary"
+                android:layout_marginTop="2dp"
+                android:maxLines="1"
+                android:ellipsize="end" />
         </LinearLayout>
-    </LinearLayout>
 
-    <View
-        android:layout_width="match_parent"
-        android:layout_height="1dp"
-        android:layout_marginStart="16dp"
-        android:background="#EEEEEE" />
+        
+        <TextView
+            android:layout_width="wrap_content"
+            android:layout_height="wrap_content"
+            android:text="&#8250;"
+            android:textSize="22sp"
+            android:textColor="@color/text_hint"
+            android:layout_marginStart="4dp" />
+
+    </LinearLayout>
 
 </LinearLayout>
```

</details>

---

## D. FILE GIỮ NGUYÊN (không đổi 1 ký tự)
- `FolderPickerActivity.java`
- `TrashActivity.java`
- `data/repository/FileRepository.java`
- `data/repository/FileRepositoryImpl.java`
- `data/trash/TrashManager.java`
- `domain/operation/*.java (Copy, CreateFile, CreateFolder, Delete, FileOperation, Move, Rename, SoftDelete)`
- `res/drawable/ic_delete.xml`
- `res/layout/activity_folder_picker.xml`
- `res/layout/activity_trash.xml`
- `res/layout/dialog_progress.xml`
- `res/xml/file_paths.xml`
