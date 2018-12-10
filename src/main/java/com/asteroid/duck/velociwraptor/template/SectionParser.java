package com.asteroid.duck.velociwraptor.template;

import org.apache.commons.io.IOUtils;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class SectionParser implements Closeable {
    public static final String END_TEMPLATE = "#end-template";
    public static final String BEGIN_TEMPLATE = "#begin-template";
    ArrayList<TemplateSection> sections = new ArrayList<>();
    private String currentSection = "";
    private boolean isTemplate = true;

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

    private void beginSection(boolean isTemplate) {
        this.isTemplate = isTemplate;
        currentSection = "";
    }

    private void endSection() {
        if (currentSection != null && currentSection.length() > 0) {
            TemplateSection newSection = new TemplateSection(isTemplate, currentSection);
            sections.add(newSection);
        }
    }

    public void parseLine(String line) {
        if (line.startsWith(END_TEMPLATE)) {
            endSection();
            beginSection(false);
        }
        else if (line.startsWith(BEGIN_TEMPLATE)) {
            endSection();
            beginSection(true);
        }
        else {
            currentSection += line;
            currentSection += "\n";
        }
    }

    public void close() {
        endSection();
    }
}
