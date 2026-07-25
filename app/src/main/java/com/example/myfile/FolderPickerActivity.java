package com.example.myfile;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myfile.data.model.FileItem;
import com.example.myfile.data.repository.FileRepository;
import com.example.myfile.data.repository.FileRepositoryImpl;
import com.example.myfile.ui.main.FileAdapter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FolderPickerActivity extends AppCompatActivity implements FileAdapter.OnItemClickListener {

    public static final String EXTRA_START_PATH = "start_path";
    public static final String EXTRA_SELECTED_PATH = "selected_path";

    private FileRepository fileRepository;
    private FileAdapter adapter;
    private TextView tvCurrentPath;
    private TextView btnUp;
    private TextView tvEmpty;
    private String rootPath;
    private String currentPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_picker);

        fileRepository = new FileRepositoryImpl();
        tvCurrentPath = findViewById(R.id.tvPickerPath);
        btnUp = findViewById(R.id.btnUp);
        tvEmpty = findViewById(R.id.tvPickerEmpty);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewPicker);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FileAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        String startPath = getIntent().getStringExtra(EXTRA_START_PATH);
        currentPath = startPath != null ? startPath : rootPath;
        loadFiles(currentPath);

        btnUp.setOnClickListener(v -> {
            File parent = new File(currentPath).getParentFile();
            if (parent != null) {
                loadFiles(parent.getAbsolutePath());
            }
        });

        findViewById(R.id.btnSelectFolder).setOnClickListener(v -> {
            Intent result = new Intent();
            result.putExtra(EXTRA_SELECTED_PATH, currentPath);
            setResult(RESULT_OK, result);
            finish();
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (!currentPath.equals(rootPath)) {
                    File parent = new File(currentPath).getParentFile();
                    if (parent != null) {
                        loadFiles(parent.getAbsolutePath());
                    } else {
                        loadFiles(rootPath);
                    }
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    private void loadFiles(String path) {
        currentPath = path;
        tvCurrentPath.setText(path);
        List<FileItem> items = fileRepository.list(path);
        List<FileItem> foldersOnly = new ArrayList<>();
        for (FileItem item : items) {
            if (item.isDirectory()) {
                foldersOnly.add(item);
            }
        }
        adapter.updateData(foldersOnly);
        tvEmpty.setVisibility(foldersOnly.isEmpty() ? View.VISIBLE : View.GONE);
        btnUp.setVisibility(path.equals(rootPath) ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onItemClick(FileItem item) {
        if (item.isDirectory()) {
            loadFiles(item.getPath());
        }
    }

    @Override
    public void onItemLongClick(FileItem item) {
    }

    @Override
    public void onSelectionChanged(boolean selectionMode, int count) {
    }
}
