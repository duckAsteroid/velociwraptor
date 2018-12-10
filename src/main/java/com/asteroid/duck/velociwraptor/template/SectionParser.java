package com.asteroid.duck.velociwraptor.template;

import org.apache.commons.io.IOUtils;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class SectionParser implements Closeable {
    enum Section {
        BEGIN_TEMPLATE("#begin-template"),
        NO_TEMPLATE("#no-template"),
        END_TEMPLATE("#end-template");

        private final String lineStart;

        Section(String lineStart) {
            this.lineStart = lineStart;
        }

        public static Section fromLine(String line) {
            if (line != null) {
                for(Section s : values()) {
                    if (line.startsWith(s.lineStart)) {
                        return s;
                    }
                }
            }
            return null;
        }
    }

    ArrayList<TemplateSection> sections = new ArrayList<>();
    private String currentSection = "";
    private Section current = Section.BEGIN_TEMPLATE;

    public static List<TemplateSection> parse(InputStream rawContent) throws IOException {
        List<String> lines = IOUtils.readLines(rawContent, Charset.defaultCharset());
        try(SectionParser parser = new SectionParser()) {
            for (String line : lines) {
                parser.parseLine(line);
            }
            return parser.getSections();
        }
    }

    private List<TemplateSection> getSections() {
        return sections;
    }

    private void beginSection(Section newSection) {
        this.current = newSection;
        currentSection = "";
    }

    private void endSection() {
        if (currentSection != null && currentSection.length() > 0) {
            boolean isTemplate = current == Section.BEGIN_TEMPLATE;
            TemplateSection newSection = new TemplateSection(isTemplate, currentSection);
            sections.add(newSection);
        }
    }

    public void parseLine(String line) {
        Section section = Section.fromLine(line);
        if (section == null) {
            if (current != Section.NO_TEMPLATE) {
                if (!currentSection.isEmpty()) {
                    currentSection += "\n";
                }
                currentSection += line;
            }
        }
        else if (section != current) {
            beginSection(section);
        }
    }

    public void close() {
        endSection();
    }
}
