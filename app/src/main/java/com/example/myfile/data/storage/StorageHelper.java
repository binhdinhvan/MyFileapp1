package com.example.myfile.data.storage;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Liet ke cac noi luu tru cho drawer.
 * - Bo nho trong: Environment.getExternalStorageDirectory()
 * - The SD / USB: suy ra tu getExternalFilesDirs() bang cach cat bo "/Android/data/..."
 *   de lay duong dan goc cua volume (vd /storage/XXXX-XXXX).
 */
public final class StorageHelper {

    private StorageHelper() {
    }

    public static List<StorageVolumeItem> getStorages(Context context) {
        List<StorageVolumeItem> list = new ArrayList<>();

        String primary = Environment.getExternalStorageDirectory().getAbsolutePath();
        list.add(new StorageVolumeItem("Bo nho trong", primary));

        File[] externals = context.getExternalFilesDirs(null);
        if (externals != null) {
            // index 0 = bo nho trong (da them o tren) -> bat dau tu 1
            for (int i = 1; i < externals.length; i++) {
                File ext = externals[i];
                if (ext == null) {
                    continue;
                }
                String p = ext.getAbsolutePath();
                int idx = p.indexOf("/Android/data");
                if (idx > 0) {
                    String root = p.substring(0, idx);
                    File rootFile = new File(root);
                    if (rootFile.exists() && rootFile.canRead()) {
                        list.add(new StorageVolumeItem("The SD / USB", root));
                    }
                }
            }
        }
        return list;
    }
}
