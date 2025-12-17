package com.asteroid.duck.velociwraptor.template.fs;

import com.asteroid.duck.velociwraptor.template.TemplateDirectory;
import com.asteroid.duck.velociwraptor.template.TemplateFile;
import com.asteroid.duck.velociwraptor.template.TemplateNode;
import com.asteroid.duck.velociwraptor.template.visit.TemplateNodeVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Provides a {@link TemplateDirectory} model of a path
 */
class FsTemplateDirectory implements TemplateDirectory {
    private static final Logger log = LoggerFactory.getLogger(FsTemplateDirectory.class);
    private final Path path;

    FsTemplateDirectory(Path path) {
        this.path = path;
    }

    @Override
    public String rawName() {
        // need to trim '/' from ZIP paths...
        return path.getFileName().toString().replace("/", "");
    }

    @Override
    public Stream<TemplateFile> childFiles() {
        return stream(Files::isRegularFile, FsTemplateFile::new);
    }

    @Override
    public Stream<TemplateDirectory> childDirs()  {
       return stream(Files::isDirectory, FsTemplateDirectory::new);
    }

    /**
     * Wraps a DirectoryStream in a Stream that:
     *  - closes the DirectoryStream when the Stream is closed
     *  - closes the DirectoryStream when iteration exhausts the entries
     */
    private <T> Stream<T> stream(Predicate<Path> pathFilter, Function<Path, T> mapper) {
        try {
            final DirectoryStream<Path> ds = Files.newDirectoryStream(path);

            Spliterator<Path> spl = new Spliterators.AbstractSpliterator<Path>(Long.MAX_VALUE, Spliterator.ORDERED) {
                final Iterator<Path> it = ds.iterator();
                boolean closed = false;

                private void closeOnce() {
                    if (!closed) {
                        closed = true;
                        try {
                            ds.close();
                        } catch (IOException e) {
                            log.debug("Error closing directory stream for {}", path, e);
                        }
                    }
                }

                @Override
                public boolean tryAdvance(Consumer<? super Path> action) {
                    if (it.hasNext()) {
                        action.accept(it.next());
                        return true;
                    } else {
                        closeOnce();
                        return false;
                    }
                }

                @Override
                public void forEachRemaining(Consumer<? super Path> action) {
                    while (it.hasNext()) {
                        action.accept(it.next());
                    }
                    closeOnce();
                }
            };

            return StreamSupport.stream(spl, false)
                    .filter(pathFilter)
                    .map(mapper)
                    .onClose(() -> {
                        try {
                            ds.close();
                        } catch (IOException e) {
                            log.debug("Directory stream already closed or failed to close for {}", path, e);
                        }
                    });

        } catch (IOException e) {
            log.error("Error reading template directory", e);
            return Stream.empty();
        }
    }

    public void accept(TemplateNodeVisitor visitor) {
        if (visitor != null) {
            visitor.visitDirectory(this);
            childNodes().forEach(node -> node.accept(visitor));
        }
    }
}
