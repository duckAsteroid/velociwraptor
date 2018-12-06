package com.asteroid.duck.velociwraptor.template.fs;

import com.asteroid.duck.velociwraptor.template.File;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Provides a {@link File} model for a path
 */
class FsFile implements File {
    private final Path path;

    FsFile(Path path) {
        this.path = path;
    }

    @Override
    public String rawName() {
        return path.getFileName().toString();
    }

    @Override
    public InputStream rawContent() throws IOException {
        return Files.newInputStream(path);
    }
}
