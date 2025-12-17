package com.asteroid.duck.velociwraptor.template.section;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class SectionParser implements Iterator<TemplateSection>, Closeable {
    private static final Logger log = LoggerFactory.getLogger(SectionParser.class);
    private final BufferedReader reader;
    private final ArrayDeque<TemplateSection> queue = new ArrayDeque<>();
    private StringBuilder currentSection = new StringBuilder();
    private Section current = Section.BEGIN_TEMPLATE;
    private boolean eof = false;
    private boolean closed = false;

    public SectionParser(Reader reader) {
        this.reader = new BufferedReader(reader);
    }

    public SectionParser(Path p) throws IOException {
        this.reader = Files.newBufferedReader(p, Charset.defaultCharset());
    }

    public static Stream<TemplateSection> stream(Path p) {
        try {
            return stream(new SectionParser(p));
        } catch (IOException e) {
            log.error("Error creating SectionParser for path: {}", p, e);
        }
        return Stream.empty();
    }

    public static Stream<TemplateSection> stream(SectionParser parser) {
        Spliterator<TemplateSection> spl = Spliterators.spliteratorUnknownSize(parser,
                Spliterator.ORDERED | Spliterator.NONNULL);
        return StreamSupport.stream(spl, false).onClose(() -> {
            try {
                parser.close();
            } catch (IOException e) {
                log.error("Error closing parser", e);
            }
        });
    }

    public static List<TemplateSection> parse(Path p) throws IOException {
        try (Stream<TemplateSection> s = stream(p)) {
            return s.collect(Collectors.toList());
        }
    }

    private void endSectionIfAny() {
        if (currentSection != null && !currentSection.isEmpty()) {
            boolean isTemplate = current == Section.BEGIN_TEMPLATE;
            TemplateSection newSection = new TemplateSection(isTemplate, currentSection.toString());
            queue.addLast(newSection);
            currentSection = new StringBuilder();
        }
    }

    private void consumeUntilSectionProduced() throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            Optional<Section> maybe = Section.fromLine(line);
            if (maybe.isPresent()) {
                Section newSection = maybe.get();
                if (newSection != current) {
                    // flush accumulated text as a section, then switch mode
                    endSectionIfAny();
                    current = newSection;
                    currentSection = new StringBuilder();
                }
                // if marker equals current, ignore
            } else {
                if (current != Section.NO_TEMPLATE) {
                    if (!currentSection.isEmpty()) currentSection.append("\n");
                    currentSection.append(line);
                }
            }
            if (!queue.isEmpty()) return; // we produced a section
        }
        // EOF reached
        endSectionIfAny();
        eof = true;
    }

    @Override
    public boolean hasNext() {
        if (!queue.isEmpty()) return true;
        if (eof) return false;
        try {
            consumeUntilSectionProduced();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return !queue.isEmpty();
    }

    @Override
    public TemplateSection next() {
        if (!hasNext()) throw new java.util.NoSuchElementException();
        return queue.removeFirst();
    }

    boolean isClosed() {
        return closed;
    }

    @Override
    public void close() throws IOException {
        if (!closed) {
            closed = true;
            reader.close();
        }
    }
}
