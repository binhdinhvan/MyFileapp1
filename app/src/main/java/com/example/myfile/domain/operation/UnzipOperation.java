package com.example.myfile.domain.operation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class UnzipOperation implements FileOperation {
    private final String zipFilePath;
    private final String destDirectory;

    public UnzipOperation(String zipFilePath, String destDirectory) {
        this.zipFilePath = zipFilePath;
        this.destDirectory = destDirectory;
    }

    @Override
    public boolean execute() {
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        
        try (ZipFile zipFile = new ZipFile(zipFilePath)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                File entryDestination = new File(destDirectory, entry.getName());
                
                // Prevent Zip Slip vulnerability
                String destDirPath = destDir.getCanonicalPath();
                String destFilePath = entryDestination.getCanonicalPath();
                if (!destFilePath.startsWith(destDirPath + File.separator)) {
                    continue; // Skip invalid entries
                }
                
                if (entry.isDirectory()) {
                    entryDestination.mkdirs();
                } else {
                    entryDestination.getParentFile().mkdirs();
                    try (InputStream in = zipFile.getInputStream(entry);
                         FileOutputStream out = new FileOutputStream(entryDestination)) {
                         
                        byte[] buffer = new byte[4096];
                        int len;
                        while ((len = in.read(buffer)) >= 0) {
                            out.write(buffer, 0, len);
                        }
                    }
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
