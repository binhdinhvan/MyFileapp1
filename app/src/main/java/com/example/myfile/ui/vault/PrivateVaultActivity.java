package com.example.myfile.ui.vault;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myfile.R;
import com.example.myfile.data.model.FileItem;
import com.example.myfile.data.vault.PrivateVaultManager;
import com.example.myfile.ui.main.FileAdapter;
import java.util.ArrayList;
import java.util.List;

public class PrivateVaultActivity extends AppCompatActivity implements FileAdapter.OnItemClickListener {

    private PrivateVaultManager vaultManager;
    private FileAdapter adapter;
    private View tvEmpty;
    
    private View defaultToolbar;
    private View selectionToolbar;
    private TextView tvSelectionCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_vault);

        vaultManager = new PrivateVaultManager(this);
        tvEmpty = findViewById(R.id.tvVaultEmpty);
        defaultToolbar = findViewById(R.id.defaultToolbarVault);
        selectionToolbar = findViewById(R.id.selectionToolbarVault);
        tvSelectionCount = findViewById(R.id.tvSelectionCountVault);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewVault);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FileAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btnSelCancelVault).setOnClickListener(v -> adapter.exitSelectionMode());
        findViewById(R.id.btnSelRestoreVault).setOnClickListener(v -> bulkRestore());

        loadVault();
    }

    private void loadVault() {
        List<FileItem> items = vaultManager.listVaultItems();
        adapter.updateData(items);
        tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
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
        String[] options = {"Restore to original location"};
        new AlertDialog.Builder(this)
                .setTitle(item.getName())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        boolean ok = vaultManager.restore(item.getPath());
                        Toast.makeText(this, ok ? "Restored successfully" : "Restore failed", Toast.LENGTH_SHORT).show();
                        loadVault();
                    }
                })
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
            if (vaultManager.restore(item.getPath())) {
                successCount++;
            }
        }
        Toast.makeText(this, "Restored " + successCount + " items", Toast.LENGTH_SHORT).show();
        adapter.exitSelectionMode();
        loadVault();
    }
}
