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

    public PrivateVaultManager(Context context) {
        vaultDir = new File(Environment.getExternalStorageDirectory(), ".PrivateVault");
        if (!vaultDir.exists()) {
            vaultDir.mkdirs();
        }
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
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
        if (!source.renameTo(dest)) {
            return null;
        }
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
        boolean ok = vaultedFile.renameTo(originalFile);
        if (ok) {
            prefs.edit().remove(vaultedName).apply();
        }
        return ok;
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
