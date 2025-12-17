package com.asteroid.duck.velociwraptor.template.fs;

import com.asteroid.duck.velociwraptor.Velociwraptor;
import com.asteroid.duck.velociwraptor.model.vars.JsonValueSource;
import com.asteroid.duck.velociwraptor.model.vars.ValueProvider;
import com.asteroid.duck.velociwraptor.model.vars.ValueSource;
import com.asteroid.duck.velociwraptor.template.TemplateDirectory;
import com.asteroid.duck.velociwraptor.template.TemplateRoot;
import com.asteroid.duck.velociwraptor.template.visit.TemplateNodeVisitor;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The class provides a {@link TemplateRoot} view of the resources in a given root {@link Path}.
 * The root is expected to contain a project file named <code>project.json</code> and a folder
 * called <code>template</code> containing the actual template files.
 *
 * This path can be a local file system directory or a path in a ZipFileSystem.
 */
public class FileSystemTemplateRoot implements TemplateRoot {
    private interface CleanupAction {
        void cleanup() throws Exception;
    }
    /**
     * The root directory containing the actual template
     */
    private final FsTemplateDirectory template;
    /**
     * The JSON data from the 'default.json' file
     */
    private final JsonValueSource defaultValues;

    private CleanupAction cleanup = null;

    private final static ObjectMapper mapper = new ObjectMapper();

    public static TemplateRoot fromZip(Path pathToZip) throws IOException {
        return fromZip(pathToZip, ".");
    }

    @SuppressWarnings("resource") // TemplateRoot is AutoCloseable
    public static TemplateRoot fromZip(Path pathToZip, String root) throws IOException {
        FileSystem zipFileSystem = FileSystems.newFileSystem(pathToZip, Velociwraptor.class.getClassLoader());
        Path zipRoot = zipFileSystem.getPath(root);
        return new FileSystemTemplateRoot(zipRoot).setCleanup(zipFileSystem::close);
    }

    public FileSystemTemplateRoot(Path root) throws IOException {
        Path projectFile = root.resolve("default.json");
        if (Files.exists(projectFile)) {
            var jsonObject = mapper.readTree(Files.newInputStream(projectFile));
            defaultValues = new JsonValueSource(ValueSource.TEMPLATE, "default.json", jsonObject);
        }
        else {
            defaultValues = new JsonValueSource(ValueSource.TEMPLATE, "default.json", mapper.createObjectNode());
        }

        Path templatePath = root.resolve("template");
        if (Files.exists(templatePath)) {
            this.template = new FsTemplateDirectory(templatePath);
        }
        else {
            this.template = new FsTemplateDirectory(root);
        }
    }

    @Override
    public TemplateDirectory rootDirectory() {
        return template;
    }

    @Override
    public ValueProvider projectSettings() {
        return defaultValues;
    }

    private TemplateRoot setCleanup(CleanupAction cleanup) {
        this.cleanup = cleanup;
        return this;
    }

    @Override
    public void close() throws Exception {
        if (cleanup != null) {
            cleanup.cleanup();
        }
    }

    @Override
    public void accept(TemplateNodeVisitor visitor) {
        if (visitor != null) {
            visitor.visitRoot(this);
            template.accept(visitor);
        }
    }
}
