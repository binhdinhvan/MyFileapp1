package com.example.myfile.data.vault;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import com.example.myfile.data.model.FileItem;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PrivateVaultManager {

    private static final String PREFS_NAME = "vault_metadata";
    private static final String PREF_PASSWORD = "vault_password";
    private final File vaultDir;
    private final SharedPreferences prefs;
    private final Context context;

    public PrivateVaultManager(Context context) {
        this.context = context.getApplicationContext();
        // Muc 2: dat vault trong bo nho rieng cua app (/sdcard/Android/data/<pkg>/files/vault).
        // Tren Android 11+, app khac va ca file manager (ke ca co MANAGE_EXTERNAL_STORAGE)
        // deu KHONG truy cap duoc thu muc nay, MediaStore cung khong index -> khong bi soi.
        File base = context.getExternalFilesDir(null);
        if (base == null) {
            // Storage ngoai khong san sang -> fallback ve bo nho trong (van rieng tu cho app)
            base = context.getFilesDir();
        }
        vaultDir = new File(base, "vault");
        if (!vaultDir.exists()) {
            vaultDir.mkdirs();
        }
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /** Di chuyen file: uu tien renameTo (nhanh); neu khac volume thi copy roi xoa nguon. */
    private boolean moveFile(File src, File dest) {
        if (src.renameTo(dest)) {
            return true;
        }
        try (java.io.InputStream in = new java.io.FileInputStream(src);
             java.io.OutputStream out = new java.io.FileOutputStream(dest)) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
        } catch (java.io.IOException e) {
            dest.delete();
            return false;
        }
        return src.delete();
    }

    public boolean isPasswordSet() {
        return prefs.contains(PREF_PASSWORD);
    }

    public void setPassword(String password) {
        prefs.edit().putString(PREF_PASSWORD, password).apply();
    }

    public boolean checkPassword(String password) {
        String saved = prefs.getString(PREF_PASSWORD, "");
        return saved.equals(password);
    }

    public String moveToVault(String originalPath) {
        File source = new File(originalPath);
        if (!source.exists()) {
            return null;
        }
        String vaultedName = System.currentTimeMillis() + "_" + source.getName();
        File dest = new File(vaultDir, vaultedName);
        if (!moveFile(source, dest)) {
            return null;
        }
        removeFromMediaStore(originalPath); // xoa vet o Gallery sau khi dua vao ket
        prefs.edit().putString(vaultedName, originalPath).apply();
        return dest.getAbsolutePath();
    }

    public boolean restore(String vaultedPath) {
        File vaultedFile = new File(vaultedPath);
        if (!vaultedFile.exists()) {
            return false;
        }
        String vaultedName = vaultedFile.getName();
        String originalPath = prefs.getString(vaultedName, null);
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
        boolean ok = moveFile(vaultedFile, originalFile);
        if (ok) {
            // Quet lai de file khoi phuc hien lai trong Gallery
            android.media.MediaScannerConnection.scanFile(
                    context, new String[]{ originalPath }, null, null);
            prefs.edit().remove(vaultedName).apply();
        }
        return ok;
    }

    /** Xoa vet cua file o path cu trong MediaStore de Gallery khong con hien. */
    private void removeFromMediaStore(String path) {
        try {
            context.getContentResolver().delete(
                    android.provider.MediaStore.Files.getContentUri("external"),
                    android.provider.MediaStore.Files.FileColumns.DATA + "=?",
                    new String[]{ path });
        } catch (Exception ignored) {}
    }

    public List<FileItem> listVaultItems() {
        List<FileItem> result = new ArrayList<>();
        File[] children = vaultDir.listFiles();
        if (children == null) {
            return result;
        }
        for (File f : children) {
            String vaultedName = f.getName();
            String originalPath = prefs.getString(vaultedName, null);
            String displayName = originalPath != null ? new File(originalPath).getName() : vaultedName;
            long vaultTime = extractTimestamp(vaultedName);
            if (vaultTime <= 0) {
                vaultTime = f.lastModified();
            }
            result.add(new FileItem(displayName, f.getAbsolutePath(), f.isDirectory(), f.isDirectory() ? 0L : f.length(), vaultTime));
        }
        return result;
    }

    private long extractTimestamp(String vaultedName) {
        int underscoreIndex = vaultedName.indexOf('_');
        if (underscoreIndex <= 0) return -1;
        try {
            return Long.parseLong(vaultedName.substring(0, underscoreIndex));
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}

