package com.asteroid.duck.velociwraptor.template.fs;

import com.asteroid.duck.velociwraptor.template.TemplateFile;
import com.asteroid.duck.velociwraptor.template.TemplateNode;
import com.asteroid.duck.velociwraptor.template.section.SectionParser;
import com.asteroid.duck.velociwraptor.template.section.TemplateSection;
import com.asteroid.duck.velociwraptor.template.visit.TemplateNodeVisitor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Provides a {@link TemplateFile} model for a path
 */
class FsTemplateFile implements TemplateFile {
    private final Path path;

    FsTemplateFile(Path path) {
        this.path = path;
    }

    @Override
    public String rawName() {
        return path.getFileName().toString();
    }

    public Path templateContentPath() {
        return path;
    }

    @Override
    public InputStream rawContent() throws IOException {
        return Files.newInputStream(path);
    }

    @Override
    public Stream<TemplateSection> childNodes () {
        return SectionParser.stream(path);
    }

    @Override
    public void accept(TemplateNodeVisitor visitor) {
        if (visitor != null) {
            visitor.visitFile(this);
            childNodes().forEach(node -> node.accept(visitor));
        }
    }
}
