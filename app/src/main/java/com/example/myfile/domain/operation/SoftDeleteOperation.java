package com.example.myfile.domain.operation;

import com.example.myfile.data.trash.TrashManager;

public class SoftDeleteOperation implements FileOperation {
    private final TrashManager trashManager;
    private final String path;
    private String trashedPath;

    public SoftDeleteOperation(TrashManager trashManager, String path) {
        this.trashManager = trashManager;
        this.path = path;
    }

    @Override
    public boolean execute() {
        trashedPath = trashManager.moveToTrash(path);
        return trashedPath != null;
    }

    public String getTrashedPath() {
        return trashedPath;
    }
}
