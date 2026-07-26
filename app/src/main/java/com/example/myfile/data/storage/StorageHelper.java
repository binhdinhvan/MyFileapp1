package com.example.myfile.data.storage;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public final class StorageHelper {

    private StorageHelper() {
    }

    public static List<StorageVolumeItem> getStorages(Context context) {
        List<StorageVolumeItem> list = new ArrayList<>();

        String primary = Environment.getExternalStorageDirectory().getAbsolutePath();
        list.add(new StorageVolumeItem("Internal Storage", primary));

        File[] externals = context.getExternalFilesDirs(null);
        if (externals != null) {

            for (int i = 1; i < externals.length; i++) {
                File ext = externals[i];
                if (ext == null) {
                    continue;
                }
                String p = ext.getAbsolutePath();
                int idx = p.indexOf("/Android/data");
                if (idx > 0) {
                    String root = p.substring(0, idx);
                    File rootFile = new File(root);
                    if (rootFile.exists() && rootFile.canRead()) {
                        list.add(new StorageVolumeItem("SD Card / USB", root));
                    }
                }
            }
        }
        return list;
    }

    
    public static List<QuickFolderItem> getQuickFolders() {
        List<QuickFolderItem> list = new ArrayList<>();
        String base = Environment.getExternalStorageDirectory().getAbsolutePath();

        addIfExists(list, "\uD83D\uDCE5", "Downloads",      base + "/Download");
        addIfExists(list, "\uD83D\uDCF7", "Camera (DCIM)", base + "/DCIM");
        addIfExists(list, "\uD83D\uDDBC", "Pictures",       Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath());
        addIfExists(list, "\uD83C\uDFAC", "Videos",         Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getAbsolutePath());
        addIfExists(list, "\uD83C\uDFB5", "Music",          Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath());
        addIfExists(list, "\uD83D\uDCC4", "Documents",      Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath());
        addIfExists(list, "\uD83D\uDCF2", "Android",        base + "/Android");
        addIfExists(list, "\uD83D\uDD14", "Notifications",  base + "/Notifications");
        addIfExists(list, "\uD83C\uDF99", "Podcasts",       base + "/Podcasts");
        addIfExists(list, "\uD83D\uDCF3", "Ringtones",      base + "/Ringtones");
        addIfExists(list, "\u23F0",       "Alarms",          base + "/Alarms");

        return list;
    }

    private static void addIfExists(List<QuickFolderItem> list, String emoji, String label, String path) {
        File f = new File(path);
        if (f.exists()) {
            list.add(new QuickFolderItem(emoji, label, path));
        }
    }
}
