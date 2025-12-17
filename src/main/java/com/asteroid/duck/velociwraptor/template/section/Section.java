package com.asteroid.duck.velociwraptor.template.section;

import java.util.Optional;
import java.util.stream.Stream;

enum Section {
    BEGIN_TEMPLATE("#begin-template"),
    NO_TEMPLATE("#no-template"),
    END_TEMPLATE("#end-template");

    private final String lineStart;

    Section(String lineStart) {
        this.lineStart = lineStart;
    }

    public static Optional<Section> fromLine(String line) {
        if (line == null) return Optional.empty();
        return Stream.of(values())
                .filter(section -> line.startsWith(section.lineStart))
                .findFirst();
    }
}
