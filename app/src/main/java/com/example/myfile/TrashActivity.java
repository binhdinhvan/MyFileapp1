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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trash);

        trashManager = new TrashManager(this);
        tvEmpty = findViewById(R.id.tvTrashEmpty);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewTrash);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FileAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btnEmptyTrash).setOnClickListener(v -> confirmEmptyTrash());

        loadTrash();
    }

    private void loadTrash() {
        int expiredCount = trashManager.cleanupExpiredItems();
        if (expiredCount > 0) {
            Toast.makeText(this, expiredCount + " item(s) auto-deleted after 30 days", Toast.LENGTH_SHORT).show();
        }
        List<FileItem> items = trashManager.listTrashItems();
        adapter.updateData(items);
        tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onItemClick(FileItem item) {
        showItemOptions(item);
    }

    @Override
    public void onItemLongClick(FileItem item) {
        showItemOptions(item);
    }

    private void showItemOptions(FileItem item) {
        String[] options = {"Restore", "Delete Permanently"};
        new AlertDialog.Builder(this)
                .setTitle(item.getName())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        boolean ok = trashManager.restore(item.getPath());
                        Toast.makeText(this, ok ? "Restored successfully" : "Restore failed (destination exists?)", Toast.LENGTH_SHORT).show();
                        loadTrash();
                    } else {
                        boolean ok = trashManager.permanentlyDelete(item.getPath());
                        Toast.makeText(this, ok ? "Deleted permanently" : "Delete failed", Toast.LENGTH_SHORT).show();
                        loadTrash();
                    }
                })
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
    }
}
