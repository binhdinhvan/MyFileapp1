package com.example.myfile.domain.operation;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.example.myfile.feature.archive.ArchiveDialogs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipOperation {
    public void compressFiles(Context context, List<String> sourcePaths, String destFolderPath) {
        ProgressDialog progressDialog = ArchiveDialogs.showProgressDialog(context, "Đang nén file...");
        new Thread(() -> {
            try {
                String zipFileName = generateUniqueZipName(destFolderPath, "archive.zip");
                File destZip = new File(destFolderPath, zipFileName);
                
                try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(destZip))) {
                    for (String sourcePath : sourcePaths) {
                        File sourceFile = new File(sourcePath);
                        if (sourceFile.isHidden()) continue; // Bỏ qua file ẩn (Code mới nhóm)
                        addFileToZip(sourceFile, sourceFile.getName(), zos);
                    }
                }
                
                new Handler(Looper.getMainLooper()).post(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(context, "Nén thành công: " + zipFileName, Toast.LENGTH_SHORT).show();
                });
            } catch (SecurityException e) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(context, "Phát hiện tệp độc hại (Zip Slip)! Đã hủy nén toàn bộ.", Toast.LENGTH_LONG).show();
                });
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(context, "Lỗi khi nén file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void addFileToZip(File file, String entryName, ZipOutputStream zos) throws Exception {
        // Bảo vệ Zip Slip
        if (entryName.contains("../") || entryName.contains("..\\")) {
            throw new SecurityException("Zip Slip detected");
        }
        
        if (file.isDirectory()) {
            if (!entryName.endsWith("/")) entryName += "/";
            zos.putNextEntry(new ZipEntry(entryName));
            zos.closeEntry();
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    addFileToZip(child, entryName + child.getName(), zos);
                }
            }
        } else {
            try (FileInputStream fis = new FileInputStream(file)) {
                zos.putNextEntry(new ZipEntry(entryName));
                byte[] buffer = new byte[4096];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, length);
                }
                zos.closeEntry();
            }
        }
    }

    private String generateUniqueZipName(String destFolder, String baseName) {
        File file = new File(destFolder, baseName);
        if (!file.exists()) return baseName;
        
        String nameWithoutExt = baseName.substring(0, baseName.lastIndexOf('.'));
        String ext = baseName.substring(baseName.lastIndexOf('.'));
        int counter = 1;
        
        while (file.exists()) {
            String newName = nameWithoutExt + " (" + counter + ")" + ext;
            file = new File(destFolder, newName);
            counter++;
        }
        return file.getName();
    }
}
