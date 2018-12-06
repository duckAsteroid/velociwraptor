package com.asteroid.duck.velociwraptor.template;

import com.asteroid.duck.velociwraptor.Main;
import com.asteroid.duck.velociwraptor.model.Directory;
import com.asteroid.duck.velociwraptor.model.File;
import com.asteroid.duck.velociwraptor.model.Template;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.*;
import java.util.stream.Stream;

/**
 * The class provides a {@link Template} view of the resources in a given root {@link Path}.
 * The root is expected to contain a project file named <code>project.json</code> and a folder
 * called <code>template</code> containing the actual template files.
 *
 * This path can be a local file system directory or a path in a ZipFileSystem.
 */
public class FileSystemTemplate implements Template {
    /**
     * Provides a {@link File} model for a path
     */
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

    /**
     * Provides a {@link Directory} model of a path
     */
    static class FsDirectory implements Directory {
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

    /**
     * The root directory containing the actual template
     */
    private final FsDirectory template;
    /**
     * The JSON data from the 'project.json' file
     */
    private final JsonObject projectSettings;

    public static Template fromZip(Path pathToZip) throws IOException {
        return fromZip(pathToZip, ".");
    }

    public static Template fromZip(Path pathToZip, String root) throws IOException {
        FileSystem zipFileSystem = FileSystems.newFileSystem(pathToZip, Main.class.getClassLoader());
        Path zipRoot = zipFileSystem.getPath(root);
        return new FileSystemTemplate(zipRoot);
    }

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
