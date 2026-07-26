package com.example.myfile.data.model;

import java.io.File;

public class FileItem {
    private final String name;
    private final String path;
    private final boolean isDirectory;
    private final long size;
    private final long lastModified;
    private final boolean isHeader;
    private final String headerTitle;

    public FileItem(String name, String path, boolean isDirectory, long size, long lastModified) {
        this.name = name;
        this.path = path;
        this.isDirectory = isDirectory;
        this.size = size;
        this.lastModified = lastModified;
        this.isHeader = false;
        this.headerTitle = null;
    }
    
    public FileItem(String headerTitle) {
        this.name = "";
        this.path = "";
        this.isDirectory = false;
        this.size = 0;
        this.lastModified = 0;
        this.isHeader = true;
        this.headerTitle = headerTitle;
    }

    public static FileItem fromFile(File file) {
        String name = file.getName();
        String path = file.getAbsolutePath();
        boolean isDirectory = file.isDirectory();
        long size = isDirectory ? 0L : file.length();
        long lastModified = file.lastModified();
        return new FileItem(name, path, isDirectory, size, lastModified);
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public long getSize() {
        return size;
    }

    public long getLastModified() {
        return lastModified;
    }
    
    public boolean isHeader() {
        return isHeader;
    }
    
    public String getHeaderTitle() {
        return headerTitle;
    }
}
