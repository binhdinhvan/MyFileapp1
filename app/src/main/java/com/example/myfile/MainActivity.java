package com.example.myfile;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.PopupWindow;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myfile.data.model.FileItem;
import com.example.myfile.data.repository.FileRepository;
import com.example.myfile.data.repository.FileRepositoryImpl;
import com.example.myfile.data.trash.TrashManager;
import com.example.myfile.domain.operation.CopyOperation;
import com.example.myfile.domain.operation.CreateFileOperation;
import com.example.myfile.domain.operation.CreateFolderOperation;
import com.example.myfile.domain.operation.FileOperation;
import com.example.myfile.domain.operation.MoveOperation;
import com.example.myfile.domain.operation.RenameOperation;
import com.example.myfile.domain.operation.SoftDeleteOperation;
import com.example.myfile.ui.main.FileAdapter;
import com.example.myfile.ui.main.FileListHelper;
import com.example.myfile.ui.main.SortMode;
import com.example.myfile.data.storage.StorageHelper;
import com.example.myfile.ui.storage.QuickFolderAdapter;
import com.example.myfile.ui.storage.StorageAdapter;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements FileAdapter.OnItemClickListener {

    private FileRepository fileRepository;
    private TrashManager trashManager;
    private FileAdapter adapter;
    private TextView tvCurrentPath;
    private LinearLayout emptyState;
    private LinearLayout selectionToolbar;
    private LinearLayout normalToolbar;
    private TextView tvSelectionCount;
    private String rootPath;
    private String currentPath;
    private String cutSourcePath = null;
    private String pendingSourcePath = null;
    private List<String> pendingBulkPaths = null;
    private AlertDialog progressDialog;
    private android.widget.ProgressBar bulkProgressBar;
    private TextView bulkProgressText;
    private static final int MOVE_REQUEST_CODE = 200;
    private static final int COPY_REQUEST_CODE = 201;
    private static final int BULK_MOVE_REQUEST_CODE = 202;
    private static final int BULK_COPY_REQUEST_CODE = 203;

    private DrawerLayout drawerLayout;
    private HorizontalScrollView breadcrumbScroll;
    private LinearLayout breadcrumbContainer;
    private SortMode sortMode = SortMode.DATE_DESC;
    private String searchQuery = "";
    private int viewMode = 0; // 0: List, 1: Grid (3 cols), 2: Large Grid (2 cols)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fileRepository = new FileRepositoryImpl();
        trashManager = new TrashManager(this);
        tvCurrentPath = findViewById(R.id.tvCurrentPath);
        emptyState = findViewById(R.id.emptyState);
        selectionToolbar = findViewById(R.id.selectionToolbar);
        normalToolbar = findViewById(R.id.normalToolbar);
        tvSelectionCount = findViewById(R.id.tvSelectionCount);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FileAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        checkPermissionAndLoad();

        findViewById(R.id.btnNewFolder).setOnClickListener(v -> showCreateFolderDialog());
        findViewById(R.id.btnNewFile).setOnClickListener(v -> showCreateFileDialog());
        findViewById(R.id.btnPaste).setOnClickListener(v -> pasteFile());
        findViewById(R.id.btnSelectMode).setOnClickListener(v -> adapter.enterSelectionMode());
        findViewById(R.id.btnSelCancel).setOnClickListener(v -> adapter.exitSelectionMode());
        findViewById(R.id.btnSelDelete).setOnClickListener(v -> bulkDelete());
        findViewById(R.id.btnSelMove).setOnClickListener(v -> bulkMove());
        findViewById(R.id.btnSelCopy).setOnClickListener(v -> bulkCopy());
        findViewById(R.id.btnTrash).setOnClickListener(v -> startActivity(new Intent(this, TrashActivity.class)));
        findViewById(R.id.btnBack).setOnClickListener(v -> navigateUp());
        findViewById(R.id.btnSearchIcon).setOnClickListener(v -> startActivity(new Intent(this, SearchActivity.class)));
        findViewById(R.id.btnSortMenu).setOnClickListener(v -> showSortMenu(v));
        
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (adapter.isSelectionMode()) {
                    adapter.exitSelectionMode();
                    return;
                }
                if (!currentPath.equals(rootPath)) {
                    navigateUp();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        setupBrowseFeatures();
    }

    private void checkPermissionAndLoad() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                loadFiles(rootPath);
            } else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 100);
            }
        } else {
            boolean readGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
            boolean writeGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
            if (readGranted && writeGranted) {
                loadFiles(rootPath);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, 101);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                loadFiles(rootPath);
            } else {
                Toast.makeText(this, "Permission denied, cannot read files", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == MOVE_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null && pendingSourcePath != null) {
                String destPath = data.getStringExtra(FolderPickerActivity.EXTRA_SELECTED_PATH);
                FileOperation operation = new MoveOperation(fileRepository, pendingSourcePath, destPath);
                runOperationWithProgress(operation, "Moving...", "Moved successfully", "Move failed");
            }
            pendingSourcePath = null;
        } else if (requestCode == COPY_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null && pendingSourcePath != null) {
                String destPath = data.getStringExtra(FolderPickerActivity.EXTRA_SELECTED_PATH);
                FileOperation operation = new CopyOperation(fileRepository, pendingSourcePath, destPath);
                runOperationWithProgress(operation, "Copying...", "Copied successfully", "Copy failed");
            }
            pendingSourcePath = null;
        } else if (requestCode == BULK_MOVE_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null && pendingBulkPaths != null) {
                String destPath = data.getStringExtra(FolderPickerActivity.EXTRA_SELECTED_PATH);
                runBulkMove(pendingBulkPaths, destPath);
            } else {
                adapter.exitSelectionMode();
            }
            pendingBulkPaths = null;
        } else if (requestCode == BULK_COPY_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null && pendingBulkPaths != null) {
                String destPath = data.getStringExtra(FolderPickerActivity.EXTRA_SELECTED_PATH);
                runBulkCopy(pendingBulkPaths, destPath);
            } else {
                adapter.exitSelectionMode();
            }
            pendingBulkPaths = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101 && grantResults.length >= 2
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            loadFiles(rootPath);
        } else {
            Toast.makeText(this, "Permission denied, cannot read/write files", Toast.LENGTH_LONG).show();
        }
    }

    private void loadFiles(String path) {
        currentPath = path;
        tvCurrentPath.setText(path);
        List<FileItem> items = fileRepository.list(path);
        items = FileListHelper.sort(items, sortMode);
        items = FileListHelper.filter(items, searchQuery);
        if (!path.equals(rootPath)) {
            items = groupFilesByDate(items);
        }
        
        updateBreadcrumb(path);
        adapter.updateData(items);
        emptyState.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
        findViewById(R.id.btnBack).setVisibility(path.equals(rootPath) ? View.GONE : View.VISIBLE);
    }

    private void navigateUp() {
        if (!currentPath.equals(rootPath)) {
            File parent = new File(currentPath).getParentFile();
            if (parent != null) {
                loadFiles(parent.getAbsolutePath());
            } else {
                loadFiles(rootPath);
            }
        }
    }

    private List<FileItem> groupFilesByDate(List<FileItem> original) {
        if (original.isEmpty()) return original;
        
        List<FileItem> folders = new ArrayList<>();
        List<FileItem> filesToGroup = new ArrayList<>();
        for (FileItem item : original) {
            if (item.isDirectory()) {
                folders.add(item);
            } else {
                filesToGroup.add(item);
            }
        }
        
        // Sort files by date descending before grouping
        java.util.Collections.sort(filesToGroup, (f1, f2) -> Long.compare(f2.getLastModified(), f1.getLastModified()));
        
        List<FileItem> finalItems = new ArrayList<>(folders);
        
        if (filesToGroup.isEmpty()) {
            return finalItems;
        }

        java.util.Map<String, List<FileItem>> groups = new java.util.LinkedHashMap<>();
        
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        long todayStart = cal.getTimeInMillis();
        long yesterdayStart = todayStart - 86400000L;
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault());
        
        for (FileItem item : filesToGroup) {
            long time = item.getLastModified();
            String groupName;
            if (time >= todayStart) {
                groupName = "Today";
            } else if (time >= yesterdayStart) {
                groupName = "Yesterday";
            } else if (time >= todayStart - 7L * 86400000L) {
                int days = (int) ((todayStart - time) / 86400000L) + 1;
                groupName = days + " days ago";
            } else {
                groupName = sdf.format(new java.util.Date(time));
            }
            
            if (!groups.containsKey(groupName)) {
                groups.put(groupName, new ArrayList<>());
            }
            groups.get(groupName).add(item);
        }
        
        for (java.util.Map.Entry<String, List<FileItem>> entry : groups.entrySet()) {
            String title = entry.getKey() + "  |  " + entry.getValue().size() + " items";
            finalItems.add(new FileItem(title));
            finalItems.addAll(entry.getValue());
        }
        
        return finalItems;
    }

    @Override
    public void onItemClick(FileItem item) {
        if (item.isDirectory()) {
            loadFiles(item.getPath());
        } else {
            Toast.makeText(this, "Open file: " + item.getName() + " (viewer not implemented yet)", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onItemLongClick(FileItem item) {
        String[] options = {"Rename", "Move", "Copy", "Cut", "Delete", "Share", "Properties"};
        new AlertDialog.Builder(this)
                .setTitle(item.getName())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showRenameDialog(item);
                    } else if (which == 1) {
                        showMoveDialog(item);
                    } else if (which == 2) {
                        showCopyDialog(item);
                    } else if (which == 3) {
                        cutSourcePath = item.getPath();
                        Toast.makeText(this, item.getName() + " cut. Navigate to destination and tap Paste.", Toast.LENGTH_SHORT).show();
                    } else if (which == 4) {
                        showDeleteConfirm(item);
                    } else if (which == 5) {
                        shareFile(item);
                    } else {
                        showPropertiesDialog(item);
                    }
                })
                .show();
    }

    @Override
    public void onSelectionChanged(boolean selectionMode, int count) {
        if (selectionMode) {
            selectionToolbar.setVisibility(View.VISIBLE);
            normalToolbar.setVisibility(View.GONE);
            tvSelectionCount.setText(count + " selected");
        } else {
            selectionToolbar.setVisibility(View.GONE);
            normalToolbar.setVisibility(View.VISIBLE);
        }
    }

    private void bulkDelete() {
        List<String> paths = adapter.getSelectedPaths();
        if (paths.isEmpty()) {
            Toast.makeText(this, "No items selected", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Delete " + paths.size() + " items?")
                .setMessage("Selected items will be moved to Trash.")
                .setPositiveButton("Delete", (d, w) -> runBulkDelete(paths))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void runBulkDelete(List<String> paths) {
        int total = paths.size();
        showDeterminateProgress("Deleting", total);
        new Thread(() -> {
            int successCount = 0;
            List<String> trashedPaths = new ArrayList<>();
            for (int i = 0; i < total; i++) {
                SoftDeleteOperation operation = new SoftDeleteOperation(trashManager, paths.get(i));
                if (operation.execute()) {
                    successCount++;
                    trashedPaths.add(operation.getTrashedPath());
                }
                int current = i + 1;
                runOnUiThread(() -> updateDeterminateProgress("Deleting", current, total));
            }
            int finalSuccessCount = successCount;
            runOnUiThread(() -> {
                dismissProgress();
                adapter.exitSelectionMode();
                loadFiles(currentPath);
                Snackbar.make(findViewById(R.id.recyclerView), "Moved " + finalSuccessCount + "/" + total + " items to Trash", Snackbar.LENGTH_LONG)
                        .setAction("UNDO", v -> {
                            for (String trashedPath : trashedPaths) {
                                trashManager.restore(trashedPath);
                            }
                            loadFiles(currentPath);
                        })
                        .show();
            });
        }).start();
    }

    private void bulkMove() {
        List<String> paths = adapter.getSelectedPaths();
        if (paths.isEmpty()) {
            Toast.makeText(this, "No items selected", Toast.LENGTH_SHORT).show();
            return;
        }
        pendingBulkPaths = paths;
        Intent intent = new Intent(this, FolderPickerActivity.class);
        intent.putExtra(FolderPickerActivity.EXTRA_START_PATH, currentPath);
        startActivityForResult(intent, BULK_MOVE_REQUEST_CODE);
    }

    private void bulkCopy() {
        List<String> paths = adapter.getSelectedPaths();
        if (paths.isEmpty()) {
            Toast.makeText(this, "No items selected", Toast.LENGTH_SHORT).show();
            return;
        }
        pendingBulkPaths = paths;
        Intent intent = new Intent(this, FolderPickerActivity.class);
        intent.putExtra(FolderPickerActivity.EXTRA_START_PATH, currentPath);
        startActivityForResult(intent, BULK_COPY_REQUEST_CODE);
    }

    private void runBulkMove(List<String> paths, String destPath) {
        int total = paths.size();
        showDeterminateProgress("Moving", total);
        new Thread(() -> {
            int successCount = 0;
            for (int i = 0; i < total; i++) {
                FileOperation operation = new MoveOperation(fileRepository, paths.get(i), destPath);
                if (operation.execute()) {
                    successCount++;
                }
                int current = i + 1;
                runOnUiThread(() -> updateDeterminateProgress("Moving", current, total));
            }
            int finalSuccessCount = successCount;
            runOnUiThread(() -> {
                dismissProgress();
                Toast.makeText(this, "Moved " + finalSuccessCount + "/" + total + " items", Toast.LENGTH_SHORT).show();
                adapter.exitSelectionMode();
                loadFiles(currentPath);
            });
        }).start();
    }

    private void runBulkCopy(List<String> paths, String destPath) {
        int total = paths.size();
        showDeterminateProgress("Copying", total);
        new Thread(() -> {
            int successCount = 0;
            for (int i = 0; i < total; i++) {
                FileOperation operation = new CopyOperation(fileRepository, paths.get(i), destPath);
                if (operation.execute()) {
                    successCount++;
                }
                int current = i + 1;
                runOnUiThread(() -> updateDeterminateProgress("Copying", current, total));
            }
            int finalSuccessCount = successCount;
            runOnUiThread(() -> {
                dismissProgress();
                Toast.makeText(this, "Copied " + finalSuccessCount + "/" + total + " items", Toast.LENGTH_SHORT).show();
                adapter.exitSelectionMode();
                loadFiles(currentPath);
            });
        }).start();
    }

    private void showIndeterminateProgress(String message) {
        View view = getLayoutInflater().inflate(R.layout.dialog_progress, null);
        android.widget.ProgressBar barIndeterminate = view.findViewById(R.id.progressBarIndeterminate);
        View barDeterminate = view.findViewById(R.id.progressBarDeterminate);
        TextView tvText = view.findViewById(R.id.tvProgressText);
        barDeterminate.setVisibility(View.GONE);
        barIndeterminate.setVisibility(View.VISIBLE);
        tvText.setText(message);
        progressDialog = new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false)
                .create();
        progressDialog.show();
    }

    private void showDeterminateProgress(String message, int total) {
        View view = getLayoutInflater().inflate(R.layout.dialog_progress, null);
        View barIndeterminate = view.findViewById(R.id.progressBarIndeterminate);
        bulkProgressBar = view.findViewById(R.id.progressBarDeterminate);
        bulkProgressText = view.findViewById(R.id.tvProgressText);
        barIndeterminate.setVisibility(View.GONE);
        bulkProgressBar.setVisibility(View.VISIBLE);
        bulkProgressBar.setMax(total);
        bulkProgressBar.setProgress(0);
        bulkProgressText.setText(message + " 0/" + total);
        progressDialog = new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false)
                .create();
        progressDialog.show();
    }

    private void updateDeterminateProgress(String message, int current, int total) {
        if (bulkProgressBar != null) {
            bulkProgressBar.setProgress(current);
        }
        if (bulkProgressText != null) {
            bulkProgressText.setText(message + " " + current + "/" + total);
        }
    }

    private void dismissProgress() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        progressDialog = null;
    }

    private void runOperationWithProgress(FileOperation operation, String progressMessage, String successMessage, String failMessage) {
        showIndeterminateProgress(progressMessage);
        new Thread(() -> {
            boolean ok = operation.execute();
            runOnUiThread(() -> {
                dismissProgress();
                Toast.makeText(this, ok ? successMessage : failMessage, Toast.LENGTH_SHORT).show();
                loadFiles(currentPath);
            });
        }).start();
    }

    private void runOperation(FileOperation operation, String successMessage, String failMessage) {
        boolean ok = operation.execute();
        Toast.makeText(this, ok ? successMessage : failMessage, Toast.LENGTH_SHORT).show();
        loadFiles(currentPath);
    }

    private void showRenameDialog(FileItem item) {
        EditText input = new EditText(this);
        input.setText(item.getName());
        new AlertDialog.Builder(this)
                .setTitle("Rename")
                .setView(input)
                .setPositiveButton("OK", (d, w) -> {
                    String newName = input.getText().toString().trim();
                    if (newName.isEmpty()) {
                        Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    FileOperation operation = new RenameOperation(fileRepository, item.getPath(), newName);
                    runOperation(operation, "Renamed successfully", "Rename failed (name already exists?)");
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showMoveDialog(FileItem item) {
        pendingSourcePath = item.getPath();
        Intent intent = new Intent(this, FolderPickerActivity.class);
        intent.putExtra(FolderPickerActivity.EXTRA_START_PATH, currentPath);
        startActivityForResult(intent, MOVE_REQUEST_CODE);
    }

    private void showCopyDialog(FileItem item) {
        pendingSourcePath = item.getPath();
        Intent intent = new Intent(this, FolderPickerActivity.class);
        intent.putExtra(FolderPickerActivity.EXTRA_START_PATH, currentPath);
        startActivityForResult(intent, COPY_REQUEST_CODE);
    }

    private void pasteFile() {
        if (cutSourcePath == null) {
            Toast.makeText(this, "Nothing to paste. Cut a file first.", Toast.LENGTH_SHORT).show();
            return;
        }
        FileOperation operation = new MoveOperation(fileRepository, cutSourcePath, currentPath);
        boolean ok = operation.execute();
        Toast.makeText(this, ok ? "Pasted successfully" : "Paste failed", Toast.LENGTH_SHORT).show();
        if (ok) {
            cutSourcePath = null;
        }
        loadFiles(currentPath);
    }

    private void showDeleteConfirm(FileItem item) {
        String message = item.isDirectory() ? "This will move the folder to Trash." : "This will move the file to Trash.";
        new AlertDialog.Builder(this)
                .setTitle("Delete " + item.getName() + "?")
                .setMessage(message)
                .setPositiveButton("Delete", (d, w) -> softDeleteSingle(item))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void softDeleteSingle(FileItem item) {
        SoftDeleteOperation operation = new SoftDeleteOperation(trashManager, item.getPath());
        boolean ok = operation.execute();
        loadFiles(currentPath);
        if (ok) {
            String trashedPath = operation.getTrashedPath();
            Snackbar.make(findViewById(R.id.recyclerView), "Moved to Trash", Snackbar.LENGTH_LONG)
                    .setAction("UNDO", v -> {
                        trashManager.restore(trashedPath);
                        loadFiles(currentPath);
                    })
                    .show();
        } else {
            Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void showPropertiesDialog(FileItem item) {
        String type = item.isDirectory() ? "Folder" : "File";
        String sizeText = formatSize(item.getSize());
        String dateText = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(item.getLastModified());

        String itemCountText = "";
        if (item.isDirectory()) {
            File folder = new File(item.getPath());
            File[] children = folder.listFiles();
            int count = children != null ? children.length : 0;
            itemCountText = "\nItems inside: " + count;
        }

        String message = "Name: " + item.getName()
                + "\nType: " + type
                + "\nPath: " + item.getPath()
                + "\nSize: " + sizeText
                + "\nLast modified: " + dateText
                + itemCountText;

        new AlertDialog.Builder(this)
                .setTitle("Properties")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String unit = "KMGTPE".charAt(exp - 1) + "B";
        return String.format(java.util.Locale.getDefault(), "%.1f %s", bytes / Math.pow(1024, exp), unit);
    }

    private void showCreateFolderDialog() {
        EditText input = new EditText(this);
        input.setHint("Folder name");
        new AlertDialog.Builder(this)
                .setTitle("New Folder")
                .setView(input)
                .setPositiveButton("Create", (d, w) -> {
                    String folderName = input.getText().toString().trim();
                    if (folderName.isEmpty()) {
                        Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    FileOperation operation = new CreateFolderOperation(fileRepository, currentPath, folderName);
                    runOperation(operation, "Folder created", "Create failed (name already exists?)");
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showCreateFileDialog() {
        EditText input = new EditText(this);
        input.setHint("File name (e.g. note.txt)");
        new AlertDialog.Builder(this)
                .setTitle("New File")
                .setView(input)
                .setPositiveButton("Create", (d, w) -> {
                    String fileName = input.getText().toString().trim();
                    if (fileName.isEmpty()) {
                        Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    FileOperation operation = new CreateFileOperation(fileRepository, currentPath, fileName);
                    runOperation(operation, "File created", "Create failed (name already exists?)");
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void shareFile(FileItem item) {
        if (item.isDirectory()) {
            Toast.makeText(this, "Cannot share a folder", Toast.LENGTH_SHORT).show();
            return;
        }
        File file = new File(item.getPath());
        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
        String mimeType = getContentResolver().getType(uri);
        if (mimeType == null) {
            mimeType = "*/*";
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(mimeType);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, "Share " + item.getName()));
    }


    private void setupBrowseFeatures() {

        drawerLayout = findViewById(R.id.drawerLayout);
        findViewById(R.id.btnMenu).setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        RecyclerView rvStorage = findViewById(R.id.recyclerViewStorage);
        rvStorage.setLayoutManager(new LinearLayoutManager(this));
        rvStorage.setAdapter(new StorageAdapter(StorageHelper.getStorages(this), storage -> {
            rootPath = storage.getPath();
            searchQuery = "";
            loadFiles(rootPath);
            drawerLayout.closeDrawer(GravityCompat.START);
        }));

        RecyclerView rvQuickFolders = findViewById(R.id.recyclerViewQuickFolders);
        rvQuickFolders.setLayoutManager(new LinearLayoutManager(this));
        rvQuickFolders.setAdapter(new QuickFolderAdapter(StorageHelper.getQuickFolders(), folder -> {
            rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            searchQuery = "";
            loadFiles(folder.getPath());
            drawerLayout.closeDrawer(GravityCompat.START);
        }));

        breadcrumbScroll = findViewById(R.id.breadcrumbScroll);
        breadcrumbContainer = findViewById(R.id.breadcrumbContainer);


        if (currentPath != null) {
            updateBreadcrumb(currentPath);
        }
    }

    private void showSortMenu(View anchor) {
        View popupView = getLayoutInflater().inflate(R.layout.layout_popup_sort, null);
        PopupWindow popupWindow = new PopupWindow(popupView, 
                LinearLayout.LayoutParams.WRAP_CONTENT, 
                LinearLayout.LayoutParams.WRAP_CONTENT, true);
        
        popupView.findViewById(R.id.check_grid).setVisibility(viewMode == 1 ? View.VISIBLE : View.INVISIBLE);
        popupView.findViewById(R.id.check_large_grid).setVisibility(viewMode == 2 ? View.VISIBLE : View.INVISIBLE);
        popupView.findViewById(R.id.check_list).setVisibility(viewMode == 0 ? View.VISIBLE : View.INVISIBLE);

        boolean isDesc = sortMode.name().endsWith("_DESC");
        String field = sortMode.name().split("_")[0]; 

        popupView.findViewById(R.id.check_name).setVisibility("NAME".equals(field) ? View.VISIBLE : View.INVISIBLE);
        popupView.findViewById(R.id.check_size).setVisibility("SIZE".equals(field) ? View.VISIBLE : View.INVISIBLE);
        popupView.findViewById(R.id.check_time).setVisibility("DATE".equals(field) ? View.VISIBLE : View.INVISIBLE);
        popupView.findViewById(R.id.check_type).setVisibility("TYPE".equals(field) ? View.VISIBLE : View.INVISIBLE);

        popupView.findViewById(R.id.check_forward).setVisibility(!isDesc ? View.VISIBLE : View.INVISIBLE);
        popupView.findViewById(R.id.check_reverse).setVisibility(isDesc ? View.VISIBLE : View.INVISIBLE);

        popupView.findViewById(R.id.menu_grid).setOnClickListener(v -> { viewMode = 1; updateViewMode(); popupWindow.dismiss(); });
        popupView.findViewById(R.id.menu_list).setOnClickListener(v -> { viewMode = 0; updateViewMode(); popupWindow.dismiss(); });
        popupView.findViewById(R.id.menu_large_grid).setOnClickListener(v -> { viewMode = 2; updateViewMode(); popupWindow.dismiss(); });

        popupView.findViewById(R.id.menu_name).setOnClickListener(v -> { updateSortMode("NAME", isDesc); popupWindow.dismiss(); });
        popupView.findViewById(R.id.menu_size).setOnClickListener(v -> { updateSortMode("SIZE", isDesc); popupWindow.dismiss(); });
        popupView.findViewById(R.id.menu_time).setOnClickListener(v -> { updateSortMode("DATE", isDesc); popupWindow.dismiss(); });
        popupView.findViewById(R.id.menu_type).setOnClickListener(v -> { updateSortMode("TYPE", isDesc); popupWindow.dismiss(); });

        popupView.findViewById(R.id.menu_forward).setOnClickListener(v -> { updateSortMode(field, false); popupWindow.dismiss(); });
        popupView.findViewById(R.id.menu_reverse).setOnClickListener(v -> { updateSortMode(field, true); popupWindow.dismiss(); });

        popupWindow.setElevation(16f);
        popupWindow.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        
        int xOffset = -popupView.getMeasuredWidth() + anchor.getWidth();
        popupWindow.showAsDropDown(anchor, xOffset, 0);
    }
    
    private void updateSortMode(String field, boolean isDesc) {
        if ("TYPE".equals(field)) field = "NAME"; 
        String suffix = isDesc ? "_DESC" : "_ASC";
        sortMode = SortMode.valueOf(field + suffix);
        if (currentPath != null) {
            loadFiles(currentPath);
        }
    }

    private void updateViewMode() {
        RecyclerView rv = findViewById(R.id.recyclerView);
        
        if (viewMode == 0) {
            rv.setLayoutManager(new LinearLayoutManager(this));
        } else if (viewMode == 1) {
            GridLayoutManager glm = new GridLayoutManager(this, 3);
            glm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if (adapter != null && adapter.getItemViewType(position) == 3) return 3;
                    return 1;
                }
            });
            rv.setLayoutManager(glm);
        } else if (viewMode == 2) {
            GridLayoutManager glm = new GridLayoutManager(this, 2);
            glm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if (adapter != null && adapter.getItemViewType(position) == 3) return 2;
                    return 1;
                }
            });
            rv.setLayoutManager(glm);
        }
        
        adapter.setViewMode(viewMode);
    }

    private void updateBreadcrumb(String path) {
        if (breadcrumbContainer == null || rootPath == null) {
            return;
        }
        breadcrumbContainer.removeAllViews();
        addCrumb("Root", rootPath, true);
        if (path != null && path.startsWith(rootPath) && path.length() > rootPath.length()) {
            String rest = path.substring(rootPath.length());
            if (rest.startsWith("/")) {
                rest = rest.substring(1);
            }
            String[] parts = rest.split("/");
            StringBuilder acc = new StringBuilder(rootPath);
            for (String part : parts) {
                if (part.isEmpty()) {
                    continue;
                }
                acc.append("/").append(part);
                addCrumb(part, acc.toString(), false);
            }
        }
        if (breadcrumbScroll != null) {
            breadcrumbScroll.post(() -> breadcrumbScroll.fullScroll(View.FOCUS_RIGHT));
        }
    }

    private void addCrumb(String label, String targetPath, boolean isRoot) {
        if (!isRoot) {
            TextView sep = new TextView(this);
            sep.setText("  \u203A  ");
            sep.setTextColor(0xFF9E9E9E);
            breadcrumbContainer.addView(sep);
        }
        TextView crumb = new TextView(this);
        crumb.setText(label);
        crumb.setTextColor(0xFF1976D2);
        crumb.setTextSize(13);
        crumb.setTypeface(null, android.graphics.Typeface.BOLD);
        crumb.setMaxLines(1);
        int pad = dpToPx(6);
        crumb.setPadding(pad, pad, pad, pad);
        android.util.TypedValue tv = new android.util.TypedValue();
        getTheme().resolveAttribute(android.R.attr.selectableItemBackground, tv, true);
        crumb.setBackgroundResource(tv.resourceId);
        crumb.setOnClickListener(v -> {
            if (!targetPath.equals(currentPath)) {
                loadFiles(targetPath);
            }
        });
        breadcrumbContainer.addView(crumb);
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}
