package com.example.myfile.feature.viewer.exif;

import androidx.exifinterface.media.ExifInterface;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** [C] Doc cac tag EXIF pho bien tu 1 file anh, tra ve danh sach da gan nhan. */
public final class ExifReader {

    public static class Entry {
        public final String label;
        public final String value;

        public Entry(String label, String value) {
            this.label = label;
            this.value = value;
        }
    }

    private ExifReader() { }

    public static List<Entry> read(File imageFile) {
        List<Entry> result = new ArrayList<>();
        try {
            ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
            for (Map.Entry<String, String> tag : labels().entrySet()) {
                String value = exif.getAttribute(tag.getKey());
                if (value != null && !value.trim().isEmpty()) {
                    result.add(new Entry(tag.getValue(), value));
                }
            }
            double[] latLong = exif.getLatLong();
            if (latLong != null) {
                result.add(new Entry("Latitude", String.valueOf(latLong[0])));
                result.add(new Entry("Longitude", String.valueOf(latLong[1])));
            }
        } catch (IOException e) {
            result.add(new Entry("Error", "Failed to read metadata: " + e.getMessage()));
        }
        return result;
    }

    private static Map<String, String> labels() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put(ExifInterface.TAG_DATETIME, "Date taken");
        map.put(ExifInterface.TAG_MAKE, "Camera make");
        map.put(ExifInterface.TAG_MODEL, "Camera model");
        map.put(ExifInterface.TAG_IMAGE_WIDTH, "Width (px)");
        map.put(ExifInterface.TAG_IMAGE_LENGTH, "Height (px)");
        map.put(ExifInterface.TAG_F_NUMBER, "Aperture (f)");
        map.put(ExifInterface.TAG_EXPOSURE_TIME, "Shutter speed");
        map.put(ExifInterface.TAG_ISO_SPEED_RATINGS, "ISO");
        map.put(ExifInterface.TAG_FOCAL_LENGTH, "Focal length");
        map.put(ExifInterface.TAG_WHITE_BALANCE, "White balance");
        map.put(ExifInterface.TAG_FLASH, "Flash");
        return map;
    }
}
