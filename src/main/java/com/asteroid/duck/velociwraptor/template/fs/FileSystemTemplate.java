package com.asteroid.duck.velociwraptor.template.fs;

import com.asteroid.duck.velociwraptor.Main;
import com.asteroid.duck.velociwraptor.template.Directory;
import com.asteroid.duck.velociwraptor.template.Template;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.IOException;
import java.nio.file.*;

/**
 * The class provides a {@link Template} view of the resources in a given root {@link Path}.
 * The root is expected to contain a project file named <code>project.json</code> and a folder
 * called <code>template</code> containing the actual template files.
 *
 * This path can be a local file system directory or a path in a ZipFileSystem.
 */
public class FileSystemTemplate implements Template {

    /**
     * The root directory containing the actual template
     */
    private final FsDirectory template;
    /**
     * The JSON data from the 'default.json' file
     */
    private final JsonObject defaultValues;

    public static Template fromZip(Path pathToZip) throws IOException {
        return fromZip(pathToZip, ".");
    }

    public static Template fromZip(Path pathToZip, String root) throws IOException {
        FileSystem zipFileSystem = FileSystems.newFileSystem(pathToZip, Main.class.getClassLoader());
        Path zipRoot = zipFileSystem.getPath(root);
        return new FileSystemTemplate(zipRoot);
    }

    public FileSystemTemplate(Path root) throws IOException {
        Path projectFile = root.resolve("default.json");
        if (Files.exists(projectFile)) {
            JsonReader reader = Json.createReader(Files.newBufferedReader(projectFile));
            defaultValues = reader.readObject();
        }
        else {
            defaultValues = null;
        }

        Path templatePath = root.resolve("template");
        if (Files.exists(templatePath)) {
            this.template = new FsDirectory(templatePath);
        }
        else {
            this.template = new FsDirectory(root);
        }
    }

    @Override
    public Directory rootDirectory() {
        return template;
    }

    @Override
    public JsonObject projectSettings() {
        return defaultValues;
    }
}
