package com.example.myfile.utils;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.util.LruCache;
import android.widget.ImageView;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThumbnailLoader {

    private static ThumbnailLoader instance;
    private final LruCache<String, Bitmap> memoryCache;
    private final ExecutorService executorService;
    private final Handler mainHandler;

    private ThumbnailLoader() {
        // Use 1/8th of the available memory for this memory cache.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;

        memoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than number of items.
                return bitmap.getByteCount() / 1024;
            }
        };

        executorService = Executors.newFixedThreadPool(4);
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public static synchronized ThumbnailLoader getInstance() {
        if (instance == null) {
            instance = new ThumbnailLoader();
        }
        return instance;
    }

    public void loadPdfThumbnail(String filePath, ImageView imageView) {
        Bitmap bitmap = memoryCache.get(filePath);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
            imageView.setVisibility(android.view.View.VISIBLE);
            return;
        }

        // Tag the ImageView to prevent wrong image being loaded when recycled
        imageView.setTag(filePath);
        imageView.setImageBitmap(null); // Clear previous

        executorService.execute(() -> {
            Bitmap generatedBitmap = generatePdfThumbnail(filePath);
            if (generatedBitmap != null) {
                memoryCache.put(filePath, generatedBitmap);
                mainHandler.post(() -> {
                    if (filePath.equals(imageView.getTag())) {
                        imageView.setImageBitmap(generatedBitmap);
                        imageView.setVisibility(android.view.View.VISIBLE);
                    }
                });
            }
        });
    }

    private Bitmap generatePdfThumbnail(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists() || !file.canRead()) return null;

            ParcelFileDescriptor fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                PdfRenderer renderer = new PdfRenderer(fd);
                if (renderer.getPageCount() > 0) {
                    PdfRenderer.Page page = renderer.openPage(0);
                    
                    // Render at a decent thumbnail resolution, e.g., width 400
                    int width = 400;
                    int height = (int) (width * ((float) page.getHeight() / page.getWidth()));
                    
                    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    bitmap.eraseColor(Color.WHITE);
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                    
                    page.close();
                    renderer.close();
                    fd.close();
                    
                    return bitmap;
                }
                renderer.close();
            }
            fd.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
