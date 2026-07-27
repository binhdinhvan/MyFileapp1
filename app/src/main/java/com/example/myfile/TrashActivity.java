package com.example.myfile;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myfile.data.model.FileItem;
import com.example.myfile.data.trash.TrashManager;
import com.example.myfile.ui.main.FileAdapter;
import java.util.ArrayList;
import java.util.List;

public class TrashActivity extends AppCompatActivity implements FileAdapter.OnItemClickListener {

    private TrashManager trashManager;
    private FileAdapter adapter;
    private TextView tvEmpty;
    
    private View defaultToolbar;
    private View selectionToolbar;
    private TextView tvSelectionCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trash);

        trashManager = new TrashManager(this);
        tvEmpty = findViewById(R.id.tvTrashEmpty);
        defaultToolbar = findViewById(R.id.defaultToolbarTrash);
        selectionToolbar = findViewById(R.id.selectionToolbarTrash);
        tvSelectionCount = findViewById(R.id.tvSelectionCountTrash);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewTrash);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FileAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btnEmptyTrash).setOnClickListener(v -> confirmEmptyTrash());
        
        findViewById(R.id.btnSelCancelTrash).setOnClickListener(v -> adapter.exitSelectionMode());
        findViewById(R.id.btnSelRestore).setOnClickListener(v -> bulkRestore());
        findViewById(R.id.btnSelDeleteTrash).setOnClickListener(v -> confirmBulkDelete());

        loadTrash();
    }

    private void loadTrash() {
        int expiredCount = trashManager.cleanupExpiredItems();
        if (expiredCount > 0) {
            Toast.makeText(this, expiredCount + " item(s) auto-deleted after 30 days", Toast.LENGTH_SHORT).show();
        }
        List<FileItem> items = trashManager.listTrashItems();
        items = groupTrashItemsByDate(items);
        adapter.updateData(items);
        tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private List<FileItem> groupTrashItemsByDate(List<FileItem> original) {
        if (original.isEmpty()) return original;
        
        List<FileItem> filesToGroup = new ArrayList<>(original);
        
        // Sort files by date descending before grouping
        java.util.Collections.sort(filesToGroup, (f1, f2) -> Long.compare(f2.getLastModified(), f1.getLastModified()));
        
        List<FileItem> finalItems = new ArrayList<>();
        java.util.Map<String, List<FileItem>> groups = new java.util.LinkedHashMap<>();
        
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        long todayStart = cal.getTimeInMillis();
        long yesterdayStart = todayStart - 86400000L;
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault());
        
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
        showItemOptions(item);
    }

    @Override
    public void onItemLongClick(FileItem item) {
        adapter.enterSelectionMode();
        adapter.toggleSelection(item);
    }

    private void showItemOptions(FileItem item) {
        String[] options = {"Restore", "Delete Permanently"};
        new AlertDialog.Builder(this)
                .setTitle(item.getName())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        boolean ok = trashManager.restore(item.getPath());
                        Toast.makeText(this, ok ? "Restored successfully" : "Restore failed", Toast.LENGTH_SHORT).show();
                        loadTrash();
                    } else {
                        confirmSingleDelete(item);
                    }
                })
                .show();
    }
    
    private void confirmSingleDelete(FileItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Permanently?")
                .setMessage("Are you sure you want to permanently delete " + item.getName() + "? This action cannot be undone.")
                .setPositiveButton("Delete", (d, w) -> {
                    boolean ok = trashManager.permanentlyDelete(item.getPath());
                    Toast.makeText(this, ok ? "Deleted permanently" : "Delete failed", Toast.LENGTH_SHORT).show();
                    loadTrash();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmEmptyTrash() {
        new AlertDialog.Builder(this)
                .setTitle("Empty Trash?")
                .setMessage("All items in trash will be permanently deleted.")
                .setPositiveButton("Empty", (d, w) -> {
                    trashManager.emptyTrash();
                    loadTrash();
                })
                .setNegativeButton("Cancel", null)
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
            if (trashManager.restore(item.getPath())) {
                successCount++;
            }
        }
        Toast.makeText(this, "Restored " + successCount + " items", Toast.LENGTH_SHORT).show();
        adapter.exitSelectionMode();
        loadTrash();
    }
    
    private void confirmBulkDelete() {
        List<FileItem> selected = adapter.getSelectedItems();
        if (selected.isEmpty()) return;
        
        new AlertDialog.Builder(this)
                .setTitle("Delete Permanently?")
                .setMessage("Are you sure you want to permanently delete " + selected.size() + " items? This action cannot be undone.")
                .setPositiveButton("Delete", (d, w) -> {
                    int successCount = 0;
                    for (FileItem item : selected) {
                        if (trashManager.permanentlyDelete(item.getPath())) {
                            successCount++;
                        }
                    }
                    Toast.makeText(this, "Deleted " + successCount + " items", Toast.LENGTH_SHORT).show();
                    adapter.exitSelectionMode();
                    loadTrash();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
