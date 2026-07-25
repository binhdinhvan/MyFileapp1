# My File - Source code (Thanh vien B - Thao tac file)

Ban cap nhat moi nhat, gom toan bo tinh nang da hoan thanh. Day KHONG PHAI
mot project Android Studio day du (khong co build.gradle, gradlew, icon...)
- chi la phan source code de copy vao project that.

## Cach dung

1. Mo project that (project chinh cua nhom, co san MainActivity.java o
   com.example.myfile).
2. Copy toan bo cay thu muc trong `app/src/main/java/...` va
   `app/src/main/res/layout/...`, `app/src/main/res/xml/...` vao dung vi tri
   tuong ung trong project that (ghi de neu trung ten file).
3. Mo file `MANIFEST_ADDITIONS.txt`, them dung cac dong do vao
   `AndroidManifest.xml` that (quyen + FileProvider + 3 activity moi) -
   KHONG copy de AndroidManifest.xml vi file that co the co noi dung khac
   (icon, theme, activity cua nguoi khac...).
4. Kiem tra `app/build.gradle.kts` co 2 dong nay chua, neu chua them vao:
   - `implementation("androidx.recyclerview:recyclerview:1.3.2")`
   - `implementation("com.google.android.material:material:1.11.0")`
5. Sync Gradle, build thu (Ctrl+F9), roi push len git that.

## CANH BAO QUAN TRONG - conflict ten file

Neu nguoi khac trong nhom da tao san MainActivity.java hoac FileAdapter.java
theo kien truc rieng cua ho (vi du dung ViewBinding), COPY DE se lam mat code
cua ho. Nen trao doi truoc voi nhom xem ai giu vai tro "nen" (base) truoc khi
ghi de cac file dung chung: MainActivity.java, FileAdapter.java, item_file.xml,
activity_main.xml, AndroidManifest.xml.

## Danh sach file

- data/model/FileItem.java
- data/repository/FileRepository.java (interface)
- data/repository/FileRepositoryImpl.java
- data/trash/TrashManager.java (thung rac, dung SharedPreferences luu metadata)
- domain/operation/ (Command pattern: FileOperation + Rename/Delete/Move/Copy/
  CreateFolder/CreateFile/SoftDelete)
- ui/main/FileAdapter.java (co CheckBox cho multi-select, badge mau theo loai file)
- MainActivity.java
- FolderPickerActivity.java (man hinh chon thu muc dich cho Move/Copy)
- TrashActivity.java (xem/khoi phuc/xoa vinh vien file trong thung rac)
- res/layout/activity_main.xml
- res/layout/activity_folder_picker.xml
- res/layout/activity_trash.xml
- res/layout/item_file.xml
- res/layout/dialog_progress.xml
- res/xml/file_paths.xml (can cho FileProvider/Share)
- res/drawable/ic_delete.xml (icon thung rac o header)

## Tinh nang da hoan thanh (Thanh vien B - du 9/9 theo phan cong)

- View / Open / Up one level (nut back o header + gesture he thong)
- Rename
- Delete (chuyen vao Thung rac, co Snackbar UNDO ngay lap tuc)
- Move, Copy (chon thu muc dich qua FolderPickerActivity - bam chon, khong go tay path)
- Cut / Paste
- Create Folder, Create File
- Xem thuoc tinh (Properties)
- Chon nhieu (multi-select voi CheckBox) + bulk Delete/Move/Copy
- Hien thi tien trinh (progress bar dang xac dinh cho bulk, dang vo dinh cho tung file)
- Chia se (Share qua FileProvider + Intent.ACTION_SEND)
- Thung rac + Restore + Xoa vinh vien + Empty Trash + tu dong xoa sau 30 ngay
  (tinh nang tu de xuat them, nut Trash o header dung icon thay vi chu)

Kien truc: Command pattern (FileOperation), tach Repository/UI, chay bat dong bo
(Thread + runOnUiThread) cho cac thao tac co the cham (copy/move/delete).

## Con thieu / chua lam (khong thuoc pham vi Thanh vien B)

- Xem noi dung file that (hien tai bam vao file chi hien Toast placeholder)
- Zip/Unzip, xem EXIF, phat MP4 (Thanh vien C)
- Danh sach dang List/Grid, breadcrumb, sap xep, tim kiem, drawer chon storage
  (Thanh vien A)
