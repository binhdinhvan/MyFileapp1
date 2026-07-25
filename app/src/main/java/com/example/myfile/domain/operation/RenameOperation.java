package com.example.myfile.domain.operation;

import com.example.myfile.data.repository.FileRepository;

public class RenameOperation implements FileOperation {
    private final FileRepository repository;
    private final String path;
    private final String newName;

    public RenameOperation(FileRepository repository, String path, String newName) {
        this.repository = repository;
        this.path = path;
        this.newName = newName;
    }

    @Override
    public boolean execute() {
        return repository.rename(path, newName);
    }
}
