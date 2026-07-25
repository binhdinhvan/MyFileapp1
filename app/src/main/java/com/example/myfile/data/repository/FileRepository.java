package com.example.myfile.data.repository;

import com.example.myfile.data.model.FileItem;
import java.util.List;

public interface FileRepository {
    List<FileItem> list(String path);
    boolean rename(String path, String newName);
    boolean delete(String path);
    boolean move(String sourcePath, String destFolderPath);
    boolean copy(String sourcePath, String destFolderPath);
    boolean createFolder(String parentPath, String folderName);
    boolean createFile(String parentPath, String fileName);
}
