package com.example.myfile.data.storage;


public class StorageVolumeItem {
    private final String label;
    private final String path;

    public StorageVolumeItem(String label, String path) {
        this.label = label;
        this.path = path;
    }

    public String getLabel() {
        return label;
    }

    public String getPath() {
        return path;
    }
}
