# Phân công 3 thành viên — chia đều & không đụng file

**Mục tiêu:** mỗi file chỉ 1 người sở hữu; khối lượng cân bằng. C hiện quá mỏng vì
phần "xem nội dung" đang bị đẩy ra app ngoài (`Intent.ACTION_VIEW`) và nằm nhờ trong
`MainActivity`. Giải pháp: **C tự làm viewer trong app** + rút phần của C/B ra khỏ
`MainActivity`.

## Tổng số file sau khi chia lại
| Thành viên | Hiện có | Tạo mới | Tổng |
|---|---|---|---|
| A — Duyệt & Điều hướng | 29 | 0 | **~29** (đa số là XML nhỏ) |
| B — Thao tác file | 19 | 2 | **~21** |
| C — Xem nội dung & Nén/Giải nén | 8 | 16 | **~24** |

> Lưu ý: A nhiều file nhưng phần lớn là drawable/layout ngắn (5–20 dòng). B và C
> thiên về code logic/nặng hơn nên số file ít hơn là hợp lý. Đây là cân bằng **theo
> công sức**, không phải đếm file 1:1.

---

## A — Duyệt & Điều hướng (giữ nguyên)
**Java**
- `MainActivity.java` *(vai trò người tích hợp — xem phần Quy tắc)*
- `SearchActivity.java`
- `ui/main/FileListHelper.java`, `ui/main/SortMode.java`
- `ui/main/FileAdapter.java`
- `data/storage/StorageHelper.java`, `StorageVolumeItem.java`, `QuickFolderItem.java`
- `ui/storage/StorageAdapter.java`, `QuickFolderAdapter.java`

**Layout**
- `activity_main.xml`, `activity_search.xml`
- `item_file.xml`, `item_file_grid.xml`, `item_date_header.xml`
- `item_storage.xml`, `item_quick_folder.xml`, `item_recent_search.xml`
- `layout_popup_sort.xml`

**Menu / Drawable**
- `menu/menu_bottom_nav.xml`
- `bg_search_box`, `bg_search_pill`, `bg_category_chip`, `bg_storage_icon`,
  `bg_toolbar_gradient`, `bg_icon_circle`, `bg_action_pill`, `bg_card_item`, `ic_history`

---

## B — Thao tác file (thêm 2 file)
**Java (hiện có)**
- `FolderPickerActivity.java`, `TrashActivity.java`
- `data/model/FileItem.java`  *(model — chủ sở hữu là B, người khác chỉ đọc)*
- `data/repository/FileRepository.java`, `FileRepositoryImpl.java`
- `data/trash/TrashManager.java`
- `domain/operation/`: `FileOperation`, `RenameOperation`, `DeleteOperation`,
  `SoftDeleteOperation`, `CopyOperation`, `MoveOperation`,
  `CreateFolderOperation`, `CreateFileOperation`

**Layout / Drawable (hiện có)**
- `activity_folder_picker.xml`, `activity_trash.xml`, `dialog_progress.xml`
- `ic_delete.xml`, `bg_card_selected.xml` *(highlight khi chọn nhiều)*

**➕ Tạo mới (rút "Xem thuộc tính" ra khỏi MainActivity)**
- `ui/dialog/PropertiesHelper.java` — dựng dialog thuộc tính (tên, kích thước, ngày,
  đường dẫn). *EXIF do C cung cấp qua `ExifHelper` (gọi sang, không tự đọc).*
- `layout/dialog_properties.xml` — giao diện dialog thuộc tính.

---

## C — Xem nội dung & Nén/Giải nén  ⭐ (phần được bổ sung nhiều nhất)
**Java (hiện có)**
- `domain/operation/ZipOperation.java`, `UnzipOperation.java`
- `utils/ThumbnailLoader.java`

**Drawable (hiện có — huy hiệu theo loại file)**
- `bg_badge_folder`, `bg_badge_img`, `bg_badge_video`, `bg_badge_audio`, `bg_badge_file`

**➕ Tạo mới — Viewer trong app (thay cho việc đẩy ra app ngoài)**
- `ui/viewer/ImageViewerActivity.java` — xem ảnh toàn màn hình, zoom / vuốt trái phải
- `ui/viewer/VideoPlayerActivity.java` — phát MP4 (VideoView/MediaPlayer, tua, play/pause)
- `ui/viewer/TextViewerActivity.java` — xem file `.txt/.log/.json...`
- `layout/activity_image_viewer.xml`
- `layout/activity_video_player.xml`
- `layout/activity_text_viewer.xml`

**➕ Tạo mới — Helper (rút logic của C ra khỏi MainActivity)**
- `utils/OpenFileHelper.java` — mở file theo loại: ảnh→ImageViewer, video→VideoPlayer,
  text→TextViewer, còn lại→`ACTION_VIEW`. *(rút `openFile()` khỏi MainActivity)*
- `utils/FileTypeHelper.java` — nhận diện loại file (image/video/audio/pdf/zip/text)
  + chọn badge/icon. *(gom logic loại file rải rác trong FileAdapter/MainActivity)*
- `utils/ExifHelper.java` — đọc EXIF ảnh. *(rút phần EXIF ở MainActivity dòng ~833)*
- `utils/ArchiveHelper.java` — dialog + luồng Zip/Unzip. *(rút `showZipDialog()` /
  `showUnzipDialog()` khỏi MainActivity, gọi `ZipOperation`/`UnzipOperation`)*

**➕ Tạo mới — Icon loại file (nếu chưa có)**
- `ic_image`, `ic_video`, `ic_audio`, `ic_pdf`, `ic_zip`, `ic_text`

---

## File dùng chung → giao 1 người + quy tắc
| File | Chủ sở hữu | Ghi chú |
|---|---|---|
| `MainActivity.java` | **A (người tích hợp)** | B/C KHÔNG sửa trực tiếp; chỉ gọi class của mình |
| `activity_main.xml` | **A** | Như trên |
| `AndroidManifest.xml` | **A** | Ai thêm Activity thì báo A khai báo (ImageViewer/Video/Text = của C) |
| `app/build.gradle.kts` | **A** | Ai cần thư viện (vd Glide/ExoPlayer của C) thì báo A thêm |
| `file_paths.xml` | **A** | Config FileProvider (B share, C open đều dùng) |
| `values/strings.xml` | **tách 3 file** | `strings_a.xml` (A), `strings_b.xml` (B), `strings_c.xml` (C) |
| `values/colors.xml` | **tách 3 file** | `colors_a.xml` / `colors_b.xml` / `colors_c.xml` |
| `values/themes.xml`, `ic_launcher.xml`, `build.gradle.kts` (gốc), `settings.gradle.kts` | **A** | File cấu hình, hiếm khi sửa |

---

## Cần rút khỏi `MainActivity.java` (để hết đụng)
| Đang nằm trong MainActivity | Chuyển sang | Của |
|---|---|---|
| `openFile()` | `OpenFileHelper` | C |
| đọc EXIF (dòng ~833) | `ExifHelper` | C |
| `showZipDialog()` / `showUnzipDialog()` | `ArchiveHelper` | C |
| `showPropertiesDialog()` (tên/size/ngày) | `PropertiesHelper` | B |

MainActivity chỉ còn **gọi**: `OpenFileHelper.open(item)`,
`ArchiveHelper.zip(...)`, `PropertiesHelper.show(item)` → mỗi người sửa file của mình,
A chỉ ghép 1 dòng gọi.

---

## Quy tắc chống đụng file (bắt buộc)
1. **1 file = 1 người.** Cần đổi file của người khác → nhắn người đó, hoặc tạo Pull
   Request để họ duyệt.
2. **Tách `strings`/`colors` theo người** — Android tự gộp mọi file trong `values/`,
   nên 3 người sửa 3 file riêng, không bao giờ đụng nhau.
3. **A là người tích hợp** cho `MainActivity`, `activity_main.xml`, `Manifest`,
   `build.gradle`. B/C đưa tính năng qua class riêng, A chỉ nối dây.
4. **Commit nhỏ, kéo (pull) trước khi làm** để giảm xung đột Git.

