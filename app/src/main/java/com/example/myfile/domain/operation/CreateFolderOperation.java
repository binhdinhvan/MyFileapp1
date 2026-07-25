package com.example.myfile.domain.operation;

import com.example.myfile.data.repository.FileRepository;

public class CreateFolderOperation implements FileOperation {
    private final FileRepository repository;
    private final String parentPath;
    private final String folderName;

    public CreateFolderOperation(FileRepository repository, String parentPath, String folderName) {
        this.repository = repository;
        this.parentPath = parentPath;
        this.folderName = folderName;
    }

    @Override
    public boolean execute() {
        return repository.createFolder(parentPath, folderName);
    }
}
