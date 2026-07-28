package com.example.myfile.domain.operation;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.example.myfile.feature.archive.ArchiveDialogs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class UnzipOperation {
    public void extractFile(Context context, String zipFilePath, String destFolderPath) {
        // Hiển thị dialog xác nhận đích đến trước khi giải nén (Của Part C)
        ArchiveDialogs.showExtractConfirmDialog(context, destFolderPath, () -> {
            ProgressDialog progressDialog = ArchiveDialogs.showProgressDialog(context, "Đang giải nén...");
            
            new Thread(() -> {
                File targetDir = null;
                try {
                    String uniqueDestPath = generateUniqueFolderName(destFolderPath, new File(zipFilePath).getName().replace(".zip", ""));
                    targetDir = new File(destFolderPath, uniqueDestPath);
                    if (!targetDir.exists()) targetDir.mkdirs();

                    try (ZipFile zipFile = new ZipFile(zipFilePath)) {
                        Enumeration<? extends ZipEntry> entries = zipFile.entries();
                        while (entries.hasMoreElements()) {
                            ZipEntry entry = entries.nextElement();
                            
                            // Kiểm tra Zip Slip chặt chẽ
                            if (entry.getName().contains("../") || entry.getName().contains("..\\")) {
                                throw new SecurityException("Zip Slip detected");
                            }
                            File entryDest = new File(targetDir, entry.getName());
                            if (!entryDest.getCanonicalPath().startsWith(targetDir.getCanonicalPath() + File.separator)) {
                                throw new SecurityException("Zip Slip detected");
                            }

                            if (entry.isDirectory()) {
                                entryDest.mkdirs();
                            } else {
                                entryDest.getParentFile().mkdirs();
                                try (InputStream is = zipFile.getInputStream(entry);
                                     FileOutputStream fos = new FileOutputStream(entryDest)) {
                                    byte[] buffer = new byte[4096];
                                    int length;
                                    while ((length = is.read(buffer)) > 0) {
                                        fos.write(buffer, 0, length);
                                    }
                                }
                            }
                        }
                    }

                    final String finalDirName = targetDir.getName();
                    new Handler(Looper.getMainLooper()).post(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(context, "Giải nén thành công vào: " + finalDirName, Toast.LENGTH_SHORT).show();
                    });

                } catch (SecurityException e) {
                    final File cleanupDir = targetDir;
                    new Handler(Looper.getMainLooper()).post(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(context, "Lỗi bảo mật (Zip Slip). Đã hủy toàn bộ thao tác giải nén!", Toast.LENGTH_LONG).show();
                        if (cleanupDir != null && cleanupDir.exists()) {
                            deleteRecursive(cleanupDir); // Xóa thư mục giải nén dở
                        }
                    });
                } catch (Exception e) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(context, "Lỗi giải nén: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            }).start();
        });
    }

    private void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            File[] children = fileOrDirectory.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursive(child);
                }
            }
        }
        fileOrDirectory.delete();
    }

    private String generateUniqueFolderName(String destFolder, String baseName) {
        File file = new File(destFolder, baseName);
        if (!file.exists()) return baseName;
        int counter = 1;
        while (file.exists()) {
            String newName = baseName + " (" + counter + ")";
            file = new File(destFolder, newName);
            counter++;
        }
        return file.getName();
    }
}
