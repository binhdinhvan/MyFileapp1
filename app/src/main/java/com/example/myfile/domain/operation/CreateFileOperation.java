package com.example.myfile.domain.operation;

import com.example.myfile.data.repository.FileRepository;

public class CreateFileOperation implements FileOperation {
    private final FileRepository repository;
    private final String parentPath;
    private final String fileName;

    public CreateFileOperation(FileRepository repository, String parentPath, String fileName) {
        this.repository = repository;
        this.parentPath = parentPath;
        this.fileName = fileName;
    }

    @Override
    public boolean execute() {
        return repository.createFile(parentPath, fileName);
    }
}
