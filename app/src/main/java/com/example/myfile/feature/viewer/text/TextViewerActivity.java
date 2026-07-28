package com.example.myfile.feature.viewer.text;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myfile.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** [C] (optional) Xem nhanh noi dung file .txt/.log/.json/.md... */
public class TextViewerActivity extends AppCompatActivity {

    private static final String EXTRA_FILE_PATH = "extra_file_path";
    private static final long MAX_BYTES = 2L * 1024 * 1024; // 2MB, tranh treo UI voi file qua lon

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public static void start(Context context, String filePath) {
        if (filePath == null) return;
        Intent intent = new Intent(context, TextViewerActivity.class);
        intent.putExtra(EXTRA_FILE_PATH, filePath);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_viewer);

        String path = getIntent().getStringExtra(EXTRA_FILE_PATH);
        TextView tvTitle = findViewById(R.id.tvTextTitle);
        ProgressBar progressBar = findViewById(R.id.progressBarText);
        TextView tvContent = findViewById(R.id.tvTextContent);

        findViewById(R.id.btnTextBack).setOnClickListener(v -> finish());

        if (path == null) {
            finish();
            return;
        }
        File file = new File(path);
        tvTitle.setText(file.getName());

        progressBar.setVisibility(View.VISIBLE);
        executor.execute(() -> {
            String content;
            try {
                content = readText(file);
            } catch (IOException e) {
                content = "Failed to read file: " + e.getMessage();
            }
            String finalContent = content;
            mainHandler.post(() -> {
                progressBar.setVisibility(View.GONE);
                tvContent.setText(finalContent.isEmpty() ? "(Empty file)" : finalContent);
            });
        });
    }

    private String readText(File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (sb.length() > MAX_BYTES) {
                    sb.append("\n\n--- (Truncated, file too large) ---");
                    break;
                }
                sb.append(line).append('\n');
            }
        }
        return sb.toString();
    }

    @Override
    protected void onDestroy() {
        executor.shutdownNow();
        super.onDestroy();
    }
}
