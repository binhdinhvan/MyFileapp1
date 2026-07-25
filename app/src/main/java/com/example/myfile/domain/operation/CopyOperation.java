package com.example.myfile.domain.operation;

import com.example.myfile.data.repository.FileRepository;

public class CopyOperation implements FileOperation {
    private final FileRepository repository;
    private final String sourcePath;
    private final String destFolderPath;

    public CopyOperation(FileRepository repository, String sourcePath, String destFolderPath) {
        this.repository = repository;
        this.sourcePath = sourcePath;
        this.destFolderPath = destFolderPath;
    }

    @Override
    public boolean execute() {
        return repository.copy(sourcePath, destFolderPath);
    }
}
