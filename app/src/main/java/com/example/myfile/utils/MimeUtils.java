package com.example.myfile.utils;

import android.webkit.MimeTypeMap;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * [C] Phan loai file theo phan mo rong (khong co FileType enum san co trong
 * data.model nhu ban dau, nen dinh nghia rieng o day) + doan MIME type that
 * de dung cho Intent.ACTION_VIEW / FileProvider / chia se.
 */
public final class MimeUtils {

    public enum Category {
        FOLDER, IMAGE, VIDEO, AUDIO, ARCHIVE, DOCUMENT, TEXT, APK, OTHER
    }

    private static final Set<String> IMAGE = set("jpg", "jpeg", "png", "gif", "bmp", "webp", "heic", "heif");
    private static final Set<String> VIDEO = set("mp4", "mkv", "avi", "mov", "3gp", "webm", "flv", "m4v", "ts");
    private static final Set<String> AUDIO = set("mp3", "wav", "flac", "aac", "ogg", "m4a", "wma", "opus");
    private static final Set<String> ARCHIVE = set("zip", "rar", "7z", "tar", "gz");
    private static final Set<String> DOCUMENT = set("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx");
    private static final Set<String> TEXT = set("txt", "log", "json", "xml", "md", "csv", "ini", "java", "kt");

    private MimeUtils() { }

    public static String extensionOf(String fileName) {
        if (fileName == null) return "";
        int dot = fileName.lastIndexOf('.');
        if (dot <= 0 || dot == fileName.length() - 1) return "";
        return fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    public static Category categoryOf(String fileName, boolean isDirectory) {
        if (isDirectory) return Category.FOLDER;
        String ext = extensionOf(fileName);
        if (ext.isEmpty()) return Category.OTHER;
        if (IMAGE.contains(ext)) return Category.IMAGE;
        if (VIDEO.contains(ext)) return Category.VIDEO;
        if (AUDIO.contains(ext)) return Category.AUDIO;
        if (ARCHIVE.contains(ext)) return Category.ARCHIVE;
        if (DOCUMENT.contains(ext)) return Category.DOCUMENT;
        if (TEXT.contains(ext)) return Category.TEXT;
        if ("apk".equals(ext)) return Category.APK;
        return Category.OTHER;
    }

    public static Category categoryOf(String fileName) {
        return categoryOf(fileName, false);
    }

    public static boolean isImage(String fileName) { return categoryOf(fileName) == Category.IMAGE; }
    public static boolean isVideo(String fileName) { return categoryOf(fileName) == Category.VIDEO; }
    public static boolean isArchive(String fileName) { return categoryOf(fileName) == Category.ARCHIVE; }

    /** MIME type thuc su, dung cho Intent.ACTION_VIEW / chia se qua FileProvider. */
    public static String mimeTypeOf(String fileName) {
        String ext = extensionOf(fileName);
        if (!ext.isEmpty()) {
            String system = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
            if (system != null) return system;
        }
        switch (categoryOf(fileName)) {
            case IMAGE: return "image/*";
            case VIDEO: return "video/*";
            case AUDIO: return "audio/*";
            case TEXT: return "text/plain";
            case ARCHIVE: return "application/zip";
            case APK: return "application/vnd.android.package-archive";
            default: return "*/*";
        }
    }

    private static Set<String> set(String... values) {
        return new HashSet<>(Arrays.asList(values));
    }
}
