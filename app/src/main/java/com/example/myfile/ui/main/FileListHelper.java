package com.example.myfile.ui.main;

import com.example.myfile.data.model.FileItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Ham thuan tuy (khong sua doi list goc) cho sap xep & tim kiem.
 * Tach rieng de MainActivity/FolderPicker deu dung lai duoc.
 */
public final class FileListHelper {

    private FileListHelper() {
    }

    /** Sap xep: thu muc truoc, sau do theo mode. Tra ve list moi (khong sua list goc). */
    public static List<FileItem> sort(List<FileItem> items, SortMode mode) {
        List<FileItem> copy = new ArrayList<>(items);
        final SortMode m = mode != null ? mode : SortMode.NAME_ASC;
        Collections.sort(copy, new Comparator<FileItem>() {
            @Override
            public int compare(FileItem a, FileItem b) {
                if (a.isDirectory() != b.isDirectory()) {
                    return a.isDirectory() ? -1 : 1;
                }
                switch (m) {
                    case NAME_DESC:
                        return b.getName().compareToIgnoreCase(a.getName());
                    case SIZE_ASC:
                        return Long.compare(a.getSize(), b.getSize());
                    case SIZE_DESC:
                        return Long.compare(b.getSize(), a.getSize());
                    case DATE_ASC:
                        return Long.compare(a.getLastModified(), b.getLastModified());
                    case DATE_DESC:
                        return Long.compare(b.getLastModified(), a.getLastModified());
                    case NAME_ASC:
                    default:
                        return a.getName().compareToIgnoreCase(b.getName());
                }
            }
        });
        return copy;
    }

    /** Loc theo ten (khong phan biet hoa thuong). Query rong -> tra ve nguyen list. */
    public static List<FileItem> filter(List<FileItem> items, String query) {
        if (query == null || query.trim().isEmpty()) {
            return items;
        }
        String q = query.trim().toLowerCase(Locale.getDefault());
        List<FileItem> out = new ArrayList<>();
        for (FileItem item : items) {
            if (item.getName().toLowerCase(Locale.getDefault()).contains(q)) {
                out.add(item);
            }
        }
        return out;
    }
}
