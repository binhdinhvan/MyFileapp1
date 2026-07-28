package com.example.myfile.feature.viewer;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.myfile.data.model.FileItem;
import com.example.myfile.feature.viewer.image.ImageViewerActivity;
import com.example.myfile.feature.viewer.text.TextViewerActivity;
import com.example.myfile.feature.viewer.video.VideoPlayerActivity;
import com.example.myfile.utils.FileUtils;
import com.example.myfile.utils.MimeUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * ⚑ FACTORY — noi duy nhat quyet dinh "mo file nay bang gi".
 *
 * Goi tu MainActivity.onItemClick() khi nguoi dung tap vao 1 file (khong
 * phai thu muc). "siblings" la danh sach file dang hien thi trong thu muc
 * hien tai (cung list cua adapter), dung de ImageViewer/VideoPlayer cho
 * vuot/next-prev giua cac file cung loai.
 *
 * Dung String path (khong dung FileItem lam Parcelable) vi FileItem hien
 * tai (data.model.FileItem) chua implement Parcelable.
 */
public final class ViewerFactory {

    private ViewerFactory() { }

    public static void open(Context context, FileItem clicked, List<FileItem> siblings) {
        if (clicked.isDirectory()) return;

        MimeUtils.Category category = MimeUtils.categoryOf(clicked.getName());
        switch (category) {
            case IMAGE:
                ImageViewerActivity.start(context, clicked.getPath(), collectPaths(siblings, MimeUtils.Category.IMAGE));
                return;
            case VIDEO:
                VideoPlayerActivity.start(context, clicked.getPath(), collectPaths(siblings, MimeUtils.Category.VIDEO));
                return;
            case TEXT:
                TextViewerActivity.start(context, clicked.getPath());
                return;
            default:
                openWithSystemViewer(context, clicked);
        }
    }

    private static ArrayList<String> collectPaths(List<FileItem> siblings, MimeUtils.Category category) {
        ArrayList<String> paths = new ArrayList<>();
        if (siblings == null) return paths;
        for (FileItem item : siblings) {
            if (!item.isDirectory() && MimeUtils.categoryOf(item.getName()) == category) {
                paths.add(item.getPath());
            }
        }
        return paths;
    }

    /** APK, DOC, PDF... khong co viewer rieng -> nho app ngoai xu ly qua FileProvider. */
    private static void openWithSystemViewer(Context context, FileItem item) {
        try {
            File file = new File(item.getPath());
            android.net.Uri uri = FileUtils.contentUriFor(context, file);
            String mime = MimeUtils.mimeTypeOf(item.getName());

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, mime);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, "No suitable app found to open this file", Toast.LENGTH_SHORT).show();
        }
    }
}
