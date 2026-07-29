# Migration: Hilt DI → ViewModel + LiveData

Dự án Java thuần, AGP 8.2.2, compileSdk 34, Java 8. Toàn bộ giữ Java, không thêm Kotlin.
Thứ tự: **Phase 1 (Hilt DI)** ổn định trước, rồi **Phase 2 (ViewModel + LiveData)**.

---

## PHASE 1 — Hilt DI

### 1. Gradle

**Root `build.gradle.kts`**
```kotlin
plugins {
    id("com.android.application") version "8.2.2" apply false
    id("com.google.dagger.hilt.android") version "2.51.1" apply false
}
```

**`app/build.gradle.kts`** — thêm plugin + dependencies:
```kotlin
plugins {
    id("com.android.application")
    id("com.google.dagger.hilt.android")   // + dòng này
}

// ... android { } giữ nguyên (Java 8 OK với Hilt)

dependencies {
    // ... deps cũ giữ nguyên

    // Hilt
    implementation("com.google.dagger:hilt-android:2.51.1")
    annotationProcessor("com.google.dagger:hilt-android-compiler:2.51.1")

    // ViewModel + LiveData (dùng ở Phase 2, thêm luôn)
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime:2.7.0")
}
```
> Dùng `annotationProcessor` (không phải `kapt`) vì project là Java thuần.

### 2. Application class

**File mới: `app/src/main/java/com/example/myfile/MyFileApp.java`**
```java
package com.example.myfile;

import android.app.Application;
import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class MyFileApp extends Application {
}
```

**`AndroidManifest.xml`** — thêm `android:name` vào `<application>`:
```xml
<application
    android:name=".MyFileApp"
    android:allowBackup="true"
    ... >
```

### 3. Cho phép Hilt tạo `FileRepositoryImpl`

`@Binds` cần constructor `@Inject`. Sửa **`FileRepositoryImpl.java`** — thêm constructor:
```java
public class FileRepositoryImpl implements FileRepository {

    @javax.inject.Inject
    public FileRepositoryImpl() {}   // + thêm dòng này

    // ... phần còn lại giữ nguyên
}
```

### 4. Hilt Modules

**File mới: `app/src/main/java/com/example/myfile/di/RepositoryModule.java`**
```java
package com.example.myfile.di;

import com.example.myfile.data.repository.FileRepository;
import com.example.myfile.data.repository.FileRepositoryImpl;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public abstract class RepositoryModule {

    @Binds
    @Singleton
    public abstract FileRepository bindFileRepository(FileRepositoryImpl impl);
}
```

**File mới: `app/src/main/java/com/example/myfile/di/AppModule.java`**
```java
package com.example.myfile.di;

import android.content.Context;

import com.example.myfile.data.trash.TrashManager;
import com.example.myfile.data.vault.PrivateVaultManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class AppModule {

    @Provides
    @Singleton
    public TrashManager provideTrashManager(@ApplicationContext Context context) {
        return new TrashManager(context);
    }

    @Provides
    @Singleton
    public PrivateVaultManager provideVaultManager(@ApplicationContext Context context) {
        return new PrivateVaultManager(context);
    }

    @Provides
    @Singleton
    public ExecutorService provideExecutorService() {
        return Executors.newFixedThreadPool(4);
    }
}
```
> `TrashManager` và `PrivateVaultManager` chỉ dùng `SharedPreferences` + external storage → `@ApplicationContext` an toàn (không giữ Activity context → không leak).

### 5. Inject vào `MainActivity`

Đầu class, thêm annotation:
```java
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity implements FileAdapter.OnItemClickListener {
```

Đổi field từ `private` → **package-private + `@Inject`** (Hilt không inject được field `private`):
```java
    // TRƯỚC:
    // private FileRepository fileRepository;
    // private TrashManager trashManager;
    // private com.example.myfile.data.vault.PrivateVaultManager vaultManager;

    // SAU:
    @javax.inject.Inject FileRepository fileRepository;
    @javax.inject.Inject TrashManager trashManager;
    @javax.inject.Inject com.example.myfile.data.vault.PrivateVaultManager vaultManager;
```

Trong `onCreate`, **xóa 3 dòng `new`** (Hilt đã inject sau `super.onCreate()`):
```java
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // XÓA 3 dòng:
        // fileRepository = new FileRepositoryImpl();
        // trashManager = new TrashManager(this);
        // vaultManager = new com.example.myfile.data.vault.PrivateVaultManager(this);

        tvCurrentPath = findViewById(R.id.tvCurrentPath);
        // ... giữ nguyên phần còn lại
    }
```

Xóa import không còn dùng: `FileRepositoryImpl`.

✅ **Build & chạy thử.** Hoạt động y hệt cũ nhưng đã tách khởi tạo phụ thuộc ra khỏi Activity. `StorageHelper` toàn static → không cần đụng.

---

## PHASE 2 — ViewModel + LiveData

Mục tiêu: chuyển `fileRepository.list()` khỏi main thread, quản lý state qua LiveData, Activity chỉ observe.

### 1. `MainViewModel`

**File mới: `app/src/main/java/com/example/myfile/ui/main/MainViewModel.java`**
```java
package com.example.myfile.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myfile.data.model.FileItem;
import com.example.myfile.data.repository.FileRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class MainViewModel extends ViewModel {

    private final FileRepository repository;
    private final ExecutorService executor;

    private final MutableLiveData<List<FileItem>> files = new MutableLiveData<>();
    private final MutableLiveData<String> currentPath = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);

    @Inject
    public MainViewModel(FileRepository repository, ExecutorService executor) {
        this.repository = repository;
        this.executor = executor;
    }

    public LiveData<List<FileItem>> getFiles() { return files; }
    public LiveData<String> getCurrentPath() { return currentPath; }
    public LiveData<Boolean> getLoading() { return loading; }

    /** Tải + sort + filter trên background thread, post kết quả về main. */
    public void load(String path, SortMode sortMode, String searchQuery) {
        loading.postValue(true);
        currentPath.postValue(path);
        executor.execute(() -> {
            List<FileItem> items = repository.list(path);
            items = FileListHelper.sort(items, sortMode);
            items = FileListHelper.filter(items, searchQuery);
            files.postValue(items);
            loading.postValue(false);
        });
    }
}
```
> Nhóm theo ngày (`groupFilesByDate`) là việc của UI → giữ ở Activity trong observer, ViewModel chỉ trả list đã sort/filter.

### 2. Sửa `MainActivity`

Thêm field + import:
```java
import androidx.lifecycle.ViewModelProvider;
import com.example.myfile.ui.main.MainViewModel;

    private MainViewModel viewModel;
```

Trong `onCreate` (sau khi set adapter cho RecyclerView), khởi tạo VM + observe:
```java
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        viewModel.getFiles().observe(this, items -> {
            String path = viewModel.getCurrentPath().getValue();
            if (path == null) path = currentPath;

            List<FileItem> display =
                (!path.equals(rootPath)) ? groupFilesByDate(items) : items;

            currentPath = path;
            tvCurrentPath.setText(path);
            updateBreadcrumb(path);
            adapter.updateData(display);
            emptyState.setVisibility(display.isEmpty() ? View.VISIBLE : View.GONE);
            findViewById(R.id.btnBack)
                .setVisibility(path.equals(rootPath) ? View.GONE : View.VISIBLE);
        });
```
> `new ViewModelProvider(this)` hoạt động với `@HiltViewModel` vì `@AndroidEntryPoint` Activity tự cấp `HiltViewModelFactory`.

Thay **toàn bộ thân** `loadFiles(String path)` bằng 1 dòng gọi VM:
```java
    private void loadFiles(String path) {
        viewModel.load(path, sortMode, searchQuery);
    }
```
> Mọi nơi đang gọi `loadFiles(...)` giữ nguyên — giờ chạy async, kết quả về qua observer.

(Tùy chọn) hiện progress khi loading:
```java
        viewModel.getLoading().observe(this, isLoading -> {
            // ví dụ: progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
```

✅ **Build & chạy.** Danh sách file giờ load off main thread, xoay màn hình không mất state (ViewModel sống qua config change).

---

## Thứ tự thực thi & lưu ý

1. Làm hết Phase 1 → **sync + build + chạy** (xác nhận DI OK) rồi mới sang Phase 2.
2. Field `@Inject` **không được** để `private`.
3. Cần chạy Gradle bằng **JDK 17** (yêu cầu của AGP 8.2) — `sourceCompatibility = 1_8` vẫn giữ, không sao.
4. `settings.gradle.kts` đã có `google()` + `gradlePluginPortal()` → plugin Hilt resolve được, không cần sửa.

## Bước tiếp theo (tùy chọn, sau khi 2 phase ổn)
- Đưa `new Thread { ... }` ở các thao tác bulk (delete/move/copy) vào ViewModel + `ExecutorService` đã inject.
- Bọc kết quả thao tác bằng một "event một lần" (`LiveData` + wrapper `Event<T>`) để show Snackbar không lặp khi xoay màn hình → đây là "event bus" đúng chuẩn Android, an toàn lifecycle hơn greenrobot EventBus.
- 
