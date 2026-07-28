package com.example.myfile.feature.viewer.video;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myfile.R;
import com.example.myfile.utils.FileUtils;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.PlayerView;

import java.io.File;
import java.util.ArrayList;

/**
 * [C] Phat video bang ExoPlayer, cho phep chuyen Next/Previous giua cac
 * video khac trong cung thu muc (danh sach path duoc ViewerFactory gom san).
 */
public class VideoPlayerActivity extends AppCompatActivity {

    private static final String EXTRA_START_PATH = "extra_start_path";
    private static final String EXTRA_VIDEO_PATHS = "extra_video_paths";

    private ArrayList<String> videoPaths;
    private int currentIndex;
    private ExoPlayer player;
    private TextView tvTitle;
    private View btnNext;
    private View btnPrevious;

    public static void start(Context context, String clickedPath, ArrayList<String> siblingVideoPaths) {
        Intent intent = new Intent(context, VideoPlayerActivity.class);
        if (siblingVideoPaths == null || siblingVideoPaths.isEmpty()) {
            siblingVideoPaths = new ArrayList<>();
            siblingVideoPaths.add(clickedPath);
        }
        intent.putExtra(EXTRA_START_PATH, clickedPath);
        intent.putStringArrayListExtra(EXTRA_VIDEO_PATHS, siblingVideoPaths);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        videoPaths = getIntent().getStringArrayListExtra(EXTRA_VIDEO_PATHS);
        if (videoPaths == null) videoPaths = new ArrayList<>();
        String startPath = getIntent().getStringExtra(EXTRA_START_PATH);
        currentIndex = Math.max(0, videoPaths.indexOf(startPath));

        tvTitle = findViewById(R.id.tvVideoTitle);
        btnNext = findViewById(R.id.btnVideoNext);
        btnPrevious = findViewById(R.id.btnVideoPrevious);
        PlayerView playerView = findViewById(R.id.playerView);

        findViewById(R.id.btnVideoBack).setOnClickListener(v -> finish());
        btnNext.setOnClickListener(v -> {
            if (currentIndex < videoPaths.size() - 1) {
                currentIndex++;
                playCurrent();
            }
        });
        btnPrevious.setOnClickListener(v -> {
            if (currentIndex > 0) {
                currentIndex--;
                playCurrent();
            }
        });

        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        playCurrent();
    }

    private void playCurrent() {
        if (videoPaths.isEmpty()) return;
        String path = videoPaths.get(currentIndex);
        File file = new File(path);
        tvTitle.setText(file.getName());

        Uri uri = FileUtils.contentUriFor(this, file);
        player.setMediaItem(MediaItem.fromUri(uri));
        player.prepare();
        player.setPlayWhenReady(true);

        btnNext.setVisibility(currentIndex < videoPaths.size() - 1 ? View.VISIBLE : View.GONE);
        btnPrevious.setVisibility(currentIndex > 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (player != null) player.setPlayWhenReady(false);
    }

    @Override
    protected void onDestroy() {
        if (player != null) {
            player.release();
            player = null;
        }
        super.onDestroy();
    }
}
