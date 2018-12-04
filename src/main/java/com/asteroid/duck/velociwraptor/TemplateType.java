package com.asteroid.duck.velociwraptor;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum TemplateType {
    DIR, GIT, ZIP;

    public static String listTemplateTypes() {
        return Arrays.asList(values()).stream()
                .map(template -> template.name())
                .collect(Collectors.joining(","));
    }
}
