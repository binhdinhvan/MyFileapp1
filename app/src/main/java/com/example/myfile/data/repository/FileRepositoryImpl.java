package com.example.myfile.data.repository;

import com.example.myfile.data.model.FileItem;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileRepositoryImpl implements FileRepository {

    @Override
    public List<FileItem> list(String path) {
        List<FileItem> result = new ArrayList<>();
        File folder = new File(path);
        File[] children = folder.listFiles();
        if (children == null) {
            return result;
        }
        Arrays.sort(children, (a, b) -> {
            if (a.isDirectory() != b.isDirectory()) {
                return a.isDirectory() ? -1 : 1;
            }
            return a.getName().compareToIgnoreCase(b.getName());
        });
        for (File f : children) {
            result.add(FileItem.fromFile(f));
        }
        return result;
    }

    @Override
    public boolean rename(String path, String newName) {
        File target = new File(path);
        if (!target.exists()) {
            return false;
        }
        File newFile = new File(target.getParentFile(), newName);
        if (newFile.exists()) {
            return false;
        }
        return target.renameTo(newFile);
    }

    @Override
    public boolean delete(String path) {
        File target = new File(path);
        if (!target.exists()) {
            return false;
        }
        return deleteRecursive(target);
    }

    private boolean deleteRecursive(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    if (!deleteRecursive(child)) {
                        return false;
                    }
                }
            }
        }
        return file.delete();
    }

    @Override
    public boolean move(String sourcePath, String destFolderPath) {
        File source = new File(sourcePath);
        File destFolder = new File(destFolderPath);
        if (!source.exists() || !destFolder.isDirectory()) {
            return false;
        }
        if (source.isDirectory() && isSubPath(source, destFolder)) {
            return false;
        }
        File dest = new File(destFolder, source.getName());
        if (dest.exists()) {
            return false;
        }
        if (source.renameTo(dest)) {
            return true;
        }
        try {
            copyRecursive(source, dest);
            return deleteRecursive(source);
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public boolean copy(String sourcePath, String destFolderPath) {
        File source = new File(sourcePath);
        File destFolder = new File(destFolderPath);
        if (!source.exists() || !destFolder.isDirectory()) {
            return false;
        }
        if (source.isDirectory() && isSubPath(source, destFolder)) {
            return false;
        }
        File dest = new File(destFolder, source.getName());
        if (dest.exists()) {
            return false;
        }
        try {
            copyRecursive(source, dest);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public boolean createFolder(String parentPath, String folderName) {
        File parent = new File(parentPath);
        if (!parent.isDirectory()) {
            return false;
        }
        File newFolder = new File(parent, folderName);
        if (newFolder.exists()) {
            return false;
        }
        return newFolder.mkdir();
    }

    @Override
    public boolean createFile(String parentPath, String fileName) {
        File parent = new File(parentPath);
        if (!parent.isDirectory()) {
            return false;
        }
        File newFile = new File(parent, fileName);
        if (newFile.exists()) {
            return false;
        }
        try {
            return newFile.createNewFile();
        } catch (IOException e) {
            return false;
        }
    }

    private boolean isSubPath(File parent, File possibleChild) {
        File current = possibleChild;
        while (current != null) {
            if (current.equals(parent)) {
                return true;
            }
            current = current.getParentFile();
        }
        return false;
    }

    private void copyRecursive(File source, File dest) throws IOException {
        if (source.isDirectory()) {
            if (!dest.mkdirs()) {
                throw new IOException("Cannot create destination folder");
            }
            File[] children = source.listFiles();
            if (children != null) {
                for (File child : children) {
                    copyRecursive(child, new File(dest, child.getName()));
                }
            }
        } else {
            FileInputStream in = new FileInputStream(source);
            FileOutputStream out = new FileOutputStream(dest);
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            in.close();
            out.close();
        }
    }
}
