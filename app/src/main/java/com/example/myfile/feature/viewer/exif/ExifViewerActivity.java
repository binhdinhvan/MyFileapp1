package com.example.myfile.feature.viewer.exif;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myfile.R;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * [C] Xem metadata EXIF cua 1 anh. Doc file bang ExecutorService rieng (khong
 * chan UI thread) - du an nay chua co lop AppExecutors dung chung nen tao
 * ExecutorService cuc bo, huy khi Activity dong.
 */
public class ExifViewerActivity extends AppCompatActivity {

    private static final String EXTRA_IMAGE_PATH = "extra_image_path";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public static void start(Context context, String imagePath) {
        if (imagePath == null) return;
        Intent intent = new Intent(context, ExifViewerActivity.class);
        intent.putExtra(EXTRA_IMAGE_PATH, imagePath);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exif_viewer);

        String path = getIntent().getStringExtra(EXTRA_IMAGE_PATH);
        TextView tvTitle = findViewById(R.id.tvExifTitle);
        ProgressBar progressBar = findViewById(R.id.progressBarExif);
        TextView tvEmpty = findViewById(R.id.tvExifEmpty);
        RecyclerView recyclerView = findViewById(R.id.recyclerExif);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.btnExifBack).setOnClickListener(v -> finish());

        if (path == null) {
            finish();
            return;
        }
        File file = new File(path);
        tvTitle.setText(file.getName());

        progressBar.setVisibility(View.VISIBLE);
        executor.execute(() -> {
            List<ExifReader.Entry> entries = ExifReader.read(file);
            mainHandler.post(() -> {
                progressBar.setVisibility(View.GONE);
                if (entries.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setAdapter(new ExifAdapter(entries));
                }
            });
        });
    }

    @Override
    protected void onDestroy() {
        executor.shutdownNow();
        super.onDestroy();
    }
}
