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

    public TrashManager(Context context) {
        trashDir = new File(Environment.getExternalStorageDirectory(), ".AppTrash");
        if (!trashDir.exists()) {
            trashDir.mkdirs();
        }
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
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
            result.add(new FileItem(displayName, f.getAbsolutePath(), f.isDirectory(), f.isDirectory() ? 0L : f.length(), f.lastModified()));
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
