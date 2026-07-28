package com.example.myfile.utils;

import android.content.Context;
import android.net.Uri;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/** [C] Tien ich dung chung cho feature/archive va feature/viewer. */
public final class FileUtils {

    private static final int BUFFER_SIZE = 8192;

    private FileUtils() { }

    public static long copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        long total = 0;
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
            total += read;
        }
        out.flush();
        return total;
    }

    public static int countFilesRecursive(File file) {
        if (file.isFile()) return 1;
        File[] children = file.listFiles();
        if (children == null) return 0;
        int count = 0;
        for (File c : children) count += countFilesRecursive(c);
        return count;
    }

    /** Sinh ten khong trung trong thu muc dich, vd "abc.zip" -> "abc (1).zip". */
    public static String uniqueName(File parentDir, String desiredName) {
        File target = new File(parentDir, desiredName);
        if (!target.exists()) return desiredName;

        String base = desiredName;
        String ext = "";
        int dot = desiredName.lastIndexOf('.');
        if (dot > 0) {
            base = desiredName.substring(0, dot);
            ext = desiredName.substring(dot);
        }
        int index = 1;
        String candidate;
        do {
            candidate = base + " (" + index + ")" + ext;
            index++;
        } while (new File(parentDir, candidate).exists() && index < 1000);
        return candidate;
    }

    /** content:// Uri qua FileProvider - dung khi mo/chia se file bang app khac. */
    public static Uri contentUriFor(Context context, File file) {
        String authority = context.getPackageName() + ".fileprovider";
        return FileProvider.getUriForFile(context, authority, file);
    }
}
