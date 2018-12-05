package com.asteroid.duck.velociwraptor.template;

import com.asteroid.duck.velociwraptor.model.Directory;
import com.asteroid.duck.velociwraptor.model.File;
import com.asteroid.duck.velociwraptor.model.Template;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.stream.Stream;

public class FileSystemTemplate implements Template {

    static class FsFile implements File {
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

    static class FsDirectory implements Directory {
        private final Path path;

        FsDirectory(Path path) {
            this.path = path;
        }

        @Override
        public String rawName() {
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


    private final FsDirectory template;
    private final JsonObject projectSettings;

    public FileSystemTemplate(Path root) throws IOException {
        Path templatePath = root.resolve("template");
        this.template = new FsDirectory(templatePath);
        Path projectFile = root.resolve("project.json");
        JsonReader reader = Json.createReader(Files.newBufferedReader(projectFile));
        projectSettings = reader.readObject();
    }

    @Override
    public Directory rootDirectory() {
        return template;
    }

    @Override
    public JsonObject projectSettings() {
        return projectSettings;
    }
}
