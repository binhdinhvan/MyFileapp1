package com.example.myfile.feature.archive;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;

public class ArchiveDialogs {
    public static ProgressDialog showProgressDialog(Context context, String message) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.show();
        return progressDialog;
    }

    public static void showExtractConfirmDialog(Context context, String destPath, Runnable onConfirm) {
        new AlertDialog.Builder(context)
            .setTitle("Xác nhận giải nén")
            .setMessage("Bạn muốn giải nén file vào thư mục?\n\nĐích: " + destPath)
            .setPositiveButton("Giải nén", (dialog, which) -> onConfirm.run())
            .setNegativeButton("Hủy", null)
            .show();
    }
}
