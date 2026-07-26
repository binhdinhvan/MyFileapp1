package com.example.myfile.ui.main;


public enum SortMode {
    NAME_ASC("Name (A-Z)"),
    NAME_DESC("Name (Z-A)"),
    SIZE_DESC("Size (Largest first)"),
    SIZE_ASC("Size (Smallest first)"),
    DATE_DESC("Modified (Newest first)"),
    DATE_ASC("Modified (Oldest first)");

    public final String label;

    SortMode(String label) {
        this.label = label;
    }
}
