package com.example.myfile.data.storage;


public class QuickFolderItem {
    private final String label;
    private final String path;
    private final String emoji;

    public QuickFolderItem(String emoji, String label, String path) {
        this.emoji = emoji;
        this.label = label;
        this.path = path;
    }

    public String getEmoji() {
        return emoji;
    }

    public String getLabel() {
        return label;
    }

    public String getPath() {
        return path;
    }
}
