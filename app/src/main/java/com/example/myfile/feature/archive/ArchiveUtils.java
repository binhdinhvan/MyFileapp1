package com.example.myfile.feature.archive;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/** [C] Tien ich rieng cho zip/unzip. */
public final class ArchiveUtils {

    private ArchiveUtils() { }

    public static int countEntries(File zipFile) throws IOException {
        try (ZipFile zf = new ZipFile(zipFile)) {
            return zf.size();
        }
    }

    public static long totalUncompressedSize(File zipFile) throws IOException {
        long total = 0;
        try (ZipFile zf = new ZipFile(zipFile)) {
            Enumeration<? extends ZipEntry> entries = zf.entries();
            while (entries.hasMoreElements()) {
                long size = entries.nextElement().getSize();
                if (size > 0) total += size;
            }
        }
        return total;
    }

    /** "abc" hoac "abc.zip" -> "abc.zip" khong trung ten trong destDir. */
    public static String uniqueZipName(File destDir, String desiredBaseName) {
        String name = desiredBaseName.toLowerCase().endsWith(".zip")
                ? desiredBaseName : desiredBaseName + ".zip";
        File target = new File(destDir, name);
        if (!target.exists()) return name;

        String base = name.substring(0, name.length() - 4);
        int index = 1;
        String candidate;
        do {
            candidate = base + " (" + index + ").zip";
            index++;
        } while (new File(destDir, candidate).exists() && index < 1000);
        return candidate;
    }
}
