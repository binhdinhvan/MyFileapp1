package com.example.myfile.ui.main;

/**
 * Cac kieu sap xep danh sach file. Thu muc luon dung truoc file (xu ly trong FileListHelper).
 */
public enum SortMode {
    NAME_ASC("Ten (A-Z)"),
    NAME_DESC("Ten (Z-A)"),
    SIZE_DESC("Kich thuoc (lon-nho)"),
    SIZE_ASC("Kich thuoc (nho-lon)"),
    DATE_DESC("Ngay sua (moi-cu)"),
    DATE_ASC("Ngay sua (cu-moi)");

    public final String label;

    SortMode(String label) {
        this.label = label;
    }
}
