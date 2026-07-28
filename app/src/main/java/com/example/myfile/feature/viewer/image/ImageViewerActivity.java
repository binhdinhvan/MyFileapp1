package com.example.myfile.feature.viewer.image;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myfile.R;
import com.example.myfile.feature.viewer.exif.ExifViewerActivity;
import com.example.myfile.utils.FileUtils;
import com.example.myfile.utils.MimeUtils;

import java.io.File;
import java.util.ArrayList;

/**
 * [C] Xem anh toan man hinh: Glide (giai ma) + PhotoView (zoom) + ViewPager2 (vuot trai/phai
 * giua cac anh cung thu muc).
 */
public class ImageViewerActivity extends AppCompatActivity {

    private static final String EXTRA_START_PATH = "extra_start_path";
    private static final String EXTRA_IMAGE_PATHS = "extra_image_paths";

    private ArrayList<String> imagePaths;
    private ViewPager2 viewPager;
    private TextView tvTitle;
    private TextView tvCounter;

    public static void start(Context context, String clickedPath, ArrayList<String> siblingImagePaths) {
        Intent intent = new Intent(context, ImageViewerActivity.class);
        if (siblingImagePaths == null || siblingImagePaths.isEmpty()) {
            siblingImagePaths = new ArrayList<>();
            siblingImagePaths.add(clickedPath);
        }
        intent.putExtra(EXTRA_START_PATH, clickedPath);
        intent.putStringArrayListExtra(EXTRA_IMAGE_PATHS, siblingImagePaths);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        imagePaths = getIntent().getStringArrayListExtra(EXTRA_IMAGE_PATHS);
        if (imagePaths == null) imagePaths = new ArrayList<>();
        String startPath = getIntent().getStringExtra(EXTRA_START_PATH);

        tvTitle = findViewById(R.id.tvImageTitle);
        tvCounter = findViewById(R.id.tvImageCounter);
        viewPager = findViewById(R.id.viewPager);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnExif).setOnClickListener(v -> ExifViewerActivity.start(this, currentPath()));
        findViewById(R.id.btnShare).setOnClickListener(v -> shareCurrent());

        viewPager.setAdapter(new ImagePagerAdapter(imagePaths));

        int startIndex = Math.max(0, imagePaths.indexOf(startPath));
        viewPager.setCurrentItem(startIndex, false);
        updateHeader(startIndex);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateHeader(position);
            }
        });
    }

    private void updateHeader(int position) {
        if (imagePaths.isEmpty()) return;
        tvTitle.setText(new File(imagePaths.get(position)).getName());
        tvCounter.setText((position + 1) + " / " + imagePaths.size());
    }

    private String currentPath() {
        int pos = viewPager.getCurrentItem();
        return imagePaths.isEmpty() ? null : imagePaths.get(pos);
    }

    private void shareCurrent() {
        String path = currentPath();
        if (path == null) return;
        File file = new File(path);
        Uri uri = FileUtils.contentUriFor(this, file);
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType(MimeUtils.mimeTypeOf(file.getName()));
        share.putExtra(Intent.EXTRA_STREAM, uri);
        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivity(Intent.createChooser(share, "Share image"));
        } catch (Exception e) {
            Toast.makeText(this, "Unable to share this file", Toast.LENGTH_SHORT).show();
        }
    }
}
