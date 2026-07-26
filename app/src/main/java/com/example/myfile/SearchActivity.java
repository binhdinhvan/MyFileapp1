package com.example.myfile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myfile.data.model.FileItem;
import com.example.myfile.ui.main.FileAdapter;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SearchActivity extends AppCompatActivity {

    private EditText etSearchQuery;
    private View btnClear;
    private View btnBack;
    private View layoutSuggestions;
    private View layoutResults;
    private View layoutLoading;
    private TextView tvResultsCount;
    private RecyclerView rvSearchResults;
    private FileAdapter adapter;
    private List<FileItem> searchResults = new ArrayList<>();
    
    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private boolean isProgrammaticTextChange = false;
    
    private static final String PREF_NAME = "search_prefs";
    private static final String KEY_RECENT = "recent_queries";
    
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Future<?> currentSearchTask;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        
        etSearchQuery = findViewById(R.id.etSearchQuery);
        btnClear = findViewById(R.id.btnClear);
        btnBack = findViewById(R.id.btnBack);
        layoutSuggestions = findViewById(R.id.layoutSuggestions);
        layoutResults = findViewById(R.id.layoutResults);
        layoutLoading = findViewById(R.id.layoutLoading);
        tvResultsCount = findViewById(R.id.tvResultsCount);
        rvSearchResults = findViewById(R.id.rvSearchResults);
        
        adapter = new FileAdapter(searchResults, new FileAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(FileItem item) {
                if (item.isDirectory()) {
                    Intent intent = new Intent(SearchActivity.this, MainActivity.class);
                    intent.putExtra("OPEN_FOLDER", item.getPath());
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(SearchActivity.this, "File: " + item.getName(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onItemLongClick(FileItem item) {}
            @Override
            public void onSelectionChanged(boolean selectionMode, int count) {}
        });
        
        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
        rvSearchResults.setAdapter(adapter);
        
        btnBack.setOnClickListener(v -> finish());
        
        btnClear.setOnClickListener(v -> {
            etSearchQuery.setText("");
            showSuggestions();
        });
        
        findViewById(R.id.btnClearRecent).setOnClickListener(v -> {
            getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().remove(KEY_RECENT).apply();
            loadRecentSearches();
        });
        
        etSearchQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnClear.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                
                if (isProgrammaticTextChange) {
                    return;
                }
                
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    showSuggestions();
                } else {
                    searchRunnable = () -> performSearch(query, null);
                    searchHandler.postDelayed(searchRunnable, 500); // 500ms delay before triggering search
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        etSearchQuery.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = etSearchQuery.getText().toString().trim();
                if (!query.isEmpty()) {
                    performSearch(query, null);
                }
                return true;
            }
            return false;
        });
        
        setupChips();
        loadRecentSearches();
        etSearchQuery.requestFocus();
    }
    
    private void setupChips() {
        findViewById(R.id.chipDocs).setOnClickListener(v -> performSearch(null, "docs"));
        findViewById(R.id.chipImages).setOnClickListener(v -> performSearch(null, "images"));
        findViewById(R.id.chipVideos).setOnClickListener(v -> performSearch(null, "videos"));
        findViewById(R.id.chipMusic).setOnClickListener(v -> performSearch(null, "music"));
        findViewById(R.id.chipArchives).setOnClickListener(v -> performSearch(null, "archives"));
        findViewById(R.id.chipApks).setOnClickListener(v -> performSearch(null, "apks"));
        findViewById(R.id.chipFolders).setOnClickListener(v -> performSearch(null, "folders"));
    }
    
    private void showSuggestions() {
        layoutSuggestions.setVisibility(View.VISIBLE);
        layoutResults.setVisibility(View.GONE);
        layoutLoading.setVisibility(View.GONE);
        findViewById(R.id.chipGroupCategories).setVisibility(View.VISIBLE);
        loadRecentSearches();
        if (currentSearchTask != null) {
            currentSearchTask.cancel(true);
        }
    }
    
    private void showResults() {
        layoutSuggestions.setVisibility(View.GONE);
        layoutResults.setVisibility(View.VISIBLE);
        layoutLoading.setVisibility(View.GONE);
    }
    
    private void performSearch(String query, String category) {
        if (currentSearchTask != null) {
            currentSearchTask.cancel(true);
        }
        
        if (category != null) {
            isProgrammaticTextChange = true;
            etSearchQuery.setText(category.substring(0, 1).toUpperCase() + category.substring(1));
            etSearchQuery.setSelection(etSearchQuery.getText().length());
            isProgrammaticTextChange = false;
        } else if (query != null && !query.isEmpty()) {
            saveRecentQuery(query);
        }
        
        searchResults.clear();
        adapter.notifyDataSetChanged();
        
        layoutSuggestions.setVisibility(View.VISIBLE);
        layoutResults.setVisibility(View.GONE);
        layoutLoading.setVisibility(View.VISIBLE);
        findViewById(R.id.chipGroupCategories).setVisibility(View.GONE);
        
        File root = Environment.getExternalStorageDirectory();
        
        currentSearchTask = executorService.submit(() -> {
            List<FileItem> results = new ArrayList<>();
            searchRecursive(root, query, category, results);
            
            runOnUiThread(() -> {
                findViewById(R.id.chipGroupCategories).setVisibility(View.VISIBLE);
                searchResults.clear();
                searchResults.addAll(results);
                adapter.notifyDataSetChanged();
                tvResultsCount.setText("Found " + results.size() + " items");
                showResults();
            });
        });
    }
    
    private void searchRecursive(File dir, String query, String category, List<FileItem> results) {
        if (Thread.currentThread().isInterrupted()) return;
        
        File[] files = dir.listFiles();
        if (files == null) return;
        
        for (File f : files) {
            if (Thread.currentThread().isInterrupted()) return;
            
            boolean match = false;
            if (query != null && !query.isEmpty()) {
                if (f.getName().toLowerCase(Locale.getDefault()).contains(query.toLowerCase(Locale.getDefault()))) {
                    match = true;
                }
            } else if (category != null) {
                if (f.isDirectory() && category.equals("folders")) {
                    match = true;
                } else if (!f.isDirectory()) {
                    String ext = getExtension(f.getName()).toLowerCase();
                    switch (category) {
                        case "docs": match = Arrays.asList("doc","docx","pdf","txt","xls","xlsx","ppt","pptx").contains(ext); break;
                        case "images": match = Arrays.asList("jpg","jpeg","png","gif","webp").contains(ext); break;
                        case "videos": match = Arrays.asList("mp4","mkv","avi","mov").contains(ext); break;
                        case "music": match = Arrays.asList("mp3","wav","flac","ogg","m4a").contains(ext); break;
                        case "archives": match = Arrays.asList("zip","rar","7z","tar","gz").contains(ext); break;
                        case "apks": match = ext.equals("apk"); break;
                    }
                }
            }
            
            if (match) {
                results.add(FileItem.fromFile(f));
                if (results.size() % 15 == 0) {
                    List<FileItem> currentBatch = new ArrayList<>(results);
                    runOnUiThread(() -> {
                        searchResults.clear();
                        searchResults.addAll(currentBatch);
                        adapter.notifyDataSetChanged();
                        tvResultsCount.setText("Found " + currentBatch.size() + " items...");
                        showResults();
                    });
                }
            }
            
            if (f.isDirectory() && !f.getName().startsWith(".")) { // Skip hidden folders to speed up
                searchRecursive(f, query, category, results);
            }
        }
    }
    
    private void saveRecentQuery(String query) {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String current = prefs.getString(KEY_RECENT, "");
        List<String> list = new ArrayList<>(Arrays.asList(current.split(";;")));
        list.remove("");
        list.remove(query);
        list.add(0, query);
        if (list.size() > 5) {
            list = list.subList(0, 5);
        }
        prefs.edit().putString(KEY_RECENT, android.text.TextUtils.join(";;", list)).apply();
    }
    
    private void loadRecentSearches() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String current = prefs.getString(KEY_RECENT, "");
        List<String> list = new ArrayList<>(Arrays.asList(current.split(";;")));
        list.remove("");
        
        View container = findViewById(R.id.layoutRecentSearchesContainer);
        android.widget.LinearLayout listLayout = findViewById(R.id.layoutRecentSearchesList);
        listLayout.removeAllViews();
        
        if (list.isEmpty()) {
            container.setVisibility(View.GONE);
            return;
        }
        
        container.setVisibility(View.VISIBLE);
        for (String q : list) {
            View item = getLayoutInflater().inflate(R.layout.item_recent_search, listLayout, false);
            TextView tv = item.findViewById(R.id.tvRecentQuery);
            tv.setText(q);
            item.setOnClickListener(v -> {
                isProgrammaticTextChange = true;
                etSearchQuery.setText(q);
                etSearchQuery.setSelection(q.length());
                isProgrammaticTextChange = false;
                performSearch(q, null);
            });
            listLayout.addView(item);
        }
    }

    private String getExtension(String name) {
        int dotIndex = name.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < name.length() - 1) {
            return name.substring(dotIndex + 1);
        }
        return "";
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }
}
