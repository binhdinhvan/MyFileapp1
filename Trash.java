package com.example.myfile.data.trash;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import com.example.myfile.data.model.FileItem;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TrashManager {

    private static final String PREFS_NAME = "trash_metadata";
    private final File trashDir;
    private final SharedPreferences prefs;
    private final Context context;

    public TrashManager(Context context) {
        this.context = context.getApplicationContext();
        trashDir = new File(Environment.getExternalStorageDirectory(), ".AppTrash");
        if (!trashDir.exists()) {
            trashDir.mkdirs();
        }
        // .nomedia: bao MediaScanner bo qua thu muc nay -> anh/video trong thung rac
        // khong con hien o Gallery / Google Photos.
        File noMedia = new File(trashDir, ".nomedia");
        if (!noMedia.exists()) {
            try { noMedia.createNewFile(); } catch (java.io.IOException ignored) {}
        }
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /** Xoa vet cua file o path cu trong MediaStore de Gallery khong con hien thumbnail chet. */
    private void removeFromMediaStore(String path) {
        try {
            context.getContentResolver().delete(
                    android.provider.MediaStore.Files.getContentUri("external"),
                    android.provider.MediaStore.Files.FileColumns.DATA + "=?",
                    new String[]{ path });
        } catch (Exception ignored) {}
    }

    public String moveToTrash(String originalPath) {
        File source = new File(originalPath);
        if (!source.exists()) {
            return null;
        }
        String trashedName = System.currentTimeMillis() + "_" + source.getName();
        File dest = new File(trashDir, trashedName);
        if (!source.renameTo(dest)) {
            return null;
        }
        removeFromMediaStore(originalPath); // xoa vet o Gallery
        prefs.edit().putString(trashedName, originalPath).apply();
        return dest.getAbsolutePath();
    }

    public boolean restore(String trashedPath) {
        File trashedFile = new File(trashedPath);
        if (!trashedFile.exists()) {
            return false;
        }
        String trashedName = trashedFile.getName();
        String originalPath = prefs.getString(trashedName, null);
        if (originalPath == null) {
            return false;
        }
        File originalFile = new File(originalPath);
        File originalParent = originalFile.getParentFile();
        if (originalParent != null && !originalParent.exists()) {
            originalParent.mkdirs();
        }
        if (originalFile.exists()) {
            return false;
        }
        boolean ok = trashedFile.renameTo(originalFile);
        if (ok) {
            // Quet lai de file khoi phuc hien lai trong Gallery
            android.media.MediaScannerConnection.scanFile(
                    context, new String[]{ originalPath }, null, null);
            prefs.edit().remove(trashedName).apply();
        }
        return ok;
    }

    public boolean permanentlyDelete(String trashedPath) {
        File trashedFile = new File(trashedPath);
        String trashedName = trashedFile.getName();
        boolean ok = deleteRecursive(trashedFile);
        if (ok) {
            prefs.edit().remove(trashedName).apply();
        }
        return ok;
    }

    private boolean deleteRecursive(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    if (!deleteRecursive(child)) {
                        return false;
                    }
                }
            }
        }
        return file.delete();
    }

    public List<FileItem> listTrashItems() {
        List<FileItem> result = new ArrayList<>();
        File[] children = trashDir.listFiles();
        if (children == null) {
            return result;
        }
        for (File f : children) {
            String trashedName = f.getName();
            String originalPath = prefs.getString(trashedName, null);
            String displayName = originalPath != null ? new File(originalPath).getName() : trashedName;
            long trashedTime = extractTimestamp(trashedName);
            if (trashedTime <= 0) {
                trashedTime = f.lastModified();
            }
            result.add(new FileItem(displayName, f.getAbsolutePath(), f.isDirectory(), f.isDirectory() ? 0L : f.length(), trashedTime));
        }
        return result;
    }

    public void emptyTrash() {
        File[] children = trashDir.listFiles();
        if (children != null) {
            for (File f : children) {
                permanentlyDelete(f.getAbsolutePath());
            }
        }
    }

    private static final long THIRTY_DAYS_MILLIS = 30L * 24 * 60 * 60 * 1000;

    public int cleanupExpiredItems() {
        File[] children = trashDir.listFiles();
        if (children == null) {
            return 0;
        }
        long now = System.currentTimeMillis();
        int removedCount = 0;
        for (File f : children) {
            long trashedTime = extractTimestamp(f.getName());
            if (trashedTime > 0 && (now - trashedTime) > THIRTY_DAYS_MILLIS) {
                if (permanentlyDelete(f.getAbsolutePath())) {
                    removedCount++;
                }
            }
        }
        return removedCount;
    }

    private long extractTimestamp(String trashedName) {
        int underscoreIndex = trashedName.indexOf('_');
        if (underscoreIndex <= 0) {
            return -1;
        }
        try {
            return Long.parseLong(trashedName.substring(0, underscoreIndex));
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}

