private void openFile(FileItem item) {
    String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(item.getPath());
    String mimeType = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
    if (mimeType == null) mimeType = "*/*";

    if (mimeType.startsWith("image/") || mimeType.startsWith("video/") || mimeType.startsWith("text/")) {
        com.example.myfile.feature.viewer.ViewerFactory.open(this, item, searchResults);
    } else {
        android.net.Uri uri = androidx.core.content.FileProvider.getUriForFile(
                this, getApplicationContext().getPackageName() + ".fileprovider",
                new java.io.File(item.getPath()));
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, mimeType);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivity(Intent.createChooser(intent, "Open with"));
        } catch (Exception e) {
            Toast.makeText(this, "Cannot open file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
