package com.example.myfile.domain.operation;

import com.example.myfile.data.repository.FileRepository;

public class DeleteOperation implements FileOperation {
    private final FileRepository repository;
    private final String path;

    public DeleteOperation(FileRepository repository, String path) {
        this.repository = repository;
        this.path = path;
    }

    @Override
    public boolean execute() {
        return repository.delete(path);
    }
}
