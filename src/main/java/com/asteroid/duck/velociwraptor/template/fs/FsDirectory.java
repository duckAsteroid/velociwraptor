package com.asteroid.duck.velociwraptor.template.fs;

import com.asteroid.duck.velociwraptor.template.Directory;
import com.asteroid.duck.velociwraptor.template.File;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Provides a {@link Directory} model of a path
 */
class FsDirectory implements Directory {
    private final Path path;

    FsDirectory(Path path) {
        this.path = path;
    }

    @Override
    public String rawName() {
        // need to trim '/' from ZIP paths...
        return path.getFileName().toString().replace("/", "");
    }

    @Override
    public Stream<File> childFiles() throws IOException {
        return Files.list(path)
                .filter(Files::isRegularFile)
                .map(child -> new FsFile(child));
    }

    @Override
    public Stream<Directory> childDirs() throws IOException {
        return Files.list(path)
                .filter(Files::isDirectory)
                .map(child -> new FsDirectory(child) );
    }
}
