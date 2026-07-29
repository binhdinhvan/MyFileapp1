package com.example.myfile.ui.vault;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myfile.R;
import com.example.myfile.data.model.FileItem;
import com.example.myfile.data.repository.FileRepository;
import com.example.myfile.data.repository.FileRepositoryImpl;
import com.example.myfile.data.vault.PrivateVaultManager;
import com.example.myfile.ui.main.FileAdapter;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PrivateVaultActivity extends AppCompatActivity implements FileAdapter.OnItemClickListener {

    private PrivateVaultManager vaultManager;
    private FileRepository repository;
    private FileAdapter adapter;
    private View tvEmpty;

    private View defaultToolbar;
    private View selectionToolbar;
    private TextView tvSelectionCount;
    private TextView tvVaultPath;
    private View btnVaultUp;
    private View btnVaultPaste;

    private String rootPath;
    private String currentPath;

    // Clipboard for cut/copy within the vault
    private final List<String> clipboardPaths = new ArrayList<>();
    private boolean clipboardIsCut = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_vault);

        vaultManager = new PrivateVaultManager(this);
        repository = new FileRepositoryImpl();
        rootPath = vaultManager.getVaultRootPath();
        currentPath = rootPath;

        tvEmpty = findViewById(R.id.tvVaultEmpty);
        defaultToolbar = findViewById(R.id.defaultToolbarVault);
        selectionToolbar = findViewById(R.id.selectionToolbarVault);
        tvSelectionCount = findViewById(R.id.tvSelectionCountVault);
        tvVaultPath = findViewById(R.id.tvVaultPath);
        btnVaultUp = findViewById(R.id.btnVaultUp);
        btnVaultPaste = findViewById(R.id.btnVaultPaste);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewVault);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FileAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btnSelCancelVault).setOnClickListener(v -> adapter.exitSelectionMode());
        findViewById(R.id.btnSelRestoreVault).setOnClickListener(v -> bulkRestore());
        findViewById(R.id.btnSelCopyVault).setOnClickListener(v -> bulkClipboard(false));
        findViewById(R.id.btnSelCutVault).setOnClickListener(v -> bulkClipboard(true));
        findViewById(R.id.btnSelDeleteVault).setOnClickListener(v -> confirmBulkDelete());

        btnVaultUp.setOnClickListener(v -> navigateUp());
        findViewById(R.id.btnVaultNewFolder).setOnClickListener(v -> showCreateDialog(true));
        findViewById(R.id.btnVaultNewFile).setOnClickListener(v -> showCreateDialog(false));
        btnVaultPaste.setOnClickListener(v -> pasteClipboard());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (adapter.isSelectionMode()) {
                    adapter.exitSelectionMode();
                } else if (!currentPath.equals(rootPath)) {
                    navigateUp();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        loadVault();
    }

    private void loadVault() {
        List<FileItem> items = vaultManager.listItems(currentPath);
        adapter.updateData(items);
        tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
        updatePathBar();
    }

    private void updatePathBar() {
        if (currentPath.equals(rootPath)) {
            tvVaultPath.setText("\uD83D\uDD12 Private Vault");
            btnVaultUp.setVisibility(View.GONE);
        } else {
            String rel = currentPath.substring(rootPath.length());
            if (rel.startsWith("/")) rel = rel.substring(1);
            tvVaultPath.setText("\uD83D\uDD12 " + rel);
            btnVaultUp.setVisibility(View.VISIBLE);
        }
        btnVaultPaste.setVisibility(clipboardPaths.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void navigateUp() {
        if (currentPath.equals(rootPath)) return;
        File parent = new File(currentPath).getParentFile();
        currentPath = parent != null ? parent.getAbsolutePath() : rootPath;
        // Never allow going above the vault root
        if (!currentPath.startsWith(rootPath)) {
            currentPath = rootPath;
        }
        loadVault();
    }

    @Override
    public void onItemClick(FileItem item) {
        if (item.isDirectory()) {
            currentPath = item.getPath();
            loadVault();
        } else {
            openFile(item);
        }
    }

    @Override
    public void onItemLongClick(FileItem item) {
        showItemOptions(item);
    }

    private void showItemOptions(FileItem item) {
        List<String> opts = new ArrayList<>();
        if (!item.isDirectory()) opts.add("Open");
        opts.add("Rename");
        opts.add("Copy");
        opts.add("Cut");
        opts.add("Delete");
        if (vaultManager.isRestorable(item.getPath())) {
            opts.add("Restore to original location");
        }
        opts.add("Properties");
        opts.add("Select");
        String[] options = opts.toArray(new String[0]);

        new AlertDialog.Builder(this)
                .setTitle(item.getName())
                .setItems(options, (dialog, which) -> {
                    String sel = options[which];
                    switch (sel) {
                        case "Open": openFile(item); break;
                        case "Rename": showRenameDialog(item); break;
                        case "Copy": setClipboard(java.util.Collections.singletonList(item.getPath()), false); break;
                        case "Cut": setClipboard(java.util.Collections.singletonList(item.getPath()), true); break;
                        case "Delete": confirmSingleDelete(item); break;
                        case "Restore to original location": restoreSingle(item); break;
                        case "Properties": showProperties(item); break;
                        case "Select":
                            adapter.enterSelectionMode();
                            adapter.toggleSelection(item);
                            break;
                    }
                })
                .show();
    }

    private void openFile(FileItem item) {
        String filePath = item.getPath();
        String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(filePath);
        String mimeType = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
        if (mimeType == null) mimeType = "*/*";
        if (mimeType.startsWith("image/") || mimeType.startsWith("video/") || mimeType.startsWith("text/")) {
            com.example.myfile.feature.viewer.ViewerFactory.open(this, item, adapter.getItems());
        } else {
            android.net.Uri uri = androidx.core.content.FileProvider.getUriForFile(
                    this, getApplicationContext().getPackageName() + ".fileprovider", new java.io.File(filePath));
            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
            intent.setDataAndType(uri, mimeType);
            intent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                startActivity(android.content.Intent.createChooser(intent, "Open with"));
            } catch (Exception e) {
                Toast.makeText(this, "Cannot open file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showCreateDialog(boolean isFolder) {
        EditText input = new EditText(this);
        input.setHint(isFolder ? "Folder name" : "File name (e.g. note.txt)");
        new AlertDialog.Builder(this)
                .setTitle(isFolder ? "New Folder" : "New File")
                .setView(input)
                .setPositiveButton("Create", (d, w) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    boolean ok = isFolder
                            ? repository.createFolder(currentPath, name)
                            : repository.createFile(currentPath, name);
                    Toast.makeText(this, ok ? "Created" : "Failed (name already exists?)", Toast.LENGTH_SHORT).show();
                    if (ok) loadVault();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showRenameDialog(FileItem item) {
        EditText input = new EditText(this);
        input.setText(item.getName());
        input.setSelection(item.getName().length());
        new AlertDialog.Builder(this)
                .setTitle("Rename")
                .setView(input)
                .setPositiveButton("Rename", (d, w) -> {
                    String newName = input.getText().toString().trim();
                    if (newName.isEmpty()) {
                        Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    boolean ok = repository.rename(item.getPath(), newName);
                    if (ok) {
                        // A renamed moved-in file loses its restore link; drop the stale mapping.
                        vaultManager.clearMapping(item.getPath());
                    }
                    Toast.makeText(this, ok ? "Renamed" : "Failed (name already exists?)", Toast.LENGTH_SHORT).show();
                    if (ok) loadVault();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setClipboard(List<String> paths, boolean isCut) {
        clipboardPaths.clear();
        clipboardPaths.addAll(paths);
        clipboardIsCut = isCut;
        Toast.makeText(this, paths.size() + (isCut ? " item(s) cut" : " item(s) copied") + ". Navigate and tap Paste.", Toast.LENGTH_SHORT).show();
        updatePathBar();
    }

    private void bulkClipboard(boolean isCut) {
        List<FileItem> selected = adapter.getSelectedItems();
        if (selected.isEmpty()) {
            Toast.makeText(this, "No items selected", Toast.LENGTH_SHORT).show();
            return;
        }
        List<String> paths = new ArrayList<>();
        for (FileItem it : selected) paths.add(it.getPath());
        setClipboard(paths, isCut);
        adapter.exitSelectionMode();
    }

    private void pasteClipboard() {
        if (clipboardPaths.isEmpty()) return;
        int success = 0;
        for (String src : clipboardPaths) {
            boolean ok = clipboardIsCut
                    ? repository.move(src, currentPath)
                    : repository.copy(src, currentPath);
            if (ok) {
                success++;
                if (clipboardIsCut) vaultManager.clearMapping(src);
            }
        }
        Toast.makeText(this, "Pasted " + success + "/" + clipboardPaths.size() + " item(s)", Toast.LENGTH_SHORT).show();
        clipboardPaths.clear();
        loadVault();
    }

    private void restoreSingle(FileItem item) {
        boolean ok = vaultManager.restore(item.getPath());
        Toast.makeText(this, ok ? "Restored successfully" : "Restore failed", Toast.LENGTH_SHORT).show();
        loadVault();
    }

    private void confirmSingleDelete(FileItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Permanently?")
                .setMessage("\"" + item.getName() + "\" will be permanently deleted from the vault. This cannot be undone.")
                .setPositiveButton("Delete", (d, w) -> {
                    boolean ok = repository.delete(item.getPath());
                    if (ok) vaultManager.clearMapping(item.getPath());
                    Toast.makeText(this, ok ? "Deleted" : "Delete failed", Toast.LENGTH_SHORT).show();
                    loadVault();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showProperties(FileItem item) {
        File f = new File(item.getPath());
        String type = item.isDirectory() ? "Folder" : "File";
        String size = item.isDirectory() ? "-" : Formatter.formatShortFileSize(this, item.getSize());
        String modified = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                .format(new Date(item.getLastModified()));
        String msg = "Name: " + item.getName()
                + "\nType: " + type
                + "\nSize: " + size
                + "\nModified: " + modified
                + "\nLocation: " + f.getParent();
        new AlertDialog.Builder(this)
                .setTitle("Properties")
                .setMessage(msg)
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    public void onSelectionChanged(boolean selectionMode, int count) {
        if (selectionMode) {
            defaultToolbar.setVisibility(View.GONE);
            selectionToolbar.setVisibility(View.VISIBLE);
            tvSelectionCount.setText(count + " selected");
        } else {
            defaultToolbar.setVisibility(View.VISIBLE);
            selectionToolbar.setVisibility(View.GONE);
        }
    }

    private void bulkRestore() {
        List<FileItem> selected = adapter.getSelectedItems();
        if (selected.isEmpty()) return;

        int successCount = 0;
        for (FileItem item : selected) {
            if (vaultManager.isRestorable(item.getPath()) && vaultManager.restore(item.getPath())) {
                successCount++;
            }
        }
        Toast.makeText(this, "Restored " + successCount + " item(s)", Toast.LENGTH_SHORT).show();
        adapter.exitSelectionMode();
        loadVault();
    }

    private void confirmBulkDelete() {
        List<FileItem> selected = adapter.getSelectedItems();
        if (selected.isEmpty()) {
            Toast.makeText(this, "No items selected", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Delete Permanently?")
                .setMessage(selected.size() + " item(s) will be permanently deleted from the vault. This cannot be undone.")
                .setPositiveButton("Delete", (d, w) -> {
                    int successCount = 0;
                    for (FileItem item : selected) {
                        if (repository.delete(item.getPath())) {
                            vaultManager.clearMapping(item.getPath());
                            successCount++;
                        }
                    }
                    Toast.makeText(this, "Deleted " + successCount + " item(s)", Toast.LENGTH_SHORT).show();
                    adapter.exitSelectionMode();
                    loadVault();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
                      }

