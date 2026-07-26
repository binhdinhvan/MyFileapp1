package com.example.myfile.domain.operation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipOperation implements FileOperation {
    private final List<String> sourcePaths;
    private final String destZipPath;

    public ZipOperation(List<String> sourcePaths, String destZipPath) {
        this.sourcePaths = sourcePaths;
        this.destZipPath = destZipPath;
    }

    @Override
    public boolean execute() {
        try (FileOutputStream fos = new FileOutputStream(destZipPath);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            for (String sourcePath : sourcePaths) {
                File srcFile = new File(sourcePath);
                zipFile(srcFile, srcFile.getName(), zos);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void zipFile(File fileToZip, String fileName, ZipOutputStream zos) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                zos.putNextEntry(new ZipEntry(fileName));
                zos.closeEntry();
            } else {
                zos.putNextEntry(new ZipEntry(fileName + "/"));
                zos.closeEntry();
            }
            File[] children = fileToZip.listFiles();
            if (children != null) {
                for (File childFile : children) {
                    zipFile(childFile, fileName + "/" + childFile.getName(), zos);
                }
            }
            return;
        }
        
        try (FileInputStream fis = new FileInputStream(fileToZip)) {
            ZipEntry zipEntry = new ZipEntry(fileName);
            zos.putNextEntry(zipEntry);
            byte[] bytes = new byte[1024 * 4];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zos.write(bytes, 0, length);
            }
        }
    }
}
