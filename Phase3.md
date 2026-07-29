# Phase 3 — Event một lần (`Event<T>`) + Snackbar an toàn lifecycle

Tiếp nối Phase 2 (đã có `MainViewModel`). Không thêm thư viện. Kết quả thao tác file
được phát qua `LiveData<Event<T>>`; xoay màn hình **không** show lại Snackbar cũ.

Nguyên lý: `LiveData` luôn phát lại giá trị cuối khi observer re-subscribe (lúc xoay màn hình).
Bọc trong `Event<T>` với cờ `handled` → lần đọc thứ 2 trả `null` → Snackbar không lặp.

---

## 1. Wrapper `Event<T>`

**File mới: `app/src/main/java/com/example/myfile/utils/Event.java`**
```java
package com.example.myfile.utils;

/** Bọc dữ liệu chỉ tiêu thụ 1 lần (Snackbar, Toast, điều hướng...). */
public class Event<T> {

    private final T content;
    private boolean handled = false;

    public Event(T content) {
        this.content = content;
    }

    /** Trả nội dung đúng 1 lần; các lần sau (vd xoay màn hình) trả null. */
    public T getContentIfNotHandled() {
        if (handled) return null;
        handled = true;
        return content;
    }

    /** Xem nội dung mà không đánh dấu đã xử lý. */
    public T peekContent() {
        return content;
    }
}
```

---

## 2. Mở rộng `MainViewModel`

Đưa các thao tác file vào ViewModel (chạy nền), sau mỗi thao tác **phát 1 event** rồi reload.
Cần inject thêm `TrashManager` (đã có `@Provides` ở `AppModule` từ Phase 1).

**`MainViewModel.java`** (bản đầy đủ, thay thế bản Phase 2):
```java
package com.example.myfile.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myfile.data.model.FileItem;
import com.example.myfile.data.repository.FileRepository;
import com.example.myfile.data.trash.TrashManager;
import com.example.myfile.domain.operation.CopyOperation;
import com.example.myfile.domain.operation.CreateFileOperation;
import com.example.myfile.domain.operation.CreateFolderOperation;
import com.example.myfile.domain.operation.MoveOperation;
import com.example.myfile.domain.operation.RenameOperation;
import com.example.myfile.utils.Event;

import java.util.List;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class MainViewModel extends ViewModel {

    private final FileRepository repository;
    private final TrashManager trashManager;
    private final ExecutorService executor;

    private final MutableLiveData<List<FileItem>> files = new MutableLiveData<>();
    private final MutableLiveData<String> currentPath = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);

    // Event 1 lần
    private final MutableLiveData<Event<String>> message = new MutableLiveData<>();
    private final MutableLiveData<Event<String>> undoEvent = new MutableLiveData<>();

    // Nhớ tham số hiển thị để reload sau thao tác
    private SortMode lastSort = SortMode.DATE_DESC;
    private String lastQuery = "";

    @Inject
    public MainViewModel(FileRepository repository,
                         TrashManager trashManager,
                         ExecutorService executor) {
        this.repository = repository;
        this.trashManager = trashManager;
        this.executor = executor;
    }

    public LiveData<List<FileItem>> getFiles() { return files; }
    public LiveData<String> getCurrentPath() { return currentPath; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<Event<String>> getMessage() { return message; }
    public LiveData<Event<String>> getUndoEvent() { return undoEvent; }

    public void load(String path, SortMode sortMode, String searchQuery) {
        this.lastSort = sortMode;
        this.lastQuery = searchQuery;
        currentPath.postValue(path);
        reloadInternal(path);
    }

    private void reloadInternal(String path) {
        loading.postValue(true);
        executor.execute(() -> {
            List<FileItem> items = repository.list(path);
            items = FileListHelper.sort(items, lastSort);
            items = FileListHelper.filter(items, lastQuery);
            files.postValue(items);
            loading.postValue(false);
        });
    }

    private void reloadCurrent() {
        String path = currentPath.getValue();
        if (path != null) reloadInternal(path);
    }

    // ---- Thao tác file: chạy nền → phát event 1 lần → reload ----

    public void rename(String path, String newName) {
        executor.execute(() -> {
            boolean ok = new RenameOperation(repository, path, newName).execute();
            message.postValue(new Event<>(ok ? "Đã đổi tên" : "Đổi tên thất bại"));
            reloadCurrent();
        });
    }

    public void createFolder(String parentPath, String name) {
        executor.execute(() -> {
            boolean ok = new CreateFolderOperation(repository, parentPath, name).execute();
            message.postValue(new Event<>(ok ? "Đã tạo thư mục" : "Tạo thư mục thất bại"));
            reloadCurrent();
        });
    }

    public void createFile(String parentPath, String name) {
        executor.execute(() -> {
            boolean ok = new CreateFileOperation(repository, parentPath, name).execute();
            message.postValue(new Event<>(ok ? "Đã tạo file" : "Tạo file thất bại"));
            reloadCurrent();
        });
    }

    public void move(String source, String destFolder) {
        executor.execute(() -> {
            boolean ok = new MoveOperation(repository, source, destFolder).execute();
            message.postValue(new Event<>(ok ? "Đã di chuyển" : "Di chuyển thất bại"));
            reloadCurrent();
        });
    }

    public void copy(String source, String destFolder) {
        executor.execute(() -> {
            boolean ok = new CopyOperation(repository, source, destFolder).execute();
            message.postValue(new Event<>(ok ? "Đã sao chép" : "Sao chép thất bại"));
            reloadCurrent();
        });
    }

    /** Xóa mềm — phát undoEvent kèm đường dẫn trong thùng rác để hoàn tác. */
    public void softDelete(String path) {
        executor.execute(() -> {
            String trashedPath = trashManager.moveToTrash(path);
            if (trashedPath != null) {
                undoEvent.postValue(new Event<>(trashedPath));
            } else {
                message.postValue(new Event<>("Xóa thất bại"));
            }
            reloadCurrent();
        });
    }

    public void restoreFromTrash(String trashedPath) {
        executor.execute(() -> {
            boolean ok = trashManager.restore(trashedPath);
            message.postValue(new Event<>(ok ? "Đã hoàn tác" : "Hoàn tác thất bại"));
            reloadCurrent();
        });
    }
}
```

---

## 3. Observe trong `MainActivity`

Thêm import:
```java
import com.google.android.material.snackbar.Snackbar;  // đã có sẵn
```

Trong `onCreate` (sau khi khởi tạo `viewModel`), thêm 2 observer:
```java
        // Thông báo 1 lần
        viewModel.getMessage().observe(this, event -> {
            String msg = event.getContentIfNotHandled();   // null nếu đã show
            if (msg != null) {
                Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_SHORT).show();
            }
        });

        // Xóa mềm + Hoàn tác
        viewModel.getUndoEvent().observe(this, event -> {
            String trashedPath = event.getContentIfNotHandled();
            if (trashedPath != null) {
                Snackbar.make(findViewById(android.R.id.content),
                                "Đã chuyển vào thùng rác", Snackbar.LENGTH_LONG)
                        .setAction("Hoàn tác", v -> viewModel.restoreFromTrash(trashedPath))
                        .show();
            }
        });
```

---

## 4. Trỏ các thao tác qua ViewModel

Thay việc tự tạo `FileOperation` + `new Thread` bằng lời gọi ViewModel. Ví dụ các chỗ cần đổi:

**Đổi tên** (dialog rename):
```java
// CŨ: FileOperation op = new RenameOperation(fileRepository, item.getPath(), newName); op.execute(); loadFiles(currentPath);
viewModel.rename(item.getPath(), newName);
```

**Tạo thư mục / tạo file:**
```java
viewModel.createFolder(currentPath, folderName);
viewModel.createFile(currentPath, fileName);
```

**Xóa 1 mục** (bỏ `SoftDeleteOperation` + Snackbar thủ công):
```java
viewModel.softDelete(item.getPath());
```

**Paste (move/copy):**
```java
viewModel.move(cutSourcePath, currentPath);   // cắt-dán
viewModel.copy(pendingSourcePath, destPath);  // sao chép
```

> Không cần gọi `loadFiles(...)` sau thao tác nữa — ViewModel tự `reloadCurrent()`,
> kết quả về qua observer `getFiles()` (Phase 2). Thao tác bulk làm tương tự:
> thêm `bulkDelete(List<String>)`, `bulkMove(...)` trong ViewModel theo cùng khuôn.

---

## Vì sao chuẩn hơn greenrobot EventBus

| Tiêu chí | `Event<T>` + LiveData | greenrobot EventBus |
|---|---|---|
| Lặp khi xoay màn hình | ❌ Không (cờ `handled`) | ⚠️ Dễ bị nếu dùng sticky sai |
| Ràng buộc lifecycle | ✅ Tự động (observe theo owner) | Phải `register/unregister` tay |
| Rò rỉ bộ nhớ | ✅ Không | ⚠️ Quên `unregister` là leak |
| Thêm thư viện | ✅ Không | Có |
| Phạm vi | Trong 1 màn hình (ViewModel↔UI) | Toàn app, cross-Activity |

> Nếu sau này cần bắn event **giữa nhiều Activity/Service** thật sự, lúc đó mới cân nhắc
> greenrobot; còn cho luồng thao tác → UI trong 1 màn hình, `Event<T>` là chuẩn.
